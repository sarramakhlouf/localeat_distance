package com.iset.dsi.localeat;
import android.app.Application;
import com.google.firebase.FirebaseApp;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialisation Firebase
        FirebaseApp.initializeApp(this);
    }
}

