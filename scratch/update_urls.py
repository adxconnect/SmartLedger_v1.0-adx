import os
import re

src_dir = r'c:\Users\softl\OneDrive\Desktop\SmartLedger_v1.0-adx-main\frontend\src'

config_content = '''export const API_BASE_URL_PYTHON = import.meta.env.VITE_PYTHON_API_URL || 'http://localhost:8000';
export const API_BASE_URL_JAVA = import.meta.env.VITE_JAVA_API_URL || 'http://localhost:8080';
'''

with open(os.path.join(src_dir, 'config.js'), 'w', encoding='utf-8') as f:
    f.write(config_content)

for root, _, files in os.walk(src_dir):
    for file in files:
        if file.endswith(('.jsx', '.js', '.ts', '.tsx')) and file != 'config.js':
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            if 'http://localhost:8000' in content or 'http://localhost:8080' in content:
                # Add import if not exists
                if 'from "./config"' not in content and "from './config'" not in content:
                    rel_path = os.path.relpath(os.path.join(src_dir, 'config'), root).replace('\\', '/')
                    if not rel_path.startswith('.'):
                        rel_path = './' + rel_path
                    import_stmt = f"import {{ API_BASE_URL_PYTHON, API_BASE_URL_JAVA }} from '{rel_path}';\n"
                    
                    imports = [m for m in re.finditer(r'^import .*?;?\n', content, re.MULTILINE)]
                    if imports:
                        last_import = imports[-1]
                        content = content[:last_import.end()] + import_stmt + content[last_import.end():]
                    else:
                        content = import_stmt + content
                
                # Use regex to safely replace all string definitions
                content = re.sub(r"(?<!`)['\"]http://localhost:8000(.*?)['\"]", r"`${API_BASE_URL_PYTHON}\1`", content)
                content = re.sub(r"(?<!`)['\"]http://localhost:8080(.*?)['\"]", r"`${API_BASE_URL_JAVA}\1`", content)
                
                # Replace literal matches inside already templated strings
                content = content.replace("http://localhost:8000", "${API_BASE_URL_PYTHON}")
                content = content.replace("http://localhost:8080", "${API_BASE_URL_JAVA}")
                
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(content)
print("Updated all files.")
