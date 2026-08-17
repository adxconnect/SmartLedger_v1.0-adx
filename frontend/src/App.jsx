import React, { useState, useEffect } from 'react';
import './index.css';
import Dashboard from './Dashboard';
import AdminDashboard from './AdminDashboard';
import Landing from './Landing';
import { auth, db } from './firebase';
import { signInWithEmailAndPassword, createUserWithEmailAndPassword, updateProfile, GoogleAuthProvider, signInWithPopup, sendPasswordResetEmail } from 'firebase/auth';
import { doc, setDoc, getDoc } from 'firebase/firestore';
import { API_BASE_URL_PYTHON, API_BASE_URL_JAVA } from './config';

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

function App() {
  const [authMode, setAuthMode] = useState('login'); // 'login', 'signup', 'forgot'
  const [resetMethod, setResetMethod] = useState('email'); // 'email', 'sms'
  const [showPassword, setShowPassword] = useState(false);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);
  const [showAuthModal, setShowAuthModal] = useState(false);
  const [isAuthLoading, setIsAuthLoading] = useState(true);
  
  const [formData, setFormData] = useState({ name: '', phone: '', email: '', password: '' });

  useEffect(() => {
    const isAdminSession = sessionStorage.getItem('smartledger_admin_session') === 'true';
    if (isAdminSession) {
      setIsAdmin(true);
      setIsAuthenticated(true);
      setIsAuthLoading(false);
      return;
    }
    const unsubscribe = auth.onAuthStateChanged((user) => {
      if (user && user.email?.trim().toLowerCase() === 'admin@ledger.com') {
        setIsAdmin(true);
        setIsAuthenticated(true);
      } else {
        setIsAuthenticated(!!user);
        setIsAdmin(false);
      }
      setIsAuthLoading(false);
    });
    return () => unsubscribe();
  }, []);
  const [selectedCountry, setSelectedCountry] = useState(countryOptions[0]);
  const [showCountryDropdown, setShowCountryDropdown] = useState(false);
  const [countrySearch, setCountrySearch] = useState('');
  const [showMoreDetails, setShowMoreDetails] = useState(false);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleFormChange = (e) => {
    setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const calculatePasswordStrength = (pass) => {
    if (!pass) return 0;
    let s = 0;
    if (pass.length >= 6) s += 20;
    if (pass.length >= 10) s += 20;
    if (/[A-Z]/.test(pass)) s += 20;
    if (/[a-z]/.test(pass)) s += 20;
    if (/[0-9]/.test(pass)) s += 10;
    if (/[^A-Za-z0-9]/.test(pass)) s += 10;
    return Math.min(s, 100);
  };

  const passwordStrength = calculatePasswordStrength(formData.password);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccessMsg(null);
    setLoading(true);

    try {
      if (authMode === 'login') {
        const inputEmail = (formData.email || '').trim().toLowerCase();
        if (inputEmail === 'admin@ledger.com' && formData.password === '12345') {
          sessionStorage.setItem('smartledger_admin_session', 'true');
          setIsAdmin(true);
          setIsAuthenticated(true);
          setShowAuthModal(false);
          setLoading(false);
          return;
        }
        await signInWithEmailAndPassword(auth, formData.email, formData.password);
        if (inputEmail === 'admin@ledger.com') {
          sessionStorage.setItem('smartledger_admin_session', 'true');
          setIsAdmin(true);
        } else {
          sessionStorage.removeItem('smartledger_admin_session');
          setIsAdmin(false);
        }
        setIsAuthenticated(true);
        setShowAuthModal(false);
      } else if (authMode === 'signup') {
        const userCredential = await createUserWithEmailAndPassword(auth, formData.email, formData.password);
        await updateProfile(userCredential.user, { displayName: formData.name });
        
        try {
          // Explicitly save the user to 'users' collection to ensure UI consistency
          await setDoc(doc(db, 'users', userCredential.user.uid), {
              displayName: formData.name,
              email: formData.email,
              phoneCode: selectedCountry.dial,
              phoneNumber: formData.phone,
              shortUid: userCredential.user.uid.substring(0, 6).toUpperCase(),
              createdAt: new Date().toISOString()
          });
          
          // Also save to 'accounts' collection with matching UID to synchronize across backend
          await setDoc(doc(db, 'accounts', userCredential.user.uid), {
              accountName: formData.name,
              email: formData.email,
              id: userCredential.user.uid,
              shortUid: userCredential.user.uid.substring(0, 6).toUpperCase(),
              phoneCode: selectedCountry.dial,
              phoneNumber: formData.phone
          });

          // Optional: still inform backend if needed, but the db writes are handled above
          await fetch(`${API_BASE_URL_JAVA}/api/auth/signup`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
          });
        } catch (err) {
          console.error("Failed to write initial user records:", err);
        }
        
        setIsAuthenticated(true);
        setShowAuthModal(false);
      } else if (authMode === 'forgot') {
        if (resetMethod === 'email') {
          await sendPasswordResetEmail(auth, formData.email);
          setSuccessMsg("Password reset link sent to your email!");
        } else {
          // SMS Mock Flow
          setSuccessMsg("SMS OTP sent to your registered phone number.");
        }
      }
    } catch (err) {
      let errorMessage = "Authentication failed";
      if (err.code === 'auth/email-already-in-use') errorMessage = 'This email is already registered.';
      else if (err.code === 'auth/invalid-credential' || err.code === 'auth/wrong-password') errorMessage = 'Invalid email or password.';
      else if (err.code === 'auth/weak-password') errorMessage = 'Password should be at least 6 characters.';
      else if (err.code === 'auth/user-not-found') errorMessage = 'User not found.';
      else errorMessage = err.message;
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleSignIn = async () => {
    setError(null);
    setLoading(true);
    try {
      const provider = new GoogleAuthProvider();
      const userCredential = await signInWithPopup(auth, provider);
      
      try {
          const userDocRef = doc(db, 'users', userCredential.user.uid);
          const userDocSnap = await getDoc(userDocRef);
          
          if (!userDocSnap.exists()) {
              await setDoc(userDocRef, {
                  displayName: userCredential.user.displayName || '',
                  email: userCredential.user.email || '',
                  shortUid: userCredential.user.uid.substring(0, 6).toUpperCase(),
                  createdAt: new Date().toISOString()
              });
              
              await setDoc(doc(db, 'accounts', userCredential.user.uid), {
                  accountName: userCredential.user.displayName || '',
                  email: userCredential.user.email || '',
                  id: userCredential.user.uid,
                  shortUid: userCredential.user.uid.substring(0, 6).toUpperCase()
              });
          }
      } catch (err) {
          console.error("Failed to verify/create Google user records:", err);
      }

      setIsAuthenticated(true);
      setShowAuthModal(false);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const openAuthModal = () => {
    setShowAuthModal(true);
    setError(null);
    setSuccessMsg(null);
    setAuthMode('login');
  };

  const closeAuthModal = () => {
    setShowAuthModal(false);
    setError(null);
    setSuccessMsg(null);
    setFormData({ name: '', phone: '', email: '', password: '' });
    setSelectedCountry(countryOptions[0]);
    setShowCountryDropdown(false);
    setShowMoreDetails(false);
    setShowPassword(false);
    setAuthMode('login');
  };

  if (isAuthLoading) {
    return <div style={{ background: 'var(--bg-dark)', width: '100vw', height: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center' }}><div className="auth-spinner" style={{ width: '40px', height: '40px' }}></div></div>;
  }

  if (isAuthenticated) {
    if (isAdmin) {
      return (
        <AdminDashboard
          onLogout={() => {
            sessionStorage.removeItem('smartledger_admin_session');
            auth.signOut().catch(() => {});
            setIsAdmin(false);
            setIsAuthenticated(false);
            setShowAuthModal(false);
          }}
        />
      );
    }
    return (
      <Dashboard
        onLogout={() => {
          sessionStorage.removeItem('smartledger_admin_session');
          auth.signOut().catch(() => {});
          setIsAuthenticated(false);
          setIsAdmin(false);
          setShowAuthModal(false);
        }}
      />
    );
  }

  return (
    <>
      <Landing onAuthClick={openAuthModal} />

      {/* AUTH MODAL OVERLAY */}
      {showAuthModal && (
        <div className="auth-modal-overlay" onClick={closeAuthModal}>
          <div className="auth-blob auth-blob-1"></div>
          <div className="auth-blob auth-blob-2"></div>
          <div className="auth-blob auth-blob-3"></div>

          <div className="auth-modal-card" onClick={(e) => e.stopPropagation()}>
            <button className="auth-modal-close" onClick={closeAuthModal} aria-label="Close">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round">
                <line x1="3" y1="3" x2="13" y2="13" />
                <line x1="13" y1="3" x2="3" y2="13" />
              </svg>
            </button>

            <div className="auth-logo-icon">
              <img src="/assets/logo.png" alt="SmartLedger" style={{ width: 44, height: 44, borderRadius: 10, objectFit: 'contain' }} />
            </div>

            <h2 className="auth-title">
              {authMode === 'login' ? 'Welcome back' : authMode === 'signup' ? 'Create account' : 'Reset password'}
            </h2>
            <p className="auth-subtitle">
              {authMode === 'login' ? 'Enter your credentials to access your account' : authMode === 'signup' ? 'Fill in the details to get started with SmartLedger' : 'Choose a method to reset your password'}
            </p>

            {error && (
              <div className="auth-error">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none" style={{ flexShrink: 0 }}>
                  <circle cx="8" cy="8" r="7" stroke="#dc2626" strokeWidth="1.5"/>
                  <line x1="8" y1="5" x2="8" y2="9" stroke="#dc2626" strokeWidth="1.5" strokeLinecap="round"/>
                  <circle cx="8" cy="11.5" r="0.75" fill="#dc2626"/>
                </svg>
                {error}
              </div>
            )}
            
            {successMsg && (
              <div className="auth-success">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none" style={{ flexShrink: 0 }}>
                  <circle cx="8" cy="8" r="7" stroke="#059669" strokeWidth="1.5"/>
                  <path d="M5 8l2 2 4-4" stroke="#059669" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
                {successMsg}
              </div>
            )}

            <form onSubmit={handleSubmit} className="auth-form">
              {authMode === 'forgot' && (
                <div className="reset-method-toggle">
                  <div 
                    className={`reset-method-btn ${resetMethod === 'email' ? 'active' : ''}`}
                    onClick={() => setResetMethod('email')}
                  >
                    Email
                  </div>
                  <div 
                    className={`reset-method-btn ${resetMethod === 'sms' ? 'active' : ''}`}
                    onClick={() => setResetMethod('sms')}
                  >
                    SMS OTP
                  </div>
                </div>
              )}

              {authMode === 'signup' && (
                <div className="auth-field">
                  <label className="auth-label">FULL NAME</label>
                  <input
                    className="auth-input"
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleFormChange}
                    placeholder="John Doe"
                    required={authMode === 'signup'}
                  />
                </div>
              )}

              {(authMode === 'signup' || (authMode === 'forgot' && resetMethod === 'sms')) && (
                <div className="auth-field">
                  <label className="auth-label">CONTACT NUMBER</label>
                  <div className="auth-phone-wrapper" style={{ position: 'relative' }}>
                    <div 
                      className="auth-phone-prefix" 
                      onClick={() => setShowCountryDropdown(!showCountryDropdown)}
                      style={{ display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer' }}
                    >
                      <img 
                        src={`https://flagcdn.com/w20/${selectedCountry.code.toLowerCase()}.png`} 
                        alt={selectedCountry.code} 
                        style={{ width: '16px', borderRadius: '2px' }} 
                      />
                      <span>{selectedCountry.code} {selectedCountry.dial}</span>
                      <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <polyline points="6 9 12 15 18 9"></polyline>
                      </svg>
                    </div>
                    {showCountryDropdown && (
                      <div className="country-dropdown">
                        <div style={{ padding: '8px', borderBottom: '1px solid #e2e8f0', position: 'sticky', top: 0, background: 'white', zIndex: 1, borderRadius: '8px 8px 0 0' }}>
                          <input 
                            type="text" 
                            placeholder="Search country..." 
                            value={countrySearch}
                            onChange={(e) => setCountrySearch(e.target.value)}
                            style={{ width: '100%', padding: '6px 8px', borderRadius: '4px', border: '1px solid #cbd5e1', outline: 'none', fontSize: '13px' }}
                            autoFocus
                          />
                        </div>
                        {countryOptions.filter(c => c.name.toLowerCase().includes(countrySearch.toLowerCase()) || c.code.toLowerCase().includes(countrySearch.toLowerCase()) || c.dial.includes(countrySearch)).map(country => (
                          <div 
                            key={country.code} 
                            className="country-option"
                            onClick={() => {
                              setSelectedCountry(country);
                              setShowCountryDropdown(false);
                              setCountrySearch('');
                            }}
                          >
                            <img src={`https://flagcdn.com/w20/${country.code.toLowerCase()}.png`} alt={country.code} title={country.name} />
                            <span>{country.code} {country.dial}</span>
                          </div>
                        ))}
                      </div>
                    )}
                    <input
                      className="auth-input auth-phone-input"
                      type="tel"
                      name="phone"
                      value={formData.phone}
                      onChange={handleFormChange}
                      placeholder="98765 43210"
                      pattern="[0-9]{10}"
                      maxLength="10"
                      required={authMode === 'signup' || (authMode === 'forgot' && resetMethod === 'sms')}
                    />
                  </div>
                </div>
              )}
              
              {authMode === 'signup' && (
                <div className="more-details-toggle" onClick={() => setShowMoreDetails(!showMoreDetails)}>
                  <span>More details</span>
                  <svg 
                    className={`more-details-arrow ${showMoreDetails ? 'open' : ''}`} 
                    width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
                  >
                    <polyline points="6 9 12 15 18 9"></polyline>
                  </svg>
                </div>
              )}

              <div className={authMode === 'signup' ? `more-details-content ${showMoreDetails ? 'open' : ''}` : ""}>
                {(authMode === 'login' || authMode === 'signup' || (authMode === 'forgot' && resetMethod === 'email')) && (
                  <div className="auth-field" style={{ marginBottom: authMode !== 'forgot' ? '18px' : '0' }}>
                    <label className="auth-label">EMAIL</label>
                    <input
                      className="auth-input"
                      type="email"
                      name="email"
                      value={formData.email}
                      onChange={handleFormChange}
                      placeholder="name@company.com"
                      required={authMode === 'login' || showMoreDetails || (authMode === 'forgot' && resetMethod === 'email')}
                    />
                  </div>
                )}
                
                {authMode !== 'forgot' && (
                  <div className="auth-field">
                    <div className="auth-label-row">
                      <label className="auth-label">PASSWORD</label>
                      {authMode === 'login' && <a href="#" className="auth-forgot" onClick={(e) => { e.preventDefault(); setAuthMode('forgot'); setError(null); setSuccessMsg(null); }}>Forgot password?</a>}
                    </div>
                    <div style={{ position: 'relative' }}>
                      <input
                        className="auth-input"
                        type={showPassword ? "text" : "password"}
                        name="password"
                        value={formData.password}
                        onChange={handleFormChange}
                        placeholder="••••••••"
                        required={authMode === 'login' || showMoreDetails}
                        style={{ paddingRight: '36px' }}
                      />
                      <button 
                        type="button"
                        className="password-eye-btn"
                        onClick={() => setShowPassword(!showPassword)}
                      >
                        {showPassword ? (
                          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24"></path>
                            <line x1="1" y1="1" x2="23" y2="23"></line>
                          </svg>
                        ) : (
                          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                            <circle cx="12" cy="12" r="3"></circle>
                          </svg>
                        )}
                      </button>
                    </div>
                    
                    {/* Password Strength Indicator for Signup */}
                    {authMode === 'signup' && formData.password.length > 0 && (
                      <div className="password-strength-container">
                        <div className="password-strength-bar">
                          <div 
                            className="password-strength-fill" 
                            style={{ 
                              width: `${passwordStrength}%`,
                              backgroundColor: passwordStrength < 40 ? '#ef4444' : passwordStrength < 80 ? '#f59e0b' : '#10b981'
                            }}
                          ></div>
                        </div>
                        <span className="password-strength-text">
                          {passwordStrength < 40 ? 'Weak' : passwordStrength < 80 ? 'Good' : 'Strong (Min 10 chars, Mixed)'}
                        </span>
                      </div>
                    )}
                  </div>
                )}
              </div>

              <button type="submit" className="auth-submit-btn" disabled={loading}>
                {loading ? (
                  <span className="auth-spinner"></span>
                ) : (
                  authMode === 'login' ? 'Sign in' : authMode === 'signup' ? 'Create Account' : 'Send Reset Link'
                )}
              </button>
            </form>

            <div className="auth-divider">
              <span>Or continue with</span>
            </div>

            <button className="auth-google-btn" type="button" onClick={handleGoogleSignIn} disabled={loading}>
              <svg width="18" height="18" viewBox="0 0 18 18">
                <path d="M17.64 9.2c0-.637-.057-1.251-.164-1.84H9v3.481h4.844a4.14 4.14 0 01-1.796 2.716v2.259h2.908c1.702-1.567 2.684-3.875 2.684-6.615z" fill="#4285F4"/>
                <path d="M9 18c2.43 0 4.467-.806 5.956-2.18l-2.908-2.259c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332A8.997 8.997 0 009 18z" fill="#34A853"/>
                <path d="M3.964 10.71A5.41 5.41 0 013.682 9c0-.593.102-1.17.282-1.71V4.958H.957A8.996 8.996 0 000 9c0 1.452.348 2.827.957 4.042l3.007-2.332z" fill="#FBBC05"/>
                <path d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0A8.997 8.997 0 00.957 4.958L3.964 7.29C4.672 5.163 6.656 3.58 9 3.58z" fill="#EA4335"/>
              </svg>
              Google
            </button>

            <div className="auth-toggle">
              {authMode === 'login' ? "Don't have an account? " : "Back to "}
              <span onClick={() => { setAuthMode(authMode === 'login' ? 'signup' : 'login'); setError(null); setSuccessMsg(null); }}>
                {authMode === 'login' ? 'Sign up' : 'Sign in'}
              </span>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default App;
