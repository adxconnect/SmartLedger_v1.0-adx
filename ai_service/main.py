import os
import json
import tempfile
import re
import pandas as pd
import pypdf
from typing import List, Optional
from fastapi import FastAPI, UploadFile, File, HTTPException, Form, Body
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
import google.generativeai as genai

# Load API Key from Environment
GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY", "")
if GEMINI_API_KEY:
    genai.configure(api_key=GEMINI_API_KEY)

app = FastAPI(
    title="SmartLedger AI & RAG Virtual CA Service",
    description="Microservice for PDF Bank Statement Parsing, Transaction Categorization, and Personal CA Advisory",
    version="1.0.0"
)

# Enable CORS for React Frontend (localhost:5173) and Java Backend (localhost:8080)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- Pydantic Schemas ---
class ExtractedTransaction(BaseModel):
    date: str = Field(..., description="Transaction date in YYYY-MM-DD format")
    description: str = Field(..., description="Cleaned merchant or transaction description")
    amount: float = Field(..., description="Numeric amount")
    type: str = Field(..., description="Either DEBIT or CREDIT")
    category: Optional[str] = Field("General", description="Expense category e.g. Food, Salary, Rent, Investment")
    taxSection: Optional[str] = Field(None, description="Tax section if applicable e.g. 80C, 80D, Business Expense")
    refId: Optional[str] = Field(None, description="Unique transaction reference ID / UTR / Ref ID")

class CAAdvisorRequest(BaseModel):
    userQuery: str = Field(..., description="User's question to the Virtual CA")
    annualIncome: float = Field(0.0, description="Total annual income in INR")
    investments80C: float = Field(0.0, description="Total 80C investments (PPF, ELSS, LIC, etc.)")
    healthInsurance80D: float = Field(0.0, description="Total 80D health insurance premium paid")
    categoryBreakdown: dict = Field(default_factory=dict, description="Dictionary of Category -> Total Amount Spent")
    portfolioContext: Optional[dict] = Field(default_factory=dict, description="Complete user financial data across Overview, Transactions, Live Investments, Len-Den, and Loans")

# --- Static CA Tax Knowledge Base (Built-in RAG Rules for Zero Cost) ---
INDIAN_TAX_KNOWLEDGE_BASE = """
### CHARTERED ACCOUNTANT STATUTORY TAX RULES (INDIA):
1. Section 80C: Maximum deduction allowed is ₹1,50,000 per financial year. Eligible investments include PPF, ELSS Mutual Funds, EPF, Life Insurance Premium (LIC), Principal Repayment of Home Loan, and Tuition Fees.
2. Section 80D: Deduction for Health Insurance Premium paid:
   - For Self, Spouse, and Dependent Children: Up to ₹25,000 (₹50,000 if senior citizen).
   - For Parents: Additional up to ₹25,000 (₹50,000 if parents are senior citizens).
3. Section 80CCD(1B): Additional deduction of ₹50,000 for contributions to National Pension System (NPS), over and above the ₹1.5 Lakh 80C limit.
4. Old vs New Tax Regime:
   - New Tax Regime (Default from FY 2023-24): Standard deduction of ₹75,000. Most deductions under 80C/80D are NOT allowed in the New Tax Regime.
   - Old Tax Regime: Allows full utilization of 80C, 80D, HRA, and Home Loan Interest deductions.
"""

def extract_transactions_from_text(text: str) -> List[ExtractedTransaction]:
    """
    Local regex/NLP heuristic engine to extract transactions from PDF bank statement text.
    Ensures real transactions are fetched from the document even without an external API key.
    """
    transactions = []
    last_balance = None
    if not text or not text.strip():
        return transactions

    lines = text.split("\n")
    date_regex = re.compile(
        r"\b(\d{4}[-/.]\d{1,2}[-/.]\d{1,2}|\d{1,2}[-/.]\d{1,2}[-/.]\d{2,4}|\d{1,2}[-/.\s]+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*[-/.\s]+\d{2,4})\b",
        re.IGNORECASE
    )
    amount_regex = re.compile(
        r"\b(\d{1,3}(?:,\d{2,3})*(?:\.\d{2})|\d+(?:\.\d{2}))\b"
    )

    for line in lines:
        line_clean = line.strip()
        if not line_clean or len(line_clean) < 8:
            continue

        date_match = date_regex.search(line_clean)
        if not date_match:
            if transactions and len(line_clean) < 80 and not amount_regex.search(line_clean):
                extra_desc = re.sub(r"\b(CR|DR|Cr|Dr|INR|Rs\.?|₹|Debit|Credit)\b", "", line_clean)
                extra_desc = re.sub(r"\s+", " ", extra_desc).strip(" -/,.*#|:")
                if extra_desc:
                    transactions[-1].description = (transactions[-1].description + " " + extra_desc)[:100]
            continue

        raw_date = date_match.group(1)
        normalized_date = raw_date
        month_map = {
            "jan": "01", "feb": "02", "mar": "03", "apr": "04",
            "may": "05", "jun": "06", "jul": "07", "aug": "08",
            "sep": "09", "oct": "10", "nov": "11", "dec": "12"
        }
        try:
            parts = re.split(r"[-/.\s]+", raw_date.strip())
            if len(parts) == 3:
                m_str = parts[1].lower()[:3]
                m_num = month_map.get(m_str, None)
                if not m_num and parts[1].isdigit():
                    m_num = f"{int(parts[1]):02d}"
                
                if m_num and len(parts[2]) == 4 and parts[0].isdigit() and int(parts[0]) <= 31:
                    normalized_date = f"{parts[2]}-{m_num}-{int(parts[0]):02d}"
                elif m_num and len(parts[0]) == 4 and parts[2].isdigit() and int(parts[2]) <= 31:
                    normalized_date = f"{parts[0]}-{m_num}-{int(parts[2]):02d}"
        except Exception:
            normalized_date = raw_date

        line_no_date = line_clean[:date_match.start()] + " " + line_clean[date_match.end():]
        amount_matches = amount_regex.findall(line_no_date)

        valid_amounts = []
        for amt_str in amount_matches:
            try:
                val = float(amt_str.replace(",", ""))
                if 0 < val < 50000000:
                    valid_amounts.append((val, amt_str))
            except ValueError:
                continue

        if not valid_amounts:
            continue

        if len(valid_amounts) >= 2:
            closing_balance = valid_amounts[-1][0]
            amount_val = valid_amounts[-2][0]
            amt_str_raw = valid_amounts[-2][1]
        else:
            closing_balance = None
            amount_val = valid_amounts[0][0]
            amt_str_raw = valid_amounts[0][1]

        txn_type = None
        if closing_balance is not None and last_balance is not None:
            if closing_balance > last_balance:
                txn_type = "CREDIT"
            elif closing_balance < last_balance:
                txn_type = "DEBIT"
        elif closing_balance is not None and last_balance is None:
            # First row deduction
            line_lower = line_clean.lower()
            if any(w in line_lower for w in ["wdl", "dr", "debit", "withdrawal"]):
                txn_type = "DEBIT"
            elif any(w in line_lower for w in ["dep", "cr", "credit", "deposit"]):
                txn_type = "CREDIT"
            elif abs(closing_balance - amount_val) < 1.0:
                txn_type = "CREDIT"
            elif line.find(amt_str_raw) > len(line) // 2 and "   " in line[line.find(amt_str_raw)-6:line.find(amt_str_raw)]:
                # If there's a big gap before the first amount, it's likely a deposit (credit) since withdrawal is skipped
                txn_type = "CREDIT"
        
        if closing_balance is not None:
            last_balance = closing_balance
        
        if not txn_type:
            line_lower = line_clean.lower()
            if any(w in line_lower for w in [" cr", "credit", "dep", "deposit", "salary", "refund", "interest credit", "received"]):
                txn_type = "CREDIT"
            elif any(w in line_lower for w in [" dr", "debit", "withdrawal", "paid", "sent", "pos*", "upi/", "atm "]):
                txn_type = "DEBIT"
            else:
                txn_type = "CREDIT" if ("+" in line_no_date) else "DEBIT"

        ref_regex = re.compile(
            r"(MB\d{10,16}|(?:UTR|REF|CHQ|UPI|NEFT|IMPS|RTGS|TXN|CMS|INF)[A-Z0-9/-]{6,25}|\b[A-Z]{2,4}\d{8,16}\b|\b\d{12,22}\b)",
            re.IGNORECASE
        )
        ref_matches = ref_regex.findall(line_no_date)
        ref_id = " / ".join(ref_matches) if ref_matches else None

        desc = line_no_date
        for _, raw_amt in valid_amounts:
            desc = desc.replace(raw_amt, "")
        if ref_matches:
            for r_val in ref_matches:
                desc = desc.replace(r_val, "")
        desc = re.sub(r"\b\d{10,22}\b", "", desc)
        desc = re.sub(r"\b\d{1,2}[-/.]\d{1,2}(?:[-/.]\d{2,4})?\b", "", desc)
        desc = re.sub(r"\b(CR|DR|Cr|Dr|INR|Rs\.?|₹|Debit|Credit)\b", "", desc)
        desc = re.sub(r"\s+", " ", desc).strip(" -/,.*#|:")

        if not desc or len(desc) < 2:
            desc = "Bank Transaction"

        desc_lower = desc.lower()
        category = "General"
        tax_section = None
        if any(k in desc_lower for k in ["salary", "payroll", "stipend"]):
            category = "Salary"
        elif any(k in desc_lower for k in ["rent", "housing", "landlord", "hra"]):
            category = "Rent"
            tax_section = "10(13A) HRA"
        elif any(k in desc_lower for k in ["lic", "ppf", "elss", "mutual fund", "tax saver", "insurance", "nps", "sip"]):
            category = "Investment"
            tax_section = "80C"
        elif any(k in desc_lower for k in ["star health", "health insurance", "mediclaim", "hospital", "pharmacy", "medical"]):
            category = "Health"
            if "insurance" in desc_lower or "mediclaim" in desc_lower:
                tax_section = "80D"
        elif any(k in desc_lower for k in ["zomato", "swiggy", "restaurant", "cafe", "food", "dining", "starbucks", "mcdonald", "kfc", "domino"]):
            category = "Food & Dining"
        elif any(k in desc_lower for k in ["amazon", "flipkart", "myntra", "shopping", "store", "supermarket", "mart", "retail", "reliance"]):
            category = "Shopping"
        elif any(k in desc_lower for k in ["uber", "ola", "fuel", "petrol", "diesel", "metro", "railway", "flight", "cab"]):
            category = "Transport"
        elif any(k in desc_lower for k in ["electricity", "water", "gas", "wifi", "broadband", "recharge", "airtel", "jio", "bill"]):
            category = "Utilities"

        transactions.append(
            ExtractedTransaction(
                date=normalized_date,
                description=desc[:50],
                amount=amount_val,
                type=txn_type,
                category=category,
                taxSection=tax_section,
                refId=ref_id
            )
        )

    return transactions

def enhance_descriptions_with_ai(transactions: List[ExtractedTransaction]) -> List[ExtractedTransaction]:
    if not GEMINI_API_KEY or not GEMINI_API_KEY.startswith("AIza"):
        return transactions

    try:
        model = genai.GenerativeModel("gemini-1.5-flash")
        unique_descs = list(set([t.description for t in transactions]))
        desc_mapping = {}
        
        batch_size = 100
        for i in range(0, len(unique_descs), batch_size):
            batch = unique_descs[i:i+batch_size]
            prompt = f"""
            You are a financial AI assistant. Rewrite these raw bank transaction descriptions into clean, detailed, and human-readable formats.
            - Keep important details like payee names, merchant names, locations, and platforms.
            - Remove unnecessary banking jargon (e.g., 'WDL TFR', 'DEP TFR') ONLY IF there are other identifying details. If there are no other details, expand them (e.g., 'WDL TFR' -> 'Withdrawal Transfer').
            - Fix capitalizations to Title Case.
            
            Return ONLY a valid JSON object mapping the exact original string to the detailed string.
            Example: {{"WDL TFR TO MR SMITH": "Transfer to Mr Smith", "POS 1234 AMAZON": "Amazon Purchase"}}
            
            Raw descriptions:
            {json.dumps(batch)}
            """
            response = model.generate_content(prompt)
            raw_text = response.text.strip()
            if raw_text.startswith("```json"):
                raw_text = raw_text[7:-3].strip()
            elif raw_text.startswith("```"):
                raw_text = raw_text[3:-3].strip()
                
            try:
                mapping = json.loads(raw_text)
                desc_mapping.update(mapping)
            except Exception:
                pass

        for t in transactions:
            if t.description in desc_mapping:
                t.description = desc_mapping[t.description][:80]
    except Exception as e:
        print(f"AI description enhancement failed: {e}")
        
    return transactions

def identify_recurring_transactions(transactions: List[ExtractedTransaction]) -> List[ExtractedTransaction]:
    from datetime import datetime
    amount_groups = {}
    for t in transactions:
        amt = abs(t.amount)
        if amt not in amount_groups:
            amount_groups[amt] = []
        amount_groups[amt].append(t)
        
    for amt, txns in amount_groups.items():
        if len(txns) < 2:
            continue
            
        desc_groups = {}
        for t in txns:
            # Group by the first two words of the description to match identical senders
            words = tuple(t.description.lower().split()[:2]) if t.description else ("unknown",)
            if words not in desc_groups:
                desc_groups[words] = []
            desc_groups[words].append(t)
            
        for words, similar_txns in desc_groups.items():
            if len(similar_txns) >= 2:
                # Calculate gaps between transactions to confirm they are spaced out (e.g., >20 days)
                try:
                    sorted_txns = sorted(similar_txns, key=lambda x: datetime.strptime(x.date, "%Y-%m-%d") if x.date and len(x.date)==10 else datetime.min)
                    
                    is_recurring = False
                    for i in range(len(sorted_txns) - 1):
                        d1_str = sorted_txns[i].date
                        d2_str = sorted_txns[i+1].date
                        if d1_str and d2_str and len(d1_str)==10 and len(d2_str)==10:
                            d1 = datetime.strptime(d1_str, "%Y-%m-%d")
                            d2 = datetime.strptime(d2_str, "%Y-%m-%d")
                            gap = abs((d2 - d1).days)
                            if gap >= 20: # Monthly or more
                                is_recurring = True
                                break
                                
                    if is_recurring:
                        for st in similar_txns:
                            if st.type == 'DEBIT':
                                st.category = f"Recurring Subscription - {st.description[:15]}"
                            else:
                                st.category = f"Recurring Income - {st.description[:15]}"
                except Exception:
                    pass
    return transactions

@app.get("/api/ai/status")
def health_check():
    return {
        "status": "online",
        "service": "SmartLedger RAG & CA Service",
        "geminiApiKeyConfigured": bool(GEMINI_API_KEY)
    }

@app.post("/api/ai/parse-statement", response_model=List[ExtractedTransaction])
async def parse_statement(
    file: UploadFile = File(...),
    password: Optional[str] = Form(None)
):
    """
    Ingest a Bank Statement (PDF or CSV) and extract standardized transactions using AI.
    """
    if password == "":
        password = None

    filename = file.filename.lower()
    
    # 1. Handle CSV / Excel Files with Pandas
    if filename.endswith(".csv") or filename.endswith(".xlsx"):
        try:
            content = await file.read()
            with tempfile.NamedTemporaryFile(delete=False, suffix=".csv" if filename.endswith(".csv") else ".xlsx") as tmp:
                tmp.write(content)
                tmp_path = tmp.name
                
            df = pd.read_csv(tmp_path) if filename.endswith(".csv") else pd.read_excel(tmp_path)
            if os.path.exists(tmp_path):
                os.remove(tmp_path)
            
            transactions = []
            for _, row in df.iterrows():
                cols = [str(c).lower() for c in df.columns]
                date_val = str(row.iloc[0]) if len(row) > 0 else "2026-07-29"
                desc_val = str(row.iloc[1]) if len(row) > 1 else "Transaction"
                amount_val = float(row.iloc[2]) if len(row) > 2 and pd.notnull(row.iloc[2]) else 0.0
                
                transactions.append(
                    ExtractedTransaction(
                        date=date_val,
                        description=desc_val,
                        amount=abs(amount_val),
                        type="DEBIT" if amount_val < 0 else "CREDIT",
                        category="General",
                        taxSection=None
                    )
                )
            return transactions
        except Exception as e:
            raise HTTPException(status_code=400, detail=f"Error parsing CSV/Excel: {str(e)}")

    # 2. Handle PDF Bank Statements via pypdf + Gemini (or Local Extraction Engine)
    elif filename.endswith(".pdf"):
        try:
            content = await file.read()
            with tempfile.NamedTemporaryFile(delete=False, suffix=".pdf") as tmp:
                tmp.write(content)
                tmp_path = tmp.name

            extracted_text = ""
            try:
                reader = pypdf.PdfReader(tmp_path)
                if reader.is_encrypted:
                    if not password:
                        if os.path.exists(tmp_path):
                            os.remove(tmp_path)
                        raise HTTPException(
                            status_code=400,
                            detail="PDF is password-protected. Please enter the correct password to unlock and access this statement."
                        )
                    decrypted = reader.decrypt(password)
                    if decrypted == 0:
                        if os.path.exists(tmp_path):
                            os.remove(tmp_path)
                        raise HTTPException(
                            status_code=400,
                            detail="Incorrect PDF password. Please check the password and try again."
                        )

                for page in reader.pages:
                    page_text = page.extract_text(extraction_mode="layout")
                    if page_text:
                        extracted_text += page_text + "\n"
            except HTTPException:
                raise
            except Exception as e:
                print(f"Error reading PDF with pypdf: {e}")
                extracted_text = ""

            # Try Gemini AI if API key is configured and valid
            if GEMINI_API_KEY and GEMINI_API_KEY.startswith("AIza"):
                try:
                    model = genai.GenerativeModel("gemini-1.5-flash")
                    prompt = f"""
                    You are an expert Chartered Accountant and data extraction assistant.
                    Extract all financial transactions from the following bank statement text into a clean JSON array.
                    
                    CRITICAL INSTRUCTION FOR DEBIT/CREDIT CLASSIFICATION:
                    You MUST use BOTH the visual column alignment and the "Closing Balance" trend to correctly identify DEBIT vs CREDIT:
                    1. The text extraction preserves horizontal spacing. Look at the column headers ("Withdrawal", "Deposit", "Debit", "Credit") and see which column the amount aligns under.
                    2. VERIFY using the Closing Balance trend:
                       - If closing balance INCREASES compared to the previous row, it is a CREDIT (Money In).
                       - If closing balance DECREASES compared to the previous row, it is a DEBIT (Money Out).
                    3. For the VERY FIRST transaction (where there is no previous row to compare):
                       - Look extremely closely at the spacing. If the amount is positioned in the "Deposit" or "Credit" column, it is CREDIT.
                       - If the (Closing Balance - Amount) exactly equals 0 or a logical round opening balance, it is a CREDIT.
                       - DO NOT default to DEBIT. Use the visual columns and math to deduce it accurately.
                    4. Ignore confusing keywords in the description (like "UPI" or "Transfer") when deciding type. Math and columns are the source of truth.
                    
                    For each transaction, provide:
                    - date: in YYYY-MM-DD format
                    - description: clean readable merchant, payee, or transfer description WITHOUT any long reference numbers, UTRs, or cheque numbers
                    - amount: positive float number
                    - type: "DEBIT" for withdrawals/expenses, "CREDIT" for deposits/income
                    - category: Assign a logical category e.g., "Food", "Rent", "Salary", "Shopping", "Investment", "Health", "Utilities"
                    - taxSection: If the transaction qualifies for Indian tax deduction, put the section name e.g. "80C" (for LIC/PPF/ELSS), "80D" (for health insurance), else null.
                    - refId: Unique transaction reference ID, UTR, cheque number, or transaction ID (e.g. "MB2300713573", "0000000000576482", "UPI/12345678"). If none found, put null.
                    
                    Here is the text extracted from the bank statement:
                    {extracted_text[:15000]}
                    
                    Return ONLY a valid JSON array of objects. No markdown formatting outside the JSON array.
                    """
                    response = model.generate_content(prompt)
                    raw_text = response.text.strip()
                    if raw_text.startswith("```json"):
                        raw_text = raw_text[7:-3].strip()
                    elif raw_text.startswith("```"):
                        raw_text = raw_text[3:-3].strip()
                        
                    data = json.loads(raw_text)
                    if os.path.exists(tmp_path):
                        os.remove(tmp_path)
                    return [ExtractedTransaction(**item) for item in data]
                except Exception as gemini_err:
                    print(f"Gemini AI parsing failed, falling back to local statement extraction: {gemini_err}")

            if os.path.exists(tmp_path):
                os.remove(tmp_path)

            # Use local transaction extractor on actual PDF text (No dummy fallback!)
            parsed_txns = extract_transactions_from_text(extracted_text)
            if not parsed_txns:
                raise HTTPException(
                    status_code=400,
                    detail="Could not detect any transactions in this PDF. Please ensure the PDF contains selectable transaction text."
                )
            
            # Enhance with AI as requested
            parsed_txns = enhance_descriptions_with_ai(parsed_txns)
            
            # Identify recurring transactions
            parsed_txns = identify_recurring_transactions(parsed_txns)
            
            return parsed_txns
        except HTTPException:
            raise
        except Exception as e:
            raise HTTPException(status_code=400, detail=f"Error processing PDF: {str(e)}")
    else:
        raise HTTPException(status_code=400, detail="Unsupported file format. Please upload .pdf, .csv, or .xlsx")

@app.post("/api/ai/ca-advisor")
async def ca_advisor(request: CAAdvisorRequest):
    """
    RAG-powered Virtual Chartered Accountant endpoint.
    Takes user financial metrics + query and generates professional tax & advisory guidance.
    """
    # 0. Strict Non-Financial Query Filter (Only allow finance, tax, accounts, money, investments, etc.)
    q_lower = request.userQuery.lower()
    non_financial_keywords = [
        "cricket", "football", "movie", "song", "poem", "joke", "weather", 
        "recipe", "actor", "actress", "celebrity", "politics", "president", 
        "prime minister", "game", "gaming", "python code", "java code", 
        "love", "dating", "horoscope"
    ]
    if any(k in q_lower for k in non_financial_keywords):
        return {
            "advisorResponse": "### 🛑 Non-Financial Query Not Allowed\n\n**I am your SmartLedger Virtual Chartered Accountant.** I am strictly specialized in personal finance, taxation, Indian tax laws (Section 80C, 80D, Old vs New Regime), investment planning, budgeting, and SmartLedger accounts.\n\n*Please ask a question related to your finances, taxes, or transactions.*",
            "metricsAnalyzed": {"gap80C": 0.0, "gap80D": 0.0}
        }

    gap80C = max(0.0, 150000.0 - request.investments80C)
    gap80D = max(0.0, 25000.0 - request.healthInsurance80D)

    # 1. Check if Gemini API is available and valid
    if GEMINI_API_KEY and GEMINI_API_KEY.startswith("AIza"):
        try:
            model = genai.GenerativeModel("gemini-1.5-flash")
            
            system_prompt = f"""
            You are a highly experienced Chartered Accountant (CA) and Personal Financial Advisor in India for SmartLedger.
            
            CRITICAL MANDATE: Other than financial, tax, investment, expense, budgeting, account, overview, transaction, len-den, loan, and portfolio chat, NO OTHER TYPE OF CHATS ARE ALLOWED. If the user asks a non-financial question (e.g. general trivia, sports, poetry, coding, politics, jokes, etc.), you MUST politely refuse and state: "I am your SmartLedger Virtual Chartered Accountant. I only provide advice on personal finance, taxation, investments, and your financial accounts."
            
            {INDIAN_TAX_KNOWLEDGE_BASE}
            
            ### USER'S VERIFIED FINANCIAL PORTFOLIO (SMARTLEDGER FULL DATA ACCESS):
            - **Overview (Accounts & Deposits)**:
              {json.dumps((request.portfolioContext or {}).get('overview', {}), indent=2)}
            - **Transactions Summary & History**:
              {json.dumps((request.portfolioContext or {}).get('transactions', {}), indent=2)}
            - **Live Investments (Mutual Funds, Gold/Silver, Stocks)**:
              {json.dumps((request.portfolioContext or {}).get('liveInvestments', {}), indent=2)}
            - **Len-Den (Peer Lending & Borrowing)**:
              {json.dumps((request.portfolioContext or {}).get('lenDen', {}), indent=2)}
            - **Loans & EMI Obligations**:
              {json.dumps((request.portfolioContext or {}).get('loans', {}), indent=2)}
            - **Tax Profile & Annual Metrics**:
              * Annual Income: ₹{request.annualIncome:,.2f}
              * Section 80C Used: ₹{request.investments80C:,.2f} (Max limit: ₹1,50,000)
              * Section 80D Used: ₹{request.healthInsurance80D:,.2f} (Max limit: ₹25,000)
            
            ### INSTRUCTIONS:
            1. First check if the user's question is related to finance, tax, accounts, budgeting, investments, loans, len-den, overview, or money. If NOT, refuse immediately per CRITICAL MANDATE.
            2. If the user asks about their Overview, Bank Accounts, Deposits, Transactions, Live Investments, Len-Den, or Loans, ANSWER ACCURATELY using the verified portfolio data above!
            3. Provide specific numbers, account names, loan balances, investment totals, or len-den records as requested by the user.
            4. When analyzing transactions, actively track same-value payments made to or received from the same sender/merchant. Identify their frequency by calculating the gaps/intervals between transaction dates (e.g., monthly, quarterly, or irregular) and group these recurring transactions together (e.g., Subscriptions, EMIs, Salaries, or Rent).
            5. For tax queries, calculate remaining gaps under 80C/80D and recommend tax-saving strategies.
            6. Format your response in clean GitHub Markdown with bullet points, bold highlights, and relevant emojis.
            """
            
            full_prompt = f"{system_prompt}\n\nUser Question: {request.userQuery}"
            response = model.generate_content(full_prompt)
            
            return {
                "advisorResponse": response.text,
                "metricsAnalyzed": {
                    "gap80C": gap80C,
                    "gap80D": gap80D
                }
            }
        except Exception as e:
            print(f"Gemini API error in CA Advisor, falling back to statutory rule engine: {str(e)}")

    # 2. Zero-Config Smart Portfolio & Statutory Rule Engine Fallback (Works without API Key)
    ctx = request.portfolioContext or {}
    ov = ctx.get("overview", {})
    tx = ctx.get("transactions", {})
    inv = ctx.get("liveInvestments", {})
    lenden = ctx.get("lenDen", {})
    lns = ctx.get("loans", {})

    is_overview_q = any(k in q_lower for k in ["overview", "balance", "bank", "account", "deposit", "fd", "rd", "cash", "total money"])
    is_tx_q = any(k in q_lower for k in ["transaction", "spent", "spend", "expense", "debit", "credit", "category", "food", "recharge", "bill"])
    is_inv_q = any(k in q_lower for k in ["invest", "mutual fund", "mf", "gold", "silver", "stock", "portfolio", "sip"])
    is_lenden_q = any(k in q_lower for k in ["len", "den", "lenden", "owe", "lent", "borrow", "peer", "friend", "gave", "took"])
    is_loan_q = any(k in q_lower for k in ["loan", "emi", "debt", "principal", "lender", "interest"])

    response_sections = []
    response_sections.append(f"### ✨ Ledger AI — Comprehensive Portfolio Answer\n\n**Question:** *\" {request.userQuery} \"*\n")

    show_all = not (is_overview_q or is_tx_q or is_inv_q or is_lenden_q or is_loan_q)

    if is_overview_q or show_all:
        bank_bal = ov.get('totalBankBalance', 0.0)
        dep_bal = ov.get('totalDepositBalance', 0.0)
        banks_list = ov.get('bankAccountsList', [])
        deps_list = ov.get('depositsList', [])
        ov_text = f"#### 🏛️ 1. Overview (Bank Accounts & Deposits)\n"
        ov_text += f"* **Total Bank Balance:** ₹{bank_bal:,.2f} | **Total Deposit Principal:** ₹{dep_bal:,.2f} | **Net Liquid Balance:** ₹{(bank_bal + dep_bal):,.2f}\n"
        if banks_list:
            ov_text += f"* **Active Bank Accounts:** " + "; ".join(banks_list) + "\n"
        if deps_list:
            ov_text += f"* **Fixed & Recurring Deposits:** " + "; ".join(deps_list) + "\n"
        response_sections.append(ov_text)

    if is_tx_q or show_all:
        tx_count = tx.get('totalCount', 0)
        total_exp = tx.get('totalExpense', 0.0)
        total_inc = tx.get('totalIncome', 0.0)
        groups = tx.get('categoryGroupsSpent', {})
        tx_text = f"#### 💳 2. Transactions & Cashflow Summary\n"
        tx_text += f"* **Total Transactions Logged:** {tx_count} | **Total Inflow (Credit):** ₹{total_inc:,.2f} | **Total Outflow (Debit):** ₹{total_exp:,.2f}\n"
        if groups:
            tx_text += f"* **Category Group Breakdown:** " + ", ".join([f"**{g}:** ₹{amt:,.2f}" for g, amt in groups.items()]) + "\n"
        response_sections.append(tx_text)

    if is_inv_q or show_all:
        inv_tot = inv.get('totalInvestedValue', 0.0)
        mf_tot = inv.get('mutualFundsTotal', 0.0)
        gold_tot = inv.get('goldSilverTotal', 0.0)
        items = inv.get('items', [])
        inv_text = f"#### 📈 3. Live Investments (Mutual Funds, Gold & Silver)\n"
        inv_text += f"* **Total Invested Portfolio Value:** ₹{inv_tot:,.2f}\n"
        inv_text += f"* **Mutual Funds Total:** ₹{mf_tot:,.2f} | **Gold & Silver Assets:** ₹{gold_tot:,.2f}\n"
        if items:
            inv_text += f"* **Key Holdings:** " + "; ".join(items[:5]) + ("..." if len(items) > 5 else "") + "\n"
        response_sections.append(inv_text)

    if is_lenden_q or show_all:
        lent_tot = lenden.get('totalLent', 0.0)
        borrowed_tot = lenden.get('totalBorrowed', 0.0)
        records = lenden.get('records', [])
        ld_text = f"#### 🤝 4. Len-Den (Peer Lending & Borrowings)\n"
        ld_text += f"* **Total Amount Lent (To Receive):** ₹{lent_tot:,.2f} | **Total Amount Borrowed (To Pay):** ₹{borrowed_tot:,.2f}\n"
        if records:
            ld_text += f"* **Active Len-Den Records:** " + "; ".join(records) + "\n"
        else:
            ld_text += f"* *No active peer Len-Den records logged.* \n"
        response_sections.append(ld_text)

    if is_loan_q or show_all:
        rem_princ = lns.get('totalRemainingPrincipal', 0.0)
        monthly_emi = lns.get('totalMonthlyEMI', 0.0)
        loan_list = lns.get('loansList', [])
        ln_text = f"#### 🏦 5. Loans & Monthly EMI Obligations\n"
        ln_text += f"* **Total Remaining Loan Principal:** ₹{rem_princ:,.2f} | **Total Monthly EMI Burden:** ₹{monthly_emi:,.2f}\n"
        if loan_list:
            ln_text += f"* **Active Loans:** " + "; ".join(loan_list) + "\n"
        else:
            ln_text += f"* *No active loan liabilities logged.* \n"
        response_sections.append(ln_text)

    tax_text = f"#### 🏛️ 6. Statutory Tax Deduction Gap Analysis (FY 2025–26)\n"
    tax_text += f"* **Section 80C Limit:** ₹1,50,000 | **Current Utilization:** ₹{request.investments80C:,.2f} | **Remaining Gap:** **₹{gap80C:,.2f}**\n"
    tax_text += f"* **Section 80D Limit:** ₹25,000 | **Current Utilization:** ₹{request.healthInsurance80D:,.2f} | **Remaining Gap:** **₹{gap80D:,.2f}**\n\n"
    tax_text += f"> [!TIP]\n> **Ledger AI Insight:** All your modules (**Overview, Transactions, Live Investments, Len-Den, Loans**) are actively synced. You can ask specific questions like *'How much did I spend on food?'* or *'What is my bank balance?'*"
    response_sections.append(tax_text)

    advisor_text = "\n\n---\n\n".join(response_sections)
    return {
        "advisorResponse": advisor_text,
        "metricsAnalyzed": {
            "gap80C": gap80C,
            "gap80D": gap80D
        }
    }

if __name__ == "__main__":
    import uvicorn
    print("Starting SmartLedger AI & RAG Microservice on port 8000...")
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
