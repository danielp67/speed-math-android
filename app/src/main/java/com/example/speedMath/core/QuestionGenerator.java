package com.example.speedMath.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestionGenerator {

    private final Random random = new Random();

    // Configuration
    private int difficulty;         // ex: 1,2,3
    private int operandCount;       // ex: 2,3,4...

    private int level;
    private boolean qcmMode;     // QCM ou non
    private boolean allowPlus;
    private boolean allowMinus;
    private boolean allowMultiply;
    private boolean allowDivide;
    private boolean avoidNegative;  // éviter résultats négatifs

    public QuestionGenerator(int difficulty,
                             int operandCount,
                             boolean qcmMode,
                             boolean allowPlus,
                             boolean allowMinus,
                             boolean allowMultiply,
                             boolean allowDivide,
                             boolean avoidNegative) {

        this.difficulty = Math.max(1, difficulty);
        this.operandCount = Math.max(2, operandCount);
        this.qcmMode = qcmMode;
        this.allowPlus = allowPlus;
        this.allowMinus = allowMinus;
        this.allowMultiply = allowMultiply;
        this.allowDivide = allowDivide;
        this.avoidNegative = avoidNegative;
    }

    // Classe interne pour la question
    public static class MathQuestion {
        public String expression;
        public int answer;
        public List<Integer> answersChoice = new ArrayList<>();
    }

    public MathQuestion generateQuestion() {
        MathQuestion q = new MathQuestion();

        // Liste des opérateurs autorisés
        List<Character> ops = new ArrayList<>();
        if (allowPlus) ops.add('+');
        if (allowMinus) ops.add('-');
        if (allowMultiply) ops.add('x');
        if (allowDivide) ops.add('÷');

        if (ops.isEmpty()) ops.add('+');

        int difficultyCorrector = 100;
        if (allowMultiply || allowDivide) difficultyCorrector = 20;
        operandCount = Math.max(2, (level/40) + 2);
        difficulty = Math.max(1, (level%40 * operandCount)/2);
        int min = 1;
        int max = 10 * difficulty * difficultyCorrector / 100;

        List<Integer> values = new ArrayList<>();
        List<Character> operations = new ArrayList<>();

        // Génération des valeurs et opérateurs
        for (int i = 0; i < operandCount; i++) {
            values.add(random.nextInt(max - min + 1) + min);
            if (i > 0) {
                operations.add(ops.get(random.nextInt(ops.size())));
            }
        }

        // Construction de l'expression avec priorité et parenthèses
        StringBuilder expr = new StringBuilder();
        int currentResult = values.get(0);
        expr.append(values.get(0));

        for (int i = 1; i < values.size(); i++) {
            char op = operations.get(i - 1);
            int nextVal = values.get(i);

            // Gestion spéciale pour la division
            if (op == '÷') {
                // Générer un dividende "propre"
                int dividend = currentResult * nextVal;
                expr = new StringBuilder("(" + dividend + " ÷ " + nextVal + ")");
                currentResult = dividend / nextVal;
            } else if (op == 'x') {
                if (operandCount > 2) expr = new StringBuilder("(" + expr + " x " + nextVal + ")");
                else expr.append(" x ").append(nextVal);
                currentResult *= nextVal;
            } else if (op == '-') {
                if (avoidNegative) {
                    // nextVal doit être ≤ currentResult
                    if (nextVal > currentResult) {
                        nextVal = random.nextInt(currentResult + 1); // 0 à currentResult
                    }
                }
                if (operandCount > 2) expr = new StringBuilder("(" + expr + " - " + nextVal + ")");
                else expr.append(" - ").append(nextVal);
                currentResult -= nextVal;
            } else { // '+'
                if (operandCount > 2) expr = new StringBuilder("(" + expr + " + " + nextVal + ")");
                else expr.append(" + ").append(nextVal);
                currentResult += nextVal;
            }
        }

        q.expression = expr.append(" = ?").toString();
        q.answer = currentResult;

        // Génération des mauvaises réponses si QCM
        if (qcmMode) generateAnswersChoice(q);

        return q;
    }

    private void generateAnswersChoice(MathQuestion q) {
        while (q.answersChoice.size() < 3) {
            int wrong = q.answer + random.nextInt(11) - 5;
            if (wrong != q.answer && !q.answersChoice.contains(wrong)) {
                q.answersChoice.add(wrong);
            }
        }
        int correctIndex = random.nextInt(4); // 0,1,2,3
        q.answersChoice.add(correctIndex, q.answer);
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }
}
