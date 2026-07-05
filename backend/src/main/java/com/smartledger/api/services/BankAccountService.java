package com.smartledger.api.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.smartledger.api.models.BankAccount;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class BankAccountService {

    private static final String COLLECTION_NAME = "bankaccounts";

    public String saveBankAccount(BankAccount entity) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        if (entity.getId() == null || entity.getId().isEmpty() || entity.getId().equals("0")) {
            DocumentReference docRef = dbFirestore.collection(COLLECTION_NAME).document();
            entity.setId(docRef.getId());
        }
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection(COLLECTION_NAME).document(entity.getId()).set(entity);
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public List<BankAccount> getAllBankAccounts() throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        CollectionReference collection = dbFirestore.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> future = collection.get();
        List<BankAccount> list = new ArrayList<>();
        for (DocumentSnapshot document : future.get().getDocuments()) {
            BankAccount entity = document.toObject(BankAccount.class);
            list.add(entity);
        }
        return list;
    }

    public String deleteBankAccount(String id) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> writeResult = dbFirestore.collection(COLLECTION_NAME).document(id).delete();
        return writeResult.get().getUpdateTime().toString();
    }
}
