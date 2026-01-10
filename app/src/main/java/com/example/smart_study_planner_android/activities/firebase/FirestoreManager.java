package com.example.smart_study_planner_android.activities.firebase;

import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreManager {

    private static FirebaseFirestore db;

    public static FirebaseFirestore getDb() {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }
        return db;
    }
}
