package MusicCommands;

import Commands.CommandContext;
import Commands.ICommand;
import Lofi.LofiCommand;
import Music.GuildMusicManager;
import Music.PlayerManager;
import Music.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

@SuppressWarnings("DuplicatedCode")
public class SkipToCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();
    @Override
    public void handle(CommandContext ctx) {
        String[] args = ctx.getMessage().getContentRaw().split(" ");
        final TextChannel channel = ctx.getChannel();


        final GuildVoiceState BotVoiceState = ctx.getSelfMember().getVoiceState();
        final GuildVoiceState memberVoiceState = ctx.getMember().getVoiceState();

        if(args.length != 2){
            embed.setTitle("❌ Oops");
            embed.setDescription("Usage: **`-skipto`** `Number of track in queue`.");
            embed.setColor(0xdd2e44);
            ctx.getChannel().sendMessage(embed.build()).queue();
            embed.clear();
            return;
        }

        if(!isNumber(args[1])){
            embed.setTitle("❌ Oops");
            embed.setDescription("Usage: **`-skipto`** `Number of track in queue`.");
            embed.setColor(0xdd2e44);
            ctx.getChannel().sendMessage(embed.build()).queue();
            embed.clear();
            return;
        }


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

        GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(ctx.getGuild());
        BlockingQueue<AudioTrack> queue = musicManager.trackScheduler.queue;
        int element = Integer.parseInt(args[1]);
        element -=1;


        if(!musicManager.trackScheduler.lofi){
            boolean newRepeating = musicManager.trackScheduler.repeating;
            if(newRepeating){
                try{
                    AudioTrack track = musicManager.trackScheduler.trackArrayList.get(element);
                    AudioPlayer player = musicManager.trackScheduler.player;
                    embed.setDescription("✅  **Skipped to track number ** **`" + (element + 1) +"`** ** in queue.**");
                    embed.setColor(0x77b255);
                    channel.sendMessage(embed.build()).queue();
                    embed.clear();
                    player.startTrack(track.makeClone(),false);
//                    PlayerManager.getINSTANCE().queueNowPlaying(player);

                } catch (IndexOutOfBoundsException e){
                    embed.setTitle("❌ Oops");
                    embed.setDescription("**Cannot find track**");
                    embed.setColor(0xdd2e44);
                    channel.sendMessage(embed.build()).queue();
                    embed.clear();
                    return;
                }
                return;

            }



            ArrayList<AudioTrack> trackList = new ArrayList<>(musicManager.trackScheduler.queue);
            try{
                AudioTrack track= trackList.get(element);
                if(queue.contains(track)){
                    for(int i=0;i<element;i++){
                        queue.remove();
                    }
                    embed.setDescription("✅  **Skipped to track number ** **`" + (element + 1) +"`** ** in queue.**");
                    embed.setColor(0x77b255);
                    channel.sendMessage(embed.build()).queue();
                    embed.clear();
                    musicManager.trackScheduler.nextTrack();
                }
            } catch (IndexOutOfBoundsException e ){
                embed.setDescription("**Cannot find track**");
                channel.sendMessage(embed.build()).queue();
                embed.clear();
            }
        }
        else{
            embed.setTitle("Lofi mode on");
            embed.setDescription("`Loop`, `Seek`,`Shuffle`, `Play`, `Remove`, `SkipTo`, `Queue`, `Clear` commands are disabled.");
            embed.setColor(0xffbc12);
            channel.sendMessage(embed.build()).queue();
            embed.clear();
        }


    }

    @Override
    public String getName() {
        return "skipto";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Skip to");
        embed.setDescription("Skips to a song in the current queue.");
        embed.addField("Usage","**`-remove`** `Number of track in queue`",true);
        embed.setColor(0xffffff);
        return embed;
    }

    private boolean isNumber(String message){
        try{
            Integer.parseInt(message);
            return true;
        } catch (NumberFormatException exception){
            return false;
        }
    }
}
