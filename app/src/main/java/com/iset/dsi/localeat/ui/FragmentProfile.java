package com.iset.dsi.localeat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iset.dsi.localeat.R;

public class FragmentProfile extends Fragment {

    private ImageView imgProfile, menuBtn;
    private TextView tvUsername, tvEmail;
    private Button btnEditProfile;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imgProfile = view.findViewById(R.id.imgProfile);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        menuBtn = view.findViewById(R.id.menuBtn);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Récupérer DrawerLayout et NavigationView depuis l'activité parent
        drawerLayout = requireActivity().findViewById(R.id.drawer_layout);
        navigationView = requireActivity().findViewById(R.id.navigation_view);

        FirebaseUser user = auth.getCurrentUser();

        // Ouvrir le drawer au clic sur le bouton
        menuBtn.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        // Gérer les clics sur le menu
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();

            if (id == R.id.menu_logout) {
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        });

        if (user != null) {
            tvEmail.setText(user.getEmail() != null ? user.getEmail() : "email@gmail.com");

            // Récupérer le document Firestore correspondant à l'UID
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String name = document.getString("name");
                            String imageUrl = document.getString("imageUrl");

                            tvUsername.setText(name != null ? name : "Username");

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(imageUrl)
                                        .into(imgProfile);
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Erreur récupération profil: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        }

        // Redirection vers EditProfileFragment
        btnEditProfile.setOnClickListener(v -> {
            FragmentEditProfile editProfileFragment = new FragmentEditProfile();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, editProfileFragment);
            transaction.addToBackStack(null); // permet le retour au profil
            transaction.commit();
        });

        return view;
    }
}
