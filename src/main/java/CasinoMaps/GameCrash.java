package CasinoMaps;

public class GameCrash {
    public boolean gameEnd;
    public int betAmount;
    public double multiplierStart;
    public double multiplierEnd;


    public GameCrash(boolean gameEnd ,int betAmount, double multiplierStart, double multiplierEnd) {
        this.gameEnd = gameEnd;
        this.betAmount = betAmount;
        this.multiplierStart = multiplierStart;
        this.multiplierEnd = multiplierEnd;
    }
}
