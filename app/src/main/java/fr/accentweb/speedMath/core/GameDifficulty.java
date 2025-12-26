package fr.accentweb.speedMath.core;

public enum GameDifficulty {

    PROGRESSIVE("Progressive", 0),
    EASY("Easy", 1),
    MEDIUM("Medium", 2),
    HARD("Hard", 3),
    EXTREME("Extreme", 4);

    private final String displayName;
    private final int value;

    GameDifficulty(String displayName, int value) {
        this.displayName = displayName;
        this.value = value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getValue() {
        return value;
    }

    public static GameDifficulty fromValue(int value) {
        for (GameDifficulty difficulty : values()) {
            if (difficulty.value == value) {
                return difficulty;
            }
        }
        return MEDIUM; // Valeur par défaut
    }

    public static GameDifficulty[] getAllDifficultiesInOrder() {
        return new GameDifficulty[] {
                PROGRESSIVE,
                EASY,
                MEDIUM,
                HARD,
                EXTREME
        };
    }
}

