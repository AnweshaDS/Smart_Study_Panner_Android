package com.example.smart_study_planner_android.activities.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseManager {

    private static FirebaseAuth auth;
    private static FirebaseFirestore db;

    public static FirebaseAuth auth() {
        if (auth == null) auth = FirebaseAuth.getInstance();
        return auth;
    }

    public static FirebaseFirestore db() {
        if (db == null) db = FirebaseFirestore.getInstance();
        return db;
    }
}
