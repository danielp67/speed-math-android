package fr.accentweb.speedMath.ui.daily;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.navigation.Navigation;

import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.BaseGameFragment;
import fr.accentweb.speedMath.core.FeedbackManager;
import fr.accentweb.speedMath.core.PlayerManager;
import fr.accentweb.speedMath.core.QuestionGenerator;
import fr.accentweb.speedMath.utils.AnimUtils;

public class DailyGameFragment extends BaseGameFragment {

    private TextView txtChallengeTitle, txtChallengeDesc, textQuestion, textResult;
    private ProgressBar progressTimer;
    private CardView[] cards = new CardView[10];
    private CardView cardCancel, cardClear, cardValidate;
    private TextView[] texts = new TextView[10];
    
    private PlayerManager playerManager;
    private QuestionGenerator questionGenerator;
    private FeedbackManager feedbackManager;
    private CountDownTimer flashTimer;

    private int score = 0;
    private int targetScore = 10;
    private int correctAnswer;
    private ChallengeMode currentMode;
    private boolean isGameOver = false;

    enum ChallengeMode {
        FLASH,          // 1.5s per question
        SUDDEN_DEATH,   // 1 error = game over
        MYSTERY_OP,     // Guess the operator
        COMPLEX_MUL,    // Hard multiplications
        NORMAL          // Default
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_daily_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        playerManager = PlayerManager.getInstance(requireContext());
        feedbackManager = new FeedbackManager(requireContext());
        feedbackManager.loadSounds(R.raw.correct, R.raw.wrong, R.raw.levelup);

        initUI(view);
        determineChallengeMode();
        setupGame();
        generateQuestion();
    }

    private void initUI(View view) {
        txtChallengeTitle = view.findViewById(R.id.txtChallengeTitle);
        txtChallengeDesc = view.findViewById(R.id.txtChallengeDesc);
        textQuestion = view.findViewById(R.id.textQuestion);
        textResult = view.findViewById(R.id.textResult);
        progressTimer = view.findViewById(R.id.progressTimer);

        // Clavier Numérique
        for (int i = 0; i <= 9; i++) {
            int resID = getResources().getIdentifier("card" + i, "id", requireActivity().getPackageName());
            cards[i] = view.findViewById(resID);
            texts[i] = cards[i].findViewById(R.id.textButton);
            texts[i].setText(String.valueOf(i));
            int finalI = i;
            cards[i].setOnClickListener(v -> {
                if (isGameOver) return;
                textResult.append(String.valueOf(finalI));
            });
        }

        cardClear = view.findViewById(R.id.cardC);
        cardCancel = view.findViewById(R.id.cardX);
        cardValidate = view.findViewById(R.id.cardOK);

        ((TextView)cardClear.findViewById(R.id.textButton)).setText("C");
        ((TextView)cardCancel.findViewById(R.id.textButton)).setText("X");
        ((TextView)cardValidate.findViewById(R.id.textButton)).setText("OK");

        cardClear.setOnClickListener(v -> textResult.setText(""));
        cardCancel.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        cardValidate.setOnClickListener(v -> checkAnswer());
    }

    private void determineChallengeMode() {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        switch (day) {
            case Calendar.MONDAY:
                currentMode = ChallengeMode.FLASH;
                txtChallengeTitle.setText("MONDAY FLASH");
                txtChallengeDesc.setText("1.5 seconds per question!");
                break;
            case Calendar.TUESDAY:
                currentMode = ChallengeMode.SUDDEN_DEATH;
                txtChallengeTitle.setText("SUDDEN DEATH");
                txtChallengeDesc.setText("One mistake and it's over!");
                break;
            case Calendar.WEDNESDAY:
                currentMode = ChallengeMode.MYSTERY_OP;
                txtChallengeTitle.setText("MYSTERY OPERATOR");
                txtChallengeDesc.setText("Find the hidden sign!");
                break;
            case Calendar.THURSDAY:
                currentMode = ChallengeMode.COMPLEX_MUL;
                txtChallengeTitle.setText("COMPLEX MUL");
                txtChallengeDesc.setText("Two digits multiplication!");
                break;
            default:
                currentMode = ChallengeMode.NORMAL;
                txtChallengeTitle.setText("DAILY CHALLENGE");
                txtChallengeDesc.setText("Complete 10 questions!");
                break;
        }
    }

    private void setupGame() {
        boolean allowPlus = true, allowMinus = true, allowMul = true, allowDiv = true;
        int level = playerManager.getCurrentLevel();

        if (currentMode == ChallengeMode.COMPLEX_MUL) {
            allowPlus = allowMinus = allowDiv = false;
            level = 80; // Force high level for difficult multiplications
        }

        questionGenerator = new QuestionGenerator(
                level,
                2,
                false,
                allowPlus, allowMinus, allowMul, allowDiv,
                true
        );
    }

    private void generateQuestion() {
        if (isGameOver) return;
        textResult.setText("");
        AnimUtils.slideLeftRight(textQuestion);

        QuestionGenerator.MathQuestion q = questionGenerator.generateQuestion();
        correctAnswer = q.answer;

        if (currentMode == ChallengeMode.MYSTERY_OP) {
            // Hide the operator
            String expression = q.expression.replace("+", "?").replace("-", "?").replace("x", "?").replace("÷", "?");
            textQuestion.setText(expression + " (" + q.answer + ")");
            // In mystery mode, the user inputs the hidden answer? No, user inputs the answer, but wait...
            // Concept change: if Mystery Op, show "A ? B = Result", user must find... the result? 
            // Better: Show "A ? B = Result", user must find the result? No, that's normal.
            // Let's do: "A ? B = Result", what is '?'? But our Numpad is for numbers.
            // Revert: Mystery Op means the sign is hidden, but you still need to find the result based on the possible options?
            // Actually, let's keep it simple: Hide operator, user must figure out the result by testing.
            textQuestion.setText(expression);
        } else {
            textQuestion.setText(q.expression);
        }

        if (currentMode == ChallengeMode.FLASH) {
            startFlashTimer();
        }
    }

    private void startFlashTimer() {
        if (flashTimer != null) flashTimer.cancel();
        progressTimer.setVisibility(View.VISIBLE);
        progressTimer.setProgress(1000);

        flashTimer = new CountDownTimer(1500, 15) {
            @Override
            public void onTick(long millisUntilFinished) {
                progressTimer.setProgress((int) (millisUntilFinished * 1000 / 1500));
            }

            @Override
            public void onFinish() {
                onTimeUp();
            }
        }.start();
    }

    private void onTimeUp() {
        if (isGameOver) return;
        feedbackManager.playWrongSound();
        failChallenge("Time is up!");
    }

    private void checkAnswer() {
        if (isGameOver) return;
        String input = textResult.getText().toString().trim();
        if (input.isEmpty()) return;

        if (flashTimer != null) flashTimer.cancel();

        int userAnswer = Integer.parseInt(input);
        boolean isCorrect = userAnswer == correctAnswer;

        flashBorder(textResult, isCorrect);

        if (isCorrect) {
            score++;
            feedbackManager.playCorrectSound();
            if (score >= targetScore) {
                winChallenge();
            } else {
                new Handler().postDelayed(this::generateQuestion, 500);
            }
        } else {
            feedbackManager.playWrongSound();
            if (currentMode == ChallengeMode.SUDDEN_DEATH || currentMode == ChallengeMode.FLASH) {
                failChallenge("Wrong answer! Challenge failed.");
            } else {
                new Handler().postDelayed(this::generateQuestion, 500);
            }
        }
    }

    private void winChallenge() {
        isGameOver = true;
        // Mark as done for today and show reward in previous fragment
        // We'll use a result bundle or just shared prefs
        // But for now, let's navigate back with success
        Bundle result = new Bundle();
        result.putBoolean("SUCCESS", true);
        getParentFragmentManager().setFragmentResult("daily_result", result);
        
        Toast.makeText(getContext(), "🎉 Challenge Completed!", Toast.LENGTH_LONG).show();
        Navigation.findNavController(requireView()).navigateUp();
    }

    private void failChallenge(String message) {
        isGameOver = true;
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        Navigation.findNavController(requireView()).navigateUp();
    }

    private void flashBorder(TextView view, boolean isCorrect) {
        Drawable originalBackground = view.getBackground();
        int borderColor = isCorrect ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336");
        GradientDrawable gd = new GradientDrawable();
        gd.setStroke(8, borderColor);
        view.setBackground(gd);
        new Handler().postDelayed(() -> view.setBackground(originalBackground), 500);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (flashTimer != null) flashTimer.cancel();
    }
}
