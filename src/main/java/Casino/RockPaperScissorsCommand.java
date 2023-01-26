package Casino;

import Commands.CommandContext;
import Commands.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;

public class RockPaperScissorsCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {


    }

    @Override
    public String getName() {
        return "rps";
    }

    @Override
    public EmbedBuilder getHelp() {
        return null;
    }

}
