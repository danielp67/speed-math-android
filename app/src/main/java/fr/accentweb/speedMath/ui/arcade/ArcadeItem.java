package fr.accentweb.speedMath.ui.arcade;

public class ArcadeItem {
    public String icon;
    public int iconSize;
    public String title;
    public String description;
    public String mode;

    public ArcadeItem(String icon,int iconSize, String title, String description, String mode) {
        this.icon = icon;
        this.iconSize = iconSize;
        this.title = title;
        this.description = description;
        this.mode = mode;
    }
}
