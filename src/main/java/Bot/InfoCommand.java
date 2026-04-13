package Bot;

import Commands.CommandContext;
import Commands.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class InfoCommand implements ICommand {
    EmbedBuilder embed = new EmbedBuilder();
    @Override
    public void handle(CommandContext ctx) {
        embed.setTitle("🍔 Information");
        embed.setDescription("Discord: " + "Mazen#0654\n" +
                             "Instagram: " + "[Mazen_Tayseer](https://www.instagram.com/mazen_tayseer/)");
        ctx.getChannel().sendMessage(embed.build()).queue();

        embed.clear();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Creator Info");
        embed.setDescription("shows information about the bot creator.");
        embed.addField("Usage","**`-info`**",true);
        embed.setColor(0xffffff);
        return embed;
    }

}
