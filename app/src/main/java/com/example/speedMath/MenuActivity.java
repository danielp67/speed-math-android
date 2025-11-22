package com.example.speedMath;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    Button btnAdd, btnSub, btnMul, btnDiv, btnAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btnAdd = findViewById(R.id.btnAdd);
        btnSub = findViewById(R.id.btnSub);
        btnMul = findViewById(R.id.btnMul);
        btnDiv = findViewById(R.id.btnDiv);
        btnAll = findViewById(R.id.btnAll);

        btnAdd.setOnClickListener(v -> openGame("ADD"));
        btnSub.setOnClickListener(v -> openGame("SUB"));
        btnMul.setOnClickListener(v -> openGame("MUL"));
        btnDiv.setOnClickListener(v -> openGame("DIV"));
        btnAll.setOnClickListener(v -> openGame("ALL"));
    }

    private void openGame(String mode) {
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra("MODE", mode);
        startActivity(i);
    }
}
