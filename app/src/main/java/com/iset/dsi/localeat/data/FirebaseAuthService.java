package com.iset.dsi.localeat.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.iset.dsi.localeat.models.User;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FirebaseAuthService {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    // -------------------- SIGNUP -------------------------
    public void signup(Context context, String email, String password, String name, String phone, Uri imageUri,
                       OnSuccessListener<Void> onSuccess,
                       OnFailureListener onFailure) {

        // 1️⃣ Créer l'utilisateur dans Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {

                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser == null) {
                        onFailure.onFailure(new Exception("Utilisateur non créé"));
                        return;
                    }

                    String uid = firebaseUser.getUid();

                    String imageBase64 = null;
                    if (imageUri != null) {
                        imageBase64 = convertImageToBase64(context,imageUri);
                    }

                    saveUser(uid, name, email, phone, imageBase64, onSuccess, onFailure);
                })
                .addOnFailureListener(onFailure);
    }

    // -------------------- LOGIN --------------------------
    public void loginWithEmailOrUsername(Context context, String emailOrUsername, String password,
                                         OnSuccessListener<FirebaseUser> onSuccess,
                                         OnFailureListener onFailure) {

        if (emailOrUsername.contains("@")) {
            // Connexion directe par email
            auth.signInWithEmailAndPassword(emailOrUsername, password)
                    .addOnSuccessListener(result -> onSuccess.onSuccess(result.getUser()))
                    .addOnFailureListener(onFailure);
        } else {
            // Connexion par username : recherche dans Firestore
            db.collection("users")
                    .whereEqualTo("name", emailOrUsername)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            String email = querySnapshot.getDocuments().get(0).getString("email");
                            auth.signInWithEmailAndPassword(email, password)
                                    .addOnSuccessListener(result -> onSuccess.onSuccess(result.getUser()))
                                    .addOnFailureListener(onFailure);
                        } else {
                            onFailure.onFailure(new Exception("Username introuvable"));
                        }
                    })
                    .addOnFailureListener(onFailure);
        }
    }

    // -------------------- CONVERT IMAGE TO BASE64 -----------------
    private String convertImageToBase64(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // -------------------- FIRESTORE SAVE -----------------
    private void saveUser(String uid, String name, String email, String phone, String imageBase64,
                          OnSuccessListener<Void> onSuccess,
                          OnFailureListener onFailure) {

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("uid", uid);
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("phone", phone);
        userMap.put("photoBase64", imageBase64);

        db.collection("users")
                .document(uid)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Utilisateur ajouté avec succès dans Firestore");
                    onSuccess.onSuccess(aVoid);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Erreur lors de l'ajout de l'utilisateur: " + e.getMessage());
                    onFailure.onFailure(e);
                });
    }

    // -------------------- FORGOT PASSWORD -----------------
    public void recoverPassword(String email,
                                OnSuccessListener<Void> onSuccess,
                                OnFailureListener onFailure) {

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // Mettre à jour le displayName
    public void updateUsername(String username, OnCompleteListener<Void> listener) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(listener);
        }
    }

    // Mettre à jour l'email
    public void updateEmail(String email, OnCompleteListener<Void> listener) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            user.updateEmail(email)
                    .addOnCompleteListener(listener);
        }
    }

    // Mettre à jour le mot de passe
    public void updatePassword(String newPassword, OnCompleteListener<Void> listener) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(listener);
        }
    }
}
