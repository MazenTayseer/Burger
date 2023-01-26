package Music;

import Bot.Main;
import CasinoMaps.*;
import MusicCommands.NowPlayingCommand;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.managers.AudioManager;
import org.w3c.dom.Text;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("DuplicatedCode")
public class TrackScheduler extends AudioEventAdapter{
    public final AudioPlayer player;
    public final BlockingQueue<AudioTrack> queue;
    public boolean repeating =false;
    public boolean lofi = false;
    public List<AudioTrack> trackArrayList;
    public TextChannel channel;
    private int index;
    public List<AudioTrack> nowPlayingList;
    public Boolean available = true;
    public Boolean playlist = false;

    public Guild guild;
    public final Map<User, GameBlackjack> blackjackMap = new HashMap<>();
    public final Map<User, String> winningHorseDouble = new HashMap<>();
    public final Map<User, GameHorse> winningHorseString = new HashMap<>();
    public final Map<User, GameHighLow> highLowGame = new HashMap<>();
    public final Map<User, Timer> wageMap = new HashMap<>();
    public final Map<User, GameTrivia> triviaMap = new HashMap<>();
    public final Map<User, GameVote> voteMap = new HashMap<>();
    public final Map<User, GameCrash> crashMap = new HashMap<>();


    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        queue = new LinkedBlockingQueue<>();
        trackArrayList = new ArrayList<>();
        nowPlayingList = new ArrayList<>();
    }

    public void queue(AudioTrack track) {
        if (!this.player.startTrack(track, true)) {
            queue.offer(track);
        }
    }


    public void nextTrack() {
        if(repeating) {
            AudioTrack playingTrack = player.getPlayingTrack();
            index = findAudioTrack(playingTrack);

            List<AudioTrack> audioTracks = trackArrayList;
            AudioTrack lastTrack = audioTracks.get(trackArrayList.size() - 1);

            if (playingTrack.getInfo().title.equals(lastTrack.getInfo().title)) {
                index = -1;
            }

            this.player.startTrack(audioTracks.get(index + 1).makeClone(), false);
            return;
        }

        this.player.startTrack(queue.poll(), false);

        if(player.getPlayingTrack() == null && queue.size()==0){
//            AudioManager audioManager = guild.getAudioManager();
//            Main.jda.getGatewayPool().schedule(audioManager::closeAudioConnection,1,TimeUnit.SECONDS);
//            EmbedBuilder embedBuilder = new EmbedBuilder();
//            embedBuilder.setTitle("GoodBye!");
//            embedBuilder.setDescription("Queue ended so I left the voice channel.");
//            embedBuilder.setColor(Color.white);
//
//            Button voteMe = Button.link("https://top.gg/bot/991196140626247702", "🆙️Vote me");
//
//            channel.sendMessageEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(voteMe)).queueAfter(1,TimeUnit.SECONDS);
//            trackArrayList.clear();
//            nowPlayingList.clear();
//            embedBuilder.clear();
        }

    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            List<AudioTrack> audioTracks = trackArrayList;
            AudioTrack lastTrack = audioTracks.get(audioTracks.size()-1);
            if (repeating) {
                index = findAudioTrack(track);
                if (track.getInfo().title.equals(lastTrack.getInfo().title)) {
                    index = -1;
                }


                this.player.startTrack(audioTracks.get(index + 1).makeClone(), false);
                return;
            }


            nextTrack();
        }
        else if(track == null && queue.size()==0){
//            AudioManager audioManager = guild.getAudioManager();
//            Main.jda.getGatewayPool().schedule(audioManager::closeAudioConnection,1,TimeUnit.SECONDS);
//            EmbedBuilder embedBuilder = new EmbedBuilder();
//            embedBuilder.setTitle("GoodBye!");
//            embedBuilder.setDescription("Queue ended so I left the voice channel.");
//            embedBuilder.setColor(Color.white);
//
//            Button voteMe = Button.link("https://top.gg/bot/991196140626247702", "🆙️Vote me");
//
//            channel.sendMessageEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(voteMe)).queueAfter(1,TimeUnit.SECONDS);
//            trackArrayList.clear();
//            nowPlayingList.clear();
//            embedBuilder.clear();
        }


    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        PlayerManager.getINSTANCE().queueNowPlaying(channel,player);
    }

    private int findAudioTrack(AudioTrack track){
        for(int i=0;i<trackArrayList.size();i++){
            if(trackArrayList.get(i).getInfo().title.equals(track.getInfo().title)){
                return i;
            }
        }
        return -1;
    }

}


