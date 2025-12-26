package fr.accentweb.speedMath.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestionGenerator {
    private final Random random = new Random();

    // Configuration
    private int difficulty;
    private int operandCount;
    private int level;
    private boolean qcmMode;
    private boolean allowPlus;
    private boolean allowMinus;
    private boolean allowMultiply;
    private boolean allowDivide;
    private boolean avoidNegative;

    // Paliers de progression pour MUL/DIV (par niveau)
    private static final int[][] MUL_DIV_PALIERS = {
            {1, 2, 5},                     // Niveau 1-19: 1,2,5
            {1, 2, 3, 4, 5, 10},           // Niveau 20-39: +3,4,10
            {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, // Niveau 40-59: +6-9
            {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, // Niveau 60-79: +11-15
            {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20} // Niveau 80+: +16-20
    };

    // Plages de valeurs pour ADD/SUB par palier
    private static final int[] ADD_SUB_MAX_VALUES = {10,20, 30, 40, 50, 60, 70, 80, 90, 100,125,150,175, 200, 250, 400, 500,750,1000,2000,5000};

    public QuestionGenerator(int level, int operandCount, boolean qcmMode,
                             boolean allowPlus, boolean allowMinus,
                             boolean allowMultiply, boolean allowDivide,
                             boolean avoidNegative) {
        this.level = Math.max(1, level);
        this.operandCount = Math.max(2, operandCount);
        this.qcmMode = qcmMode;
        this.allowPlus = allowPlus;
        this.allowMinus = allowMinus;
        this.allowMultiply = allowMultiply;
        this.allowDivide = allowDivide;
        this.avoidNegative = avoidNegative;
    }

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

        // Calcul des paramètres de difficulté
        int difficultyCorrector = allowMultiply || allowDivide ? 20 : 100;
        this.operandCount = Math.max(2, ((this.level-1)/50) + 2);
        this.difficulty = Math.max(1, (this.level * this.operandCount)/2);

        List<Integer> values = new ArrayList<>();
        List<Character> operations = new ArrayList<>();

        // Génération des valeurs et opérateurs
        for (int i = 0; i < this.operandCount; i++) {
            values.add(getOperandValue(i == 0, ops.get(ops.size()-1)));
            if (i > 0) {
                operations.add(ops.get(random.nextInt(ops.size())));
            }
        }

        // Construction de l'expression
        StringBuilder expr = new StringBuilder();
        int currentResult = values.get(0);
        expr.append(values.get(0));

        for (int i = 1; i < values.size(); i++) {
            char op = operations.get(i - 1);
            int nextVal = values.get(i);

            if (op == '÷') {
                int dividend = currentResult * nextVal;
                expr = new StringBuilder("(" + dividend + " ÷ " + nextVal + ")");
                currentResult = dividend / nextVal;
            } else if (op == 'x') {
                if (this.operandCount > 2) expr = new StringBuilder("(" + expr + " x " + nextVal + ")");
                else expr.append(" x ").append(nextVal);
                currentResult *= nextVal;
            } else if (op == '-') {
                if (avoidNegative && nextVal > currentResult) {
                    nextVal = random.nextInt(currentResult) + 1;
                }
                if (this.operandCount > 2) expr = new StringBuilder("(" + expr + " - " + nextVal + ")");
                else expr.append(" - ").append(nextVal);
                currentResult -= nextVal;
            } else { // '+'
                if (this.operandCount > 2) expr = new StringBuilder("(" + expr + " + " + nextVal + ")");
                else expr.append(" + ").append(nextVal);
                currentResult += nextVal;
            }
        }

        q.expression = expr.append(" = ?").toString();
        q.answer = currentResult;

        if (qcmMode) generateAnswersChoice(q);
        return q;
    }

    private int getOperandValue(boolean isFirstOperand, char lastOp) {
        int palier = Math.min(4, this.level / 10);
        int maxValue;

        // Pour ADD/SUB, utiliser des valeurs plus larges
        if (lastOp == '+' || lastOp == '-') {
            maxValue = ADD_SUB_MAX_VALUES[Math.min(20, this.level / 10)];
            return random.nextInt(maxValue) + 1;
        }
        // Pour MUL/DIV, utiliser les paliers définis
        else {
            if (isFirstOperand) {
                // Premier opérande peut être plus grand
                maxValue = new int[]{5, 10, 15, 20, 30}[palier];
                return random.nextInt(maxValue) + 1;
            } else {
                // Deuxième opérande utilise les nombres des paliers
                int[] palierNumbers = MUL_DIV_PALIERS[palier];
                return palierNumbers[random.nextInt(palierNumbers.length)];
            }
        }
    }

    private void generateAnswersChoice(MathQuestion q) {
        while (q.answersChoice.size() < 3) {
            int wrong = q.answer + random.nextInt(11) - 5;
            if (wrong != q.answer && !q.answersChoice.contains(wrong)) {
                q.answersChoice.add(wrong);
            }
        }
        q.answersChoice.add(random.nextInt(4), q.answer);
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }
}
