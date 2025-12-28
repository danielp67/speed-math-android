package fr.accentweb.speedMath.ui.arcade;

public class OnlineStats {
    public int playersOnline;
    public int gamesPlayedToday;
    public int dailyLimit;

    public OnlineStats(int playersOnline, int gamesPlayedToday, int dailyLimit) {
        this.playersOnline = playersOnline;
        this.gamesPlayedToday = gamesPlayedToday;
        this.dailyLimit = dailyLimit;
    }
}
