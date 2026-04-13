package Commands;

import Bot.*;
import Casino.*;
import Lofi.LofiCommand;
import MusicCommands.*;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CommandManager {
    private final List<ICommand> commands = new ArrayList<>();

    public CommandManager(EventWaiter waiter) {
        addCommand(new DeleteCommand());
        addCommand(new CoinFlipCommand());
        addCommand(new HelpCommand(this));
        addCommand(new BurgerCommand());
        addCommand(new MemeCommand());
        addCommand(new StatsCommand());

        addCommand(new PlayCommand());
        addCommand(new StopCommand());
        addCommand(new SkipCommand());
        addCommand(new QueueCommand());
        addCommand(new PauseCommand());
        addCommand(new ResumeCommand());
        addCommand(new RemoveCommand());
        addCommand(new NowPlayingCommand());
        addCommand(new LoopCommand());
        addCommand(new ClearCommand());
        addCommand(new SkipToCommand());
        addCommand(new SeekCommand());
        addCommand(new LeaveCommand());
        addCommand(new VolumeCommand());
        addCommand(new ShuffleCommand());
        addCommand(new ServerInfoCommand());
        addCommand(new LofiCommand());
        addCommand(new LyricsCommand());

        addCommand(new BlackJackCommand(waiter));
        addCommand(new HorsesCommand(waiter));
        addCommand(new RegisterCommand());
        addCommand(new BalanceCommand());
        addCommand(new TriviaCommand(waiter));
        addCommand(new HigherOrLowerCommand(waiter));
        addCommand(new LeaderBoardsCommand());
        addCommand(new WageCommand());
        addCommand(new VoteCommand());
        addCommand(new DiceCommand());
        addCommand(new CrashCommand());
    }

    private void addCommand(ICommand cmd) {
        boolean nameFound = this.commands.stream().anyMatch((it) -> it.getName().equalsIgnoreCase(cmd.getName()));

        if (nameFound) {
            throw new IllegalArgumentException("Command name already found");
        }

        commands.add(cmd);
    }

    @Nullable
    public ICommand getCommand(String search) {
        String searchLower = search.toLowerCase();

        for (ICommand cmd : this.commands) {
            if (cmd.getName().equals(searchLower) || cmd.getAliases().contains(searchLower)) {
                return cmd;
            }
        }

        return null;
    }

    public List<ICommand> getCommands() {
        return commands;
    }

    void handle(GuildMessageReceivedEvent event) {
        String[] split = event.getMessage().getContentRaw()
                .replaceFirst("(?i)" + Pattern.quote(Config.get("PREFIX")), "")
                .split("\\s+");

        String invoke = split[0].toLowerCase();
        ICommand cmd = this.getCommand(invoke);

        if (cmd != null) {
            List<String> args = Arrays.asList(split).subList(1, split.length);

            CommandContext ctx = new CommandContext(event, args);

            cmd.handle(ctx);
        }
    }

}
