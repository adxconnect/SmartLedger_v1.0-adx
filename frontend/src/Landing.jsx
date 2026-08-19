import React, { useState, useEffect } from 'react';
import './Landing.css';
import './mobile/mobile.css';
import { API_BASE_URL_PYTHON } from './config';

const PricingCard = ({ plan, onAuthClick }) => {
  const [showFeatures, setShowFeatures] = useState(false);

  const features = [
    "AI Bank Statement Analysis",
    "AI Financial Advice",
    "Live Investment Tracker",
    "Live Loans Tracker",
    "Track Expenses",
    "Tax Planning"
  ];

  const lowerTitle = (plan.title || "").toLowerCase();
  let badgeText = plan.title;
  let badgeClass = 'price-badge';
  let isHighlight = false;

  if (lowerTitle.includes('popular') || lowerTitle.includes('premium')) {
    badgeText = 'Popular';
    badgeClass = 'price-badge orange';
    isHighlight = true;
  } else if (lowerTitle.includes('student')) {
    badgeText = 'Student';
  } else if (lowerTitle.includes('enterprise')) {
    badgeText = 'Enterprise';
    badgeClass = 'price-badge';
  }

  const getDurationText = (duration) => {
    if (!duration) return 'mo';
    const d = duration.toLowerCase();
    if (d === 'monthly') return 'mo';
    if (d === 'yearly') return 'yr';
    if (d === 'quarterly') return 'qtr';
    return d;
  };

  return (
    <div className={`pricing-card ${isHighlight ? 'highlight' : ''}`}>
      <div className="price-col">
        <h4>₹{plan.amount}<span style={{ fontSize: 16, color: '#666' }}>/{getDurationText(plan.duration)}</span></h4>
        <p>Per user • {plan.title}</p>
        <button className="btn-white" style={{ border: '1px solid #ddd' }} onClick={onAuthClick}>Get Started</button>
      </div>
      <div className="price-features">
        <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
          <li style={{ marginBottom: 8 }}>• {features[0]}</li>
          <li style={{ marginBottom: 8 }}>• {features[1]}</li>
          {showFeatures && features.slice(2).map((feat, idx) => (
            <li key={idx} style={{ marginBottom: 8 }}>• {feat}</li>
          ))}
        </ul>
        <button
          onClick={() => setShowFeatures(!showFeatures)}
          style={{ background: 'none', border: 'none', color: '#10b981', cursor: 'pointer', fontSize: '13px', fontWeight: 'bold', padding: 0, marginTop: '12px', display: 'flex', alignItems: 'center' }}
        >
          {showFeatures ? 'View Less' : 'View Full Features'}
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ marginLeft: '4px', transform: showFeatures ? 'rotate(180deg)' : 'none', transition: 'transform 0.2s' }}>
            <polyline points="6 9 12 15 18 9"></polyline>
          </svg>
        </button>
      </div>
      <div className={badgeClass} style={badgeText === 'Enterprise' ? { background: '#1a1a1a' } : {}}>{badgeText}</div>
    </div>
  );
};

export default function Landing({ onAuthClick }) {
  const [activeModal, setActiveModal] = useState(null);
  const [plans, setPlans] = useState([]);

  useEffect(() => {
    const fetchPlans = async () => {
      try {
        const response = await fetch(`${API_BASE_URL_PYTHON}/api/admin/firestore-read`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ collection_name: 'subscriptions' })
        });
        if (response.ok) {
          const data = await response.json();
          const plansData = data.data || [];
          plansData.sort((a, b) => parseFloat(a.amount) - parseFloat(b.amount));
          setPlans(plansData);
        } else {
          console.error("Failed to fetch plans from backend:", await response.text());
        }
      } catch (error) {
        console.error("Error fetching plans:", error);
      }
    };
    fetchPlans();
  }, []);

  const closeModal = () => setActiveModal(null);

  const renderModalContent = () => {
    switch (activeModal) {
      case 'about':
        return (
          <div style={{ padding: '20px' }}>
            <h2 style={{ fontSize: '36px', fontWeight: 700, marginBottom: '24px', letterSpacing: '-1px' }}>About SledgerAI</h2>
            <div style={{ display: 'flex', gap: '40px', alignItems: 'center' }}>
              <div style={{ flex: 1 }}>
                <p style={{ fontSize: '16px', color: '#4b5563', lineHeight: '1.6', marginBottom: '20px' }}>
                  SledgerAI was founded on a simple principle: financial management shouldn't require a finance degree. We believe that everyone deserves crystal-clear visibility into their wealth, backed by intelligent insights.
                </p>
                <p style={{ fontSize: '16px', color: '#4b5563', lineHeight: '1.6', marginBottom: '32px' }}>
                  By combining enterprise-grade security with advanced AI capabilities, we've built a Virtual Chartered Accountant that works for you 24/7. From automated categorization to proactive tax planning, SledgerAI is your dedicated partner in financial growth.
                </p>
                <div className="about-stats" style={{ display: 'flex', gap: '32px' }}>
                  <div style={{ borderLeft: '4px solid #10b981', paddingLeft: '16px' }}>
                    <h4 style={{ fontSize: '24px', fontWeight: 700, margin: '0 0 4px 0', color: '#111827' }}>1M+</h4>
                    <p style={{ margin: 0, color: '#6b7280', fontSize: '14px', fontWeight: 500 }}>Transactions Processed</p>
                  </div>
                  <div style={{ borderLeft: '4px solid #10b981', paddingLeft: '16px' }}>
                    <h4 style={{ fontSize: '24px', fontWeight: 700, margin: '0 0 4px 0', color: '#111827' }}>99.9%</h4>
                    <p style={{ margin: 0, color: '#6b7280', fontSize: '14px', fontWeight: 500 }}>Uptime Reliability</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        );
      case 'support':
        return (
          <div style={{ padding: '20px', textAlign: 'center' }}>
            <h2 style={{ fontSize: '36px', fontWeight: 700, marginBottom: '16px', letterSpacing: '-1px' }}>We're here to help</h2>
            <p style={{ fontSize: '16px', color: '#6b7280', marginBottom: '40px' }}>Get the support you need, when you need it.</p>
            <div className="support-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '20px', textAlign: 'left' }}>
              <div style={{ background: '#f9fafb', padding: '24px', borderRadius: '16px', border: '1px solid #f3f4f6' }}>
                <div style={{ width: '40px', height: '40px', background: '#d1fae5', color: '#059669', borderRadius: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '16px' }}>
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg>
                </div>
                <h3 style={{ fontSize: '18px', fontWeight: 600, marginBottom: '8px' }}>Email Support</h3>
                <p style={{ color: '#6b7280', fontSize: '14px', marginBottom: '16px' }}>Reach our team anytime. We aim to respond within 24 hours.</p>
                <a href="mailto:support@sledgerai.com" style={{ color: '#10b981', fontWeight: 600, textDecoration: 'none', fontSize: '14px' }}>support@sledgerai.com &rarr;</a>
              </div>

              <div style={{ background: '#f9fafb', padding: '24px', borderRadius: '16px', border: '1px solid #f3f4f6' }}>
                <div style={{ width: '40px', height: '40px', background: '#dbeafe', color: '#2563eb', borderRadius: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '16px' }}>
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
                </div>
                <h3 style={{ fontSize: '18px', fontWeight: 600, marginBottom: '8px' }}>Live Chat</h3>
                <p style={{ color: '#6b7280', fontSize: '14px', marginBottom: '16px' }}>Available for Premium and Enterprise members 24/7.</p>
                <span style={{ color: '#2563eb', fontWeight: 600, cursor: 'pointer', fontSize: '14px' }} onClick={() => { closeModal(); onAuthClick(); }}>Sign in to chat &rarr;</span>
              </div>

              <div style={{ background: '#f9fafb', padding: '24px', borderRadius: '16px', border: '1px solid #f3f4f6' }}>
                <div style={{ width: '40px', height: '40px', background: '#fef3c7', color: '#d97706', borderRadius: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '16px' }}>
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"></circle><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>
                </div>
                <h3 style={{ fontSize: '18px', fontWeight: 600, marginBottom: '8px' }}>Help Center</h3>
                <p style={{ color: '#6b7280', fontSize: '14px', marginBottom: '16px' }}>Browse our comprehensive guides and FAQ documentation.</p>
                <span style={{ color: '#d97706', fontWeight: 600, cursor: 'pointer', fontSize: '14px' }} onClick={closeModal}>Visit Help Center &rarr;</span>
              </div>
            </div>
          </div>
        );
      case 'privacy':
        return (
          <div style={{ padding: '20px' }}>
            <h3 style={{ fontSize: '28px', fontWeight: 700, marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '12px' }}>
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#10b981" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
              Privacy Policy
            </h3>
            <div style={{ maxHeight: '60vh', overflowY: 'auto', paddingRight: '16px', fontSize: '15px', color: '#4b5563', lineHeight: '1.7' }}>
              <p style={{ marginBottom: '16px' }}><strong>1. Information Collection</strong><br />SmartLedger collects personal information you provide when creating an account, such as your name, email, and phone number. We also collect financial data you choose to upload (e.g., bank statements) strictly for providing AI-driven insights.</p>
              <p style={{ marginBottom: '16px' }}><strong>2. Data Usage & AI</strong><br />Your financial data is processed by our secure AI engine to generate categorized reports and tax advice. We do NOT use your personal financial data to train public AI models. All processing happens within our secure, isolated environment.</p>
              <p style={{ marginBottom: '16px' }}><strong>3. Data Protection</strong><br />We employ industry-standard AES-256 encryption for data at rest and TLS 1.3 for data in transit. Your data is yours. We do not sell your personal or financial information to third parties.</p>
              <p style={{ marginBottom: '16px' }}><strong>4. Third-Party Integrations</strong><br />When using features like Razorpay for subscriptions, only necessary billing information is shared with the payment processor. We do not store full credit card details.</p>
              <p><strong>5. User Rights</strong><br />You have the right to request a complete deletion of your account and associated data at any time from your account settings.</p>
            </div>
          </div>
        );
      case 'terms':
        return (
          <div style={{ padding: '20px' }}>
            <h3 style={{ fontSize: '28px', fontWeight: 700, marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '12px' }}>
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
              Terms & Conditions
            </h3>
            <div style={{ maxHeight: '60vh', overflowY: 'auto', paddingRight: '16px', fontSize: '15px', color: '#4b5563', lineHeight: '1.7' }}>
              <p style={{ marginBottom: '16px' }}><strong>1. Acceptance of Terms</strong><br />By accessing and using SmartLedger, you accept and agree to be bound by the terms and provision of this agreement.</p>
              <p style={{ marginBottom: '16px' }}><strong>2. Service Description</strong><br />SmartLedger provides AI-powered financial tracking, statement analysis, and Virtual CA services. The insights provided are for informational purposes and should not replace professional financial or legal advice.</p>
              <p style={{ marginBottom: '16px' }}><strong>3. Subscriptions & Billing</strong><br />Certain features require a premium subscription. Billing is handled securely via our payment partners. Subscriptions auto-renew unless cancelled at least 24 hours before the end of the current period.</p>
              <p style={{ marginBottom: '16px' }}><strong>4. User Responsibilities</strong><br />You are responsible for maintaining the confidentiality of your account credentials. You agree to provide accurate information and to update it as necessary. You must not use the platform for any illegal activities.</p>
              <p><strong>5. Limitation of Liability</strong><br />SmartLedger shall not be liable for any indirect, incidental, special, consequential or punitive damages, or any loss of profits or revenues, whether incurred directly or indirectly, based on the use of our AI insights.</p>
            </div>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <div className="landing-page">
      <nav className="landing-nav">
        <div className="nav-logo" style={{ display: 'flex', alignItems: 'center', gap: '2px' }}>
          <img src="/assets/logo.png" alt="SledgerAI" className="nav-logo-icon" style={{ marginRight: '6px' }} />
          <span style={{ color: '#ffffff', fontWeight: 700, letterSpacing: '-0.5px' }}>Sledger</span>
          <span style={{ color: '#34d399', fontWeight: 600, letterSpacing: '-0.5px' }}>AI</span>
          <svg viewBox="0 0 50 50" width="26" height="26" style={{ marginLeft: '4px', transform: 'translateY(1px)' }}>
            <path d="M 8 22 L 18 38 L 28 12 C 38 12 42 22 24 42 L 44 42" fill="none" stroke="#34d399" strokeWidth="3.5" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        </div>

        <button className="nav-btn" onClick={onAuthClick}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
            <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
          </svg>
          <span className="nav-btn-text">Account</span>
        </button>
      </nav>

      {/* HERO SECTION */}
      <section className="hero-section">
        <div className="hero-overlay"></div>
        <div className="hero-content">
          <h1 className="hero-title">
            Manage your finances <span className="smarter-highlight">smarter</span>, with <span className="ai-text-animated">AI</span>.
          </h1>
        </div>

        <div className="hero-glass-card">
          <div className="premium-icon-wrapper">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#fbbf24" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
            </svg>
          </div>
          <div className="hero-glass-text">
            <h4>Made in India</h4>
            <p>Secure. Safe. Encrypted.</p>
          </div>
        </div>


        <div className="nav-links">
          <a href="#features">Features</a>
          <a href="#pricing">Pricing</a>
        </div>
      </section>

      <div className="transactions-banner">
        <span style={{ opacity: 0.9, fontWeight: 'bold' }}>Transactions made via</span>
        <div className="payment-methods">
          <span className="payment-badge">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="5" y="2" width="14" height="20" rx="2" ry="2"></rect><line x1="12" y1="18" x2="12.01" y2="18"></line></svg>
            UPI
          </span>
          <span className="payment-badge">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="2" y="6" width="20" height="12" rx="2"></rect><circle cx="12" cy="12" r="2"></circle><path d="M6 12h.01M18 12h.01"></path></svg>
            Cash
          </span>
          <span className="payment-badge">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="3" y1="21" x2="21" y2="21"></line><line x1="3" y1="10" x2="21" y2="10"></line><polygon points="12 2 20 10 4 10 12 2"></polygon><line x1="7" y1="10" x2="7" y2="21"></line><line x1="17" y1="10" x2="17" y2="21"></line></svg>
            NEFT
          </span>
          <span className="payment-badge">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect><line x1="1" y1="10" x2="23" y2="10"></line></svg>
            Credit/Debit Card
          </span>
        </div>
        <span className="separator">•</span>
        <span style={{ fontWeight: 600, color: '#34d399' }}>Financial Management made easy.</span>
      </div>

      <div className="landing-container">
        {/* SMART CHOICES */}
        <section className="center-heading">
          <h2>SmartLedger's platform helps users make smarter financial choices</h2>
          <p>Built for people who want clarity, control, and confidence.</p>
        </section>

        <section className="stats-grid">
          <div className="stats-text-block" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between', height: '100%' }}>
            <div style={{ marginBottom: '20px' }}>
              <h3 style={{ marginBottom: '8px' }}>AI-Powered Core</h3>
              <p style={{ marginBottom: '16px' }}>Experience the next generation of financial management. Our built-in AI analyzes your data instantly.</p>
              <button onClick={onAuthClick} style={{ background: 'linear-gradient(135deg, #34d399 0%, #10b981 100%)', color: 'white', border: 'none', padding: '12px 28px', borderRadius: '40px', fontWeight: 600, fontSize: '15px', cursor: 'pointer', display: 'inline-flex', alignItems: 'center', boxShadow: '0 8px 20px rgba(16, 185, 129, 0.3)' }}>
                Explore AI <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ marginLeft: '8px' }}><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
              </button>
            </div>

            <div className="stats-numbers" style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              <div className="stat-item" style={{ display: 'flex', alignItems: 'center', gap: '12px', background: '#f8fafc', padding: '10px 12px', borderRadius: '8px', border: '1px solid #edf2f7' }}>
                <div style={{ background: '#d1fae5', padding: '6px', borderRadius: '6px', color: '#059669', display: 'flex' }}>
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline></svg>
                </div>
                <div>
                  <h4 style={{ fontSize: '16px', color: '#1a1a1a', marginBottom: '2px', fontWeight: 600 }}>Automated Insights</h4>
                  <p style={{ margin: 0, color: '#666', fontSize: '13px' }}>Stop categorizing manually</p>
                </div>
              </div>
              <div className="stat-item" style={{ display: 'flex', alignItems: 'center', gap: '12px', background: '#f8fafc', padding: '10px 12px', borderRadius: '8px', border: '1px solid #edf2f7' }}>
                <div style={{ background: '#d1fae5', padding: '6px', borderRadius: '6px', color: '#059669', display: 'flex' }}>
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
                </div>
                <div>
                  <h4 style={{ fontSize: '16px', color: '#1a1a1a', marginBottom: '2px', fontWeight: 600 }}>Smart Tax Planning</h4>
                  <p style={{ margin: 0, color: '#666', fontSize: '13px' }}>Stay ahead of tax season</p>
                </div>
              </div>
            </div>
          </div>

          <div className="stats-img" style={{ height: '100%', overflow: 'hidden', borderRadius: '24px' }}>
            <img src="/assets/hero.png" alt="Happy user" style={{ height: '100%', width: '100%', objectFit: 'cover', transform: 'scale(1.08)' }} />
          </div>

          <div className="stats-testimonial" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between', height: '100%' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '12px', marginBottom: '32px' }}>
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#10b981" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ marginTop: '4px' }}>
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                </svg>
                <h2 style={{ fontSize: '28px', fontWeight: 700, color: '#111827', margin: 0, lineHeight: '1.3' }}>
                  End-To-End<br />Encryption
                </h2>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', marginBottom: '24px', textAlign: 'left' }}>
                <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                  <div style={{ marginTop: '3px', color: '#10b981' }}>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
                  </div>
                  <div>
                    <h4 style={{ fontSize: '15px', fontWeight: 600, color: '#111827', margin: '0 0 4px 0' }}>Secure Storage</h4>
                    <p style={{ margin: 0, fontSize: '14px', color: '#4b5563', lineHeight: '1.5' }}>Your data is securely stored in our database.</p>
                  </div>
                </div>
                <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                  <div style={{ marginTop: '3px', color: '#10b981' }}>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
                  </div>
                  <div>
                    <h4 style={{ fontSize: '15px', fontWeight: 600, color: '#111827', margin: '0 0 4px 0' }}>Complete Privacy</h4>
                    <p style={{ margin: 0, fontSize: '14px', color: '#4b5563', lineHeight: '1.5' }}>Not visible to anyone in the admin side.</p>
                  </div>
                </div>
              </div>
              <small style={{ fontSize: '13px', color: '#9ca3af', display: 'block', textAlign: 'right', fontStyle: 'italic' }}>*T&C Applies</small>
            </div>
            <div className="testimonial-author" style={{ marginTop: 'auto', display: 'flex', alignItems: 'center', gap: '16px' }}>
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#10b981" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
              </svg>
              <div className="testimonial-author-text">
                <h5 style={{ margin: 0, fontSize: '16px', fontWeight: 600, color: '#10b981' }}>256-bit Encryption</h5>
                <p style={{ margin: 0, fontSize: '14px', color: '#6b7280', fontStyle: 'normal' }}>Industry standard protection</p>
              </div>
            </div>
          </div>
        </section>

        {/* SUITE OF TOOLS */}
        <section className="center-heading">
          <h2>A suite of tools to help you take charge of your financial future</h2>
          <p>Our platform provides personalized insights and flexible financial tools.</p>
        </section>

        <section className="tools-section" id="features">
          <style>{`
            .dashboard-preview-card {
              transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.5s cubic-bezier(0.4, 0, 0.2, 1);
            }
            .dashboard-preview-card:hover {
              transform: translateY(-12px) scale(1.02);
              box-shadow: 0 30px 60px rgba(16, 185, 129, 0.25), 0 0 40px rgba(16, 185, 129, 0.15);
              border-color: rgba(16, 185, 129, 0.3) !important;
            }
          `}</style>
          <div className="tools-cards" style={{ background: '#f0fdf4', padding: '40px', borderRadius: '32px', display: 'flex', gap: '24px' }}>
            <div className="dashboard-preview-card" style={{ flex: 1, borderRadius: '24px', overflow: 'hidden', display: 'flex', height: '340px', boxShadow: '0 20px 40px rgba(0,0,0,0.08)', border: '1px solid rgba(0,0,0,0.05)', cursor: 'pointer' }}>
              <img src="/assets/01.png" alt="Dashboard Preview 1" style={{ width: '100%', height: '100%', objectFit: 'cover', objectPosition: 'left top' }} />
            </div>
            <div className="dashboard-preview-card" style={{ flex: 1, borderRadius: '24px', overflow: 'hidden', display: 'flex', height: '340px', boxShadow: '0 20px 40px rgba(0,0,0,0.08)', border: '1px solid rgba(0,0,0,0.05)', cursor: 'pointer' }}>
              <img src="/assets/02.png" alt="Dashboard Preview 2" style={{ width: '100%', height: '100%', objectFit: 'cover', objectPosition: 'left top' }} />
            </div>
          </div>
        </section>

        {/* PRICING */}
        <section className="pricing-section" id="pricing">
          <div className="pricing-info">
            <h2>Choose the plan that fits your needs</h2>
            <ul>
              <li>Flexible plans for every need</li>
              <li>Comprehensive features included</li>
              <li>Risk-free trial</li>
            </ul>
          </div>

          <div className="pricing-cards">
            {plans.map(plan => (
              <PricingCard key={plan.id} plan={plan} onAuthClick={onAuthClick} />
            ))}
            {plans.length === 0 && <p style={{ color: '#666' }}>Loading plans...</p>}
          </div>
        </section>

        {/* TRUST */}
        <section className="trust-section" id="testimonials">
          <div className="trust-block" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between', height: '420px', padding: '40px', background: '#ffffff', borderRadius: '32px' }}>
            <div>
              <h2 style={{ fontSize: '32px', lineHeight: '1.2', marginBottom: '20px', letterSpacing: '-0.5px' }}>Built for modern professionals and businesses.</h2>
              <button className="btn-white" style={{ border: '1px solid #e5e7eb', borderRadius: '30px', padding: '10px 24px', display: 'inline-flex', alignItems: 'center', gap: '8px', background: 'transparent' }} onClick={onAuthClick}>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#2563eb" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><line x1="7" y1="17" x2="17" y2="7"></line><polyline points="7 7 17 7 17 17"></polyline></svg>
                <span style={{ fontWeight: 600, color: '#111827' }}>Get Started</span>
              </button>
            </div>

            <div style={{ marginTop: 'auto' }}>
              <div style={{ marginBottom: '12px' }}>
                <span style={{ fontWeight: 700, fontSize: '16px', color: '#10b981', display: 'inline-block' }}>Innovation First</span>
              </div>
              <p style={{ fontStyle: 'italic', fontSize: '16px', color: '#4b5563', lineHeight: '1.5', marginBottom: '20px' }}>
                "Empowering you with AI-driven financial insights, so you can focus on what matters most."
              </p>

              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#10b981" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"></path></svg>
                <div>
                  <h5 style={{ margin: 0, fontSize: '16px', fontWeight: 600, color: '#111827' }}>SledgerAI Vision</h5>
                  <p style={{ margin: 0, fontSize: '14px', color: '#6b7280' }}>Designed for growth</p>
                </div>
              </div>
            </div>
          </div>

          <div className="trust-image" style={{ overflow: 'hidden', borderRadius: '32px', height: '420px', position: 'relative' }}>
            <img src="/assets/hero.png" alt="Happy User" style={{ width: '100%', height: '100%', objectFit: 'cover', transform: 'scale(1.08)' }} />
            <div className="trust-image-overlay" style={{ position: 'absolute', bottom: 0, left: 0, right: 0, padding: '32px', background: 'linear-gradient(to top, rgba(0,0,0,0.85) 0%, rgba(0,0,0,0.4) 60%, transparent 100%)', display: 'flex', flexDirection: 'column', alignItems: 'flex-start' }}>
              <h3 style={{ color: 'white', fontSize: '26px', marginBottom: '20px', textShadow: '0 2px 10px rgba(0,0,0,0.3)', lineHeight: '1.2', letterSpacing: '-0.5px', textAlign: 'left' }}>Get ready to experience the future of personal finance</h3>
              <a href="https://wa.me/9475715126" target="_blank" rel="noopener noreferrer" style={{ background: '#10b981', color: 'white', border: 'none', padding: '12px 28px', borderRadius: '30px', fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: '8px', cursor: 'pointer', boxShadow: '0 4px 14px rgba(16, 185, 129, 0.4)', textDecoration: 'none' }}>
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path></svg>
                Contact Us
              </a>
            </div>
          </div>
        </section>

        {/* FOOTER */}
        <footer className="landing-footer">
          <div className="footer-top" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '60px', alignItems: 'center', marginBottom: '80px' }}>
            <h2 style={{ fontSize: '42px', fontWeight: 700, lineHeight: '1.2', margin: 0, color: '#111827', letterSpacing: '-1px' }}>
              Transforming <span style={{ color: '#10b981', fontStyle: 'italic', paddingRight: '4px' }}>modern</span> finance with intelligent, AI-driven solutions
            </h2>
            <style>{`
              .footer-fluid-card-animated {
                transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.4s cubic-bezier(0.4, 0, 0.2, 1) !important;
              }
              .footer-fluid-card-animated:hover {
                transform: translateY(-8px) scale(1.02) !important;
                box-shadow: 0 24px 48px rgba(16, 185, 129, 0.25) !important;
              }
            `}</style>
            <div className="footer-fluid-card footer-fluid-card-animated" style={{ backgroundImage: "linear-gradient(to right, rgba(0,0,0,0.6) 0%, rgba(0,0,0,0.1) 60%), url('/assets/fluid.png')", backgroundSize: 'cover', backgroundPosition: 'center', borderRadius: '32px', height: '220px', position: 'relative', overflow: 'hidden', padding: '32px', display: 'flex', alignItems: 'flex-end', boxShadow: '0 20px 40px rgba(16, 185, 129, 0.15)', cursor: 'pointer' }}>
              <span style={{ color: '#ffffff', fontWeight: 600, fontSize: '24px', lineHeight: '1.3', zIndex: 2, textShadow: '0 4px 12px rgba(0,0,0,0.4)', letterSpacing: '-0.5px' }}>Explore the future<br />of personal finance</span>
            </div>
          </div>

          <div className="footer-bottom-section" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginTop: '20px' }}>
            <style>{`
              .footer-nav-link {
                color: #111827;
                text-decoration: none;
                transition: color 0.3s ease;
                position: relative;
              }
              .footer-nav-link:hover {
                color: #10b981;
              }
              .footer-sub-link {
                color: #6b7280;
                text-decoration: none;
                transition: color 0.3s ease;
              }
              .footer-sub-link:hover {
                color: #111827;
              }
            `}</style>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '32px' }}>
              <div style={{ display: 'flex', gap: '48px', fontWeight: 500, fontSize: '18px' }}>
                <a href="#about" className="footer-nav-link" onClick={(e) => { e.preventDefault(); setActiveModal('about'); }}>About</a>
                <a href="#features" className="footer-nav-link">Features</a>
                <a href="#pricing" className="footer-nav-link">Pricing</a>
                <a href="#support" className="footer-nav-link" onClick={(e) => { e.preventDefault(); setActiveModal('support'); }}>Support</a>
              </div>
              <div style={{ display: 'flex', gap: '32px', fontSize: '15px' }}>
                <a href="#privacy" className="footer-sub-link" onClick={(e) => { e.preventDefault(); setActiveModal('privacy'); }}>Privacy Policy</a>
                <a href="#terms" className="footer-sub-link" onClick={(e) => { e.preventDefault(); setActiveModal('terms'); }}>Terms & Conditions</a>
              </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '8px', paddingBottom: '4px' }}>
              <div style={{ fontSize: '14px', color: '#6b7280' }}>
                support@sledgerai.com © 2026 SledgerAI. All rights reserved.
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '13px', color: '#9ca3af' }}>
                <span>Developed by</span>
                <a 
                  href="https://www.linkedin.com/in/adxconnect" 
                  target="_blank" 
                  rel="noopener noreferrer"
                  className="footer-nav-link"
                  style={{ color: '#10b981', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '4px' }}
                >
                  adxconnect
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path><polyline points="15 3 21 3 21 9"></polyline><line x1="10" y1="14" x2="21" y2="3"></line></svg>
                </a>
              </div>
            </div>
          </div>
        </footer>
      </div>

      {/* MODAL OVERLAY */}
      {activeModal && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(17, 24, 39, 0.6)', backdropFilter: 'blur(8px)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px' }} onClick={closeModal}>
          <div style={{ background: '#fff', borderRadius: '24px', width: '100%', maxWidth: activeModal === 'support' ? '900px' : '700px', maxHeight: '90vh', position: 'relative', overflow: 'hidden', boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)' }} onClick={(e) => e.stopPropagation()}>
            <button onClick={closeModal} style={{ position: 'absolute', top: '24px', right: '24px', background: '#f3f4f6', border: 'none', cursor: 'pointer', color: '#6b7280', zIndex: 10, width: '36px', height: '36px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', transition: 'background 0.2s' }} onMouseOver={(e) => e.currentTarget.style.background = '#e5e7eb'} onMouseOut={(e) => e.currentTarget.style.background = '#f3f4f6'}>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
            </button>
            <div style={{ padding: '16px' }}>
              {renderModalContent()}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
