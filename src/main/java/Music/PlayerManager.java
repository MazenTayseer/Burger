package Music;

import Lofi.LofiCommand;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.*;
import java.util.concurrent.TimeUnit;


@SuppressWarnings("ALL")
public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;
    private static String trackName;
    private static String trackLength;
    private EmbedBuilder embed = new EmbedBuilder();
    private EmbedBuilder embedBuilder = new EmbedBuilder();
    private static TextChannel textChannel;
    private static ArrayList<String> trackNameList = new ArrayList<>();
    private static ArrayList<String> trackLengthList = new ArrayList<>();

    public PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);

            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());

            return guildMusicManager;
        });
    }

    public void loadAndPlay(TextChannel channel, String trackURL){
        final GuildMusicManager musicManager = this.getMusicManager(channel.getGuild());
        textChannel = channel;
        this.audioPlayerManager.loadItemOrdered(musicManager, trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.trackScheduler.trackArrayList.add(track);
                String title = track.getInfo().title;

                trackName = track.getInfo().title;

                trackLength =  formatTime(track.getInfo().length);
                trackNameList.add(trackName);


                embed.setTitle("Queued");
                embed.setDescription("["+ track.getInfo().title+"](https://www.youtube.com/watch?v=" + track + ")\n" +
                        "`[ " + "00:00" +  " / " + formatTime(track.getInfo().length) + " ]`\n" +
                        "Position in queue: `" + String.valueOf(musicManager.trackScheduler.queue.size()+1) + "`");
                embed.setThumbnail("https://img.youtube.com/vi/"+track.getIdentifier()+"/mqdefault.jpg");
                channel.sendMessage(embed.build()).queue();
                embed.clear();

                musicManager.trackScheduler.queue(track);

            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if(musicManager.trackScheduler.lofi){
                    List<AudioTrack> lofiTracks = playlist.getTracks();
                    Collections.shuffle(lofiTracks);

                    for(int i=0;i<lofiTracks.size();i++){
                        musicManager.trackScheduler.queue(lofiTracks.get(i));
                    }
                    return;
                }

                if(musicManager.trackScheduler.playlist){
                    List<AudioTrack> playlistTracks = playlist.getTracks();
                    musicManager.trackScheduler.nowPlayingList.add(playlistTracks.get(0));

                    for(int i=0;i<playlistTracks.size();i++){
                        musicManager.trackScheduler.queue(playlistTracks.get(i));
                    }
                    return;
                }

                final List<AudioTrack> tracks = playlist.getTracks();
                musicManager.trackScheduler.trackArrayList.add(tracks.get(0));
                musicManager.trackScheduler.nowPlayingList.add(tracks.get(0));
                String title = tracks.get(0).getInfo().title;

                trackName = tracks.get(0).getInfo().title;

                trackLength =  formatTime(tracks.get(0).getInfo().length);
                trackNameList.add(trackName);

                if(musicManager.trackScheduler.nowPlayingList.size()==1){
                    musicManager.trackScheduler.queue(tracks.get(0));
                }
                else{
                    embed.setTitle("Queued");
                    embed.setDescription("["+ tracks.get(0).getInfo().title+"](https://www.youtube.com/watch?v=" + tracks.get(0) + ")\n" +
                            "`[ " + "00:00" +  " / " + formatTime(tracks.get(0).getInfo().length) + " ]`\n" +
                            "Position in queue: `" + String.valueOf(musicManager.trackScheduler.queue.size()+1) + "`");
                    embed.setThumbnail("https://img.youtube.com/vi/"+tracks.get(0).getIdentifier()+"/mqdefault.jpg");
                    channel.sendMessage(embed.build()).queue();
                    embed.clear();

                    musicManager.trackScheduler.queue(tracks.get(0));

                }
            }

            @Override
            public void noMatches() {
                embed.setTitle("❌ Oops");
                embed.setDescription("I Couldnt find searched song on youtube");
                embed.setColor(0xdd2e44);
                channel.sendMessage(embed.build()).queue();
                embed.clear();
                return;
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                embed.setTitle("❌ Oops");
                embed.setDescription("Sorry, I caught an error.");
                embed.setColor(0xdd2e44);
                channel.sendMessage(embed.build()).queue();
                embed.clear();
                return;
            }





        });

    }

    public static PlayerManager getINSTANCE() {
        if(INSTANCE == null){
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

    @SuppressWarnings("DuplicatedCode")
    private static String formatTime(long timeInMillis) {
        final long minutes = timeInMillis / TimeUnit.MINUTES.toMillis(1);
        final long seconds = timeInMillis % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);

        return String.format("%02d:%02d", minutes, seconds);
    }

    public void queueNowPlaying(TextChannel textChannel,AudioPlayer nowPlaying){
        Button pause = Button.secondary("pause","⏯️Pause / Resume");
        Button volumeUp = Button.secondary("volumeUp","🔊 Volume Up");
        Button volumeDown = Button.secondary("volumeDown","🔉 Volume down");
        Button skip = Button.secondary("skip","⏭️Skip");
        Button stop = Button.secondary("stop","⏹️Stop");
        Button queue = Button.secondary("queue","#️⃣  ️Queue");
        Button loop = Button.secondary("loop","🔁  Loop");
        Button clear = Button.secondary("clear","🗑️  Clear");

        Button pause1 = Button.secondary("pause","⏸️Pause / Resume").asDisabled();
        Button volumeUp1 = Button.secondary("volumeUp","🔊 Volume Up").asDisabled();
        Button volumeDown1 = Button.secondary("volumeDown","🔉 Volume down").asDisabled();
        Button skip1 = Button.secondary("skip","⏭️Skip").asDisabled();
        Button stop1 = Button.secondary("stop","⏹️Stop").asDisabled();
        Button queue1 = Button.secondary("queue","#️  Queue").asDisabled();
        Button loop1 = Button.secondary("loop","🔁  Loop").asDisabled();
        Button clear1 = Button.secondary("clear","🗑️  Clear").asDisabled();
        embedBuilder.setTitle("Now Playing");
        embedBuilder.setDescription("["+ nowPlaying.getPlayingTrack().getInfo().title+"](https://www.youtube.com/watch?v=" + nowPlaying.getPlayingTrack() + ")\n" +
                "`[" + "00:00" +  " / " + formatTime(nowPlaying.getPlayingTrack().getInfo().length) + "] `" );
        embedBuilder.setColor(0x0d8bbe);
        embedBuilder.setThumbnail("https://img.youtube.com/vi/"+nowPlaying.getPlayingTrack().getIdentifier()+"/mqdefault.jpg");

        long duration = nowPlaying.getPlayingTrack().getDuration();
        embed.setTitle("Now Playing");
        embed.setDescription("["+ nowPlaying.getPlayingTrack().getInfo().title+"](https://www.youtube.com/watch?v=" + nowPlaying.getPlayingTrack() + ")\n" +
                "`[ " + "00:00" +  " / " + formatTime(nowPlaying.getPlayingTrack().getInfo().length) + " ]`" );
        embed.setColor(0x0d8bbe);
        embed.setThumbnail("https://img.youtube.com/vi/"+nowPlaying.getPlayingTrack().getIdentifier()+"/mqdefault.jpg");

        textChannel.sendMessageEmbeds(embed.build()).setActionRows(ActionRow.of(pause,skip,stop,clear) ,ActionRow.of(volumeDown,queue,loop,volumeUp))
                .queue(e->e.editMessageEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(pause1,skip1,stop1,clear1) ,ActionRow.of(volumeDown1,queue1,loop1,volumeUp1)).queueAfter(duration,TimeUnit.MILLISECONDS));


        embed.clear();
    }
}