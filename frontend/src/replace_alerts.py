import re

with open('c:/Users/softl/OneDrive/Desktop/SmartLedger_v1.0-adx-main/frontend/src/Dashboard.jsx', 'r', encoding='utf-8') as f:
    content = f.read()

def replacer(match):
    msg = match.group(1)
    # determine type
    if any(word in msg.lower() for word in ['fail', 'error']):
        t = "'error'"
    elif any(word in msg.lower() for word in ['please', 'warning']):
        t = "'warning'"
    else:
        t = "'success'"
    return f"showToast({msg}, {t})"

content = re.sub(r'alert\((.*?)\)', replacer, content)

with open('c:/Users/softl/OneDrive/Desktop/SmartLedger_v1.0-adx-main/frontend/src/Dashboard.jsx', 'w', encoding='utf-8') as f:
    f.write(content)
