import re

with open('frontend/src/Dashboard.jsx', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace hardcoded colors with CSS variables
replacements = [
    (r"#1c2025", "var(--dash-card)"),
    (r"rgba\(255,255,255,0\.05\)", "var(--dash-border)"),
    (r"rgba\(255,\s*255,\s*255,\s*0\.05\)", "var(--dash-border)"),
    (r"#0a0d10", "var(--dash-bg)"),
    (r"#121619", "var(--dash-sidebar)"),
    # Careful with #fff and #8892b0
    (r"color:\s*'#fff'", "color: 'var(--dash-text)'"),
    (r"color:\s*'#8892b0'", "color: 'var(--dash-text-muted)'"),
    (r"background:\s*'rgba\(255,255,255,0\.1\)'", "background: 'var(--dash-border-strong)'"),
    (r"color:\s*'#000'", "color: 'var(--dash-text-inverse)'"),
]

for old, new in replacements:
    content = re.sub(old, new, content)

# Change default theme to dark
content = content.replace("useState('light'); // default to light theme", "useState('dark'); // default to dark theme")

# Find where to inject the button. It's in the header flex box.
# <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
#    {!activeModule.isSummary && (

target_insertion = "{!activeModule.isSummary && ("
button_code = """<button onClick={toggleTheme} style={{
                            width: '44px', height: '44px', borderRadius: '50%',
                            background: 'var(--dash-card)', border: '1px solid var(--dash-border)',
                            color: 'var(--dash-text)', cursor: 'pointer', display: 'flex',
                            alignItems: 'center', justifyContent: 'center', fontSize: '20px',
                            boxShadow: '0 4px 15px rgba(0,0,0,0.1)', transition: 'all 0.3s ease'
                        }} title="Toggle Theme">
                            {theme === 'dark' ? '☀️' : '🌙'}
                        </button>
                        
                        """

content = content.replace(target_insertion, button_code + target_insertion)

with open('frontend/src/Dashboard.jsx', 'w', encoding='utf-8') as f:
    f.write(content)

with open('frontend/src/index.css', 'a', encoding='utf-8') as f:
    f.write('''
[data-theme='dark'] {
  --dash-bg: #0a0d10;
  --dash-sidebar: #121619;
  --dash-card: #1c2025;
  --dash-text: #fff;
  --dash-text-muted: #8892b0;
  --dash-border: rgba(255,255,255,0.05);
  --dash-border-strong: rgba(255,255,255,0.1);
  --dash-text-inverse: #000;
}

[data-theme='light'] {
  --dash-bg: #f8fafc;
  --dash-sidebar: #ffffff;
  --dash-card: #ffffff;
  --dash-text: #0f172a;
  --dash-text-muted: #475569;
  --dash-border: rgba(0,0,0,0.1);
  --dash-border-strong: rgba(0,0,0,0.15);
  --dash-text-inverse: #fff;
}
''')
print("Done.")
