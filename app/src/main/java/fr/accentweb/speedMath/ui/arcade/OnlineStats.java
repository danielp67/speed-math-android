package fr.accentweb.speedMath.ui.arcade;

public class OnlineStats {
    public int playersOnline;
    public int gamesPlayedToday;
    public int dailyLimit;

    // Constructeur
    public OnlineStats(int playersOnline, int gamesPlayedToday, int dailyLimit) {
        this.playersOnline = playersOnline;
        this.gamesPlayedToday = gamesPlayedToday;
        this.dailyLimit = dailyLimit;
    }

    // Exemple de stat par défaut
    public static OnlineStats getDefault() {
        return new OnlineStats(128, 3, 10);
    }
}
