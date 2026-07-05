import os
import re

SRC_DIR = 'src'
DEST_API_DIR = 'backend/src/main/java/com/smartledger/api'

MODELS_DIR = os.path.join(DEST_API_DIR, 'models')
SERVICES_DIR = os.path.join(DEST_API_DIR, 'services')
CONTROLLERS_DIR = os.path.join(DEST_API_DIR, 'controllers')

os.makedirs(MODELS_DIR, exist_ok=True)
os.makedirs(SERVICES_DIR, exist_ok=True)
os.makedirs(CONTROLLERS_DIR, exist_ok=True)

# We will skip Transaction since it already exists and is working
models = [
    'BankAccount', 'Card', 'Deposit', 'GoldSilverInvestment', 
    'Investment', 'Lending', 'Loan', 'MutualFund', 'TaxProfile'
]

for model in models:
    java_file = os.path.join(SRC_DIR, f"{model}.java")
    if not os.path.exists(java_file):
        continue
    
    with open(java_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Extract fields
    # Match lines like: private String type; or private double amount;
    # But some classes have fields initialized like: private int id = 0; or don't use private
    # Let's just find `private <Type> <name>;`
    fields_raw = re.findall(r'private\s+([a-zA-Z0-9<>_]+)\s+([a-zA-Z0-9_]+)', content)
    
    fields = []
    has_id = False
    for type_name, var_name in fields_raw:
        if var_name == 'id':
            has_id = True
            fields.append(('String', 'id')) # Convert ID to String for Firestore
        else:
            fields.append((type_name, var_name))
            
    if not has_id:
        fields.insert(0, ('String', 'id'))

    # Generate Model
    model_content = f"""package com.smartledger.api.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class {model} {{
"""
    for type_name, var_name in fields:
        model_content += f"    private {type_name} {var_name};\n"
    model_content += "}\n"
    
    with open(os.path.join(MODELS_DIR, f"{model}.java"), 'w', encoding='utf-8') as f:
        f.write(model_content)
        
    # Generate Service
    service_content = f"""package com.smartledger.api.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.smartledger.api.models.{model};
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class {model}Service {{

    private static final String COLLECTION_NAME = "{model.lower()}s";

    public String save{model}({model} entity) throws ExecutionException, InterruptedException {{
        Firestore dbFirestore = FirestoreClient.getFirestore();
        if (entity.getId() == null || entity.getId().isEmpty() || entity.getId().equals("0")) {{
            DocumentReference docRef = dbFirestore.collection(COLLECTION_NAME).document();
            entity.setId(docRef.getId());
        }}
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection(COLLECTION_NAME).document(entity.getId()).set(entity);
        return collectionsApiFuture.get().getUpdateTime().toString();
    }}

    public List<{model}> getAll{model}s() throws ExecutionException, InterruptedException {{
        Firestore dbFirestore = FirestoreClient.getFirestore();
        CollectionReference collection = dbFirestore.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> future = collection.get();
        List<{model}> list = new ArrayList<>();
        for (DocumentSnapshot document : future.get().getDocuments()) {{
            {model} entity = document.toObject({model}.class);
            list.add(entity);
        }}
        return list;
    }}
}}
"""
    with open(os.path.join(SERVICES_DIR, f"{model}Service.java"), 'w', encoding='utf-8') as f:
        f.write(service_content)

    # Generate Controller
    controller_content = f"""package com.smartledger.api.controllers;

import com.smartledger.api.models.{model};
import com.smartledger.api.services.{model}Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/{model.lower()}s")
@CrossOrigin(origins = "*")
public class {model}Controller {{

    @Autowired
    private {model}Service service;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody {model} entity) {{
        try {{
            return ResponseEntity.ok(service.save{model}(entity));
        }} catch (Exception e) {{
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }}
    }}

    @GetMapping
    public List<{model}> getAll() throws ExecutionException, InterruptedException {{
        return service.getAll{model}s();
    }}
}}
"""
    with open(os.path.join(CONTROLLERS_DIR, f"{model}Controller.java"), 'w', encoding='utf-8') as f:
        f.write(controller_content)

print("Generated Models, Services, Controllers successfully!")
