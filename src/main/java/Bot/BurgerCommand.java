package Bot;

import Commands.CommandContext;
import Commands.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;

public class BurgerCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();
    @Override
    public void handle(CommandContext ctx) {
        embed.setTitle("🍔");
        embed.setDescription("**One burger for** " + ctx.getAuthor().getAsMention());

        ctx.getChannel().sendMessage(embed.build()).queue();
        embed.clear();
    }

    @Override
    public String getName() {
        return "burger";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Burger");
        embed.setDescription("I make you a burger ❤.");
        embed.addField("Usage","**`-burger`**",true);
        embed.setColor(0xffffff);
        return embed;
    }
}
