package fr.accentweb.speedMath.ui.arcade;

import fr.accentweb.speedMath.core.GameDifficulty;
import fr.accentweb.speedMath.core.MemoryDifficulty;

public class ArcadeItem {

    public String icon;
    public int iconSize;
    public String title;
    public String description;
    public String mode;

    public GameDifficulty gameDifficulty;
    public MemoryDifficulty memoryDifficulty;

    public ArcadeItem(String icon, int iconSize, String title, String description, String mode) {
        this.icon = icon;
        this.iconSize = iconSize;
        this.title = title;
        this.description = description;
        this.mode = mode;

        this.gameDifficulty = GameDifficulty.PROGRESSIVE;
        this.memoryDifficulty = MemoryDifficulty.MEDIUM;
    }

    public boolean hasSettings() {
        return !"ONLINE".equals(mode);
    }
}
