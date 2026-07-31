package fr.accentweb.speedMath;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import fr.accentweb.speedMath.core.PlayerManager;
import fr.accentweb.speedMath.ui.arcade.ArcadeFragment;
import fr.accentweb.speedMath.ui.arcade.OnlineStats;

public class MainActivity extends AppCompatActivity {

    private PlayerManager playerManager;
    private DatabaseReference statusRef;
    private OnlineStats onlineStats;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setContentView
        playerManager = PlayerManager.getInstance(this);
        boolean isDark = playerManager.isDarkModeEnabled();
        applyTheme(isDark);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkForUpdates();
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d("MainActivity", "Allowed notification");
                    } else {
                        Log.d("MainActivity", "Not allowed notification");
                    }
                }
        );

        if (Build.VERSION.SDK_INT >= 33) {
            if (playerManager.isNotificationEnabled() &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED) {

                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }


        if (getIntent().getBooleanExtra("open_daily", false)) {
            NavController navController =
                    Navigation.findNavController(this, R.id.nav_host_fragment);
            navController.navigate(R.id.action_navigation_home_to_dailyChallengeFragment);
        }
        // Configure system bars
        configureSystemBars(isDark);


        BottomNavigationView navView = findViewById(R.id.bottomNav);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_stats,
                R.id.navigation_notifications
        ).build();

        if(playerManager.isMusicEnabled()) {
            playerManager.setMusicEnabled(true);
        }

        // Setup action bar
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // Setup nav host fragment
        View navHostFragment = findViewById(R.id.nav_host_fragment);
        ViewCompat.setOnApplyWindowInsetsListener(navHostFragment, (v, insets) -> {
            // Set padding bottom for navigation bar height
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
        createNotificationChannel();
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

        // Option : Disable navigation items
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
            uid = "anonymous_" + System.currentTimeMillis(); // ID temporary if not connected
            playerManager.setOnlineUid(uid);
        }

        DatabaseReference myStatus = statusRef.child(uid);
        myStatus.setValue(true);
        myStatus.onDisconnect().removeValue(); // Remove value when disconnected
    }

    private void startListeningOnline() {
        // Listen for online players
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

                // Update online player
                onlineStats.playersOnline = onlineCount;

                // Update Fragment
                updateOnlineStatsInFragments();

                Log.d("OnlineStats", "Online players: " + onlineCount);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error listening online players : " + error.getMessage());
            }
        });

        // Listen for daily match limit
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
                                Log.e("Firebase", "Error parsing daily match limit", e);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Error listening daily match limit", error.toException());
                    }
                });
    }

    private void updateOnlineStatsInFragments() {
        // Update ArcadeFragment
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

    private void checkForUpdates() {

        FirebaseDatabase.getInstance()
                .getReference("system/update_message")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {

                        Boolean enabled = snap.child("enabled").getValue(Boolean.class);
                        if (enabled == null || !enabled) return;

                        String title = snap.child("title").getValue(String.class);
                        String message = snap.child("message").getValue(String.class);
                        String buttonLabel = snap.child("button_label").getValue(String.class);
                        String actionUrl = snap.child("action_url").getValue(String.class);
                        Integer latestVersion = snap.child("latest_version").getValue(Integer.class);
                        Boolean mandatory = snap.child("mandatory").getValue(Boolean.class);

                            if (latestVersion != null &&
                                    latestVersion > BuildConfig.VERSION_CODE &&
                                    (!playerManager.hasPopupBeenSeen(latestVersion) ||  (mandatory!=null && mandatory ))) {

                                showUnifiedPopup(
                                        title,
                                        message,
                                        buttonLabel != null ? buttonLabel : "Update",
                                        actionUrl,
                                        mandatory != null && mandatory
                                );
                                playerManager.markPopupAsSeen(latestVersion);

                            }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Message fetch failed", error.toException());
                    }
                });
    }


    private void showUnifiedPopup(
            String title,
            String message,
            String buttonLabel,
            String actionUrl,
            boolean mandatory
    ) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.SpeedMath_Dialog)
                .setTitle(title != null ? title : "Information")
                .setMessage(message != null ? message : "")
                .setCancelable(!mandatory)
                .setPositiveButton(buttonLabel, (dialog, which) -> {

                    if (actionUrl != null && !actionUrl.isEmpty()) {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(actionUrl)));
                        } catch (Exception ignored) {}
                    }

                    if (mandatory) finish();
                });

        if (!mandatory) {
            builder.setNegativeButton("Close", (d, w) -> d.dismiss());
        }

        builder.show();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "daily_channel",
                    "Daily Challenge",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Daily reminders & rewards");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }


}
