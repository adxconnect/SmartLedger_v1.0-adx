import React, { useState, useEffect } from 'react';
import { Sparkles, Calendar, Filter, FileText, Landmark, Shield, Upload, Download, CheckCircle, AlertCircle, TrendingUp, TrendingDown, PieChart, RefreshCw, MessageSquare, DollarSign, ArrowRight, Check, Wallet, Tag, Layers } from 'lucide-react';

// ─── Mini Donut Chart Component ───
function MiniDonut({ segments, size = 120, label }) {
    const total = segments.reduce((s, seg) => s + seg.value, 0);
    if (total === 0) return null;
    const cx = size / 2, cy = size / 2, r = size * 0.36, strokeW = size * 0.16;
    let cumulative = 0;
    const circumference = 2 * Math.PI * r;
    return (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '10px' }}>
            <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
                {segments.map((seg, i) => {
                    const pct = seg.value / total;
                    const dashLen = pct * circumference;
                    const dashOff = -cumulative * circumference;
                    cumulative += pct;
                    return (
                        <circle key={i} cx={cx} cy={cy} r={r}
                            fill="none" stroke={seg.color} strokeWidth={strokeW}
                            strokeDasharray={`${dashLen} ${circumference - dashLen}`}
                            strokeDashoffset={dashOff}
                            style={{ transform: 'rotate(-90deg)', transformOrigin: '50% 50%', transition: 'all 0.6s ease' }}
                        />
                    );
                })}
                <text x={cx} y={cy - 4} textAnchor="middle" fill="#e2e8f0" fontSize={size * 0.12} fontWeight="700">
                    ₹{total >= 100000 ? `${(total / 100000).toFixed(1)}L` : total.toLocaleString('en-IN')}
                </text>
                <text x={cx} y={cy + 12} textAnchor="middle" fill="#64748b" fontSize={size * 0.085}>
                    {label || 'Total'}
                </text>
            </svg>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', justifyContent: 'center' }}>
                {segments.filter(s => s.value > 0).map((seg, i) => (
                    <div key={i} style={{ display: 'flex', alignItems: 'center', gap: '5px', fontSize: '11px', color: '#94a3b8' }}>
                        <span style={{ width: 8, height: 8, borderRadius: '50%', background: seg.color, display: 'inline-block' }} />
                        {seg.label}
                    </div>
                ))}
            </div>
        </div>
    );
}

// ─── Horizontal Bar Chart Component ───
function HBarChart({ items, maxVal }) {
    const max = maxVal || Math.max(...items.map(i => i.value), 1);
    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', width: '100%' }}>
            {items.map((item, i) => (
                <div key={i} style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <span style={{ fontSize: '12px', color: '#94a3b8', width: '120px', textAlign: 'right', flexShrink: 0, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {item.label}
                    </span>
                    <div style={{ flex: 1, background: 'rgba(255,255,255,0.04)', borderRadius: '6px', height: '22px', overflow: 'hidden', position: 'relative' }}>
                        <div style={{
                            height: '100%', borderRadius: '6px',
                            background: `linear-gradient(90deg, ${item.color || '#6366f1'}, ${item.color2 || '#818cf8'})`,
                            width: `${Math.max((item.value / max) * 100, 2)}%`,
                            transition: 'width 0.8s cubic-bezier(0.4, 0, 0.2, 1)',
                            display: 'flex', alignItems: 'center', justifyContent: 'flex-end', paddingRight: '8px'
                        }}>
                            {(item.value / max) > 0.25 && (
                                <span style={{ fontSize: '10px', color: '#fff', fontWeight: '600' }}>
                                    ₹{item.value >= 100000 ? `${(item.value / 100000).toFixed(1)}L` : item.value.toLocaleString('en-IN')}
                                </span>
                            )}
                        </div>
                    </div>
                    {(item.value / max) <= 0.25 && (
                        <span style={{ fontSize: '11px', color: '#94a3b8', flexShrink: 0 }}>
                            ₹{item.value.toLocaleString('en-IN')}
                        </span>
                    )}
                </div>
            ))}
        </div>
    );
}

// ─── Stat Row Component ───
function StatRow({ items }) {
    return (
        <div style={{ display: 'grid', gridTemplateColumns: `repeat(${Math.min(items.length, 4)}, 1fr)`, gap: '12px', margin: '10px 0' }}>
            {items.map((item, i) => (
                <div key={i} style={{
                    background: 'rgba(99, 102, 241, 0.06)', border: '1px solid rgba(99, 102, 241, 0.1)',
                    borderRadius: '14px', padding: '14px', textAlign: 'center'
                }}>
                    <div style={{ fontSize: '11px', color: '#64748b', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '6px' }}>{item.label}</div>
                    <div style={{ fontSize: '18px', fontWeight: '700', color: item.color || '#a5b4fc' }}>
                        {item.prefix || ''}{item.raw ? '' : '₹'}{typeof item.value === 'number' ? item.value.toLocaleString('en-IN', { minimumFractionDigits: 0, maximumFractionDigits: 0 }) : item.value}
                    </div>

                </div>
            ))}
        </div>
    );
}

// ─── Markdown-to-JSX Renderer ───
function renderMarkdown(text) {
    if (!text) return null;
    const lines = text.split('\n');
    const elements = [];
    let listBuffer = [];

    const flushList = () => {
        if (listBuffer.length > 0) {
            elements.push(
                <ul key={`ul-${elements.length}`} style={{ margin: '8px 0', paddingLeft: '20px', listStyle: 'none' }}>
                    {listBuffer.map((li, i) => (
                        <li key={i} style={{ fontSize: '13.5px', color: '#cbd5e1', lineHeight: '1.8', position: 'relative', paddingLeft: '14px' }}>
                            <span style={{ position: 'absolute', left: 0, color: '#818cf8' }}>•</span>
                            {formatInline(li)}
                        </li>
                    ))}
                </ul>
            );
            listBuffer = [];
        }
    };

    const formatInline = (str) => {
        const parts = [];
        // Process bold+italic, bold, italic, and code
        const regex = /(\*\*\*(.+?)\*\*\*|\*\*(.+?)\*\*|\*(.+?)\*|`(.+?)`)/g;
        let lastIndex = 0;
        let match;
        while ((match = regex.exec(str)) !== null) {
            if (match.index > lastIndex) {
                parts.push(str.substring(lastIndex, match.index));
            }
            if (match[2]) {
                parts.push(<strong key={match.index} style={{ fontWeight: '700', fontStyle: 'italic', color: '#e2e8f0' }}>{match[2]}</strong>);
            } else if (match[3]) {
                parts.push(<strong key={match.index} style={{ fontWeight: '700', color: '#e2e8f0' }}>{match[3]}</strong>);
            } else if (match[4]) {
                parts.push(<em key={match.index} style={{ fontStyle: 'italic', color: '#a5b4fc' }}>{match[4]}</em>);
            } else if (match[5]) {
                parts.push(<code key={match.index} style={{ background: 'rgba(99,102,241,0.12)', padding: '2px 6px', borderRadius: '4px', fontSize: '12px', color: '#c7d2fe' }}>{match[5]}</code>);
            }
            lastIndex = match.index + match[0].length;
        }
        if (lastIndex < str.length) parts.push(str.substring(lastIndex));
        return parts.length > 0 ? parts : str;
    };

    for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        const trimmed = line.trim();

        // Skip empty lines
        if (!trimmed) {
            flushList();
            continue;
        }

        // HR
        if (trimmed === '---' || trimmed === '***' || trimmed === '___') {
            flushList();
            elements.push(<div key={`hr-${i}`} style={{ height: '1px', background: 'rgba(99, 102, 241, 0.1)', margin: '14px 0' }} />);
            continue;
        }

        // Headings
        const h4Match = trimmed.match(/^#{4}\s+(.*)/);
        const h3Match = trimmed.match(/^#{3}\s+(.*)/);
        const h2Match = trimmed.match(/^#{2}\s+(.*)/);
        const h1Match = trimmed.match(/^#{1}\s+(.*)/);

        if (h4Match) {
            flushList();
            elements.push(<h4 key={`h4-${i}`} style={{ fontSize: '14px', fontWeight: '700', color: '#a5b4fc', margin: '16px 0 6px', letterSpacing: '0.2px' }}>{formatInline(h4Match[1])}</h4>);
            continue;
        }
        if (h3Match) {
            flushList();
            elements.push(<h3 key={`h3-${i}`} style={{ fontSize: '15px', fontWeight: '700', color: '#c7d2fe', margin: '18px 0 8px' }}>{formatInline(h3Match[1])}</h3>);
            continue;
        }
        if (h2Match || h1Match) {
            flushList();
            const text = h2Match ? h2Match[1] : h1Match[1];
            elements.push(<h3 key={`h2-${i}`} style={{ fontSize: '16px', fontWeight: '700', color: '#e0e7ff', margin: '18px 0 8px' }}>{formatInline(text)}</h3>);
            continue;
        }

        // List items (* or -)
        const listMatch = trimmed.match(/^[\*\-]\s+(.*)/);
        if (listMatch) {
            listBuffer.push(listMatch[1]);
            continue;
        }

        // Regular paragraph
        flushList();
        elements.push(<p key={`p-${i}`} style={{ fontSize: '13.5px', color: '#cbd5e1', lineHeight: '1.75', margin: '6px 0' }}>{formatInline(trimmed)}</p>);
    }
    flushList();
    return elements;
}

export default function AiVirtualCaImportView({ userUid, refreshTrigger, onTransactionsSaved }) {
    const [activeTab, setActiveTab] = useState('advisor'); // 'import' | 'advisor'
    const [transactions, setTransactions] = useState([]);
    const [bankAccounts, setBankAccounts] = useState([]);
    const [loading, setLoading] = useState(true);

    // Filter States for "Analyse All Transactions"
    const [fromDate, setFromDate] = useState('');
    const [toDate, setToDate] = useState('');
    const [selectedCategory, setSelectedCategory] = useState('All');
    const [aiInsight, setAiInsight] = useState('');
    const [generatingInsight, setGeneratingInsight] = useState(false);

    // Import States for "Account Statement Import & Save"
    const [aiFile, setAiFile] = useState(null);
    const [pdfPassword, setPdfPassword] = useState('');
    const [isPdfProtected, setIsPdfProtected] = useState(false);
    const [aiLoading, setAiLoading] = useState(false);
    const [extractedTransactions, setExtractedTransactions] = useState([]);
    const [selectedBankAccountId, setSelectedBankAccountId] = useState('');
    const [savingTransactions, setSavingTransactions] = useState(false);
    const [importSuccessMsg, setImportSuccessMsg] = useState('');

    // Automatically detect if selected PDF file is password-protected (encrypted)
    useEffect(() => {
        let isMounted = true;
        const checkProtection = async () => {
            if (!aiFile) {
                if (isMounted) {
                    setIsPdfProtected(false);
                    setPdfPassword('');
                }
                return;
            }

            const fileName = (aiFile.name || '').toLowerCase();
            if (!fileName.endsWith('.pdf')) {
                if (isMounted) {
                    setIsPdfProtected(false);
                    setPdfPassword('');
                }
                return;
            }

            try {
                const arrayBuffer = await aiFile.arrayBuffer();
                const bytes = new Uint8Array(arrayBuffer);
                const text = new TextDecoder("latin1").decode(bytes);
                // In PDF specifications, encrypted PDFs contain /Encrypt token in trailer or xref stream
                const isEncrypted = /\/Encrypt\s*($|\d|\<|\/|R)/i.test(text) || text.includes('/Encrypt');
                if (isMounted) {
                    setIsPdfProtected(isEncrypted);
                    if (!isEncrypted) {
                        setPdfPassword('');
                    }
                }
            } catch (err) {
                console.error("Error checking PDF protection:", err);
                if (isMounted) {
                    setIsPdfProtected(false);
                }
            }
        };

        checkProtection();
        return () => { isMounted = false; };
    }, [aiFile]);

    // Statement Category Grouping Engine
    const CATEGORY_GROUPS = [
        { id: 'RECURRING', label: 'Recurring Payments', keywords: ['recurring', 'subscription', 'emi'] },
        { id: 'FOOD', label: 'Food & Dining', keywords: ['food', 'dining', 'groceries', 'restaurant', 'cafe', 'zomato', 'swiggy', 'supermarket', 'mart', 'coffee'] },
        { id: 'BILLS', label: 'Bills & Utilities', keywords: ['recharge', 'electricity', 'water', 'gas', 'wifi', 'broadband', 'utility', 'bills', 'mobile', 'bill'] },
        { id: 'EDUCATION', label: 'Education & Learning', keywords: ['education', 'tuition', 'books', 'course', 'school', 'college', 'university', 'training'] },
        { id: 'SHOPPING', label: 'Shopping & Retail', keywords: ['shopping', 'ecommerce', 'clothes', 'electronics', 'amazon', 'flipkart', 'myntra', 'store', 'retail'] },
        { id: 'TRAVEL', label: 'Travel & Transport', keywords: ['travel', 'transport', 'fuel', 'petrol', 'diesel', 'uber', 'ola', 'flight', 'train', 'metro', 'cab', 'taxi'] },
        { id: 'HEALTH', label: 'Health & Medical', keywords: ['health', 'medical', 'hospital', 'pharmacy', 'insurance', 'mediclaim', 'doctor', 'clinic'] },
        { id: 'INCOME', label: 'Income & Salary', keywords: ['salary', 'income', 'bonus', 'refund', 'interest', 'dividend', 'stipend', 'credit'] },
        { id: 'INVESTMENT', label: 'Investments & Savings', keywords: ['investment', 'mutual fund', 'sip', 'ppf', 'elss', 'fd', 'rd', 'stocks', 'gold', 'nps'] }
    ];

    const getTransactionGroup = (category) => {
        if (!category) return 'General / Others';
        const c = String(category).toLowerCase();
        for (const g of CATEGORY_GROUPS) {
            if (g.keywords.some(kw => c.includes(kw))) return g.label;
        }
        return 'General / Others';
    };

    const statementSummary = React.useMemo(() => {
        const totalTxns = extractedTransactions.length;
        let credited = 0;
        let debited = 0;
        const grpMap = {};

        extractedTransactions.forEach(t => {
            const amt = parseFloat(t.amount || 0);
            const isCredit = t.type === 'CREDIT' || String(t.type).toUpperCase() === 'INCOME' || String(t.type).toUpperCase() === 'CREDIT';
            if (isCredit) {
                credited += amt;
            } else {
                debited += amt;
            }
            const grp = getTransactionGroup(t.category || 'General');
            if (!grpMap[grp]) {
                grpMap[grp] = { count: 0, totalAmount: 0 };
            }
            grpMap[grp].count += 1;
            grpMap[grp].totalAmount += amt;
        });

        const netFlow = credited - debited;
        const selectedAcc = bankAccounts.find(acc => String(acc.id) === String(selectedBankAccountId));
        const currentAccBalance = selectedAcc ? parseFloat(selectedAcc.balance || 0) : 0;
        const totalBalanceLeft = currentAccBalance + netFlow;

        const groupBreakdown = Object.entries(grpMap).map(([grp, stats]) => ({
            groupName: grp,
            count: stats.count,
            totalAmount: stats.totalAmount
        }));

        return {
            totalTxns,
            credited,
            debited,
            netFlow,
            currentAccBalance,
            totalBalanceLeft,
            groupBreakdown
        };
    }, [extractedTransactions, selectedBankAccountId, bankAccounts]);

    // Virtual CA RAG Advisory States & Interactive Chat History
    const [caQuery, setCaQuery] = useState('');
    const [caResponse, setCaResponse] = useState('');
    const [caLoading, setCaLoading] = useState(false);
    const [chatHistory, setChatHistory] = useState([
        {
            sender: 'ai',
            text: {
                question: "SmartLedger Ledger AI Connected",
                isWelcome: true,
                sections: [
                    {
                        title: "👋 Welcome to Ledger AI!",
                        stats: [
                            { label: "Ledger AI", value: "Online", color: "#34d399", raw: true },
                            { label: "Synced Modules", value: "5 / 5", color: "#818cf8", raw: true }
                        ],
                        detail: "I am fully synchronized with your Overview, Transactions, Live Investments, Len-Den, and Loans modules. Ask me anything about your balances, spending category breakdown, investment allocation, loans, or tax optimization!"
                    }
                ],
                charts: []
            }
        }
    ]);

    const [conversations, setConversations] = useState([]);
    const [caMetrics, setCaMetrics] = useState({
        annualIncome: 1200000,
        investments80C: 150000,
        healthInsurance80D: 25000
    });

    const [portfolioData, setPortfolioData] = useState({
        bankAccounts: [],
        deposits: [],
        transactions: [],
        investments: [],
        mutualFunds: [],
        goldSilver: [],
        lendings: [],
        loans: []
    });

    // Fetch existing transactions, bank accounts, investments, len-den, and loans
    useEffect(() => {
        const fetchAllData = async () => {
            setLoading(true);
            try {
                const [txRes, banksRes, depsRes, invRes, mfRes, goldRes, lendenRes, loansRes] = await Promise.all([
                    fetch('http://localhost:8080/api/transactions').then(r => r.ok ? r.json() : []),
                    fetch('http://localhost:8080/api/bankaccounts').then(r => r.ok ? r.json() : []),
                    fetch('http://localhost:8080/api/deposits').then(r => r.ok ? r.json() : []),
                    fetch('http://localhost:8080/api/investments').then(r => r.ok ? r.json() : []),
                    fetch('http://localhost:8080/api/mutualfunds').then(r => r.ok ? r.json() : []),
                    fetch('http://localhost:8080/api/goldsilverinvestments').then(r => r.ok ? r.json() : []),
                    fetch('http://localhost:8080/api/lendings').then(r => r.ok ? r.json() : []),
                    fetch('http://localhost:8080/api/loans').then(r => r.ok ? r.json() : [])
                ]);

                const txList = Array.isArray(txRes) ? txRes : [];
                setTransactions(txList);

                const banksList = Array.isArray(banksRes) ? banksRes : [];
                const depositsList = Array.isArray(depsRes) ? depsRes : [];
                const invList = Array.isArray(invRes) ? invRes : [];
                const mfList = Array.isArray(mfRes) ? mfRes : [];
                const goldList = Array.isArray(goldRes) ? goldRes : [];
                const lendenList = Array.isArray(lendenRes) ? lendenRes : [];
                const loansList = Array.isArray(loansRes) ? loansRes : [];

                setPortfolioData({
                    bankAccounts: banksList,
                    deposits: depositsList,
                    transactions: txList,
                    investments: invList,
                    mutualFunds: mfList,
                    goldSilver: goldList,
                    lendings: lendenList,
                    loans: loansList
                });

                const accountsList = [];
                banksList.forEach(b => {
                    accountsList.push({
                        id: b.id,
                        name: b.accountName || b.holderName || 'Bank Account',
                        bankName: b.bankName || 'Bank',
                        accountNumber: b.accountNumber || '',
                        accountType: b.accountType || 'Savings Account',
                        balance: b.balance ?? 0
                    });
                });
                depositsList.forEach(d => {
                    accountsList.push({
                        id: d.id,
                        name: d.holderName || d.accountName || 'Deposit Account',
                        bankName: d.bankName || 'Bank',
                        accountNumber: d.accountNumber || '',
                        accountType: d.depositType === 'FD' ? 'Fixed Deposit' : d.depositType === 'RD' ? 'Recurring Deposit' : 'Deposit',
                        balance: d.principalAmount ?? d.balance ?? 0
                    });
                });
                setBankAccounts(accountsList);
                if (accountsList.length > 0 && !selectedBankAccountId) {
                    setSelectedBankAccountId(accountsList[0].id);
                }
            } catch (err) {
                console.error("Error loading data in AI Virtual CA view:", err);
            } finally {
                setLoading(false);
            }
        };

        fetchAllData();
    }, [refreshTrigger]);

    // Complete 360-degree Financial Portfolio Context (Overview, Transactions, Live Investments, Len-Den, Loans)
    const portfolioContext = React.useMemo(() => {
        const { bankAccounts: banks, deposits: deps, transactions: txns, investments: invs, mutualFunds: mfs, goldSilver: golds, lendings: lenden, loans } = portfolioData;

        // 1. Overview
        const totalBankBal = banks.reduce((sum, b) => sum + (parseFloat(b.balance) || 0), 0);
        const totalDepositBal = deps.reduce((sum, d) => sum + (parseFloat(d.principalAmount || d.balance) || 0), 0);

        // 2. Transactions
        const totalDebitedTxns = txns.filter(t => t.type === 'DEBIT' || t.type === 'Debit' || t.type === 'Expense');
        const totalCreditedTxns = txns.filter(t => t.type === 'CREDIT' || t.type === 'Credit' || t.type === 'Income');
        const totalExpense = totalDebitedTxns.reduce((sum, t) => sum + (parseFloat(t.amount) || 0), 0);
        const totalIncome = totalCreditedTxns.reduce((sum, t) => sum + (parseFloat(t.amount) || 0), 0);
        const categoryGroupsSpent = {};
        totalDebitedTxns.forEach(t => {
            const grp = getTransactionGroup(t.category || 'General');
            categoryGroupsSpent[grp] = (categoryGroupsSpent[grp] || 0) + (parseFloat(t.amount) || 0);
        });

        // 3. Live Investments
        const totalMfVal = mfs.reduce((sum, m) => sum + (parseFloat(m.currentValue || m.investedAmount) || 0), 0);
        const totalGoldVal = golds.reduce((sum, g) => sum + (parseFloat(g.currentValue || g.investedAmount || g.purchasePrice) || 0), 0);
        const totalGenInvVal = invs.reduce((sum, i) => sum + (parseFloat(i.currentValue || i.investedAmount) || 0), 0);
        const totalInvestedValue = totalMfVal + totalGoldVal + totalGenInvVal;

        // 4. Len-Den
        const totalLent = lenden.filter(l => l.type === 'Lent' || l.type === 'Gave').reduce((s, l) => s + (parseFloat(l.amount) || 0), 0);
        const totalBorrowed = lenden.filter(l => l.type === 'Borrowed' || l.type === 'Took').reduce((s, l) => s + (parseFloat(l.amount) || 0), 0);

        // 5. Loans
        const totalLoanPrincipalLeft = loans.reduce((s, l) => s + (parseFloat(l.remainingBalance || l.principalAmount) || 0), 0);
        const totalEmi = loans.reduce((s, l) => s + (parseFloat(l.emiAmount) || 0), 0);

        return {
            overview: {
                totalBankBalance: totalBankBal,
                totalDepositBalance: totalDepositBal,
                bankAccountsList: banks.map(b => `${b.name || b.bankName}: ₹${parseFloat(b.balance || 0).toLocaleString('en-IN')}`),
                depositsList: deps.map(d => `${d.holderName || d.bankName} (${d.depositType || 'Deposit'}): ₹${parseFloat(d.principalAmount || d.balance || 0).toLocaleString('en-IN')}`)
            },
            transactions: {
                totalCount: txns.length,
                totalExpense: totalExpense,
                totalIncome: totalIncome,
                categoryGroupsSpent: categoryGroupsSpent,
                recentTxns: txns.slice(0, 15).map(t => `${t.date || ''} | ${t.description || ''} | ₹${t.amount} (${t.type}) [Group: ${getTransactionGroup(t.category)}]`)
            },
            liveInvestments: {
                totalInvestedValue: totalInvestedValue,
                mutualFundsCount: mfs.length,
                mutualFundsTotal: totalMfVal,
                goldSilverCount: golds.length,
                goldSilverTotal: totalGoldVal,
                generalInvestmentsTotal: totalGenInvVal,
                items: [
                    ...mfs.map(m => `MF: ${m.fundName || 'Mutual Fund'} (₹${parseFloat(m.currentValue || m.investedAmount || 0).toLocaleString('en-IN')})`),
                    ...golds.map(g => `Gold/Silver: ${g.metalType || 'Gold'} ${g.purity || ''} (₹${parseFloat(g.currentValue || g.purchasePrice || 0).toLocaleString('en-IN')})`),
                    ...invs.map(i => `Inv: ${i.name || i.title || 'Investment'} (₹${parseFloat(i.currentValue || i.investedAmount || 0).toLocaleString('en-IN')})`)
                ]
            },
            lenDen: {
                totalLent: totalLent,
                totalBorrowed: totalBorrowed,
                records: lenden.map(l => `${l.personName || 'Person'}: ₹${parseFloat(l.amount || 0).toLocaleString('en-IN')} (${l.type || 'Lent'}) — Status: ${l.status || 'Pending'}`)
            },
            loans: {
                totalRemainingPrincipal: totalLoanPrincipalLeft,
                totalMonthlyEMI: totalEmi,
                loansList: loans.map(l => `${l.loanName || l.lenderName || 'Loan'}: Remaining ₹${parseFloat(l.remainingBalance || l.principalAmount || 0).toLocaleString('en-IN')}, EMI: ₹${parseFloat(l.emiAmount || 0).toLocaleString('en-IN')}`)
            }
        };
    }, [portfolioData]);

    // Categories list including default categories and any found in transactions
    const defaultCategories = ['Food/Dining', 'Education', 'Recharge', 'Shopping', 'Bills & Utilities', 'Travel', 'Salary', 'Entertainment', 'Health', 'Others'];
    const allCategories = ['All', ...Array.from(new Set([
        ...defaultCategories,
        ...transactions.map(t => t.category).filter(Boolean)
    ]))];

    // Filter transactions based on From Date, To Date, and Category
    const filteredTransactions = transactions.filter(t => {
        if (!t.date) return true;
        if (fromDate && t.date < fromDate) return false;
        if (toDate && t.date > toDate) return false;
        if (selectedCategory !== 'All' && t.category !== selectedCategory) return false;
        return true;
    });

    // Compute metrics for filtered transactions
    const totalDebit = filteredTransactions
        .filter(t => t.type === 'Debit')
        .reduce((sum, t) => sum + (parseFloat(t.amount) || 0), 0);

    const totalCredit = filteredTransactions
        .filter(t => t.type === 'Credit' || t.type === 'Interest')
        .reduce((sum, t) => sum + (parseFloat(t.amount) || 0), 0);

    const netAmount = totalCredit - totalDebit;

    // Category breakdown for progress bars
    const categorySpending = {};
    filteredTransactions.forEach(t => {
        const cat = t.category || 'Others';
        const amt = parseFloat(t.amount) || 0;
        if (t.type === 'Debit') {
            categorySpending[cat] = (categorySpending[cat] || 0) + amt;
        }
    });

    const topCategory = Object.entries(categorySpending).sort((a, b) => b[1] - a[1])[0] || ['None', 0];

    // Generate AI smart financial analysis for filtered transactions
    const generateAiAnalyticsInsight = () => {
        setGeneratingInsight(true);
        setTimeout(() => {
            const count = filteredTransactions.length;
            if (count === 0) {
                setAiInsight("No transactions found in the selected date range and category. Adjust your From/To date range to analyze your cash flow.");
            } else {
                const debitCount = filteredTransactions.filter(t => t.type === 'Debit').length;
                const creditCount = filteredTransactions.filter(t => t.type === 'Credit' || t.type === 'Interest').length;
                const topCatName = topCategory[0];
                const topCatAmt = topCategory[1].toLocaleString('en-IN', { maximumFractionDigits: 2 });
                const ratio = totalCredit > 0 ? ((totalDebit / totalCredit) * 100).toFixed(1) : 100;

                let tip = "Consider allocating 20% of your net inflow to Section 80C tax-saving Mutual Funds (ELSS).";
                if (topCatName === 'Food/Dining') {
                    tip = "Food/Dining is your highest spending category. Setting a weekly dining budget cap could save up to ₹4,500/month.";
                } else if (topCatName === 'Education') {
                    tip = "Education expenses qualify for tuition fee tax exemption under Section 80C up to ₹1.5L per financial year.";
                } else if (topCatName === 'Recharge') {
                    tip = "You can bundle telecom and utility recharges to utilize cashback credit cards for an effective 5% return.";
                }

                setAiInsight(
                    `In the selected period (${fromDate || 'Start'} to ${toDate || 'Present'}), you have ${count} transaction(s) total: ${debitCount} debit(s) and ${creditCount} credit(s).\n\n` +
                    `• Total Spent: ₹${totalDebit.toLocaleString('en-IN', { maximumFractionDigits: 2 })}\n` +
                    `• Total Received: ₹${totalCredit.toLocaleString('en-IN', { maximumFractionDigits: 2 })}\n` +
                    `• Top Expense Category: ${topCatName} (₹${topCatAmt})\n\n` +
                    `💡 Ledger AI Recommendation: ${tip}`
                );
            }
            setGeneratingInsight(false);
        }, 600);
    };

    // Auto-generate insight when filter changes
    useEffect(() => {
        generateAiAnalyticsInsight();
    }, [fromDate, toDate, selectedCategory, transactions.length]);

    // Handle AI Parse Statement (PDF / CSV / Excel)
    const handleAiParseStatement = async () => {
        if (!aiFile) return;
        setAiLoading(true);
        setImportSuccessMsg('');
        try {
            const fd = new FormData();
            fd.append('file', aiFile);
            if (pdfPassword) {
                fd.append('password', pdfPassword);
            }

            const res = await fetch("http://localhost:8000/api/ai/parse-statement", {
                method: "POST",
                body: fd
            });

            if (res.ok) {
                const data = await res.json();
                if (Array.isArray(data) && data.length > 0) {
                    setExtractedTransactions(data);
                    setAiLoading(false);
                    return;
                } else {
                    alert("No transactions could be extracted from this statement.");
                    setAiLoading(false);
                    return;
                }
            } else {
                let errDetail = '';
                try {
                    const errJson = await res.json();
                    errDetail = errJson.detail || '';
                } catch (e) {
                    // ignore
                }
                if (errDetail && errDetail.toLowerCase().includes('password')) {
                    alert("🔒 " + errDetail);
                    setIsPdfProtected(true);
                } else {
                    alert("Error parsing statement: " + (errDetail || res.statusText));
                }
                setAiLoading(false);
                return;
            }
        } catch (err) {
            console.error("AI statement parse error:", err);
            alert("Network error: Could not connect to the parsing service.");
        } finally {
            setAiLoading(false);
        }
    };

    // Save Extracted Transactions to the selected bank account ("whatever is already there")
    const handleSaveExtractedTransactions = async () => {
        if (extractedTransactions.length === 0) return;
        if (!selectedBankAccountId) {
            alert("Please select a Bank Account to save these transactions to!");
            return;
        }

        setSavingTransactions(true);
        setImportSuccessMsg('');
        const selectedAccount = bankAccounts.find(acc => acc.id === selectedBankAccountId);

        try {
            let count = 0;
            for (const txn of extractedTransactions) {
                const payload = {
                    date: txn.date || new Date().toISOString().split('T')[0],
                    refId: txn.refId || '',
                    description: `${txn.description} ${txn.taxSection ? '[' + txn.taxSection + ']' : ''}`.trim(),
                    amount: parseFloat(txn.amount) || 0,
                    type: txn.type === 'CREDIT' ? 'Credit' : 'Debit',
                    category: txn.category || 'Others',
                    accountType: selectedAccount ? selectedAccount.accountType : 'Savings Account',
                    accountId: selectedBankAccountId
                };
                await fetch("http://localhost:8080/api/transactions", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload)
                });
                count++;
            }

            const targetName = selectedAccount ? selectedAccount.name : 'Bank Account';
            setImportSuccessMsg(`✅ Successfully saved ${count} transaction(s) to ${targetName}! They are now included in your statement & analytics.`);
            setExtractedTransactions([]);
            setAiFile(null);
            setPdfPassword('');
            setIsPdfProtected(false);
            if (onTransactionsSaved) onTransactionsSaved();
        } catch (err) {
            console.error("Error saving transactions:", err);
            alert("Error saving transactions: " + err.message);
        } finally {
            setSavingTransactions(false);
        }
    };

    // Build unified structured CA response with paragraph-wise sections and interactive charts
    const buildCaResponseObject = (queryText, portfolioContext, apiText = null) => {
        const ov = portfolioContext.overview || {};
        const tx = portfolioContext.transactions || {};
        const inv = portfolioContext.liveInvestments || {};
        const ld = portfolioContext.lenDen || {};
        const lns = portfolioContext.loans || {};

        const qLower = queryText.toLowerCase();
        const showOverview = qLower.includes('overview') || qLower.includes('balance') || qLower.includes('summary') || qLower.includes('net') || qLower.includes('bank');
        const showSpending = qLower.includes('spend') || qLower.includes('food') || qLower.includes('bill') || qLower.includes('category') || qLower.includes('expense') || qLower.includes('transaction');
        const showInvestments = qLower.includes('invest') || qLower.includes('mutual') || qLower.includes('gold') || qLower.includes('portfolio') || qLower.includes('silver');
        const showLenDen = qLower.includes('len-den') || qLower.includes('lend') || qLower.includes('borrow') || qLower.includes('owe') || qLower.includes('peer');
        const showLoans = qLower.includes('loan') || qLower.includes('emi') || qLower.includes('debt');
        const showAll = !showOverview && !showSpending && !showInvestments && !showLenDen && !showLoans;

        const overviewChartData = [
            { value: parseFloat(ov.totalBankBalance) || 0, color: '#6366f1', label: 'Bank Balance' },
            { value: parseFloat(ov.totalDepositBalance) || 0, color: '#8b5cf6', label: 'Deposits' }
        ];
        const cashflowChartData = [
            { value: parseFloat(tx.totalIncome) || 0, color: '#34d399', label: 'Inflow' },
            { value: parseFloat(tx.totalExpense) || 0, color: '#f87171', label: 'Outflow' }
        ];
        const investChartData = [
            { value: parseFloat(inv.mutualFundsTotal) || 0, color: '#818cf8', label: 'Mutual Funds' },
            { value: parseFloat(inv.goldSilverTotal) || 0, color: '#fbbf24', label: 'Gold & Silver' },
            { value: parseFloat(inv.generalInvestmentsTotal) || 0, color: '#34d399', label: 'Other' }
        ];
        const categoryItems = Object.entries(tx.categoryGroupsSpent || {}).map(([cat, amt], i) => ({
            label: cat, value: parseFloat(amt) || 0,
            color: ['#6366f1','#ec4899','#f59e0b','#34d399','#0ea5e9','#a78bfa','#f87171','#14b8a6'][i % 8],
            color2: ['#818cf8','#f472b6','#fbbf24','#6ee7b7','#38bdf8','#c4b5fd','#fca5a5','#5eead4'][i % 8]
        }));

        const responseObj = {
            question: queryText,
            sections: [],
            charts: []
        };

        // If API returned a custom LLM string that is not the default raw markdown fallback, attach it
        if (apiText && typeof apiText === 'string' && !apiText.includes("Ledger AI — Comprehensive Portfolio Answer")) {
            responseObj.markdownText = apiText;
        }

        if (showAll || showOverview) {
            responseObj.sections.push({
                title: '🏛️ Overview — Bank Accounts & Deposits',
                stats: [
                    { label: 'Bank Balance', value: parseFloat(ov.totalBankBalance) || 0, color: '#818cf8' },
                    { label: 'Deposit Principal', value: parseFloat(ov.totalDepositBalance) || 0, color: '#a78bfa' }
                ],
                detail: ov.bankAccountsList?.length > 0
                    ? `Your active accounts include ${ov.bankAccountsList.join(', ')}. Net liquid balance across all bank accounts and deposits is ₹${((parseFloat(ov.totalBankBalance)||0) + (parseFloat(ov.totalDepositBalance)||0)).toLocaleString('en-IN')}.`
                    : 'No bank accounts found in the system yet.'
            });
            responseObj.charts.push({ type: 'donut', data: overviewChartData, label: 'Assets' });
        }

        if (showAll || showSpending) {
            responseObj.sections.push({
                title: '💳 Transactions & Spending Cashflow',
                stats: [
                    { label: 'Total Txns', value: tx.totalCount || 0, color: '#a5b4fc', raw: true },
                    { label: 'Total Inflow', value: parseFloat(tx.totalIncome) || 0, color: '#34d399' },
                    { label: 'Total Outflow', value: parseFloat(tx.totalExpense) || 0, color: '#f87171' }
                ],
                detail: categoryItems.length > 0
                    ? `You have logged ${tx.totalCount || 0} transactions across ${categoryItems.length} categories. See the visual category breakdown chart below for point-to-point expense distribution.`
                    : 'No spending transactions recorded yet.'
            });
            responseObj.charts.push({ type: 'donut', data: cashflowChartData, label: 'Cashflow' });
            if (categoryItems.length > 0) {
                responseObj.charts.push({ type: 'bar', data: categoryItems });
            }
        }

        if (showAll || showInvestments) {
            responseObj.sections.push({
                title: '📈 Live Investments & Portfolio Allocation',
                stats: [
                    { label: 'Total Invested', value: parseFloat(inv.totalInvestedValue) || 0, color: '#818cf8' },
                    { label: 'Mutual Funds', value: parseFloat(inv.mutualFundsTotal) || 0, color: '#a78bfa' },
                    { label: 'Gold & Silver', value: parseFloat(inv.goldSilverTotal) || 0, color: '#fbbf24' }
                ],
                detail: inv.items?.length > 0
                    ? `Your investment holdings include ${inv.items.slice(0, 4).join(', ')}. Your portfolio is actively tracked for long-term growth and capital appreciation.`
                    : 'No live investment holdings found in the system yet.'
            });
            if ((parseFloat(inv.totalInvestedValue) || 0) > 0) {
                responseObj.charts.push({ type: 'donut', data: investChartData, label: 'Investments' });
            }
        }

        if (showAll || showLenDen) {
            responseObj.sections.push({
                title: '🤝 Len-Den — Peer Lending & Borrowing',
                stats: [
                    { label: 'Lent (To Receive)', value: parseFloat(ld.totalLent) || 0, color: '#34d399' },
                    { label: 'Borrowed (To Pay)', value: parseFloat(ld.totalBorrowed) || 0, color: '#f87171' }
                ],
                detail: ld.records?.length > 0
                    ? `Active peer records: ${ld.records.slice(0, 3).join(', ')}. All peer lending receivables and payables are reconciled in real time.`
                    : 'No active Len-Den records logged.'
            });
        }

        if (showAll || showLoans) {
            responseObj.sections.push({
                title: '🏦 Loans & Monthly EMI Obligations',
                stats: [
                    { label: 'Remaining Principal', value: parseFloat(lns.totalRemainingPrincipal) || 0, color: '#f87171' },
                    { label: 'Monthly EMI Burden', value: parseFloat(lns.totalMonthlyEMI) || 0, color: '#fbbf24' }
                ],
                detail: lns.loansList?.length > 0
                    ? `Your active loan accounts include ${lns.loansList.slice(0, 3).join(', ')}. Monitoring your monthly EMI burden helps maintain optimal cash flow and credit health.`
                    : 'No active loan liabilities logged.'
            });
        }

        return responseObj;
    };

    // Handle Virtual CA RAG Advisory Query
    const handleCaAdvisorQuery = async (e, customQueryText = null) => {
        if (e && e.preventDefault) e.preventDefault();
        const queryText = (customQueryText || caQuery || '').trim();
        if (!queryText) return;
        setCaQuery('');
        setChatHistory(prev => [...prev, { sender: 'user', text: queryText }]);
        setCaLoading(true);
        try {
            const payload = {
                userQuery: queryText,
                annualIncome: caMetrics.annualIncome,
                investments80C: caMetrics.investments80C,
                healthInsurance80D: caMetrics.healthInsurance80D,
                categoryBreakdown: categorySpending,
                portfolioContext: portfolioContext
            };
            const res = await fetch("http://localhost:8000/api/ai/ca-advisor", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            if (!res.ok) throw new Error("CA Advisor query failed");
            const data = await res.json();
            const structuredObj = buildCaResponseObject(queryText, portfolioContext, data.advisorResponse);
            setChatHistory(prev => [...prev, { sender: 'ai', text: structuredObj }]);
            setCaResponse(JSON.stringify(structuredObj));
        } catch (err) {
            console.error(err);
            const structuredObj = buildCaResponseObject(queryText, portfolioContext, null);
            setChatHistory(prev => [...prev, { sender: 'ai', text: structuredObj }]);
            setCaResponse(JSON.stringify(structuredObj));
        } finally {
            setCaLoading(false);
        }
    };


    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '28px', animation: 'slideUp 0.3s ease forwards', flex: 1, minHeight: 0 }}>


            {/* TAB 1: ANALYSE ALL TRANSACTIONS */}
            {activeTab === 'analyse' && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
                    {/* Filters Bar: From Date, To Date, and Category */}
                    <div className="glass-card" style={{
                        background: 'var(--surface-dark)',
                        border: '1px solid var(--border-dark)',
                        borderRadius: '20px',
                        padding: '24px',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: '16px',
                        boxShadow: '0 8px 32px rgba(0,0,0,0.15)'
                    }}>
                        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                                <Filter size={20} color="#34d399" />
                                <span style={{ fontWeight: '700', fontSize: '16px', color: 'var(--dash-text)' }}>
                                    Transactions Filter — Date Rate "From" &amp; "To" and Category
                                </span>
                            </div>

                            {(fromDate || toDate || selectedCategory !== 'All') && (
                                <button
                                    onClick={() => { setFromDate(''); setToDate(''); setSelectedCategory('All'); }}
                                    style={{
                                        background: 'rgba(239, 68, 68, 0.1)',
                                        border: '1px solid rgba(239, 68, 68, 0.3)',
                                        color: '#ef4444',
                                        padding: '6px 14px',
                                        borderRadius: '8px',
                                        fontSize: '12px',
                                        fontWeight: '600',
                                        cursor: 'pointer'
                                    }}
                                >
                                    Reset Filters
                                </button>
                            )}
                        </div>

                        {/* Input Row: From, To, and Category Dropdown */}
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px', alignItems: 'center' }}>
                            <div>
                                <label style={{ display: 'block', fontSize: '12px', fontWeight: '600', color: 'var(--dash-text-muted)', marginBottom: '6px' }}>
                                    📅 Date Rate "From"
                                </label>
                                <input
                                    type="date"
                                    value={fromDate}
                                    onChange={(e) => setFromDate(e.target.value)}
                                    onClick={(e) => { if (e.target.showPicker) e.target.showPicker(); }}
                                    style={{
                                        width: '100%',
                                        padding: '12px 14px',
                                        borderRadius: '12px',
                                        background: 'var(--bg-dark)',
                                        color: 'var(--dash-text)',
                                        border: '1px solid var(--border-dark)',
                                        outline: 'none',
                                        fontSize: '14px',
                                        cursor: 'pointer'
                                    }}
                                />
                            </div>

                            <div>
                                <label style={{ display: 'block', fontSize: '12px', fontWeight: '600', color: 'var(--dash-text-muted)', marginBottom: '6px' }}>
                                    📅 Date Rate "To"
                                </label>
                                <input
                                    type="date"
                                    value={toDate}
                                    onChange={(e) => setToDate(e.target.value)}
                                    onClick={(e) => { if (e.target.showPicker) e.target.showPicker(); }}
                                    style={{
                                        width: '100%',
                                        padding: '12px 14px',
                                        borderRadius: '12px',
                                        background: 'var(--bg-dark)',
                                        color: 'var(--dash-text)',
                                        border: '1px solid var(--border-dark)',
                                        outline: 'none',
                                        fontSize: '14px',
                                        cursor: 'pointer'
                                    }}
                                />
                            </div>

                            <div>
                                <label style={{ display: 'block', fontSize: '12px', fontWeight: '600', color: 'var(--dash-text-muted)', marginBottom: '6px' }}>
                                    🏷️ By Category (Food/Dining, Education, Recharge, and etc.)
                                </label>
                                <select
                                    value={selectedCategory}
                                    onChange={(e) => setSelectedCategory(e.target.value)}
                                    style={{
                                        width: '100%',
                                        padding: '12px 14px',
                                        borderRadius: '12px',
                                        background: 'var(--bg-dark)',
                                        color: 'var(--dash-text)',
                                        border: '1px solid var(--border-dark)',
                                        outline: 'none',
                                        fontSize: '14px',
                                        cursor: 'pointer'
                                    }}
                                >
                                    {allCategories.map(cat => (
                                        <option key={cat} value={cat} style={{ background: 'var(--bg-dark)', color: 'var(--dash-text)' }}>
                                            {cat === 'All' ? 'All Categories (View Everything)' : cat}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        {/* Quick Category Filter Pills */}
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', paddingTop: '4px' }}>
                            {allCategories.map(cat => {
                                const isSelected = selectedCategory === cat;
                                return (
                                    <button
                                        key={cat}
                                        onClick={() => setSelectedCategory(cat)}
                                        style={{
                                            padding: '6px 14px',
                                            borderRadius: '20px',
                                            border: isSelected ? '1px solid #34d399' : '1px solid var(--border-dark)',
                                            background: isSelected ? 'rgba(52, 211, 153, 0.15)' : 'rgba(255, 255, 255, 0.03)',
                                            color: isSelected ? '#34d399' : 'var(--dash-text-muted)',
                                            fontSize: '12px',
                                            fontWeight: isSelected ? '700' : '500',
                                            cursor: 'pointer',
                                            transition: 'all 0.2s ease',
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: '6px'
                                        }}
                                    >
                                        <span>{cat}</span>
                                        {isSelected && <Check size={12} />}
                                    </button>
                                );
                            })}
                        </div>
                    </div>

                    {/* KPI Summary Cards */}
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px' }}>
                        <div className="stat-card" style={{
                            background: 'var(--surface-dark)',
                            border: '1px solid var(--border-dark)',
                            borderRadius: '20px',
                            padding: '20px',
                            display: 'flex',
                            flexDirection: 'column',
                            gap: '8px'
                        }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <span style={{ fontSize: '13px', color: 'var(--dash-text-muted)', fontWeight: '600' }}>Total Expense (Debit)</span>
                                <TrendingDown size={18} color="#ef4444" />
                            </div>
                            <span style={{ fontSize: '26px', fontWeight: '700', color: '#ef4444' }}>
                                ₹{totalDebit.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                            </span>
                            <span style={{ fontSize: '11px', color: 'var(--dash-text-muted)' }}>
                                Filtered date range &amp; category
                            </span>
                        </div>

                        <div className="stat-card" style={{
                            background: 'var(--surface-dark)',
                            border: '1px solid var(--border-dark)',
                            borderRadius: '20px',
                            padding: '20px',
                            display: 'flex',
                            flexDirection: 'column',
                            gap: '8px'
                        }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <span style={{ fontSize: '13px', color: 'var(--dash-text-muted)', fontWeight: '600' }}>Total Inflow (Credit)</span>
                                <TrendingUp size={18} color="#34d399" />
                            </div>
                            <span style={{ fontSize: '26px', fontWeight: '700', color: '#34d399' }}>
                                ₹{totalCredit.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                            </span>
                            <span style={{ fontSize: '11px', color: 'var(--dash-text-muted)' }}>
                                Filtered credits &amp; interest
                            </span>
                        </div>

                        <div className="stat-card" style={{
                            background: 'var(--surface-dark)',
                            border: '1px solid var(--border-dark)',
                            borderRadius: '20px',
                            padding: '20px',
                            display: 'flex',
                            flexDirection: 'column',
                            gap: '8px'
                        }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <span style={{ fontSize: '13px', color: 'var(--dash-text-muted)', fontWeight: '600' }}>Net Cash Flow</span>
                                <DollarSign size={18} color={netAmount >= 0 ? '#38bdf8' : '#f59e0b'} />
                            </div>
                            <span style={{ fontSize: '26px', fontWeight: '700', color: netAmount >= 0 ? '#38bdf8' : '#f59e0b' }}>
                                ₹{netAmount.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                            </span>
                            <span style={{ fontSize: '11px', color: 'var(--dash-text-muted)' }}>
                                Net savings in this selection
                            </span>
                        </div>

                        <div className="stat-card" style={{
                            background: 'var(--surface-dark)',
                            border: '1px solid var(--border-dark)',
                            borderRadius: '20px',
                            padding: '20px',
                            display: 'flex',
                            flexDirection: 'column',
                            gap: '8px'
                        }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <span style={{ fontSize: '13px', color: 'var(--dash-text-muted)', fontWeight: '600' }}>Transactions Found</span>
                                <FileText size={18} color="#a78bfa" />
                            </div>
                            <span style={{ fontSize: '26px', fontWeight: '700', color: '#a78bfa' }}>
                                {filteredTransactions.length}
                            </span>
                            <span style={{ fontSize: '11px', color: 'var(--dash-text-muted)' }}>
                                Top category: {topCategory[0]}
                            </span>
                        </div>
                    </div>

                    {/* AI Virtual CA Analytics Summary Box */}
                    <div style={{
                        background: 'linear-gradient(135deg, rgba(52, 211, 153, 0.1), rgba(16, 185, 129, 0.05))',
                        border: '1px solid rgba(52, 211, 153, 0.3)',
                        borderRadius: '20px',
                        padding: '24px',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: '12px'
                    }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                                <span style={{ fontSize: '20px' }}>🤖</span>
                                <span style={{ fontWeight: '700', fontSize: '16px', color: '#34d399' }}>
                                    SmartLedger AI Virtual CA — Cashflow &amp; Tax Insight
                                </span>
                            </div>
                            <button
                                onClick={generateAiAnalyticsInsight}
                                disabled={generatingInsight}
                                style={{
                                    background: 'transparent',
                                    border: '1px solid rgba(52, 211, 153, 0.4)',
                                    color: '#34d399',
                                    padding: '6px 14px',
                                    borderRadius: '10px',
                                    fontSize: '12px',
                                    fontWeight: '600',
                                    cursor: generatingInsight ? 'not-allowed' : 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '6px'
                                }}
                            >
                                <RefreshCw size={14} className={generatingInsight ? 'spin' : ''} />
                                <span>{generatingInsight ? 'Analyzing...' : 'Refresh AI Insight'}</span>
                            </button>
                        </div>

                        <div style={{
                            fontSize: '14px',
                            color: 'var(--dash-text)',
                            lineHeight: '1.6',
                            whiteSpace: 'pre-wrap',
                            background: 'rgba(0,0,0,0.2)',
                            padding: '16px',
                            borderRadius: '12px',
                            border: '1px solid rgba(255,255,255,0.05)'
                        }}>
                            {aiInsight || 'Select dates "From" and "To" and a category to generate AI analysis.'}
                        </div>
                    </div>

                    {/* Filtered Transactions Table */}
                    <div className="table-container" style={{
                        background: 'var(--surface-dark)',
                        border: '1px solid var(--border-dark)',
                        borderRadius: '20px',
                        padding: '24px',
                        overflow: 'hidden'
                    }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                            <h3 style={{ margin: 0, fontSize: '18px', fontWeight: '700', color: 'var(--dash-text)' }}>
                                Analysed Transactions ({filteredTransactions.length})
                            </h3>
                            <button
                                onClick={() => setActiveTab('import')}
                                style={{
                                    background: 'linear-gradient(135deg, #34d399, #10b981)',
                                    color: '#000',
                                    border: 'none',
                                    padding: '8px 16px',
                                    borderRadius: '10px',
                                    fontWeight: '700',
                                    fontSize: '13px',
                                    cursor: 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '6px'
                                }}
                            >
                                <span>➕ Import Statement</span>
                            </button>
                        </div>

                        {filteredTransactions.length === 0 ? (
                            <div style={{ textAlign: 'center', padding: '48px 0', color: 'var(--dash-text-muted)' }}>
                                <FileText size={48} style={{ opacity: 0.3, marginBottom: '12px' }} />
                                <div style={{ fontSize: '16px', fontWeight: '600' }}>No matching transactions found</div>
                                <div style={{ fontSize: '13px', marginTop: '4px' }}>
                                    Try expanding your Date Rate "From" and "To" or select "All Categories".
                                </div>
                            </div>
                        ) : (
                            <div style={{ overflowX: 'auto' }}>
                                <table className="transaction-table" style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                                    <thead>
                                        <tr style={{ background: 'rgba(255,255,255,0.03)', color: 'var(--dash-text-muted)', fontSize: '13px' }}>
                                            <th style={{ padding: '14px 12px' }}>Date</th>
                                            <th style={{ padding: '14px 12px' }}>Ref ID / UTR</th>
                                            <th style={{ padding: '14px 12px' }}>Description</th>
                                            <th style={{ padding: '14px 12px' }}>Category</th>
                                            <th style={{ padding: '14px 12px' }}>Type</th>
                                            <th style={{ padding: '14px 12px' }}>Amount</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {filteredTransactions.map((t, index) => {
                                            const isCredit = t.type === 'Credit' || t.type === 'Interest';
                                            return (
                                                <tr key={t.id || index} style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                                                    <td style={{ padding: '14px 12px', fontSize: '14px', color: 'var(--dash-text)' }}>
                                                        {t.date || '-'}
                                                    </td>
                                                    <td style={{ padding: '14px 12px', fontSize: '13px', fontFamily: 'monospace', color: '#38bdf8' }}>
                                                        {t.refId || 'N/A'}
                                                    </td>
                                                    <td style={{ padding: '14px 12px', fontSize: '14px', color: 'var(--dash-text)', fontWeight: '500' }}>
                                                        {t.description || 'Transaction'}
                                                    </td>
                                                    <td style={{ padding: '14px 12px' }}>
                                                        <span style={{
                                                            padding: '4px 10px',
                                                            borderRadius: '20px',
                                                            fontSize: '12px',
                                                            fontWeight: '600',
                                                            background: t.category === 'Food/Dining' ? 'rgba(245, 158, 11, 0.15)'
                                                                : t.category === 'Education' ? 'rgba(99, 102, 241, 0.15)'
                                                                : t.category === 'Recharge' ? 'rgba(236, 72, 153, 0.15)'
                                                                : 'rgba(52, 211, 153, 0.15)',
                                                            color: t.category === 'Food/Dining' ? '#f59e0b'
                                                                : t.category === 'Education' ? '#818cf8'
                                                                : t.category === 'Recharge' ? '#f472b6'
                                                                : '#34d399'
                                                        }}>
                                                            {t.category || 'General'}
                                                        </span>
                                                    </td>
                                                    <td style={{ padding: '14px 12px' }}>
                                                        <span style={{
                                                            color: isCredit ? '#34d399' : '#ef4444',
                                                            fontWeight: '700',
                                                            fontSize: '13px'
                                                        }}>
                                                            {t.type}
                                                        </span>
                                                    </td>
                                                    <td style={{
                                                        padding: '14px 12px',
                                                        fontSize: '15px',
                                                        fontWeight: '700',
                                                        color: isCredit ? '#34d399' : '#ef4444'
                                                    }}>
                                                        {isCredit ? '+' : '-'}₹{parseFloat(t.amount || 0).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                                                    </td>
                                                </tr>
                                            );
                                        })}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>
                </div>
            )}

            {/* TAB 2: IMPORT ACCOUNT STATEMENT & SAVE TO BANK ACCOUNT */}
            {activeTab === 'import' && (
                <div className="vca-import-container">
                    {/* Back Button */}
                    <button
                        onClick={() => setActiveTab('advisor')}
                        style={{
                            display: 'inline-flex', alignItems: 'center', gap: '8px', padding: '8px 16px', borderRadius: '10px', border: '1px solid rgba(255,255,255,0.1)', cursor: 'pointer', fontWeight: '600', fontSize: '13px',
                            background: 'rgba(255,255,255,0.03)', color: '#94a3b8', width: 'fit-content', marginBottom: '16px'
                        }}
                        onMouseOver={(e) => { e.currentTarget.style.background = 'rgba(255,255,255,0.08)'; e.currentTarget.style.color = '#fff'; }}
                        onMouseOut={(e) => { e.currentTarget.style.background = 'rgba(255,255,255,0.03)'; e.currentTarget.style.color = '#94a3b8'; }}
                    >
                        <ArrowRight size={16} style={{ transform: 'rotate(180deg)' }} />
                        <span>Back to Ledger AI Chatbot</span>
                    </button>
                    {/* Account Statement Upload Box with Password field */}
                    <div className="vca-upload-card">
                        <div style={{ textAlign: 'center' }}>
                            <h3 className="vca-upload-title">
                                📄 Select Account Statement (PDF, CSV, or Excel)
                            </h3>
                            <p className="vca-upload-sub">
                                Upload your bank statement to automatically extract reference IDs and categorize transactions with AI.
                            </p>
                        </div>

                        {/* Drag and Drop File Input Area */}
                        <label
                            className="vca-dropzone"
                            onDragOver={(e) => { e.preventDefault(); e.stopPropagation(); }}
                            onDrop={(e) => {
                                e.preventDefault();
                                e.stopPropagation();
                                if (e.dataTransfer.files && e.dataTransfer.files[0]) {
                                    setAiFile(e.dataTransfer.files[0]);
                                }
                            }}
                        >
                            <Upload size={36} color="#818cf8" />
                            <div style={{ textAlign: 'center' }}>
                                <span style={{ color: '#e2e8f0', fontWeight: '600', fontSize: '15px' }}>
                                    {aiFile ? aiFile.name : 'Click to select account statement file'}
                                </span>
                                <div style={{ color: '#64748b', fontSize: '12px', marginTop: '4px' }}>
                                    Supports .pdf, .csv, and .xlsx files
                                </div>
                            </div>
                            <input
                                type="file"
                                accept=".pdf,.csv,.xlsx"
                                onChange={(e) => setAiFile(e.target.files[0])}
                                style={{ display: 'none' }}
                            />
                        </label>

                        {/* Password Protection Input - Only appears if PDF is protected */}
                        {isPdfProtected && (
                            <div style={{
                                width: '100%',
                                maxWidth: '380px',
                                display: 'flex',
                                flexDirection: 'column',
                                gap: '8px',
                                background: 'rgba(248, 113, 113, 0.08)',
                                border: '1px solid rgba(248, 113, 113, 0.25)',
                                borderRadius: '14px',
                                padding: '14px 18px',
                                transition: 'all 0.3s ease',
                                animation: 'vcaFadeIn 0.3s ease'
                            }}>
                                <label className="vca-metric-label" style={{ justifyContent: 'center', color: '#fca5a5' }}>
                                    <span className="metric-dot" style={{ background: '#f87171' }}></span>
                                    Statement Password (Protected PDF)
                                </label>
                                <input
                                    type="password"
                                    placeholder="Enter statement PDF password"
                                    value={pdfPassword}
                                    onChange={(e) => setPdfPassword(e.target.value)}
                                    className="vca-metric-input"
                                    style={{ textAlign: 'center', borderColor: 'rgba(248, 113, 113, 0.4)' }}
                                    autoFocus
                                />
                            </div>
                        )}

                        <button
                            onClick={handleAiParseStatement}
                            disabled={aiLoading || !aiFile}
                            className="vca-extract-btn"
                        >
                            {aiLoading ? (
                                <>
                                    <RefreshCw size={18} className="spin" />
                                    <span>Extracting Transactions with AI...</span>
                                </>
                            ) : (
                                <>
                                    <Sparkles size={18} />
                                    <span>Extract Transactions from Statement</span>
                                </>
                            )}
                        </button>
                    </div>

                    {/* Success Alert if Saved */}
                    {importSuccessMsg && (
                        <div className="vca-success-alert">
                            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', color: '#6ee7b7', fontWeight: '600' }}>
                                <CheckCircle size={20} />
                                <span>{importSuccessMsg}</span>
                            </div>
                            <button
                                onClick={() => { setActiveTab('analyse'); }}
                                style={{
                                    background: 'linear-gradient(135deg, #6366f1, #8b5cf6)',
                                    color: '#fff',
                                    border: 'none',
                                    padding: '8px 16px',
                                    borderRadius: '12px',
                                    fontWeight: '700',
                                    cursor: 'pointer',
                                    fontSize: '12px'
                                }}
                            >
                                View in Analyse Tab →
                            </button>
                        </div>
                    )}

                    {/* Extracted Transactions List & Saving to Bank Account */}
                    {extractedTransactions.length > 0 && (
                        <div className="vca-extracted-card">
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
                                <div>
                                    <h3 style={{ margin: 0, fontSize: '20px', fontWeight: '700', color: 'var(--dash-text)' }}>
                                        Extracted Statement Transactions ({extractedTransactions.length})
                                    </h3>
                                    <p style={{ margin: '4px 0 0 0', fontSize: '13px', color: 'var(--dash-text-muted)' }}>
                                        Review extracted reference IDs and categories before saving to your ledger.
                                    </p>
                                </div>

                                {/* Bank Account Selector: "selecting bank account whatever is already there" */}
                                <div style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '12px',
                                    background: 'rgba(0,0,0,0.3)',
                                    padding: '10px 16px',
                                    borderRadius: '14px',
                                    border: '1px solid var(--border-dark)'
                                }}>
                                    <Landmark size={20} color="#34d399" />
                                    <div>
                                        <label style={{ display: 'block', fontSize: '11px', fontWeight: '600', color: 'var(--dash-text-muted)' }}>
                                            SAVE TO BANK ACCOUNT (WHATEVER IS ALREADY THERE):
                                        </label>
                                        <select
                                            value={selectedBankAccountId}
                                            onChange={(e) => setSelectedBankAccountId(e.target.value)}
                                            style={{
                                                background: 'transparent',
                                                color: 'var(--dash-text)',
                                                border: 'none',
                                                fontWeight: '700',
                                                fontSize: '14px',
                                                outline: 'none',
                                                cursor: 'pointer',
                                                padding: '4px 0 0 0'
                                            }}
                                        >
                                            <option value="" style={{ background: 'var(--bg-dark)', color: 'var(--dash-text)' }}>
                                                Select Bank Account...
                                            </option>
                                            {bankAccounts.map(acc => (
                                                <option key={acc.id} value={acc.id} style={{ background: 'var(--bg-dark)', color: 'var(--dash-text)' }}>
                                                    {acc.name} — {acc.bankName} ({acc.accountNumber || 'N/A'}) • Balance: ₹{parseFloat(acc.balance).toLocaleString('en-IN')}
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                </div>
                            </div>

                            {/* Save Button Bar */}
                            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
                                <button
                                    onClick={() => setExtractedTransactions([])}
                                    style={{
                                        background: 'rgba(255, 255, 255, 0.05)',
                                        border: '1px solid var(--border-dark)',
                                        color: 'var(--dash-text)',
                                        padding: '10px 20px',
                                        borderRadius: '12px',
                                        fontWeight: '600',
                                        cursor: 'pointer'
                                    }}
                                >
                                    Cancel
                                </button>
                                <button
                                    onClick={handleSaveExtractedTransactions}
                                    disabled={savingTransactions || !selectedBankAccountId}
                                    style={{
                                        background: 'linear-gradient(135deg, #10b981, #059669)',
                                        color: '#fff',
                                        border: 'none',
                                        padding: '12px 28px',
                                        borderRadius: '12px',
                                        fontWeight: '700',
                                        fontSize: '14px',
                                        cursor: savingTransactions || !selectedBankAccountId ? 'not-allowed' : 'pointer',
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: '8px',
                                        boxShadow: '0 4px 15px rgba(16, 185, 129, 0.3)'
                                    }}
                                >
                                    <Download size={18} />
                                    <span>{savingTransactions ? 'Saving Transactions...' : `Save ${extractedTransactions.length} Transactions to Selected Bank Account`}</span>
                                </button>
                            </div>

                            {/* AI Statement Summary & Cashflow Overview */}
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px' }}>
                                <div className="stat-card" style={{
                                    background: 'var(--surface-dark)',
                                    border: '1px solid var(--border-dark)',
                                    borderRadius: '20px',
                                    padding: '20px',
                                    display: 'flex',
                                    flexDirection: 'column',
                                    gap: '8px',
                                    boxShadow: '0 4px 15px rgba(0,0,0,0.1)'
                                }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                        <span style={{ fontSize: '13px', color: 'var(--dash-text-muted)', fontWeight: '600' }}>Total No. of Transactions</span>
                                        <FileText size={18} color="#a78bfa" />
                                    </div>
                                    <span style={{ fontSize: '26px', fontWeight: '700', color: '#a78bfa' }}>
                                        {statementSummary.totalTxns}
                                    </span>
                                    <span style={{ fontSize: '11px', color: 'var(--dash-text-muted)' }}>
                                        Fetched by AI Statement Parser
                                    </span>
                                </div>

                                <div className="stat-card" style={{
                                    background: 'var(--surface-dark)',
                                    border: '1px solid var(--border-dark)',
                                    borderRadius: '20px',
                                    padding: '20px',
                                    display: 'flex',
                                    flexDirection: 'column',
                                    gap: '8px',
                                    boxShadow: '0 4px 15px rgba(0,0,0,0.1)'
                                }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                        <span style={{ fontSize: '13px', color: 'var(--dash-text-muted)', fontWeight: '600' }}>Total Amount Credited</span>
                                        <TrendingUp size={18} color="#34d399" />
                                    </div>
                                    <span style={{ fontSize: '26px', fontWeight: '700', color: '#34d399' }}>
                                        ₹{statementSummary.credited.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                                    </span>
                                    <span style={{ fontSize: '11px', color: 'var(--dash-text-muted)' }}>
                                        Total inflow / deposits
                                    </span>
                                </div>

                                <div className="stat-card" style={{
                                    background: 'var(--surface-dark)',
                                    border: '1px solid var(--border-dark)',
                                    borderRadius: '20px',
                                    padding: '20px',
                                    display: 'flex',
                                    flexDirection: 'column',
                                    gap: '8px',
                                    boxShadow: '0 4px 15px rgba(0,0,0,0.1)'
                                }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                        <span style={{ fontSize: '13px', color: 'var(--dash-text-muted)', fontWeight: '600' }}>Total Amount Debited</span>
                                        <TrendingDown size={18} color="#ef4444" />
                                    </div>
                                    <span style={{ fontSize: '26px', fontWeight: '700', color: '#ef4444' }}>
                                        ₹{statementSummary.debited.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                                    </span>
                                    <span style={{ fontSize: '11px', color: 'var(--dash-text-muted)' }}>
                                        Total outflow / expenses
                                    </span>
                                </div>

                                <div className="stat-card" style={{
                                    background: 'var(--surface-dark)',
                                    border: '1px solid var(--border-dark)',
                                    borderRadius: '20px',
                                    padding: '20px',
                                    display: 'flex',
                                    flexDirection: 'column',
                                    gap: '8px',
                                    boxShadow: '0 4px 15px rgba(0,0,0,0.1)'
                                }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                        <span style={{ fontSize: '13px', color: 'var(--dash-text-muted)', fontWeight: '600' }}>Total Balance Left</span>
                                        <Wallet size={18} color="#38bdf8" />
                                    </div>
                                    <span style={{ fontSize: '26px', fontWeight: '700', color: '#38bdf8' }}>
                                        ₹{statementSummary.totalBalanceLeft.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                                    </span>
                                    <span style={{ fontSize: '11px', color: 'var(--dash-text-muted)' }}>
                                        {selectedBankAccountId ? 'Projected final balance in selected bank account' : 'Net statement flow (Select bank account for total)'}
                                    </span>
                                </div>
                            </div>

                            {/* AI Category Groups Breakdown Summary Box */}
                            <div style={{
                                background: 'linear-gradient(135deg, rgba(52, 211, 153, 0.08), rgba(16, 185, 129, 0.04))',
                                border: '1px solid rgba(52, 211, 153, 0.25)',
                                borderRadius: '20px',
                                padding: '20px',
                                display: 'flex',
                                flexDirection: 'column',
                                gap: '14px'
                            }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#34d399', fontWeight: '700', fontSize: '15px' }}>
                                    <Layers size={18} />
                                    <span>AI Category Groups Breakdown — Statement Overview</span>
                                </div>

                                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '12px' }}>
                                    {statementSummary.groupBreakdown.map((g, idx) => (
                                        <div key={idx} style={{
                                            background: 'rgba(0, 0, 0, 0.25)',
                                            border: '1px solid var(--border-dark)',
                                            borderRadius: '14px',
                                            padding: '14px',
                                            display: 'flex',
                                            flexDirection: 'column',
                                            gap: '4px'
                                        }}>
                                            <span style={{ fontSize: '13px', fontWeight: '700', color: 'var(--dash-text)' }}>
                                                {g.groupName}
                                            </span>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '4px' }}>
                                                <span style={{ fontSize: '12px', color: 'var(--dash-text-muted)' }}>
                                                    {g.count} transaction{g.count !== 1 ? 's' : ''}
                                                </span>
                                                <span style={{ fontSize: '14px', fontWeight: '700', color: '#34d399' }}>
                                                    ₹{g.totalAmount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                                                </span>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>

                            {/* Table of Extracted Transactions */}
                            <div style={{ overflowX: 'auto', border: '1px solid var(--border-dark)', borderRadius: '16px' }}>
                                <table className="transaction-table" style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                                    <thead>
                                        <tr style={{ background: 'rgba(255,255,255,0.03)', color: 'var(--dash-text-muted)', fontSize: '13px' }}>
                                            <th style={{ padding: '14px 12px' }}>Date</th>
                                            <th style={{ padding: '14px 12px' }}>Ref ID / UTR</th>
                                            <th style={{ padding: '14px 12px' }}>Description</th>
                                            <th style={{ padding: '14px 12px' }}>Amount</th>
                                            <th style={{ padding: '14px 12px' }}>Type</th>
                                            <th style={{ padding: '14px 12px' }}>Category</th>
                                            <th style={{ padding: '14px 12px' }}>Category Group</th>
                                            <th style={{ padding: '14px 12px' }}>Tax Section</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {extractedTransactions.map((t, idx) => (
                                            <tr key={idx} style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                                                <td style={{ padding: '12px', fontSize: '14px', color: 'var(--dash-text)' }}>
                                                    {t.date}
                                                </td>
                                                <td style={{ padding: '12px', fontSize: '13px', fontFamily: 'monospace', color: '#38bdf8' }}>
                                                    {t.refId || '-'}
                                                </td>
                                                <td style={{ padding: '12px', fontSize: '14px', color: 'var(--dash-text)', fontWeight: '500' }}>
                                                    {t.description}
                                                </td>
                                                <td style={{ padding: '12px', fontSize: '14px', fontWeight: '700' }}>
                                                    ₹{parseFloat(t.amount || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                                                </td>
                                                <td style={{ padding: '12px' }}>
                                                    <span style={{
                                                        color: t.type === 'CREDIT' ? '#34d399' : '#ef4444',
                                                        fontWeight: '700',
                                                        fontSize: '13px'
                                                    }}>
                                                        {t.type}
                                                    </span>
                                                </td>
                                                <td style={{ padding: '12px' }}>
                                                    <select
                                                        value={t.category || 'Others'}
                                                        onChange={(e) => {
                                                            const val = e.target.value;
                                                            setExtractedTransactions(prev => prev.map((item, i) => i === idx ? { ...item, category: val } : item));
                                                        }}
                                                        style={{
                                                            padding: '6px 10px',
                                                            borderRadius: '8px',
                                                            background: 'var(--bg-dark)',
                                                            color: 'var(--dash-text)',
                                                            border: '1px solid var(--border-dark)',
                                                            fontSize: '12px',
                                                            fontWeight: '600'
                                                        }}
                                                    >
                                                        {allCategories.filter(c => c !== 'All').map(opt => (
                                                            <option key={opt} value={opt} style={{ background: 'var(--bg-dark)', color: 'var(--dash-text)' }}>
                                                                {opt}
                                                            </option>
                                                        ))}
                                                    </select>
                                                </td>
                                                <td style={{ padding: '12px' }}>
                                                    <span style={{
                                                        padding: '4px 10px',
                                                        borderRadius: '16px',
                                                        fontSize: '11px',
                                                        fontWeight: '700',
                                                        background: 'rgba(52, 211, 153, 0.12)',
                                                        color: '#34d399',
                                                        border: '1px solid rgba(52, 211, 153, 0.25)',
                                                        display: 'inline-block'
                                                    }}>
                                                        {getTransactionGroup(t.category || 'General')}
                                                    </span>
                                                </td>
                                                <td style={{ padding: '12px', color: '#818cf8', fontWeight: '600', fontSize: '13px' }}>
                                                    {t.taxSection || '-'}
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    )}
                </div>
            )}

            {activeTab === 'advisor' && (
                <div className="vca-container">
                    {/* Conversations Bar */}
                    <div style={{ display: 'flex', gap: '10px', padding: '12px 32px', background: 'transparent', alignItems: 'center', overflowX: 'auto', borderBottom: '1px solid var(--dash-border)', flexShrink: 0 }}>
                        <button 
                            type="button"
                            onClick={() => {
                                if (chatHistory.length > 1) {
                                    if (conversations.length >= 5) {
                                        alert("You must delete an old conversation before starting a new one (maximum 5 saved conversations).");
                                        return;
                                    }
                                    setConversations([...conversations, { id: Date.now(), history: chatHistory }]);
                                }
                                setChatHistory([chatHistory[0]]);
                            }}
                            style={{ padding: '6px 12px', background: '#34d399', color: '#000', borderRadius: '8px', border: 'none', fontWeight: 'bold', cursor: 'pointer', whiteSpace: 'nowrap', display: 'flex', alignItems: 'center', gap: '6px' }}
                        >
                            <span>+</span> New Conversation
                        </button>
                        
                        <div style={{ width: '1px', height: '24px', background: 'var(--dash-border)', margin: '0 8px' }}></div>
                        
                        {conversations.map((conv, idx) => (
                            <div key={conv.id} style={{ display: 'flex', alignItems: 'center', background: 'var(--dash-glass-bg, rgba(255,255,255,0.05))', border: '1px solid var(--dash-border)', borderRadius: '8px', padding: '4px 8px', gap: '8px' }}>
                                <span style={{ fontSize: '13px', color: 'var(--dash-text-muted, #c7d2fe)', cursor: 'pointer' }} onClick={() => setChatHistory(conv.history)}>
                                    Chat {idx + 1}
                                </span>
                                <button 
                                    type="button"
                                    onClick={() => setConversations(conversations.filter(c => c.id !== conv.id))}
                                    style={{ background: 'transparent', border: 'none', color: '#ef4444', cursor: 'pointer', padding: '0 4px', fontSize: '16px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                                >
                                    &times;
                                </button>
                            </div>
                        ))}
                    </div>


                    {/* Quick Suggestion Pills */}
                    <div className="vca-suggestions">
                        <span className="vca-suggest-label">💡 Try asking:</span>
                        {[
                            "How can I optimize my taxes under Section 80C & 80D?",
                            "How should I budget my monthly income using 50-30-20 rule?",
                            "What is the difference between Old and New Tax Regime?",
                            "Who won the cricket match yesterday? (Test Guardrail)"
                        ].map((prompt, index) => (
                            <button
                                key={index}
                                onClick={() => setCaQuery(prompt)}
                                className="vca-suggest-pill"
                            >
                                {prompt}
                            </button>
                        ))}
                    </div>

                    {/* Conversation Window */}
                    <div className="vca-chat-area">
                        <div className="vca-chat-scroll">
                            {chatHistory.map((msg, idx) => (
                                <div key={idx} className={`vca-msg ${msg.sender}`}>
                                    <div className="vca-msg-avatar" style={msg.sender === 'ai' ? { padding: 0, overflow: 'hidden' } : {}}>
                                        {msg.sender === 'ai' ? (
                                            <img
                                                src="/assets/logo.png"
                                                alt="Ledger AI"
                                                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                                            />
                                        ) : '👤'}
                                    </div>
                                    <div className="vca-msg-bubble">
                                        {msg.sender === 'ai' && (
                                            <div className="vca-msg-label">
                                                <Sparkles size={12} />
                                                <span>Ledger AI</span>
                                            </div>
                                        )}
                                        {/* Render AI message: structured object or markdown string */}
                                        {msg.sender === 'ai' && typeof msg.text === 'object' && msg.text.sections ? (
                                            <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                                                <p style={{ fontSize: '13px', color: '#94a3b8', margin: '0 0 8px', fontStyle: 'italic' }}>
                                                    Regarding: "{msg.text.question}"
                                                </p>
                                                {/* Charts Row */}
                                                {msg.text.charts?.length > 0 && (
                                                    <div style={{
                                                        display: 'flex', flexWrap: 'wrap', gap: '20px', justifyContent: 'center',
                                                        padding: '16px', background: 'rgba(0,0,0,0.15)', borderRadius: '16px',
                                                        border: '1px solid rgba(99,102,241,0.08)', margin: '4px 0 12px'
                                                    }}>
                                                        {msg.text.charts.map((chart, ci) => (
                                                            <div key={ci}>
                                                                {chart.type === 'donut' && <MiniDonut segments={chart.data} label={chart.label} size={130} />}
                                                                {chart.type === 'bar' && (
                                                                    <div style={{ minWidth: '280px', flex: 1 }}>
                                                                        <div style={{ fontSize: '11px', color: '#64748b', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '10px' }}>Category Breakdown</div>
                                                                        <HBarChart items={chart.data} />
                                                                    </div>
                                                                )}
                                                            </div>
                                                        ))}
                                                    </div>
                                                )}
                                                {/* Sections */}
                                                {msg.text.sections.map((sec, si) => (
                                                    <div key={si} style={{ margin: '4px 0' }}>
                                                        <h4 style={{ fontSize: '14px', fontWeight: '700', color: '#a5b4fc', margin: '12px 0 6px' }}>{sec.title}</h4>
                                                        <StatRow items={sec.stats.map(s => ({
                                                            ...s,
                                                            value: s.raw ? s.value : s.value
                                                        }))} />
                                                        {sec.detail && (
                                                            <p style={{ fontSize: '13px', color: '#94a3b8', margin: '6px 0 0', lineHeight: '1.6' }}>{sec.detail}</p>
                                                        )}
                                                    </div>
                                                ))}
                                                <p style={{ fontSize: '11.5px', color: '#475569', margin: '12px 0 0', fontStyle: 'italic' }}>
                                                    All SmartLedger modules are fully synchronized with Ledger AI.
                                                </p>
                                            </div>
                                        ) : msg.sender === 'ai' && typeof msg.text === 'string' ? (
                                            <div>{renderMarkdown(msg.text)}</div>
                                        ) : (
                                            <span>{msg.text}</span>
                                        )}
                                    </div>
                                </div>
                            ))}
                            {caLoading && (
                                <div className="vca-typing">
                                    <div className="vca-msg-avatar" style={{ width: '32px', height: '32px', borderRadius: '10px', padding: 0, overflow: 'hidden' }}>
                                        <img
                                            src="/assets/logo.png"
                                            alt="Ledger AI"
                                            style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                                        />
                                    </div>
                                    <div className="vca-typing-dots">
                                        <span></span><span></span><span></span>
                                    </div>
                                    <span className="vca-typing-text">Ledger AI is analyzing financial data...</span>
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Quick Portfolio Suggestion Pills */}
                    <div className="vca-portfolio-pills">
                        {[
                            "What is my Overview & Net Balance?",
                            "How much did I spend on Food & Bills?",
                            "Summarize my Live Investments",
                            "Who owes me money in Len-Den?",
                            "What are my active Loans & EMI?"
                        ].map((q, idx) => (
                            <button
                                key={idx}
                                type="button"
                                onClick={() => handleCaAdvisorQuery(null, q)}
                                disabled={caLoading}
                                className="vca-portfolio-pill"
                            >
                                <span>📊</span> {q}
                            </button>
                        ))}
                    </div>

                    {/* Ask CA Chat Form */}
                    <form onSubmit={handleCaAdvisorQuery} className="vca-input-bar">
                        <button
                            type="button"
                            onClick={() => setActiveTab('import')}
                            style={{
                                display: 'flex', alignItems: 'center', gap: '6px', padding: '12px 16px', borderRadius: '14px',
                                border: '1px solid var(--dash-border)', cursor: 'pointer', fontWeight: '600', fontSize: '13px',
                                background: 'var(--dash-glass-bg, rgba(255,255,255,0.03))',
                                color: 'var(--dash-text-muted, #94a3b8)',
                                transition: 'all 0.3s ease', whiteSpace: 'nowrap', flexShrink: 0
                            }}
                        >
                            <Upload size={15} />
                            {extractedTransactions.length > 0 && (
                                <span style={{ background: '#ef4444', color: '#fff', borderRadius: '10px', padding: '2px 6px', fontSize: '10px' }}>
                                    {extractedTransactions.length}
                                </span>
                            )}
                        </button>
                        <input
                            type="text"
                            value={caQuery}
                            onChange={(e) => setCaQuery(e.target.value)}
                            placeholder="Ask Ledger AI anything about finance & taxes..."
                            className="vca-chat-input"
                        />
                        <button
                            type="submit"
                            disabled={caLoading || !caQuery.trim()}
                            className="vca-send-btn"
                        >
                            <Sparkles size={18} />
                            <span>{caLoading ? 'Consulting...' : 'Send Query'}</span>
                        </button>
                    </form>
                </div>
            )}
        </div>
    );
}
