import React from 'react';
import './Landing.css';

export default function Landing({ onAuthClick }) {
  return (
    <div className="landing-page">
      <nav className="landing-nav">
        <div className="nav-logo" style={{ display: 'flex', alignItems: 'center', gap: '2px' }}>
          <img src="/assets/logo.png" alt="SmartLedger" className="nav-logo-icon" style={{ marginRight: '6px' }} />
          <span style={{ color: '#ffffff', fontWeight: 700, letterSpacing: '-0.5px' }}>Smart</span>
          <span style={{ color: '#34d399', fontWeight: 400, letterSpacing: '-0.5px' }}>Ledger</span>
          <svg viewBox="0 0 50 50" width="26" height="26" style={{ marginLeft: '4px', transform: 'translateY(1px)' }}>
            <path d="M 8 22 L 18 38 L 28 12 C 38 12 42 22 24 42 L 44 42" fill="none" stroke="#34d399" strokeWidth="3.5" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        </div>

        <button className="nav-btn" onClick={onAuthClick}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
            <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
          </svg>
          Account
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
            <p>Secured. Safe. Encrypted.</p>
          </div>
        </div>

        <div className="hero-bottom-right">
          <div className="enterprise-badge">
            <span className="dot"></span>
            For Business/ Enterprise Solutions.
          </div>
          <button className="hero-contact-btn">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="red" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path></svg>
            <span>Contact Us</span>
          </button>
        </div>

        <div className="nav-links">
          <a href="#features">Features</a>
          <a href="#pricing">Pricing</a>
          <a href="#about">About Us</a>
        </div>
      </section>

      <div className="transactions-banner">
        <span style={{ opacity: 0.9, fontWeight: 'bold' }}>Transactions made in</span>
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
          <div className="stats-text-block">
            <div>
              <h3>SmartLedger stats</h3>
              <p>Relied upon by more than 100K users globally. With 98% satisfaction rate.</p>
              <button className="btn-green">↗ About Us</button>
            </div>
            <div className="stats-numbers">
              <div className="stat-item">
                <p>Global users</p>
                <h4>100K</h4>
              </div>
              <div className="stat-item">
                <p>Satisfaction rate</p>
                <h4>98%</h4>
              </div>
            </div>
          </div>

          <div className="stats-img">
            <img src="/assets/hero.png" alt="Happy user" />
          </div>

          <div className="stats-testimonial">
            <div style={{ fontWeight: 700, fontSize: '24px', marginBottom: 24, display: 'flex', alignItems: 'center', gap: 8 }}>
              <div style={{ width: 20, height: 20, background: '#1a1a1a', borderRadius: 4 }}></div> Netdot
            </div>
            <p>"SmartLedger helps me monitor everything in real-time. It feels like having a financial expert beside me 24/7."</p>
            <div className="testimonial-author">
              <img src="/assets/hero.png" alt="Jane Smith" />
              <div className="testimonial-author-text">
                <h5>Jane Smith</h5>
                <p>Manager, Rwanda</p>
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
          <div className="tools-cards">
            <div className="tool-card">
              <h3>Intelligent budgeting</h3>
              <p>Establish and monitor budgets that adjust to your spending behavior. Our AI examines your financial trends.</p>
              <button className="btn-white">↗ Try Now</button>
              <img src="/assets/dashboard.png" alt="Budget Dashboard" className="tool-mockup" />
            </div>
            <div className="tool-card">
              <h3>Seamless multi-account financial management</h3>
              <ul>
                <li>Instant sync across all accounts</li>
                <li>Unified dashboard for total oversight</li>
                <li>Customizable reports for deep insights</li>
              </ul>
              <img src="/assets/dashboard.png" alt="Multi-account Dashboard" className="tool-mockup" style={{ bottom: '-40px' }} />
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
            <button className="btn-green">↗ Compare Plan</button>
          </div>

          <div className="pricing-cards">
            <div className="pricing-card">
              <div className="price-col">
                <h4>₹9<span style={{ fontSize: 16, color: '#666' }}>/mo</span></h4>
                <p>Per user • Student Pack</p>
                <button className="btn-white" style={{ border: '1px solid #ddd' }}>Get Started</button>
              </div>
              <div className="price-features">
                <ul style={{ listStyle: 'none', padding: 0 }}>
                  <li style={{ marginBottom: 8 }}>• Full access to financial tracking tools</li>
                  <li style={{ marginBottom: 8 }}>• AI-powered budget insights</li>
                  <li>• Single user account</li>
                </ul>
              </div>
              <div className="price-badge">Student</div>
            </div>

            <div className="pricing-card highlight">
              <div className="price-col">
                <h4>₹49<span style={{ fontSize: 16, color: '#666' }}>/mo</span></h4>
                <p>Per user • Individual</p>
                <button className="btn-white" style={{ border: '1px solid #ddd' }}>Get Started</button>
              </div>
              <div className="price-features">
                <ul style={{ listStyle: 'none', padding: 0 }}>
                  <li style={{ marginBottom: 8 }}>• Everything in Student Pack</li>
                  <li style={{ marginBottom: 8 }}>• Advanced analytics and insights</li>
                  <li>• Priority support & multi-account sync</li>
                </ul>
              </div>
              <div className="price-badge orange">Popular</div>
            </div>

            <div className="pricing-card">
              <div className="price-col">
                <h4 style={{ fontSize: 24 }}>Custom</h4>
                <p>Business Enterprise</p>
                <button className="btn-green">Contact Us</button>
              </div>
              <div className="price-features">
                <ul style={{ listStyle: 'none', padding: 0 }}>
                  <li style={{ marginBottom: 8 }}>• Everything in Individual plan</li>
                  <li style={{ marginBottom: 8 }}>• Custom features & integrations</li>
                  <li>• Unlimited users & dedicated support</li>
                </ul>
              </div>
              <div className="price-badge" style={{ background: '#1a1a1a' }}>Enterprise</div>
            </div>
          </div>
        </section>

        {/* TRUST */}
        <section className="trust-section" id="testimonials">
          <div className="trust-block">
            <h2>Users trust us to manage their finances.</h2>
            <button className="btn-white" style={{ border: '1px solid #ddd', alignSelf: 'flex-start', marginBottom: 40 }}>↗ Read Stories</button>

            <div className="trust-brands">
              <span style={{ fontWeight: 700, fontSize: 18 }}>travelers.</span>
            </div>

            <p className="trust-quote">"Working with this platform has transformed how I manage my business finances."</p>

            <div className="testimonial-author">
              <img src="/assets/hero.png" alt="Riya Alisara" />
              <div className="testimonial-author-text">
                <h5>Riya Alisara</h5>
                <p>CEO, Travelers</p>
              </div>
            </div>
          </div>

          <div className="trust-image">
            <img src="/assets/hero.png" alt="Happy User" />
            <div className="trust-image-overlay">
              <h3>Get ready to experience the future of personal finance</h3>
              <button className="btn-green">📞 Contact Us</button>
            </div>
          </div>
        </section>

        {/* FOOTER */}
        <footer className="landing-footer">
          <div className="footer-top">
            <h2>Transforming <span style={{ color: '#34d399', letterSpacing: '-2px' }}>⬤⬤⬤</span> finance with innovative, data-driven solutions</h2>
            <div className="footer-fluid-card">
              <span>Explore the future<br />of personal finance</span>
              <div style={{ position: 'absolute', bottom: 24, right: 24, width: 40, height: 40, background: '#34d399', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#064e3b', fontWeight: 'bold' }}>↗</div>
            </div>
          </div>

          <div style={{ display: 'flex', gap: 40, marginBottom: 40, fontWeight: 500 }}>
            <a href="#about" style={{ color: '#1a1a1a', textDecoration: 'none' }}>About</a>
            <a href="#features" style={{ color: '#1a1a1a', textDecoration: 'none' }}>Features</a>
            <a href="#pricing" style={{ color: '#1a1a1a', textDecoration: 'none' }}>Pricing</a>
            <a href="#support" style={{ color: '#1a1a1a', textDecoration: 'none' }}>Support</a>
          </div>

          <div className="footer-bottom">
            <div className="footer-links">
              <a href="#privacy">Privacy Policy</a>
              <a href="#terms">Terms & Conditions</a>
            </div>
            <div>
              hello@smartledger.com © 2026 SmartLedger. All rights reserved.
            </div>
          </div>
        </footer>
      </div>
    </div>
  );
}
