package Bot;

import Commands.CommandContext;
import Commands.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class StatsCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        embed.setAuthor("Burger","https://www.instagram.com/mazen_tayseer/",ctx.getSelfUser().getAvatarUrl());
        embed.addField("Guilds", String.valueOf(channel.getJDA().getGuildCache().size()),true);
        embed.addField("Users", String.valueOf(channel.getJDA().getUserCache().size()),true);
        embed.addField("Voice channels", String.valueOf(channel.getJDA().getVoiceChannelCache().size()),true);
        embed.setColor(0xffffff);

        channel.sendMessage(embed.build()).queue();
        embed.clear();

    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Stats");
        embed.setDescription("Shows the current stats of the bot");
        embed.addField("Usage","**`-stats`**",true);
        embed.setColor(0xffffff);
        return embed;
    }
}
