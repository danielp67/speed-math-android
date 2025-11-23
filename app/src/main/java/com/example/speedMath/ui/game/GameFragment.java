package com.example.speedMath.ui.game;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.speedMath.R;
import com.example.speedMath.core.QuestionGenerator;

public class GameFragment extends Fragment {

    private TextView textQuestion, textResult;
    private CardView[] cards = new CardView[10];
    private CardView cardCancel, cardClear, cardValidate;
    private TextView[] texts = new TextView[10];
    private TextView textCancel, textClear, textValidate;
    private String gameMode;
    private int correctAnswer;

    private QuestionGenerator questionGenerator;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_game, container, false);

        gameMode = getArguments() != null ? getArguments().getString("MODE") : "ALL";

        textQuestion = root.findViewById(R.id.textQuestion);
        textResult = root.findViewById(R.id.textResult);
        textResult.setText("");

        // Boutons numériques 0-9
        for (int i = 0; i <= 9; i++) {
            int resID = getResources().getIdentifier("card" + i, "id", getActivity().getPackageName());
            cards[i] = root.findViewById(resID);
            texts[i] = cards[i].findViewById(R.id.textButton);
            texts[i].setText(i + "");
            int finalI = i;
            cards[i].setOnClickListener(v -> textResult.append(String.valueOf(finalI)));
        }

        // Boutons clavier
        cardCancel = root.findViewById(R.id.cardX);
        cardClear = root.findViewById(R.id.cardC);
        cardValidate = root.findViewById(R.id.cardOK);

        textCancel = cardCancel.findViewById(R.id.textButton);
        textClear = cardClear.findViewById(R.id.textButton);
        textValidate = cardValidate.findViewById(R.id.textButton);

        textCancel.setText("X");
        textClear.setText("C");
        textValidate.setText("OK");

        cardClear.setOnClickListener(v -> textResult.setText(""));
        cardCancel.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        cardValidate.setOnClickListener(v -> checkAnswer());

        // ----- Initialisation du QuestionGenerator -----
        boolean allowAdd = gameMode.equals("ADD") || gameMode.equals("ALL");
        boolean allowSub = gameMode.equals("SUB") || gameMode.equals("ALL");
        boolean allowMul = gameMode.equals("MUL") || gameMode.equals("ALL");
        boolean allowDiv = gameMode.equals("DIV") || gameMode.equals("ALL");

        questionGenerator = new QuestionGenerator(
                1,      // difficulty par défaut
                2,      // nombre d'opérandes
                false,  // pas de QCM ici
                allowAdd,
                allowSub,
                allowMul,
                allowDiv,
                true
        );

        generateQuestion();

        return root;
    }

    private void generateQuestion() {

        // Génération via QuestionGenerator
        QuestionGenerator.MathQuestion q = questionGenerator.generateQuestion();

        textQuestion.setText(q.expression);
        correctAnswer = q.answer;
    }

    private void checkAnswer() {
        String userInput = textResult.getText().toString().trim();
        if (userInput.isEmpty()) return;

        int userAnswer = Integer.parseInt(userInput);

        if (userAnswer == correctAnswer) {
            flashBorder(textResult, true);
        } else {
            flashBorder(textResult, false);
            //score = 0;
        }
        generateQuestion();
    }

    private void flashBorder(TextView view, boolean isCorrect) {

        // Stocke le fond actuel pour restauration
        Drawable originalBackground = view.getBackground();

        // Couleur de feedback
        int borderColor = isCorrect
                ? Color.parseColor("#4CAF50") // Vert
                : Color.parseColor("#F44336"); // Rouge

        // Crée un drawable dynamique
        GradientDrawable gd = new GradientDrawable();
        gd.setStroke(8, borderColor);

        // Applique temporairement
        view.setBackground(gd);

        // Après 0,5s → retour à la normale
        new Handler().postDelayed(() -> {
            view.setBackground(originalBackground);
            view.setText("");
        }, 500);
    }
}
