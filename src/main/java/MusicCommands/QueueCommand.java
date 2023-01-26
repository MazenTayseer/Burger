package MusicCommands;

import Commands.CommandContext;
import Commands.ICommand;
import Lofi.LofiCommand;
import Music.GuildMusicManager;
import Music.PlayerManager;
import Music.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class QueueCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();
    @SuppressWarnings("DuplicatedCode")
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(ctx.getGuild());
        ArrayList<AudioTrack> trackList = new ArrayList<>(musicManager.trackScheduler.queue);
        final AudioTrack currentTrack = musicManager.audioPlayer.getPlayingTrack();
        final AudioPlayer audioPlayer = musicManager.audioPlayer;
        AudioTrack nowPlaying = audioPlayer.getPlayingTrack();
        final int trackCount = Math.min(trackList.size(), 10);


        if(!musicManager.trackScheduler.lofi){
            boolean newRepeating = musicManager.trackScheduler.repeating;
            if(newRepeating){

                for(int i=0; i < trackCount; i++){
                    embed.setTitle("#️⃣  Looping Queue");
                    embed.setDescription("**Now Playing: **" + nowPlaying.getInfo().title + "   "+'`' + formatTime(nowPlaying.getPosition()) + '`' + " / " +'`' + formatTime(nowPlaying.getInfo().length) + '`');
                    embed.addField("", "**"+(i + 1)+".** " + musicManager.trackScheduler.trackArrayList.get(i).getInfo().title + "   "+'`' + formatTime(musicManager.trackScheduler.trackArrayList.get(i).getInfo().length) + '`'+ "\n",false);
                    embed.setColor(0x5dadec);

                    if (trackList.size() > trackCount) {
                        embed.setFooter("and " + (trackList.size() - trackCount) + " more... ");
                    }
                }
                channel.sendMessage(embed.build()).queue();
                embed.clear();
                return;
            }

            if(trackList.isEmpty()){
                if(currentTrack != null){
                    embed.setTitle("#️⃣   Queue");
                    embed.setDescription("**Now Playing: **" + nowPlaying.getInfo().title + "   "+'`' + formatTime(nowPlaying.getPosition()) + '`' + " / " +'`' + formatTime(nowPlaying.getInfo().length) + '`');
                    embed.addField("","**Queue is empty.**",false);
                    embed.setColor(0x5dadec);
                }else{
                    embed.setDescription("**Queue is empty.**");
                    embed.setColor(0xdd2e44);
                }
                channel.sendMessage(embed.build()).queue();
                embed.clear();
                return;
            }




            for(int i=0; i < trackCount; i++){
                AudioTrack track = trackList.get(i);
                AudioTrackInfo info = track.getInfo();


                embed.setTitle("🔁   Queue");
                embed.setDescription("**Now Playing: **" + nowPlaying.getInfo().title + "   "+'`' + formatTime(nowPlaying.getPosition()) + '`' + " / " +'`' + formatTime(nowPlaying.getInfo().length) + '`');
                embed.addField("", "**"+(i + 1)+".** " + info.title + "   "+'`' + formatTime(track.getInfo().length) + '`'+ "\n",false);
                embed.setColor(0x5dadec);

                if (trackList.size() > trackCount) {
                    embed.setFooter("and " + (trackList.size() - trackCount) + " more... ");
                }

            }
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
        return "queue";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Queue");
        embed.setDescription("Shows the queued songs");
        embed.addField("Usage","**`-queue`**",true);
        embed.setColor(0xffffff);
        return embed;
    }

    @SuppressWarnings("DuplicatedCode")
    private String formatTime(long timeInMillis) {
        final long minutes = timeInMillis / TimeUnit.MINUTES.toMillis(1);
        final long seconds = timeInMillis % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);

        return String.format("%02d:%02d", minutes, seconds);
    }
}
