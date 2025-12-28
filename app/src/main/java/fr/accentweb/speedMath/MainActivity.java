package fr.accentweb.speedMath;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Method;

import fr.accentweb.speedMath.core.PlayerManager;
import fr.accentweb.speedMath.ui.arcade.ArcadeFragment;
import fr.accentweb.speedMath.ui.arcade.OnlineStats;

public class MainActivity extends AppCompatActivity {

    private PlayerManager playerManager;
    private DatabaseReference statusRef;
    private OnlineStats onlineStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Appliquer le thème avant setContentView
        playerManager = PlayerManager.getInstance(this);
        boolean isDark = playerManager.isDarkModeEnabled();
        applyTheme(isDark);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configuration de la barre de statut pour les versions récentes
        configureSystemBars(isDark);


        BottomNavigationView navView = findViewById(R.id.bottomNav);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications
        ).build();

        if(playerManager.isMusicEnabled()) {
            playerManager.setMusicEnabled(true);
        }

        // Lien toolbar avec NavController pour gérer flèche Up
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // Gestion des insets pour le fragment host
        View navHostFragment = findViewById(R.id.nav_host_fragment);
        ViewCompat.setOnApplyWindowInsetsListener(navHostFragment, (v, insets) -> {
            // On ajuste uniquement le padding inférieur pour la barre de navigation
            int navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    navigationBarHeight
            );
            return insets;
        });

        onlineStats = new OnlineStats(0,0,0);

        statusRef = FirebaseDatabase.getInstance().getReference("status");

        setPlayerOnline();
        startListeningOnline();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playerManager.isMusicEnabled()) {
            playerManager.stopMusic();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(playerManager.isMusicEnabled()) {
            playerManager.startMusic();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(playerManager.isMusicEnabled()) {
            playerManager.stopMusic();
        }
    }

    public void setNavigationEnabled(boolean enabled) {
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setEnabled(enabled);
        nav.setClickable(enabled);

        // Optionnel : désactiver l'animation visuelle
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
                    .translationY(nav.getHeight())
                    .alpha(0.2f)
                    .setDuration(200)
                    .start();
        }
    }

    private void setPlayerOnline() {
        String uid = playerManager.getOnlineUid();
        if (uid == null || uid.isEmpty()) {
            uid = "anonymous_" + System.currentTimeMillis(); // ID temporaire si pas connecté
            playerManager.setOnlineUid(uid);
        }

        DatabaseReference myStatus = statusRef.child(uid);
        myStatus.setValue(true);
        myStatus.onDisconnect().removeValue(); // Supprime complètement à la déconnexion
    }

    private void startListeningOnline() {
        // Écouteur pour les joueurs en ligne
        statusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int onlineCount = 0;
                for (DataSnapshot child : snapshot.getChildren()) {
                    Boolean isOnline = child.getValue(Boolean.class);
                    if (isOnline != null && isOnline) {
                        onlineCount++;
                    }
                }

                if (onlineStats == null) {
                    onlineStats = new OnlineStats(0, 0, 10);
                }

                // Mise à jour du nombre de joueurs
                onlineStats.playersOnline = onlineCount;

                // Mise à jour de l'UI
                updateOnlineStats(onlineStats);

                Log.d("OnlineStats", "Joueurs en ligne: " + onlineCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Erreur de lecture des stats: " + error.getMessage());
            }
        });

        // Écouteur pour la limite quotidienne
        FirebaseDatabase.getInstance().getReference("system/daily_match_limit")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (onlineStats != null && snapshot.exists()) {
                            try {
                                Integer limit = snapshot.getValue(Integer.class);
                                if (limit != null) {
                                    onlineStats.dailyLimit = limit;
                                    updateOnlineStatsInFragments();
                                }
                            } catch (Exception e) {
                                Log.e("Firebase", "Erreur de parsing de la limite", e);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Erreur de lecture de la limite", error.toException());
                    }
                });
    }

    private void updateOnlineStatsInFragments() {
        // Récupération fiable du fragment ArcadeFragment
        androidx.fragment.app.Fragment navHost = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHost != null) {
            for (androidx.fragment.app.Fragment fragment : navHost.getChildFragmentManager().getFragments()) {
                if (fragment instanceof ArcadeFragment) {
                    ((ArcadeFragment) fragment).refreshOnlineStats(onlineStats);
                }
            }
        }
    }

    public OnlineStats getOnlineStats() {
        return onlineStats;
    }

    public void updateOnlineStats(OnlineStats stats) {
        this.onlineStats = stats;
        updateOnlineStatsInFragments();
    }

    private void configureSystemBars(boolean isDark) {
        Window window = getWindow();

        window.setStatusBarColor(
                ContextCompat.getColor(this,
                        isDark ? R.color.black : R.color.blue_primary)
        );

        window.setNavigationBarColor(
                ContextCompat.getColor(this,
                        isDark ? R.color.black : R.color.blue_primary)
        );

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());

        if (controller != null) {
            controller.setAppearanceLightStatusBars(!isDark);
            controller.setAppearanceLightNavigationBars(false);
        }
    }

}
