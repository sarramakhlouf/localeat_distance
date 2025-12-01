package com.iset.dsi.localeat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iset.dsi.localeat.R;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNav;

    // Map pour associer menu item id à fragments
    private final Map<Integer, Fragment> fragmentMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        bottomNav = findViewById(R.id.bottom_nav);

        // Initialiser la Map
        fragmentMap.put(R.id.nav_home, new HomeFragment());
        fragmentMap.put(R.id.nav_favorites, new FragmentFavorites());
        fragmentMap.put(R.id.nav_profile, new FragmentProfile());

        // Fragment par défaut
        if (savedInstanceState == null) {
            replaceFragment(fragmentMap.get(R.id.nav_home));
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = fragmentMap.get(item.getItemId());
            if (fragment == null) {
                fragment = new HomeFragment(); // valeur par défaut si l'id n'existe pas
            }
            replaceFragment(fragment);
            return true;
        });

        // Drawer Navigation
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();

            if (id == R.id.menu_logout) {
                // Déconnexion utilisateur Firebase
                FirebaseAuth.getInstance().signOut();

                // Rediriger vers LoginActivity
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                // Fermer le drawer
                drawerLayout.closeDrawer(GravityCompat.START);

            }
            return true;
        });

        String fragmentToOpen = getIntent().getStringExtra("openFragment");
        if ("FragmentHome".equals(fragmentToOpen)) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

    }

    // Méthode utilitaire pour remplacer les fragments
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
