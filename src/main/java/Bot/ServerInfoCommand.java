package Bot;

import Commands.CommandContext;
import Commands.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ServerInfoCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();
    @Override
    public void handle(CommandContext ctx) {
        Guild guild = ctx.getGuild();
        embed.setTitle(guild.getName());
        embed.setDescription("**Owner: **" + guild.getOwner().getEffectiveName());
        embed.addField("Time created",guild.getTimeCreated().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),true);
        embed.addBlankField(true);
        embed.addField("Members count", String.valueOf(guild.getMemberCount()),false);

        ctx.getChannel().sendMessage(embed.build()).queue();
        embed.clear();
    }

    @Override
    public String getName() {
        return "serverinfo";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Server Info");
        embed.setDescription("Shows information about the server.");
        embed.addField("Usage","**`-serverinfo`**",true);
        embed.setColor(0xffffff);
        return embed;
    }
}
