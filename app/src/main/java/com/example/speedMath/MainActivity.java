package com.example.speedMath;

import android.media.MediaPlayer;
import android.os.Bundle;

import com.example.speedMath.core.PlayerManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
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

}
