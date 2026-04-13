package Bot;

import Commands.CommandContext;
import Commands.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class CoinFlipCommand implements ICommand {
    private EmbedBuilder embed = new EmbedBuilder();
    @Override
    public void handle(CommandContext ctx) {
        Random random = new Random();
        boolean coin = random.nextBoolean();
        String result;
        if(coin){
            result = "Heads!";
        }else{
            result = "Tails!";
        }
        ctx.getChannel().sendMessage("Flipping a coin 🪙").queue( e -> e.editMessage("The coin landed on `" + result + "`").queueAfter(1, TimeUnit.SECONDS));

    }

    @Override
    public String getName() {
        return "coinflip";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Coin FLip");
        embed.setDescription("Flips a coin.");
        embed.addField("Usage","**`-coinflip`**",true);
        embed.setColor(0xffffff);
        return embed;
    }
}
