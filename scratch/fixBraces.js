const fs = require('fs');
const path = require('path');

const backendDir = 'c:\\Users\\softl\\OneDrive\\Desktop\\SmartLedger_v1.0-adx-main\\backend\\src\\main\\java\\com\\smartledger\\api';
const controllersDir = path.join(backendDir, 'controllers');
const servicesDir = path.join(backendDir, 'services');

function fixDir(dir) {
    fs.readdirSync(dir).forEach(file => {
        if (!file.endsWith('.java')) return;
        const filePath = path.join(dir, file);
        let content = fs.readFileSync(filePath, 'utf8');
        
        // Remove trailing spaces/newlines and check for double closing braces
        content = content.replace(/}\r?\n}\r?\n?$/, '}\n');
        
        fs.writeFileSync(filePath, content, 'utf8');
        console.log('Fixed:', file);
    });
}

fixDir(controllersDir);
fixDir(servicesDir);
