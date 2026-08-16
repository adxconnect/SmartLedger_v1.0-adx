// AdminDashboard.jsx - SmartLedger Enterprise Admin Control Center
import React, { useState, useEffect, useRef, useMemo } from 'react';
import './AdminDashboard.css';
import {
  Shield, Users, Activity, Cpu, Database, Key, Settings,
  LogOut, Sun, Moon, Search, Bell, CheckCircle2, AlertTriangle,
  RefreshCw, Filter, ChevronRight, Eye, Edit2, Trash2,
  ShieldAlert, BarChart3, TrendingUp, DollarSign, Lock, Server, Menu, Megaphone, Ticket, Download, MessageSquare
} from 'lucide-react';
import { auth, db } from './firebase';
import { collection, getDocs, doc, setDoc } from 'firebase/firestore';
import ReactQuill from 'react-quill-new';
import 'react-quill-new/dist/quill.snow.css';
import 'react-quill-new/dist/quill.snow.css';

const ExpandableMessage = ({ text }) => {
    const [expanded, setExpanded] = useState(false);
    const isLong = text && text.length > 60;
    
    return (
        <div>
            <span>{expanded ? text : (isLong ? text.substring(0, 60) + '...' : text)}</span>
            {isLong && (
                <span 
                    onClick={() => setExpanded(!expanded)} 
                    style={{ color: '#10b981', cursor: 'pointer', marginLeft: '8px', fontSize: '11px', fontWeight: 'bold', display: 'inline-block' }}
                >
                    {expanded ? 'Show Less' : 'View Full'}
                </span>
            )}
        </div>
    );
};
export default function AdminDashboard({ onLogout }) {
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  const [theme, setTheme] = useState('dark');
  const [isMaintenanceMode, setIsMaintenanceMode] = useState(false);
  const [activeTab, setActiveTab] = useState('overview');
  const [searchQuery, setSearchQuery] = useState('');
  const [userFilter, setUserFilter] = useState('ALL');
  const [isAddUserModalOpen, setIsAddUserModalOpen] = useState(false);
  const [selectedUserForInspect, setSelectedUserForInspect] = useState(null);
  const [userToDelete, setUserToDelete] = useState(null);
  const [currentUser, setCurrentUser] = useState(null);
  const [isAdmin, setIsAdmin] = useState(false);

  const [isCreateSubscriptionModalOpen, setIsCreateSubscriptionModalOpen] = useState(false);
  const [editingSubscriptionId, setEditingSubscriptionId] = useState(null);
  const [newSubscriptionForm, setNewSubscriptionForm] = useState({
    title: '',
    duration: 'Monthly',
    amount: '',
    currency: 'INR',
    autopay: false,
    trialEnabled: false,
    trialDays: ''
  });
  const [subscriptionsList, setSubscriptionsList] = useState([]);

  const [isCreateCouponModalOpen, setIsCreateCouponModalOpen] = useState(false);
  const [editingCouponId, setEditingCouponId] = useState(null);
  const [newCouponForm, setNewCouponForm] = useState({
    code: '',
    discountType: 'Percentage',
    discountValue: '',
    targetType: 'All',
    targetUser: '',
    expiryDate: ''
  });
  
  const [couponsList, setCouponsList] = useState([]);
  const [grievancesList, setGrievancesList] = useState([]);

  const adminFirestoreWrite = async (collectionName, documentId, data) => {
    try {
      const response = await fetch('http://localhost:8000/api/admin/firestore-write', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          collection_name: collectionName,
          document_id: documentId,
          data: data
        })
      });
      if (!response.ok) {
         const errText = await response.text();
         throw new Error(errText);
      }
    } catch (error) {
      console.error("Admin Firestore Write Error:", error);
      throw error;
    }
  };


  const adminFirestoreRead = async (collectionName) => {
    try {
      const response = await fetch('http://localhost:8000/api/admin/firestore-read', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ collection_name: collectionName })
      });
      if (!response.ok) throw new Error(await response.text());
      const data = await response.json();
      return data.data || [];
    } catch (error) {
      console.error(`Admin Firestore Read Error (${collectionName}):`, error);
      return [];
    }
  };

  const adminFirestoreDelete = async (collectionName, documentId) => {
    try {
      const response = await fetch('http://localhost:8000/api/admin/firestore-delete', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ collection_name: collectionName, document_id: documentId })
      });
      if (!response.ok) {
         const errText = await response.text();
         throw new Error(errText);
      }
    } catch (error) {
      console.error("Admin Firestore Delete Error:", error);
      throw error;
    }
  };

  const handleResolveGrievance = async (grievanceId) => {
    try {
      const grievance = grievancesList.find(g => g.id === grievanceId);
      if (!grievance) return;
      
      const updatedGrievance = { ...grievance, status: 'Resolved' };
      
      if (!grievanceId.startsWith('g_')) {
        await adminFirestoreWrite('grievances', grievanceId, updatedGrievance);
      }
      
      setGrievancesList(grievancesList.map(g => g.id === grievanceId ? updatedGrievance : g));
    } catch (e) {
      console.error(e);
      alert("Failed to resolve grievance.");
    }
  };

  const openCreateCouponModal = () => {
    setEditingCouponId(null);
    setNewCouponForm({ code: '', discountType: 'Percentage', discountValue: '', targetType: 'All', targetUser: '', expiryDate: '' });
    setIsCreateCouponModalOpen(true);
  };

  const openEditCouponModal = (coupon) => {
    setEditingCouponId(coupon.id);
    setNewCouponForm({
      code: coupon.code,
      discountType: coupon.discountType,
      discountValue: coupon.discountValue,
      targetType: coupon.targetType,
      targetUser: coupon.targetUser || '',
      expiryDate: coupon.expiryDate
    });
    setIsCreateCouponModalOpen(true);
  };

  const handleCreateCoupon = async (e) => {
    e.preventDefault();
    if (!newCouponForm.code || !newCouponForm.discountValue || !newCouponForm.expiryDate) return;

    if (editingCouponId) {
      const updatedList = couponsList.map(c => 
        c.id === editingCouponId ? { ...c, ...newCouponForm, code: newCouponForm.code.toUpperCase() } : c
      );
      setCouponsList(updatedList);
      setIsCreateCouponModalOpen(false);
      setEditingCouponId(null);
      
      const updatedCoupon = updatedList.find(c => c.id === editingCouponId);
      if (updatedCoupon) {
          await adminFirestoreWrite('coupons', editingCouponId, updatedCoupon);
      }

      alert("Coupon Updated Successfully!");
      return;
    }

    const newCoupon = {
      id: `coup_${Math.random().toString(36).substring(2, 9)}`,
      code: newCouponForm.code.toUpperCase(),
      discountType: newCouponForm.discountType,
      discountValue: newCouponForm.discountValue,
      targetType: newCouponForm.targetType,
      targetUser: newCouponForm.targetUser,
      expiryDate: newCouponForm.expiryDate,
      status: 'Active'
    };
    
    setCouponsList([newCoupon, ...couponsList]);
    setIsCreateCouponModalOpen(false);
    
    await adminFirestoreWrite('coupons', newCoupon.id, newCoupon);
    
    alert("Coupon Created Successfully!");
  };

  const openCreateModal = () => {
    setEditingSubscriptionId(null);
    setNewSubscriptionForm({ title: '', duration: 'Monthly', amount: '', currency: 'INR', autopay: false, trialEnabled: false, trialDays: '' });
    setIsCreateSubscriptionModalOpen(true);
  };

  const openEditModal = (sub) => {
    setEditingSubscriptionId(sub.id);
    setNewSubscriptionForm({
      title: sub.title,
      duration: sub.duration,
      amount: sub.amount,
      currency: sub.currency || 'INR',
      autopay: sub.autopay,
      trialEnabled: sub.trialEnabled || false,
      trialDays: sub.trialDays || ''
    });
    setIsCreateSubscriptionModalOpen(true);
  };
  
  const handleCreateSubscription = async (e) => {
    e.preventDefault();
    if (!newSubscriptionForm.title || !newSubscriptionForm.amount) return;
    
    if (editingSubscriptionId) {
       const updatedList = subscriptionsList.map(sub => 
         sub.id === editingSubscriptionId ? { 
           ...sub, 
           ...newSubscriptionForm, 
           amount: parseFloat(newSubscriptionForm.amount), 
           trialDays: newSubscriptionForm.trialEnabled ? parseInt(newSubscriptionForm.trialDays) : 0 
         } : sub
       );
       setSubscriptionsList(updatedList);
       setIsCreateSubscriptionModalOpen(false);
       setEditingSubscriptionId(null);
       
       const updatedSub = updatedList.find(s => s.id === editingSubscriptionId);
       if (updatedSub) {
           await adminFirestoreWrite('subscriptions', editingSubscriptionId, updatedSub);
       }

       alert("Subscription Tier Updated Successfully!");
       return;
    }
    
    try {
      const response = await fetch('http://localhost:8000/api/billing/create-tier', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title: newSubscriptionForm.title,
          amount: parseFloat(newSubscriptionForm.amount),
          currency: newSubscriptionForm.currency,
          duration: newSubscriptionForm.duration,
          autopay: newSubscriptionForm.autopay,
          trialEnabled: newSubscriptionForm.trialEnabled,
          trialDays: newSubscriptionForm.trialEnabled ? parseInt(newSubscriptionForm.trialDays) : 0
        })
      });
      
      if (response.ok) {
        const data = await response.json();
        const newSub = {
          id: data.id,
          title: newSubscriptionForm.title,
          duration: newSubscriptionForm.duration,
          amount: newSubscriptionForm.amount,
          currency: newSubscriptionForm.currency,
          autopay: newSubscriptionForm.autopay,
          trialEnabled: newSubscriptionForm.trialEnabled,
          trialDays: newSubscriptionForm.trialEnabled ? parseInt(newSubscriptionForm.trialDays) : 0,
          status: 'Active'
        };
        setSubscriptionsList([newSub, ...subscriptionsList]);
        setIsCreateSubscriptionModalOpen(false);
        setNewSubscriptionForm({ title: '', duration: 'Monthly', amount: '', currency: 'INR', autopay: false, trialEnabled: false, trialDays: '' });
        
        await adminFirestoreWrite('subscriptions', newSub.id, newSub);

        alert(data.type === 'plan' ? 'Razorpay Autopay Plan Created Successfully!' : 'Standard Tier Created Successfully!');
      } else {
        const errText = await response.text();
        let errMsg = 'Unknown error';
        try {
           const errObj = JSON.parse(errText);
           errMsg = errObj.detail || errText;
        } catch (e) {
           errMsg = errText;
        }
        alert('Failed to create tier: ' + errMsg);
      }
    } catch (error) {
      console.error("Create Tier Error:", error);
      alert('Error creating subscription tier: ' + error.message);
    }
  };

  const handleDeleteSubscription = async (subId) => {
    if (!window.confirm("Are you sure you want to delete this subscription tier?")) return;
    try {
      await adminFirestoreDelete('subscriptions', subId);
      setSubscriptionsList(subscriptionsList.filter(s => s.id !== subId));
      alert("Subscription Tier Deleted Successfully!");
    } catch (error) {
      alert("Failed to delete subscription tier.");
    }
  };

  // Fetch billing data from backend (subscriptions, coupons, grievances)
  const fetchBillingData = async () => {
    try {
      const subs = await adminFirestoreRead('subscriptions');
      if (subs.length > 0) setSubscriptionsList(subs);
      
      const coups = await adminFirestoreRead('coupons');
      if (coups.length > 0) setCouponsList(coups);

      const grievs = await adminFirestoreRead('grievances');
      if (grievs.length > 0) {
        setGrievancesList(grievs);
      }

      const settings = await adminFirestoreRead('settings');
      const systemSettings = settings.find(s => s.id === 'system');
      if (systemSettings && systemSettings.maintenanceMode !== undefined) {
        setIsMaintenanceMode(systemSettings.maintenanceMode);
      }
    } catch (e) {
      console.error("Firebase fetch error", e);
    }
  };

  // Sync theme with html & body tags
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    document.body.setAttribute('data-theme', theme);
  }, [theme]);

  // Always fetch billing data on mount (covers both Firebase auth and hardcoded admin session)
  useEffect(() => {
    fetchBillingData();
  }, []);

  useEffect(() => {
    const unsubscribe = auth.onAuthStateChanged((user) => {
      setCurrentUser(user);
      if (user) {
        const adminStatus = user.email && user.email.toLowerCase().includes('admin');
        setIsAdmin(adminStatus);
        if (adminStatus) {
            fetchBillingData();
        }
      } else {
        setIsAdmin(false);
      }
    });
    return () => unsubscribe();
  }, []);

  // Fetch Real Users from Backend
  useEffect(() => {
    if (activeTab === 'users' || activeTab === 'overview' || activeTab === 'coupons') {
      const fetchUsers = async () => {
        try {
          const response = await fetch('http://localhost:8000/api/admin/users');
          if (response.ok) {
            const data = await response.json();
            if (data.users && data.users.length > 0) {
              setUsersList(data.users);
            }
          }
        } catch (error) {
          console.error("Failed to fetch real users from backend:", error);
        }
      };
      fetchUsers();
    }
  }, [activeTab]);

  const [aiMetrics, setAiMetrics] = useState({
    modelEngine: 'Loading...',
    avgProcessingTime: '-- sec',
    successRate: '--%',
    totalRequests: 0,
    systemMetrics: {
      totalLedgerAssets: 0,
      totalAccounts: 0,
      activeUsers: 0,
      assetGrowth: "+0.0%"
    }
  });

  const [infraStatus, setInfraStatus] = useState({
    gemini: { label: 'CHECKING...', class: 'admin-badge-warning' },
    springBoot: { label: 'CHECKING...', class: 'admin-badge-warning' },
    ocr: { label: 'CHECKING...', class: 'admin-badge-warning' }
  });

  useEffect(() => {
    if (activeTab === 'ai_engine' || activeTab === 'overview') {
      const fetchMetrics = async () => {
        try {
          const response = await fetch('http://localhost:8000/api/ai/status');
          if (response.ok) {
            const data = await response.json();
            if (data.metrics) {
              setAiMetrics({
                ...data.metrics,
                systemMetrics: data.systemMetrics || aiMetrics.systemMetrics
              });
              setInfraStatus(prev => ({
                ...prev,
                gemini: { label: 'CONNECTED', class: 'admin-badge-success' },
                ocr: { label: 'ACTIVE', class: 'admin-badge-success' }
              }));
            }
          } else {
            setInfraStatus(prev => ({
              ...prev,
              gemini: { label: 'ERROR', class: 'admin-badge-danger' },
              ocr: { label: 'ERROR', class: 'admin-badge-danger' }
            }));
          }
        } catch (error) {
          console.error("Failed to fetch AI metrics", error);
          setInfraStatus(prev => ({
            ...prev,
            gemini: { label: 'OFFLINE', class: 'admin-badge-danger' },
            ocr: { label: 'OFFLINE', class: 'admin-badge-danger' }
          }));
        }

        try {
          const logsResponse = await fetch('http://localhost:8000/api/ai/audit-logs');
          if (logsResponse.ok) {
            const logsData = await logsResponse.json();
            setAuditLogs(logsData);
          }
        } catch (error) {
          console.error("Failed to fetch audit logs", error);
        }

        try {
          // Poll the backend
          const sbResponse = await fetch('http://localhost:8000/api/ai/status');
          if (sbResponse.ok) {
            setInfraStatus(prev => ({ ...prev, springBoot: { label: 'OPTIMAL', class: 'admin-badge-success' } }));
          } else {
            setInfraStatus(prev => ({ ...prev, springBoot: { label: 'DEGRADED', class: 'admin-badge-warning' } }));
          }
        } catch (error) {
          setInfraStatus(prev => ({ ...prev, springBoot: { label: 'OFFLINE', class: 'admin-badge-danger' } }));
        }
      };
      fetchMetrics();
    }
  }, [activeTab]);

  const [usersList, setUsersList] = useState([
    {
      id: 'usr_01',
      uidDisplay: '492810',
      name: 'SmartLedger Admin',
      email: 'admin@ledger.com',
      role: 'Super Admin',
      status: 'Active',
      aiTokensUsed: '0',
      lastLogin: 'Aug 15, 2026, 09:12 AM',
      tier: 'Enterprise',
      flaggedTransactions: 0
    },
    {
      id: 'usr_02',
      uidDisplay: '109245',
      name: 'Standard User',
      email: 'user@ledger.com',
      role: 'Pro User',
      status: 'Active',
      aiTokensUsed: '1,240',
      lastLogin: 'Aug 14, 2026, 14:30 PM',
      tier: 'Pro',
      flaggedTransactions: 0
    }
  ]);

  const totalTokensUsed = usersList.reduce((sum, user) => sum + parseInt(user.aiTokensUsed.replace(/,/g, ''), 10), 0).toLocaleString();

  const [auditLogs, setAuditLogs] = useState([]);
  const [auditLogPage, setAuditLogPage] = useState(1);
  const [isNotificationsOpen, setIsNotificationsOpen] = useState(false);
  const [showAllNotifications, setShowAllNotifications] = useState(false);

  const notifications = useMemo(() => {
    const now = new Date();
    const sevenDaysAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);

    const activeGrievances = grievancesList
      .filter(g => g.status !== 'Resolved')
      .map(g => {
        let date = new Date(g.createdAt || g.timestamp);
        if (isNaN(date.getTime())) date = now;
        return {
          id: g.id,
          type: 'grievance',
          title: `New Grievance from ${g.name || g.userName || 'User'}`,
          message: g.message || g.details || 'No message provided.',
          date: date,
          isNew: true
        };
      });

    const importantLogs = auditLogs
      .filter(log => log.severity === 'CRITICAL' || log.severity === 'WARNING')
      .map(log => {
        let dateStr = log.timestamp.replace(/ (AM|PM)/i, ' $1');
        let date = new Date(dateStr);
        if (isNaN(date.getTime())) date = now;
        return {
          id: log.id,
          type: 'system',
          title: `System ${log.severity}`,
          message: log.event,
          date: date,
          severity: log.severity,
          isNew: true
        };
      });

    let allNotifs = [...activeGrievances, ...importantLogs];
    allNotifs = allNotifs.filter(n => n.date >= sevenDaysAgo);
    allNotifs.sort((a, b) => b.date - a.date);
    return allNotifs;
  }, [grievancesList, auditLogs]);
  const [auditLogStartDate, setAuditLogStartDate] = useState('');
  const [auditLogEndDate, setAuditLogEndDate] = useState('');
  const logsPerPage = 10;

  const [razorpayTransactions, setRazorpayTransactions] = useState([]);
  const [isRefundModalOpen, setIsRefundModalOpen] = useState(false);
  const [selectedTxnForRefund, setSelectedTxnForRefund] = useState(null);
  const [refundType, setRefundType] = useState('existing');
  const [refundAccountDetails, setRefundAccountDetails] = useState('');

  const [announcementTargetType, setAnnouncementTargetType] = useState('ALL');
  const [announcementUserSearch, setAnnouncementUserSearch] = useState('');
  const [announcementSearchResults, setAnnouncementSearchResults] = useState([]);
  const [announcementSelectedUser, setAnnouncementSelectedUser] = useState(null);
  const [announcementContent, setAnnouncementContent] = useState('');

  const quillRef = useRef(null);
  const [isLinkModalOpen, setIsLinkModalOpen] = useState(false);
  const [linkText, setLinkText] = useState('');
  const [linkUrl, setLinkUrl] = useState('');

  const handleLinkClick = () => {
    if (quillRef.current) {
      const editor = quillRef.current.getEditor();
      const range = editor.getSelection();
      if (range && range.length > 0) {
        setLinkText(editor.getText(range.index, range.length));
      } else {
        setLinkText('');
      }
    } else {
      setLinkText('');
    }
    setLinkUrl('');
    setIsLinkModalOpen(true);
  };

  useEffect(() => {
    if (announcementTargetType === 'SPECIFIC' && announcementUserSearch.trim().length > 0) {
      const fetchResults = async () => {
        try {
          const res = await fetch(`http://localhost:8000/api/admin/users/search?q=${announcementUserSearch}`);
          if (res.ok) {
            const data = await res.json();
            setAnnouncementSearchResults(data.users || []);
          }
        } catch (e) {
          console.error(e);
        }
      };
      const debounce = setTimeout(() => {
        fetchResults();
      }, 300);
      return () => clearTimeout(debounce);
    } else {
      setAnnouncementSearchResults([]);
    }
  }, [announcementTargetType, announcementUserSearch]);

  const quillModules = useMemo(() => ({
    toolbar: {
      container: [
        [{ 'header': [1, 2, 3, false] }],
        ['bold', 'italic', 'underline', 'strike'],
        [{ 'align': [] }, { 'list': 'ordered'}, { 'list': 'bullet' }],
        ['link', 'image'],
        ['clean']
      ],
      handlers: {
        link: handleLinkClick
      }
    }
  }), []);

  const handleLinkSubmit = (e) => {
    e.preventDefault();
    if (!linkUrl) return;
    if (quillRef.current) {
      const editor = quillRef.current.getEditor();
      const range = editor.getSelection() || { index: editor.getLength(), length: 0 };
      const textToInsert = linkText || linkUrl;
      
      if (range.length > 0) {
         editor.deleteText(range.index, range.length);
      }
      
      editor.insertText(range.index, textToInsert, 'link', linkUrl);
      editor.setSelection(range.index + textToInsert.length);
    }
    setIsLinkModalOpen(false);
  };

  const handleSendAnnouncement = async () => {
    if (!announcementContent || announcementContent === '<p><br></p>') {
      alert("Announcement cannot be empty");
      return;
    }
    const target = announcementTargetType === 'ALL' ? 'ALL' : (announcementSelectedUser ? announcementSelectedUser.id : null);
    if (!target) {
      alert("Please select a specific user to send the announcement to.");
      return;
    }
    try {
      const payload = {
        target: target,
        content: announcementContent
      };
      const response = await fetch('http://localhost:8000/api/admin/announcement', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (response.ok) {
        alert('Announcement sent successfully! It will appear in client notifications.');
        setAnnouncementContent('');
        setAnnouncementTargetType('ALL');
        setAnnouncementSelectedUser(null);
      } else {
        alert('Failed to send announcement');
      }
    } catch (e) {
      console.error(e);
      alert('Error sending announcement');
    }
  };

  useEffect(() => {
    if (activeTab === 'ledger') {
      const fetchTransactions = async () => {
        try {
          const response = await fetch('http://localhost:8000/api/billing/transactions');
          if (response.ok) {
            const data = await response.json();
            if (data && data.items) {
              setRazorpayTransactions(data.items);
            }
          }
        } catch (error) {
          console.error("Failed to fetch razorpay transactions:", error);
        }
      };
      fetchTransactions();
    }
  }, [activeTab]);

  const handleRefundSubmit = async (e) => {
    e.preventDefault();
    if (!selectedTxnForRefund) return;
    try {
      const payload = {
        payment_id: selectedTxnForRefund.id,
        amount: selectedTxnForRefund.amount, // Full refund
        speed: "normal",
        notes: {
          refundType,
          refundAccountDetails: refundType === 'new' ? refundAccountDetails : 'existing'
        }
      };
      const response = await fetch('http://localhost:8000/api/billing/refund', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (response.ok) {
        alert('Refund processed successfully!');
        setIsRefundModalOpen(false);
        setSelectedTxnForRefund(null);
        const fetchResponse = await fetch('http://localhost:8000/api/billing/transactions');
        if (fetchResponse.ok) {
           const data = await fetchResponse.json();
           if (data && data.items) setRazorpayTransactions(data.items);
        }
      } else {
        const errorData = await response.json();
        alert('Refund failed: ' + (errorData.detail || 'Unknown error'));
      }
    } catch (error) {
      console.error(error);
      alert('Error processing refund');
    }
  };

  const filteredLedgerTransactions = razorpayTransactions.filter(txn => {
    const query = searchQuery.toLowerCase();
    const idMatch = txn.id?.toLowerCase().includes(query) || false;
    const descMatch = (txn.description || (txn.notes && txn.notes.planName) || '').toLowerCase().includes(query);
    const orderMatch = txn.order_id?.toLowerCase().includes(query) || false;
    return idMatch || descMatch || orderMatch;
  });

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
      uidDisplay: `USR0${usersList.length + 1}`,
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

  const handleBlockUser = (id) => {
    setUsersList(usersList.map(u => u.id === id ? { ...u, status: u.status === 'Blocked' ? 'Active' : 'Blocked' } : u));
  };

  const handleDeleteUser = (id) => {
    setUserToDelete(id);
  };

  const confirmDeleteUser = () => {
    if (userToDelete) {
      setUsersList(usersList.filter(u => u.id !== userToDelete));
      setUserToDelete(null);
    }
  };

  const [usersCurrentPage, setUsersCurrentPage] = useState(1);
  const usersPerPage = 10;

  const filteredUsers = usersList.filter(u => {
    const matchesSearch =
      u.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      u.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
      u.id.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesFilter = userFilter === 'ALL' || u.status.toUpperCase() === userFilter;
    return matchesSearch && matchesFilter;
  });

  const totalUserPages = Math.ceil(filteredUsers.length / usersPerPage) || 1;
  const currentUsers = filteredUsers.slice((usersCurrentPage - 1) * usersPerPage, usersCurrentPage * usersPerPage);
  const handleExportAuditLogs = () => {
    let filteredLogs = auditLogs;
    if (auditLogStartDate) {
      filteredLogs = filteredLogs.filter(log => new Date(log.timestamp.split(' ')[0]) >= new Date(auditLogStartDate));
    }
    if (auditLogEndDate) {
      filteredLogs = filteredLogs.filter(log => new Date(log.timestamp.split(' ')[0]) <= new Date(auditLogEndDate));
    }
    
    if (filteredLogs.length === 0) {
      alert("No logs found in this date range.");
      return;
    }
    
    let csvContent = "data:text/csv;charset=utf-8,";
    csvContent += "ID,TIMESTAMP,EVENT TYPE,DESCRIPTION,USER,SEVERITY\n";
    filteredLogs.forEach(log => {
      const row = `${log.id},${log.timestamp},${log.type},"${log.event}",${log.user},${log.severity}`;
      csvContent += row + "\n";
    });
    
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `audit_logs_${new Date().toISOString().split('T')[0]}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const paginatedAuditLogs = useMemo(() => {
    let filteredLogs = auditLogs;
    if (auditLogStartDate) {
      filteredLogs = filteredLogs.filter(log => {
        const d = new Date(log.timestamp.split(' ')[0]);
        const s = new Date(auditLogStartDate);
        return d >= s;
      });
    }
    if (auditLogEndDate) {
      filteredLogs = filteredLogs.filter(log => {
        const d = new Date(log.timestamp.split(' ')[0]);
        const e = new Date(auditLogEndDate);
        return d <= e;
      });
    }
    const startIndex = (auditLogPage - 1) * logsPerPage;
    return filteredLogs.slice(startIndex, startIndex + logsPerPage);
  }, [auditLogs, auditLogPage, auditLogStartDate, auditLogEndDate, logsPerPage]);

  const totalAuditLogPages = Math.ceil(
    (auditLogs.filter(log => {
      let keep = true;
      const d = new Date(log.timestamp.split(' ')[0]);
      if (auditLogStartDate && d < new Date(auditLogStartDate)) keep = false;
      if (auditLogEndDate && d > new Date(auditLogEndDate)) keep = false;
      return keep;
    }).length) / logsPerPage
  ) || 1;

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
        <div
          className={`admin-nav-item ${activeTab === 'ledger' ? 'active' : ''}`}
          onClick={() => setActiveTab('ledger')}
          title="Payment Ledger"
        >
          <Database size={18} />
          <span>Payment Ledger</span>
        </div>
        <div
          className={`admin-nav-item ${activeTab === 'announcements' ? 'active' : ''}`}
          onClick={() => setActiveTab('announcements')}
          title="Announcements"
        >
          <Megaphone size={18} />
          <span>Announcements</span>
        </div>
        <div
          className={`admin-nav-item ${activeTab === 'subscriptions' ? 'active' : ''}`}
          onClick={() => setActiveTab('subscriptions')}
          title="Subscriptions"
        >
          <DollarSign size={18} />
          <span>Subscription Plans</span>
        </div>
        <div
          className={`admin-nav-item ${activeTab === 'coupons' ? 'active' : ''}`}
          onClick={() => setActiveTab('coupons')}
          title="Coupons"
        >
          <Ticket size={18} />
          <span>Coupons</span>
        </div>
        <div
          className={`admin-nav-item ${activeTab === 'grievances' ? 'active' : ''}`}
          onClick={() => setActiveTab('grievances')}
          title="Grievances & Support"
        >
          <MessageSquare size={18} />
          <span>Grievances</span>
        </div>
      </aside>

      {/* Main Container */}
      <main className="admin-main">
        {/* Top Header */}
        <header className="admin-header">
          <div className="admin-header-left">
          </div>

          <div className="admin-header-right">
            {/* System Maintenance Toggle */}
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                background: isMaintenanceMode ? 'rgba(239, 68, 68, 0.1)' : 'var(--dash-glass-bg)',
                border: `1px solid ${isMaintenanceMode ? 'rgba(239, 68, 68, 0.3)' : 'var(--dash-border)'}`,
                padding: '6px 12px',
                borderRadius: '20px',
                cursor: 'pointer',
                transition: 'all 0.3s ease'
              }}
              onClick={async () => {
                const newValue = !isMaintenanceMode;
                setIsMaintenanceMode(newValue);
                try {
                  await adminFirestoreWrite('settings', 'system', { maintenanceMode: newValue });
                } catch (e) {
                  console.error("Failed to update maintenance mode", e);
                }
              }}
              title="Toggle System Maintenance Mode"
            >
              <div
                style={{
                  width: '32px',
                  height: '18px',
                  borderRadius: '10px',
                  background: isMaintenanceMode ? '#ef4444' : 'var(--dash-border)',
                  position: 'relative',
                  transition: 'background 0.3s ease'
                }}
              >
                <div
                  style={{
                    width: '14px',
                    height: '14px',
                    borderRadius: '50%',
                    background: '#fff',
                    position: 'absolute',
                    top: '2px',
                    left: isMaintenanceMode ? '16px' : '2px',
                    transition: 'left 0.3s ease',
                    boxShadow: '0 2px 4px rgba(0,0,0,0.2)'
                  }}
                />
              </div>
              <span style={{
                fontSize: '12px',
                fontWeight: '700',
                color: isMaintenanceMode ? '#ef4444' : 'var(--dash-text-muted)'
              }}>
                {isMaintenanceMode ? 'Maintenance ON' : 'Maintenance OFF'}
              </span>
            </div>

            <button
              className="admin-header-btn"
              onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
              title="Toggle Theme"
            >
              {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
            </button>

            <div style={{ position: 'relative' }}>
              <button 
                className="admin-header-btn" 
                title="Notifications"
                onClick={() => setIsNotificationsOpen(!isNotificationsOpen)}
              >
                <Bell size={18} />
                {notifications.length > 0 && (
                  <span style={{
                    position: 'absolute', top: '4px', right: '4px',
                    width: '8px', height: '8px', background: '#ef4444', borderRadius: '50%'
                  }} />
                )}
              </button>

              {isNotificationsOpen && (
                <div style={{
                  position: 'absolute', top: '100%', right: '0',
                  marginTop: '10px', width: '350px', background: 'var(--dash-bg)',
                  border: '1px solid var(--dash-border)', borderRadius: '12px',
                  boxShadow: '0 10px 25px rgba(0,0,0,0.2)', zIndex: 100,
                  display: 'flex', flexDirection: 'column', overflow: 'hidden'
                }}>
                  <div style={{ padding: '16px', borderBottom: '1px solid var(--dash-border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <h3 style={{ margin: 0, fontSize: '15px', fontWeight: '700' }}>Notifications</h3>
                    <span className="admin-badge admin-badge-warning">{notifications.length} New</span>
                  </div>
                  
                  <div style={{ maxHeight: showAllNotifications ? '400px' : '300px', overflowY: 'auto' }}>
                    {notifications.length > 0 ? (
                      (showAllNotifications ? notifications : notifications.slice(0, 4)).map(n => (
                        <div key={n.id} style={{ 
                          padding: '14px 16px', borderBottom: '1px solid var(--dash-border)',
                          cursor: 'pointer', display: 'flex', gap: '12px', alignItems: 'flex-start'
                        }}
                        onClick={() => {
                          setIsNotificationsOpen(false);
                          setActiveTab(n.type === 'grievance' ? 'grievances' : 'audit_logs');
                        }}
                        onMouseEnter={(e) => e.currentTarget.style.background = 'var(--dash-glass-bg)'}
                        onMouseLeave={(e) => e.currentTarget.style.background = 'transparent'}
                        >
                          <div style={{ 
                            padding: '8px', borderRadius: '8px',
                            background: n.type === 'grievance' ? 'rgba(59, 130, 246, 0.1)' : 'rgba(239, 68, 68, 0.1)',
                            color: n.type === 'grievance' ? '#3b82f6' : '#ef4444'
                          }}>
                            {n.type === 'grievance' ? <MessageSquare size={16} /> : <ShieldAlert size={16} />}
                          </div>
                          <div>
                            <div style={{ fontSize: '13px', fontWeight: '600', color: 'var(--dash-text)', marginBottom: '4px' }}>
                              {n.title}
                            </div>
                            <div style={{ fontSize: '12px', color: 'var(--dash-text-muted)', lineHeight: '1.4' }}>
                              {n.message.length > 80 ? n.message.substring(0, 80) + '...' : n.message}
                            </div>
                            <div style={{ fontSize: '11px', color: 'var(--dash-text-muted)', marginTop: '6px', opacity: 0.7 }}>
                              {n.date.toLocaleString()}
                            </div>
                          </div>
                        </div>
                      ))
                    ) : (
                      <div style={{ padding: '24px', textAlign: 'center', color: 'var(--dash-text-muted)', fontSize: '13px' }}>
                        No new notifications
                      </div>
                    )}
                  </div>
                  
                  {notifications.length > 4 && !showAllNotifications && (
                    <div 
                      style={{ padding: '12px', textAlign: 'center', borderTop: '1px solid var(--dash-border)', background: 'var(--dash-glass-bg)', cursor: 'pointer', fontSize: '13px', fontWeight: '600', color: '#10b981' }}
                      onClick={() => setShowAllNotifications(true)}
                    >
                      View All Notifications
                    </div>
                  )}
                </div>
              )}
            </div>

            <div className="admin-profile-pill" title="Admin Profile">
              {currentUser?.photoURL ? (
                <img
                  src={currentUser.photoURL}
                  alt="Profile"
                  className="admin-profile-avatar"
                  style={{ padding: 0, objectFit: 'cover', background: 'transparent' }}
                />
              ) : (
                <div className="admin-profile-avatar">
                  {currentUser?.displayName ? currentUser.displayName.charAt(0).toUpperCase() : 'A'}
                </div>
              )}
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                <span style={{ fontSize: '12.5px', fontWeight: '700', lineHeight: '1.2' }}>{currentUser?.email || 'admin@ledger.com'}</span>
                <span style={{ fontSize: '10.5px', color: '#10b981', fontWeight: '600' }}>Super Admin • Active</span>
              </div>
            </div>
            
            <button 
              className="admin-header-btn" 
              style={{ background: 'rgba(239, 68, 68, 0.15)', color: '#ef4444', border: '1px solid rgba(239, 68, 68, 0.3)', borderRadius: '50%' }} 
              onClick={onLogout} 
              title="Sign out Admin"
            >
              <LogOut size={18} />
            </button>
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
                  <div className="admin-stat-value">{aiMetrics.successRate}</div>
                  <div className="admin-stat-footer" style={{ color: '#f59e0b' }}>
                    <span>{aiMetrics.totalRequests} PDF statements processed</span>
                  </div>
                </div>

                <div className="admin-stat-card">
                  <div className="admin-stat-label">
                    <span>API & SYSTEM LATENCY</span>
                    <Server size={18} color="#10b981" />
                  </div>
                  <div className="admin-stat-value">{aiMetrics.avgProcessingTime}</div>
                  <div className="admin-stat-footer" style={{ color: '#10b981' }}>
                    <CheckCircle2 size={14} />
                    <span>{aiMetrics.successRate === '--%' ? '0.00' : (100 - parseFloat(aiMetrics.successRate)).toFixed(2)}% Error Rate (24h avg)</span>
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
                      <span className={`admin-badge ${infraStatus.gemini.class}`}>{infraStatus.gemini.label}</span>
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px', borderRadius: '12px', background: 'var(--dash-glass-bg)', border: '1px solid var(--dash-border)' }}>
                      <div>
                        <div style={{ fontSize: '14px', fontWeight: '700' }}>Spring Boot Transaction Gateway</div>
                        <div style={{ fontSize: '12px', color: 'var(--dash-text-muted)' }}>Backend SQL & Firestore Engine (Port 8000)</div>
                      </div>
                      <span className={`admin-badge ${infraStatus.springBoot.class}`}>{infraStatus.springBoot.label}</span>
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px', borderRadius: '12px', background: 'var(--dash-glass-bg)', border: '1px solid var(--dash-border)' }}>
                      <div>
                        <div style={{ fontSize: '14px', fontWeight: '700' }}>PDF OCR Layout Decoder</div>
                        <div style={{ fontSize: '12px', color: 'var(--dash-text-muted)' }}>Column-Header Proximity Engine</div>
                      </div>
                      <span className={`admin-badge ${infraStatus.ocr.class}`}>{infraStatus.ocr.label}</span>
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
                  <p style={{ margin: '8px 0 0', color: '#10b981', fontSize: '14px', fontWeight: '700' }}>
                    Total AI Tokens Used: {totalTokensUsed} Tokens
                  </p>
                </div>

                <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                  <div className="admin-search-box" style={{ background: 'var(--dash-bg)', border: '1px solid var(--dash-border)', borderRadius: '10px', padding: '6px 12px', display: 'flex', alignItems: 'center' }}>
                    <Search size={14} color="var(--dash-text-muted)" />
                    <input
                      type="text"
                      placeholder="Search by name or UID..."
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      style={{ border: 'none', background: 'transparent', outline: 'none', color: 'var(--dash-text)', fontSize: '13px', marginLeft: '8px', width: '180px' }}
                    />
                  </div>
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
                    <option value="BLOCKED">Blocked Users</option>
                  </select>

                  <button className="admin-btn-primary" onClick={() => setIsAddUserModalOpen(true)}>
                    + Invite New User
                  </button>
                </div>
              </div>

              <div className="admin-table-wrapper" style={{ maxHeight: '600px', overflowY: 'auto' }}>
                <table className="admin-table">
                  <thead style={{ position: 'sticky', top: 0, zIndex: 1, background: 'var(--dash-bg)' }}>
                    <tr>
                      <th>USER PROFILE</th>
                      <th>UID</th>
                      <th>SUBSCRIPTION</th>
                      <th>STATUS</th>
                      <th>AI TOKENS USED</th>
                      <th>LAST LOGIN</th>
                      <th style={{ textAlign: 'right' }}>ACTIONS</th>
                    </tr>
                  </thead>
                  <tbody>
                    {currentUsers.map(user => (
                      <tr key={user.id}>
                        <td>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                            {user.photoURL ? (
                              <img
                                src={user.photoURL}
                                alt={user.name}
                                style={{
                                  width: '36px',
                                  height: '36px',
                                  borderRadius: '50%',
                                  objectFit: 'cover'
                                }}
                              />
                            ) : (
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
                                {user.name ? user.name.charAt(0).toUpperCase() : 'U'}
                              </div>
                            )}
                            <div>
                              <div style={{ fontWeight: '700', color: 'var(--dash-text)' }}>{user.name}</div>
                              <div style={{ fontSize: '12px', color: 'var(--dash-text-muted)' }}>{user.email}</div>
                            </div>
                          </div>
                        </td>
                        <td style={{ fontWeight: '600', color: 'var(--dash-text-muted)', fontSize: '12px' }}>{user.uidDisplay || user.id}</td>
                        <td>
                          <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                            <div style={{ fontWeight: '600', color: 'var(--dash-text)' }}>{user.tier}</div>
                            <button className="admin-action-btn" style={{ fontSize: '11px', padding: '4px 8px', width: 'fit-content', border: '1px solid var(--dash-border)' }} onClick={() => alert('Downloading invoice PDF...')}>
                              Download Invoice (PDF)
                            </button>
                          </div>
                        </td>
                        <td>
                          <span className={`admin-badge ${user.status === 'Active' ? 'admin-badge-success' :
                              user.status === 'Blocked' ? 'admin-badge-danger' :
                                user.status === 'Flagged' ? 'admin-badge-warning' : 'admin-badge-info'
                            }`}>
                            {user.status}
                          </span>
                        </td>
                        <td style={{ fontWeight: '500' }}>{user.aiTokensUsed} Tokens</td>
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
                              onClick={() => handleBlockUser(user.id)}
                              style={{ color: user.status === 'Blocked' ? '#10b981' : '#f59e0b' }}
                              title={user.status === 'Blocked' ? "Unblock User" : "Block User"}
                            >
                              <Lock size={14} />
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
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '20px' }}>
                <div style={{ fontSize: '13px', color: 'var(--dash-text-muted)' }}>
                  Showing {filteredUsers.length === 0 ? 0 : (usersCurrentPage - 1) * usersPerPage + 1} to {Math.min(usersCurrentPage * usersPerPage, filteredUsers.length)} of {filteredUsers.length} Users
                </div>
                <div style={{ display: 'flex', gap: '8px' }}>
                  <button
                    disabled={usersCurrentPage === 1}
                    onClick={() => setUsersCurrentPage(prev => Math.max(prev - 1, 1))}
                    style={{ padding: '6px 14px', borderRadius: '8px', background: usersCurrentPage === 1 ? 'transparent' : 'var(--dash-glass-bg)', color: usersCurrentPage === 1 ? 'var(--dash-text-muted)' : 'var(--dash-text)', border: '1px solid var(--dash-border)', cursor: usersCurrentPage === 1 ? 'not-allowed' : 'pointer', fontWeight: '600', transition: 'all 0.2s' }}
                  >
                    Previous
                  </button>
                  <span style={{ display: 'flex', alignItems: 'center', padding: '0 10px', fontSize: '13px', fontWeight: '600', color: 'var(--dash-text)' }}>
                    Page {usersCurrentPage} of {totalUserPages}
                  </span>
                  <button
                    disabled={usersCurrentPage === totalUserPages}
                    onClick={() => setUsersCurrentPage(prev => Math.min(prev + 1, totalUserPages))}
                    style={{ padding: '6px 14px', borderRadius: '8px', background: usersCurrentPage === totalUserPages ? 'transparent' : 'var(--dash-glass-bg)', color: usersCurrentPage === totalUserPages ? 'var(--dash-text-muted)' : 'var(--dash-text)', border: '1px solid var(--dash-border)', cursor: usersCurrentPage === totalUserPages ? 'not-allowed' : 'pointer', fontWeight: '600', transition: 'all 0.2s' }}
                  >
                    Next
                  </button>
                </div>
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
                    <div className="admin-stat-value">{aiMetrics.modelEngine}</div>
                    <div className="admin-stat-footer" style={{ color: '#10b981' }}>
                      <span>Temperature: 0.1 • Layout-Aware OCR</span>
                    </div>
                  </div>

                  <div className="admin-stat-card">
                    <div className="admin-stat-label">
                      <span>AVG PROCESSING TIME</span>
                      <Activity size={18} color="#6366f1" />
                    </div>
                    <div className="admin-stat-value">{aiMetrics.avgProcessingTime}</div>
                    <div className="admin-stat-footer" style={{ color: '#6366f1' }}>
                      <span>PDF column headers aligned per row</span>
                    </div>
                  </div>

                  <div className="admin-stat-card">
                    <div className="admin-stat-label">
                      <span>SUCCESS RATE</span>
                      <CheckCircle2 size={18} color="#10b981" />
                    </div>
                    <div className="admin-stat-value">{aiMetrics.successRate}</div>
                    <div className="admin-stat-footer" style={{ color: '#10b981' }}>
                      <span>Total Requests: {aiMetrics.totalRequests}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* AUDIT LOGS TAB */}
          {activeTab === 'audit_logs' && (
            <div className="admin-card">
              <div style={{ display: 'flex', flexDirection: 'column', gap: '20px', marginBottom: '24px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '16px' }}>
                  <div>
                    <h2 style={{ margin: 0, fontSize: '24px', fontWeight: '800', letterSpacing: '-0.5px' }}>Security & Ledger Audit Logs</h2>
                    <p style={{ margin: '6px 0 0', color: 'var(--dash-text-muted)', fontSize: '14px', maxWidth: '600px', lineHeight: '1.5' }}>
                      Immutable trace of all administrative logins, system overrides, and high-value transactions.
                    </p>
                  </div>
                  <button className="admin-action-btn" onClick={() => alert('Refreshing live audit feed...')} style={{ display: 'flex', alignItems: 'center', gap: '6px', padding: '8px 14px', borderRadius: '10px' }}>
                    <RefreshCw size={14} /> Refresh Feed
                  </button>
                </div>

                <div style={{ 
                  display: 'flex', 
                  gap: '16px', 
                  alignItems: 'center', 
                  background: 'var(--dash-glass-bg)', 
                  padding: '16px 20px', 
                  borderRadius: '16px', 
                  border: '1px solid var(--dash-border)',
                  flexWrap: 'wrap',
                  justifyContent: 'space-between'
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '16px', flexWrap: 'wrap' }}>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                      <label style={{ fontSize: '11px', fontWeight: '700', color: 'var(--dash-text-muted)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Start Date</label>
                      <input 
                        type="date" 
                        value={auditLogStartDate} 
                        onChange={(e) => {setAuditLogStartDate(e.target.value); setAuditLogPage(1);}}
                        style={{ padding: '10px 14px', borderRadius: '10px', border: '1px solid var(--dash-border)', background: 'var(--dash-bg)', color: 'var(--dash-text)', outline: 'none', colorScheme: theme, fontSize: '14px', fontWeight: '500', fontFamily: 'inherit' }}
                      />
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                      <label style={{ fontSize: '11px', fontWeight: '700', color: 'var(--dash-text-muted)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>End Date</label>
                      <input 
                        type="date" 
                        value={auditLogEndDate} 
                        onChange={(e) => {setAuditLogEndDate(e.target.value); setAuditLogPage(1);}}
                        style={{ padding: '10px 14px', borderRadius: '10px', border: '1px solid var(--dash-border)', background: 'var(--dash-bg)', color: 'var(--dash-text)', outline: 'none', colorScheme: theme, fontSize: '14px', fontWeight: '500', fontFamily: 'inherit' }}
                      />
                    </div>
                  </div>
                  
                  <button 
                    onClick={handleExportAuditLogs}
                    style={{ 
                      padding: '10px 20px', 
                      display: 'flex', 
                      alignItems: 'center', 
                      gap: '8px', 
                      background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
                      boxShadow: '0 4px 12px rgba(16, 185, 129, 0.25)',
                      border: 'none',
                      color: 'white',
                      fontWeight: '600',
                      borderRadius: '10px',
                      cursor: 'pointer',
                      transition: 'all 0.2s ease',
                      fontSize: '14px'
                    }}
                    onMouseEnter={(e) => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 6px 16px rgba(16, 185, 129, 0.35)'; }}
                    onMouseLeave={(e) => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 4px 12px rgba(16, 185, 129, 0.25)'; }}
                  >
                    <Download size={16} /> Export Report
                  </button>
                </div>
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
                    {paginatedAuditLogs.map(log => (
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
                    {paginatedAuditLogs.length === 0 && (
                      <tr>
                        <td colSpan="5" style={{ textAlign: 'center', padding: '24px', color: 'var(--dash-text-muted)' }}>
                          No audit logs found for the selected dates.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>

              {/* Pagination Controls */}
              {totalAuditLogPages > 1 && (
                <div style={{ display: 'flex', justifyContent: 'center', marginTop: '20px', gap: '8px' }}>
                  {Array.from({ length: totalAuditLogPages }, (_, i) => i + 1).map(page => (
                    <button
                      key={page}
                      onClick={() => setAuditLogPage(page)}
                      style={{
                        padding: '6px 12px',
                        borderRadius: '8px',
                        border: '1px solid var(--dash-border)',
                        background: auditLogPage === page ? '#10b981' : 'var(--dash-bg)',
                        color: auditLogPage === page ? '#fff' : 'var(--dash-text)',
                        fontWeight: '700',
                        cursor: 'pointer',
                        transition: 'all 0.2s ease'
                      }}
                    >
                      {page}
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* LEDGER TAB */}
          {activeTab === 'ledger' && (
            <div className="admin-card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <div>
                  <h2 style={{ margin: 0, fontSize: '22px', fontWeight: '800' }}>Payment Ledger</h2>
                  <p style={{ margin: '4px 0 0', color: 'var(--dash-text-muted)', fontSize: '13.5px' }}>
                    Centralized payment ledger for all enterprise and platform transactions.
                  </p>
                </div>
                <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                  <div className="admin-search-box" style={{ background: 'var(--dash-bg)', border: '1px solid var(--dash-border)', borderRadius: '10px', padding: '6px 12px', display: 'flex', alignItems: 'center' }}>
                    <Search size={14} color="var(--dash-text-muted)" />
                    <input
                      type="text"
                      placeholder="Search transactions..."
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      style={{ border: 'none', background: 'transparent', outline: 'none', color: 'var(--dash-text)', fontSize: '13px', marginLeft: '8px', width: '220px' }}
                    />
                  </div>
                  <button className="admin-action-btn" onClick={() => alert('Exporting Ledger as CSV...')}>
                    Export CSV
                  </button>
                  <button className="admin-btn-primary" onClick={() => alert('Add Manual Entry feature coming soon')}>
                    + Manual Entry
                  </button>
                </div>
              </div>

              <div className="admin-table-wrapper">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>TXN ID</th>
                      <th>DATE</th>
                      <th>DESCRIPTION</th>
                      <th>REF / ORDER ID</th>
                      <th>AMOUNT</th>
                      <th>STATUS</th>
                      <th>ACTIONS</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredLedgerTransactions.length > 0 ? filteredLedgerTransactions.map(txn => {
                      const amount = (txn.amount / 100).toLocaleString();
                      const dateObj = new Date(txn.created_at * 1000);
                      const dateStr = !isNaN(dateObj.getTime()) ? dateObj.toISOString().split('T')[0] : 'N/A';
                      const desc = txn.description || (txn.notes && txn.notes.planName) || 'Razorpay Transaction';
                      const ref = txn.order_id || 'N/A';
                      const status = txn.status === 'captured' ? 'Completed' : (txn.status === 'refunded' ? 'Refunded' : (txn.status === 'failed' ? 'Failed' : 'Pending'));
                      return (
                      <tr key={txn.id}>
                        <td style={{ fontWeight: '600', color: 'var(--dash-text-muted)', fontSize: '12px' }}>{txn.id}</td>
                        <td style={{ color: 'var(--dash-text-muted)', fontSize: '13px' }}>{dateStr}</td>
                        <td style={{ fontWeight: '700', color: 'var(--dash-text)' }}>{desc}</td>
                        <td style={{ color: 'var(--dash-text-muted)', fontSize: '13px' }}>{ref}</td>
                        <td style={{
                          fontWeight: '700',
                          color: '#10b981'
                        }}>
                          ₹{amount}
                        </td>
                        <td>
                          <span className={`admin-badge ${status === 'Completed' ? 'admin-badge-success' :
                              status === 'Refunded' ? 'admin-badge-info' :
                              status === 'Pending' ? 'admin-badge-warning' : 'admin-badge-danger'
                            }`}>
                            {status}
                          </span>
                        </td>
                        <td>
                          {txn.status === 'captured' && (
                            <button
                              className="admin-action-btn"
                              style={{ color: '#ef4444', border: '1px solid rgba(239, 68, 68, 0.3)' }}
                              onClick={() => {
                                setSelectedTxnForRefund(txn);
                                setIsRefundModalOpen(true);
                              }}
                            >
                              Refund
                            </button>
                          )}
                        </td>
                      </tr>
                    )}) : (
                      <tr>
                        <td colSpan="7" style={{ textAlign: 'center', padding: '24px', color: 'var(--dash-text-muted)' }}>
                          No transactions found.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* ANNOUNCEMENTS TAB */}
          {activeTab === 'announcements' && (
            <div className="admin-card" style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <div>
                <h2 style={{ margin: 0, fontSize: '22px', fontWeight: '800' }}>Platform Announcements</h2>
                <p style={{ margin: '4px 0 0', color: 'var(--dash-text-muted)', fontSize: '13.5px' }}>
                  Send targeted alerts, updates, or advertisements to users. They will receive it as a live notification.
                </p>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <label style={{ fontSize: '13px', fontWeight: '700', color: 'var(--dash-text-muted)' }}>SELECT TARGET AUDIENCE</label>
                <div style={{ display: 'flex', gap: '16px', marginBottom: '8px' }}>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                    <input type="radio" name="targetType" checked={announcementTargetType === 'ALL'} onChange={() => setAnnouncementTargetType('ALL')} />
                    All Users (Global Broadcast)
                  </label>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                    <input type="radio" name="targetType" checked={announcementTargetType === 'SPECIFIC'} onChange={() => setAnnouncementTargetType('SPECIFIC')} />
                    Specific User
                  </label>
                </div>
                
                {announcementTargetType === 'SPECIFIC' && (
                  <div style={{ position: 'relative', maxWidth: '400px' }}>
                    {announcementSelectedUser ? (
                      <div style={{ padding: '12px', borderRadius: '10px', background: 'rgba(99, 102, 241, 0.1)', border: '1px solid #6366f1', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                          <div style={{ fontWeight: '600', color: 'var(--dash-text)' }}>{announcementSelectedUser.name}</div>
                          <div style={{ fontSize: '12px', color: 'var(--dash-text-muted)' }}>{announcementSelectedUser.email}</div>
                        </div>
                        <button className="admin-action-btn" onClick={() => setAnnouncementSelectedUser(null)}>
                          <Trash2 size={14} color="#ef4444" />
                        </button>
                      </div>
                    ) : (
                      <>
                        <div className="admin-search-box" style={{ background: 'var(--dash-bg)', border: '1px solid var(--dash-border)', borderRadius: '10px', padding: '10px 12px', display: 'flex', alignItems: 'center' }}>
                          <Search size={16} color="var(--dash-text-muted)" />
                          <input
                            type="text"
                            placeholder="Search by name, email or UID..."
                            value={announcementUserSearch}
                            onChange={(e) => setAnnouncementUserSearch(e.target.value)}
                            style={{ border: 'none', background: 'transparent', outline: 'none', color: 'var(--dash-text)', marginLeft: '8px', width: '100%' }}
                          />
                        </div>
                        {announcementSearchResults.length > 0 && (
                          <div style={{ position: 'absolute', top: '100%', left: 0, right: 0, background: theme === 'dark' ? '#1e293b' : '#ffffff', border: '1px solid var(--dash-border)', borderRadius: '10px', marginTop: '4px', zIndex: 50, maxHeight: '200px', overflowY: 'auto', boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)' }}>
                            {announcementSearchResults.map(user => (
                              <div 
                                key={user.id}
                                style={{ padding: '10px 12px', borderBottom: '1px solid var(--dash-border)', cursor: 'pointer', display: 'flex', flexDirection: 'column' }}
                                onClick={() => {
                                  setAnnouncementSelectedUser(user);
                                  setAnnouncementUserSearch('');
                                  setAnnouncementSearchResults([]);
                                }}
                                onMouseEnter={(e) => e.currentTarget.style.background = 'var(--dash-bg)'}
                                onMouseLeave={(e) => e.currentTarget.style.background = 'transparent'}
                              >
                                <span style={{ fontWeight: '600' }}>{user.name}</span>
                                <span style={{ fontSize: '12px', color: 'var(--dash-text-muted)' }}>{user.email} (UID: {user.uidDisplay})</span>
                              </div>
                            ))}
                          </div>
                        )}
                      </>
                    )}
                  </div>
                )}
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <label style={{ fontSize: '13px', fontWeight: '700', color: 'var(--dash-text-muted)' }}>MESSAGE CONTENT</label>
                <div style={{ background: '#fff', borderRadius: '10px', color: '#000' }}>
                  <ReactQuill 
                    ref={quillRef}
                    theme="snow" 
                    value={announcementContent} 
                    onChange={setAnnouncementContent} 
                    modules={quillModules}
                    style={{ height: '300px', display: 'flex', flexDirection: 'column' }}
                  />
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '40px' }}>
                <button className="admin-btn-primary" onClick={handleSendAnnouncement}>
                  <Megaphone size={16} /> Send Announcement
                </button>
              </div>
            </div>
          )}

          {/* GRIEVANCES TAB */}
          {activeTab === 'grievances' && (
            <div className="admin-card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <div>
                  <h2 style={{ margin: 0, fontSize: '22px', fontWeight: '800' }}>User Grievances</h2>
                  <p style={{ margin: '4px 0 0', color: 'var(--dash-text-muted)', fontSize: '13.5px' }}>
                    Live messages and support tickets from users requiring attention.
                  </p>
                </div>
              </div>

              <div className="admin-table-wrapper">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>UID</th>
                      <th>USER DETAILS</th>
                      <th>MESSAGE</th>
                      <th>STATUS</th>
                      <th>ACTIONS</th>
                    </tr>
                  </thead>
                  <tbody>
                    {grievancesList.length > 0 ? grievancesList.map(g => (
                      <tr key={g.id}>
                        <td style={{ fontWeight: '600', color: 'var(--dash-text-muted)', fontSize: '12px' }}>{(g.uid || g.userId || '000000').substring(0, 6).toUpperCase()}</td>
                        <td>
                          <div style={{ display: 'flex', flexDirection: 'column' }}>
                            <span style={{ fontWeight: '700', color: 'var(--dash-text)' }}>{g.name || g.userName || 'Unknown'}</span>
                            {(g.email) && <span style={{ fontSize: '12px', color: 'var(--dash-text-muted)' }}>{g.email}</span>}
                            {(g.phone) && <span style={{ fontSize: '12px', color: 'var(--dash-text-muted)' }}>{g.phone}</span>}
                          </div>
                        </td>
                        <td style={{ maxWidth: '300px' }}>
                          <div style={{ margin: 0, fontSize: '13px', lineHeight: '1.4', color: 'var(--dash-text)', wordWrap: 'break-word' }}>
                            {g.type && <div style={{ fontSize: '11px', fontWeight: 'bold', color: '#10b981', marginBottom: '4px' }}>{(g.actionType || g.type).replace('_', ' ').toUpperCase()}</div>}
                            <ExpandableMessage text={g.message || g.details || 'No message provided.'} />
                          </div>
                        </td>
                        <td>
                          <span className={`admin-badge ${g.status === 'Resolved' ? 'admin-badge-success' : 'admin-badge-warning'}`}>
                            {g.status}
                          </span>
                        </td>
                        <td>
                          {g.status !== 'Resolved' && (
                            <button
                              className="admin-action-btn"
                              style={{ color: '#10b981', border: '1px solid rgba(16, 185, 129, 0.3)' }}
                              onClick={() => handleResolveGrievance(g.id)}
                            >
                              Mark as Resolved
                            </button>
                          )}
                        </td>
                      </tr>
                    )) : (
                      <tr>
                        <td colSpan="5" style={{ textAlign: 'center', padding: '24px', color: 'var(--dash-text-muted)' }}>
                          No grievances found.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* SUBSCRIPTIONS TAB */}
          {activeTab === 'subscriptions' && (
            <div className="admin-card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <div>
                  <h2 style={{ margin: 0, fontSize: '22px', fontWeight: '800' }}>Subscription Plans</h2>
                  <p style={{ margin: '4px 0 0', color: 'var(--dash-text-muted)', fontSize: '13.5px' }}>
                    Manage and create subscription tiers for your users.
                  </p>
                </div>
                <button className="admin-btn-primary" onClick={openCreateModal}>
                  + Create Subscription Tier
                </button>
              </div>

              <div className="admin-table-wrapper">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>PLAN ID</th>
                      <th>TITLE</th>
                      <th>DURATION</th>
                      <th>AMOUNT</th>
                      <th>TRIAL / AUTOPAY</th>
                      <th>STATUS</th>
                      <th>ACTIONS</th>
                    </tr>
                  </thead>
                  <tbody>
                    {subscriptionsList.length > 0 ? subscriptionsList.map(sub => (
                      <tr key={sub.id}>
                        <td style={{ fontWeight: '600', color: 'var(--dash-text-muted)', fontSize: '12px' }}>{sub.id}</td>
                        <td style={{ fontWeight: '700', color: 'var(--dash-text)' }}>{sub.title}</td>
                        <td>{sub.duration}</td>
                        <td style={{ fontWeight: '700', color: '#10b981' }}>{sub.currency === 'INR' ? '₹' : (sub.currency === 'GBP' ? '£' : (sub.currency === 'EUR' ? '€' : '$'))}{sub.amount}</td>
                        <td>
                          <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', alignItems: 'flex-start' }}>
                            <span className={`admin-badge ${sub.autopay ? 'admin-badge-success' : 'admin-badge-warning'}`}>
                              Autopay: {sub.autopay ? 'On' : 'Off'}
                            </span>
                            {sub.trialEnabled && (
                              <span className="admin-badge" style={{ background: 'rgba(59, 130, 246, 0.1)', color: '#3b82f6', border: '1px solid rgba(59, 130, 246, 0.2)' }}>
                                {sub.trialDays} Days Trial
                              </span>
                            )}
                          </div>
                        </td>
                        <td>
                          <span className="admin-badge admin-badge-success">{sub.status}</span>
                        </td>
                        <td>
                          <div style={{ display: 'flex', gap: '8px' }}>
                            <button
                              className="admin-action-btn"
                              title="Edit Tier"
                              onClick={() => openEditModal(sub)}
                            >
                              <Edit2 size={14} />
                            </button>
                            <button
                              className="admin-action-btn"
                              title="Delete Tier"
                              style={{ color: '#ef4444', border: '1px solid rgba(239, 68, 68, 0.3)' }}
                              onClick={() => handleDeleteSubscription(sub.id)}
                            >
                              <Trash2 size={14} />
                            </button>
                          </div>
                        </td>
                      </tr>
                    )) : (
                      <tr>
                        <td colSpan="7" style={{ textAlign: 'center', padding: '24px', color: 'var(--dash-text-muted)' }}>
                          No subscription plans found.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* COUPONS TAB */}
          {activeTab === 'coupons' && (
            <div className="admin-card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <div>
                  <h2 style={{ margin: 0, fontSize: '22px', fontWeight: '800' }}>Coupons Management</h2>
                  <p style={{ margin: '4px 0 0', color: 'var(--dash-text-muted)', fontSize: '13.5px' }}>
                    Create and manage discount coupons for users.
                  </p>
                </div>
                <button className="admin-btn-primary" onClick={openCreateCouponModal}>
                  + Create Coupon
                </button>
              </div>

              <div className="admin-table-wrapper">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>CODE</th>
                      <th>DISCOUNT</th>
                      <th>SCOPE</th>
                      <th>EXPIRY DATE</th>
                      <th>STATUS</th>
                      <th>ACTIONS</th>
                    </tr>
                  </thead>
                  <tbody>
                    {couponsList.length > 0 ? couponsList.map(coupon => (
                      <tr key={coupon.id}>
                        <td style={{ fontWeight: '700', color: 'var(--dash-text)' }}>{coupon.code}</td>
                        <td style={{ fontWeight: '700', color: '#10b981' }}>
                          {coupon.discountType === 'Percentage' ? `${coupon.discountValue}%` : `$${coupon.discountValue}`}
                        </td>
                        <td>
                          {coupon.targetType === 'All' ? (
                            <span className="admin-badge admin-badge-info">Global</span>
                          ) : (
                            <span className="admin-badge admin-badge-warning">{coupon.targetUser}</span>
                          )}
                        </td>
                        <td>{new Date(coupon.expiryDate).toLocaleString()}</td>
                        <td>
                          <span className={`admin-badge ${new Date(coupon.expiryDate) > new Date() ? 'admin-badge-success' : 'admin-badge-danger'}`}>
                            {new Date(coupon.expiryDate) > new Date() ? 'Active' : 'Expired'}
                          </span>
                        </td>
                        <td>
                          <div style={{ display: 'flex', gap: '8px' }}>
                            <button
                              className="admin-action-btn"
                              title="Edit Coupon"
                              onClick={() => openEditCouponModal(coupon)}
                            >
                              <Edit2 size={14} />
                            </button>
                          </div>
                        </td>
                      </tr>
                    )) : (
                      <tr>
                        <td colSpan="6" style={{ textAlign: 'center', padding: '24px', color: 'var(--dash-text-muted)' }}>
                          No coupons found.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
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
                <span style={{ fontWeight: '700' }}>{selectedUserForInspect.ledgerAccounts || 0} active</span>
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

      {/* Refund Modal */}
      {isRefundModalOpen && (
        <div className="admin-modal-overlay" onClick={() => setIsRefundModalOpen(false)}>
          <div className="admin-modal-card" onClick={(e) => e.stopPropagation()}>
            <h3 style={{ margin: '0 0 6px', fontSize: '20px', fontWeight: '800' }}>Process Refund</h3>
            <p style={{ margin: '0 0 20px', color: 'var(--dash-text-muted)', fontSize: '13.5px' }}>
              Select where the refund should be deposited.
            </p>

            <form onSubmit={handleRefundSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: 'var(--dash-text-muted)', marginBottom: '6px' }}>REFUND TO</label>
                <select
                  value={refundType}
                  onChange={(e) => setRefundType(e.target.value)}
                  style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                >
                  <option value="existing">Existing Account (Original Source)</option>
                  <option value="new">Add New Account (UPI / Bank Details)</option>
                </select>
              </div>

              {refundType === 'new' && (
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: 'var(--dash-text-muted)', marginBottom: '6px' }}>NEW ACCOUNT DETAILS</label>
                  <input
                    type="text"
                    required
                    placeholder="Enter UPI ID or Bank Account No"
                    value={refundAccountDetails}
                    onChange={(e) => setRefundAccountDetails(e.target.value)}
                    style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                  />
                </div>
              )}

              <div style={{ display: 'flex', gap: '12px', marginTop: '16px' }}>
                <button
                  type="button"
                  className="admin-action-btn"
                  style={{ flex: 1, padding: '12px' }}
                  onClick={() => setIsRefundModalOpen(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="admin-btn-primary"
                  style={{ flex: 1, padding: '12px', background: '#ef4444', borderColor: '#ef4444' }}
                >
                  Confirm Refund
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Custom Link Modal */}
      {isLinkModalOpen && (
        <div className="admin-modal-overlay" onClick={() => setIsLinkModalOpen(false)}>
          <div className="admin-modal-card" onClick={(e) => e.stopPropagation()}>
            <h3 style={{ margin: '0 0 6px', fontSize: '20px', fontWeight: '800' }}>Insert Link</h3>
            <p style={{ margin: '0 0 20px', color: 'var(--dash-text-muted)', fontSize: '13.5px' }}>
              Add a hyperlink to the announcement content.
            </p>

            <form onSubmit={handleLinkSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: 'var(--dash-text-muted)', marginBottom: '6px' }}>TEXT TO DISPLAY</label>
                <input
                  type="text"
                  placeholder="e.g. Click Here"
                  value={linkText}
                  onChange={(e) => setLinkText(e.target.value)}
                  style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                />
              </div>
              
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: 'var(--dash-text-muted)', marginBottom: '6px' }}>URL</label>
                <input
                  type="url"
                  required
                  placeholder="e.g. https://example.com"
                  value={linkUrl}
                  onChange={(e) => setLinkUrl(e.target.value)}
                  style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                />
              </div>

              <div style={{ display: 'flex', gap: '12px', marginTop: '16px' }}>
                <button
                  type="button"
                  className="admin-action-btn"
                  style={{ flex: 1, padding: '12px' }}
                  onClick={() => setIsLinkModalOpen(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="admin-btn-primary"
                  style={{ flex: 1, padding: '12px' }}
                >
                  Insert Link
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete User Modal */}
      {userToDelete && (
        <div className="admin-modal-overlay" onClick={() => setUserToDelete(null)}>
          <div className="admin-modal-card" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '400px', textAlign: 'center' }}>
            <div style={{ width: '48px', height: '48px', borderRadius: '50%', background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px' }}>
              <AlertTriangle size={24} />
            </div>
            <h3 style={{ margin: '0 0 8px', fontSize: '20px', fontWeight: '800' }}>Delete User Account</h3>
            <p style={{ margin: '0 0 24px', color: 'var(--dash-text-muted)', fontSize: '14px' }}>
              Are you sure you want to permanently delete this user account? This action cannot be undone and all associated data will be removed.
            </p>
            <div style={{ display: 'flex', gap: '12px' }}>
              <button
                className="admin-action-btn"
                style={{ flex: 1, padding: '12px' }}
                onClick={() => setUserToDelete(null)}
              >
                Cancel
              </button>
              <button
                className="admin-btn-primary"
                style={{ flex: 1, padding: '12px', background: '#ef4444', borderColor: '#ef4444' }}
                onClick={confirmDeleteUser}
              >
                Delete User
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Create Subscription Modal */}
      {isCreateSubscriptionModalOpen && (
        <div className="admin-modal-overlay" onClick={() => setIsCreateSubscriptionModalOpen(false)}>
          <div className="admin-modal-card" onClick={(e) => e.stopPropagation()}>
            <h3 style={{ margin: '0 0 6px', fontSize: '20px', fontWeight: '800' }}>{editingSubscriptionId ? 'Edit Subscription Tier' : 'Create Subscription Tier'}</h3>
            <p style={{ margin: '0 0 20px', color: 'var(--dash-text-muted)', fontSize: '13.5px' }}>
              {editingSubscriptionId ? 'Modify the selected subscription plan details.' : 'Define a new subscription plan for the platform.'}
            </p>

            <form onSubmit={handleCreateSubscription} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: 'var(--dash-text-muted)', marginBottom: '6px' }}>TITLE</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Premium Plan"
                  value={newSubscriptionForm.title}
                  onChange={(e) => setNewSubscriptionForm({ ...newSubscriptionForm, title: e.target.value })}
                  style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '16px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: 'var(--dash-text-muted)', marginBottom: '6px' }}>DURATION</label>
                  <select
                    value={newSubscriptionForm.duration}
                    onChange={(e) => setNewSubscriptionForm({ ...newSubscriptionForm, duration: e.target.value })}
                    style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                  >
                    <option value="Monthly">Monthly</option>
                    <option value="Quarterly">Quarterly</option>
                    <option value="Yearly">Yearly</option>
                  </select>
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: 'var(--dash-text-muted)', marginBottom: '6px' }}>CURRENCY</label>
                  <select
                    value={newSubscriptionForm.currency}
                    onChange={(e) => setNewSubscriptionForm({ ...newSubscriptionForm, currency: e.target.value })}
                    style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                  >
                    <option value="INR">INR (₹)</option>
                    <option value="USD">USD ($)</option>
                    <option value="EUR">EUR (€)</option>
                    <option value="GBP">GBP (£)</option>
                  </select>
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: 'var(--dash-text-muted)', marginBottom: '6px' }}>AMOUNT</label>
                  <input
                    type="number"
                    step="0.01"
                    required
                    placeholder="e.g. 29.99"
                    value={newSubscriptionForm.amount}
                    onChange={(e) => setNewSubscriptionForm({ ...newSubscriptionForm, amount: e.target.value })}
                    style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                  />
                </div>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '24px', marginTop: '4px' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', fontSize: '14px', fontWeight: '600', color: 'var(--dash-text)' }}>
                  <input
                    type="checkbox"
                    checked={newSubscriptionForm.autopay}
                    onChange={(e) => setNewSubscriptionForm({ ...newSubscriptionForm, autopay: e.target.checked })}
                    style={{ width: '18px', height: '18px', accentColor: '#10b981' }}
                  />
                  Enable Autopay
                </label>
                <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', fontSize: '14px', fontWeight: '600', color: 'var(--dash-text)' }}>
                  <input
                    type="checkbox"
                    checked={newSubscriptionForm.trialEnabled}
                    onChange={(e) => setNewSubscriptionForm({ ...newSubscriptionForm, trialEnabled: e.target.checked })}
                    style={{ width: '18px', height: '18px', accentColor: '#3b82f6' }}
                  />
                  Trial Option
                </label>
              </div>

              {newSubscriptionForm.trialEnabled && (
                <div style={{ marginTop: '8px' }}>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: 'var(--dash-text-muted)', marginBottom: '6px' }}>TRIAL DURATION (DAYS)</label>
                  <input
                    type="number"
                    min="1"
                    required
                    placeholder="e.g. 14"
                    value={newSubscriptionForm.trialDays}
                    onChange={(e) => setNewSubscriptionForm({ ...newSubscriptionForm, trialDays: e.target.value })}
                    style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                  />
                </div>
              )}

              <div style={{ display: 'flex', gap: '12px', marginTop: '16px' }}>
                <button
                  type="button"
                  className="admin-action-btn"
                  style={{ flex: 1, padding: '12px' }}
                  onClick={() => setIsCreateSubscriptionModalOpen(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="admin-btn-primary"
                  style={{ flex: 1, padding: '12px' }}
                >
                  {editingSubscriptionId ? 'Save Changes' : 'Create Tier'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Create / Edit Coupon Modal */}
      {isCreateCouponModalOpen && (
        <div className="admin-modal-overlay" onClick={() => setIsCreateCouponModalOpen(false)}>
          <div className="admin-modal-card" onClick={(e) => e.stopPropagation()}>
            <h3 style={{ margin: '0 0 6px', fontSize: '20px', fontWeight: '800' }}>
              {editingCouponId ? 'Edit Coupon' : 'Create Coupon'}
            </h3>
            <p style={{ margin: '0 0 20px', color: 'var(--dash-text-muted)', fontSize: '13.5px' }}>
              {editingCouponId ? 'Update the details for this coupon.' : 'Define a new discount coupon.'}
            </p>

            <form onSubmit={handleCreateCoupon} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: 'var(--dash-text-muted)', marginBottom: '6px' }}>COUPON CODE</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. SUMMER20"
                  value={newCouponForm.code}
                  onChange={(e) => setNewCouponForm({ ...newCouponForm, code: e.target.value })}
                  style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none', textTransform: 'uppercase' }}
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: 'var(--dash-text-muted)', marginBottom: '6px' }}>DISCOUNT TYPE</label>
                  <select
                    value={newCouponForm.discountType}
                    onChange={(e) => setNewCouponForm({ ...newCouponForm, discountType: e.target.value })}
                    style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                  >
                    <option value="Percentage">Percentage (%)</option>
                    <option value="Fixed">Fixed Amount ($)</option>
                  </select>
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: 'var(--dash-text-muted)', marginBottom: '6px' }}>DISCOUNT VALUE</label>
                  <input
                    type="number"
                    min="1"
                    step={newCouponForm.discountType === 'Percentage' ? "1" : "0.01"}
                    required
                    placeholder={newCouponForm.discountType === 'Percentage' ? "e.g. 20" : "e.g. 10.00"}
                    value={newCouponForm.discountValue}
                    onChange={(e) => setNewCouponForm({ ...newCouponForm, discountValue: e.target.value })}
                    style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: 'var(--dash-text-muted)', marginBottom: '6px' }}>SCOPE</label>
                  <select
                    value={newCouponForm.targetType}
                    onChange={(e) => setNewCouponForm({ ...newCouponForm, targetType: e.target.value, targetUser: '' })}
                    style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                  >
                    <option value="All">All Users</option>
                    <option value="Individual">Individual User</option>
                  </select>
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: 'var(--dash-text-muted)', marginBottom: '6px' }}>EXPIRY DATE & TIME</label>
                  <input
                    type="datetime-local"
                    required
                    value={newCouponForm.expiryDate}
                    onChange={(e) => setNewCouponForm({ ...newCouponForm, expiryDate: e.target.value })}
                    style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none', colorScheme: theme }}
                  />
                </div>
              </div>

              {newCouponForm.targetType === 'Individual' && (
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: 'var(--dash-text-muted)', marginBottom: '6px' }}>TARGET USER (EMAIL OR ID)</label>
                  <input
                    type="text"
                    required
                    list="user-suggestions"
                    placeholder="e.g. user@example.com"
                    value={newCouponForm.targetUser}
                    onChange={(e) => setNewCouponForm({ ...newCouponForm, targetUser: e.target.value })}
                    style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'var(--dash-bg)', color: 'var(--dash-text)', border: '1px solid var(--dash-border)', outline: 'none' }}
                  />
                  <datalist id="user-suggestions">
                    {usersList.map(user => (
                      <option key={user.id} value={user.email}>{user.name} ({user.uidDisplay})</option>
                    ))}
                  </datalist>
                </div>
              )}

              <div style={{ display: 'flex', gap: '12px', marginTop: '16px' }}>
                <button
                  type="button"
                  className="admin-action-btn"
                  style={{ flex: 1, padding: '12px' }}
                  onClick={() => setIsCreateCouponModalOpen(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="admin-btn-primary"
                  style={{ flex: 1, padding: '12px' }}
                >
                  {editingCouponId ? 'Save Changes' : 'Create Coupon'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
}
