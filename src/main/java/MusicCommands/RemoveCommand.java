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
import java.util.concurrent.BlockingQueue;


@SuppressWarnings("DuplicatedCode")
public class RemoveCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();
    @SuppressWarnings("AccessStaticViaInstance")
    @Override
    public void handle(CommandContext ctx) {
        String[] args = ctx.getMessage().getContentRaw().split(" ");
        final TextChannel channel = ctx.getChannel();



        if(args.length != 2){
            embed.setTitle("❌ Oops");
            embed.setDescription("Usage:  **`-remove`** `Number of track in queue`");
            embed.setColor(0xdd2e44);
            ctx.getChannel().sendMessage(embed.build()).queue();
            embed.clear();
            return;
        }

        if(!isNumber(args[1])){
            embed.setTitle("❌ Oops");
            embed.setDescription("Usage: **`-remove`** `Number of track in queue`");
            embed.setColor(0xdd2e44);
            ctx.getChannel().sendMessage(embed.build()).queue();
            embed.clear();
            return;
        }

        final GuildVoiceState BotVoiceState = ctx.getSelfMember().getVoiceState();
        final GuildVoiceState memberVoiceState = ctx.getMember().getVoiceState();
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

        boolean lofi = musicManager.trackScheduler.lofi;

        if(!lofi){
            boolean newRepeating = musicManager.trackScheduler.repeating;
            if(newRepeating){
                try{
                    AudioTrack track = musicManager.trackScheduler.trackArrayList.get(element);
                    musicManager.trackScheduler.trackArrayList.remove(track);
                    embed.setDescription("✅  **Song removed.**");
                    embed.setColor(0x77b255);
                    channel.sendMessage(embed.build()).queue();
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
                AudioTrack track1 = musicManager.trackScheduler.trackArrayList.get(element);
                musicManager.trackScheduler.trackArrayList.remove(track1);
                if(queue.contains(track)){
                    queue.remove();
                }
            } catch (IndexOutOfBoundsException e ){
                embed.setTitle("❌ Oops");
                embed.setDescription("**Cannot find track.**");
                embed.setColor(0xdd2e44);
                channel.sendMessage(embed.build()).queue();
                embed.clear();
                return;
            }
            embed.setDescription("✅  **Song removed.**");
            embed.setColor(0x77b255);
            channel.sendMessage(embed.build()).queue();
            embed.clear();
        }
        else{
            embed.setTitle("Lofi mode on");
            embed.setDescription("`Loop`, `Seek`,`Shuffle`, `Play`, `Remove`, `SkipTo`, `Queue` commands are disabled.");
            embed.setColor(0xffbc12);
            channel.sendMessage(embed.build()).queue();
            embed.clear();
        }


    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Remove");
        embed.setDescription("Removes a song from the queue.");
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
