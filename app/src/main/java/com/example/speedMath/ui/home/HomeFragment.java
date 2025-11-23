package com.example.speedMath.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.speedMath.R;

public class HomeFragment extends Fragment {

    CardView cardAdd, cardSub, cardMul, cardDiv, cardAll, cardQCM, cardDual;
    TextView tvAdd, tvSub, tvMul, tvDiv, tvAll, tvQCM, tvDual;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        cardAdd = root.findViewById(R.id.cardAdd);
        cardSub = root.findViewById(R.id.cardSub);
        cardMul = root.findViewById(R.id.cardMul);
        cardDiv = root.findViewById(R.id.cardDiv);
        cardAll = root.findViewById(R.id.cardAll);
        cardQCM = root.findViewById(R.id.cardQCM);
        cardDual = root.findViewById(R.id.cardDual);
        tvAdd = cardAdd.findViewById(R.id.textButton);
        tvSub = cardSub.findViewById(R.id.textButton);
        tvMul = cardMul.findViewById(R.id.textButton);
        tvDiv = cardDiv.findViewById(R.id.textButton);
        tvAll = cardAll.findViewById(R.id.textButton);
        tvQCM = cardQCM.findViewById(R.id.textButton);
        tvDual = cardDual.findViewById(R.id.textButton);


        tvAdd.setText("a + b");
        tvSub.setText("a - b");
        tvMul.setText("a x b");
        tvDiv.setText("a ÷ b");
        tvAll.setText("+ - x ÷");
        tvQCM.setText("QCM");
        tvDual.setText("Dual");

        cardAdd.setOnClickListener(v -> openGame(v, "ADD"));
        cardSub.setOnClickListener(v -> openGame(v, "SUB"));
        cardMul.setOnClickListener(v -> openGame(v, "MUL"));
        cardDiv.setOnClickListener(v -> openGame(v, "DIV"));
        cardAll.setOnClickListener(v -> openGame(v, "ALL"));
        cardQCM.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_qcmFragment)
        );
        cardDual.setOnClickListener(v ->
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
