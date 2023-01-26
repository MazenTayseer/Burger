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

public class ResumeCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();

    @SuppressWarnings({"DuplicatedCode", "ConstantConditions"})
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

        if (track == null) {
            embed.setTitle("❌ Oops");
            embed.setDescription("There is no track playing right now.");
            embed.setColor(0xdd2e44);
            channel.sendMessage(embed.build()).queue();
            embed.clear();
            return;
        }

        if(audioPlayer.isPaused()){
            embed.setDescription("✅  **Song resumed.**");
            embed.setColor(0x77b255);
            channel.sendMessage(embed.build()).queue();
            embed.clear();
            audioPlayer.setPaused(false);
        }
        else{
            embed.setDescription("❌ Song is not paused");
            embed.setColor(0xdd2e44);
            channel.sendMessage(embed.build()).queue();
            embed.clear();

        }



    }

    @Override
    public String getName() {
        return "resume";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Resume");
        embed.setDescription("Resumes the current paused song.");
        embed.addField("Usage","**`-resume`**",true);
        embed.setColor(0xffffff);
        return embed;
    }
}
