package com.example.erik.destination;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Erik on 3/21/2017.
 */

public class FirebaseOffline extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}