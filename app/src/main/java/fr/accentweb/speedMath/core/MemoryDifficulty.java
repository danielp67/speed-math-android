package fr.accentweb.speedMath.core;

public enum MemoryDifficulty {
    EASY("Easy", 4, 3, 2000),
    MEDIUM("Medium", 4, 4, 1500),
    HARD("Hard", 4, 6, 1000);

    public final String label;
    public final int cols;
    public final int rows;
    public final int previewMs;

    MemoryDifficulty(String label, int cols, int rows, int previewMs) {
        this.label = label;
        this.cols = cols;
        this.rows = rows;
        this.previewMs = previewMs;
    }
}

