const fs = require('fs');
const path = require('path');

const backendDir = 'c:\\Users\\softl\\OneDrive\\Desktop\\SmartLedger_v1.0-adx-main\\backend\\src\\main\\java\\com\\smartledger\\api';
const controllersDir = path.join(backendDir, 'controllers');
const servicesDir = path.join(backendDir, 'services');

const capitalize = s => s.charAt(0).toUpperCase() + s.slice(1);

// Update Services
fs.readdirSync(servicesDir).forEach(file => {
    if (!file.endsWith('Service.java')) return;
    
    let content = fs.readFileSync(path.join(servicesDir, file), 'utf8');
    
    // Check if delete method already exists
    if (content.includes('public String delete')) return;
    
    const entityName = file.replace('Service.java', '');
    const deleteMethod = `
    public String delete${entityName}(String id) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> writeResult = dbFirestore.collection(COLLECTION_NAME).document(id).delete();
        return writeResult.get().getUpdateTime().toString();
    }
}`;

    // Insert before the last brace
    const lastBraceIndex = content.lastIndexOf('}');
    content = content.substring(0, lastBraceIndex) + deleteMethod + '\n}' + content.substring(lastBraceIndex + 1);
    
    fs.writeFileSync(path.join(servicesDir, file), content, 'utf8');
    console.log('Updated service:', file);
});

// Update Controllers
fs.readdirSync(controllersDir).forEach(file => {
    if (!file.endsWith('Controller.java')) return;
    
    let content = fs.readFileSync(path.join(controllersDir, file), 'utf8');
    
    // Check if delete method already exists
    if (content.includes('@DeleteMapping')) return;
    
    const entityName = file.replace('Controller.java', '');
    
    // Try to find the service variable name
    const serviceMatch = content.match(/private\s+\w+Service\s+(\w+);/);
    const serviceVarName = serviceMatch ? serviceMatch[1] : 'service';
    
    const deleteEndpoint = `
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        try {
            return ResponseEntity.ok(${serviceVarName}.delete${entityName}(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error deleting: " + e.getMessage());
        }
    }
`;

    const lastBraceIndex = content.lastIndexOf('}');
    content = content.substring(0, lastBraceIndex) + deleteEndpoint + '\n}' + content.substring(lastBraceIndex + 1);
    
    fs.writeFileSync(path.join(controllersDir, file), content, 'utf8');
    console.log('Updated controller:', file);
});
