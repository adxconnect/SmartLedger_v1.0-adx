# SmartLedger AI & RAG Microservice Setup Guide ($0 Cost)

This folder contains your Python-based **AI & RAG Virtual CA Microservice** using the **free Google Gemini 1.5 Flash API**.

---

## 🚀 Step 1: Get Your Free API Key (Takes 1 Minute, Zero Cost)
1. Go to **[aistudio.google.com](https://aistudio.google.com/)** and sign in with your Google account.
2. Click **"Get API Key"** -> **"Create API key in new project"**.
3. Copy your new `AIzaSy...` key. You do not need to enter a credit card!

---

## 🛠 Step 2: Install Python Dependencies
Open your PowerShell/Command Prompt in this `ai_service` folder and run:
```powershell
# Create a virtual environment (optional but recommended)
python -m venv venv
.\venv\Scripts\activate

# Install required packages
pip install -r requirements.txt
```

---

## 🖥 Step 3: Run the AI Server
Set your API key as an environment variable and start the server:

```powershell
# Set your API Key in PowerShell
$env:GEMINI_API_KEY="your_copied_api_key_here"

# Start the FastAPI Uvicorn Server
python -m uvicorn main:app --reload --port 8000
```

Your AI microservice is now live at:
*   **API Docs & Interactive Tester:** `http://localhost:8000/docs`
*   **Health Status:** `http://localhost:8000/api/ai/status`

---

## 🧪 Step 4: Test the Endpoints right from your Browser!
Once the server is running, open **`http://localhost:8000/docs`** in your browser:
1.  **To test Statement Parsing:** Click on `POST /api/ai/parse-statement` -> **Try it out** -> Choose any `.pdf` or `.csv` bank statement -> Click **Execute**. It will return clean structured JSON!
2.  **To test the Virtual CA Advisor:** Click on `POST /api/ai/ca-advisor` -> **Try it out** -> Paste sample metrics and click **Execute**.
