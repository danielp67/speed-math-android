package com.example.speedMath.core;

import com.example.speedMath.ui.dashboard.LevelItem;
import java.util.ArrayList;
import java.util.List;

public class LevelGenerator {

    public static List<LevelItem> generateLevels() {

        List<LevelItem> levels = new ArrayList<>();

        // Exemple : 2 difficultés, 5 niveaux chacune
        String[] modes = {"ADD", "SUB", "MUL", "DIV", "ALL"};
        int levelNumber = 1;

        for (String mode : modes) {
            levels.add(new LevelItem(levelNumber++, mode, 5, 1, LevelItem.Status.COMPLETED));
        }
        for (String mode : modes) {
            levels.add(new LevelItem(levelNumber++, mode, 10, 1, LevelItem.Status.UNLOCKED));
        }
        for (String mode : modes) {
            levels.add(new LevelItem(levelNumber++, mode, 5, 2, LevelItem.Status.UNLOCKED));
        }

        for (String mode : modes) {
            levels.add(new LevelItem(levelNumber++, mode, 10, 2, LevelItem.Status.LOCKED));
        }

        for (String mode : modes) {
            levels.add(new LevelItem(levelNumber++, mode, 5, 3, LevelItem.Status.LOCKED));
        }

        for (String mode : modes) {
            levels.add(new LevelItem(levelNumber++, mode, 10, 3, LevelItem.Status.LOCKED));
        }

        return levels;
    }
}
