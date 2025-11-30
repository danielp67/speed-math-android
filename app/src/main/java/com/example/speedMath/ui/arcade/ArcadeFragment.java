package com.example.speedMath.ui.arcade;

import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.speedMath.R;
import com.example.speedMath.core.PlayerManager;

public class ArcadeFragment extends Fragment {

    CardView cardAdd, cardSub, cardMul, cardDiv, cardAll, cardQCM, cardDual;
    TextView iconAdd, titleAdd, descriptionAdd;
    TextView iconSub, titleSub, descriptionSub;
    TextView iconMul, titleMul, descriptionMul;
    TextView iconDiv, titleDiv, descriptionDiv;
    TextView iconAll, titleAll, descriptionAll;
    TextView iconQCM, titleQCM, descriptionQCM;
    TextView iconDual, titleDual, descriptionDual;
    private PlayerManager playerManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_arcade, container, false);
        playerManager = PlayerManager.getInstance(requireContext());

        cardAdd = root.findViewById(R.id.cardAdd);
        cardSub = root.findViewById(R.id.cardSub);
        cardMul = root.findViewById(R.id.cardMul);
        cardDiv = root.findViewById(R.id.cardDiv);
        cardAll = root.findViewById(R.id.cardAll);
        cardQCM = root.findViewById(R.id.cardQCM);
        cardDual = root.findViewById(R.id.cardDual);


        iconAdd = cardAdd.findViewById(R.id.iconCard);
        titleAdd = cardAdd.findViewById(R.id.titleCard);
        descriptionAdd = cardAdd.findViewById(R.id.descriptionCard);
        iconAdd.setText(R.string.icon_add);
        titleAdd.setText(R.string.title_add);
        descriptionAdd.setText(R.string.description_add);

        iconSub = cardSub.findViewById(R.id.iconCard);
        titleSub = cardSub.findViewById(R.id.titleCard);
        descriptionSub = cardSub.findViewById(R.id.descriptionCard);
        iconSub.setText("-");
        titleSub.setText("Subtraction Suite");
        descriptionSub.setText("a - b");

        iconMul = cardMul.findViewById(R.id.iconCard);
        titleMul = cardMul.findViewById(R.id.titleCard);
        descriptionMul = cardMul.findViewById(R.id.descriptionCard);
        iconMul.setText("x");
        titleMul.setText("Multiplication Suite");
        descriptionMul.setText("a x b");

        iconDiv = cardDiv.findViewById(R.id.iconCard);
        titleDiv = cardDiv.findViewById(R.id.titleCard);
        descriptionDiv = cardDiv.findViewById(R.id.descriptionCard);
        iconDiv.setText("÷");
        titleDiv.setText("Division Suite");
        descriptionDiv.setText("a ÷ b");

        iconAll = cardAll.findViewById(R.id.iconCard);
        titleAll = cardAll.findViewById(R.id.titleCard);
        descriptionAll = cardAll.findViewById(R.id.descriptionCard);
        iconAll.setText("+ - x ÷");
        iconAll.setTextSize(10);
        titleAll.setText("All Suite");
        descriptionAll.setText("+ - x ÷");

        iconQCM = cardQCM.findViewById(R.id.iconCard);
        titleQCM = cardQCM.findViewById(R.id.titleCard);
        descriptionQCM = cardQCM.findViewById(R.id.descriptionCard);
        iconQCM.setText("\uD83D\uDC64");
        titleQCM.setText("Solo");
        descriptionQCM.setText("\uD83D\uDD22 \uD83D\uDC64");

        iconDual = cardDual.findViewById(R.id.iconCard);
        titleDual = cardDual.findViewById(R.id.titleCard);
        descriptionDual = cardDual.findViewById(R.id.descriptionCard);
        iconDual.setText("\uD83D\uDC65");
        titleDual.setText("Battle");
        descriptionDual.setText("\uD83D\uDD22 \uD83D\uDC65");


        cardAdd.setOnClickListener(v -> openGame(v, "ADD"));
        cardSub.setOnClickListener(v -> openGame(v, "SUB"));
        cardMul.setOnClickListener(v -> openGame(v, "MUL"));
        cardDiv.setOnClickListener(v -> openGame(v, "DIV"));
        cardAll.setOnClickListener(v -> openGame(v, "ALL"));
        cardQCM.setOnClickListener(v -> openGame(v, "QCM"));
        cardDual.setOnClickListener(v -> openGame(v, "DUAL"));

        return root;
    }

    private void openGame(View v, String mode){
        Bundle args = new Bundle();
        args.putString("MODE", mode);

        switch (mode) {
            case "QCM":
                if(playerManager.isHapticEnabled()) v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_qcmFragment, args);

                break;
            case "DUAL":
                if(playerManager.isHapticEnabled()) v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_dualFragment, args);

                break;
            default:
                if(playerManager.isHapticEnabled()) v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Navigation.findNavController(v)
                        .navigate(R.id.action_navigation_home_to_gameFragment, args);

        }
    }
}
