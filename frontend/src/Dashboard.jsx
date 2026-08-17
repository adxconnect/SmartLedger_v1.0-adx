import React, { useState, useEffect, useCallback, useRef } from 'react';
import Cropper from 'react-easy-crop';
import getCroppedImg from './cropImage';
import { auth, db } from './firebase';
import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import { collection, query, where, getDocs, addDoc, doc, setDoc, getDoc } from 'firebase/firestore';
import { ref, uploadBytes, getDownloadURL } from 'firebase/storage';
import { updateProfile } from 'firebase/auth';
import { LayoutDashboard, ArrowRightLeft, Landmark, CreditCard, PiggyBank, Coins, TrendingUp, Handshake, ReceiptText, PieChart, FileText, LogOut, Sun, Moon, User, Shield, Lock, Settings, Camera, Bell, ArrowLeft, MoreHorizontal, Edit2, Trash2, MessageSquare, List, BarChart3 } from 'lucide-react';
import AiVirtualCaImportView from './AiVirtualCaImportView';
import { API_BASE_URL_PYTHON, API_BASE_URL_JAVA } from './config';

const LiveTracker = ({ principalAmount, interestRate, dateOfAccountOpening, interestType, dateOfMaturity, isRD }) => {
    const [income, setIncome] = useState(0);

    useEffect(() => {
        if (!principalAmount || !interestRate || !dateOfAccountOpening) {
            setIncome(0);
            return;
        }

        const p = parseFloat(principalAmount);
        const r = parseFloat(interestRate);
        const openingDate = new Date(dateOfAccountOpening);

        const getMultiplier = (type) => {
            if (type === 'Quarter' || type === 'Quarterly') return 4;
            if (type === 'Monthly') return 12;
            if (type === 'Per Day' || type === 'Daily') return 365.25;
            return 1;
        };
        const multiplier = getMultiplier(interestType);

        const intervalId = setInterval(() => {
            const now = new Date();
            let elapsedMs = now - openingDate;

            if (dateOfMaturity && dateOfMaturity !== '-') {
                const matureDate = new Date(dateOfMaturity);
                if (!isNaN(matureDate) && now > matureDate) {
                    elapsedMs = matureDate - openingDate;
                }
            }

            if (elapsedMs < 0) {
                setIncome(0);
                return;
            }

            if (isRD) {
                const elapsedMonths = elapsedMs / (1000 * 60 * 60 * 24 * 30.4375);
                const payments = Math.floor(elapsedMonths) + 1; // Number of monthly payments made
                let totalInterest = 0;
                const compoundFreq = multiplier;

                for (let i = 0; i < payments; i++) {
                    const paymentElapsedMs = elapsedMs - (i * 1000 * 60 * 60 * 24 * 30.4375);
                    if (paymentElapsedMs > 0) {
                        const years = paymentElapsedMs / (1000 * 60 * 60 * 24 * 365.25);
                        const amount = p * Math.pow(1 + (r / 100) / compoundFreq, compoundFreq * years);
                        totalInterest += (amount - p);
                    }
                }
                setIncome(totalInterest);
            } else {
                const compoundFreq = multiplier;
                const elapsedYears = elapsedMs / (1000 * 60 * 60 * 24 * 365.25);
                const totalAmount = p * Math.pow(1 + (r / 100) / compoundFreq, compoundFreq * elapsedYears);
                const currentIncome = totalAmount - p;
                setIncome(currentIncome);
            }
        }, 1000);

        return () => clearInterval(intervalId);
    }, [principalAmount, interestRate, dateOfAccountOpening, interestType, dateOfMaturity, isRD]);

    return (
        <span style={{ color: '#34d399', fontWeight: 'bold' }}>
            + ₹{income.toLocaleString(undefined, { minimumFractionDigits: 6, maximumFractionDigits: 6 })}
        </span>
    );
};

const MaturityTracker = ({ principalAmount, interestRate, dateOfAccountOpening, interestType, dateOfMaturity, isRD }) => {
    const [maturityValue, setMaturityValue] = useState(parseFloat(principalAmount) || 0);

    useEffect(() => {
        if (!principalAmount || !interestRate || !dateOfAccountOpening) {
            setMaturityValue(parseFloat(principalAmount) || 0);
            return;
        }

        const p = parseFloat(principalAmount);
        const r = parseFloat(interestRate);
        const openingDate = new Date(dateOfAccountOpening);

        const getMultiplier = (type) => {
            if (type === 'Quarter' || type === 'Quarterly') return 4;
            if (type === 'Monthly') return 12;
            if (type === 'Per Day' || type === 'Daily') return 365.25;
            return 1;
        };
        const multiplier = getMultiplier(interestType);

        const intervalId = setInterval(() => {
            const now = new Date();
            let elapsedMs = now - openingDate;

            if (dateOfMaturity && dateOfMaturity !== '-') {
                const matureDate = new Date(dateOfMaturity);
                if (!isNaN(matureDate) && now > matureDate) {
                    elapsedMs = matureDate - openingDate;
                }
            }

            if (elapsedMs < 0) {
                setMaturityValue(p);
                return;
            }

            if (isRD) {
                const elapsedMonths = elapsedMs / (1000 * 60 * 60 * 24 * 30.4375);
                const payments = Math.floor(elapsedMonths) + 1; // Number of monthly payments made
                let totalInterest = 0;
                let totalPrincipal = p * payments;
                const compoundFreq = multiplier;

                for (let i = 0; i < payments; i++) {
                    const paymentElapsedMs = elapsedMs - (i * 1000 * 60 * 60 * 24 * 30.4375);
                    if (paymentElapsedMs > 0) {
                        const years = paymentElapsedMs / (1000 * 60 * 60 * 24 * 365.25);
                        const amount = p * Math.pow(1 + (r / 100) / compoundFreq, compoundFreq * years);
                        totalInterest += (amount - p);
                    }
                }
                setMaturityValue(totalPrincipal + totalInterest);
            } else {
                const compoundFreq = multiplier;
                const elapsedYears = elapsedMs / (1000 * 60 * 60 * 24 * 365.25);
                const totalAmount = p * Math.pow(1 + (r / 100) / compoundFreq, compoundFreq * elapsedYears);
                setMaturityValue(totalAmount);
            }
        }, 1000);

        return () => clearInterval(intervalId);
    }, [principalAmount, interestRate, dateOfAccountOpening, interestType, dateOfMaturity, isRD]);

    return (
        <span style={{ color: 'var(--dash-text)', fontWeight: 'bold' }}>
            ₹{maturityValue.toLocaleString(undefined, { minimumFractionDigits: 4, maximumFractionDigits: 4 })}
        </span>
    );
};

const LiveAssetTracker = ({ assetType, assetSymbol, quantity, amountInvested }) => {
    const [liveValue, setLiveValue] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!assetSymbol || !quantity || !assetType) {
            setLoading(false);
            return;
        }

        const fetchPrice = async () => {
            try {
                let price = 0;
                if (assetType === 'Crypto') {
                    const res = await fetch(`https://api.coingecko.com/api/v3/simple/price?ids=${assetSymbol.toLowerCase()}&vs_currencies=inr&x_cg_demo_api_key=CG-pEM82i2k2UeBRhTE8mmQ1ari`);
                    const data = await res.json();
                    if (data[assetSymbol.toLowerCase()] && data[assetSymbol.toLowerCase()].inr) {
                        price = data[assetSymbol.toLowerCase()].inr;
                    }
                } else if (assetType === 'Stock') {
                    const res = await fetch(`https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=${assetSymbol.toUpperCase()}&apikey=8NVZ6CVEL0QT2Q26`);
                    const data = await res.json();
                    if (data['Global Quote'] && data['Global Quote']['05. price']) {
                        price = parseFloat(data['Global Quote']['05. price']);
                    }
                } else if (assetType === 'Mutual Fund') {
                    const res = await fetch(`https://api.mfapi.in/mf/${assetSymbol}/latest`);
                    const data = await res.json();
                    if (data.data && data.data.length > 0) {
                        price = parseFloat(data.data[0].nav);
                    }
                } else if (assetType === 'Metal - Resource') {
                    const baseSymbol = assetSymbol.split('-')[0];
                    const res = await fetch(`${API_BASE_URL_JAVA}/api/market/metal/${baseSymbol}`);
                    const data = await res.json();
                    
                    if (assetSymbol === 'XAU-22K' && data.price_gram_22k) {
                        price = parseFloat(data.price_gram_22k);
                    } else if (data.price_gram_24k) {
                        price = parseFloat(data.price_gram_24k);
                    } else if (data.price) {
                        price = parseFloat(data.price) / 31.1034768; // fallback to per gram if oz
                    }
                }

                if (price > 0) {
                    setLiveValue(price * quantity);
                }
            } catch (err) {
                console.error('Error fetching live price:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchPrice();
        const interval = setInterval(fetchPrice, 60000);
        return () => clearInterval(interval);
    }, [assetType, assetSymbol, quantity]);

    if (loading) return <span style={{ color: 'var(--dash-text-muted)' }}>Loading...</span>;
    if (liveValue === null) return <span style={{ color: 'var(--dash-text-muted)' }}>-</span>;

    const profit = liveValue - (amountInvested || 0);
    const isProfit = profit >= 0;

    return (
        <div style={{ display: 'flex', flexDirection: 'column' }}>
            <span style={{ fontWeight: 'bold', color: 'var(--dash-text)' }}>
                ₹{liveValue.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </span>
            {amountInvested > 0 && (
                <span style={{ fontSize: '11px', fontWeight: 'bold', color: isProfit ? '#34d399' : '#ef4444' }}>
                    {isProfit ? '+' : ''}₹{profit.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })} ({(((liveValue / amountInvested) - 1) * 100).toFixed(2)}%)
                </span>
            )}
        </div>
    );
};

const AssetSymbolAutocomplete = ({ assetType, value, onChange }) => {
    const [query, setQuery] = useState(value || '');
    const [results, setResults] = useState([]);
    const [loading, setLoading] = useState(false);
    const [showDropdown, setShowDropdown] = useState(false);
    const searchCache = useRef({});

    useEffect(() => {
        setQuery(value || '');
    }, [value]);

    useEffect(() => {
        const fetchResults = async () => {
            if (!query || query.length < 2 || !assetType) {
                setResults([]);
                return;
            }

            const cacheKey = `${assetType}-${query.toLowerCase()}`;
            if (searchCache.current[cacheKey]) {
                setResults(searchCache.current[cacheKey]);
                return;
            }

            setLoading(true);
            try {
                let fetchedResults = [];
                if (assetType === 'Crypto') {
                    const res = await fetch(`https://api.coingecko.com/api/v3/search?query=${query}`);
                    const data = await res.json();
                    if (data.coins) {
                        fetchedResults = data.coins.slice(0, 50).map(c => ({ id: c.id, name: `${c.name} (${c.symbol.toUpperCase()})` }));
                    }
                } else if (assetType === 'Stock') {
                    const res = await fetch(`https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=${query}&apikey=8NVZ6CVEL0QT2Q26`);
                    const data = await res.json();
                    if (data.bestMatches) {
                        fetchedResults = data.bestMatches.slice(0, 25).map(m => ({ id: m['1. symbol'], name: `${m['2. name']} (${m['4. region']})` }));
                    }
                } else if (assetType === 'Mutual Fund') {
                    const res = await fetch(`https://api.mfapi.in/mf/search?q=${query}`);
                    const data = await res.json();
                    if (Array.isArray(data)) {
                        fetchedResults = data.slice(0, 50).map(m => ({ id: m.schemeCode.toString(), name: m.schemeName }));
                    }
                }

                searchCache.current[cacheKey] = fetchedResults;
                setResults(fetchedResults);
            } catch (err) {
                console.error("Search error", err);
            } finally {
                setLoading(false);
            }
        };

        const timer = setTimeout(fetchResults, 250);
        return () => clearTimeout(timer);
    }, [query, assetType]);

    const handleSelect = (item) => {
        setQuery(item.id);
        setShowDropdown(false);
        onChange({ target: { name: 'assetSymbol', value: item.id } });
        onChange({ target: { name: 'investmentName', value: item.name } });
    };

    if (assetType === 'Metal - Resource') {
        const metals = [
            { id: 'XAU', name: 'Physical Gold (24K) - Per Gram' },
            { id: 'XAU-22K', name: 'Physical Gold (22K) - Per Gram' },
            { id: 'XAG', name: 'Physical Silver - Per Gram' },
            { id: 'XPT', name: 'Physical Platinum - Per Gram' },
            { id: 'XPD', name: 'Physical Palladium - Per Gram' }
        ];
        return (
            <select
                value={value || ''}
                onChange={(e) => {
                    const selected = metals.find(m => m.id === e.target.value);
                    if (selected) {
                        onChange({ target: { name: 'assetSymbol', value: selected.id } });
                        onChange({ target: { name: 'investmentName', value: selected.name } });
                    } else {
                        onChange({ target: { name: 'assetSymbol', value: '' } });
                    }
                }}
                style={{ width: '100%', padding: '10px', borderRadius: '8px', background: 'var(--bg-dark)', color: 'var(--text-main)', border: '1px solid var(--border-dark)' }}
                required
            >
                <option value="" style={{ background: 'var(--bg-dark)', color: 'var(--text-main)' }}>Select Metal...</option>
                {metals.map(m => (
                    <option key={m.id} value={m.id} style={{ background: 'var(--bg-dark)', color: 'var(--text-main)' }}>
                        {m.name} (ID: {m.id})
                    </option>
                ))}
            </select>
        );
    }

    return (
        <div style={{ position: 'relative' }}>
            <input
                type="text"
                name="assetSymbol"
                value={query}
                onChange={(e) => {
                    setQuery(e.target.value);
                    setShowDropdown(true);
                    onChange(e);
                }}
                onFocus={() => setShowDropdown(true)}
                onBlur={() => setTimeout(() => setShowDropdown(false), 200)}
                placeholder={`Search ${assetType || 'asset'}...`}
                style={{ width: '100%', padding: '10px', borderRadius: '8px', background: 'var(--bg-dark)', color: 'var(--text-main)', border: '1px solid var(--border-dark)', outline: 'none' }}
                autoComplete="off"
                required
            />
            {showDropdown && (results.length > 0 || loading) && (
                <div style={{ position: 'absolute', top: '100%', left: 0, right: 0, background: 'var(--surface-dark)', border: '1px solid var(--border-dark)', borderRadius: '8px', marginTop: '4px', zIndex: 100, maxHeight: '200px', overflowY: 'auto', boxShadow: '0 4px 20px rgba(0,0,0,0.5)' }}>
                    {loading ? (
                        <div style={{ padding: '12px', color: 'var(--text-muted)', fontSize: '13px' }}>Searching...</div>
                    ) : (
                        results.map(item => (
                            <div
                                key={item.id}
                                onMouseDown={() => handleSelect(item)}
                                style={{ padding: '10px 12px', cursor: 'pointer', borderBottom: '1px solid rgba(255,255,255,0.05)', fontSize: '13px', transition: 'background 0.2s' }}
                                onMouseOver={(e) => e.currentTarget.style.background = 'rgba(52, 211, 153, 0.1)'}
                                onMouseOut={(e) => e.currentTarget.style.background = 'transparent'}
                            >
                                <div style={{ color: 'var(--text-main)', fontWeight: '500' }}>{item.name}</div>
                                <div style={{ color: 'var(--text-muted)', fontSize: '11px', marginTop: '2px' }}>ID: {item.id}</div>
                            </div>
                        ))
                    )}
                </div>
            )}
        </div>
    );
};

const MODULES = [
    { id: 'overview', label: 'Overview Dashboard', isSummary: true, icon: LayoutDashboard },
    {
        id: 'transactions', label: 'Transactions', endpoint: '/api/transactions', icon: ArrowRightLeft,
        fields: [
            { name: 'date', type: 'date' },
            { name: 'refId', type: 'text', label: 'Ref ID / UTR' },
            { name: 'description', type: 'text' },
            { name: 'amount', type: 'number' },
            { name: 'type', type: 'select', options: ['Debit', 'Credit', 'Interest'], label: 'Transaction Type' },
            { name: 'category', type: 'select', options: [] },
            { name: 'accountType', type: 'select', options: ['Savings Account', 'Current Account', 'Fixed Deposit', 'Recurring Deposit', 'Gullak-cash', 'Credit Card'] },
            { name: 'accountId', type: 'select', options: [] }
        ]
    },
    {
        id: 'bankaccounts', label: 'Bank Accounts', endpoint: '/api/bankaccounts', icon: Landmark,
        fields: [
            { name: 'accountName', type: 'text' },
            { name: 'accountNumber', type: 'text' },
            { name: 'ifscCode', type: 'text', label: 'IFSC Code' },
            { name: 'bankName', type: 'text' },
            { name: 'branchName', type: 'text' },
            { name: 'accountType', type: 'select', options: ['Savings Account', 'Current Account', 'Fixed Deposit', 'Recurring Deposit'] },
            { name: 'upiId', type: 'text', label: 'UPI ID' },
            { name: 'interestRate', type: 'number' },
            { name: 'creditPeriod', label: 'Interest Type', type: 'select', options: ['Annual', 'Quarter', 'Monthly', 'Per Day'] },
            { name: 'dateOfMature', type: 'date', label: 'Date of Mature' },
            { name: 'dateOfAccountOpening', type: 'date', label: 'Date of Account Opening' },
            { name: 'balance', type: 'number', label: 'Total Deposit Balance' },
            { name: 'connectedCardId', type: 'select', options: [], label: 'Connected Card' }
        ]
    },
    {
        id: 'cards', label: 'Cards', endpoint: '/api/cards', icon: CreditCard,
        fields: [
            { name: 'cardName', type: 'text' },
            { name: 'cardType', type: 'select', options: ['Credit', 'Debit'] },
            { name: 'cardNetwork', type: 'select', options: ['VISA', 'Mastercard', 'Rupay', 'Others (Mention)'] },
            { name: 'cardNetworkOther', type: 'text', label: 'Other Card Network' },
            { name: 'cardNumber', type: 'text' },
            { name: 'cvv', type: 'password', label: 'CVV' },
            { name: 'cardFeeType', type: 'select', options: ['Lifetime Free', 'FD Based', 'Annual Fee'], label: 'Card Fee Type' },
            { name: 'annualFee', type: 'number', label: 'Annual Fee Amount' },
            { name: 'creditLimit', type: 'number' }
        ]
    },
    {
        id: 'deposits', label: 'Deposits', endpoint: '/api/deposits', icon: PiggyBank,
        fields: [
            { name: 'holderName', label: 'Account Name', type: 'text' },
            { name: 'accountNumber', label: 'Account Number', type: 'text' },
            { name: 'ifscCode', label: 'IFSC Code', type: 'text' },
            { name: 'bankName', label: 'Bank Name', type: 'text' },
            { name: 'branchName', label: 'Branch Name', type: 'text' },
            { name: 'depositType', label: 'Account Type', type: 'select', options: ['FD', 'RD', 'Gullak'] },
            { name: 'interestRate', label: 'Interest Rate', type: 'number' },
            { name: 'interestType', label: 'Interest Type', type: 'select', options: ['Annual', 'Quarter', 'Monthly', 'Per Day'] },
            { name: 'dateOfMaturity', label: 'Date of Mature', type: 'date' },
            { name: 'startDate', label: 'Date of Account Opening', type: 'date' },
            { name: 'principalAmount', label: 'Total Deposit Balance', type: 'number' },
            { name: 'interestAmount', label: 'Interest Amount', type: 'custom' },
            { name: 'maturityValue', label: 'Maturity Value', type: 'custom' }
        ]
    },
    {
        id: 'goldsilverinvestments', label: 'Gold & Silver', endpoint: '/api/goldsilverinvestments', icon: Coins,
        fields: [{ name: 'metalType', type: 'select', options: ['Gold', 'Silver'] }, { name: 'weightGrams', type: 'number' }, { name: 'purchasePrice', type: 'number' }]
    },
    {
        id: 'investments', label: 'Investments', endpoint: '/api/investments', icon: TrendingUp,
        fields: [
            { name: 'assetType', type: 'select', options: ['Crypto', 'Stock', 'Mutual Fund', 'Metal - Resource'], label: 'Asset Type' },
            { name: 'assetSymbol', type: 'asset-search', label: 'Symbol/ID' },
            { name: 'investmentName', type: 'text', label: 'Investment Name (Auto-filled)' },
            { name: 'quantity', type: 'number', label: 'Quantity Owned' },
            { name: 'amountInvested', type: 'number', label: 'Amount Invested (₹)' },
            { name: 'investmentType', type: 'select', options: ['Daily', 'Monthly', 'Quarterly', 'Annually'], label: 'Investment Type' },
            { name: 'startDate', type: 'date', label: 'Investment Date' },
            { name: 'dateOfMaturity', type: 'date', label: 'Date of Maturity' },
            { name: 'currentValue', type: 'custom', label: 'Current Value (Live)' }
        ]
    },
    {
        id: 'lendings', label: 'Len-Den', endpoint: '/api/lendings', icon: Handshake,
        fields: [
            { name: 'borrowerUid', type: 'text', label: 'Borrower UID' },
            { name: 'borrowerName', type: 'text', label: 'Borrower Name' },
            { name: 'principalAmount', type: 'number', label: 'Principal Amount' },
            { name: 'interestRate', type: 'number', label: 'Interest Rate' },
            { name: 'interestType', type: 'select', options: ['Day', 'Month', 'Quarter', 'Annual'], label: 'Interest Type' },
            { name: 'tenure', type: 'number', label: 'Tenure' },
            { name: 'dateLent', type: 'date', label: 'Date of Lending' },
            { name: 'dateOfClosing', type: 'date', label: 'Clear Amount By', readOnly: true },
            { name: 'amountToRepay', type: 'custom', label: 'Amount to Repay' }
        ]
    },
    {
        id: 'loans', label: 'Loans', endpoint: '/api/loans', icon: ReceiptText,
        fields: [
            { name: 'accountName', type: 'text', label: 'Account Name' },
            { name: 'accountNumber', type: 'text', label: 'Account Number' },
            { name: 'loanType', type: 'select', options: ['Personal', 'Car', 'Home', 'Gold', 'Education'], label: 'Loan Type' },
            { name: 'provider', type: 'select', options: ['Government Schemes', 'Bank'], label: 'Provider' },
            { name: 'principalAmount', type: 'number', label: 'Principal Amount' },
            { name: 'interestRate', type: 'number', label: 'Interest Rate (%)' },
            { name: 'interestType', type: 'select', options: ['Day', 'Quarter', 'Monthly', 'Annually'], label: 'Interest Type' },
            { name: 'tenure', type: 'number', label: 'Tenure' },
            { name: 'amountToRepay', type: 'custom', label: 'To Pay' }
        ]
    },
    {
        id: 'mutualfunds', label: 'Mutual Funds', endpoint: '/api/mutualfunds', icon: PieChart,
        fields: [{ name: 'fundName', type: 'text' }, { name: 'units', type: 'number' }, { name: 'nav', type: 'number' }]
    },
    {
        id: 'taxprofiles', label: 'Tax Profiles', endpoint: '/api/taxprofiles', icon: FileText,
        fields: [{ name: 'profileName', type: 'text' }, { name: 'financialYear', type: 'text' }, { name: 'grossIncome', type: 'number' }, { name: 'totalDeductions', type: 'number' }]
    }
];

const countryOptions = [
    { code: 'IN', dial: '+91', name: 'India' },
    { code: 'PK', dial: '+92', name: 'Pakistan' },
    { code: 'AE', dial: '+971', name: 'United Arab Emirates' },
    { code: 'US', dial: '+1', name: 'United States' },
    { code: 'GB', dial: '+44', name: 'United Kingdom' },
    { code: 'AU', dial: '+61', name: 'Australia' },
    { code: 'JP', dial: '+81', name: 'Japan' },
    { code: 'DE', dial: '+49', name: 'Germany' },
    { code: 'FR', dial: '+33', name: 'France' },
    { code: 'CN', dial: '+86', name: 'China' },
    { code: 'SG', dial: '+65', name: 'Singapore' },
    { code: 'CA', dial: '+1', name: 'Canada' },
    { code: 'IT', dial: '+39', name: 'Italy' },
    { code: 'BR', dial: '+55', name: 'Brazil' },
    { code: 'ZA', dial: '+27', name: 'South Africa' },
    { code: 'RU', dial: '+7', name: 'Russia' },
    { code: 'MX', dial: '+52', name: 'Mexico' },
    { code: 'KR', dial: '+82', name: 'South Korea' },
    { code: 'ES', dial: '+34', name: 'Spain' },
    { code: 'ID', dial: '+62', name: 'Indonesia' },
    { code: 'TR', dial: '+90', name: 'Turkey' },
    { code: 'SA', dial: '+966', name: 'Saudi Arabia' },
    { code: 'BD', dial: '+880', name: 'Bangladesh' },
    { code: 'LK', dial: '+94', name: 'Sri Lanka' },
    { code: 'NP', dial: '+977', name: 'Nepal' }
];

function SummaryView({ onNavigate, isSubscribed }) {
    const [invIndex, setInvIndex] = useState(0);
    const investmentTypes = [
        { name: 'Gold (24K)', amount: '₹1,24,500', profit: '1,250', percent: '+2.4%', isLoss: false, data: [40, 45, 43, 60, 55, 65, 80] },
        { name: 'Mutual Funds', amount: '₹45,000', profit: '450', percent: '-1.2%', isLoss: true, data: [80, 75, 60, 65, 55, 45, 30] },
        { name: 'Stocks (AAPL)', amount: '₹89,000', profit: '4,900', percent: '+5.6%', isLoss: false, data: [20, 30, 25, 45, 40, 65, 90] },
    ];
    const currentInv = investmentTypes[invIndex];
    const [stats, setStats] = useState({
        balance: 0, income: 0, expenses: 0,
        weeklyChartHeights: [0, 0, 0, 0, 0, 0, 0],
        weeklyLabels: ['M', 'T', 'W', 'T', 'F', 'S', 'S'],
        weeklyExpensesRaw: [0, 0, 0, 0, 0, 0, 0],
        weeklyDates: ['', '', '', '', '', '', ''],
        savedPercentage: 0,
        breakdown: { bank: { total: 0, count: 0 }, fd: { total: 0, count: 0 }, rd: { total: 0, count: 0 }, cash: { total: 0, count: 0 } },
        allTransactions: []
    });
    const [selectedBar, setSelectedBar] = useState(null);
    const [loading, setLoading] = useState(true);
    const [isGullakModalOpen, setIsGullakModalOpen] = useState(false);
    const [selectedBreakdown, setSelectedBreakdown] = useState('bank');
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const [denominations, setDenominations] = useState({
        500: 0, 200: 0, 100: 0, 50: 0, 20: 0, 10: 0, 5: 0, 2: 0, 1: 0
    });
    const [weekOffset, setWeekOffset] = useState(0);
    const [showTopSpends, setShowTopSpends] = useState(false);

    const totalGullak = Object.entries(denominations).reduce((sum, [den, qty]) => sum + (Number(den) * (Number(qty) || 0)), 0);

    const handleSaveGullak = (e) => {
        e.preventDefault();
        const payload = {
            depositType: 'Gullak',
            holderName: 'Cash Hold',
            principalAmount: totalGullak,
            interestRate: 0
        };
        fetch(`${API_BASE_URL_JAVA}/api/deposits`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        }).then(res => {
            if (res.ok) {
                setIsGullakModalOpen(false);
                setDenominations({ 500: 0, 200: 0, 100: 0, 50: 0, 20: 0, 10: 0, 5: 0, 2: 0, 1: 0 });
                fetchStats();
                showToast('Gullak Cash Hold saved successfully!', 'success');
            }
        }).catch(err => console.error(err));
    };

    const handleDeleteGullak = () => {
        if(window.confirm("Are you sure you want to delete all Cash Holds? This will permanently reset your Gullak to ₹0.")) {
            fetch(`${API_BASE_URL_JAVA}/api/deposits`)
                .then(r => r.json())
                .then(deposits => {
                    if (Array.isArray(deposits)) {
                        const gullaks = deposits.filter(d => d.depositType === 'Gullak');
                        Promise.all(gullaks.map(g => fetch(`${API_BASE_URL_JAVA}/api/deposits/${g.id}`, { method: 'DELETE' })))
                            .then(() => {
                                setDenominations({ 500: 0, 200: 0, 100: 0, 50: 0, 20: 0, 10: 0, 5: 0, 2: 0, 1: 0 });
                                fetchStats();
                                showToast('Cash holds reset successfully!', 'success');
                            })
                            .catch(err => console.error(err));
                    }
                });
        }
    };

    const fetchStats = () => {
        Promise.all([
            fetch(`${API_BASE_URL_JAVA}/api/bankaccounts`).then(r => r.ok ? r.json() : []),
            fetch(`${API_BASE_URL_JAVA}/api/transactions`).then(r => r.ok ? r.json() : []),
            fetch(`${API_BASE_URL_JAVA}/api/deposits`).then(r => r.ok ? r.json() : [])
        ]).then(([accounts, transactions, deposits]) => {
            let bankTotal = 0, bankCount = 0;
            let fdTotal = 0, fdCount = 0;
            let rdTotal = 0, rdCount = 0;
            let cashTotal = 0, cashCount = 0;

            if (Array.isArray(accounts)) {
                accounts.forEach(acc => {
                    const amt = acc.balance || 0;
                    if (acc.accountType === 'Fixed Deposit') { fdTotal += amt; fdCount++; }
                    else if (acc.accountType === 'Recurring Deposit') { rdTotal += amt; rdCount++; }
                    else { bankTotal += amt; bankCount++; }
                });
            }

            if (Array.isArray(deposits)) {
                deposits.forEach(dep => {
                    const amt = dep.principalAmount || 0;
                    if (dep.depositType === 'FD') { fdTotal += amt; fdCount++; }
                    else if (dep.depositType === 'RD') { rdTotal += amt; rdCount++; }
                    else if (dep.depositType === 'Gullak') { cashTotal += amt; cashCount++; }
                });
            }

            const totalBalance = bankTotal + fdTotal + rdTotal + cashTotal;
            let totalIncome = 0;
            let totalExpenses = 0;

            let weeklyExpenses = [0, 0, 0, 0, 0, 0, 0];
            const today = new Date();
            today.setDate(today.getDate() - (weekOffset * 7));
            const last7Days = [];
            const dayNames = ['S', 'M', 'T', 'W', 'T', 'F', 'S'];
            const weeklyLabels = [];

            for (let i = 6; i >= 0; i--) {
                const d = new Date(today);
                d.setDate(today.getDate() - i);
                last7Days.push(d.toISOString().split('T')[0]);
                weeklyLabels.push(dayNames[d.getDay()]);
            }

            if (Array.isArray(transactions)) {
                transactions.forEach(t => {
                    const typeStr = String(t.transactionType || t.type || '').toUpperCase();
                    if (['INCOME', 'CREDIT', 'INTEREST'].includes(typeStr)) {
                        totalIncome += (parseFloat(t.amount) || 0);
                    }
                    else if (['EXPENSE', 'DEBIT'].includes(typeStr)) {
                        totalExpenses += (parseFloat(t.amount) || 0);
                        if (t.date) {
                            try {
                                let d = new Date(t.date);
                                if (typeof t.date === 'string' && t.date.includes('/')) {
                                    const parts = t.date.split('/');
                                    if (parts.length === 3) {
                                        let day = parseInt(parts[0], 10);
                                        let month = parseInt(parts[1], 10) - 1;
                                        let year = parseInt(parts[2], 10);
                                        if (year < 100) year += 2000;
                                        d = new Date(year, month, day);
                                    }
                                }
                                if (!isNaN(d.getTime())) {
                                    const localDate = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
                                    const dateIdx = last7Days.indexOf(localDate);
                                    if (dateIdx !== -1) {
                                        weeklyExpenses[dateIdx] += (parseFloat(t.amount) || 0);
                                    }
                                }
                            } catch (e) {}
                        }
                    }
                });
            }

            const maxExpense = Math.max(...weeklyExpenses, 1);
            const weeklyChartHeights = weeklyExpenses.map(val => (val / maxExpense) * 100);

            const goalAmount = 100000;
            const savedPercentage = Math.max(0, Math.min(100, Math.round((totalBalance / goalAmount) * 100)));

            setStats({ balance: totalBalance, income: totalIncome, expenses: totalExpenses, weeklyChartHeights, weeklyLabels, weeklyExpensesRaw: weeklyExpenses, weeklyDates: last7Days, savedPercentage, breakdown: { bank: { total: bankTotal, count: bankCount }, fd: { total: fdTotal, count: fdCount }, rd: { total: rdTotal, count: rdCount }, cash: { total: cashTotal, count: cashCount } }, allTransactions: transactions });
            setLoading(false);
        }).catch(err => {
            console.error("Failed to load summary stats", err);
            setLoading(false);
        });
    };

    useEffect(() => {
        fetchStats();
    }, [weekOffset]);

    let topSpends = [];
    if (showTopSpends && stats.allTransactions) {
        const targetDates = selectedBar !== null ? [stats.weeklyDates[selectedBar]] : stats.weeklyDates;
        const spends = stats.allTransactions.filter(t => {
            const typeStr = String(t.transactionType || t.type || '').toUpperCase();
            if (!['EXPENSE', 'DEBIT', 'WITHDRAWAL', 'WITHDRAW'].includes(typeStr)) return false;
            if (t.date) {
                try {
                    let d = new Date(t.date);
                    if (typeof t.date === 'string' && t.date.includes('/')) {
                        const parts = t.date.split('/');
                        if (parts.length === 3) {
                            let day = parseInt(parts[0], 10);
                            let month = parseInt(parts[1], 10) - 1;
                            let year = parseInt(parts[2], 10);
                            if (year < 100) year += 2000;
                            d = new Date(year, month, day);
                        }
                    }
                    if (!isNaN(d.getTime())) {
                        const localDate = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
                        return targetDates.includes(localDate);
                    }
                } catch(e) {}
            }
            return false;
        });
        topSpends = spends.sort((a, b) => (parseFloat(b.amount) || 0) - (parseFloat(a.amount) || 0)).slice(0, 4);
    }

    if (loading) return <div className="loading-spinner">Loading Financial Summary...</div>;

    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
            {/* Quick Income/Expenses Overview */}
            <div style={{ display: 'flex', gap: '16px', flexWrap: 'wrap' }}>
                <div style={{
                    background: 'var(--dash-glass-bg)', backdropFilter: 'blur(16px)', WebkitBackdropFilter: 'blur(16px)',
                    padding: '10px 20px', borderRadius: '100px', border: '1px solid rgba(52,211,153,0.3)',
                    display: 'flex', alignItems: 'center', gap: '12px', boxShadow: '0 4px 15px rgba(52,211,153,0.05)'
                }}>
                    <div style={{ width: 28, height: 28, borderRadius: '50%', background: 'rgba(52,211,153,0.15)', color: '#10b981', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '14px', fontWeight: '900' }}>↑</div>
                    <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px' }}>
                        <span style={{ color: 'var(--dash-text-muted)', fontSize: '13px', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Income</span>
                        <span style={{ color: 'var(--dash-text)', fontSize: '18px', fontWeight: '800' }}>₹{stats.income.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
                    </div>
                </div>
                
                <div style={{
                    background: 'var(--dash-glass-bg)', backdropFilter: 'blur(16px)', WebkitBackdropFilter: 'blur(16px)',
                    padding: '10px 20px', borderRadius: '100px', border: '1px solid rgba(239,68,68,0.3)',
                    display: 'flex', alignItems: 'center', gap: '12px', boxShadow: '0 4px 15px rgba(239,68,68,0.05)'
                }}>
                    <div style={{ width: 28, height: 28, borderRadius: '50%', background: 'rgba(239,68,68,0.15)', color: '#ef4444', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '14px', fontWeight: '900' }}>↓</div>
                    <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px' }}>
                        <span style={{ color: 'var(--dash-text-muted)', fontSize: '13px', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Expenses</span>
                        <span style={{ color: 'var(--dash-text)', fontSize: '18px', fontWeight: '800' }}>₹{stats.expenses.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
                    </div>
                </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '24px' }}>

                {/* Monthly Overview (Mock Chart) */}
                <div style={{
                    background: 'var(--dash-glass-bg)', backdropFilter: 'blur(16px)', WebkitBackdropFilter: 'blur(16px)',
                    padding: '28px', borderRadius: '28px',
                    border: '1px solid var(--dash-border)', boxShadow: '0 10px 40px rgba(0,0,0,0.04)',
                    display: 'flex', flexDirection: 'column', gap: '16px',
                    minHeight: '280px', position: 'relative', overflow: 'hidden'
                }}>
                    <div style={{ position: 'absolute', top: -100, right: -100, width: 250, height: 250, background: 'radial-gradient(circle, rgba(99, 102, 241, 0.15) 0%, transparent 70%)', filter: 'blur(40px)', zIndex: 0 }}></div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', position: 'relative', zIndex: 1 }}>
                        <div>
                            <h3 style={{ margin: 0, color: 'var(--dash-text)', fontSize: '18px', fontWeight: '700', letterSpacing: '-0.3px' }}>Weekly Overview</h3>
                            <p style={{ margin: '4px 0 0 0', color: 'var(--dash-text-muted)', fontSize: '13.5px', fontWeight: '500' }}>
                                {showTopSpends ? (selectedBar !== null ? `Top spends for ${stats.weeklyLabels[selectedBar]}` : (weekOffset === 0 ? 'Top spends this week' : `Top spends ${weekOffset} weeks ago`)) : (weekOffset === 0 ? 'Spending for this week' : weekOffset === 1 ? 'Spending for last week' : `Spending for ${weekOffset} weeks ago`)}
                            </p>
                        </div>
                        <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                            <button onClick={() => setShowTopSpends(!showTopSpends)} style={{ width: 32, height: 32, borderRadius: '50%', background: 'var(--dash-card)', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', transition: 'all 0.2s', border: '1px solid var(--dash-border)', color: showTopSpends ? '#34d399' : 'var(--dash-text-muted)', boxShadow: '0 2px 8px rgba(0,0,0,0.05)' }} onMouseOver={(e) => e.currentTarget.style.background = 'var(--dash-border)'} onMouseOut={(e) => e.currentTarget.style.background = 'var(--dash-card)'} title={showTopSpends ? "Show Chart" : "Top 5 Spends"}>
                                {showTopSpends ? <BarChart3 size={16} /> : <List size={16} />}
                            </button>
                            <button onClick={() => setWeekOffset(prev => prev + 1)} style={{ width: 32, height: 32, borderRadius: '50%', background: 'var(--dash-card)', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', transition: 'all 0.2s', border: '1px solid var(--dash-border)', color: 'var(--dash-text-muted)', boxShadow: '0 2px 8px rgba(0,0,0,0.05)' }} onMouseOver={(e) => e.currentTarget.style.background = 'var(--dash-border)'} onMouseOut={(e) => e.currentTarget.style.background = 'var(--dash-card)'}>
                                &larr;
                            </button>
                            <button onClick={() => setWeekOffset(prev => Math.max(0, prev - 1))} disabled={weekOffset === 0} style={{ width: 32, height: 32, borderRadius: '50%', background: 'var(--dash-card)', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: weekOffset === 0 ? 'not-allowed' : 'pointer', transition: 'all 0.2s', border: '1px solid var(--dash-border)', color: 'var(--dash-text-muted)', opacity: weekOffset === 0 ? 0.3 : 1, boxShadow: '0 2px 8px rgba(0,0,0,0.05)' }} onMouseOver={(e) => { if(weekOffset > 0) e.currentTarget.style.background = 'var(--dash-border)'}} onMouseOut={(e) => e.currentTarget.style.background = 'var(--dash-card)'}>
                                &rarr;
                            </button>
                        </div>
                    </div>

                    {!showTopSpends ? (
                        <>
                            <div style={{ flex: 1, display: 'flex', alignItems: 'flex-end', gap: '14px', marginTop: '30px', paddingBottom: '12px', position: 'relative', zIndex: 1 }}>
                                {stats.weeklyChartHeights.map((h, i) => (
                                    <div key={i} style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'flex-end', height: '100%', position: 'relative', group: 'true' }} className="bar-group">
                                        <div style={{ 
                                            width: '100%', height: `${Math.max(h, 4)}%`, 
                                            background: h === 100 && h > 0 ? 'linear-gradient(180deg, #34d399 0%, #10b981 100%)' : (selectedBar === i ? 'linear-gradient(180deg, #60a5fa 0%, rgba(96,165,250,0.2) 100%)' : 'linear-gradient(180deg, rgba(99,102,241,0.6) 0%, rgba(99,102,241,0.1) 100%)'), 
                                            borderRadius: '6px 6px 4px 4px', 
                                            boxShadow: h === 100 && h > 0 ? '0 0 20px rgba(52, 211, 153, 0.4)' : (selectedBar === i ? '0 0 15px rgba(96,165,250,0.3)' : 'none'),
                                            transition: 'all 0.3s ease', cursor: 'pointer'
                                        }} onClick={() => setSelectedBar(selectedBar === i ? null : i)} onMouseOver={(e) => { e.currentTarget.style.transform = 'scaleY(1.05)'; e.currentTarget.style.filter = 'brightness(1.2)'; }} onMouseOut={(e) => { e.currentTarget.style.transform = 'scaleY(1)'; e.currentTarget.style.filter = 'brightness(1)'; }}></div>

                                        {selectedBar === i && (
                                            <div style={{ 
                                                position: 'absolute', bottom: `calc(${Math.max(h, 4)}% + 12px)`, left: '50%', transform: 'translateX(-50%)', 
                                                background: 'var(--dash-glass-bg)', backdropFilter: 'blur(16px)', border: '1px solid var(--dash-border)', 
                                                padding: '10px 14px', borderRadius: '12px', zIndex: 50, whiteSpace: 'nowrap', 
                                                boxShadow: '0 8px 30px rgba(0,0,0,0.2)', pointerEvents: 'none', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '4px' 
                                            }}>
                                                <div style={{ fontSize: '11px', color: 'var(--dash-text-muted)', fontWeight: '600', letterSpacing: '0.5px' }}>
                                                    {stats.weeklyDates[i] ? new Date(stats.weeklyDates[i]).toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' }) : ''}
                                                </div>
                                                <div style={{ fontSize: '15px', color: 'var(--dash-text)', fontWeight: '800' }}>
                                                    ₹{(stats.weeklyExpensesRaw[i] || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                                                </div>
                                                <div style={{ position: 'absolute', bottom: '-6px', left: '50%', transform: 'translateX(-50%) rotate(45deg)', width: '10px', height: '10px', background: 'var(--dash-card)', borderRight: '1px solid var(--dash-border)', borderBottom: '1px solid var(--dash-border)', pointerEvents: 'none' }}></div>
                                            </div>
                                        )}
                                    </div>
                                ))}
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--dash-text-muted)', fontSize: '12px', marginTop: '4px', fontWeight: '600', position: 'relative', zIndex: 1 }}>
                                {stats.weeklyLabels.map((l, i) => <span key={i} style={{ flex: 1, textAlign: 'center', color: selectedBar === i ? '#60a5fa' : 'var(--dash-text-muted)' }}>{l}</span>)}
                            </div>
                        </>
                    ) : (
                        <div className="custom-scrollbar" style={{ flex: 1, marginTop: '20px', display: 'flex', flexDirection: 'column', gap: '10px', overflowY: 'auto', zIndex: 1, maxHeight: '180px', paddingRight: '4px' }}>
                            {topSpends.length > 0 ? topSpends.map((ts, idx) => (
                                <div key={ts.id || idx} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'linear-gradient(145deg, rgba(255,255,255,0.02) 0%, rgba(255,255,255,0.05) 100%)', padding: '14px 18px', borderRadius: '16px', border: '1px solid rgba(255,255,255,0.08)', animation: `vcaFadeIn 0.4s ease-out ${idx * 0.1}s forwards`, opacity: 0, boxShadow: '0 4px 15px rgba(0,0,0,0.1)', transition: 'transform 0.2s ease, background 0.2s ease', gap: '12px' }} onMouseOver={(e) => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.background = 'linear-gradient(145deg, rgba(255,255,255,0.04) 0%, rgba(255,255,255,0.08) 100%)'; }} onMouseOut={(e) => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.background = 'linear-gradient(145deg, rgba(255,255,255,0.02) 0%, rgba(255,255,255,0.05) 100%)'; }}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flex: 1, minWidth: 0 }}>
                                        <div style={{ width: '36px', height: '36px', borderRadius: '10px', background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: '800', fontSize: '15px', border: '1px solid rgba(239, 68, 68, 0.2)', flexShrink: 0 }}>
                                            {idx + 1}
                                        </div>
                                        <div style={{ minWidth: 0, flex: 1 }}>
                                            <div style={{ color: 'var(--dash-text)', fontSize: '14.5px', fontWeight: '700', letterSpacing: '-0.2px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{ts.description || ts.category || 'Unknown Spend'}</div>
                                            <div style={{ color: 'var(--dash-text-muted)', fontSize: '11px', marginTop: '3px', fontWeight: '500', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{ts.date ? (ts.date.includes('/') ? ts.date : new Date(ts.date).toLocaleDateString()) : ''} • {ts.category || 'General'}</div>
                                        </div>
                                    </div>
                                    <div style={{ color: '#ef4444', fontWeight: '800', fontSize: '15px', whiteSpace: 'nowrap', flexShrink: 0 }}>
                                        ₹{parseFloat(ts.amount || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                                    </div>
                                </div>
                            )) : (
                                <div style={{ flex: 1, minHeight: '100px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--dash-text-muted)', fontSize: '14px', fontWeight: '600' }}>
                                    No spending found for this {selectedBar !== null ? 'day' : 'week'}.
                                </div>
                            )}
                        </div>
                    )}
                </div>

                {/* Current Budget */}
                <div style={{
                    background: 'linear-gradient(145deg, rgba(52, 211, 153, 0.12) 0%, rgba(16, 185, 129, 0.04) 100%)', backdropFilter: 'blur(16px)', WebkitBackdropFilter: 'blur(16px)',
                    padding: '28px', borderRadius: '28px',
                    border: '1px solid rgba(52, 211, 153, 0.25)', boxShadow: '0 15px 35px rgba(52,211,153,0.12), inset 0 0 20px rgba(52,211,153,0.05)',
                    display: 'flex', flexDirection: 'column', position: 'relative', zIndex: 10
                }}>
                    <div style={{ position: 'absolute', inset: 0, borderRadius: '28px', overflow: 'hidden', zIndex: 0, pointerEvents: 'none' }}>
                        <div style={{ position: 'absolute', top: -50, right: -50, width: 200, height: 200, background: 'radial-gradient(circle, rgba(52, 211, 153, 0.2) 0%, transparent 70%)', filter: 'blur(30px)' }}></div>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '20px', position: 'relative', zIndex: 20 }}>
                        <div>
                            <p style={{ margin: 0, color: '#10b981', fontSize: '13.5px', marginBottom: '6px', fontWeight: '700', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Total Available</p>
                            <h3 style={{ margin: 0, color: 'var(--dash-text)', fontSize: '36px', fontWeight: '800', letterSpacing: '-1.5px', display: 'flex', alignItems: 'baseline', gap: '4px' }}>
                                <span style={{ fontSize: '24px', opacity: 0.7 }}>₹</span>
                                {stats.balance.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                            </h3>
                        </div>
                        <div style={{ position: 'relative' }}>
                            <div 
                                onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                                style={{ 
                                    background: 'var(--dash-card)', color: 'var(--dash-text)', 
                                    border: '1px solid var(--dash-border)', padding: '8px 16px', 
                                    borderRadius: '12px', fontSize: '13.5px', fontWeight: '600', cursor: 'pointer',
                                    backdropFilter: 'blur(10px)', transition: 'all 0.2s ease',
                                    boxShadow: '0 4px 15px rgba(0,0,0,0.05)', display: 'flex', alignItems: 'center', gap: '8px'
                                }}
                                onMouseOver={(e) => { e.currentTarget.style.background = 'var(--dash-border)'; }}
                                onMouseOut={(e) => { e.currentTarget.style.background = 'var(--dash-card)'; }}
                            >
                                {selectedBreakdown === 'bank' ? 'Bank Accounts' : selectedBreakdown === 'fd' ? 'Fixed Deposits' : selectedBreakdown === 'rd' ? 'Recurring Deposits' : 'Cash Holdings'}
                                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ transform: isDropdownOpen ? 'rotate(180deg)' : 'rotate(0deg)', transition: 'transform 0.2s' }}>
                                    <polyline points="6 9 12 15 18 9"></polyline>
                                </svg>
                            </div>
                            {isDropdownOpen && (
                                <>
                                    <div style={{ position: 'fixed', inset: 0, zIndex: 10 }} onClick={() => setIsDropdownOpen(false)}></div>
                                    <div style={{ 
                                        position: 'absolute', top: 'calc(100% + 2px)', right: 0, width: '180px',
                                        background: 'var(--dash-glass-bg)', backdropFilter: 'blur(20px)', WebkitBackdropFilter: 'blur(20px)',
                                        border: '1px solid var(--dash-border)', borderRadius: '12px', padding: '6px',
                                        boxShadow: '0 10px 40px rgba(0,0,0,0.1)', zIndex: 11,
                                        display: 'flex', flexDirection: 'column', gap: '4px'
                                    }}>
                                        {['bank', 'fd', 'rd', 'cash'].map(opt => (
                                            <div 
                                                key={opt}
                                                onClick={() => { setSelectedBreakdown(opt); setIsDropdownOpen(false); }}
                                                style={{ 
                                                    padding: '10px 12px', borderRadius: '8px', cursor: 'pointer', fontSize: '13.5px', fontWeight: '600',
                                                    color: selectedBreakdown === opt ? 'var(--dash-text)' : 'var(--dash-text-muted)',
                                                    background: selectedBreakdown === opt ? 'var(--dash-border)' : 'transparent',
                                                    transition: 'all 0.2s'
                                                }}
                                                onMouseOver={(e) => { if(selectedBreakdown !== opt) { e.currentTarget.style.background = 'rgba(128,128,128,0.1)'; e.currentTarget.style.color = 'var(--dash-text)'; } }}
                                                onMouseOut={(e) => { if(selectedBreakdown !== opt) { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.color = 'var(--dash-text-muted)'; } }}
                                            >
                                                {opt === 'bank' ? 'Bank Accounts' : opt === 'fd' ? 'Fixed Deposits' : opt === 'rd' ? 'Recurring Deposits' : 'Cash Holdings'}
                                            </div>
                                        ))}
                                    </div>
                                </>
                            )}
                        </div>
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', flex: 1, justifyContent: 'center', position: 'relative', zIndex: 1 }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <span style={{ color: 'var(--dash-text-muted)', fontSize: '13px', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Breakdown</span>
                        </div>
                        
                        <div style={{ background: 'var(--dash-card)', padding: '20px', borderRadius: '20px', border: '1px solid var(--dash-border)', boxShadow: '0 8px 24px rgba(0,0,0,0.06)', transition: 'all 0.3s ease' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px', alignItems: 'center' }}>
                                <span style={{ color: 'var(--dash-text-muted)', fontSize: '14px', fontWeight: '600' }}>
                                    {selectedBreakdown === 'bank' ? 'Bank' : selectedBreakdown === 'fd' ? 'FD' : selectedBreakdown === 'rd' ? 'RD' : 'Cash'} Balance
                                </span>
                                <span style={{ 
                                    background: selectedBreakdown === 'bank' ? 'rgba(99,102,241,0.1)' : selectedBreakdown === 'fd' ? 'rgba(245,158,11,0.1)' : selectedBreakdown === 'rd' ? 'rgba(236,72,153,0.1)' : 'rgba(52,211,153,0.1)', 
                                    color: selectedBreakdown === 'bank' ? '#6366f1' : selectedBreakdown === 'fd' ? '#f59e0b' : selectedBreakdown === 'rd' ? '#ec4899' : '#34d399', 
                                    padding: '6px 12px', borderRadius: '10px', fontSize: '11.5px', fontWeight: '700' 
                                }}>
                                    {stats.breakdown?.[selectedBreakdown]?.count || 0} a/c
                                </span>
                            </div>
                            <div style={{ color: 'var(--dash-text)', fontSize: '24px', fontWeight: '800', letterSpacing: '-0.5px' }}>
                                ₹{(stats.breakdown?.[selectedBreakdown]?.total || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                            </div>
                        </div>
                    </div>
                </div>

                {/* Compact Goal Progress & Investment UI */}
                <div style={{
                    background: 'var(--dash-glass-bg)', backdropFilter: 'blur(16px)', WebkitBackdropFilter: 'blur(16px)',
                    padding: '24px', borderRadius: '28px',
                    border: '1px solid var(--dash-border)', boxShadow: '0 10px 40px rgba(0,0,0,0.04)',
                    display: 'flex', flexDirection: 'column', position: 'relative', overflow: 'hidden', gap: '20px'
                }}>
                    <div style={{ position: 'absolute', bottom: -100, left: -100, width: 250, height: 250, background: 'radial-gradient(circle, rgba(14, 165, 233, 0.1) 0%, transparent 70%)', filter: 'blur(40px)', zIndex: 0 }}></div>
                    
                    {/* Compact Goal */}
                    <div style={{ position: 'relative', zIndex: 1, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                        <div>
                            <h3 style={{ margin: 0, color: 'var(--dash-text)', fontSize: '16px', fontWeight: '700', letterSpacing: '-0.3px' }}>Goal Progress</h3>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginTop: '4px' }}>
                                <span style={{ fontSize: '20px', fontWeight: '800', color: 'var(--dash-text)' }}>{stats.savedPercentage}%</span>
                                <span style={{ fontSize: '12px', color: '#0ea5e9', fontWeight: '600' }}>Save ₹1L</span>
                            </div>
                        </div>
                        <div style={{ width: '100px', height: '6px', background: 'var(--dash-border-strong)', borderRadius: '4px', overflow: 'hidden' }}>
                            <div style={{ width: `${stats.savedPercentage}%`, height: '100%', background: 'linear-gradient(90deg, #0ea5e9, #38bdf8)', borderRadius: '4px' }}></div>
                        </div>
                    </div>

                    <div style={{ height: '1px', background: 'var(--dash-border)', width: '100%', position: 'relative', zIndex: 1 }}></div>

                    {/* Investment UI */}
                    <div style={{ position: 'relative', zIndex: 1, flex: 1, display: 'flex', flexDirection: 'column' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                            <h3 style={{ margin: 0, color: 'var(--dash-text)', fontSize: '16px', fontWeight: '700', letterSpacing: '-0.3px' }}>Investments</h3>
                            {isSubscribed ? (
                                <div style={{ display: 'flex', gap: '8px' }}>
                                    <button onClick={() => setInvIndex(prev => prev === 0 ? investmentTypes.length - 1 : prev - 1)} style={{ background: 'var(--dash-card)', border: '1px solid var(--dash-border)', color: 'var(--dash-text)', width: '28px', height: '28px', borderRadius: '50%', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>&larr;</button>
                                    <button onClick={() => setInvIndex(prev => (prev + 1) % investmentTypes.length)} style={{ background: 'var(--dash-card)', border: '1px solid var(--dash-border)', color: 'var(--dash-text)', width: '28px', height: '28px', borderRadius: '50%', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>&rarr;</button>
                                </div>
                            ) : null}
                        </div>

                        {!isSubscribed ? (
                            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', textAlign: 'center', padding: '16px', background: 'rgba(52, 211, 153, 0.05)', borderRadius: '16px', border: '1px dashed rgba(52, 211, 153, 0.3)' }}>
                                <Lock size={24} color="#34d399" style={{ marginBottom: '8px' }} />
                                <span style={{ color: 'var(--dash-text)', fontSize: '14px', fontWeight: '600' }}>Premium Feature</span>
                                <span style={{ color: 'var(--dash-text-muted)', fontSize: '12px', marginTop: '4px' }}>Subscribe to unlock daily tracking.</span>
                            </div>
                        ) : (
                            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                                    <div>
                                        <div style={{ color: 'var(--dash-text-muted)', fontSize: '13px', fontWeight: '600' }}>{currentInv.name}</div>
                                        <div style={{ color: 'var(--dash-text)', fontSize: '20px', fontWeight: '800', marginTop: '2px' }}>{currentInv.amount}</div>
                                    </div>
                                    <div style={{ textAlign: 'right' }}>
                                        <div style={{ color: currentInv.isLoss ? '#ef4444' : '#34d399', fontSize: '14px', fontWeight: '700' }}>
                                            {currentInv.isLoss ? '-' : '+'}₹{currentInv.profit}
                                        </div>
                                        <div style={{ color: currentInv.isLoss ? '#ef4444' : '#34d399', fontSize: '12px', fontWeight: '600', background: currentInv.isLoss ? 'rgba(239, 68, 68, 0.1)' : 'rgba(52, 211, 153, 0.1)', padding: '2px 6px', borderRadius: '4px', display: 'inline-block', marginTop: '4px' }}>
                                            {currentInv.percent}
                                        </div>
                                    </div>
                                </div>
                                <div style={{ height: '60px', marginTop: '16px', display: 'flex', alignItems: 'flex-end', gap: '4px' }}>
                                    {currentInv.data.map((h, i) => (
                                        <div key={i} style={{ flex: 1, background: currentInv.isLoss ? 'linear-gradient(180deg, #ef4444 0%, rgba(239,68,68,0.1) 100%)' : 'linear-gradient(180deg, #34d399 0%, rgba(52,211,153,0.1) 100%)', height: `${h}%`, borderRadius: '4px 4px 0 0', transition: 'all 0.3s' }}></div>
                                    ))}
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Quick Stats Bottom Row */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '24px' }}>

                <div onClick={() => onNavigate && onNavigate('bankaccounts')} style={{
                    background: 'var(--dash-glass-bg)', backdropFilter: 'blur(16px)', WebkitBackdropFilter: 'blur(16px)',
                    padding: '24px', borderRadius: '24px',
                    border: '1px solid var(--dash-border)', cursor: 'pointer',
                    display: 'flex', alignItems: 'center', gap: '20px', transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)', boxShadow: '0 8px 32px rgba(0,0,0,0.04)'
                }} onMouseOver={(e) => { e.currentTarget.style.transform = 'translateY(-6px)'; e.currentTarget.style.boxShadow = '0 16px 40px rgba(99,102,241,0.15)'; e.currentTarget.style.borderColor = 'rgba(99,102,241,0.3)'; }} onMouseOut={(e) => { e.currentTarget.style.transform = 'none'; e.currentTarget.style.boxShadow = '0 8px 32px rgba(0,0,0,0.04)'; e.currentTarget.style.borderColor = 'var(--dash-border)'; }}>
                    <div style={{ width: 56, height: 56, borderRadius: '16px', background: 'rgba(99,102,241,0.1)', color: '#6366f1', display: 'flex', alignItems: 'center', justifyContent: 'center', transition: 'all 0.3s' }}>
                        <Landmark size={28} strokeWidth={2} />
                    </div>
                    <div>
                        <h4 style={{ margin: 0, color: 'var(--dash-text)', fontSize: '19px', fontWeight: '700', letterSpacing: '-0.3px' }}>Bank Accounts</h4>
                        <p style={{ margin: '6px 0 0 0', color: 'var(--dash-text-muted)', fontSize: '13.5px', fontWeight: '500' }}>Manage accounts</p>
                    </div>
                </div>

                <div onClick={() => onNavigate && onNavigate('cards')} style={{
                    background: 'var(--dash-glass-bg)', backdropFilter: 'blur(16px)', WebkitBackdropFilter: 'blur(16px)',
                    padding: '24px', borderRadius: '24px',
                    border: '1px solid var(--dash-border)', cursor: 'pointer',
                    display: 'flex', alignItems: 'center', gap: '20px', transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)', boxShadow: '0 8px 32px rgba(0,0,0,0.04)'
                }} onMouseOver={(e) => { e.currentTarget.style.transform = 'translateY(-6px)'; e.currentTarget.style.boxShadow = '0 16px 40px rgba(245,158,11,0.15)'; e.currentTarget.style.borderColor = 'rgba(245,158,11,0.3)'; }} onMouseOut={(e) => { e.currentTarget.style.transform = 'none'; e.currentTarget.style.boxShadow = '0 8px 32px rgba(0,0,0,0.04)'; e.currentTarget.style.borderColor = 'var(--dash-border)'; }}>
                    <div style={{ width: 56, height: 56, borderRadius: '16px', background: 'rgba(245,158,11,0.1)', color: '#f59e0b', display: 'flex', alignItems: 'center', justifyContent: 'center', transition: 'all 0.3s' }}>
                        <CreditCard size={28} strokeWidth={2} />
                    </div>
                    <div>
                        <h4 style={{ margin: 0, color: 'var(--dash-text)', fontSize: '19px', fontWeight: '700', letterSpacing: '-0.3px' }}>Cards</h4>
                        <p style={{ margin: '6px 0 0 0', color: 'var(--dash-text-muted)', fontSize: '13.5px', fontWeight: '500' }}>Manage cards</p>
                    </div>
                </div>
                
                <div onClick={() => setIsGullakModalOpen(true)} style={{
                    background: 'var(--dash-glass-bg)', backdropFilter: 'blur(16px)', WebkitBackdropFilter: 'blur(16px)',
                    padding: '24px', borderRadius: '24px',
                    border: '1px solid var(--dash-border)', cursor: 'pointer',
                    display: 'flex', alignItems: 'center', gap: '20px', transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)', boxShadow: '0 8px 32px rgba(0,0,0,0.04)'
                }} onMouseOver={(e) => { e.currentTarget.style.transform = 'translateY(-6px)'; e.currentTarget.style.boxShadow = '0 16px 40px rgba(52,211,153,0.15)'; e.currentTarget.style.borderColor = 'rgba(52,211,153,0.3)'; }} onMouseOut={(e) => { e.currentTarget.style.transform = 'none'; e.currentTarget.style.boxShadow = '0 8px 32px rgba(0,0,0,0.04)'; e.currentTarget.style.borderColor = 'var(--dash-border)'; }}>
                    <div style={{ width: 56, height: 56, borderRadius: '16px', background: 'rgba(52,211,153,0.1)', color: '#34d399', display: 'flex', alignItems: 'center', justifyContent: 'center', transition: 'all 0.3s' }}>
                        <PiggyBank size={28} strokeWidth={2} />
                    </div>
                    <div>
                        <h4 style={{ margin: 0, color: 'var(--dash-text)', fontSize: '19px', fontWeight: '700', letterSpacing: '-0.3px' }}>Gullak</h4>
                        <p style={{ margin: '6px 0 0 0', color: 'var(--dash-text-muted)', fontSize: '13.5px', fontWeight: '500' }}>Cash Hold</p>
                    </div>
                </div>
            </div>

            {/* Gullak Modal */}
            {isGullakModalOpen && (
                <div style={{
                    position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
                    background: 'rgba(0,0,0,0.5)', backdropFilter: 'blur(4px)',
                    display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000
                }}>
                    <div className="glass-card" style={{ width: '450px', padding: '24px', background: 'var(--dash-card)', border: '1px solid var(--dash-border)', borderRadius: '24px', maxHeight: '90vh', display: 'flex', flexDirection: 'column' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                            <h2 style={{ margin: 0, color: 'var(--dash-text)', flexShrink: 0 }}>Gullak - Cash Hold</h2>
                            {stats.breakdown?.cash.total > 0 && (
                                <button type="button" onClick={handleDeleteGullak} style={{ background: 'rgba(239,68,68,0.1)', color: '#ef4444', border: '1px solid rgba(239,68,68,0.2)', padding: '4px 10px', borderRadius: '6px', fontSize: '11.5px', fontWeight: 'bold', cursor: 'pointer', transition: 'all 0.2s' }} onMouseOver={(e) => e.currentTarget.style.background = 'rgba(239,68,68,0.15)'} onMouseOut={(e) => e.currentTarget.style.background = 'rgba(239,68,68,0.1)'}>
                                    Reset / Delete
                                </button>
                            )}
                        </div>
                        <p style={{ margin: '0 0 16px 0', color: 'var(--dash-text-muted)', fontSize: '13px' }}>Enter the quantity for each denomination to calculate your total cash hold.</p>

                        <div style={{ background: 'rgba(52, 211, 153, 0.1)', padding: '16px', borderRadius: '12px', marginBottom: '16px', textAlign: 'center', border: '1px solid rgba(52, 211, 153, 0.3)' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px', paddingBottom: '12px', borderBottom: '1px solid rgba(52, 211, 153, 0.2)' }}>
                                <div style={{ textAlign: 'left' }}>
                                    <div style={{ fontSize: '11px', color: 'var(--dash-text-muted)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Previously Saved</div>
                                    <div style={{ fontSize: '16px', fontWeight: '600', color: 'var(--dash-text)' }}>₹{(stats.breakdown?.cash.total || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</div>
                                </div>
                                <div style={{ textAlign: 'right' }}>
                                    <div style={{ fontSize: '11px', color: 'var(--dash-text-muted)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>New Addition</div>
                                    <div style={{ fontSize: '16px', fontWeight: '600', color: '#34d399' }}>+ ₹{totalGullak.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</div>
                                </div>
                            </div>
                            <div style={{ fontSize: '13px', color: '#34d399', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '1px' }}>Total Cash Hold (After Save)</div>
                            <div style={{ fontSize: '32px', fontWeight: 'bold', color: 'var(--dash-text)' }}>
                                ₹{((stats.breakdown?.cash.total || 0) + totalGullak).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                            </div>
                        </div>

                        <form onSubmit={handleSaveGullak} style={{ display: 'flex', flexDirection: 'column', flex: 1, overflow: 'hidden' }}>
                            <div style={{ flex: 1, overflowY: 'auto', paddingRight: '8px', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                                {[500, 200, 100, 50, 20, 10, 5, 2, 1].map(den => (
                                    <div key={den} style={{ display: 'flex', alignItems: 'center', gap: '8px', background: 'var(--dash-glass-bg)', padding: '8px 12px', borderRadius: '8px', border: '1px solid var(--dash-border)' }}>
                                        <div style={{ width: '40px', fontWeight: '600', color: 'var(--dash-text-muted)', fontSize: '14px' }}>₹{den}</div>
                                        <div style={{ color: 'var(--dash-text-muted)' }}>x</div>
                                        <input
                                            type="number"
                                            min="0"
                                            value={denominations[den] || ''}
                                            onChange={(e) => setDenominations(prev => ({ ...prev, [den]: parseInt(e.target.value) || 0 }))}
                                            style={{ flex: 1, padding: '8px', borderRadius: '6px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', width: '100%', outline: 'none' }}
                                            placeholder="0"
                                        />
                                    </div>
                                ))}
                            </div>
                            <div style={{ display: 'flex', gap: '12px', marginTop: '24px', flexShrink: 0, paddingBottom: '4px' }}>
                                <button type="button" className="btn-secondary" style={{ flex: 1, padding: '10px', borderRadius: '8px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', cursor: 'pointer' }} onClick={() => setIsGullakModalOpen(false)}>Cancel</button>
                                <button type="submit" className="btn-primary" style={{ flex: 1, padding: '10px', borderRadius: '8px', background: 'linear-gradient(135deg, #6A5AE0, #8A7CF0)', color: '#ffffff', border: 'none', cursor: 'pointer', fontWeight: 'bold' }}>Save as Asset</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}

function ModuleView({ module, refreshTrigger, onEdit, userUid, onBack, onAdd, isSubscribed, onSubscribe }) {
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [lendingTab, setLendingTab] = useState('Lent'); // 'Lent' or 'Borrowed'
    const [openDropdownId, setOpenDropdownId] = useState(null);

    // AI Analysis States for Transactions Page
    const [isAnalysing, setIsAnalysing] = useState(false);
    const [dateFrom, setDateFrom] = useState('');
    const [dateTo, setDateTo] = useState('');
    const [selectedGroup, setSelectedGroup] = useState('ALL');
    const [aiInsight, setAiInsight] = useState('');
    const [insightLoading, setInsightLoading] = useState(false);

    // Pagination and general filtering for Transactions
    const [searchTerm, setSearchTerm] = useState('');
    const [filterDate, setFilterDate] = useState('');
    const [filterYear, setFilterYear] = useState('');
    const [currentPage, setCurrentPage] = useState(1);
    const recordsPerPage = 10;

    // Smart Category Grouping Engine
    const CATEGORY_GROUPS = [
        { id: 'ALL', label: 'All Groups (View Everything)', keywords: [] },
        { id: 'FOOD', label: 'Group: Food & Dining', keywords: ['food', 'dining', 'groceries', 'restaurant', 'cafe', 'zomato', 'swiggy', 'supermarket', 'mart', 'coffee'] },
        { id: 'BILLS', label: 'Group: Bills & Utilities', keywords: ['recharge', 'electricity', 'water', 'gas', 'wifi', 'broadband', 'utility', 'bills', 'mobile', 'bill'] },
        { id: 'EDUCATION', label: 'Group: Education & Learning', keywords: ['education', 'tuition', 'books', 'course', 'school', 'college', 'university', 'training'] },
        { id: 'SHOPPING', label: 'Group: Shopping & Retail', keywords: ['shopping', 'ecommerce', 'clothes', 'electronics', 'amazon', 'flipkart', 'myntra', 'store', 'retail'] },
        { id: 'TRAVEL', label: 'Group: Travel & Transport', keywords: ['travel', 'transport', 'fuel', 'petrol', 'diesel', 'uber', 'ola', 'flight', 'train', 'metro', 'cab', 'taxi'] },
        { id: 'HEALTH', label: 'Group: Health & Medical', keywords: ['health', 'medical', 'hospital', 'pharmacy', 'insurance', 'mediclaim', 'doctor', 'clinic'] },
        { id: 'INCOME', label: 'Group: Income & Salary', keywords: ['salary', 'income', 'bonus', 'refund', 'interest', 'dividend', 'stipend', 'credit'] },
        { id: 'INVESTMENT', label: 'Group: Investments & Savings', keywords: ['investment', 'mutual fund', 'sip', 'ppf', 'elss', 'fd', 'rd', 'stocks', 'gold', 'nps'] }
    ];

    const getTransactionGroup = (category) => {
        if (!category) return 'GENERAL';
        const c = String(category).toLowerCase();
        for (const g of CATEGORY_GROUPS) {
            if (g.id === 'ALL') continue;
            if (g.keywords.some(kw => c.includes(kw))) return g.id;
        }
        return 'GENERAL';
    };

    // Calculate dynamic groups present in the data plus standard groups
    const availableGroups = React.useMemo(() => {
        const presentGroupIds = new Set(data.map(d => getTransactionGroup(d.category)));
        const list = CATEGORY_GROUPS.filter(g => g.id === 'ALL' || presentGroupIds.has(g.id));
        if (presentGroupIds.has('GENERAL')) {
            list.push({ id: 'GENERAL', label: 'Group: General / Others', keywords: [] });
        }
        return list;
    }, [data]);

    const fetchData = () => {
        setLoading(true);
        if (module.id === 'deposits') {
            Promise.all([
                fetch(`${API_BASE_URL_JAVA}/api/deposits`).then(res => res.ok ? res.json() : []),
                fetch(`${API_BASE_URL_JAVA}/api/bankaccounts`).then(res => res.ok ? res.json() : [])
            ]).then(([depositsData, bankAccountsData]) => {
                const mappedBankAccounts = bankAccountsData
                    .filter(acc => acc.accountType === 'Fixed Deposit' || acc.accountType === 'Recurring Deposit' || acc.accountType === 'Gullak' || acc.accountType === 'Cash')
                    .map(acc => ({
                        id: acc.id,
                        depositType: acc.accountType === 'Fixed Deposit' ? 'FD' : (acc.accountType === 'Recurring Deposit' ? 'RD' : acc.accountType),
                        holderName: acc.accountName || acc.bankName,
                        accountNumber: acc.accountNumber,
                        ifscCode: acc.ifscCode,
                        bankName: acc.bankName,
                        branchName: acc.branchName,
                        interestType: acc.creditPeriod,
                        dateOfMaturity: acc.dateOfMature,
                        startDate: acc.dateOfAccountOpening,
                        principalAmount: acc.balance || 0,
                        interestRate: acc.interestRate || 0,
                        _endpoint: '/api/bankaccounts',
                        _originalItem: acc
                    }));
                const mappedDeps = depositsData.map(d => ({ ...d, _endpoint: '/api/deposits', _originalItem: d }));
                setData([...mappedDeps, ...mappedBankAccounts]);
                setLoading(false);
            }).catch(err => {
                console.error("Error fetching combined deposits:", err);
                setData([]);
                setLoading(false);
            });
        } else {
            fetch(`${API_BASE_URL_JAVA}${module.endpoint}`)
                .then(res => {
                    if (!res.ok) {
                        throw new Error(`HTTP error! status: ${res.status}`);
                    }
                    return res.json();
                })
                .then(resData => {
                    if (Array.isArray(resData)) {
                        let finalData = resData;
                        if (module.id === 'investments') {
                            finalData = resData.map(item => ({
                                ...item,
                                assetSymbol: item.assetSymbol || item.tickerSymbol,
                                investmentName: item.investmentName || item.description,
                                amountInvested: item.amountInvested !== undefined ? item.amountInvested : item.initialUnitCost
                            }));
                        } else if (module.id === 'lendings') {
                            finalData = resData.map(item => ({
                                ...item,
                                counterpartyUid: item.lenderUid === userUid ? item.borrowerUid : item.lenderUid,
                                counterpartyName: item.lenderUid === userUid ? item.borrowerName : item.lenderName
                            }));
                        }
                        setData(finalData);
                    } else {
                        console.error("Expected an array but got:", resData);
                        setData([]);
                    }
                    setLoading(false);
                })
                .catch(err => {
                    console.error(`Error fetching ${module.label}:`, err);
                    setData([]);
                    setLoading(false);
                });
        }
    };

    useEffect(() => {
        fetchData();
    }, [module, refreshTrigger]);

    const displayData = React.useMemo(() => {
        let processedData = data;
        
        if (module.id === 'lendings') {
            processedData = data.filter(d => lendingTab === 'Lent' ? (d.lenderUid === userUid || !d.lenderUid) : d.borrowerUid === userUid);
        } else if (module.id === 'transactions') {
            if (isAnalysing) {
                processedData = data.filter(d => {
                    const matchFrom = !dateFrom || (d.date && d.date >= dateFrom);
                    const matchTo = !dateTo || (d.date && d.date <= dateTo);
                    const matchGroup = selectedGroup === 'ALL' || getTransactionGroup(d.category) === selectedGroup;
                    return matchFrom && matchTo && matchGroup;
                });
            } else {
                processedData = data.filter(d => {
                    const matchSearch = !searchTerm || Object.values(d).some(val => 
                        val && String(val).toLowerCase().includes(searchTerm.toLowerCase())
                    );
                    const matchDate = !filterDate || (d.date && d.date === filterDate);
                    const matchYear = !filterYear || (d.date && String(d.date).startsWith(filterYear));
                    return matchSearch && matchDate && matchYear;
                });
            }
        }
        
        return processedData;
    }, [module.id, lendingTab, data, isAnalysing, dateFrom, dateTo, selectedGroup, userUid, searchTerm, filterDate, filterYear]);

    const totalPages = Math.ceil(displayData.length / recordsPerPage);

    const finalTableData = React.useMemo(() => {
        if (module.id === 'transactions') {
            const startIndex = (currentPage - 1) * recordsPerPage;
            return displayData.slice(startIndex, startIndex + recordsPerPage);
        }
        return displayData;
    }, [displayData, currentPage, module.id]);

    const totalExpense = React.useMemo(() => {
        return (module.id === 'transactions' && isAnalysing)
            ? displayData
                  .filter(d => (d.transactionType && (String(d.transactionType).toUpperCase() === 'EXPENSE' || String(d.transactionType).toUpperCase() === 'DEBIT')) || (d.type && String(d.type).toUpperCase() === 'DEBIT'))
                  .reduce((acc, curr) => acc + (parseFloat(curr.amount) || 0), 0)
            : 0;
    }, [module.id, isAnalysing, displayData]);

    const totalInflow = React.useMemo(() => {
        return (module.id === 'transactions' && isAnalysing)
            ? displayData
                  .filter(d => (d.transactionType && (String(d.transactionType).toUpperCase() === 'INCOME' || String(d.transactionType).toUpperCase() === 'CREDIT')) || (d.type && String(d.type).toUpperCase() === 'CREDIT'))
                  .reduce((acc, curr) => acc + (parseFloat(curr.amount) || 0), 0)
            : 0;
    }, [module.id, isAnalysing, displayData]);

    const netCashFlow = totalInflow - totalExpense;

    useEffect(() => {
        if (module.id !== 'transactions' || !isAnalysing) return;
        if (displayData.length === 0) {
            setAiInsight("No transactions found in this group and date range. Adjust your filter to analyze your cash flow.");
            return;
        }
        setInsightLoading(true);
        const timeoutId = setTimeout(() => {
            const topCatMap = {};
            displayData.forEach(t => {
                const cat = t.category || 'General';
                const amt = parseFloat(t.amount) || 0;
                topCatMap[cat] = (topCatMap[cat] || 0) + amt;
            });
            const topCategoryEntry = Object.entries(topCatMap).sort((a,b) => b[1] - a[1])[0];
            const topCat = topCategoryEntry ? `${topCategoryEntry[0]} (₹${topCategoryEntry[1].toFixed(2)})` : 'None';
            const groupLabel = (availableGroups.find(g => g.id === selectedGroup) || {}).label || 'All Groups';

            setAiInsight(`In the selected period across [${groupLabel}], you have ${displayData.length} transaction(s) analyzed.\n\n• Total Spent: ₹${totalExpense.toFixed(2)}\n• Total Inflow: ₹${totalInflow.toFixed(2)}\n• Top Expense Category in Group: ${topCat}\n\n💡 AI Virtual CA Advisory: Based on your ${groupLabel} cash flow, monitor regular outflows and consider setting aside 20% of net inflows into tax-saving ELSS or emergency liquid funds.`);
            setInsightLoading(false);
        }, 300);
        return () => clearTimeout(timeoutId);
    }, [isAnalysing, dateFrom, dateTo, selectedGroup, displayData, availableGroups, module.id, totalExpense, totalInflow]);

    if (loading) {
        return <div className="loading-spinner">Loading {module.label} from Java Backend...</div>;
    }

    const handleDelete = (row) => {
        if (window.confirm("Are you sure you want to delete this record?")) {
            const endpoint = row._endpoint || module.endpoint;
            fetch(`${API_BASE_URL_JAVA}${endpoint}/${row.id}`, { method: 'DELETE' })
                .then(res => {
                    if (res.ok) {
                        setData(prev => prev.filter(item => item.id !== row.id));
                    }
                })
                .catch(err => console.error("Failed to delete", err));
        }
    };

    // Extract columns from module fields if available, otherwise from the first record
    const columns = module.fields
        ? module.fields.map(f => f.name)
        : ((Array.isArray(data) && data.length > 0)
            ? Object.keys(data[0] || {}).filter(k => k !== 'id')
            : []);

    const displayColumns = module.id === 'lendings'
        ? ['counterpartyUid', 'counterpartyName', 'principalAmount', 'interestRate', 'interestType', 'tenure', 'dateLent', 'dateOfClosing', 'amountToRepay']
        : module.id === 'bankaccounts'
        ? ['accountName', 'accountNumber', 'bankName', 'accountType', 'interestRate', 'creditPeriod', 'balance']
        : module.id === 'deposits'
        ? ['holderName', 'bankName', 'depositType', 'principalAmount', 'interestRate', 'dateOfMaturity', 'interestAmount', 'maturityValue']
        : columns;

    const getColumnLabel = (col) => {
        if (col === 'counterpartyUid') return lendingTab === 'Lent' ? 'Borrower UID' : 'Lender UID';
        if (col === 'counterpartyName') return lendingTab === 'Lent' ? 'Borrower Name' : 'Lender Name';
        if (module.fields) {
            const field = module.fields.find(f => f.name === col);
            if (field && field.label) return field.label;
        }
        return col.replace(/([A-Z])/g, ' $1').trim();
    };

    return (
        <div style={{
            background: 'var(--dash-glass-bg)', backdropFilter: 'blur(16px)', WebkitBackdropFilter: 'blur(16px)',
            padding: '32px', borderRadius: '28px',
            border: '1px solid var(--dash-border)', overflow: 'visible', position: 'relative',
            boxShadow: '0 10px 40px rgba(0,0,0,0.04)'
        }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px', flexWrap: 'wrap', gap: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                    <button
                        onClick={onBack}
                        style={{
                            background: 'var(--dash-glass-bg)', border: '1px solid var(--dash-border)',
                            color: 'var(--dash-text)', width: '40px', height: '40px', borderRadius: '50%',
                            display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer',
                            transition: 'all 0.3s ease', boxShadow: '0 4px 15px rgba(0,0,0,0.05)'
                        }}
                        onMouseOver={(e) => { e.currentTarget.style.background = 'rgba(52, 211, 153, 0.15)'; e.currentTarget.style.color = '#34d399'; e.currentTarget.style.borderColor = 'rgba(52, 211, 153, 0.4)'; e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 8px 20px rgba(52, 211, 153, 0.2)'; }}
                        onMouseOut={(e) => { e.currentTarget.style.background = 'var(--dash-glass-bg)'; e.currentTarget.style.color = 'var(--dash-text)'; e.currentTarget.style.borderColor = 'var(--dash-border)'; e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 4px 15px rgba(0,0,0,0.05)'; }}
                        title="Back to Overview"
                    >
                        <ArrowLeft size={20} />
                    </button>
                    <h3 style={{ margin: 0, color: 'var(--dash-text)', fontSize: '22px', fontWeight: '800', letterSpacing: '-0.5px' }}>{module.label} Records</h3>
                </div>

                {module.id === 'lendings' && (
                    <div style={{ display: 'flex', background: 'var(--dash-bg)', padding: '6px', borderRadius: '14px', border: '1px solid var(--dash-border)', gap: '4px' }}>
                        <button
                            onClick={() => setLendingTab('Lent')}
                            style={{
                                padding: '8px 24px', borderRadius: '10px', border: 'none', cursor: 'pointer', fontWeight: '700', fontSize: '13px',
                                background: lendingTab === 'Lent' ? 'var(--dash-card)' : 'transparent',
                                color: lendingTab === 'Lent' ? '#10b981' : 'var(--dash-text-muted)',
                                boxShadow: lendingTab === 'Lent' ? '0 2px 10px rgba(0,0,0,0.05)' : 'none',
                                transition: 'all 0.3s ease', whiteSpace: 'nowrap'
                            }}
                        >
                            Lent (You gave)
                        </button>
                        <button
                            onClick={() => setLendingTab('Borrowed')}
                            style={{
                                padding: '8px 24px', borderRadius: '10px', border: 'none', cursor: 'pointer', fontWeight: '700', fontSize: '13px',
                                background: lendingTab === 'Borrowed' ? 'var(--dash-card)' : 'transparent',
                                color: lendingTab === 'Borrowed' ? '#f59e0b' : 'var(--dash-text-muted)',
                                boxShadow: lendingTab === 'Borrowed' ? '0 2px 10px rgba(0,0,0,0.05)' : 'none',
                                transition: 'all 0.3s ease', whiteSpace: 'nowrap'
                            }}
                        >
                            Borrowed (You received)
                        </button>
                    </div>
                )}

                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    {module.id !== 'deposits' && module.id !== 'aica_import' && (
                        <button onClick={onAdd} style={{
                            background: 'var(--dash-text)', color: 'var(--dash-bg)', border: 'none',
                            padding: '10px 20px', borderRadius: '12px', fontWeight: '700',
                            cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '8px',
                            transition: 'all 0.3s ease', fontSize: '14px', letterSpacing: '0.2px',
                            boxShadow: '0 6px 20px rgba(0, 0, 0, 0.15)'
                        }} onMouseOver={(e) => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 8px 25px rgba(0, 0, 0, 0.2)'; }} onMouseOut={(e) => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 6px 20px rgba(0, 0, 0, 0.15)'; }}>
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
                            Add {module.label}
                        </button>
                    )}
                    {module.id === 'transactions' && (
                        <button
                            onClick={() => {
                                setIsAnalysing(prev => !prev);
                                setCurrentPage(1);
                                if (!isAnalysing) {
                                    setDateFrom('');
                                    setDateTo('');
                                    setSelectedGroup('ALL');
                                }
                            }}
                            style={{
                                padding: '8px 18px',
                                borderRadius: '12px',
                                border: 'none',
                                cursor: 'pointer',
                                fontWeight: '700',
                                fontSize: '14px',
                                background: isAnalysing
                                    ? 'linear-gradient(135deg, #10b981, #059669)'
                                    : 'rgba(52, 211, 153, 0.15)',
                                color: isAnalysing ? '#fff' : '#34d399',
                                display: 'flex',
                                alignItems: 'center',
                                gap: '8px',
                                transition: 'all 0.2s',
                                boxShadow: isAnalysing ? '0 4px 15px rgba(52, 211, 153, 0.3)' : 'none'
                            }}
                        >
                            <span>✨</span>
                            <span>{isAnalysing ? 'Close AI Analysis' : 'Analyse Transactions'}</span>
                        </button>
                    )}
                    <span style={{ padding: '6px 12px', background: 'rgba(52, 211, 153, 0.1)', color: '#34d399', borderRadius: '20px', fontSize: '12px', fontWeight: 'bold' }}>{displayData.length} Total</span>
                </div>
            </div>

            {/* AI TRANSACTIONS ANALYSIS VIEW (Only shown when Analyse option is selected) */}
            {module.id === 'transactions' && isAnalysing && (
                !isSubscribed ? (
                    <div style={{ marginBottom: '32px', display: 'flex', flexDirection: 'column', gap: '24px' }}>
                        <div style={{ background: 'var(--dash-glass-bg)', backdropFilter: 'blur(16px)', WebkitBackdropFilter: 'blur(16px)', border: '1px solid rgba(52, 211, 153, 0.3)', borderRadius: '24px', padding: '48px', textAlign: 'center', boxShadow: '0 20px 40px rgba(0,0,0,0.2)' }}>
                            <div style={{ width: '64px', height: '64px', borderRadius: '50%', background: 'rgba(52, 211, 153, 0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 20px', boxShadow: '0 0 20px rgba(52, 211, 153, 0.2)' }}>
                                <Lock size={32} color="#34d399" />
                            </div>
                            <h3 style={{ fontSize: '22px', fontWeight: '800', color: 'var(--dash-text)', marginBottom: '12px' }}>AI Analysis Locked</h3>
                            <p style={{ color: 'var(--dash-text-muted)', fontSize: '14px', lineHeight: '1.6', maxWidth: '400px', margin: '0 auto 24px' }}>
                                Smart AI Category Clustering and Deep Insight Analysis are premium features. Subscribe to unlock AI-powered transaction analysis.
                            </p>
                            <button 
                                onClick={onSubscribe} 
                                style={{ 
                                    padding: '12px 24px', 
                                    borderRadius: '10px', 
                                    background: 'linear-gradient(135deg, #34d399, #10b981)', 
                                    color: '#022c22', 
                                    border: 'none', 
                                    fontWeight: '800', 
                                    fontSize: '14px', 
                                    cursor: 'pointer', 
                                    boxShadow: '0 8px 20px rgba(52, 211, 153, 0.4)', 
                                    transition: 'all 0.3s ease',
                                    display: 'inline-flex',
                                    alignItems: 'center',
                                    gap: '8px'
                                }}
                                onMouseOver={(e) => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 12px 25px rgba(52, 211, 153, 0.5)'; }}
                                onMouseOut={(e) => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 8px 20px rgba(52, 211, 153, 0.4)'; }}
                            >
                                <Lock size={16} /> Subscribe Now
                            </button>
                        </div>
                    </div>
                ) : (
                <div style={{ marginBottom: '32px', display: 'flex', flexDirection: 'column', gap: '24px' }}>
                    {/* Filter Container */}
                    <div style={{
                        background: 'var(--dash-bg)', padding: '24px', borderRadius: '20px',
                        border: '1px solid var(--dash-border)', display: 'flex', flexDirection: 'column', gap: '20px'
                    }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
                            <h4 style={{ margin: 0, color: '#34d399', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '16px' }}>
                                <span>📊</span> AI Transactions Analysis — Date Range &amp; Smart Group Wise Filter
                            </h4>
                            <div style={{ fontSize: '12px', color: 'var(--dash-text-muted)' }}>
                                Grouping: <strong style={{ color: '#34d399' }}>Smart AI Category Clustering</strong>
                            </div>
                        </div>

                        {/* Date From, Date To, and Group Dropdown */}
                        <div style={{ display: 'flex', gap: '16px', flexWrap: 'wrap', alignItems: 'center' }}>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', minWidth: '170px' }}>
                                <label style={{ fontSize: '11px', color: 'var(--dash-text-muted)', fontWeight: '600' }}>DATE RATE "FROM"</label>
                                <input
                                    type="date"
                                    value={dateFrom}
                                    onChange={(e) => setDateFrom(e.target.value)}
                                    style={{
                                        padding: '10px 14px', borderRadius: '10px',
                                        background: 'var(--dash-card)', color: 'var(--dash-text)',
                                        border: '1px solid var(--dash-border)', outline: 'none'
                                    }}
                                />
                            </div>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', minWidth: '170px' }}>
                                <label style={{ fontSize: '11px', color: 'var(--dash-text-muted)', fontWeight: '600' }}>DATE RATE "TO"</label>
                                <input
                                    type="date"
                                    value={dateTo}
                                    onChange={(e) => setDateTo(e.target.value)}
                                    style={{
                                        padding: '10px 14px', borderRadius: '10px',
                                        background: 'var(--dash-card)', color: 'var(--dash-text)',
                                        border: '1px solid var(--dash-border)', outline: 'none'
                                    }}
                                />
                            </div>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', flex: 1, minWidth: '240px' }}>
                                <label style={{ fontSize: '11px', color: 'var(--dash-text-muted)', fontWeight: '600' }}>GROUP WISE CATEGORIES</label>
                                <select
                                    value={selectedGroup}
                                    onChange={(e) => setSelectedGroup(e.target.value)}
                                    style={{
                                        padding: '10px 14px', borderRadius: '10px',
                                        background: 'var(--dash-card)', color: 'var(--dash-text)',
                                        border: '1px solid var(--dash-border)', outline: 'none',
                                        fontWeight: '600', cursor: 'pointer'
                                    }}
                                >
                                    {availableGroups.map(grp => (
                                        <option key={grp.id} value={grp.id}>{grp.label}</option>
                                    ))}
                                </select>
                            </div>
                            {(dateFrom || dateTo || selectedGroup !== 'ALL') && (
                                <button
                                    onClick={() => { setDateFrom(''); setDateTo(''); setSelectedGroup('ALL'); }}
                                    style={{
                                        alignSelf: 'flex-end', padding: '10px 16px', borderRadius: '10px',
                                        background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444', border: '1px solid rgba(239, 68, 68, 0.2)',
                                        cursor: 'pointer', fontWeight: '600', fontSize: '13px'
                                    }}
                                >
                                    Reset Filters
                                </button>
                            )}
                        </div>

                        {/* Smart Group Filter Chips */}
                        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                            {availableGroups.map(grp => (
                                <button
                                    key={grp.id}
                                    onClick={() => setSelectedGroup(grp.id)}
                                    style={{
                                        padding: '6px 14px', borderRadius: '20px', fontSize: '12px', fontWeight: '600',
                                        background: selectedGroup === grp.id ? 'linear-gradient(135deg, #10b981, #059669)' : 'var(--dash-card)',
                                        color: selectedGroup === grp.id ? '#fff' : 'var(--dash-text-muted)',
                                        border: selectedGroup === grp.id ? 'none' : '1px solid var(--dash-border)',
                                        cursor: 'pointer', transition: 'all 0.2s'
                                    }}
                                >
                                    {grp.label}
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* Analytics Summary Cards */}
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px' }}>
                        <div style={{ background: 'var(--dash-bg)', padding: '20px', borderRadius: '16px', border: '1px solid var(--dash-border)' }}>
                            <div style={{ fontSize: '12px', color: 'var(--dash-text-muted)', fontWeight: '600', marginBottom: '8px' }}>Total Expense (Debit)</div>
                            <div style={{ fontSize: '24px', fontWeight: '700', color: '#ef4444' }}>₹{totalExpense.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</div>
                            <div style={{ fontSize: '11px', color: 'var(--dash-text-muted)', marginTop: '4px' }}>In selected group &amp; date range</div>
                        </div>
                        <div style={{ background: 'var(--dash-bg)', padding: '20px', borderRadius: '16px', border: '1px solid var(--dash-border)' }}>
                            <div style={{ fontSize: '12px', color: 'var(--dash-text-muted)', fontWeight: '600', marginBottom: '8px' }}>Total Inflow (Credit)</div>
                            <div style={{ fontSize: '24px', fontWeight: '700', color: '#10b981' }}>₹{totalInflow.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</div>
                            <div style={{ fontSize: '11px', color: 'var(--dash-text-muted)', marginTop: '4px' }}>In selected group &amp; date range</div>
                        </div>
                        <div style={{ background: 'var(--dash-bg)', padding: '20px', borderRadius: '16px', border: '1px solid var(--dash-border)' }}>
                            <div style={{ fontSize: '12px', color: 'var(--dash-text-muted)', fontWeight: '600', marginBottom: '8px' }}>Net Cash Flow</div>
                            <div style={{ fontSize: '24px', fontWeight: '700', color: netCashFlow >= 0 ? '#38bdf8' : '#ef4444' }}>₹{netCashFlow.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</div>
                            <div style={{ fontSize: '11px', color: 'var(--dash-text-muted)', marginTop: '4px' }}>Net savings in this group</div>
                        </div>
                        <div style={{ background: 'var(--dash-bg)', padding: '20px', borderRadius: '16px', border: '1px solid var(--dash-border)' }}>
                            <div style={{ fontSize: '12px', color: 'var(--dash-text-muted)', fontWeight: '600', marginBottom: '8px' }}>Transactions Analyzed</div>
                            <div style={{ fontSize: '24px', fontWeight: '700', color: '#a855f7' }}>{displayData.length}</div>
                            <div style={{ fontSize: '11px', color: 'var(--dash-text-muted)', marginTop: '4px' }}>Group: {availableGroups.find(g => g.id === selectedGroup)?.label || 'All Groups'}</div>
                        </div>
                    </div>

                    {/* AI Smart Insight Box */}
                    <div style={{
                        background: 'linear-gradient(135deg, rgba(52, 211, 153, 0.08), rgba(16, 185, 129, 0.03))',
                        padding: '24px', borderRadius: '20px', border: '1px solid rgba(52, 211, 153, 0.25)',
                        position: 'relative'
                    }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '12px' }}>
                            <span style={{ fontSize: '24px' }}>🤖</span>
                            <div style={{ fontWeight: '700', color: '#34d399', fontSize: '16px' }}>SmartLedger AI Virtual CA — Group Wise Analysis &amp; Insight</div>
                        </div>
                        <div style={{
                            color: 'var(--dash-text)', fontSize: '14px', lineHeight: '1.7', whiteSpace: 'pre-line',
                            background: 'var(--dash-card)', padding: '16px', borderRadius: '12px', border: '1px solid var(--dash-border)'
                        }}>
                            {insightLoading ? '🤖 Analyzing your group wise transactions...' : aiInsight}
                        </div>
                    </div>
                </div>
                )
            )}

            {/* General Filters for Transactions */}
            {module.id === 'transactions' && !isAnalysing && (
                <div style={{ display: 'flex', gap: '12px', marginBottom: '24px', flexWrap: 'wrap', alignItems: 'center', background: 'var(--dash-bg)', padding: '16px', borderRadius: '16px', border: '1px solid var(--dash-border)' }}>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', flex: 1, minWidth: '200px' }}>
                        <label style={{ fontSize: '11px', color: 'var(--dash-text-muted)', fontWeight: '600' }}>SEARCH RECORDS</label>
                        <input
                            type="text"
                            placeholder="Search descriptions, categories, amounts..."
                            value={searchTerm}
                            onChange={(e) => { setSearchTerm(e.target.value); setCurrentPage(1); }}
                            style={{ padding: '10px 14px', borderRadius: '10px', background: 'var(--dash-card)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                        />
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', minWidth: '150px' }}>
                        <label style={{ fontSize: '11px', color: 'var(--dash-text-muted)', fontWeight: '600' }}>FILTER BY DATE</label>
                        <input
                            type="date"
                            value={filterDate}
                            onChange={(e) => { setFilterDate(e.target.value); setFilterYear(''); setCurrentPage(1); }}
                            style={{ padding: '10px 14px', borderRadius: '10px', background: 'var(--dash-card)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                        />
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', minWidth: '150px' }}>
                        <label style={{ fontSize: '11px', color: 'var(--dash-text-muted)', fontWeight: '600' }}>FILTER BY YEAR</label>
                        <select
                            value={filterYear}
                            onChange={(e) => { setFilterYear(e.target.value); setFilterDate(''); setCurrentPage(1); }}
                            style={{ padding: '10px 14px', borderRadius: '10px', background: 'var(--dash-card)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none', cursor: 'pointer' }}
                        >
                            <option value="">All Years</option>
                            {Array.from(new Set(data.map(d => d.date ? String(d.date).substring(0,4) : null).filter(Boolean))).sort().reverse().map(year => (
                                <option key={year} value={year}>{year}</option>
                            ))}
                        </select>
                    </div>
                    {(searchTerm || filterDate || filterYear) && (
                        <button
                            onClick={() => { setSearchTerm(''); setFilterDate(''); setFilterYear(''); setCurrentPage(1); }}
                            style={{ alignSelf: 'flex-end', padding: '10px 16px', borderRadius: '10px', background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444', border: '1px solid rgba(239, 68, 68, 0.2)', cursor: 'pointer', fontWeight: '600', fontSize: '13px' }}
                        >
                            Clear Filters
                        </button>
                    )}
                </div>
            )}

            {!Array.isArray(finalTableData) || finalTableData.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '80px 40px', color: 'var(--dash-text-muted)', border: '2px dashed rgba(52, 211, 153, 0.2)', borderRadius: '20px', background: 'rgba(52, 211, 153, 0.02)', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', marginTop: '16px' }}>
                    <div style={{ width: '80px', height: '80px', borderRadius: '50%', background: 'rgba(52, 211, 153, 0.1)', color: '#34d399', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '32px', marginBottom: '20px', boxShadow: '0 0 20px rgba(52, 211, 153, 0.1)' }}>📄</div>
                    <div style={{ fontSize: '18px', fontWeight: '700', color: 'var(--dash-text)', marginBottom: '8px', letterSpacing: '-0.3px' }}>No {module.label} records found</div>
                    <div style={{ fontSize: '14.5px', color: 'var(--dash-text-muted)' }}>Click on <strong style={{color: '#34d399'}}>+ Add {module.label}</strong> to create a new record and see it listed here.</div>
                </div>
            ) : (
                <div style={{ overflow: 'visible', isolation: 'isolate', maxHeight: 'none', minHeight: module.id === 'transactions' ? '780px' : 'auto', borderRadius: '16px', background: 'var(--dash-card)', border: '1px solid var(--dash-border)', padding: '0', display: 'flex', flexDirection: 'column', justifyContent: 'space-between', position: 'relative' }}>
                <table style={{ width: '100%', borderCollapse: 'separate', borderSpacing: 0, borderRadius: '16px 16px 0 0', overflow: 'visible' }}>
                    <thead style={{ borderRadius: '16px 16px 0 0', overflow: 'visible' }}>
                        <tr style={{ background: 'var(--dash-card)', borderRadius: '16px 16px 0 0' }}>
                            {displayColumns.map((col, idx) => (
                                <th key={col} style={{ position: 'relative', background: 'var(--dash-card)', textAlign: 'left', padding: '16px 20px', color: 'var(--dash-text-muted)', fontSize: '11px', fontWeight: '700', textTransform: 'uppercase', letterSpacing: '0.8px', borderBottom: '2px solid var(--dash-border)', boxShadow: '0 2px 8px rgba(0, 0, 0, 0.06)', borderTopLeftRadius: idx === 0 ? '16px' : '0', borderTopRightRadius: idx === displayColumns.length - 1 && (module.id === 'lendings' && lendingTab === 'Borrowed') ? '16px' : '0' }}>
                                    {getColumnLabel(col)}
                                </th>
                            ))}
                            {!(module.id === 'lendings' && lendingTab === 'Borrowed') && (
                                <th style={{ position: 'relative', background: 'var(--dash-card)', textAlign: 'right', padding: '16px 20px', color: 'var(--dash-text-muted)', fontSize: '11px', fontWeight: '700', textTransform: 'uppercase', letterSpacing: '0.8px', borderBottom: '2px solid var(--dash-border)', boxShadow: '0 2px 8px rgba(0, 0, 0, 0.06)', borderTopRightRadius: '16px' }}>Actions</th>
                            )}
                        </tr>
                    </thead>
                    <tbody>
                        {finalTableData.map((row, index) => (
                            <tr key={row.id || index} style={{ transition: 'background 0.2s', background: 'transparent' }} onMouseOver={(e) => { e.currentTarget.style.background = 'rgba(52, 211, 153, 0.05)'; }} onMouseOut={(e) => { e.currentTarget.style.background = 'transparent'; }}>
                                {displayColumns.map((col, i) => (
                                    <td key={col} style={{ padding: '16px 20px', fontSize: '13px', color: 'var(--dash-text)', fontWeight: '500', borderBottom: '1px solid var(--dash-border)' }}>
                                        {col === 'interestAmount' ? (
                                            <LiveTracker
                                                principalAmount={row.principalAmount || row.balance}
                                                interestRate={row.interestRate}
                                                dateOfAccountOpening={row.startDate || row.dateOfAccountOpening || row.createdAt || new Date().toISOString()}
                                                interestType={row.interestType || row.creditPeriod || 'Annual'}
                                                dateOfMaturity={row.dateOfMaturity || row.dateOfMature}
                                                isRD={row.accountType === 'Recurring Deposit' || row.depositType === 'RD'}
                                            />
                                        ) : col === 'maturityValue' ? (
                                            <MaturityTracker
                                                principalAmount={row.principalAmount || row.balance}
                                                interestRate={row.interestRate}
                                                dateOfAccountOpening={row.startDate || row.dateOfAccountOpening || row.createdAt || new Date().toISOString()}
                                                interestType={row.interestType || row.creditPeriod || 'Annual'}
                                                dateOfMaturity={row.dateOfMaturity || row.dateOfMature}
                                                isRD={row.accountType === 'Recurring Deposit' || row.depositType === 'RD'}
                                            />
                                        ) : col === 'currentValue' && module.id === 'investments' ? (
                                            <LiveAssetTracker
                                                assetType={row.assetType}
                                                assetSymbol={row.assetSymbol}
                                                quantity={row.quantity}
                                                amountInvested={row.amountInvested}
                                            />
                                        ) : col === 'dateOfMaturity' ? (
                                            row.dateOfMaturity || row.dateOfMature ? new Date(row.dateOfMaturity || row.dateOfMature).toLocaleDateString() : '-'
                                        ) : col === 'amountToRepay' && (module.id === 'lendings' || module.id === 'loans') ? (
                                            <span style={{ color: '#ef4444', fontWeight: 'bold' }}>
                                                {(() => {
                                                    if (row.totalPayment) {
                                                        return `₹${row.totalPayment.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
                                                    }
                                                    const p = row.principalAmount || 0;
                                                    const r = (row.interestRate || 0) / 100;
                                                    const type = row.interestType || 'Annual';
                                                    let t = row.tenure || 0;

                                                    let amount = p;
                                                    if (r > 0 && t > 0) {
                                                        let rate = r;
                                                        if (type.toLowerCase() === 'annual' || type.toLowerCase() === 'annually') {
                                                            rate = rate / 12.0;
                                                        }
                                                        const emi = (p * rate * Math.pow(1 + rate, t)) / (Math.pow(1 + rate, t) - 1);
                                                        amount = emi * t;
                                                    }
                                                    return `₹${amount.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
                                                })()}
                                            </span>
                                        ) : col === 'cvv' ? (
                                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                                <span>***</span>
                                                <button onClick={() => showToast('OTP sent to your registered email to view CVV.', 'success')} style={{ background: 'rgba(52, 211, 153, 0.1)', color: '#34d399', border: 'none', padding: '4px 8px', borderRadius: '4px', fontSize: '11px', cursor: 'pointer', fontWeight: 'bold' }}>Verify to View</button>
                                            </div>
                                        ) : col === 'tenure' || col === 'tenureMonths' ? (
                                            `${row[col]} ${row.interestType ? row.interestType + '(s)' : 'Month(s)'}`
                                        ) : typeof row[col] === 'number'
                                            ? col.toLowerCase().includes('rate') || col.toLowerCase().includes('percentage')
                                                ? `${row[col]}%`
                                                : col.toLowerCase().includes('quantity')
                                                    ? row[col]
                                                    : `₹${row[col].toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
                                            : (row[col]?.toString() || '-')}
                                    </td>
                                ))}
                                {!(module.id === 'lendings' && lendingTab === 'Borrowed') && (
                                    <td style={{ padding: '16px 20px', textAlign: 'right', borderBottom: '1px solid var(--dash-border)' }}>
                                        <div style={{ position: 'relative', display: 'flex', justifyContent: 'flex-end' }}>
                                            <button
                                                onClick={(e) => { e.stopPropagation(); setOpenDropdownId(openDropdownId === (row.id || index) ? null : (row.id || index)); }}
                                                style={{ 
                                                    width: '36px', height: '36px', borderRadius: '50%', 
                                                    background: 'rgba(128, 128, 128, 0.15)', 
                                                    border: '1px solid rgba(128, 128, 128, 0.25)', 
                                                    color: 'var(--dash-text)', cursor: 'pointer', 
                                                    display: 'flex', alignItems: 'center', justifyContent: 'center', 
                                                    transition: 'all 0.2s', backdropFilter: 'blur(4px)',
                                                    boxShadow: '0 4px 12px rgba(0,0,0,0.05)'
                                                }}
                                                onMouseOver={(e) => { e.currentTarget.style.background = 'rgba(128, 128, 128, 0.25)'; e.currentTarget.style.transform = 'scale(1.08)'; e.currentTarget.style.borderColor = 'rgba(128, 128, 128, 0.4)'; }}
                                                onMouseOut={(e) => { e.currentTarget.style.background = 'rgba(128, 128, 128, 0.15)'; e.currentTarget.style.transform = 'scale(1)'; e.currentTarget.style.borderColor = 'rgba(128, 128, 128, 0.25)'; }}
                                                title="Actions"
                                            >
                                                <MoreHorizontal size={18} strokeWidth={2.5} />
                                            </button>
                                            
                                            {openDropdownId === (row.id || index) && (
                                                <div style={{
                                                    position: 'absolute', 
                                                    top: index >= finalTableData.length - 2 && index > 0 ? 'auto' : '100%', 
                                                    bottom: index >= finalTableData.length - 2 && index > 0 ? '100%' : 'auto', 
                                                    right: 0, 
                                                    marginTop: index >= finalTableData.length - 2 && index > 0 ? '0' : '8px',
                                                    marginBottom: index >= finalTableData.length - 2 && index > 0 ? '8px' : '0',
                                                    background: 'var(--dash-glass-bg, rgba(30, 41, 59, 0.95))', 
                                                    backdropFilter: 'blur(20px)', WebkitBackdropFilter: 'blur(20px)',
                                                    border: '1px solid rgba(255, 255, 255, 0.1)',
                                                    borderRadius: '16px', padding: '8px', zIndex: 100,
                                                    boxShadow: '0 15px 35px rgba(0,0,0,0.2), 0 0 0 1px rgba(255,255,255,0.05)', 
                                                    display: 'flex', flexDirection: 'column', gap: '4px', minWidth: '150px'
                                                }}>
                                                    <button
                                                        onClick={(e) => { e.stopPropagation(); setOpenDropdownId(null); onEdit && onEdit(row); }}
                                                        style={{ display: 'flex', alignItems: 'center', gap: '12px', background: 'transparent', border: 'none', color: 'var(--dash-text)', cursor: 'pointer', fontSize: '13.5px', padding: '10px 14px', borderRadius: '10px', fontWeight: '600', transition: 'all 0.2s', width: '100%', textAlign: 'left' }}
                                                        onMouseOver={(e) => { e.currentTarget.style.background = 'rgba(52, 211, 153, 0.15)'; e.currentTarget.style.color = '#34d399'; e.currentTarget.style.transform = 'translateY(-1px)'; }}
                                                        onMouseOut={(e) => { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.color = 'var(--dash-text)'; e.currentTarget.style.transform = 'translateY(0)'; }}
                                                    >
                                                        <Edit2 size={15} /> Edit
                                                    </button>
                                                    <button
                                                        onClick={(e) => { e.stopPropagation(); setOpenDropdownId(null); handleDelete(row); }}
                                                        style={{ display: 'flex', alignItems: 'center', gap: '12px', background: 'transparent', border: 'none', color: '#ef4444', cursor: 'pointer', fontSize: '13.5px', padding: '10px 14px', borderRadius: '10px', fontWeight: '600', transition: 'all 0.2s', width: '100%', textAlign: 'left' }}
                                                        onMouseOver={(e) => { e.currentTarget.style.background = 'rgba(239, 68, 68, 0.15)'; e.currentTarget.style.transform = 'translateY(-1px)'; }}
                                                        onMouseOut={(e) => { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.transform = 'translateY(0)'; }}
                                                    >
                                                        <Trash2 size={15} /> Delete
                                                    </button>
                                                </div>
                                            )}
                                        </div>
                                    </td>
                                )}
                            </tr>
                        ))}
                    </tbody>
                </table>
                {module.id === 'transactions' && totalPages > 1 && (
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px 20px', background: 'var(--dash-card)', borderTop: '1px solid var(--dash-border)', borderBottomLeftRadius: '16px', borderBottomRightRadius: '16px', position: 'relative', marginTop: 'auto', width: '100%', zIndex: 5 }}>
                        <div style={{ fontSize: '13px', color: 'var(--dash-text-muted)', fontWeight: '500' }}>
                            Showing {((currentPage - 1) * recordsPerPage) + 1} to {Math.min(currentPage * recordsPerPage, displayData.length)} of {displayData.length} records
                        </div>
                        <div style={{ display: 'flex', gap: '8px' }}>
                            <button
                                disabled={currentPage === 1}
                                onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
                                style={{ padding: '6px 14px', borderRadius: '8px', background: currentPage === 1 ? 'transparent' : 'var(--dash-bg)', color: currentPage === 1 ? 'var(--dash-text-muted)' : 'var(--dash-text)', border: '1px solid var(--dash-border)', cursor: currentPage === 1 ? 'not-allowed' : 'pointer', fontWeight: '600', transition: 'all 0.2s' }}
                            >
                                Previous
                            </button>
                            <span style={{ display: 'flex', alignItems: 'center', padding: '0 10px', fontSize: '13px', fontWeight: '600', color: 'var(--dash-text)' }}>
                                Page {currentPage} of {totalPages}
                            </span>
                            <button
                                disabled={currentPage === totalPages}
                                onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
                                style={{ padding: '6px 14px', borderRadius: '8px', background: currentPage === totalPages ? 'transparent' : 'var(--dash-bg)', color: currentPage === totalPages ? 'var(--dash-text-muted)' : 'var(--dash-text)', border: '1px solid var(--dash-border)', cursor: currentPage === totalPages ? 'not-allowed' : 'pointer', fontWeight: '600', transition: 'all 0.2s' }}
                            >
                                Next
                            </button>
                        </div>
                    </div>
                )}
                </div>
            )}
        </div>
    );
}

export default function Dashboard({ onLogout }) {
    const [activeModule, setActiveModule] = useState(MODULES[0]);
    const [isAddModalOpen, setIsAddModalOpen] = useState(false);
    const [formData, setFormData] = useState({});
    const [refreshTrigger, setRefreshTrigger] = useState(0);
    const [categories, setCategories] = useState(['Food & Dining', 'Transportation', 'Shopping', 'Entertainment', 'Housing', 'Utilities', 'Health & Fitness', 'Travel', 'Education', 'Personal Care']);
    const [accountData, setAccountData] = useState([]);
    const [cardData, setCardData] = useState([]);
    const [theme, setTheme] = useState('dark'); // default to dark theme
    const [isMaintenanceMode, setIsMaintenanceMode] = useState(false);

    useEffect(() => {
        document.documentElement.setAttribute('data-theme', theme);
        document.body.setAttribute('data-theme', theme);
    }, [theme]);

    const [userName, setUserName] = useState('User');
    const [userUid, setUserUid] = useState('000000');
    const [currentTime, setCurrentTime] = useState(new Date());
    const [showProfileDropdown, setShowProfileDropdown] = useState(false);
    const [showNotificationDropdown, setShowNotificationDropdown] = useState(false);
    const [activeSettingsModal, setActiveSettingsModal] = useState(null);
    const [accountAction, setAccountAction] = useState(null);
    const [deactivateDays, setDeactivateDays] = useState('7');
    const [factoryResetReason, setFactoryResetReason] = useState('');
    const [contactUsMessage, setContactUsMessage] = useState('');
    const [pendingGrievance, setPendingGrievance] = useState(null);
    const [toast, setToast] = useState({ message: '', type: '', visible: false });

    const showToast = (message, type = 'success') => {
        setToast({ message, type, visible: true });
        setTimeout(() => setToast(prev => ({ ...prev, visible: false })), 4000);
    };
    const [profilePicture, setProfilePicture] = useState(null);
    const [isUploading, setIsUploading] = useState(false);
    const [userSubscription, setUserSubscription] = useState(null);
    const [subLoading, setSubLoading] = useState(false);
    
    const [userProfileData, setUserProfileData] = useState({
        displayName: '',
        phoneCode: '+91',
        phoneNumber: '',
        dob: '',
        gender: ''
    });
    const [isSavingProfile, setIsSavingProfile] = useState(false);

    // Checkout Modal States
    const [checkoutPlan, setCheckoutPlan] = useState(null);
    const [couponCode, setCouponCode] = useState('');
    const [appliedCoupon, setAppliedCoupon] = useState(null);
    const [couponError, setCouponError] = useState('');
    const [couponSuccess, setCouponSuccess] = useState('');

    // Cropper States
    const [imageSrc, setImageSrc] = useState(null);
    const [crop, setCrop] = useState({ x: 0, y: 0 });
    const [zoom, setZoom] = useState(1);
    const [croppedAreaPixels, setCroppedAreaPixels] = useState(null);
    const [isCropping, setIsCropping] = useState(false);

    const fetchUserSubscription = async (uid) => {
        setSubLoading(true);
        try {
            const response = await fetch(`${API_BASE_URL_PYTHON}/api/admin/firestore-read`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ collection_name: 'business_subscriptions' })
            });
            if (response.ok) {
                const result = await response.json();
                const allSubs = result.data || [];
                const userSubs = allSubs.filter(d => d.userId === uid);
                if (userSubs.length > 0) {
                    const activeSub = userSubs.find(d => d.status === 'Active' || d.status === 'active') || userSubs[0];
                    setUserSubscription(activeSub);
                } else {
                    setUserSubscription(null);
                }
            }
        } catch (e) {
            console.error("Failed to fetch user subscription", e);
        } finally {
            setSubLoading(false);
        }
    };

    const [availableSubscriptions, setAvailableSubscriptions] = useState([]);
    
    const fetchAvailableSubscriptions = async () => {
        try {
            const response = await fetch(`${API_BASE_URL_PYTHON}/api/admin/firestore-read`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ collection_name: 'subscriptions' })
            });
            if (response.ok) {
                const result = await response.json();
                const docs = result.data || [];
                setAvailableSubscriptions(docs.filter(d => d.status?.toLowerCase() === 'active' || !d.status));
            }
        } catch (e) {
            console.error("Failed to fetch available subscriptions", e);
        }
    };

    const fetchPendingGrievance = async (uid) => {
        try {
            const q = query(collection(db, 'grievances'), where('userId', '==', uid));
            const querySnapshot = await getDocs(q);
            const pendingDoc = querySnapshot.docs.find(doc => doc.data().status === 'Pending');
            if (pendingDoc) {
                setPendingGrievance(pendingDoc.data());
            } else {
                setPendingGrievance(null);
            }
        } catch (e) {
            console.error("Failed to fetch pending grievances", e);
        }
    };

    const applyCoupon = async () => {
        setCouponError('');
        setCouponSuccess('');
        if (!couponCode.trim()) {
            setCouponError('Please enter a coupon code.');
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL_PYTHON}/api/admin/firestore-read`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ collection_name: 'coupons' })
            });
            if (response.ok) {
                const result = await response.json();
                const coupons = result.data || [];
                const validCoupon = coupons.find(c => c.code === couponCode.trim().toUpperCase());

                if (!validCoupon) {
                    setCouponError('Invalid coupon code.');
                    return;
                }

                if (new Date(validCoupon.expiryDate) < new Date()) {
                    setCouponError('This coupon has expired.');
                    return;
                }

                if (validCoupon.targetType === 'Individual' && validCoupon.targetUser && validCoupon.targetUser !== (auth.currentUser?.email || '')) {
                    setCouponError('This coupon is not valid for your account.');
                    return;
                }

                setAppliedCoupon(validCoupon);
                setCouponSuccess('Coupon applied successfully!');
            }
        } catch (e) {
            console.error(e);
            setCouponError('Failed to validate coupon.');
        }
    };

    const calculateDiscountedAmount = () => {
        if (!checkoutPlan) return 0;
        let amount = parseFloat(checkoutPlan.amount);
        if (appliedCoupon) {
            if (appliedCoupon.discountType === 'Percentage') {
                const discount = (amount * parseFloat(appliedCoupon.discountValue)) / 100;
                amount = amount - discount;
            } else {
                amount = amount - parseFloat(appliedCoupon.discountValue);
            }
            if (amount < 0) amount = 0;
        }
        return amount.toFixed(2);
    };

    const loadRazorpay = () => {
        return new Promise((resolve) => {
            if (window.Razorpay) {
                resolve(true);
                return;
            }
            const script = document.createElement('script');
            script.src = 'https://checkout.razorpay.com/v1/checkout.js';
            script.onload = () => resolve(true);
            script.onerror = () => resolve(false);
            document.body.appendChild(script);
        });
    };

    const handleSubscribe = async (plan) => {
        const res = await loadRazorpay();
        if (!res) {
            showToast('Razorpay SDK failed to load. Are you online?', 'error');
            return;
        }

        try {
            // Convert amount to paise (smallest currency unit) for Razorpay
            const amountInPaise = Math.round(parseFloat(plan.amount) * 100);

            const orderRes = await fetch(`${API_BASE_URL_PYTHON}/api/billing/create-order`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    amount: amountInPaise,
                    planName: plan.title
                })
            });

            if (!orderRes.ok) {
                const errData = await orderRes.json().catch(() => ({}));
                console.error("Razorpay Order Error:", errData);
                showToast(`Checkout failed: ${errData.detail || 'Internal Server Error'}. Please try again.`, 'error');
                return;
            }

            const order = await orderRes.json();

            if (!order || !order.id) {
                showToast('Invalid order received from server.', 'error');
                return;
            }

            const options = {
                key: 'rzp_test_TQoEVoA414qbUS',
                amount: order.amount,
                currency: order.currency || 'INR',
                name: 'SmartLedger',
                description: `Subscription to ${plan.title}`,
                order_id: order.id,
                handler: async function (response) {
                    try {
                        const verifyRes = await fetch(`${API_BASE_URL_PYTHON}/api/billing/verify-payment`, {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({
                                razorpay_order_id: response.razorpay_order_id,
                                razorpay_payment_id: response.razorpay_payment_id,
                                razorpay_signature: response.razorpay_signature
                            })
                        });
                        
                        if (verifyRes.ok) {
                            showToast('Payment successful! Subscription Activated.', 'success');
                            
                            const subId = `sub_${Date.now()}_${Math.random().toString(36).substring(2, 8)}`;
                            const newSub = {
                                id: subId,
                                userId: auth.currentUser?.uid || userUid,
                                planName: plan.title,
                                planId: plan.id,
                                amount: plan.amount,
                                currency: plan.currency || 'INR',
                                duration: plan.duration,
                                status: 'Active',
                                startDate: new Date().toISOString(),
                                paymentId: response.razorpay_payment_id,
                                orderId: response.razorpay_order_id
                            };
                            
                            await fetch(`${API_BASE_URL_PYTHON}/api/admin/firestore-write`, {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({
                                    collection_name: 'business_subscriptions',
                                    document_id: subId,
                                    data: newSub
                                })
                            });
                            fetchUserSubscription(auth.currentUser?.uid);
                        } else {
                            showToast('Payment verification failed.', 'error');
                        }
                    } catch (e) {
                        console.error(e);
                        showToast('Error verifying payment.', 'error');
                    }
                },
                prefill: {
                    name: userName,
                    email: auth.currentUser?.email || '',
                },
                theme: {
                    color: '#34d399'
                }
            };
            const rzp = new window.Razorpay(options);
            rzp.on('payment.failed', function (response){
                showToast('Payment failed: ' + response.error.description, 'error');
            });
            rzp.open();
        } catch (error) {
            console.error('Subscription error', error);
            showToast('Failed to initiate subscription process.', 'error');
        }
    };

    const generateInvoice = (subscriptionData) => {
        const amount = parseFloat(subscriptionData.amount || 0);
        
        const doc = new jsPDF();
        const logoUrl = '/assets/logo.png';
        
        const generatePdfContent = (img = null) => {
            if (img) {
                doc.addImage(img, 'PNG', 14, 15, 20, 20);
            }
            
            doc.setFontSize(22);
            doc.setFont("helvetica", "bold");
            doc.setTextColor(33, 33, 33);
            doc.text("INVOICE", 150, 25);
            
            doc.setFontSize(10);
            doc.setFont("helvetica", "normal");
            doc.setTextColor(100, 100, 100);
            doc.text("Smart Ledger V2", 14, 45);
            doc.text("Kaliganj Durgapur, Pin 713212", 14, 50);

            const invoiceNo = `INV-${new Date().getTime().toString().slice(-6)}`;
            const date = new Date().toLocaleDateString('en-IN');
            doc.text(`Invoice Number: ${invoiceNo}`, 140, 45);
            doc.text(`Date of Issue: ${date}`, 140, 50);
            
            doc.setFontSize(12);
            doc.setFont("helvetica", "bold");
            doc.setTextColor(33, 33, 33);
            doc.text("Billed To:", 14, 75);
            
            doc.setFontSize(10);
            doc.setFont("helvetica", "normal");
            doc.text(userProfileData.displayName || userName || "Customer", 14, 82);
            doc.text(auth.currentUser?.email || "customer@example.com", 14, 87);
            doc.text(`UID: ${userUid}`, 14, 92);

            const totalAmount = amount;
            const baseAmount = (totalAmount / 1.18).toFixed(2);
            const gstAmount = (totalAmount - parseFloat(baseAmount)).toFixed(2);

            const tableColumn = ["Description", `Amount (${subscriptionData.currency || 'INR'})`];
            const tableRows = [
                [`SmartLedger ${subscriptionData.planName} Subscription - ${subscriptionData.duration}`, `${baseAmount}`],
                ["IGST @ 18%", `${gstAmount}`]
            ];

            autoTable(doc, {
                startY: 105,
                head: [tableColumn],
                body: tableRows,
                theme: 'grid',
                headStyles: { fillColor: [16, 185, 129], textColor: [255, 255, 255], fontStyle: 'bold' },
                styles: { fontSize: 10, cellPadding: 5 },
                columnStyles: { 1: { halign: 'right' } }
            });

            const finalY = doc.lastAutoTable.finalY || 130;
            doc.setFontSize(12);
            doc.setFont("helvetica", "bold");
            doc.text("Total Amount (Incl. GST):", 155, finalY + 10, { align: 'right' });
            doc.text(`${subscriptionData.currency === 'INR' ? 'Rs.' : subscriptionData.currency} ${totalAmount.toFixed(2)}`, 196, finalY + 10, { align: 'right' });

            doc.setFontSize(9);
            doc.setFont("helvetica", "normal");
            doc.setTextColor(150, 150, 150);
            doc.text("Thank you for your business!", 105, 280, { align: 'center' });
            doc.text("This is a computer-generated invoice and does not require a signature.", 105, 285, { align: 'center' });

            doc.save(`Invoice_${invoiceNo}.pdf`);
        };

        const img = new window.Image();
        img.src = logoUrl;
        img.onload = () => { generatePdfContent(img); };
        img.onerror = () => { generatePdfContent(null); };
    };

    const fetchUserProfile = async (uid) => {
        try {
            const shortUid = uid.substring(0, 6).toUpperCase();
            const userDocRef = doc(db, 'users', uid);
            const userDocSnap = await getDoc(userDocRef);
            if (userDocSnap.exists()) {
                const data = userDocSnap.data();
                setUserProfileData({
                    displayName: data.displayName || '',
                    phoneCode: data.phoneCode || '+91',
                    phoneNumber: data.phoneNumber || '',
                    dob: data.dob || '',
                    gender: data.gender || ''
                });
                if (data.displayName) {
                    setUserName(data.displayName);
                }
                
                // Ensure shortUid is stored for lookup
                if (data.shortUid !== shortUid) {
                    await setDoc(userDocRef, { shortUid: shortUid }, { merge: true });
                }
            } else {
                // Initialize profile with shortUid if it doesn't exist
                await setDoc(userDocRef, {
                    displayName: auth.currentUser?.displayName || '',
                    email: auth.currentUser?.email || '',
                    shortUid: shortUid,
                    createdAt: new Date().toISOString()
                });
            }
        } catch (error) {
            console.error('Error fetching user profile data:', error);
        }
    };

    const handleSaveProfile = async () => {
        if (!auth.currentUser) return;
        setIsSavingProfile(true);
        try {
            // Also update Auth profile display name if changed
            if (userProfileData.displayName && userProfileData.displayName !== auth.currentUser.displayName) {
                await updateProfile(auth.currentUser, { displayName: userProfileData.displayName });
                setUserName(userProfileData.displayName);
            }
            
            const userDocRef = doc(db, 'users', auth.currentUser.uid);
            await setDoc(userDocRef, {
                displayName: userProfileData.displayName || userName,
                email: auth.currentUser.email,
                phoneCode: userProfileData.phoneCode,
                phoneNumber: userProfileData.phoneNumber,
                dob: userProfileData.dob,
                gender: userProfileData.gender,
                shortUid: auth.currentUser.uid.substring(0, 6).toUpperCase(),
                updatedAt: new Date().toISOString()
            }, { merge: true });

            const accountDocRef = doc(db, 'accounts', auth.currentUser.uid);
            await setDoc(accountDocRef, {
                accountName: userProfileData.displayName || userName,
                email: auth.currentUser.email,
                id: auth.currentUser.uid,
                shortUid: auth.currentUser.uid.substring(0, 6).toUpperCase()
            }, { merge: true });

            setActiveSettingsModal(null);
        } catch (error) {
            console.error('Error saving profile:', error);
            showToast('Failed to save profile changes.', 'error');
        } finally {
            setIsSavingProfile(false);
        }
    };

    const handleSubmitDangerAction = async (type) => {
        if (!auth.currentUser) return;
        try {
            let details = '';
            if (type === 'deactivate') {
                details = `User requested temporary account deactivation for ${deactivateDays} days.`;
            } else if (type === 'delete') {
                details = `User requested permanent account deletion.`;
            } else if (type === 'factory_reset') {
                if (!factoryResetReason.trim()) {
                    showToast('Please provide a reason for the factory reset.', 'warning');
                    return;
                }
                details = `User requested factory reset (deletion of all financial data). Reason: ${factoryResetReason}`;
            } else if (type === 'contact_us') {
                if (!contactUsMessage.trim()) {
                    showToast('Please enter a message to send.', 'warning');
                    return;
                }
                details = `Contact Us Message: ${contactUsMessage}`;
            }
            
            await addDoc(collection(db, 'grievances'), {
                userId: auth.currentUser.uid,
                name: userName,
                email: auth.currentUser.email,
                type: 'danger_action',
                actionType: type,
                details: details,
                status: 'Pending',
                createdAt: new Date().toISOString()
            });
            
            showToast(`Your request for ${type.replace('_', ' ')} has been submitted successfully to the admin support. Action will be taken within 48 hours.`);
            setAccountAction(null);
            setFactoryResetReason('');
            setContactUsMessage('');
            fetchPendingGrievance(auth.currentUser.uid);
        } catch (e) {
            console.error(e);
            showToast('Failed to submit request.', 'error');
        }
    };

    useEffect(() => {
        const unsubscribe = auth.onAuthStateChanged((user) => {
            if (user) {
                setUserName(user.displayName || 'User');
                setProfilePicture(user.photoURL || null);
                setUserUid(user.uid.substring(0, 6).toUpperCase());
                setUserProfileData(prev => ({ ...prev, displayName: user.displayName || '' }));
                fetchUserSubscription(user.uid);
                fetchUserProfile(user.uid);
                fetchPendingGrievance(user.uid);
            }
        });

        if (auth.currentUser) {
            setUserName(auth.currentUser.displayName || 'User');
            setProfilePicture(auth.currentUser.photoURL || null);
            setUserUid(auth.currentUser.uid.substring(0, 6).toUpperCase());
            setUserProfileData(prev => ({ ...prev, displayName: auth.currentUser.displayName || '' }));
            fetchUserSubscription(auth.currentUser.uid);
            fetchUserProfile(auth.currentUser.uid);
            fetchPendingGrievance(auth.currentUser.uid);
        }

        fetchAvailableSubscriptions();

        const fetchSystemSettings = async () => {
            try {
                const response = await fetch(`${API_BASE_URL_PYTHON}/api/admin/firestore-read`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ collection_name: 'settings' })
                });
                if (response.ok) {
                    const result = await response.json();
                    const settings = result.data || [];
                    const systemSettings = settings.find(s => s.id === 'system');
                    if (systemSettings && systemSettings.maintenanceMode) {
                        setIsMaintenanceMode(true);
                    } else {
                        setIsMaintenanceMode(false);
                    }
                }
            } catch (e) {
                console.error("Failed to fetch settings", e);
            }
        };
        fetchSystemSettings();
        // optionally refresh settings periodically
        const settingsTimer = setInterval(fetchSystemSettings, 30000);

        const timer = setInterval(() => setCurrentTime(new Date()), 1000);

        return () => {
            unsubscribe();
            clearInterval(timer);
            clearInterval(settingsTimer);
        };
    }, []);

    useEffect(() => {
        if (isAddModalOpen && activeModule.id === 'transactions') {
            Promise.all([
                fetch(`${API_BASE_URL_JAVA}/api/bankaccounts`).then(r => r.ok ? r.json() : []),
                fetch(`${API_BASE_URL_JAVA}/api/deposits`).then(r => r.ok ? r.json() : []),
                fetch(`${API_BASE_URL_JAVA}/api/cards`).then(r => r.ok ? r.json() : [])
            ]).then(([banks, deps, cards]) => {
                setAccountData([...banks, ...deps]);
                setCardData(cards);
            }).catch(err => console.error("Error fetching accounts for transactions modal:", err));
        }
        if (isAddModalOpen && activeModule.id === 'bankaccounts') {
            fetch(`${API_BASE_URL_JAVA}/api/cards`).then(r => r.ok ? r.json() : [])
                .then(cards => setCardData(cards))
                .catch(err => console.error("Error fetching cards for bank accounts modal:", err));
        }
    }, [isAddModalOpen, activeModule]);

    useEffect(() => {
        if (isAddModalOpen && activeModule?.id === 'cards' && formData.cardNumber) {
            const num = formData.cardNumber.replace(/\s+/g, '');
            if (num.length === 6 || num.length === 8) {
                const bin = num;
                if (window._lastFetchedBin === bin) return;
                window._lastFetchedBin = bin;

                fetch(`https://data.handyapi.com/bin/${bin}`)
                    .then(res => {
                        if (res.ok) return res.json();
                        throw new Error("BIN fetch failed");
                    })
                    .then(data => {
                        if (data?.Status === "SUCCESS") {
                            setFormData(prev => {
                                const newData = { ...prev };
                                if (data.Issuer && !prev.cardName) newData.cardName = data.Issuer;
                                if (data.Type) {
                                    const t = data.Type.toLowerCase();
                                    newData.cardType = t === 'credit' ? 'Credit' : 'Debit';
                                }
                                if (data.Scheme) {
                                    const scheme = data.Scheme.toLowerCase();
                                    if (scheme.includes('visa')) newData.cardNetwork = 'VISA';
                                    else if (scheme.includes('master')) newData.cardNetwork = 'Mastercard';
                                    else if (scheme.includes('rupay')) newData.cardNetwork = 'Rupay';
                                    else newData.cardNetwork = 'Others (Mention)';
                                }
                                return newData;
                            });
                        }
                    })
                    .catch(err => console.error("Error fetching BIN data:", err));
            }
        }
    }, [formData.cardNumber, isAddModalOpen, activeModule]);

    // Fetch Borrower Name by UID
    useEffect(() => {
        let isCurrent = true;

        if (isAddModalOpen && activeModule?.id === 'lendings' && formData.borrowerUid) {
            const uidStr = formData.borrowerUid.trim();
            if (uidStr.length >= 3) {
                console.log(`[UID Fetch] Attempting to fetch name for UID: "${uidStr}"`);
                
                const fallbackToBackend = () => {
                    console.log(`[UID Fetch] Falling back to backend API for "${uidStr}"...`);
                    fetch(`${API_BASE_URL_JAVA}/api/auth/user/${uidStr}`)
                        .then(res => {
                            console.log(`[UID Fetch] Backend API response status for "${uidStr}":`, res.status);
                            return res.ok ? res.json() : null;
                        })
                        .then(data => {
                            if (!isCurrent) return;
                            if (data && data.name) {
                                console.log(`[UID Fetch] Found user "${uidStr}" in backend API 'accounts' collection:`, data);
                                if (formData.borrowerName !== data.name) setFormData(prev => ({ ...prev, borrowerName: data.name }));
                            } else {
                                console.warn(`[UID Fetch] User "${uidStr}" NOT found in backend API.`);
                                if (formData.borrowerName) setFormData(prev => ({ ...prev, borrowerName: '' }));
                            }
                        })
                        .catch(err => {
                            if (!isCurrent) return;
                            console.error("[UID Fetch] Error fetching borrower name from backend:", err);
                            if (formData.borrowerName) setFormData(prev => ({ ...prev, borrowerName: '' }));
                        });
                };

                const processFirestoreData = (data) => {
                    if (!isCurrent) return;
                    console.log(`[UID Fetch] Found user "${uidStr}" in Firestore 'users' collection:`, data);
                    const name = data.displayName || data.name || data.accountName;
                    if (name && formData.borrowerName !== name) {
                        setFormData(prev => ({ ...prev, borrowerName: name }));
                    }
                };

                // First try direct Document ID lookup
                getDoc(doc(db, 'users', uidStr))
                    .then(userDoc => {
                        if (!isCurrent) return;
                        if (userDoc.exists()) {
                            processFirestoreData(userDoc.data());
                        } else if (uidStr.length === 6) {
                            // If Document ID fails and it's a 6-character ID, try searching by shortUid field
                            console.log(`[UID Fetch] Searching for shortUid "${uidStr}" in Firestore...`);
                            const q = query(collection(db, 'users'), where('shortUid', '==', uidStr));
                            getDocs(q).then(snapshot => {
                                if (!isCurrent) return;
                                if (!snapshot.empty) {
                                    processFirestoreData(snapshot.docs[0].data());
                                } else {
                                    fallbackToBackend();
                                }
                            }).catch(err => {
                                if (!isCurrent) return;
                                console.error("[UID Fetch] Error querying shortUid:", err);
                                fallbackToBackend();
                            });
                        } else {
                            fallbackToBackend();
                        }
                    })
                    .catch(err => {
                        if (!isCurrent) return;
                        console.error("[UID Fetch] Error fetching borrower name from Firestore:", err);
                        fallbackToBackend();
                    });
            } else {
                // Clear name if UID is less than 3 characters
                if (formData.borrowerName) {
                    setFormData(prev => ({ ...prev, borrowerName: '' }));
                }
            }
        }

        return () => {
            isCurrent = false;
        };
    }, [formData.borrowerUid, isAddModalOpen, activeModule]);

    // Auto-calculate Date of Closing
    useEffect(() => {
        if (isAddModalOpen && activeModule?.id === 'lendings') {
            const { dateLent, interestType, tenure } = formData;
            if (dateLent && interestType && tenure) {
                const date = new Date(dateLent);
                if (!isNaN(date.getTime())) {
                    if (interestType === 'Day') {
                        date.setDate(date.getDate() + parseInt(tenure));
                    } else if (interestType === 'Month') {
                        date.setMonth(date.getMonth() + parseInt(tenure));
                    } else if (interestType === 'Quarter') {
                        date.setMonth(date.getMonth() + (parseInt(tenure) * 3));
                    } else if (interestType === 'Annual') {
                        date.setFullYear(date.getFullYear() + parseInt(tenure));
                    }
                    const closingStr = date.toISOString().split('T')[0];
                    if (formData.dateOfClosing !== closingStr) {
                        setFormData(prev => ({ ...prev, dateOfClosing: closingStr }));
                    }
                }
            }
        }
    }, [formData.dateLent, formData.interestType, formData.tenure, isAddModalOpen, activeModule]);

    const onCropComplete = useCallback((croppedArea, croppedAreaPixels) => {
        setCroppedAreaPixels(croppedAreaPixels);
    }, []);

    const handleFileSelect = (e) => {
        if (e.target.files && e.target.files.length > 0) {
            const reader = new FileReader();
            reader.addEventListener('load', () => {
                setImageSrc(reader.result);
                setIsCropping(true);
            });
            reader.readAsDataURL(e.target.files[0]);
        }
    };

    const handleCropAndUpload = async () => {
        if (!imageSrc || !auth.currentUser) return;

        setIsUploading(true);
        try {
            const croppedImageBlob = await getCroppedImg(imageSrc, croppedAreaPixels);
            const storageRef = ref(storage, `profile_pictures/${auth.currentUser.uid}`);

            await uploadBytes(storageRef, croppedImageBlob);
            const downloadURL = await getDownloadURL(storageRef);

            await updateProfile(auth.currentUser, { photoURL: downloadURL });
            setProfilePicture(downloadURL);

            setIsCropping(false);
            setImageSrc(null);
            showToast("Profile picture updated successfully!", 'success');
        } catch (error) {
            console.error("Error uploading profile picture:", error);
            showToast("Failed to upload profile picture. Please try again.", 'error');
        } finally {
            setIsUploading(false);
        }
    };

    const getGreeting = () => {
        const hour = new Date().getHours();
        if (hour < 12) return 'Good Morning';
        if (hour < 17) return 'Good Afternoon';
        return 'Good Evening';
    };

    const getCurrentMonthYear = () => {
        return new Date().toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
    };

    const handleFormChange = (e) => {
        const { name, value, type } = e.target;
        const finalValue = type === 'number' ? (value ? Number(value) : 0) : value;
        
        setFormData(prev => {
            const updated = { ...prev, [name]: finalValue };
            
            // Auto-calculate Date of Maturity for Investments
            if ((name === 'investmentType' || name === 'startDate') && activeModule.id === 'investments') {
                const type = updated.investmentType;
                const start = updated.startDate;
                if (type && start) {
                    const d = new Date(start);
                    if (type === 'Daily') d.setDate(d.getDate() + 1);
                    else if (type === 'Monthly') d.setMonth(d.getMonth() + 1);
                    else if (type === 'Quarterly') d.setMonth(d.getMonth() + 3);
                    else if (type === 'Annually') d.setFullYear(d.getFullYear() + 1);
                    updated.dateOfMaturity = d.toISOString().split('T')[0];
                }
            }
            return updated;
        });
    };

    const handleAddSubmit = async (e) => {
        e.preventDefault();
        try {
            if (activeModule.id === 'transactions' && formData.accountId) {
                const isDebit = formData.type === 'Debit';
                const isCredit = formData.type === 'Credit';
                const isInterest = formData.type === 'Interest';

                if (isDebit || isCredit || isInterest) {
                    const amountToChange = (isCredit || isInterest) ? formData.amount : -formData.amount;

                    let targetBankAccount = null;
                    let targetDeposit = null;

                    if (formData.accountType === 'Credit Card') {
                        // Find the bank account connected to this card
                        targetBankAccount = accountData.find(a => a.accountType !== undefined && a.connectedCardId === formData.accountId);
                        if (!targetBankAccount) {
                            showToast("Warning: No Bank Account is connected to this Card. Balance will not be updated automatically.", 'warning');
                        }
                    } else {
                        const selectedAcc = accountData.find(a => a.id === formData.accountId);
                        if (selectedAcc) {
                            if (selectedAcc.accountType !== undefined) targetBankAccount = selectedAcc;
                            else targetDeposit = selectedAcc;
                        }
                    }

                    if (targetBankAccount) {
                        const updatedAcc = { ...targetBankAccount, balance: (targetBankAccount.balance || 0) + amountToChange };
                        await fetch(`${API_BASE_URL_JAVA}/api/bankaccounts`, {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify(updatedAcc)
                        });
                    } else if (targetDeposit) {
                        const updatedAcc = { ...targetDeposit, principalAmount: (targetDeposit.principalAmount || 0) + amountToChange };
                        await fetch(`${API_BASE_URL_JAVA}/api/deposits`, {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify(updatedAcc)
                        });
                    }
                }
            }

            let payload = { ...formData };
            if (activeModule.id === 'lendings') {
                payload.lenderUid = userUid;
                payload.lenderName = userName || 'Unknown User';
            }

            if (activeModule.id === 'investments') {
                payload = {
                    ...payload,
                    tickerSymbol: formData.assetSymbol,
                    description: formData.investmentName,
                    initialUnitCost: formData.amountInvested
                };
            }

            const res = await fetch(`${API_BASE_URL_JAVA}${activeModule.endpoint}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            await res.text();

            setIsAddModalOpen(false);
            setFormData({});
            setRefreshTrigger(prev => prev + 1);
        } catch (err) {
            console.error("Failed to add", err);
        }
    };

    const toggleTheme = () => setTheme(prev => prev === 'light' ? 'dark' : 'light');

    if (isMaintenanceMode) {
        return (
            <div data-theme={theme} style={{
                width: '100vw', height: '100vh',
                background: 'var(--dash-bg)', color: 'var(--dash-text)',
                display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
                fontFamily: 'system-ui, -apple-system, sans-serif',
                position: 'fixed', top: 0, left: 0, zIndex: 9999
            }}>
                {theme === 'dark' && (
                    <>
                        <div className="auth-blob auth-blob-1" style={{ zIndex: 0, opacity: 0.15, pointerEvents: 'none' }}></div>
                        <div className="auth-blob auth-blob-2" style={{ zIndex: 0, opacity: 0.15, pointerEvents: 'none' }}></div>
                    </>
                )}
                <div style={{
                    zIndex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center',
                    background: 'var(--dash-glass-bg)', padding: '50px', borderRadius: '24px',
                    border: '1px solid var(--dash-border)', backdropFilter: 'blur(16px)', WebkitBackdropFilter: 'blur(16px)',
                    boxShadow: '0 20px 60px rgba(0,0,0,0.1)', textAlign: 'center', maxWidth: '500px'
                }}>
                    <div style={{ position: 'relative', marginBottom: '30px' }}>
                        <div style={{
                            position: 'absolute', top: '-10px', left: '-10px', right: '-10px', bottom: '-10px',
                            background: 'radial-gradient(circle, rgba(52,211,153,0.3) 0%, transparent 70%)',
                            borderRadius: '50%', animation: 'spinPulse 4s infinite linear'
                        }}></div>
                        <img 
                            src="/assets/logo.png" 
                            alt="SmartLedger Logo" 
                            style={{ 
                                width: '120px', height: '120px', objectFit: 'cover', borderRadius: '20px',
                                border: '2px solid rgba(52,211,153,0.5)', position: 'relative', zIndex: 2,
                                animation: 'float 3s ease-in-out infinite'
                            }} 
                        />
                    </div>
                    
                    <h1 style={{ fontSize: '32px', fontWeight: '800', marginBottom: '16px', background: 'linear-gradient(135deg, #34d399, #38bdf8)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
                        System Maintenance
                    </h1>
                    
                    <p style={{ fontSize: '16px', color: 'var(--dash-text-muted)', lineHeight: '1.6', marginBottom: '24px' }}>
                        We are currently performing scheduled maintenance to upgrade SmartLedger's infrastructure. 
                        Our systems will be back online shortly.
                    </p>
                    
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', background: 'rgba(52,211,153,0.1)', padding: '12px 20px', borderRadius: '12px', border: '1px solid rgba(52,211,153,0.2)' }}>
                        <div style={{ width: '10px', height: '10px', background: '#34d399', borderRadius: '50%', animation: 'ping 1.5s cubic-bezier(0, 0, 0.2, 1) infinite' }}></div>
                        <span style={{ color: '#34d399', fontWeight: '600', fontSize: '14px', letterSpacing: '0.5px' }}>Upgrading Systems...</span>
                    </div>
                </div>

                <style>
                    {`
                    @keyframes float {
                        0% { transform: translateY(0px); }
                        50% { transform: translateY(-10px); }
                        100% { transform: translateY(0px); }
                    }
                    @keyframes spinPulse {
                        0% { transform: rotate(0deg) scale(1); opacity: 0.5; }
                        50% { transform: rotate(180deg) scale(1.1); opacity: 0.8; }
                        100% { transform: rotate(360deg) scale(1); opacity: 0.5; }
                    }
                    @keyframes ping {
                        75%, 100% { transform: scale(2); opacity: 0; }
                    }
                    `}
                </style>
            </div>
        );
    }

    return (
        <div data-theme={theme} style={{
            width: '100vw', height: '100vh',
            background: 'var(--dash-bg)', color: 'var(--dash-text)',
            display: 'flex', position: 'fixed', top: 0, left: 0, zIndex: 100,
            overflow: 'hidden', fontFamily: 'system-ui, -apple-system, sans-serif'
        }}>
            {theme === 'dark' && (
                <>
                    <div className="auth-blob auth-blob-1" style={{ zIndex: 0, opacity: 0.15, pointerEvents: 'none' }}></div>
                    <div className="auth-blob auth-blob-2" style={{ zIndex: 0, opacity: 0.15, pointerEvents: 'none' }}></div>
                    <div className="auth-blob auth-blob-3" style={{ zIndex: 0, opacity: 0.15, pointerEvents: 'none' }}></div>
                </>
            )}

            {/* Add Modal */}
            {isAddModalOpen && (
                <div style={{
                    position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
                    background: 'rgba(0,0,0,0.5)', backdropFilter: 'blur(4px)',
                    display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 100
                }}>
                    <div className="glass-card" style={{ width: '400px', padding: '24px', background: 'var(--dash-glass-bg)', backdropFilter: 'blur(16px)', WebkitBackdropFilter: 'blur(16px)', border: '1px solid var(--dash-border)', borderRadius: '24px', maxHeight: '90vh', display: 'flex', flexDirection: 'column', boxShadow: '0 10px 40px rgba(0,0,0,0.2)' }}>
                        <h2 style={{ marginBottom: '24px', color: 'var(--dash-text)', flexShrink: 0, fontWeight: '800', letterSpacing: '-0.5px' }}>{formData.id ? 'Edit' : 'Add'} {activeModule.label}</h2>
                        <form onSubmit={handleAddSubmit} style={{ display: 'flex', flexDirection: 'column', flex: 1, overflow: 'hidden' }}>
                            <div style={{ flex: 1, overflowY: 'auto', paddingRight: '8px' }}>
                                {activeModule.fields.map(field => {
                                    if (field.type === 'custom') return null;
                                    if (field.name === 'cardNetworkOther' && formData.cardNetwork !== 'Others (Mention)') return null;
                                    if (field.name === 'annualFee' && formData.cardFeeType !== 'Annual Fee') return null;
                                    if (field.name === 'dateOfAccountOpening' && formData.accountType !== 'Fixed Deposit' && formData.accountType !== 'Recurring Deposit') return null;
                                    if (field.name === 'creditLimit' && formData.cardType === 'Debit') return null;

                                    if (field.name === 'upiId' && (formData.accountType === 'Fixed Deposit' || formData.accountType === 'Recurring Deposit')) return null;

                                    if (field.name === 'dateOfMature' && formData.accountType !== 'Fixed Deposit' && formData.accountType !== 'Recurring Deposit') return null;

                                    if (field.name === 'connectedCardId') {
                                        if (formData.accountType === 'Fixed Deposit' || formData.accountType === 'Recurring Deposit') return null;
                                        return (
                                            <div key={field.name} className="input-group" style={{ marginBottom: '12px' }}>
                                                <label style={{ color: 'var(--dash-text-muted)', fontSize: '13px', fontWeight: '600', marginBottom: '4px', display: 'block' }}>{field.label}</label>
                                                <select
                                                    name={field.name}
                                                    value={formData[field.name] || ''}
                                                    onChange={handleFormChange}
                                                    style={{ width: '100%', padding: '12px 14px', borderRadius: '12px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                                                >
                                                    <option value="" style={{ background: 'var(--dash-bg)', color: 'var(--dash-text)' }}>No Card Connected</option>
                                                    {cardData.map(card => (
                                                        <option key={card.id} value={card.id} style={{ background: 'var(--dash-bg)', color: 'var(--dash-text)' }}>
                                                            {card.cardName} ending in {card.cardNumber?.slice(-4) || 'XXXX'}
                                                        </option>
                                                    ))}
                                                </select>
                                            </div>
                                        );
                                    }

                                    if (field.name === 'category') {
                                        return (
                                            <div key={field.name} className="input-group" style={{ marginBottom: '12px' }}>
                                                <label style={{ color: 'var(--dash-text-muted)', fontSize: '13px', fontWeight: '600', marginBottom: '4px', display: 'block' }}>Category</label>
                                                <div style={{ display: 'flex', gap: '8px' }}>
                                                    <select
                                                        name={field.name}
                                                        value={formData[field.name] || ''}
                                                        onChange={handleFormChange}
                                                        style={{ flex: 1, padding: '12px 14px', borderRadius: '12px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                                                        required
                                                    >
                                                        <option value="" style={{ background: 'var(--dash-bg)', color: 'var(--dash-text)' }}>Select...</option>
                                                        {categories.map(opt => <option key={opt} value={opt} style={{ background: 'var(--dash-bg)', color: 'var(--dash-text)' }}>{opt}</option>)}
                                                    </select>
                                                    <button type="button" onClick={() => {
                                                        const newCat = window.prompt("Enter new category name:");
                                                        if (newCat && newCat.trim()) {
                                                            setCategories(prev => [...prev, newCat.trim()]);
                                                            setFormData(prev => ({ ...prev, category: newCat.trim() }));
                                                        }
                                                    }} style={{ padding: '10px', borderRadius: '12px', background: 'rgba(255, 255, 255, 0.05)', border: '1px solid var(--dash-border)', cursor: 'pointer', fontSize: '16px' }}>➕</button>
                                                </div>
                                            </div>
                                        );
                                    }

                                    if (field.name === 'accountId') {
                                        let isCard = formData.accountType === 'Credit Card';
                                        let filteredList = [];

                                        if (isCard) {
                                            filteredList = cardData.filter(c => c.cardType === 'Credit');
                                        } else if (formData.accountType) {
                                            filteredList = accountData.filter(acc => {
                                                if (formData.accountType === 'Savings Account' && acc.accountType === 'Savings Account') return true;
                                                if (formData.accountType === 'Current Account' && acc.accountType === 'Current Account') return true;
                                                if (formData.accountType === 'Fixed Deposit' && (acc.accountType === 'Fixed Deposit' || acc.depositType === 'FD')) return true;
                                                if (formData.accountType === 'Recurring Deposit' && (acc.accountType === 'Recurring Deposit' || acc.depositType === 'RD')) return true;
                                                if (formData.accountType === 'Gullak-cash' && acc.depositType === 'Gullak') return true;
                                                return false;
                                            });
                                        }

                                        return (
                                            <div key={field.name} className="input-group" style={{ marginBottom: '12px' }}>
                                                <label style={{ color: 'var(--dash-text-muted)', fontSize: '13px', fontWeight: '600', marginBottom: '4px', display: 'block' }}>{isCard ? 'Card' : 'Account'}</label>
                                                <select
                                                    name={field.name}
                                                    value={formData[field.name] || ''}
                                                    onChange={handleFormChange}
                                                    style={{ width: '100%', padding: '12px 14px', borderRadius: '12px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                                                    required
                                                >
                                                    <option value="" style={{ background: 'var(--dash-bg)', color: 'var(--dash-text)' }}>Select {isCard ? 'Card' : 'Account'}...</option>
                                                    {isCard ? filteredList.map(card => (
                                                        <option key={card.id} value={card.id} style={{ background: 'var(--dash-bg)', color: 'var(--dash-text)' }}>
                                                            {card.cardName} ending in {card.cardNumber?.slice(-4) || 'XXXX'} - Limit: ₹{card.creditLimit}
                                                        </option>
                                                    )) : filteredList.map(acc => (
                                                        <option key={acc.id} value={acc.id} style={{ background: 'var(--dash-bg)', color: 'var(--dash-text)' }}>
                                                            {acc.accountName || acc.holderName} {acc.accountNumber ? `(${acc.accountNumber})` : ''} - ₹{acc.balance ?? acc.principalAmount ?? 0}
                                                        </option>
                                                    ))}
                                                </select>
                                            </div>
                                        );
                                    }

                                    let displayLabel = field.label || field.name.charAt(0).toUpperCase() + field.name.slice(1).replace(/([A-Z])/g, ' $1');
                                    if ((field.name === 'balance' || field.name === 'principalAmount') && (formData.accountType === 'Recurring Deposit' || formData.depositType === 'RD')) {
                                        displayLabel = 'Monthly Deposit Amount';
                                    }
                                    if (field.name === 'tenure' && formData.interestType && (activeModule.id === 'lendings' || activeModule.id === 'loans')) {
                                        displayLabel = `Type in ${formData.interestType}(s)`;
                                    }

                                    return (
                                        <div key={field.name} className="input-group" style={{ marginBottom: '12px' }}>
                                            <label style={{ color: 'var(--dash-text-muted)', fontSize: '13px', fontWeight: '600', marginBottom: '4px', display: 'block' }}>
                                                {field.name === 'quantity' && formData.assetType === 'Metal - Resource' ? 'Quantity Owned (Grams)' : displayLabel}
                                            </label>
                                            {field.type === 'asset-search' ? (
                                                <AssetSymbolAutocomplete
                                                    assetType={formData.assetType}
                                                    value={formData[field.name]}
                                                    onChange={handleFormChange}
                                                />
                                            ) : field.type === 'select' ? (
                                                <select
                                                    name={field.name}
                                                    value={formData[field.name] || ''}
                                                    onChange={handleFormChange}
                                                    style={{ width: '100%', padding: '12px 14px', borderRadius: '12px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                                                    required
                                                >
                                                    <option value="" style={{ background: 'var(--dash-bg)', color: 'var(--dash-text)' }}>Select...</option>
                                                    {field.options.map(opt => <option key={opt} value={opt} style={{ background: 'var(--dash-bg)', color: 'var(--dash-text)' }}>{opt}</option>)}
                                                </select>
                                            ) : (
                                                <input
                                                    type={field.type}
                                                    name={field.name}
                                                    value={formData[field.name] !== undefined ? formData[field.name] : ''}
                                                    onChange={handleFormChange}
                                                    onClick={(e) => { if (field.type === 'date' && e.target.showPicker && !field.readOnly) e.target.showPicker(); }}
                                                    style={{ width: '100%', padding: '12px 14px', borderRadius: '12px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none', cursor: field.type === 'date' && !field.readOnly ? 'pointer' : 'text', colorScheme: theme === 'dark' ? 'dark' : 'light', opacity: field.readOnly ? 0.6 : 1 }}
                                                    required={!field.readOnly}
                                                    readOnly={field.readOnly}
                                                    {...(field.type === 'number' ? { step: 'any' } : {})}
                                                />
                                            )}
                                        </div>
                                    );
                                })}
                            </div>
                            <div style={{ display: 'flex', gap: '12px', marginTop: '24px', flexShrink: 0, paddingBottom: '4px' }}>
                                <button type="button" className="btn-secondary" style={{ flex: 1, padding: '14px', borderRadius: '14px', background: 'transparent', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', cursor: 'pointer', fontWeight: '600', transition: 'all 0.3s ease' }} onMouseOver={(e) => { e.currentTarget.style.background = 'rgba(255, 255, 255, 0.05)'; e.currentTarget.style.borderColor = 'rgba(255, 255, 255, 0.2)'; }} onMouseOut={(e) => { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.borderColor = 'var(--dash-border)'; }} onClick={() => { setIsAddModalOpen(false); setFormData({}); }}>Cancel</button>
                                <button type="submit" className="btn-primary" style={{ flex: 1, padding: '14px', borderRadius: '14px', background: 'linear-gradient(135deg, #34d399, #10b981)', color: '#000', border: 'none', cursor: 'pointer', fontWeight: '700', boxShadow: '0 6px 20px rgba(52, 211, 153, 0.3)', transition: 'all 0.3s ease' }} onMouseOver={(e) => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 10px 25px rgba(52, 211, 153, 0.45)'; }} onMouseOut={(e) => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 6px 20px rgba(52, 211, 153, 0.3)'; }}>Save</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Settings Modals */}
            {activeSettingsModal && (
                <div style={{
                    position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
                    background: 'rgba(0,0,0,0.5)', backdropFilter: 'blur(4px)',
                    display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 200
                }}>
                    <div className="glass-card" style={{ width: activeSettingsModal === 'editProfile' ? '480px' : '400px', padding: '32px', background: theme === 'dark' ? 'var(--dash-glass-bg)' : 'rgba(255, 255, 255, 0.95)', backdropFilter: 'blur(24px)', WebkitBackdropFilter: 'blur(24px)', border: '1px solid var(--dash-border)', borderRadius: '24px', display: 'flex', flexDirection: 'column', maxHeight: '85vh', overflow: 'hidden', boxShadow: theme === 'dark' ? '0 25px 50px -12px rgba(0,0,0,0.3)' : '0 25px 50px -12px rgba(0,0,0,0.1)' }}>
                        <h2 style={{ marginBottom: '24px', color: 'var(--dash-text)', fontSize: '24px', fontWeight: '800', flexShrink: 0, letterSpacing: '-0.5px' }}>
                            {activeSettingsModal === 'editProfile' && 'Edit Profile'}
                            {activeSettingsModal === 'privacyPolicy' && 'Privacy Policy'}
                            {activeSettingsModal === 'enable2FA' && 'Two-Factor Authentication'}
                            {activeSettingsModal === 'subscriptions' && 'Your Subscriptions'}
                            {activeSettingsModal === 'contactUs' && 'Contact Us'}
                        </h2>

                        <div style={{ color: 'var(--text-muted)', lineHeight: '1.6', flex: 1, overflowY: 'auto', paddingRight: '8px', marginBottom: '16px' }}>
                            {activeSettingsModal === 'editProfile' && (
                                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                                    
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '20px', padding: '20px', background: 'rgba(52, 211, 153, 0.05)', borderRadius: '16px', border: '1px solid rgba(52, 211, 153, 0.1)' }}>
                                        <div style={{ position: 'relative', width: '72px', height: '72px', borderRadius: '50%', background: 'linear-gradient(135deg, #34d399, #10b981)', color: '#000', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: '800', fontSize: '28px', overflow: 'hidden', boxShadow: '0 8px 16px rgba(52, 211, 153, 0.2)' }}>
                                            {profilePicture ? (
                                                <img src={profilePicture} alt="Profile" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                                            ) : (
                                                userName.charAt(0).toUpperCase()
                                            )}
                                            {isUploading && (
                                                <div style={{ position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.6)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontSize: '11px', fontWeight: '700' }}>WAIT..</div>
                                            )}
                                            <div style={{ position: 'absolute', bottom: '0px', right: '0px', background: 'var(--dash-bg)', borderRadius: '50%', padding: '6px', border: '2px solid rgba(52, 211, 153, 0.3)', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 10 }}>
                                                <Camera size={12} color="#34d399" />
                                            </div>
                                            <input type="file" accept="image/*" onChange={handleFileSelect} disabled={isUploading} style={{ position: 'absolute', inset: 0, opacity: 0, cursor: 'pointer', zIndex: 20 }} title="Change Profile Picture" />
                                        </div>
                                        <div style={{ flex: 1 }}>
                                            <div style={{ color: 'var(--dash-text)', fontWeight: '700', fontSize: '16px', marginBottom: '4px' }}>Profile Picture</div>
                                            <div style={{ color: 'var(--dash-text-muted)', fontSize: '12.5px', lineHeight: '1.4' }}>Upload a new avatar. Larger images will be resized automatically.</div>
                                        </div>
                                    </div>

                                    <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '16px' }}>
                                        <div className="input-group">
                                            <label style={{ color: 'var(--dash-text)', display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '8px', fontSize: '13px', fontWeight: '600' }}>
                                                <User size={14} color="#34d399" /> Display Name
                                            </label>
                                            <input type="text" value={userProfileData.displayName} onChange={(e) => setUserProfileData({...userProfileData, displayName: e.target.value})} style={{ width: '100%', padding: '14px 16px', borderRadius: '12px', background: theme === 'dark' ? 'rgba(0,0,0,0.2)' : 'rgba(255,255,255,0.5)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none', transition: 'all 0.2s', fontSize: '14px', fontWeight: '500' }} onFocus={(e) => { e.target.style.borderColor = '#34d399'; e.target.style.boxShadow = '0 0 0 3px rgba(52, 211, 153, 0.1)'; }} onBlur={(e) => { e.target.style.borderColor = 'var(--dash-border)'; e.target.style.boxShadow = 'none'; }} />
                                        </div>
                                        
                                        <div className="input-group">
                                            <label style={{ color: 'var(--dash-text)', display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '8px', fontSize: '13px', fontWeight: '600' }}>
                                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#34d399" strokeWidth="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg> Email Address
                                            </label>
                                            <input type="email" defaultValue={auth.currentUser?.email || ''} readOnly style={{ width: '100%', padding: '14px 16px', borderRadius: '12px', background: theme === 'dark' ? 'rgba(0,0,0,0.1)' : 'rgba(255,255,255,0.3)', color: 'var(--dash-text-muted)', border: '1px solid var(--dash-border)', outline: 'none', fontSize: '14px', fontWeight: '500', opacity: 0.7, cursor: 'not-allowed' }} title="Email address cannot be changed here" />
                                        </div>

                                        <div className="input-group">
                                            <label style={{ color: 'var(--dash-text)', display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '8px', fontSize: '13px', fontWeight: '600' }}>
                                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#34d399" strokeWidth="2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path></svg> Phone Number
                                            </label>
                                            <div style={{ display: 'flex', gap: '8px' }}>
                                                <select value={userProfileData.phoneCode} onChange={(e) => setUserProfileData({...userProfileData, phoneCode: e.target.value})} style={{ padding: '14px', borderRadius: '12px', background: theme === 'dark' ? 'rgba(0,0,0,0.2)' : 'rgba(255,255,255,0.5)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', width: '100px', outline: 'none', transition: 'all 0.2s', appearance: 'none', cursor: 'pointer', fontSize: '14px', fontWeight: '500' }} onFocus={(e) => { e.target.style.borderColor = '#34d399'; e.target.style.boxShadow = '0 0 0 3px rgba(52, 211, 153, 0.1)'; }} onBlur={(e) => { e.target.style.borderColor = 'var(--dash-border)'; e.target.style.boxShadow = 'none'; }}>
                                                    {countryOptions.map(country => (
                                                        <option key={country.code} value={country.dial} style={{ background: 'var(--dash-bg)', color: 'var(--dash-text)' }}>
                                                            {country.dial}
                                                        </option>
                                                    ))}
                                                </select>
                                                <input type="tel" value={userProfileData.phoneNumber} onChange={(e) => setUserProfileData({...userProfileData, phoneNumber: e.target.value})} placeholder="00000 00000" style={{ flex: 1, padding: '14px 16px', borderRadius: '12px', background: theme === 'dark' ? 'rgba(0,0,0,0.2)' : 'rgba(255,255,255,0.5)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none', transition: 'all 0.2s', fontSize: '14px', fontWeight: '500' }} onFocus={(e) => { e.target.style.borderColor = '#34d399'; e.target.style.boxShadow = '0 0 0 3px rgba(52, 211, 153, 0.1)'; }} onBlur={(e) => { e.target.style.borderColor = 'var(--dash-border)'; e.target.style.boxShadow = 'none'; }} />
                                            </div>
                                        </div>
                                        
                                        <div style={{ display: 'flex', gap: '16px' }}>
                                            <div className="input-group" style={{ flex: 1 }}>
                                                <label style={{ color: 'var(--dash-text)', display: 'block', marginBottom: '8px', fontSize: '13px', fontWeight: '600' }}>Date of Birth</label>
                                                <input type="date" value={userProfileData.dob} onChange={(e) => setUserProfileData({...userProfileData, dob: e.target.value})} style={{ width: '100%', padding: '14px 16px', borderRadius: '12px', background: theme === 'dark' ? 'rgba(0,0,0,0.2)' : 'rgba(255,255,255,0.5)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none', transition: 'all 0.2s', colorScheme: theme === 'dark' ? 'dark' : 'light', fontSize: '14px', fontWeight: '500' }} onFocus={(e) => { e.target.style.borderColor = '#34d399'; e.target.style.boxShadow = '0 0 0 2px rgba(52, 211, 153, 0.1)'; }} onBlur={(e) => { e.target.style.borderColor = 'var(--dash-border)'; e.target.style.boxShadow = 'none'; }} />
                                            </div>
                                            <div className="input-group" style={{ flex: 1 }}>
                                                <label style={{ color: 'var(--dash-text)', display: 'block', marginBottom: '8px', fontSize: '13px', fontWeight: '600' }}>Gender</label>
                                                <select value={userProfileData.gender} onChange={(e) => setUserProfileData({...userProfileData, gender: e.target.value})} style={{ width: '100%', padding: '14px 16px', borderRadius: '12px', background: theme === 'dark' ? 'rgba(0,0,0,0.2)' : 'rgba(255,255,255,0.5)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none', transition: 'all 0.2s', appearance: 'none', cursor: 'pointer', fontSize: '14px', fontWeight: '500' }} onFocus={(e) => { e.target.style.borderColor = '#34d399'; e.target.style.boxShadow = '0 0 0 2px rgba(52, 211, 153, 0.1)'; }} onBlur={(e) => { e.target.style.borderColor = 'var(--dash-border)'; e.target.style.boxShadow = 'none'; }}>
                                                    <option value="" disabled style={{ background: 'var(--dash-bg)', color: 'var(--dash-text-muted)' }}>Select Gender</option>
                                                    <option value="male" style={{ background: 'var(--dash-bg)', color: 'var(--dash-text)' }}>Male</option>
                                                    <option value="female" style={{ background: 'var(--dash-bg)', color: 'var(--dash-text)' }}>Female</option>
                                                    <option value="others" style={{ background: 'var(--dash-bg)', color: 'var(--dash-text)' }}>Others</option>
                                                </select>
                                            </div>
                                        </div>
                                    </div>

                                    <div style={{ display: 'flex', gap: '16px', background: theme === 'dark' ? 'rgba(52, 211, 153, 0.05)' : 'rgba(52, 211, 153, 0.15)', border: theme === 'dark' ? '1px solid rgba(52, 211, 153, 0.15)' : '1px solid rgba(52, 211, 153, 0.3)', padding: '16px', borderRadius: '16px', marginTop: '4px' }}>
                                        <Shield size={24} color={theme === 'dark' ? '#34d399' : '#059669'} style={{ flexShrink: 0 }} />
                                        <div style={{ fontSize: '12.5px', color: 'var(--dash-text)', lineHeight: '1.5' }}>
                                            <strong style={{ color: theme === 'dark' ? '#e2e8f0' : '#064e3b' }}>Bank-grade Security.</strong> Your personal data and financial transactions are end-to-end encrypted. We never share your data.
                                        </div>
                                    </div>
                                </div>
                            )}
                            {activeSettingsModal === 'privacyPolicy' && (
                                <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
                                    <p style={{ margin: 0 }}>We take your privacy seriously. Your financial data is encrypted and securely stored. We never share your personal information with third parties without your explicit consent. Read our full policy on our website.</p>

                                    <div style={{ borderTop: '1px solid var(--dash-border)', paddingTop: '24px' }}>
                                        <h3 style={{ color: '#ef4444', fontSize: '16px', fontWeight: '600', marginBottom: '16px' }}>
                                            Danger Zone
                                        </h3>
                                        <div style={{ display: 'flex', gap: '8px', marginBottom: '16px', flexWrap: 'wrap' }}>
                                            <button
                                                style={{ flex: '1 1 auto', padding: '10px 14px', borderRadius: '10px', background: accountAction === 'deactivate' ? 'rgba(245, 158, 11, 0.1)' : (theme === 'dark' ? 'rgba(255, 255, 255, 0.05)' : 'rgba(0, 0, 0, 0.05)'), color: accountAction === 'deactivate' ? '#f59e0b' : 'var(--dash-text)', border: `1px solid ${accountAction === 'deactivate' ? 'rgba(245, 158, 11, 0.3)' : (theme === 'dark' ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)')}`, cursor: 'pointer', transition: 'all 0.2s', fontSize: '13px', fontWeight: '500' }}
                                                onClick={() => setAccountAction('deactivate')}
                                            >
                                                Deactivate
                                            </button>
                                            <button
                                                style={{ flex: '1 1 auto', padding: '10px 14px', borderRadius: '10px', background: accountAction === 'factory_reset' ? 'rgba(168, 85, 247, 0.1)' : (theme === 'dark' ? 'rgba(255, 255, 255, 0.05)' : 'rgba(0, 0, 0, 0.05)'), color: accountAction === 'factory_reset' ? '#a855f7' : 'var(--dash-text)', border: `1px solid ${accountAction === 'factory_reset' ? 'rgba(168, 85, 247, 0.3)' : (theme === 'dark' ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)')}`, cursor: 'pointer', transition: 'all 0.2s', fontSize: '13px', fontWeight: '500' }}
                                                onClick={() => setAccountAction('factory_reset')}
                                            >
                                                Factory Reset
                                            </button>
                                            <button
                                                style={{ flex: '1 1 auto', padding: '10px 14px', borderRadius: '10px', background: accountAction === 'delete' ? 'rgba(239, 68, 68, 0.1)' : (theme === 'dark' ? 'rgba(255, 255, 255, 0.05)' : 'rgba(0, 0, 0, 0.05)'), color: accountAction === 'delete' ? '#ef4444' : 'var(--dash-text)', border: `1px solid ${accountAction === 'delete' ? 'rgba(239, 68, 68, 0.3)' : (theme === 'dark' ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)')}`, cursor: 'pointer', transition: 'all 0.2s', fontSize: '13px', fontWeight: '500' }}
                                                onClick={() => setAccountAction('delete')}
                                            >
                                                Delete Account
                                            </button>
                                        </div>

                                        {accountAction === 'deactivate' && (
                                            <div style={{ background: theme === 'dark' ? 'rgba(245, 158, 11, 0.05)' : 'rgba(245, 158, 11, 0.1)', border: '1px solid rgba(245, 158, 11, 0.2)', padding: '16px', borderRadius: '12px', animation: 'slideUp 0.2s ease forwards' }}>
                                                {pendingGrievance ? (
                                                    <div style={{ color: '#f59e0b', fontSize: '14px', fontWeight: 'bold', textAlign: 'center', padding: '10px' }}>You already have an active grievance request in progress. Please wait for it to be resolved.</div>
                                                ) : (
                                                    <>
                                                        <label style={{ display: 'block', color: '#f59e0b', marginBottom: '8px', fontSize: '13px', fontWeight: '600' }}>Deactivate for how many days?</label>
                                                        <select value={deactivateDays} onChange={(e) => setDeactivateDays(e.target.value)} style={{ width: '100%', padding: '12px 16px', borderRadius: '8px', background: theme === 'dark' ? 'var(--dash-glass-bg)' : '#fff', color: 'var(--dash-text)', border: '1px solid rgba(245, 158, 11, 0.4)', outline: 'none', cursor: 'pointer', fontSize: '13px', fontWeight: '500' }}>
                                                            <option value="7" style={{ background: 'var(--dash-bg)' }}>7 Days</option>
                                                            <option value="14" style={{ background: 'var(--dash-bg)' }}>14 Days</option>
                                                            <option value="30" style={{ background: 'var(--dash-bg)' }}>30 Days</option>
                                                            <option value="90" style={{ background: 'var(--dash-bg)' }}>90 Days</option>
                                                        </select>
                                                        <p style={{ fontSize: '12px', color: 'var(--dash-text-muted)', marginTop: '12px', lineHeight: '1.4' }}>Your account will be temporarily deactivated. You can log back in anytime to reconfirm and restore access.</p>
                                                        <button onClick={() => handleSubmitDangerAction('deactivate')} style={{ width: '100%', marginTop: '16px', padding: '10px', background: '#f59e0b', color: '#fff', borderRadius: '8px', border: 'none', fontWeight: '600', cursor: 'pointer' }}>Submit Deactivation Request</button>
                                                    </>
                                                )}
                                            </div>
                                        )}

                                        {accountAction === 'factory_reset' && (
                                            <div style={{ background: theme === 'dark' ? 'rgba(168, 85, 247, 0.05)' : 'rgba(168, 85, 247, 0.1)', border: '1px solid rgba(168, 85, 247, 0.2)', padding: '16px', borderRadius: '12px', animation: 'slideUp 0.2s ease forwards' }}>
                                                {pendingGrievance ? (
                                                    <div style={{ color: '#a855f7', fontSize: '14px', fontWeight: 'bold', textAlign: 'center', padding: '10px' }}>You already have an active grievance request in progress. Please wait for it to be resolved.</div>
                                                ) : (
                                                    <>
                                                        <label style={{ display: 'block', color: '#a855f7', marginBottom: '8px', fontSize: '13px', fontWeight: '600' }}>Reason for Factory Reset</label>
                                                        <textarea value={factoryResetReason} onChange={(e) => setFactoryResetReason(e.target.value)} placeholder="Explain why you want to delete all financial data..." style={{ width: '100%', padding: '12px 16px', borderRadius: '8px', background: theme === 'dark' ? 'var(--dash-glass-bg)' : '#fff', color: 'var(--dash-text)', border: '1px solid rgba(168, 85, 247, 0.4)', outline: 'none', resize: 'vertical', minHeight: '80px', fontSize: '13px', fontFamily: 'inherit' }}></textarea>
                                                        <p style={{ fontSize: '12px', color: '#a855f7', marginTop: '12px', lineHeight: '1.4' }}><strong>Warning:</strong> This will permanently wipe all your transactions, budgets, and financial records. Support will resolve this within 48 hours.</p>
                                                        <button onClick={() => handleSubmitDangerAction('factory_reset')} style={{ width: '100%', marginTop: '16px', padding: '10px', background: '#a855f7', color: '#fff', borderRadius: '8px', border: 'none', fontWeight: '600', cursor: 'pointer' }}>Submit Reset Request</button>
                                                    </>
                                                )}
                                            </div>
                                        )}

                                        {accountAction === 'delete' && (
                                            <div style={{ background: theme === 'dark' ? 'rgba(239, 68, 68, 0.05)' : 'rgba(239, 68, 68, 0.1)', border: '1px solid rgba(239, 68, 68, 0.2)', padding: '16px', borderRadius: '12px', color: '#ef4444', fontSize: '13px', lineHeight: '1.5', animation: 'slideUp 0.2s ease forwards' }}>
                                                {pendingGrievance ? (
                                                    <div style={{ fontSize: '14px', fontWeight: 'bold', textAlign: 'center', padding: '10px' }}>You already have an active grievance request in progress. Please wait for it to be resolved.</div>
                                                ) : (
                                                    <>
                                                        <strong>Warning:</strong> If you proceed, your account will be permanently scheduled for deletion. <br /><br /><strong>It will take 30 days</strong> to fully delete your data, during which you can cancel the request by logging back in. Admin support will process this request.
                                                        <button onClick={() => handleSubmitDangerAction('delete')} style={{ width: '100%', marginTop: '16px', padding: '10px', background: '#ef4444', color: '#fff', borderRadius: '8px', border: 'none', fontWeight: '600', cursor: 'pointer' }}>Submit Deletion Request</button>
                                                    </>
                                                )}
                                            </div>
                                        )}
                                    </div>
                                </div>
                            )}
                            {activeSettingsModal === 'enable2FA' && (
                                <div style={{ textAlign: 'center' }}>
                                    <Shield size={48} color="#34d399" style={{ marginBottom: '16px' }} />
                                    <p>Protect your account with an extra layer of security. Once configured, you'll be required to enter both your password and an authentication code from your mobile phone in order to sign in.</p>
                                </div>
                            )}
                            {activeSettingsModal === 'subscriptions' && (
                                <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                                    {subLoading ? (
                                        <div style={{ color: 'var(--text-muted)' }}>Loading subscriptions...</div>
                                    ) : userSubscription ? (
                                        <div style={{ background: 'var(--bg-dark)', padding: '16px', borderRadius: '12px', border: `1px solid ${userSubscription.status?.toLowerCase() === 'active' ? '#34d399' : '#f59e0b'}` }}>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                                                <strong style={{ color: 'var(--text-main)' }}>{userSubscription.planName || 'SmartLedger Plan'}</strong>
                                                <span style={{ color: userSubscription.status?.toLowerCase() === 'active' ? '#34d399' : '#f59e0b', fontSize: '12px', fontWeight: 'bold', background: userSubscription.status?.toLowerCase() === 'active' ? 'rgba(52, 211, 153, 0.1)' : 'rgba(245, 158, 11, 0.1)', padding: '4px 8px', borderRadius: '20px' }}>{userSubscription.status ? userSubscription.status.toUpperCase() : 'PENDING'}</span>
                                            </div>
                                            <div style={{ fontSize: '14px', marginBottom: '16px' }}>
                                                {userSubscription.currency === 'INR' ? '₹' : (userSubscription.currency === 'GBP' ? '£' : (userSubscription.currency === 'EUR' ? '€' : '$'))}{userSubscription.amount || '0'} / {userSubscription.duration || 'period'} • {userSubscription.status?.toLowerCase() === 'active' ? 'Active Subscription' : 'Awaiting verification or activation'}
                                            </div>
                                            {userSubscription.status?.toLowerCase() === 'active' && userSubscription.planName?.toLowerCase() !== 'standard' && (
                                                <button 
                                                    onClick={() => generateInvoice(userSubscription)}
                                                    style={{ width: '100%', padding: '10px 16px', background: 'transparent', color: '#34d399', border: '1px solid rgba(52, 211, 153, 0.5)', borderRadius: '8px', cursor: 'pointer', fontWeight: '600', transition: 'all 0.2s ease', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
                                                    onMouseOver={(e) => { e.currentTarget.style.background = 'rgba(52, 211, 153, 0.1)'; e.currentTarget.style.borderColor = '#34d399'; }}
                                                    onMouseOut={(e) => { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.borderColor = 'rgba(52, 211, 153, 0.5)'; }}
                                                >
                                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
                                                    Download Invoice (PDF)
                                                </button>
                                            )}
                                        </div>
                                    ) : (
                                        <div style={{ color: 'var(--text-muted)', marginBottom: '16px' }}>No active subscriptions found.</div>
                                    )}
                                    
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                                        <h4 style={{ margin: '0 0 8px 0', color: 'var(--text-main)', fontSize: '15px' }}>Available Plans</h4>
                                        {availableSubscriptions.map(plan => (
                                            <div key={plan.id} style={{ background: 'var(--dash-card)', border: '1px solid var(--dash-border)', padding: '16px', borderRadius: '12px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                                <div>
                                                    <div style={{ fontWeight: 'bold', color: 'var(--text-main)', fontSize: '15px' }}>{plan.title}</div>
                                                    <div style={{ color: 'var(--text-muted)', fontSize: '13px', marginTop: '4px' }}>
                                                        {plan.currency === 'INR' ? '₹' : '$'}{plan.amount} / {plan.duration}
                                                        {plan.trialEnabled ? ` • ${plan.trialDays} Days Trial` : ''}
                                                    </div>
                                                </div>
                                                <button 
                                                    onClick={() => setCheckoutPlan(plan)}
                                                    style={{ padding: '8px 16px', background: 'linear-gradient(135deg, #34d399, #10b981)', color: '#000', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: 'bold', transition: 'transform 0.2s', boxShadow: '0 4px 15px rgba(52, 211, 153, 0.3)' }}
                                                    onMouseOver={(e) => e.currentTarget.style.transform = 'scale(1.05)'}
                                                    onMouseOut={(e) => e.currentTarget.style.transform = 'scale(1)'}
                                                >
                                                    Subscribe
                                                </button>
                                            </div>
                                        ))}
                                        {availableSubscriptions.length === 0 && (
                                            <div style={{ color: 'var(--dash-text-muted)', fontSize: '13px' }}>No plans available currently.</div>
                                        )}
                                    </div>
                                </div>
                            )}

                            {activeSettingsModal === 'contactUs' && (
                                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                                    {pendingGrievance ? (
                                        <div style={{ background: theme === 'dark' ? 'rgba(52, 211, 153, 0.1)' : 'rgba(52, 211, 153, 0.2)', padding: '24px', borderRadius: '16px', border: '1px solid rgba(52, 211, 153, 0.3)', textAlign: 'center' }}>
                                            <div style={{ color: '#34d399', fontSize: '16px', fontWeight: 'bold', marginBottom: '8px' }}>Your request is In Progress ⏳</div>
                                            <p style={{ margin: 0, color: 'var(--dash-text)', fontSize: '14px', lineHeight: '1.5' }}>
                                                You already have an active ticket open (<strong>{(pendingGrievance.actionType || pendingGrievance.type || 'request').replace('_', ' ').toUpperCase()}</strong>).<br/>
                                                Our support team will resolve it within 48 hours. You can submit another request once this one is closed.
                                            </p>
                                        </div>
                                    ) : (
                                        <>
                                            <p style={{ margin: 0, color: 'var(--dash-text)' }}>We are here to help. Describe your issue or grievance below, and our support team will get back to you.</p>
                                            <textarea 
                                                value={contactUsMessage} 
                                                onChange={(e) => setContactUsMessage(e.target.value)} 
                                                placeholder="Type your message here..." 
                                                style={{ width: '100%', padding: '16px', borderRadius: '12px', background: theme === 'dark' ? 'rgba(0,0,0,0.2)' : 'rgba(255,255,255,0.5)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none', transition: 'all 0.2s', resize: 'vertical', minHeight: '120px', fontSize: '14px', fontFamily: 'inherit' }} 
                                                onFocus={(e) => { e.target.style.borderColor = '#34d399'; e.target.style.boxShadow = '0 0 0 3px rgba(52, 211, 153, 0.1)'; }} 
                                                onBlur={(e) => { e.target.style.borderColor = 'var(--dash-border)'; e.target.style.boxShadow = 'none'; }}
                                            ></textarea>
                                            <button 
                                                onClick={() => handleSubmitDangerAction('contact_us')} 
                                                style={{ width: '100%', padding: '14px', borderRadius: '14px', background: 'linear-gradient(135deg, #34d399, #10b981)', color: '#000', border: 'none', cursor: 'pointer', fontWeight: '700', boxShadow: '0 6px 20px rgba(52, 211, 153, 0.3)', transition: 'all 0.3s ease' }} 
                                                onMouseOver={(e) => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 10px 25px rgba(52, 211, 153, 0.45)'; }} 
                                                onMouseOut={(e) => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 6px 20px rgba(52, 211, 153, 0.3)'; }}
                                            >
                                                Submit Ticket
                                            </button>
                                        </>
                                    )}
                                </div>
                            )}
                        </div>

                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', alignItems: 'center', paddingTop: '16px', borderTop: '1px solid var(--dash-border)', flexShrink: 0 }}>
                            <button className="btn-secondary" style={{ width: 'auto', padding: '8px 20px', fontSize: '13px', borderRadius: '10px', background: 'transparent', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', cursor: 'pointer', transition: 'all 0.2s ease', fontWeight: '600' }} onMouseOver={(e) => { e.currentTarget.style.background = 'rgba(255, 255, 255, 0.05)'; }} onMouseOut={(e) => { e.currentTarget.style.background = 'transparent'; }} onClick={() => setActiveSettingsModal(null)}>Close</button>
                            {(activeSettingsModal === 'editProfile' || activeSettingsModal === 'enable2FA') && (
                                <button className="btn-primary" style={{ width: 'auto', padding: '8px 20px', fontSize: '13px', borderRadius: '10px', background: 'linear-gradient(135deg, #34d399, #10b981)', color: '#022c22', border: 'none', cursor: isSavingProfile ? 'wait' : 'pointer', fontWeight: '700', boxShadow: '0 4px 15px rgba(52, 211, 153, 0.3)', transition: 'all 0.2s ease', opacity: isSavingProfile ? 0.7 : 1 }} onMouseOver={(e) => { if(!isSavingProfile) { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 6px 20px rgba(52, 211, 153, 0.4)'; } }} onMouseOut={(e) => { if(!isSavingProfile) { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 4px 15px rgba(52, 211, 153, 0.3)'; } }} onClick={activeSettingsModal === 'editProfile' ? handleSaveProfile : () => setActiveSettingsModal(null)} disabled={isSavingProfile}>{isSavingProfile ? 'Saving...' : 'Save Changes'}</button>
                            )}
                        </div>
                    </div>
                </div>
            )}

            {/* Checkout Modal */}
            {checkoutPlan && (
                <div style={{ position: 'fixed', inset: 0, zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(8px)', animation: 'fadeIn 0.3s ease' }} onClick={() => { setCheckoutPlan(null); setAppliedCoupon(null); setCouponCode(''); setCouponError(''); setCouponSuccess(''); }}>
                    <div style={{ background: 'linear-gradient(145deg, rgba(30, 41, 59, 0.9), rgba(15, 23, 42, 0.9))', padding: '32px', borderRadius: '24px', width: '90%', maxWidth: '420px', border: '1px solid rgba(255,255,255,0.1)', boxShadow: '0 30px 60px rgba(0,0,0,0.4), inset 0 1px 0 rgba(255,255,255,0.1)' }} onClick={(e) => e.stopPropagation()}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '24px' }}>
                            <div>
                                <h3 style={{ margin: 0, fontSize: '24px', fontWeight: '800', color: '#fff', letterSpacing: '-0.5px' }}>Complete Purchase</h3>
                                <p style={{ margin: '6px 0 0', color: '#94a3b8', fontSize: '14px' }}>Secure checkout via Razorpay</p>
                            </div>
                            <button onClick={() => { setCheckoutPlan(null); setAppliedCoupon(null); setCouponCode(''); setCouponError(''); setCouponSuccess(''); }} style={{ background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', color: '#94a3b8', cursor: 'pointer', fontSize: '18px', width: '32px', height: '32px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', transition: 'all 0.2s' }} onMouseOver={(e) => { e.currentTarget.style.background = 'rgba(255,255,255,0.1)'; e.currentTarget.style.color = '#fff'; }} onMouseOut={(e) => { e.currentTarget.style.background = 'rgba(255,255,255,0.05)'; e.currentTarget.style.color = '#94a3b8'; }}>&times;</button>
                        </div>
                        
                        <div style={{ background: 'rgba(15, 23, 42, 0.5)', padding: '20px', borderRadius: '16px', border: '1px solid rgba(255, 255, 255, 0.05)', marginBottom: '24px' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', paddingBottom: '16px', borderBottom: '1px dashed rgba(255,255,255,0.1)' }}>
                                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                                    <span style={{ color: '#f8fafc', fontWeight: '700', fontSize: '16px' }}>{checkoutPlan.title}</span>
                                    <span style={{ color: '#94a3b8', fontSize: '13px' }}>Billed {checkoutPlan.duration.toLowerCase()}</span>
                                </div>
                                <span style={{ fontWeight: '800', color: '#fff', fontSize: '18px' }}>{checkoutPlan.currency === 'INR' ? '₹' : '$'}{checkoutPlan.amount}</span>
                            </div>
                            
                            <div>
                                <label style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', fontWeight: '700', color: '#94a3b8', marginBottom: '10px', letterSpacing: '0.5px', textTransform: 'uppercase' }}>
                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"></path><line x1="7" y1="7" x2="7.01" y2="7"></line></svg>
                                    Discount Code
                                </label>
                                <div style={{ display: 'flex', gap: '8px' }}>
                                    <input
                                        type="text"
                                        value={couponCode}
                                        onChange={(e) => setCouponCode(e.target.value)}
                                        placeholder="Enter code"
                                        disabled={!!appliedCoupon}
                                        style={{ flex: 1, padding: '12px 14px', borderRadius: '10px', background: 'rgba(0,0,0,0.2)', color: '#fff', border: '1px solid rgba(255,255,255,0.1)', outline: 'none', textTransform: 'uppercase', fontSize: '14px', fontWeight: '600', transition: 'border 0.2s' }}
                                        onFocus={(e) => e.target.style.borderColor = 'rgba(52, 211, 153, 0.5)'}
                                        onBlur={(e) => e.target.style.borderColor = 'rgba(255,255,255,0.1)'}
                                    />
                                    {!appliedCoupon ? (
                                        <button onClick={applyCoupon} style={{ padding: '0 20px', background: 'rgba(255,255,255,0.05)', color: '#fff', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '10px', cursor: 'pointer', fontWeight: '600', fontSize: '14px', transition: 'all 0.2s' }} onMouseOver={(e) => e.currentTarget.style.background = 'rgba(255,255,255,0.1)'} onMouseOut={(e) => e.currentTarget.style.background = 'rgba(255,255,255,0.05)'}>Apply</button>
                                    ) : (
                                        <button onClick={() => { setAppliedCoupon(null); setCouponCode(''); setCouponSuccess(''); }} style={{ padding: '0 20px', background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444', border: '1px solid rgba(239, 68, 68, 0.2)', borderRadius: '10px', cursor: 'pointer', fontWeight: '600', fontSize: '14px', transition: 'all 0.2s' }} onMouseOver={(e) => e.currentTarget.style.background = 'rgba(239, 68, 68, 0.15)'} onMouseOut={(e) => e.currentTarget.style.background = 'rgba(239, 68, 68, 0.1)'}>Remove</button>
                                    )}
                                </div>
                                {couponError && <div style={{ color: '#ef4444', fontSize: '13px', marginTop: '8px', fontWeight: '500', display: 'flex', alignItems: 'center', gap: '4px' }}><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>{couponError}</div>}
                                {couponSuccess && <div style={{ color: '#34d399', fontSize: '13px', marginTop: '8px', fontWeight: '500', display: 'flex', alignItems: 'center', gap: '4px' }}><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>{couponSuccess}</div>}
                            </div>
                        </div>

                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px', padding: '0 8px' }}>
                            <span style={{ fontWeight: '600', color: '#94a3b8', fontSize: '16px' }}>Total to Pay</span>
                            <div style={{ display: 'flex', alignItems: 'baseline', gap: '4px' }}>
                                <span style={{ color: '#34d399', fontSize: '20px', fontWeight: '800' }}>{checkoutPlan.currency === 'INR' ? '₹' : '$'}</span>
                                <span style={{ fontWeight: '900', fontSize: '36px', color: '#34d399', letterSpacing: '-1px' }}>{calculateDiscountedAmount()}</span>
                            </div>
                        </div>

                        <button
                            style={{ width: '100%', padding: '16px', borderRadius: '14px', background: 'linear-gradient(135deg, #10b981, #059669)', color: '#fff', border: '1px solid rgba(255,255,255,0.2)', fontWeight: '800', fontSize: '16px', letterSpacing: '0.5px', cursor: 'pointer', boxShadow: '0 8px 25px rgba(16, 185, 129, 0.4), inset 0 1px 1px rgba(255,255,255,0.3)', transition: 'all 0.2s ease', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px' }}
                            onMouseOver={(e) => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 12px 30px rgba(16, 185, 129, 0.5), inset 0 1px 1px rgba(255,255,255,0.3)'; }}
                            onMouseOut={(e) => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 8px 25px rgba(16, 185, 129, 0.4), inset 0 1px 1px rgba(255,255,255,0.3)'; }}
                            onClick={() => {
                                const finalAmount = calculateDiscountedAmount();
                                const discountedPlan = { ...checkoutPlan, amount: finalAmount };
                                handleSubscribe(discountedPlan);
                                setCheckoutPlan(null);
                            }}
                        >
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
                            Proceed to Secure Payment
                        </button>
                        
                        <div style={{ textAlign: 'center', marginTop: '16px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px', color: '#64748b', fontSize: '12px', fontWeight: '500' }}>
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>
                            Secured by Razorpay • 256-bit Encryption
                        </div>
                    </div>
                </div>
            )}

            {/* Slim Vertical Sidebar */}
            <div style={{
                width: '100px', background: 'var(--dash-sidebar)', borderRight: '1px solid var(--dash-border)',
                display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '24px 0', zIndex: 10
            }}>
                {/* Logo Area */}
                <div style={{ marginBottom: '40px', display: 'flex', flexDirection: 'row', justifyContent: 'center', alignItems: 'flex-end', gap: '4px', width: '100%' }}>
                    <img
                        src="/assets/logo.png"
                        alt="SmartLedger Logo"
                        style={{
                            width: '32px',
                            height: '32px',
                            objectFit: 'cover',
                            borderRadius: '8px',
                            border: '1.5px solid black',
                            filter: 'drop-shadow(0 4px 12px rgba(52,211,153,0.2))'
                        }}
                    />
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#34d399" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" style={{ filter: 'drop-shadow(0 0 4px rgba(52,211,153,0.5))', marginBottom: '2px' }}>
                        <path d="M 4 10 L 9 18 L 14 6 C 17 3 21 5 21 9 C 21 14 12 18 12 18 L 21 18" />
                    </svg>
                </div>

                {/* Nav Links */}
                <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '5px', width: '100%', overflowY: 'auto' }}>
                    {MODULES.filter(m => m.id !== 'bankaccounts' && m.id !== 'cards' && m.id !== 'goldsilverinvestments' && m.id !== 'mutualfunds' && m.id !== 'taxprofiles').map(m => (
                        <div
                            key={m.id}
                            onClick={() => setActiveModule(m)}
                            className={`nav-item ${activeModule.id === m.id ? 'active' : ''}`}
                        >
                            <div className="icon-circle">
                                {m.icon ? <m.icon className="icon-svg" size={20} strokeWidth={1.5} /> : (m.isSummary ? '⊞' : '📄')}
                            </div>
                            <span className="nav-label">{m.label.replace(' Dashboard', '')}</span>
                        </div>
                    ))}
                </div>

                {/* Ledger AI - Modern Glassmorphic AI Hub Control */}
                <div 
                    onClick={() => {
                        setActiveModule({ id: 'aica_import', label: 'Ledger AI', icon: null, isSummary: false });
                    }}
                    className="ai-sidebar-hub"
                    style={{ 
                        marginTop: 'auto', width: '100%', padding: '0 8px 24px 8px', 
                        display: 'flex', flexDirection: 'column', alignItems: 'center', 
                        justifyContent: 'center', gap: '10px', cursor: 'pointer' 
                    }}
                    title="Launch Ledger AI Assistant"
                >
                    <style>
                        {`
                        @keyframes aiHubPulseGlow {
                            0% { box-shadow: 0 0 0 0 rgba(52, 211, 153, 0.45), 0 0 20px rgba(16, 185, 129, 0.3); }
                            50% { box-shadow: 0 0 0 10px rgba(52, 211, 153, 0), 0 0 32px rgba(6, 182, 212, 0.5); }
                            100% { box-shadow: 0 0 0 0 rgba(52, 211, 153, 0), 0 0 20px rgba(16, 185, 129, 0.3); }
                        }
                        @keyframes aiRingSpin {
                            0% { transform: rotate(0deg); }
                            100% { transform: rotate(360deg); }
                        }
                        @keyframes aiPingDot {
                            0%, 100% { transform: scale(1); opacity: 0.9; }
                            50% { transform: scale(1.6); opacity: 0; }
                        }
                        .ai-sidebar-hub {
                            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                        }
                        .ai-sidebar-hub:hover .ai-orb-button {
                            transform: translateY(-3px) scale(1.08);
                            border-color: #34d399;
                            box-shadow: 0 0 35px rgba(16, 185, 129, 0.6), 0 0 18px rgba(6, 182, 212, 0.4), inset 0 1px 3px rgba(255, 255, 255, 0.6);
                        }
                        .ai-sidebar-hub:hover .ai-sidebar-pill {
                            background: linear-gradient(135deg, rgba(16, 185, 129, 0.25), rgba(6, 182, 212, 0.18));
                            border-color: rgba(52, 211, 153, 0.6);
                            box-shadow: 0 4px 16px rgba(16, 185, 129, 0.25);
                            transform: translateY(-1px);
                        }
                        .ai-orb-button {
                            animation: aiHubPulseGlow 3s infinite ease-in-out;
                            transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
                        }
                        `}
                    </style>
                    
                    {/* Futuristic Glassmorphic AI Orb */}
                    <div style={{ position: 'relative', width: '54px', height: '54px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                        {/* Rotating Ambient Gradient Ring */}
                        <div style={{
                            position: 'absolute', top: '-3px', left: '-3px', right: '-3px', bottom: '-3px',
                            borderRadius: '50%',
                            background: 'conic-gradient(from 0deg, #10b981, #06b6d4, #6366f1, #10b981)',
                            opacity: activeModule.id === 'aica_import' ? 0.85 : 0.4,
                            filter: 'blur(4px)',
                            animation: 'aiRingSpin 8s linear infinite',
                            transition: 'opacity 0.3s ease'
                        }}></div>

                        <div
                            className="ai-orb-button"
                            style={{
                                background: activeModule.id === 'aica_import'
                                    ? 'radial-gradient(circle at 30% 30%, rgba(16, 185, 129, 0.35), rgba(6, 182, 212, 0.25), rgba(15, 23, 42, 0.8))'
                                    : 'radial-gradient(circle at 30% 30%, rgba(52, 211, 153, 0.2), rgba(6, 182, 212, 0.12), var(--dash-glass-bg))',
                                backdropFilter: 'blur(14px)',
                                WebkitBackdropFilter: 'blur(14px)',
                                border: activeModule.id === 'aica_import' ? '2px solid #34d399' : '1.5px solid rgba(52, 211, 153, 0.45)',
                                padding: '4px',
                                borderRadius: '50%',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                width: '54px',
                                height: '54px',
                                position: 'relative',
                                zIndex: 2
                            }}
                        >
                            <img 
                                src="/assets/logo.png" 
                                alt="AI" 
                                className="ai-logo-img"
                                style={{ 
                                    width: '100%', height: '100%', objectFit: 'cover', borderRadius: '50%',
                                    filter: activeModule.id === 'aica_import' ? 'drop-shadow(0 0 10px rgba(52,211,153,0.9))' : 'drop-shadow(0 0 5px rgba(52,211,153,0.4))',
                                    transition: 'all 0.3s ease'
                                }} 
                            />
                            {/* Live AI Status Pulse Dot */}
                            <div style={{ position: 'absolute', top: '1px', right: '1px', width: '13px', height: '13px', zIndex: 5 }}>
                                <span style={{ position: 'absolute', top: 0, right: 0, width: '100%', height: '100%', borderRadius: '50%', background: '#10b981', animation: 'aiPingDot 2s cubic-bezier(0, 0, 0.2, 1) infinite' }}></span>
                                <span style={{ position: 'absolute', top: 0, right: 0, width: '13px', height: '13px', borderRadius: '50%', background: '#10b981', border: '2px solid var(--dash-sidebar)', boxShadow: '0 0 8px #10b981' }}></span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Main Content Area */}
            <div style={{ flex: 1, padding: activeModule.id === 'aica_import' ? '0' : '24px 64px', overflowY: activeModule.id === 'aica_import' ? 'hidden' : 'auto', background: 'transparent', display: 'flex', flexDirection: 'column', minHeight: 0, position: 'relative', zIndex: 1 }}>
                
                {activeModule.id === 'aica_import' && (
                    <div style={{ position: 'absolute', top: '9px', right: '32px', zIndex: 100 }}>
                        <button onClick={toggleTheme} style={{
                            width: '38px', height: '38px', borderRadius: '50%',
                            background: 'var(--dash-glass-bg)', backdropFilter: 'blur(12px)', WebkitBackdropFilter: 'blur(12px)', border: '1px solid var(--dash-border)',
                            color: 'var(--dash-text)', cursor: 'pointer', display: 'flex',
                            alignItems: 'center', justifyContent: 'center', fontSize: '18px',
                            boxShadow: '0 4px 15px rgba(0,0,0,0.05)', transition: 'all 0.3s ease'
                        }} onMouseOver={(e) => { e.currentTarget.style.borderColor = 'rgba(52, 211, 153, 0.4)'; e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 8px 25px rgba(0,0,0,0.1)'; }} onMouseOut={(e) => { e.currentTarget.style.borderColor = 'var(--dash-border)'; e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 4px 15px rgba(0,0,0,0.05)'; }} title="Toggle Theme">
                            {theme === 'dark' ? <Sun size={20} /> : <Moon size={20} />}
                        </button>
                    </div>
                )}

                {activeModule.id !== 'aica_import' && (
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '40px', flexShrink: 0 }}>
                        <div>
                            <h2 style={{ fontSize: '28px', fontWeight: '600', color: 'var(--dash-text)', margin: '0 0 4px 0', lineHeight: '1.2', display: 'flex', alignItems: 'center', gap: '8px' }}>
                                {getGreeting()}, {userName} <span style={{ fontSize: '24px' }}>👋</span>
                            </h2>
                            <p style={{ color: 'var(--dash-text-muted)', fontSize: '14px', margin: '0', display: 'flex', gap: '8px', alignItems: 'center' }}>
                                <span style={{ fontWeight: '500' }}>{currentTime.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit' })}</span>
                                <span>•</span>
                                <span>{currentTime.toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</span>
                            </p>
                        </div>

                    <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>

                        <button onClick={toggleTheme} style={{
                            width: '38px', height: '38px', borderRadius: '50%',
                            background: 'var(--dash-glass-bg)', backdropFilter: 'blur(12px)', WebkitBackdropFilter: 'blur(12px)', border: '1px solid var(--dash-border)',
                            color: 'var(--dash-text)', cursor: 'pointer', display: 'flex',
                            alignItems: 'center', justifyContent: 'center', fontSize: '18px',
                            boxShadow: '0 4px 15px rgba(0,0,0,0.05)', transition: 'all 0.3s ease'
                        }} onMouseOver={(e) => { e.currentTarget.style.borderColor = 'rgba(52, 211, 153, 0.4)'; e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 8px 25px rgba(0,0,0,0.1)'; }} onMouseOut={(e) => { e.currentTarget.style.borderColor = 'var(--dash-border)'; e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 4px 15px rgba(0,0,0,0.05)'; }} title="Toggle Theme">
                            {theme === 'dark' ? <Sun size={20} /> : <Moon size={20} />}
                        </button>

                        <div
                            style={{ position: 'relative' }}
                            onMouseEnter={() => setShowNotificationDropdown(true)}
                            onMouseLeave={() => setShowNotificationDropdown(false)}
                        >
                            <button style={{
                                width: '38px', height: '38px', borderRadius: '50%', position: 'relative',
                                background: 'var(--dash-glass-bg)', backdropFilter: 'blur(12px)', WebkitBackdropFilter: 'blur(12px)', border: '1px solid var(--dash-border)',
                                color: 'var(--dash-text)', cursor: 'pointer', display: 'flex',
                                alignItems: 'center', justifyContent: 'center', fontSize: '18px',
                                boxShadow: '0 4px 15px rgba(0,0,0,0.05)', transition: 'all 0.3s ease'
                            }} onMouseOver={(e) => { e.currentTarget.style.borderColor = 'rgba(52, 211, 153, 0.4)'; e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 8px 25px rgba(0,0,0,0.1)'; }} onMouseOut={(e) => { e.currentTarget.style.borderColor = 'var(--dash-border)'; e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 4px 15px rgba(0,0,0,0.05)'; }} title="Notifications">
                                <Bell size={20} />
                                <div style={{ position: 'absolute', top: '10px', right: '12px', width: '8px', height: '8px', background: '#ef4444', borderRadius: '50%', border: '2px solid var(--dash-glass-bg)' }}></div>
                            </button>

                            {showNotificationDropdown && (
                                <div style={{ position: 'absolute', top: '100%', right: '50%', transform: 'translateX(50%)', paddingTop: '8px', zIndex: 100 }}>
                                    <div style={{
                                        background: 'var(--dash-glass-bg)', backdropFilter: 'blur(12px)', WebkitBackdropFilter: 'blur(12px)', border: '1px solid var(--dash-border)',
                                        borderRadius: '16px', boxShadow: '0 10px 40px rgba(0,0,0,0.2)',
                                        padding: '16px', width: '300px',
                                        display: 'flex', flexDirection: 'column', gap: '12px',
                                        animation: 'slideUp 0.2s ease forwards'
                                    }}>
                                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid var(--dash-border)', paddingBottom: '8px' }}>
                                            <span style={{ fontWeight: '600', color: 'var(--dash-text)', fontSize: '14px' }}>Notifications</span>
                                            <span style={{ fontSize: '12px', color: '#34d399', cursor: 'pointer', fontWeight: '500' }}>Mark all read</span>
                                        </div>

                                        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', maxHeight: '300px', overflowY: 'auto' }}>
                                            {[
                                                { title: 'Security Alert', desc: 'New login detected from Chrome on Windows.', time: 'Just now', unread: true },
                                                { title: 'Goal Progress', desc: 'You have reached 65% of your savings goal.', time: '2h ago', unread: false },
                                                { title: 'System Update', desc: 'SmartLedger platform has been updated successfully.', time: '1d ago', unread: false }
                                            ].map((notif, idx) => (
                                                <div key={idx} style={{ padding: '12px', borderRadius: '12px', background: notif.unread ? 'rgba(52, 211, 153, 0.1)' : 'transparent', border: notif.unread ? '1px solid rgba(52, 211, 153, 0.2)' : '1px solid transparent', cursor: 'pointer', transition: 'all 0.2s' }} onMouseOver={(e) => { if (!notif.unread) e.currentTarget.style.background = 'rgba(255, 255, 255, 0.05)'; }} onMouseOut={(e) => { if (!notif.unread) e.currentTarget.style.background = 'transparent'; }}>
                                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
                                                        <span style={{ fontSize: '13px', fontWeight: notif.unread ? '600' : '500', color: 'var(--dash-text)' }}>{notif.title}</span>
                                                        <span style={{ fontSize: '11px', color: 'var(--dash-text-muted)' }}>{notif.time}</span>
                                                    </div>
                                                    <div style={{ fontSize: '12px', color: 'var(--dash-text-muted)', lineHeight: '1.4' }}>
                                                        {notif.desc}
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>

                        <div
                            style={{ position: 'relative' }}
                            onMouseEnter={() => setShowProfileDropdown(true)}
                            onMouseLeave={() => setShowProfileDropdown(false)}
                        >
                            <button style={{
                                width: '38px', height: '38px', borderRadius: '50%',
                                background: 'var(--dash-glass-bg)', backdropFilter: 'blur(12px)', WebkitBackdropFilter: 'blur(12px)', border: '1px solid var(--dash-border)',
                                color: 'var(--dash-text)', cursor: 'pointer', display: 'flex',
                                alignItems: 'center', justifyContent: 'center',
                                boxShadow: '0 4px 15px rgba(0,0,0,0.05)', transition: 'all 0.3s ease'
                            }} onMouseOver={(e) => { e.currentTarget.style.borderColor = 'rgba(52, 211, 153, 0.4)'; e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 8px 25px rgba(0,0,0,0.1)'; }} onMouseOut={(e) => { e.currentTarget.style.borderColor = 'var(--dash-border)'; e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 4px 15px rgba(0,0,0,0.05)'; }} title="Settings">
                                <Settings size={20} />
                            </button>

                            {showProfileDropdown && (
                                <div style={{ position: 'absolute', top: '100%', right: 0, paddingTop: '8px', zIndex: 100 }}>
                                    <div style={{
                                        background: 'var(--dash-glass-bg)', backdropFilter: 'blur(12px)', WebkitBackdropFilter: 'blur(12px)', border: '1px solid var(--dash-border)',
                                        borderRadius: '16px', boxShadow: '0 10px 40px rgba(0,0,0,0.2)',
                                        padding: '8px', minWidth: '240px',
                                        display: 'flex', flexDirection: 'column', gap: '4px',
                                        animation: 'slideUp 0.2s ease forwards'
                                    }}>
                                        {[
                                            { id: 'editProfile', label: 'Edit Profile', icon: <User size={16} /> },
                                            { id: 'privacyPolicy', label: 'Privacy Policy', icon: <Shield size={16} /> },
                                            { id: 'enable2FA', label: 'Enable 2FA', icon: <Lock size={16} /> },
                                            { id: 'subscriptions', label: 'Subscriptions', icon: <CreditCard size={16} /> },
                                            { id: 'contactUs', label: 'Contact Us', icon: <MessageSquare size={16} /> }
                                        ].map((item, idx) => (
                                            <button key={idx} onClick={() => { setActiveSettingsModal(item.id); setShowProfileDropdown(false); }} style={{
                                                display: 'flex', alignItems: 'center', gap: '12px',
                                                width: '100%', padding: '10px 16px', background: 'transparent',
                                                border: 'none', color: 'var(--dash-text)', fontSize: '14px',
                                                fontWeight: '500', cursor: 'pointer', borderRadius: '12px',
                                                textAlign: 'left', transition: 'all 0.2s'
                                            }} onMouseOver={(e) => { e.currentTarget.style.background = 'rgba(52, 211, 153, 0.1)'; e.currentTarget.style.color = '#34d399'; }} onMouseOut={(e) => { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.color = 'var(--dash-text)'; }}>
                                                {item.icon}
                                                {item.label}
                                            </button>
                                        ))}
                                    </div>
                                </div>
                            )}
                        </div>

                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', background: 'var(--dash-glass-bg)', backdropFilter: 'blur(12px)', WebkitBackdropFilter: 'blur(12px)', padding: '6px 16px 6px 6px', borderRadius: '30px', border: '1px solid var(--dash-border)', boxShadow: '0 4px 15px rgba(0,0,0,0.05)', transition: 'all 0.3s ease' }} onMouseOver={(e) => { e.currentTarget.style.borderColor = 'rgba(52, 211, 153, 0.4)'; e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 8px 25px rgba(0,0,0,0.1)'; }} onMouseOut={(e) => { e.currentTarget.style.borderColor = 'var(--dash-border)'; e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 4px 15px rgba(0,0,0,0.05)'; }}>
                            <div style={{ width: '32px', height: '32px', borderRadius: '50%', background: '#34d399', color: '#000', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold', overflow: 'hidden', cursor: 'pointer' }} onClick={() => document.getElementById('profile-upload').click()}>
                                <input type="file" id="profile-upload" hidden onChange={handleFileSelect} accept="image/*" />
                                {profilePicture ? (
                                    <img src={profilePicture} alt="Profile" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                                ) : (
                                    userName.charAt(0).toUpperCase()
                                )}
                            </div>
                            <div style={{ display: 'flex', flexDirection: 'column', paddingRight: '4px' }}>
                                <span style={{ color: 'var(--dash-text)', fontSize: '14px', fontWeight: '500', lineHeight: '1.2' }}>{userName}</span>
                                <span style={{ color: 'var(--dash-text-muted)', fontSize: '11px', fontWeight: 'bold' }}>ID: {userUid}</span>
                            </div>
                            <button onClick={onLogout} style={{ background: 'transparent', border: 'none', color: '#ef4444', cursor: 'pointer', marginLeft: '4px', display: 'flex', alignItems: 'center', transition: 'all 0.2s', padding: '4px', borderRadius: '50%' }} onMouseOver={(e) => { e.currentTarget.style.color = '#dc2626'; e.currentTarget.style.background = 'rgba(239, 68, 68, 0.1)'; }} onMouseOut={(e) => { e.currentTarget.style.color = '#ef4444'; e.currentTarget.style.background = 'transparent'; }} title="Logout">
                                <LogOut size={16} />
                            </button>
                        </div>
                    </div>
                </div>
                )}

                <div style={{
                    width: '100%',
                    ...(activeModule.id === 'aica_import' ? { flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column', overflow: 'hidden' } : {})
                }}>
                    {(() => {
                        const isPremiumModule = ['aica_import', 'investments', 'goldsilverinvestments', 'mutualfunds'].includes(activeModule.id);
                        const isSubscribed = userSubscription?.status?.toLowerCase() === 'active';

                        if (isPremiumModule && !isSubscribed) {
                            return (
                                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100%', padding: '40px', textAlign: 'center', animation: 'fadeIn 0.4s ease-out', flex: 1 }}>
                                    <div style={{ background: 'var(--dash-glass-bg)', backdropFilter: 'blur(16px)', WebkitBackdropFilter: 'blur(16px)', border: '1px solid rgba(52, 211, 153, 0.3)', borderRadius: '24px', padding: '48px', maxWidth: '500px', width: '100%', boxShadow: '0 20px 40px rgba(0,0,0,0.2), inset 0 1px 1px rgba(255,255,255,0.1)' }}>
                                        <div style={{ width: '80px', height: '80px', borderRadius: '50%', background: 'rgba(52, 211, 153, 0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 24px', boxShadow: '0 0 30px rgba(52, 211, 153, 0.2)' }}>
                                            <Lock size={40} color="#34d399" />
                                        </div>
                                        <h2 style={{ fontSize: '28px', fontWeight: '800', color: 'var(--dash-text)', marginBottom: '16px', letterSpacing: '-0.5px' }}>Premium Feature Locked</h2>
                                        <p style={{ color: 'var(--dash-text-muted)', fontSize: '15px', lineHeight: '1.6', marginBottom: '32px' }}>
                                            Unlock the full potential of SmartLedger! Get exclusive access to AI-powered insights, advanced investment tracking, and much more. Elevate your financial journey today.
                                        </p>
                                        <button 
                                            onClick={() => {
                                                setActiveSettingsModal('subscriptions');
                                            }} 
                                            style={{ 
                                                padding: '16px 32px', 
                                                borderRadius: '12px', 
                                                background: 'linear-gradient(135deg, #34d399, #10b981)', 
                                                color: '#022c22', 
                                                border: 'none', 
                                                fontWeight: '800', 
                                                fontSize: '16px', 
                                                cursor: 'pointer', 
                                                boxShadow: '0 10px 25px rgba(52, 211, 153, 0.4)', 
                                                transition: 'all 0.3s ease',
                                                display: 'flex',
                                                alignItems: 'center',
                                                justifyContent: 'center',
                                                gap: '8px',
                                                width: '100%'
                                            }}
                                            onMouseOver={(e) => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 15px 30px rgba(52, 211, 153, 0.5)'; }}
                                            onMouseOut={(e) => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 10px 25px rgba(52, 211, 153, 0.4)'; }}
                                        >
                                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>
                                            Subscribe Now
                                        </button>
                                    </div>
                                </div>
                            );
                        }

                        if (activeModule.isSummary) {
                            return <SummaryView onNavigate={(id) => setActiveModule(MODULES.find(m => m.id === id))} isSubscribed={isSubscribed} />;
                        } else if (activeModule.id === 'aica_import') {
                            return (
                                <AiVirtualCaImportView
                                    userUid={userUid}
                                    refreshTrigger={refreshTrigger}
                                    onTransactionsSaved={() => setRefreshTrigger(prev => prev + 1)}
                                />
                            );
                        } else {
                            return (
                                <ModuleView
                                    module={activeModule}
                                    refreshTrigger={refreshTrigger}
                                    userUid={userUid}
                                    isSubscribed={isSubscribed}
                                    onSubscribe={() => {
                                        setShowProfileDropdown(true);
                                        setActiveSettingsModal('subscriptions');
                                    }}
                                    onBack={() => setActiveModule(MODULES[0])}
                                    onAdd={() => { setFormData({}); setIsAddModalOpen(true); }}
                                    onEdit={(row) => {
                                        if (row._originalItem && row._endpoint === '/api/bankaccounts') {
                                            setFormData(row._originalItem);
                                            setActiveModule(MODULES.find(m => m.id === 'bankaccounts'));
                                        } else {
                                            setFormData(row._originalItem || row);
                                        }
                                        setIsAddModalOpen(true);
                                    }}
                                />
                            );
                        }
                    })()}
                </div>
            </div>

            {/* Cropper Modal */}
            {isCropping && (
                <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.8)', zIndex: 1000, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
                    <div style={{ position: 'relative', width: '400px', height: '400px', background: '#333', borderRadius: '12px', overflow: 'hidden' }}>
                        <Cropper
                            image={imageSrc}
                            crop={crop}
                            zoom={zoom}
                            aspect={1}
                            cropShape="round"
                            showGrid={false}
                            onCropChange={setCrop}
                            onCropComplete={onCropComplete}
                            onZoomChange={setZoom}
                        />
                    </div>
                    <div style={{ marginTop: '24px', display: 'flex', gap: '16px' }}>
                        <button onClick={() => { setIsCropping(false); setImageSrc(null); }} style={{ padding: '10px 24px', borderRadius: '8px', background: 'transparent', border: '1px solid #fff', color: '#fff', cursor: 'pointer', fontWeight: 'bold' }}>Cancel</button>
                        <button onClick={handleCropAndUpload} disabled={isUploading} style={{ padding: '10px 24px', borderRadius: '8px', background: '#34d399', border: 'none', color: '#000', fontWeight: 'bold', cursor: 'pointer' }}>
                            {isUploading ? 'Uploading...' : 'Crop & Upload'}
                        </button>
                    </div>
                </div>
            )}

            {/* AI CA Advisor & Statement Import Modal */}
            {false && (
                <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.75)', backdropFilter: 'blur(10px)', zIndex: 2000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '24px' }}>
                    <div className="glass-card" style={{ width: '900px', maxHeight: '88vh', background: 'var(--surface-dark)', border: '1px solid #34d399', borderRadius: '24px', display: 'flex', flexDirection: 'column', overflow: 'hidden', boxShadow: '0 0 50px rgba(52,211,153,0.25)' }}>
                        {/* Modal Header */}
                        <div style={{ padding: '24px 32px', borderBottom: '1px solid var(--border-dark)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'rgba(52, 211, 153, 0.05)' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                                <span style={{ fontSize: '28px' }}>✨</span>
                                <div>
                                    <h3 style={{ margin: 0, color: 'var(--text-main)', fontSize: '20px', fontWeight: '700' }}>SmartLedger AI Virtual CA & Statement Import</h3>
                                    <p style={{ margin: 0, color: 'var(--text-muted)', fontSize: '12px' }}>RAG-powered Financial Advisor & Multimodal Statement Parser ($0 Cost)</p>
                                </div>
                            </div>
                            <button onClick={() => { setIsAiModalOpen(false); setExtractedTransactions([]); setPdfPassword(''); setAiFile(null); }} style={{ background: 'transparent', border: 'none', color: 'var(--text-muted)', fontSize: '24px', cursor: 'pointer', lineHeight: 1 }}>&times;</button>
                        </div>

                        {/* Modal Tabs */}
                        <div style={{ display: 'flex', borderBottom: '1px solid var(--border-dark)', background: 'rgba(0,0,0,0.2)' }}>
                            <button
                                onClick={() => setAiTab('import')}
                                style={{
                                    flex: 1, padding: '16px', background: aiTab === 'import' ? 'rgba(52, 211, 153, 0.1)' : 'transparent',
                                    border: 'none', borderBottom: aiTab === 'import' ? '2px solid #34d399' : '2px solid transparent',
                                    color: aiTab === 'import' ? '#34d399' : 'var(--text-muted)', fontWeight: '600', cursor: 'pointer', transition: 'all 0.2s'
                                }}
                            >
                                📄 AI Statement Parser (PDF/CSV)
                            </button>
                            <button
                                onClick={() => setAiTab('advisor')}
                                style={{
                                    flex: 1, padding: '16px', background: aiTab === 'advisor' ? 'rgba(52, 211, 153, 0.1)' : 'transparent',
                                    border: 'none', borderBottom: aiTab === 'advisor' ? '2px solid #34d399' : '2px solid transparent',
                                    color: aiTab === 'advisor' ? '#34d399' : 'var(--text-muted)', fontWeight: '600', cursor: 'pointer', transition: 'all 0.2s'
                                }}
                            >
                                👨‍💼 Virtual Chartered Accountant (RAG Chat)
                            </button>
                        </div>

                        {/* Modal Body */}
                        <div style={{ padding: '24px 32px', overflowY: 'auto', flex: 1 }}>
                            {aiTab === 'import' ? (
                                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                                    <div style={{ background: 'rgba(255,255,255,0.02)', padding: '20px', borderRadius: '16px', border: '1px dashed var(--border-dark)', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '12px' }}>
                                        <p style={{ margin: 0, color: 'var(--text-main)', fontWeight: '500' }}>Upload your Bank Statement (PDF, CSV, or Excel)</p>
                                        <input
                                            type="file"
                                            accept=".pdf,.csv,.xlsx"
                                            onChange={(e) => setAiFile(e.target.files[0])}
                                            style={{ color: 'var(--text-muted)' }}
                                        />
                                        <input
                                            type="password"
                                            placeholder="🔒 PDF Password (if protected)"
                                            value={pdfPassword}
                                            onChange={(e) => setPdfPassword(e.target.value)}
                                            style={{
                                                width: '100%',
                                                maxWidth: '320px',
                                                padding: '10px 14px',
                                                borderRadius: '10px',
                                                background: 'var(--bg-dark)',
                                                color: 'var(--text-main)',
                                                border: '1px solid var(--border-dark)',
                                                outline: 'none',
                                                fontSize: '13px',
                                                textAlign: 'center'
                                            }}
                                        />
                                        <button
                                            onClick={handleAiParseStatement}
                                            disabled={aiLoading || !aiFile}
                                            style={{
                                                padding: '10px 24px', borderRadius: '10px', background: '#34d399', color: '#000',
                                                border: 'none', fontWeight: 'bold', cursor: aiLoading || !aiFile ? 'not-allowed' : 'pointer',
                                                opacity: aiLoading || !aiFile ? 0.6 : 1
                                            }}
                                        >
                                            {aiLoading ? '🤖 AI is analyzing statement...' : '🔍 Extract Transactions with AI'}
                                        </button>
                                    </div>

                                    {extractedTransactions.length > 0 && (
                                        <div>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                                                <h4 style={{ margin: 0, color: 'var(--text-main)', fontSize: '15px' }}>Extracted {extractedTransactions.length} Transactions</h4>
                                                <button
                                                    onClick={handleImportExtractedTransactions}
                                                    disabled={aiLoading}
                                                    style={{ padding: '8px 16px', borderRadius: '8px', background: 'linear-gradient(135deg, #10b981, #059669)', color: '#fff', border: 'none', fontWeight: 'bold', cursor: 'pointer' }}
                                                >
                                                    📥 Import All into SmartLedger
                                                </button>
                                            </div>
                                            <div style={{ maxHeight: '250px', overflowY: 'auto', border: '1px solid var(--border-dark)', borderRadius: '12px' }}>
                                                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
                                                    <thead>
                                                        <tr style={{ background: 'rgba(255,255,255,0.05)', color: 'var(--text-muted)' }}>
                                                            <th style={{ padding: '10px' }}>Date</th>
                                                            <th style={{ padding: '10px' }}>Ref ID / UTR</th>
                                                            <th style={{ padding: '10px' }}>Description</th>
                                                            <th style={{ padding: '10px' }}>Amount</th>
                                                            <th style={{ padding: '10px' }}>Type</th>
                                                            <th style={{ padding: '10px' }}>Category</th>
                                                            <th style={{ padding: '10px' }}>Tax Section</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        {extractedTransactions.map((t, idx) => (
                                                            <tr key={idx} style={{ borderTop: '1px solid rgba(255,255,255,0.05)' }}>
                                                                <td style={{ padding: '10px' }}>{t.date}</td>
                                                                <td style={{ padding: '10px', color: '#38bdf8', fontFamily: 'monospace' }}>{t.refId || '-'}</td>
                                                                <td style={{ padding: '10px' }}>{t.description}</td>
                                                                <td style={{ padding: '10px', fontWeight: 'bold' }}>₹{t.amount}</td>
                                                                <td style={{ padding: '10px', color: t.type === 'CREDIT' ? '#34d399' : '#ef4444' }}>{t.type}</td>
                                                                <td style={{ padding: '10px' }}>{t.category}</td>
                                                                <td style={{ padding: '10px', color: '#6366f1' }}>{t.taxSection || '-'}</td>
                                                            </tr>
                                                        ))}
                                                    </tbody>
                                                </table>
                                            </div>
                                        </div>
                                    )}
                                </div>
                            ) : (
                                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                                    <div style={{ display: 'flex', gap: '12px', background: 'rgba(255,255,255,0.02)', padding: '16px', borderRadius: '12px', border: '1px solid var(--border-dark)' }}>
                                        <div style={{ flex: 1 }}>
                                            <label style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Annual Income (₹)</label>
                                            <input type="number" value={caMetrics.annualIncome} onChange={(e) => setCaMetrics(p => ({ ...p, annualIncome: Number(e.target.value) }))} style={{ width: '100%', padding: '6px', borderRadius: '6px', background: 'var(--bg-dark)', color: 'var(--text-main)', border: '1px solid var(--border-dark)', marginTop: '4px' }} />
                                        </div>
                                        <div style={{ flex: 1 }}>
                                            <label style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Section 80C Invested (₹)</label>
                                            <input type="number" value={caMetrics.investments80C} onChange={(e) => setCaMetrics(p => ({ ...p, investments80C: Number(e.target.value) }))} style={{ width: '100%', padding: '6px', borderRadius: '6px', background: 'var(--bg-dark)', color: 'var(--text-main)', border: '1px solid var(--border-dark)', marginTop: '4px' }} />
                                        </div>
                                        <div style={{ flex: 1 }}>
                                            <label style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Section 80D Insurance (₹)</label>
                                            <input type="number" value={caMetrics.healthInsurance80D} onChange={(e) => setCaMetrics(p => ({ ...p, healthInsurance80D: Number(e.target.value) }))} style={{ width: '100%', padding: '6px', borderRadius: '6px', background: 'var(--bg-dark)', color: 'var(--text-main)', border: '1px solid var(--border-dark)', marginTop: '4px' }} />
                                        </div>
                                    </div>

                                    <form onSubmit={handleCaAdvisorQuery} style={{ display: 'flex', gap: '10px' }}>
                                        <input
                                            type="text"
                                            value={caQuery}
                                            onChange={(e) => setCaQuery(e.target.value)}
                                            placeholder="Ask your Virtual CA (e.g., 'How can I optimize my taxes under Section 80C?')"
                                            style={{ flex: 1, padding: '12px', borderRadius: '10px', background: 'var(--bg-dark)', color: 'var(--text-main)', border: '1px solid var(--border-dark)', outline: 'none' }}
                                        />
                                        <button
                                            type="submit"
                                            disabled={caLoading || !caQuery.trim()}
                                            style={{ padding: '10px 20px', borderRadius: '10px', background: '#34d399', color: '#000', border: 'none', fontWeight: 'bold', cursor: 'pointer' }}
                                        >
                                            {caLoading ? 'Analyzing...' : 'Ask CA'}
                                        </button>
                                    </form>

                                    {caResponse && (
                                        <div style={{ background: 'rgba(52, 211, 153, 0.05)', border: '1px solid rgba(52, 211, 153, 0.2)', padding: '20px', borderRadius: '16px', color: 'var(--text-main)', fontSize: '14px', lineHeight: '1.6', whiteSpace: 'pre-wrap', maxHeight: '300px', overflowY: 'auto' }}>
                                            <div style={{ fontWeight: 'bold', color: '#34d399', marginBottom: '8px' }}>👨‍💼 Virtual CA Advisory:</div>
                                            {caResponse}
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            )}

            {/* Custom Toast UI */}
            {toast.visible && (
                <div style={{
                    position: 'fixed',
                    bottom: '40px',
                    left: '50%',
                    transform: 'translateX(-50%)',
                    background: theme === 'dark' ? 'rgba(0,0,0,0.85)' : 'rgba(255,255,255,0.95)',
                    border: `1px solid ${toast.type === 'error' ? '#ef4444' : toast.type === 'warning' ? '#f59e0b' : '#34d399'}`,
                    color: 'var(--dash-text)',
                    padding: '14px 28px',
                    borderRadius: '50px',
                    boxShadow: `0 10px 40px ${toast.type === 'error' ? 'rgba(239,68,68,0.2)' : toast.type === 'warning' ? 'rgba(245,158,11,0.2)' : 'rgba(52,211,153,0.2)'}`,
                    zIndex: 10000,
                    display: 'flex',
                    alignItems: 'center',
                    gap: '12px',
                    fontSize: '14px',
                    fontWeight: '600',
                    animation: 'slideUp 0.3s ease forwards',
                    backdropFilter: 'blur(20px)',
                    WebkitBackdropFilter: 'blur(20px)'
                }}>
                    <div style={{
                        width: '10px', height: '10px', borderRadius: '50%',
                        background: toast.type === 'error' ? '#ef4444' : toast.type === 'warning' ? '#f59e0b' : '#34d399',
                        boxShadow: `0 0 10px ${toast.type === 'error' ? '#ef4444' : toast.type === 'warning' ? '#f59e0b' : '#34d399'}`
                    }}></div>
                    {toast.message}
                </div>
            )}
        </div>
    );
}
