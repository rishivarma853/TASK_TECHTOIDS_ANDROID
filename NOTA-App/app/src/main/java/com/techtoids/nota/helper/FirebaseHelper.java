package com.techtoids.nota.helper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseHelper {
    static FirebaseFirestore db = FirebaseFirestore.getInstance();
    static CollectionReference boardsCollection = db.collection("boards");
    static CollectionReference tasksCollection = db.collection("tasks");

    public static FirebaseFirestore getDb() {
        return db;
    }

    public static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static boolean isSignedIn() {
        return getCurrentUser() != null;
    }

    public static CollectionReference getBoardsCollection() {
        return boardsCollection;
    }

    public static CollectionReference getTasksCollection() {
        return tasksCollection;
    }
}
