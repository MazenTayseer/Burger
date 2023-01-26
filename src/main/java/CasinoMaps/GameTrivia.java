package CasinoMaps;

import java.util.ArrayList;
import java.util.List;

public class GameTrivia {

    public int betAmount;
    public String question;
    public List<String> answers = new ArrayList<>();
    public String correctAnswer;

    public GameTrivia(int betAmount, String question, List<String> answers, String correctAnswer) {
        this.betAmount = betAmount;
        this.question = question;
        this.answers = answers;
        this.correctAnswer = correctAnswer;
    }
}
