package com.example.speedMath.core;

import com.example.speedMath.ui.league.LevelItem;
import java.util.ArrayList;
import java.util.List;

public class LevelGenerator {

    public static List<LevelItem> generateLevels(int playerLevel) {

        List<LevelItem> levels = new ArrayList<>();
        int levelNumber = 1;
        String[] advancedModes = {"ADD", "SUB", "MUL", "DIV", "ALL"};

        for (String mode : advancedModes) {
            levelNumber = generateBlock(levels,
                    mode,
                    levelNumber,
                    10,      // 5 niveaux par mode (ajuste comme tu veux)
                    playerLevel
            );
        }

        return levels;
    }

    private static int generateBlock(List<LevelItem> levels, String mode,
                                     int startNumber, int count, int playerLevel) {

        for (int i = 0; i < count; i++) {
            int levelNumber = startNumber + i;

            long targetScore = levelNumber * 100L;
            int difficulty = determineDifficulty(levelNumber);

            levels.add(new LevelItem(
                    levelNumber,
                    mode,
                    targetScore,
                    difficulty,
                    getLevelStatus(playerLevel, levelNumber)
            ));
        }

        return startNumber + count;
    }

    private static int determineDifficulty(int levelNumber) {

        int currentLevel = levelNumber%10;
        int currentOperand = levelNumber/50;

        if (levelNumber % 10 == 0) {
            currentLevel = 10;
        }
        if(levelNumber % 50 == 0) {
            return 1;
        }

        return currentLevel * currentOperand + 1;
    }

    public static LevelItem.Status getLevelStatus(int playerLevel, int levelNumber) {
        if (playerLevel == levelNumber -1) {
            return LevelItem.Status.UNLOCKED;
    } else if (playerLevel  >= levelNumber - 1) {
            return LevelItem.Status.COMPLETED;
        } else {
            return LevelItem.Status.LOCKED;
        }
    }
}
