// AdminDashboard.jsx - SmartLedger Enterprise Admin Control Center
import React, { useState, useEffect } from 'react';
import './AdminDashboard.css';
import { 
  Shield, Users, Activity, Cpu, Database, Key, Settings, 
  LogOut, Sun, Moon, Search, Bell, CheckCircle2, AlertTriangle, 
  RefreshCw, Filter, ChevronRight, Eye, Edit2, Trash2, 
  ShieldAlert, BarChart3, TrendingUp, DollarSign, Lock, Server, Menu 
} from 'lucide-react';

export default function AdminDashboard({ onLogout }) {
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  const [theme, setTheme] = useState('dark');
  const [activeTab, setActiveTab] = useState('overview');
  const [searchQuery, setSearchQuery] = useState('');
  const [userFilter, setUserFilter] = useState('ALL');
  const [isAddUserModalOpen, setIsAddUserModalOpen] = useState(false);
  const [selectedUserForInspect, setSelectedUserForInspect] = useState(null);

  // Sync theme with html & body tags
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    document.body.setAttribute('data-theme', theme);
  }, [theme]);

  const [usersList, setUsersList] = useState([
    {
      id: 'usr_01',
      name: 'John Doe',
      email: 'john.doe@company.com',
      role: 'Pro User',
      status: 'Active',
      ledgerAccounts: 4,
      aiTokensUsed: '42,150',
      lastLogin: '2 mins ago',
      tier: 'Enterprise',
      flaggedTransactions: 0
    },
    {
      id: 'usr_02',
      name: 'Aisha Sharma',
      email: 'aisha@ledger.com',
      role: 'Standard User',
      status: 'Active',
      ledgerAccounts: 2,
      aiTokensUsed: '12,400',
      lastLogin: '1 hour ago',
      tier: 'Pro',
      flaggedTransactions: 0
    },
    {
      id: 'usr_03',
      name: 'Vikram Mehta',
      email: 'vikram.m@investcorp.in',
      role: 'Pro User',
      status: 'Flagged',
      ledgerAccounts: 7,
      aiTokensUsed: '98,200',
      lastLogin: 'Yesterday',
      tier: 'Enterprise',
      flaggedTransactions: 2
    },
    {
      id: 'usr_04',
      name: 'Siddharth Rao',
      email: 'siddharth@fintech.io',
      role: 'Standard User',
      status: 'Active',
      ledgerAccounts: 1,
      aiTokensUsed: '3,100',
      lastLogin: '3 days ago',
      tier: 'Standard',
      flaggedTransactions: 0
    },
    {
      id: 'usr_05',
      name: 'Elena Rostova',
      email: 'elena@globaltrade.org',
      role: 'Pro User',
      status: 'Pending',
      ledgerAccounts: 3,
      aiTokensUsed: '0',
      lastLogin: 'Never',
      tier: 'Pro',
      flaggedTransactions: 0
    }
  ]);

  const [auditLogs, setAuditLogs] = useState([
    { id: 'log_101', timestamp: '10:42:15 AM', type: 'SECURITY', event: 'Admin Session Initialized', user: 'admin@ledger.com', severity: 'INFO' },
    { id: 'log_102', timestamp: '10:38:04 AM', type: 'AI_PIPELINE', event: 'Statement OCR Extracted (HDFC_Statement_May.pdf)', user: 'aisha@ledger.com', severity: 'SUCCESS' },
    { id: 'log_103', timestamp: '10:15:22 AM', type: 'TRANSACTION', event: 'High Value Transfer Flagged (₹8,45,000)', user: 'vikram.m@investcorp.in', severity: 'WARNING' },
    { id: 'log_104', timestamp: '09:51:10 AM', type: 'AUTH', event: 'Multiple Login Attempts Failed (3x)', user: 'unknown_ip_192.168.1.15', severity: 'CRITICAL' },
    { id: 'log_105', timestamp: '09:12:00 AM', type: 'SYSTEM', event: 'Gemini 3 Pro Token Cache Refreshed (Latency: 18ms)', user: 'SYSTEM_CRON', severity: 'INFO' }
  ]);

  const [newUserForm, setNewUserForm] = useState({
    name: '',
    email: '',
    tier: 'Pro',
    role: 'Standard User'
  });

  const handleAddUserSubmit = (e) => {
    e.preventDefault();
    if (!newUserForm.name || !newUserForm.email) return;
    const newUsr = {
      id: `usr_0${usersList.length + 1}`,
      name: newUserForm.name,
      email: newUserForm.email,
      role: newUserForm.role,
      status: 'Active',
      ledgerAccounts: 0,
      aiTokensUsed: '0',
      lastLogin: 'Just now',
      tier: newUserForm.tier,
      flaggedTransactions: 0
    };
    setUsersList([newUsr, ...usersList]);
    setIsAddUserModalOpen(false);
    setNewUserForm({ name: '', email: '', tier: 'Pro', role: 'Standard User' });
  };

  const handleDeleteUser = (id) => {
    if (window.confirm('Are you sure you want to suspend/remove this user account?')) {
      setUsersList(usersList.filter(u => u.id !== id));
    }
  };

  const filteredUsers = usersList.filter(u => {
    const matchesSearch = 
      u.name.toLowerCase().includes(searchQuery.toLowerCase()) || 
      u.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
      u.id.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesFilter = userFilter === 'ALL' || u.status.toUpperCase() === userFilter;
    return matchesSearch && matchesFilter;
  });

  return (
    <div className="admin-panel-container">
      {/* Sidebar */}
      {/* Sidebar */}
      <aside className={`admin-sidebar ${isSidebarCollapsed ? 'collapsed' : ''}`}>
        <div 
          className="admin-brand" 
          onClick={() => {
            if (isSidebarCollapsed) {
              setIsSidebarCollapsed(false);
            } else {
              setActiveTab('overview');
            }
          }} 
          style={{ cursor: 'pointer' }}
          title={isSidebarCollapsed ? "Expand Sidebar" : "Go to Admin Overview"}
        >
          <div className="admin-brand-icon" style={{ background: 'transparent', boxShadow: 'none', padding: 0 }}>
            <img 
              src="/assets/logo.png" 
              alt="SmartLedger Admin Logo" 
              style={{ 
                width: '38px', 
                height: '38px', 
                objectFit: 'contain', 
                borderRadius: '10px',
                filter: 'drop-shadow(0 4px 12px rgba(16, 185, 129, 0.35))'
              }} 
            />
          </div>
          {!isSidebarCollapsed && (
            <div className="admin-brand-text">
              <span className="admin-brand-title">SmartLedger</span>
              <span className="admin-brand-subtitle">Enterprise Admin</span>
            </div>
          )}
          <button
            className="admin-sidebar-toggle-btn"
            onClick={(e) => {
              e.stopPropagation();
              setIsSidebarCollapsed(!isSidebarCollapsed);
            }}
            title={isSidebarCollapsed ? "Expand Sidebar" : "Collapse Sidebar"}
            aria-label="Toggle Sidebar"
            style={{
              marginLeft: isSidebarCollapsed ? '0' : 'auto',
              background: 'transparent',
              border: 'none',
              color: 'var(--dash-text-muted)',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              padding: '6px',
              borderRadius: '8px'
            }}
          >
            <Menu size={20} />
          </button>
        </div>

        <div className="admin-nav-section-title">Telemetry & System</div>
        <div 
          className={`admin-nav-item ${activeTab === 'overview' ? 'active' : ''}`}
          onClick={() => setActiveTab('overview')}
          title="System Overview"
        >
          <Activity size={18} />
          <span>System Overview</span>
        </div>
        <div 
          className={`admin-nav-item ${activeTab === 'ai_engine' ? 'active' : ''}`}
          onClick={() => setActiveTab('ai_engine')}
          title="Ledger AI Health"
        >
          <Cpu size={18} />
          <span>Ledger AI Health</span>
        </div>

        <div className="admin-nav-section-title">User & Data Control</div>
        <div 
          className={`admin-nav-item ${activeTab === 'users' ? 'active' : ''}`}
          onClick={() => setActiveTab('users')}
          title="User Management"
        >
          <Users size={18} />
          <span>User Management</span>
        </div>
        <div 
          className={`admin-nav-item ${activeTab === 'audit_logs' ? 'active' : ''}`}
          onClick={() => setActiveTab('audit_logs')}
          title="Security Audit Logs"
        >
          <ShieldAlert size={18} />
          <span>Security Audit Logs</span>
        </div>

        <div className="admin-nav-section-title">Platform Configuration</div>
        <div 
          className={`admin-nav-item ${activeTab === 'settings' ? 'active' : ''}`}
          onClick={() => setActiveTab('settings')}
          title="Platform Settings"
        >
          <Settings size={18} />
          <span>Platform Settings</span>
        </div>

        <div style={{ marginTop: 'auto', paddingTop: '20px', borderTop: '1px solid var(--dash-border)' }}>
          <div 
            className="admin-nav-item" 
            style={{ color: '#ef4444', cursor: 'pointer' }}
            onClick={onLogout}
            title="Sign out Admin"
          >
            <LogOut size={18} />
            <span>Sign out Admin</span>
          </div>
        </div>
      </aside>

      {/* Main Container */}
      <main className="admin-main">
        {/* Top Header */}
        <header className="admin-header">
          <div className="admin-header-left">
            <div className="admin-search-box">
              <Search size={16} color="var(--dash-text-muted)" />
              <input
                type="text"
                placeholder="Search accounts, logs, transactions..."
                className="admin-search-input"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          </div>

          <div className="admin-header-right">
            <button 
              className="admin-header-btn"
              onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
              title="Toggle Theme"
            >
              {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
            </button>

            <button className="admin-header-btn" title="Notifications">
              <Bell size={18} />
            </button>

            <div className="admin-profile-pill" onClick={onLogout} title="Click to logout">
              <div className="admin-profile-avatar">A</div>
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                <span style={{ fontSize: '12.5px', fontWeight: '700', lineHeight: '1.2' }}>admin@ledger.com</span>
                <span style={{ fontSize: '10.5px', color: '#10b981', fontWeight: '600' }}>Super Admin • Active</span>
              </div>
            </div>
          </div>
        </header>

        {/* Dynamic Content */}
        <div className="admin-content">
          {/* OVERVIEW TAB */}
          {activeTab === 'overview' && (
            <>
              {/* Hero Banner */}
              <div className="admin-card" style={{ 
                background: 'linear-gradient(135deg, rgba(16, 185, 129, 0.12), rgba(6, 95, 70, 0.05))',
                border: '1px solid rgba(16, 185, 129, 0.25)',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                flexWrap: 'wrap',
                gap: '20px'
              }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
                    <span className="admin-badge admin-badge-success">
                      <CheckCircle2 size={12} /> System Operational
                    </span>
                    <span style={{ fontSize: '12px', color: 'var(--dash-text-muted)' }}>• Node ID: SL-ADX-990</span>
                  </div>
                  <h1 style={{ fontSize: '26px', fontWeight: '800', margin: '0 0 4px', letterSpacing: '-0.5px' }}>
                    Platform Overview & Enterprise Metrics
                  </h1>
                  <p style={{ margin: 0, color: 'var(--dash-text-muted)', fontSize: '14px' }}>
                    Monitoring real-time ledger syncs, Gemini 3 Pro AI token consumption, and user security events.
                  </p>
                </div>
                <div style={{ display: 'flex', gap: '12px' }}>
                  <button className="admin-action-btn" onClick={() => alert('Exporting full audit snapshot...')}>
                    Export System Report
                  </button>
                  <button className="admin-btn-primary" onClick={() => setIsAddUserModalOpen(true)}>
                    + Invite Enterprise User
                  </button>
                </div>
              </div>

              {/* KPI Stat Cards */}
              <div className="admin-stat-grid">
                <div className="admin-stat-card">
                  <div className="admin-stat-label">
                    <span>TOTAL LEDGER ASSETS</span>
                    <DollarSign size={18} color="#10b981" />
                  </div>
                  <div className="admin-stat-value">₹1,42,85,900</div>
                  <div className="admin-stat-footer" style={{ color: '#10b981' }}>
                    <TrendingUp size={14} />
                    <span>+18.4% across 1,248 accounts</span>
                  </div>
                </div>

                <div className="admin-stat-card">
                  <div className="admin-stat-label">
                    <span>ACTIVE SMARTLEDGER USERS</span>
                    <Users size={18} color="#6366f1" />
                  </div>
                  <div className="admin-stat-value">{usersList.length} Accounts</div>
                  <div className="admin-stat-footer" style={{ color: '#6366f1' }}>
                    <span>Enterprise & Pro subscriptions active</span>
                  </div>
                </div>

                <div className="admin-stat-card">
                  <div className="admin-stat-label">
                    <span>LEDGER AI QUERY ACCURACY</span>
                    <Cpu size={18} color="#f59e0b" />
                  </div>
                  <div className="admin-stat-value">98.7%</div>
                  <div className="admin-stat-footer" style={{ color: '#f59e0b' }}>
                    <span>14,290 PDF statements processed</span>
                  </div>
                </div>

                <div className="admin-stat-card">
                  <div className="admin-stat-label">
                    <span>API & DATABASE LATENCY</span>
                    <Server size={18} color="#10b981" />
                  </div>
                  <div className="admin-stat-value">24 ms</div>
                  <div className="admin-stat-footer" style={{ color: '#10b981' }}>
                    <CheckCircle2 size={14} />
                    <span>0.00% Error Rate (24h avg)</span>
                  </div>
                </div>
              </div>

              {/* System Audit Preview & Activity */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '24px' }}>
                <div className="admin-card">
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                    <h3 style={{ margin: 0, fontSize: '17px', fontWeight: '700' }}>Recent Security Event Stream</h3>
                    <button 
                      className="admin-action-btn" 
                      onClick={() => setActiveTab('audit_logs')}
                    >
                      View All Logs
                    </button>
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    {auditLogs.slice(0, 4).map(log => (
                      <div 
                        key={log.id} 
                        style={{
                          padding: '12px 16px',
                          borderRadius: '12px',
                          background: 'var(--dash-glass-bg)',
                          border: '1px solid var(--dash-border)',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'space-between'
                        }}
                      >
                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                          <div style={{
                            width: 8,
                            height: 8,
                            borderRadius: '50%',
                            background: 
                              log.severity === 'CRITICAL' ? '#ef4444' : 
                              log.severity === 'WARNING' ? '#f59e0b' : '#10b981'
                          }} />
                          <div>
                            <div style={{ fontSize: '13.5px', fontWeight: '700', color: 'var(--dash-text)' }}>{log.event}</div>
                            <div style={{ fontSize: '11.5px', color: 'var(--dash-text-muted)' }}>{log.user} • {log.timestamp}</div>
                          </div>
                        </div>
                        <span className={`admin-badge ${log.severity === 'CRITICAL' ? 'admin-badge-danger' : log.severity === 'WARNING' ? 'admin-badge-warning' : 'admin-badge-success'}`}>
                          {log.severity}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>

                {/* System Nodes & AI Cache Widget */}
                <div className="admin-card">
                  <h3 style={{ margin: '0 0 20px', fontSize: '17px', fontWeight: '700' }}>AI Infrastructure Status</h3>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px', borderRadius: '12px', background: 'var(--dash-glass-bg)', border: '1px solid var(--dash-border)' }}>
                      <div>
                        <div style={{ fontSize: '14px', fontWeight: '700' }}>Gemini 3 Pro Inference Pipeline</div>
                        <div style={{ fontSize: '12px', color: 'var(--dash-text-muted)' }}>Python FastAPI Microservice (Port 8000)</div>
                      </div>
                      <span className="admin-badge admin-badge-success">CONNECTED</span>
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px', borderRadius: '12px', background: 'var(--dash-glass-bg)', border: '1px solid var(--dash-border)' }}>
                      <div>
                        <div style={{ fontSize: '14px', fontWeight: '700' }}>Spring Boot Transaction Gateway</div>
                        <div style={{ fontSize: '12px', color: 'var(--dash-text-muted)' }}>Backend SQL & Firestore Engine (Port 8080)</div>
                      </div>
                      <span className="admin-badge admin-badge-success">OPTIMAL</span>
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px', borderRadius: '12px', background: 'var(--dash-glass-bg)', border: '1px solid var(--dash-border)' }}>
                      <div>
                        <div style={{ fontSize: '14px', fontWeight: '700' }}>PDF OCR Layout Decoder</div>
                        <div style={{ fontSize: '12px', color: 'var(--dash-text-muted)' }}>Column-Header Proximity Engine</div>
                      </div>
                      <span className="admin-badge admin-badge-success">ACTIVE</span>
                    </div>
                  </div>
                </div>
              </div>
            </>
          )}

          {/* USERS TAB */}
          {activeTab === 'users' && (
            <div className="admin-card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px', flexWrap: 'wrap', gap: '16px' }}>
                <div>
                  <h2 style={{ margin: 0, fontSize: '22px', fontWeight: '800' }}>User & Account Management</h2>
                  <p style={{ margin: '4px 0 0', color: 'var(--dash-text-muted)', fontSize: '13.5px' }}>
                    Inspect financial ledgers, reset authentication, and manage enterprise subscription tiers.
                  </p>
                </div>

                <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                  <select 
                    value={userFilter}
                    onChange={(e) => setUserFilter(e.target.value)}
                    style={{
                      padding: '8px 14px',
                      borderRadius: '10px',
                      background: 'var(--dash-bg)',
                      color: 'var(--dash-text)',
                      border: '1px solid var(--dash-border)',
                      fontWeight: '600',
                      fontSize: '13px',
                      outline: 'none'
                    }}
                  >
                    <option value="ALL">All Status</option>
                    <option value="ACTIVE">Active Users</option>
                    <option value="FLAGGED">Flagged Users</option>
                    <option value="PENDING">Pending Users</option>
                  </select>

                  <button className="admin-btn-primary" onClick={() => setIsAddUserModalOpen(true)}>
                    + Invite New User
                  </button>
                </div>
              </div>

              <div className="admin-table-wrapper">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>USER PROFILE</th>
                      <th>ROLE / TIER</th>
                      <th>STATUS</th>
                      <th>LEDGER ACCOUNTS</th>
                      <th>AI TOKENS USED</th>
                      <th>LAST LOGIN</th>
                      <th style={{ textAlign: 'right' }}>ACTIONS</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredUsers.map(user => (
                      <tr key={user.id}>
                        <td>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                            <div style={{
                              width: '36px',
                              height: '36px',
                              borderRadius: '50%',
                              background: 'linear-gradient(135deg, #6366f1, #4f46e5)',
                              color: '#fff',
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center',
                              fontWeight: '700',
                              fontSize: '14px'
                            }}>
                              {user.name.charAt(0)}
                            </div>
                            <div>
                              <div style={{ fontWeight: '700', color: 'var(--dash-text)' }}>{user.name}</div>
                              <div style={{ fontSize: '12px', color: 'var(--dash-text-muted)' }}>{user.email}</div>
                            </div>
                          </div>
                        </td>
                        <td>
                          <div style={{ fontWeight: '600' }}>{user.tier}</div>
                          <div style={{ fontSize: '11.5px', color: 'var(--dash-text-muted)' }}>{user.role}</div>
                        </td>
                        <td>
                          <span className={`admin-badge ${
                            user.status === 'Active' ? 'admin-badge-success' : 
                            user.status === 'Flagged' ? 'admin-badge-danger' : 'admin-badge-warning'
                          }`}>
                            {user.status}
                          </span>
                        </td>
                        <td style={{ fontWeight: '700' }}>{user.ledgerAccounts} Accounts</td>
                        <td>{user.aiTokensUsed} tokens</td>
                        <td style={{ color: 'var(--dash-text-muted)' }}>{user.lastLogin}</td>
                        <td style={{ textAlign: 'right' }}>
                          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px' }}>
                            <button 
                              className="admin-action-btn"
                              onClick={() => setSelectedUserForInspect(user)}
                              title="Inspect User Ledger"
                            >
                              <Eye size={14} />
                            </button>
                            <button 
                              className="admin-action-btn"
                              onClick={() => handleDeleteUser(user.id)}
                              style={{ color: '#ef4444' }}
                              title="Delete/Suspend User"
                            >
                              <Trash2 size={14} />
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* AI ENGINE TAB */}
          {activeTab === 'ai_engine' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
              <div className="admin-card">
                <h2 style={{ margin: '0 0 8px', fontSize: '22px', fontWeight: '800' }}>Ledger AI & Microservice Health</h2>
                <p style={{ margin: '0 0 24px', color: 'var(--dash-text-muted)', fontSize: '13.5px' }}>
                  Configuration for PDF statement ingestion, OCR proximity models, and Gemini 3 Pro AI token limiters.
                </p>

                <div className="admin-stat-grid">
                  <div className="admin-stat-card">
                    <div className="admin-stat-label">
                      <span>ACTIVE MODEL ENGINE</span>
                      <Cpu size={18} color="#10b981" />
                    </div>
                    <div className="admin-stat-value">Gemini 3 Pro</div>
                    <div className="admin-stat-footer" style={{ color: '#10b981' }}>
                      <span>Temperature: 0.1 • Layout-Aware OCR</span>
                    </div>
                  </div>

                  <div className="admin-stat-card">
                    <div className="admin-stat-label">
                      <span>AVG PROCESSING TIME</span>
                      <Activity size={18} color="#6366f1" />
                    </div>
                    <div className="admin-stat-value">2.41 sec</div>
                    <div className="admin-stat-footer" style={{ color: '#6366f1' }}>
                      <span>PDF column headers aligned per row</span>
                    </div>
                  </div>

                  <div className="admin-stat-card">
                    <div className="admin-stat-label">
                      <span>SUCCESS RATE</span>
                      <CheckCircle2 size={18} color="#10b981" />
                    </div>
                    <div className="admin-stat-value">99.4%</div>
                    <div className="admin-stat-footer" style={{ color: '#10b981' }}>
                      <span>Zero keyword heuristic drift</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* AUDIT LOGS TAB */}
          {activeTab === 'audit_logs' && (
            <div className="admin-card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <div>
                  <h2 style={{ margin: 0, fontSize: '22px', fontWeight: '800' }}>Security & Ledger Audit Logs</h2>
                  <p style={{ margin: '4px 0 0', color: 'var(--dash-text-muted)', fontSize: '13.5px' }}>
                    Immutable trace of all administrative logins, system overrides, and high-value transactions.
                  </p>
                </div>
                <button className="admin-action-btn" onClick={() => alert('Refreshing live audit feed...')}>
                  <RefreshCw size={14} /> Refresh Feed
                </button>
              </div>

              <div className="admin-table-wrapper">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>TIMESTAMP</th>
                      <th>EVENT TYPE</th>
                      <th>DESCRIPTION</th>
                      <th>USER / ACTOR</th>
                      <th>SEVERITY</th>
                    </tr>
                  </thead>
                  <tbody>
                    {auditLogs.map(log => (
                      <tr key={log.id}>
                        <td style={{ fontWeight: '600' }}>{log.timestamp}</td>
                        <td>
                          <span className="admin-badge admin-badge-info">{log.type}</span>
                        </td>
                        <td style={{ fontWeight: '700' }}>{log.event}</td>
                        <td style={{ color: 'var(--dash-text-muted)' }}>{log.user}</td>
                        <td>
                          <span className={`admin-badge ${log.severity === 'CRITICAL' ? 'admin-badge-danger' : log.severity === 'WARNING' ? 'admin-badge-warning' : 'admin-badge-success'}`}>
                            {log.severity}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* SETTINGS TAB */}
          {activeTab === 'settings' && (
            <div className="admin-card" style={{ maxWidth: 640 }}>
              <h2 style={{ margin: '0 0 6px', fontSize: '22px', fontWeight: '800' }}>Platform Configuration</h2>
              <p style={{ margin: '0 0 28px', color: 'var(--dash-text-muted)', fontSize: '13.5px' }}>
                Global security controls and default system parameters.
              </p>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px', borderRadius: '14px', background: 'var(--dash-glass-bg)', border: '1px solid var(--dash-border)' }}>
                  <div>
                    <div style={{ fontWeight: '700', fontSize: '15px' }}>Enforce Two-Factor Authentication</div>
                    <div style={{ fontSize: '12.5px', color: 'var(--dash-text-muted)' }}>Require OTP verification for all admin logins</div>
                  </div>
                  <input type="checkbox" defaultChecked style={{ width: 18, height: 18, accentColor: '#10b981' }} />
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px', borderRadius: '14px', background: 'var(--dash-glass-bg)', border: '1px solid var(--dash-border)' }}>
                  <div>
                    <div style={{ fontWeight: '700', fontSize: '15px' }}>System Maintenance Mode</div>
                    <div style={{ fontSize: '12.5px', color: 'var(--dash-text-muted)' }}>Lock non-admin users from creating transactions</div>
                  </div>
                  <input type="checkbox" style={{ width: 18, height: 18, accentColor: '#10b981' }} />
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px', borderRadius: '14px', background: 'var(--dash-glass-bg)', border: '1px solid var(--dash-border)' }}>
                  <div>
                    <div style={{ fontWeight: '700', fontSize: '15px' }}>AI Strict PDF Column Alignment</div>
                    <div style={{ fontSize: '12.5px', color: 'var(--dash-text-muted)' }}>Disable keyword heuristic fallback in statement extraction</div>
                  </div>
                  <input type="checkbox" defaultChecked style={{ width: 18, height: 18, accentColor: '#10b981' }} />
                </div>
              </div>
            </div>
          )}
        </div>
      </main>

      {/* Add User Modal */}
      {isAddUserModalOpen && (
        <div className="admin-modal-overlay" onClick={() => setIsAddUserModalOpen(false)}>
          <div className="admin-modal-card" onClick={(e) => e.stopPropagation()}>
            <h3 style={{ margin: '0 0 6px', fontSize: '20px', fontWeight: '800' }}>Invite Enterprise User</h3>
            <p style={{ margin: '0 0 20px', color: 'var(--dash-text-muted)', fontSize: '13.5px' }}>
              Create a new account on SmartLedger with custom role and subscription tier.
            </p>

            <form onSubmit={handleAddUserSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: 'var(--dash-text-muted)', marginBottom: '6px' }}>FULL NAME</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Maya Lin"
                  value={newUserForm.name}
                  onChange={(e) => setNewUserForm({ ...newUserForm, name: e.target.value })}
                  style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: 'var(--dash-text-muted)', marginBottom: '6px' }}>EMAIL ADDRESS</label>
                <input
                  type="email"
                  required
                  placeholder="name@company.com"
                  value={newUserForm.email}
                  onChange={(e) => setNewUserForm({ ...newUserForm, email: e.target.value })}
                  style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: 'var(--dash-text-muted)', marginBottom: '6px' }}>SUBSCRIPTION TIER</label>
                  <select
                    value={newUserForm.tier}
                    onChange={(e) => setNewUserForm({ ...newUserForm, tier: e.target.value })}
                    style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                  >
                    <option value="Enterprise">Enterprise</option>
                    <option value="Pro">Pro</option>
                    <option value="Standard">Standard</option>
                  </select>
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: 'var(--dash-text-muted)', marginBottom: '6px' }}>ROLE</label>
                  <select
                    value={newUserForm.role}
                    onChange={(e) => setNewUserForm({ ...newUserForm, role: e.target.value })}
                    style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                  >
                    <option value="Standard User">Standard User</option>
                    <option value="Pro User">Pro User</option>
                    <option value="Admin">Admin</option>
                  </select>
                </div>
              </div>

              <div style={{ display: 'flex', gap: '12px', marginTop: '16px' }}>
                <button 
                  type="button" 
                  className="admin-action-btn" 
                  style={{ flex: 1, padding: '12px' }}
                  onClick={() => setIsAddUserModalOpen(false)}
                >
                  Cancel
                </button>
                <button 
                  type="submit" 
                  className="admin-btn-primary" 
                  style={{ flex: 1, padding: '12px' }}
                >
                  Create & Invite User
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Inspect User Modal */}
      {selectedUserForInspect && (
        <div className="admin-modal-overlay" onClick={() => setSelectedUserForInspect(null)}>
          <div className="admin-modal-card" onClick={(e) => e.stopPropagation()}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <h3 style={{ margin: 0, fontSize: '20px', fontWeight: '800' }}>{selectedUserForInspect.name} • Ledger Audit</h3>
              <span className={`admin-badge ${selectedUserForInspect.status === 'Active' ? 'admin-badge-success' : 'admin-badge-danger'}`}>
                {selectedUserForInspect.status}
              </span>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginBottom: '24px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 14px', borderRadius: '10px', background: 'var(--dash-glass-bg)', border: '1px solid var(--dash-border)' }}>
                <span style={{ color: 'var(--dash-text-muted)' }}>Email:</span>
                <span style={{ fontWeight: '700' }}>{selectedUserForInspect.email}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 14px', borderRadius: '10px', background: 'var(--dash-glass-bg)', border: '1px solid var(--dash-border)' }}>
                <span style={{ color: 'var(--dash-text-muted)' }}>Ledger Accounts:</span>
                <span style={{ fontWeight: '700' }}>{selectedUserForInspect.ledgerAccounts} active</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 14px', borderRadius: '10px', background: 'var(--dash-glass-bg)', border: '1px solid var(--dash-border)' }}>
                <span style={{ color: 'var(--dash-text-muted)' }}>AI Tokens Consumed:</span>
                <span style={{ fontWeight: '700' }}>{selectedUserForInspect.aiTokensUsed}</span>
              </div>
            </div>

            <button 
              className="admin-btn-primary" 
              style={{ width: '100%', padding: '12px' }}
              onClick={() => setSelectedUserForInspect(null)}
            >
              Close Inspection
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
