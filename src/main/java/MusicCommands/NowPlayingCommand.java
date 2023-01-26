package MusicCommands;

import Commands.CommandContext;
import Commands.ICommand;
import Music.GuildMusicManager;
import Music.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NowPlayingCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();
    @SuppressWarnings("DuplicatedCode")
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
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

        final GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(ctx.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;
        final AudioTrack track = audioPlayer.getPlayingTrack();

        if(track == null){
            embed.setTitle("❌ Oops");
            embed.setDescription("There is no track playing right now");
            embed.setColor(0xdd2e44);
            channel.sendMessage(embed.build()).queue();
            embed.clear();
            return;
        }

        Button pause = Button.secondary("pause","⏯️Pause / Resume");
        Button volumeUp = Button.secondary("volumeUp","🔊 Volume Up");
        Button volumeDown = Button.secondary("volumeDown","🔉 Volume down");
        Button skip = Button.secondary("skip","⏭️Skip");
        Button stop = Button.secondary("stop","⏹️Stop");
        Button queue = Button.secondary("queue","#️⃣  ️Queue");
        Button loop = Button.secondary("loop","🔁  Loop");
        Button clear = Button.secondary("clear","🗑️  Clear");

        EmbedBuilder embedBuilder = new EmbedBuilder();
        Button pause1 = Button.secondary("pause","⏸️Pause / Resume").asDisabled();
        Button volumeUp1 = Button.secondary("volumeUp","🔊 Volume Up").asDisabled();
        Button volumeDown1 = Button.secondary("volumeDown","🔉 Volume down").asDisabled();
        Button skip1 = Button.secondary("skip","⏭️Skip").asDisabled();
        Button stop1 = Button.secondary("stop","⏹️Stop").asDisabled();
        Button queue1 = Button.secondary("queue","#️  Queue").asDisabled();
        Button loop1 = Button.secondary("loop","🔁  Loop").asDisabled();
        Button clear1 = Button.secondary("clear","🗑️  Clear").asDisabled();
        embedBuilder.setTitle("Now Playing");
        embedBuilder.setDescription("["+ track.getInfo().title+"](https://www.youtube.com/watch?v=" + track + ")\n" +
                "`[ " + "00:00" +  " / " + formatTime(track.getInfo().length) + " ]`" );
        embedBuilder.setColor(0x0d8bbe);
        embedBuilder.setThumbnail("https://img.youtube.com/vi/"+track.getIdentifier()+"/mqdefault.jpg");

        long duration = track.getDuration() - track.getPosition();

        embed.setTitle("Now Playing");
        embed.setDescription("["+ track.getInfo().title+"](https://www.youtube.com/watch?v=" + track + ")\n" +
                "`[ " + formatTime(track.getPosition()) +  " / " + formatTime(track.getInfo().length) + " ]`" );
        embed.setColor(0x0d8bbe);
        embed.setThumbnail("https://img.youtube.com/vi/"+track.getIdentifier()+"/mqdefault.jpg");
        channel.sendMessageEmbeds(embed.build()).setActionRows(ActionRow.of(pause,skip,stop,clear) ,ActionRow.of(volumeDown,queue,loop,volumeUp))
                .queue(e->e.editMessageEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(pause1,skip1,stop1) ,ActionRow.of(volumeDown1,queue1,loop1,volumeUp1,clear1)).queueAfter(duration,TimeUnit.MILLISECONDS));

        embed.clear();
    }

    @Override
    public String getName() {
        return "nowplaying";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Now Playing");
        embed.setDescription("Shows details of the songs being played.");
        embed.addField("Usage","**`-nowplaying`**",true);
        embed.addField("Aliases","`np`",true);
        embed.setColor(0xffffff);
        return embed;
    }

    @SuppressWarnings("DuplicatedCode")
    private String formatTime(long timeInMillis) {
        final long minutes = timeInMillis / TimeUnit.MINUTES.toMillis(1);
        final long seconds = timeInMillis % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);

        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("np");
    }

}
