package com.smartledger.api.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.smartledger.api.models.Transaction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class TransactionService {

    private static final String COLLECTION_NAME = "transactions";

    public String saveTransaction(Transaction transaction) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        
        // Let Firestore generate an ID if not provided
        DocumentReference docRef;
        if (transaction.getId() == null || transaction.getId().isEmpty()) {
            docRef = dbFirestore.collection(COLLECTION_NAME).document();
            transaction.setId(docRef.getId());
        } else {
            docRef = dbFirestore.collection(COLLECTION_NAME).document(transaction.getId());
        }

        ApiFuture<WriteResult> collectionsApiFuture = docRef.set(transaction);
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public List<Transaction> getAllTransactions() throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = dbFirestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        
        List<Transaction> transactions = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            transactions.add(document.toObject(Transaction.class));
        }
        return transactions;
    }

    public String deleteTransaction(String id) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> writeResult = dbFirestore.collection(COLLECTION_NAME).document(id).delete();
        return writeResult.get().getUpdateTime().toString();
    }
}
