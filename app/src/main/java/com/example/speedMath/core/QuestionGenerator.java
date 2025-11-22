package com.example.speedMath.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestionGenerator {

    private final Random random = new Random();

    // Configuration
    private int difficulty;         // ex: 1, 2, 3
    private int operandCount;       // ex: 2, 3, 4...

    private boolean choiceMode;
    private boolean allowPlus;
    private boolean allowMinus;
    private boolean allowMultiply;
    private boolean allowDivide;

    public QuestionGenerator(int difficulty,
                             int operandCount,
                             boolean choiceMode,
                             boolean allowPlus,
                             boolean allowMinus,
                             boolean allowMultiply,
                             boolean allowDivide) {

        this.difficulty = difficulty;
        this.operandCount = Math.max(2, operandCount); // minimum 2 operandes
        this.choiceMode = choiceMode;
        this.allowPlus = allowPlus;
        this.allowMinus = allowMinus;
        this.allowMultiply = allowMultiply;
        this.allowDivide = allowDivide;
    }


    // question generated
    public static class MathQuestion {
        public String expression;
        public int answer;
        public List<Integer> wrongOptions = new ArrayList<>();
    }


    // generator principal
    public MathQuestion generateQuestion() {

        MathQuestion q = new MathQuestion();

        // Liste des opérateurs autorisés
        List<Character> ops = new ArrayList<>();
        if (allowPlus) ops.add('+');
        if (allowMinus) ops.add('-');
        if (allowMultiply) ops.add('*');
        if (allowDivide) ops.add('/');

        // fallback si aucun opérateur autorisé
        if (ops.isEmpty()) ops.add('+');
        difficulty = Math.max(1, difficulty);

        int min = 0;
        int max = 10 * difficulty;

        List<Integer> values = new ArrayList<>();
        List<Character> operations = new ArrayList<>();

        // Génération des valeurs et opérateurs
        for (int i = 0; i < operandCount; i++) {
            values.add(random.nextInt(max - min + 1) + min);
            if (i > 0)
                operations.add(ops.get(random.nextInt(ops.size())));
        }

        // Calcul du résultat et création de l'expression
        int result = values.get(0);
        StringBuilder expr = new StringBuilder("" + values.get(0));

        boolean lastWasMulOrDiv = false;

        for (int i = 1; i < values.size(); i++) {

            char op = operations.get(i - 1);
            int v = values.get(i);

            // Correction division par zéro
            if (op == '/') {
                if (v == 0) v = 1;
                result = result / v;
            } else if (op == '*') {
                result = result * v;
            } else if (op == '-') {
                if (v > result) {
                    v = random.nextInt(result + 1);
                    values.set(i, v);
                }
                result = result - v;
            } else {
                result = result + v;
            }

            boolean isMulOrDiv = (op == '*' || op == '/');

            // ----- Construction de l'expression avec parenthèses -----
            if (operandCount > 2) {

                if (isMulOrDiv) {

                    // début du groupement si nécessaire
                    if (!lastWasMulOrDiv) {
                        expr.insert(0, "(");
                    }

                    expr.append(" ").append(op).append(" ").append(v);
                    lastWasMulOrDiv = true;

                    // Si prochain opérateur n’est pas * ou / --> fermer parenthèse
                    if (i == values.size() - 1 ||
                            !(operations.get(i) == '*' || operations.get(i) == '/')) {
                        expr.append(")");
                        lastWasMulOrDiv = false;
                    }

                } else {
                    expr.append(" ").append(op).append(" ").append(v);
                    lastWasMulOrDiv = false;
                }

            } else {
                // Cas simple à deux opérandes → pas de parenthèses
                expr.append(" ").append(op).append(" ").append(v);
            }
        }

        q.expression = expr.toString();
        q.answer = result;

        // Génération des mauvaises réponses si on est en QCM
        if (choiceMode) {
            generateWrongAnswers(q);
        }

        return q;
    }



    private void generateWrongAnswers(MathQuestion q) {

        while (q.wrongOptions.size() < 3) {
            int wrong = q.answer + random.nextInt(11) - 5;
            if (wrong != q.answer && !q.wrongOptions.contains(wrong)) {
                q.wrongOptions.add(wrong);
            }
        }
    }
}
