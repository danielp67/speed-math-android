package com.example.speedMath;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.speedMath.ui.home.HomeFragment;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        String mode = getIntent().getStringExtra("MODE");

        Bundle args = new Bundle();
        args.putString("MODE", mode);

        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
