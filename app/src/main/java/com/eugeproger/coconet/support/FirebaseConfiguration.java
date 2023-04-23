package com.eugeproger.coconet.support;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseConfiguration {

    public static DatabaseReference setRealtimeDatabaseConfiguration() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(Constant.REALTIME_DATABASE_LINK);
        return firebaseDatabase.getReference();
    }
}
