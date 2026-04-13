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
import net.dv8tion.jda.api.managers.AudioManager;


public class StopCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();
    @SuppressWarnings({"ConstantConditions", "DuplicatedCode"})
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


        musicManager.trackScheduler.player.stopTrack();
        musicManager.trackScheduler.queue.clear();

        AudioManager audioManager = ctx.getGuild().getAudioManager();
        audioManager.closeAudioConnection();
        musicManager.trackScheduler.lofi = false;
        musicManager.trackScheduler.nowPlayingList.clear();

        musicManager.trackScheduler.repeating = false;
        musicManager.trackScheduler.trackArrayList.clear();

        if (track == null) {
            if(audioManager.isConnected()){
                embed.setTitle("✅  GoodBye!");
                embed.setDescription("**I left the voice channel.**");
                embed.setColor(0x77b255);
                channel.sendMessage(embed.build()).queue();
            }
            else{
                embed.setTitle("❌ Oops");
                embed.setDescription("There is no track playing right now.");
                embed.setColor(0xdd2e44);
                channel.sendMessage(embed.build()).queue();
                embed.clear();
            }
            return;
        }

        embed.setTitle("✅  GoodBye!");
        embed.setDescription("**Music stopped and I left the voice channel.** \n" +
                "\nRequested by : " + ctx.getMember().getUser().getAsMention());
        embed.setColor(0x77b255);
        channel.sendMessage(embed.build()).queue();
        embed.clear();

    }

    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Stop");
        embed.setDescription("Stop the current playing song, clears queue and leaves the voice channel.");
        embed.addField("Usage","**`-stop`**",true);
        embed.setColor(0xffffff);
        return embed;
    }
}
