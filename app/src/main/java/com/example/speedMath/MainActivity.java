package com.example.speedMath;

import static androidx.core.graphics.drawable.DrawableCompat.applyTheme;

import android.os.Bundle;
import android.view.View;

import com.example.speedMath.core.PlayerManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    private PlayerManager playerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.bottomNav);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications
        ).build();

        playerManager = PlayerManager.getInstance(this);

        if(playerManager.isMusicEnabled())
        {
            playerManager.setMusicEnabled(true);
        }

        // Charger l'état actuel
        boolean isDark = playerManager.isDarkModeEnabled();

        applyTheme(isDark);
        // Lien toolbar avec NavController pour gérer flèche Up
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    // Cette méthode permet à la flèche Up de fonctionner
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playerManager.isMusicEnabled()) {
            playerManager.setMusicEnabled(false);
        }
    }

    public void setNavigationEnabled(boolean enabled) {
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setEnabled(enabled);
        nav.setClickable(enabled);

        // Optionnel : désactiver l’animation visuelle
        for (int i = 0; i < nav.getMenu().size(); i++) {
            nav.getMenu().getItem(i).setEnabled(enabled);
        }
    }
    private void applyTheme(boolean isDark) {
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    public void animateNavigation(boolean show) {
        View nav = findViewById(R.id.bottomNav);
        if (nav == null) return;

        if (show) {
            nav.animate()
                    .translationY(0)
                    .alpha(1)
                    .setDuration(200)
                    .start();
        } else {
            nav.animate()
                    .translationY(nav.getHeight() )
                    .alpha(0.2f)
                    .setDuration(200)
                    .start();
        }
    }

}
