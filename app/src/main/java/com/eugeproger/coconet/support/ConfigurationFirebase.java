package com.eugeproger.coconet.support;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConfigurationFirebase {

    public static DatabaseReference setRealtimeDatabaseRef() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(Constant.REALTIME_DATABASE_LINK);
        return firebaseDatabase.getReference();
    }
}
