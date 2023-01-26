package MusicCommands;

import Commands.CommandContext;
import Commands.ICommand;
import Lofi.LofiCommand;
import Music.GuildMusicManager;
import Music.PlayerManager;
import Music.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;

public class ClearCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();
    @SuppressWarnings({"DuplicatedCode", "ConstantConditions"})
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        final GuildVoiceState BotVoiceState = ctx.getSelfMember().getVoiceState();
        final GuildVoiceState memberVoiceState = ctx.getMember().getVoiceState();

        final GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(ctx.getGuild());

        if(!BotVoiceState.inVoiceChannel()){
            embed.setTitle("❌ Oops");
            embed.setDescription("I'm not in a voice channel for the **`" + this.getName() + "`** Command to work.");
            embed.setColor(0xdd2e44);
            channel.sendMessage(embed.build()).queue();
            embed.clear();
            return;
        }
        else if(!memberVoiceState.inVoiceChannel()){
            embed.setTitle("❌ Oops");
            embed.setDescription("You need to be in a voice channel for the **`" + this.getName() + "`** command to work.");
            embed.setColor(0xdd2e44);
            channel.sendMessage(embed.build()).queue();
            embed.clear();
            return;
        }



        if (!memberVoiceState.getChannel().equals(BotVoiceState.getChannel())) {
            embed.setTitle("❌ Oops");
            embed.setDescription("You need to be in a voice channel with me for the **`" + this.getName() + "`** command to work.");
            embed.setColor(0xdd2e44);
            channel.sendMessage(embed.build()).queue();
            embed.clear();
            return;
        }

        if(!musicManager.trackScheduler.lofi){
            if (!musicManager.trackScheduler.repeating){
                ArrayList<AudioTrack> trackList1 = new ArrayList<>(musicManager.trackScheduler.queue);
                if(trackList1.isEmpty()){
                    embed.setDescription("**Queue is empty.**");
                    channel.sendMessage(embed.build()).queue();
                    embed.clear();
                    return;
                }

                musicManager.trackScheduler.queue.clear();
            }
            else{
                List<AudioTrack> trackList2 = musicManager.trackScheduler.trackArrayList;
                if(trackList2.isEmpty()){
                    embed.setDescription("**Queue is empty.**");
                    channel.sendMessage(embed.build()).queue();
                    embed.clear();
                    return;
                }

                musicManager.trackScheduler.trackArrayList.clear();

            }
            embed.setDescription("✅  ** Queue cleared.**");
            embed.setColor(0x77b255);

        }
        else{
            embed.setTitle("Lofi mode on");
            embed.setDescription("`Loop`, `Seek`,`Shuffle`, `Play`, `Remove`, `SkipTo`, `Queue`, `Clear` commands are disabled.");
            embed.setColor(0xffbc12);
        }
        channel.sendMessage(embed.build()).queue();
        embed.clear();

    }

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Clear");
        embed.setDescription("Empties the current queue.");
        embed.addField("Usage","**`-clear`**",true);
        embed.setColor(0xffffff);
        return embed;
    }

}
