package Bot;

import Commands.CommandContext;
import Commands.CommandManager;
import Commands.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("DuplicatedCode")
public class HelpCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();
    private final EmbedBuilder embedBuilder = new EmbedBuilder();

    private final CommandManager manager;

    public HelpCommand(CommandManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(CommandContext ctx) {
        List<String> args = ctx.getArgs();
        TextChannel channel = ctx.getChannel();

        if (args.isEmpty()) {
            embed.setTitle("Commands List");

            embed.addField("🎵 Music",
                    "`Play`, `Stop`, `Pause`, `Resume`, `Queue`," +
                            " `Loop`,`SkipTo`, `Skip`, `Seek`, `Clear`," +
                            " `NowPlaying`, `Leave`, `Remove`, `Volume`, `Shuffle`, `Lyrics`",false);
            embed.setColor(0xffffff);

            embedBuilder.setTitle("Commands List");
            embedBuilder.setColor(0xffffff);

            embed.setFooter("Created by Mazen#0654");


            Button Music = Button.secondary("Music","🎵 Music");
            Button LoFi = Button.secondary("LoFi","🪶 Lo-Fi");
            Button Other = Button.secondary("other","🍔 Other");
            Button casino = Button.secondary("casino","🎰 Casino");
            Button voteMe = Button.link("https://top.gg/bot/991196140626247702", "🆙️Vote me");
            Button sourceCode = Button.link("https://www.youtube.com/watch?v=dQw4w9WgXcQ","Source code");
            Button support = Button.link("https://www.buymeacoffee.com/MazenTayseer","Support Me!");


            channel.sendMessageEmbeds(embed.build()).setActionRows(ActionRow.of(Music,LoFi,casino ,Other), ActionRow.of(voteMe,support ,sourceCode))
                    .queue();
            embed.clear();
            return;
        }

        String search = args.get(0);
        ICommand command = manager.getCommand(search);

        if (command == null) {
            channel.sendMessage("Nothing found for " + search).queue();
            return;
        }


        channel.sendMessage(command.getHelp().build()).queue();
        command.getHelp().clear();
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Help");
        embed.setDescription("Shows a list of all the available commands. \n"+
                "Use `-help` `Command name` for more information about a command.");
        embed.addField("Usage","**`-help`** `command`",true);
        embed.addField("Aliases","`commands`, `cmds`, `commandList`",true);
        embed.setColor(0xffffff);
        return embed;
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("commands", "cmds", "commandList");
    }
}
