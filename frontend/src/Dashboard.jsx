import React, { useState, useEffect } from 'react';
import { auth } from './firebase';

const MODULES = [
    { id: 'overview', label: 'Overview Dashboard', isSummary: true },
    {
        id: 'transactions', label: 'Transactions', endpoint: '/api/transactions',
        fields: [{ name: 'date', type: 'date' }, { name: 'description', type: 'text' }, { name: 'amount', type: 'number' }, { name: 'type', type: 'select', options: ['Income', 'Expense'] }]
    },
    {
        id: 'bankaccounts', label: 'Bank Accounts', endpoint: '/api/bankaccounts',
        fields: [{ name: 'accountName', type: 'text' }, { name: 'bankName', type: 'text' }, { name: 'accountNumber', type: 'text' }, { name: 'balance', type: 'number' }]
    },
    {
        id: 'cards', label: 'Cards', endpoint: '/api/cards',
        fields: [{ name: 'cardName', type: 'text' }, { name: 'cardType', type: 'select', options: ['Credit', 'Debit'] }, { name: 'cardNumber', type: 'text' }, { name: 'creditLimit', type: 'number' }]
    },
    {
        id: 'deposits', label: 'Deposits', endpoint: '/api/deposits',
        fields: [{ name: 'depositType', type: 'select', options: ['FD', 'RD', 'Gullak'] }, { name: 'holderName', type: 'text' }, { name: 'principalAmount', type: 'number' }, { name: 'interestRate', type: 'number' }]
    },
    {
        id: 'goldsilverinvestments', label: 'Gold & Silver', endpoint: '/api/goldsilverinvestments',
        fields: [{ name: 'metalType', type: 'select', options: ['Gold', 'Silver'] }, { name: 'weightGrams', type: 'number' }, { name: 'purchasePrice', type: 'number' }]
    },
    {
        id: 'investments', label: 'Investments', endpoint: '/api/investments',
        fields: [{ name: 'investmentName', type: 'text' }, { name: 'amountInvested', type: 'number' }, { name: 'currentValue', type: 'number' }]
    },
    {
        id: 'lendings', label: 'Lendings', endpoint: '/api/lendings',
        fields: [{ name: 'borrowerName', type: 'text' }, { name: 'principalAmount', type: 'number' }, { name: 'interestRate', type: 'number' }, { name: 'tenureMonths', type: 'number' }]
    },
    {
        id: 'loans', label: 'Loans', endpoint: '/api/loans',
        fields: [{ name: 'lenderName', type: 'text' }, { name: 'principalAmount', type: 'number' }, { name: 'interestRate', type: 'number' }, { name: 'tenureMonths', type: 'number' }]
    },
    {
        id: 'mutualfunds', label: 'Mutual Funds', endpoint: '/api/mutualfunds',
        fields: [{ name: 'fundName', type: 'text' }, { name: 'units', type: 'number' }, { name: 'nav', type: 'number' }]
    },
    {
        id: 'taxprofiles', label: 'Tax Profiles', endpoint: '/api/taxprofiles',
        fields: [{ name: 'profileName', type: 'text' }, { name: 'financialYear', type: 'text' }, { name: 'grossIncome', type: 'number' }, { name: 'totalDeductions', type: 'number' }]
    }
];

function SummaryView() {
    const [stats, setStats] = useState({ balance: 0, income: 0, expenses: 0 });
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        Promise.all([
            fetch('http://localhost:8080/api/bankaccounts').then(r => r.ok ? r.json() : []),
            fetch('http://localhost:8080/api/transactions').then(r => r.ok ? r.json() : [])
        ]).then(([accounts, transactions]) => {
            const totalBalance = Array.isArray(accounts) ? accounts.reduce((sum, acc) => sum + (acc.balance || 0), 0) : 0;
            let totalIncome = 0;
            let totalExpenses = 0;

            if (Array.isArray(transactions)) {
                transactions.forEach(t => {
                    if (t.type === 'Income' || t.type === 'income') totalIncome += (t.amount || 0);
                    else if (t.type === 'Expense' || t.type === 'expense') totalExpenses += (t.amount || 0);
                });
            }

            setStats({ balance: totalBalance, income: totalIncome, expenses: totalExpenses });
            setLoading(false);
        }).catch(err => {
            console.error("Failed to load summary stats", err);
            setLoading(false);
        });
    }, []);

    if (loading) return <div className="loading-spinner">Loading Financial Summary...</div>;

    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '24px' }}>
                
                {/* Monthly Overview (Mock Chart) */}
                <div style={{ 
                    background: '#1c2025', padding: '24px', borderRadius: '24px', 
                    border: '1px solid rgba(255,255,255,0.05)',
                    display: 'flex', flexDirection: 'column', gap: '16px',
                    minHeight: '280px'
                }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <div>
                            <h3 style={{ margin: 0, color: '#fff', fontSize: '16px', fontWeight: '600' }}>Monthly Overview</h3>
                            <p style={{ margin: '4px 0 0 0', color: '#8892b0', fontSize: '13px' }}>Spending for the week</p>
                        </div>
                        <span style={{ color: '#8892b0' }}>•••</span>
                    </div>
                    
                    <div style={{ flex: 1, display: 'flex', alignItems: 'flex-end', gap: '12px', marginTop: '30px', borderBottom: '1px solid rgba(255,255,255,0.05)', paddingBottom: '12px' }}>
                        {[40, 60, 90, 100, 70, 50, 80].map((h, i) => (
                            <div key={i} style={{ flex: 1, height: `${h}%`, background: h === 100 ? '#34d399' : 'linear-gradient(180deg, rgba(52,211,153,0.6) 0%, rgba(52,211,153,0.1) 100%)', borderRadius: '4px 4px 0 0', boxShadow: h === 100 ? '0 0 20px rgba(52, 211, 153, 0.4)' : 'none' }}></div>
                        ))}
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', color: '#8892b0', fontSize: '11px', marginTop: '-8px' }}>
                        <span>M</span><span>T</span><span>W</span><span>T</span><span>F</span><span>S</span><span>S</span>
                    </div>
                </div>

                {/* Current Budget */}
                <div style={{ 
                    background: '#1c2025', padding: '32px 24px', borderRadius: '24px', 
                    border: '1px solid #34d399', boxShadow: '0 0 25px rgba(52,211,153,0.15)',
                    display: 'flex', flexDirection: 'column', justifyContent: 'center'
                }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                        <h3 style={{ margin: 0, color: '#fff', fontSize: '18px', fontWeight: '500' }}>Current Budget</h3>
                        <div style={{ width: 32, height: 32, borderRadius: '8px', background: '#34d399', color: '#000', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold', cursor: 'pointer' }}>+</div>
                    </div>
                    
                    <p style={{ margin: 0, color: '#8892b0', fontSize: '13px', marginBottom: '8px' }}>Total Available</p>
                    <div style={{ fontSize: '42px', fontWeight: '700', color: '#fff', marginBottom: '16px', letterSpacing: '-1px' }}>
                        ₹{stats.balance.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </div>
                    <p style={{ margin: 0, color: '#8892b0', fontSize: '12px' }}>Updated just now</p>
                </div>

                {/* Goal Progress */}
                <div style={{ 
                    background: '#1c2025', padding: '24px', borderRadius: '24px', 
                    border: '1px solid rgba(255,255,255,0.05)',
                    display: 'flex', flexDirection: 'column'
                }}>
                    <h3 style={{ margin: 0, color: '#fff', fontSize: '16px', fontWeight: '600', marginBottom: '16px' }}>Goal Progress</h3>
                    <p style={{ margin: 0, color: '#8892b0', fontSize: '13px', marginBottom: '8px' }}>Save ₹1,00,000</p>
                    <div style={{ fontSize: '32px', fontWeight: '700', color: '#fff', marginBottom: '16px' }}>65%</div>
                    
                    <div style={{ width: '100%', height: '6px', background: 'rgba(255,255,255,0.1)', borderRadius: '3px', marginBottom: '16px' }}>
                        <div style={{ width: '65%', height: '100%', background: '#34d399', borderRadius: '3px', boxShadow: '0 0 10px rgba(52,211,153,0.5)' }}></div>
                    </div>
                    
                    <p style={{ margin: 0, color: '#34d399', fontSize: '13px', fontWeight: '500' }}>₹65,000 Saved</p>
                </div>
            </div>
            
            {/* Quick Stats Bottom Row */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '24px' }}>
                 <div style={{ 
                    background: '#1c2025', padding: '24px', borderRadius: '20px', 
                    border: '1px solid rgba(255,255,255,0.05)',
                    display: 'flex', alignItems: 'center', gap: '16px'
                }}>
                    <div style={{ width: 48, height: 48, borderRadius: '12px', background: 'rgba(52,211,153,0.1)', color: '#34d399', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '20px' }}>📈</div>
                    <div>
                        <p style={{ margin: 0, color: '#8892b0', fontSize: '13px', marginBottom: '4px' }}>Total Income</p>
                        <h4 style={{ margin: 0, color: '#fff', fontSize: '20px' }}>₹{stats.income.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</h4>
                    </div>
                </div>

                <div style={{ 
                    background: '#1c2025', padding: '24px', borderRadius: '20px', 
                    border: '1px solid rgba(255,255,255,0.05)',
                    display: 'flex', alignItems: 'center', gap: '16px'
                }}>
                    <div style={{ width: 48, height: 48, borderRadius: '12px', background: 'rgba(255,71,87,0.1)', color: '#FF4757', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '20px' }}>📉</div>
                    <div>
                        <p style={{ margin: 0, color: '#8892b0', fontSize: '13px', marginBottom: '4px' }}>Total Expenses</p>
                        <h4 style={{ margin: 0, color: '#fff', fontSize: '20px' }}>₹{stats.expenses.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</h4>
                    </div>
                </div>
            </div>
        </div>
    );
}

function ModuleView({ module, refreshTrigger }) {
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(true);

    const fetchData = () => {
        setLoading(true);
        fetch(`http://localhost:8080${module.endpoint}`)
            .then(res => {
                if (!res.ok) {
                    throw new Error(`HTTP error! status: ${res.status}`);
                }
                return res.json();
            })
            .then(resData => {
                if (Array.isArray(resData)) {
                    setData(resData);
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
    };

    useEffect(() => {
        fetchData();
    }, [module, refreshTrigger]);

    if (loading) {
        return <div className="loading-spinner">Loading {module.label} from Java Backend...</div>;
    }

    const handleDelete = (id) => {
        if (window.confirm("Are you sure you want to delete this record?")) {
            fetch(`http://localhost:8080${module.endpoint}/${id}`, { method: 'DELETE' })
                .then(res => {
                    if (res.ok) {
                        setData(prev => prev.filter(item => item.id !== id));
                    }
                })
                .catch(err => console.error("Failed to delete", err));
        }
    };

    // Extract columns from the first record if data exists
    const columns = (Array.isArray(data) && data.length > 0)
        ? Object.keys(data[0] || {}).filter(k => k !== 'id')
        : [];

    return (
        <div className="table-container" style={{ overflowX: 'auto', position: 'relative' }}>
            <h3 style={{ marginBottom: '16px' }}>{module.label} Records</h3>

            {!Array.isArray(data) || data.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
                    No {module.label} records found.
                </div>
            ) : (
                <table className="transaction-table">
                    <thead>
                        <tr>
                            {columns.map(col => (
                                <th key={col}>{col.charAt(0).toUpperCase() + col.slice(1)}</th>
                            ))}
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {data.map((row, index) => (
                            <tr key={row.id || index}>
                                {columns.map(col => (
                                    <td key={col}>
                                        {typeof row[col] === 'number'
                                            ? `₹${row[col].toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
                                            : (row[col]?.toString() || '-')}
                                    </td>
                                ))}
                                <td>
                                    <button
                                        onClick={() => handleDelete(row.id)}
                                        style={{ background: 'transparent', border: 'none', color: '#ef4444', cursor: 'pointer', fontSize: '16px' }}
                                        title="Delete"
                                    >
                                        🗑️
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}

export default function Dashboard({ onLogout }) {
    const [activeModule, setActiveModule] = useState(MODULES[0]);
    const [isAddModalOpen, setIsAddModalOpen] = useState(false);
    const [formData, setFormData] = useState({});
    const [refreshTrigger, setRefreshTrigger] = useState(0);
    const [theme, setTheme] = useState('light'); // default to light theme
    const [userName, setUserName] = useState('User');

    useEffect(() => {
        const unsubscribe = auth.onAuthStateChanged((user) => {
            if (user) {
                setUserName(user.displayName || 'User');
            }
        });
        
        if (auth.currentUser) {
            setUserName(auth.currentUser.displayName || 'User');
        }

        return () => unsubscribe();
    }, []);

    const handleFormChange = (e) => {
        const { name, value, type } = e.target;
        const finalValue = type === 'number' ? (value ? Number(value) : 0) : value;
        setFormData(prev => ({ ...prev, [name]: finalValue }));
    };

    const handleAddSubmit = (e) => {
        e.preventDefault();
        fetch(`http://localhost:8080${activeModule.endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        })
        .then(res => res.text())
        .then(textRes => {
            setIsAddModalOpen(false);
            setFormData({});
            setRefreshTrigger(prev => prev + 1);
        })
        .catch(err => console.error("Failed to add", err));
    };

    const toggleTheme = () => setTheme(prev => prev === 'light' ? 'dark' : 'light');

    return (
        <div data-theme={theme} style={{ 
            width: '100vw', height: '100vh', 
            background: 'var(--bg-dark)', color: 'var(--text-main)', 
            display: 'flex', position: 'fixed', top: 0, left: 0, zIndex: 100,
            overflow: 'hidden', fontFamily: 'system-ui, -apple-system, sans-serif'
        }}>
            
            {/* Add Modal */}
            {isAddModalOpen && (
                <div style={{
                    position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, 
                    background: 'rgba(0,0,0,0.5)', backdropFilter: 'blur(4px)', 
                    display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 100
                }}>
                    <div className="glass-card" style={{ width: '400px', padding: '24px', background: 'var(--surface-dark)', border: '1px solid var(--border-dark)', borderRadius: '24px' }}>
                        <h2 style={{ marginBottom: '16px', color: 'var(--text-main)' }}>Add {activeModule.label}</h2>
                        <form onSubmit={handleAddSubmit}>
                            {activeModule.fields.map(field => (
                                <div key={field.name} className="input-group" style={{ marginBottom: '12px' }}>
                                    <label style={{ color: 'var(--text-muted)' }}>{field.name.charAt(0).toUpperCase() + field.name.slice(1).replace(/([A-Z])/g, ' $1')}</label>
                                    {field.type === 'select' ? (
                                        <select 
                                            name={field.name} 
                                            onChange={handleFormChange} 
                                            style={{ width: '100%', padding: '10px', borderRadius: '8px', background: 'var(--bg-dark)', color: 'var(--text-main)', border: '1px solid var(--border-dark)' }}
                                            required
                                        >
                                            <option value="">Select...</option>
                                            {field.options.map(opt => <option key={opt} value={opt} style={{color: '#000'}}>{opt}</option>)}
                                        </select>
                                    ) : (
                                        <input 
                                            type={field.type} 
                                            name={field.name} 
                                            onChange={handleFormChange}
                                            style={{ width: '100%', padding: '10px', borderRadius: '8px', background: 'var(--bg-dark)', color: 'var(--text-main)', border: '1px solid var(--border-dark)' }}
                                            required
                                        />
                                    )}
                                </div>
                            ))}
                            <div style={{ display: 'flex', gap: '12px', marginTop: '24px' }}>
                                <button type="button" className="btn-secondary" style={{ flex: 1, padding: '10px', borderRadius: '8px', background: 'var(--bg-dark)', color: 'var(--text-main)', border: '1px solid var(--border-dark)', cursor: 'pointer' }} onClick={() => setIsAddModalOpen(false)}>Cancel</button>
                                <button type="submit" className="btn-primary" style={{ flex: 1, padding: '10px', borderRadius: '8px', background: 'linear-gradient(135deg, #6A5AE0, #8A7CF0)', color: '#fff', border: 'none', cursor: 'pointer', fontWeight: 'bold' }}>Save</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Slim Vertical Sidebar */}
            <div style={{ 
                width: '100px', background: '#121619', borderRight: '1px solid rgba(255,255,255,0.05)', 
                display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '24px 0', zIndex: 10
            }}>
                {/* Logo Area */}
                <div style={{ marginBottom: '40px' }}>
                    <div style={{ background: '#34d399', color: '#121619', width: 40, height: 40, borderRadius: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold', fontSize: '18px', boxShadow: '0 4px 15px rgba(52,211,153,0.3)' }}>
                        <svg viewBox="0 0 50 50" width="24" height="24">
                            <path d="M 8 22 L 18 38 L 28 12 C 38 12 42 22 24 42 L 44 42" fill="none" stroke="currentColor" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" />
                        </svg>
                    </div>
                </div>

                {/* Nav Links */}
                <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '4px', width: '100%', overflowY: 'auto' }}>
                    {MODULES.map(m => (
                        <div 
                            key={m.id}
                            onClick={() => setActiveModule(m)}
                            style={{
                                padding: '16px 0',
                                width: '100%',
                                cursor: 'pointer',
                                background: activeModule.id === m.id ? 'linear-gradient(90deg, rgba(52,211,153,0.1) 0%, transparent 100%)' : 'transparent',
                                color: activeModule.id === m.id ? '#34d399' : '#8892b0',
                                borderLeft: activeModule.id === m.id ? '3px solid #34d399' : '3px solid transparent',
                                display: 'flex',
                                flexDirection: 'column',
                                justifyContent: 'center',
                                alignItems: 'center',
                                gap: '8px',
                                transition: 'all 0.2s ease',
                            }}
                        >
                            <span style={{ fontSize: '20px' }}>{activeModule.isSummary ? '⊞' : '📄'}</span>
                            <span style={{ fontSize: '10px', fontWeight: '500', textAlign: 'center', padding: '0 4px' }}>{m.label.replace(' Dashboard', '')}</span>
                        </div>
                    ))}
                </div>

                {/* Bottom Actions */}
                <div style={{ marginTop: 'auto', paddingTop: '20px', borderTop: '1px solid rgba(255,255,255,0.05)', display: 'flex', flexDirection: 'column', gap: '16px', width: '100%', alignItems: 'center' }}>
                    <button onClick={onLogout} style={{
                        width: '40px', height: '40px', borderRadius: '50%', 
                        background: 'rgba(255,255,255,0.05)', border: 'none', 
                        color: '#8892b0', cursor: 'pointer',
                        display: 'flex', alignItems: 'center', justifyContent: 'center',
                        fontSize: '18px'
                    }} title="Logout">
                        🚪
                    </button>
                </div>
            </div>

            {/* Main Content Area */}
            <div style={{ flex: 1, padding: '48px 64px', overflowY: 'auto', background: '#0a0d10' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '40px' }}>
                    <div>
                        <h2 style={{ fontSize: '28px', fontWeight: '600', color: '#fff', margin: '0 0 4px 0', display: 'flex', alignItems: 'center', gap: '8px' }}>
                            Good Morning, {userName} <span style={{fontSize: '24px'}}>👋</span>
                        </h2>
                        <p style={{ color: '#8892b0', fontSize: '14px', margin: 0 }}>November 2023 | overview</p>
                        
                        <h1 style={{ fontSize: '32px', fontWeight: '600', color: '#fff', margin: '32px 0 0 0' }}>
                            {activeModule.label.replace(' Dashboard', '')}
                        </h1>
                    </div>
                    
                    <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
                        {!activeModule.isSummary && (
                            <button onClick={() => setIsAddModalOpen(true)} style={{
                                background: '#34d399', color: '#000', border: 'none',
                                padding: '12px 24px', borderRadius: '30px', fontWeight: '600',
                                cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '8px',
                                boxShadow: '0 4px 15px rgba(52, 211, 153, 0.2)'
                            }}>
                                Add {activeModule.label}
                            </button>
                        )}
                    </div>
                </div>

                <div style={{ 
                    width: '100%'
                }}>
                    {activeModule.isSummary ? (
                        <SummaryView />
                    ) : (
                        <ModuleView module={activeModule} refreshTrigger={refreshTrigger} />
                    )}
                </div>
            </div>
        </div>
    );
}
