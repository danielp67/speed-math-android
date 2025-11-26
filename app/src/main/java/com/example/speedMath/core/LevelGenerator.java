package com.example.speedMath.core;

import com.example.speedMath.ui.dashboard.LevelItem;
import java.util.ArrayList;
import java.util.List;

public class LevelGenerator {

    public static List<LevelItem> generateLevels(int playerLevel) {

        List<LevelItem> levels = new ArrayList<>();

        // Exemple : 2 difficultés, 5 niveaux chacune
        String[] modes = {"ADD", "SUB", "MUL", "DIV", "ALL"};
        int levelNumber = 1;

        for (String mode : modes) {
            levels.add(new LevelItem(levelNumber++, mode, 5, 1, getLevelStatus(playerLevel, levelNumber) ));
        }
        for (String mode : modes) {
            levels.add(new LevelItem(levelNumber++, mode, 10, 1, getLevelStatus(playerLevel, levelNumber) ));
        }
        for (String mode : modes) {
            levels.add(new LevelItem(levelNumber++, mode, 5, 2, getLevelStatus(playerLevel, levelNumber) ));
        }

        for (String mode : modes) {
            levels.add(new LevelItem(levelNumber++, mode, 10, 2, getLevelStatus(playerLevel, levelNumber) ));
        }

        for (String mode : modes) {
            levels.add(new LevelItem(levelNumber++, mode, 5, 3, getLevelStatus(playerLevel, levelNumber) ));
        }

        for (String mode : modes) {
            levels.add(new LevelItem(levelNumber++, mode, 10, 3, getLevelStatus(playerLevel, levelNumber) ));
        }

        return levels;
    }


    public static LevelItem.Status getLevelStatus(int playerLevel, int levelNumber) {
        if (playerLevel +2 == levelNumber) {
            return LevelItem.Status.UNLOCKED;
        } else if (playerLevel  >= levelNumber -1) {
            return LevelItem.Status.COMPLETED;
        } else {
            return LevelItem.Status.LOCKED;
        }
    }
}
