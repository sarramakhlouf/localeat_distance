package com.iset.dsi.localeat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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

    private final HomeFragment homeFragment = new HomeFragment();
    private final FragmentFavorites favoritesFragment = new FragmentFavorites();
    private final FragmentProfile profileFragment = new FragmentProfile();

    private Fragment activeFragment = homeFragment; // fragment actif actuel

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        bottomNav = findViewById(R.id.bottom_nav);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }

        // Ajouter tous les fragments au container, mais cacher ceux qui ne sont pas actifs
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, profileFragment).hide(profileFragment)
                .add(R.id.fragment_container, favoritesFragment).hide(favoritesFragment)
                .add(R.id.fragment_container, homeFragment)
                .commit();

        bottomNav.setOnItemSelectedListener(item -> {
            switchFragment(item.getItemId());
            return true;
        });

        // Drawer Navigation
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.menu_logout) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            return true;
        });

        // Ouvrir un fragment spécifique si demandé
        String fragmentToOpen = getIntent().getStringExtra("openFragment");
        if ("FragmentHome".equals(fragmentToOpen)) {
            switchFragment(R.id.nav_home);
        }
    }

    // Méthode pour changer de fragment
    private void switchFragment(int itemId) {
        Fragment fragmentToShow = null;
        if (itemId == R.id.nav_home) fragmentToShow = homeFragment;
        else if (itemId == R.id.nav_favorites) fragmentToShow = favoritesFragment;
        else if (itemId == R.id.nav_profile) fragmentToShow = profileFragment;

        if (fragmentToShow != null && fragmentToShow != activeFragment) {
            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(fragmentToShow)
                    .commit();
            activeFragment = fragmentToShow;
        }
    }

    public void showParentFragment(String parentName) {
        if ("FragmentFavorites".equals(parentName)) {
            switchFragment(R.id.nav_favorites);
        } else {
            switchFragment(R.id.nav_home);
        }
    }

}

