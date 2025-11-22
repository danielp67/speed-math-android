package com.example.speedMath.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.speedMath.R;

public class HomeFragment extends Fragment {

    Button btnAdd, btnSub, btnMul, btnDiv, btnAll, btnQCM, btnDual;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        btnAdd = root.findViewById(R.id.btnAdd);
        btnSub = root.findViewById(R.id.btnSub);
        btnMul = root.findViewById(R.id.btnMul);
        btnDiv = root.findViewById(R.id.btnDiv);
        btnAll = root.findViewById(R.id.btnAll);
        btnQCM = root.findViewById(R.id.btnQCM);
        btnDual = root.findViewById(R.id.btnDual);


        btnAdd.setOnClickListener(v -> openGame(v, "ADD"));
        btnSub.setOnClickListener(v -> openGame(v, "SUB"));
        btnMul.setOnClickListener(v -> openGame(v, "MUL"));
        btnDiv.setOnClickListener(v -> openGame(v, "DIV"));
        btnAll.setOnClickListener(v -> openGame(v, "ALL"));
        btnQCM.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_qcmFragment)
        );
        btnDual.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_dualFragment)
        );
        return root;
    }

    private void openGame(View v, String mode){
        Bundle args = new Bundle();
        args.putString("MODE", mode);

        Navigation.findNavController(v)
                .navigate(R.id.action_navigation_home_to_gameFragment, args);


    }
}
