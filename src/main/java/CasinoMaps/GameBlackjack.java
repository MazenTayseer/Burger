package CasinoMaps;

import java.util.ArrayList;
import java.util.List;

public class GameBlackjack {
    public List<String> playerList = new ArrayList<>();
    public List<String> dealerList = new ArrayList<>();
    public int playerAces = 0;
    public int dealerAces = 0;
    public int card1;
    public int card2;

    public String dealerCard1;
    public String dealerCard2;

    public int dealersTotal;
    public int playersTotal;

    public int card3;
    public int card4;

    public String playerCard1;
    public String playerCard2;

    public int betAmount;

    public GameBlackjack(int betAmount, int card1, int card2, String dealerCard1, String dealerCard2, int dealersTotal, int playersTotal , int card3, int card4, String playerCard1, String playerCard2) {
        this.betAmount = betAmount;
        this.card1 = card1;
        this.card2 = card2;
        this.dealerCard1 = dealerCard1;
        this.dealerCard2 = dealerCard2;
        this.dealersTotal = dealersTotal;
        this.playersTotal = playersTotal;
        this.card3 = card3;
        this.card4 = card4;
        this.playerCard1 = playerCard1;
        this.playerCard2 = playerCard2;
    }

}
