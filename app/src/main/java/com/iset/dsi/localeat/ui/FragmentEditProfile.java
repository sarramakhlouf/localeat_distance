package com.iset.dsi.localeat.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iset.dsi.localeat.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class FragmentEditProfile extends Fragment {

    private ImageView profileImage, btnAddPhoto;
    private EditText etUsername, etEmail, etOldPassword, etNewPassword;
    private Button btnSave;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            selectedImageUri = result.getData().getData();
                            profileImage.setImageURI(selectedImageUri);
                        }
                    });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        profileImage = view.findViewById(R.id.profileImage);
        btnAddPhoto = view.findViewById(R.id.btnAddPhoto);
        etUsername = view.findViewById(R.id.inputUsername);
        etEmail = view.findViewById(R.id.inputEmail);
        etOldPassword = view.findViewById(R.id.inputOldPassword);
        etNewPassword = view.findViewById(R.id.inputNewPassword);
        btnSave = view.findViewById(R.id.btnLogin);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadUserProfile();

        // Sélection de la photo
        btnAddPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });

        // Sauvegarder les modifications
        btnSave.setOnClickListener(v -> saveProfile());

        return view;
    }

    private void loadUserProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        String email = document.getString("email");
                        String photoBase64 = document.getString("photoBase64");

                        etUsername.setText(name != null ? name : "");
                        etEmail.setText(email != null ? email : "");

                        if (!TextUtils.isEmpty(photoBase64)) {
                            byte[] decodedBytes = Base64.decode(photoBase64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                            profileImage.setImageBitmap(bitmap);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Erreur récupération profil: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private String convertImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void saveProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String newName = etUsername.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();
        String oldPassword = etOldPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();

        if (TextUtils.isEmpty(oldPassword)) {
            etOldPassword.setError("Entrez votre ancien mot de passe");
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(getContext(), "Mot de passe incorrect", Toast.LENGTH_LONG).show();
                return;
            }

            // Modifier le mot de passe
            if (!TextUtils.isEmpty(newPassword)) {
                user.updatePassword(newPassword);
            }

            // Modifier l’email
            if (!TextUtils.isEmpty(newEmail) && !newEmail.equals(user.getEmail())) {
                user.updateEmail(newEmail);
            }

            // Modifier nom et photo
            UserProfileChangeRequest.Builder profileUpdates = new UserProfileChangeRequest.Builder();
            profileUpdates.setDisplayName(newName);
            if (selectedImageUri != null) {
                profileUpdates.setPhotoUri(selectedImageUri);
            }

            user.updateProfile(profileUpdates.build()).addOnCompleteListener(profileTask -> {
                if (profileTask.isSuccessful()) {
                    String imageBase64 = selectedImageUri != null ? convertImageToBase64(selectedImageUri) : "";

                    db.collection("users").document(user.getUid())
                            .update(
                                    "name", newName,
                                    "email", newEmail,
                                    "photoBase64", imageBase64
                            )
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Profil mis à jour", Toast.LENGTH_SHORT).show();

                                // Redirection vers FragmentProfile
                                FragmentProfile profileFragment = new FragmentProfile();
                                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment_container, profileFragment);
                                transaction.commit();
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Erreur Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            });
        });
    }
}
