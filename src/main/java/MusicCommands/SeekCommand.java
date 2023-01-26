package MusicCommands;

import Commands.CommandContext;
import Commands.ICommand;
import Lofi.LofiCommand;
import Music.GuildMusicManager;
import Music.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.TextChannel;

public class SeekCommand implements ICommand {
    private EmbedBuilder embed = new EmbedBuilder();
    @SuppressWarnings({"DuplicatedCode", "ConstantConditions"})
    @Override
    public void handle(CommandContext ctx) {
        String[] args = ctx.getMessage().getContentRaw().split(" ");
        final TextChannel channel = ctx.getChannel();
        EmbedBuilder embed = new EmbedBuilder();

        if(args.length != 2){
            embed.setTitle("❌ Oops");
            embed.setDescription("Usage:  **`-seek`** `duration in seconds`");
            embed.setColor(0xdd2e44);
            ctx.getChannel().sendMessage(embed.build()).queue();
            embed.clear();
            return;
        }

        if(!isNumber(args[1])){
            embed.setTitle("❌ Oops");
            embed.setDescription("Usage: `**-seek** duration in seconds`");
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

        final GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(ctx.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;
        final AudioTrack track = audioPlayer.getPlayingTrack();

        if(!musicManager.trackScheduler.lofi){
            if(track == null){
                embed.setTitle("❌ Oops");
                embed.setDescription("There is no track playing right now");
                embed.setColor(0xdd2e44);
                channel.sendMessage(embed.build()).queue();
                embed.clear();
                return;
            }

            int newPosition = Integer.parseInt(args[1]);
            newPosition *= 1000;


            if(newPosition > track.getDuration()){
                embed.setDescription("✅  **Song Skipped.**");
                embed.setColor(0x77b255);
                channel.sendMessage(embed.build()).queue();
                track.setPosition(newPosition);
                embed.clear();
                return;
            }

            track.setPosition(newPosition);
            embed.setDescription("✅  Jumped to **`" + newPosition/1000 +"`** **`seconds`** of the song.");
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
        return "seek";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Seek");
        embed.setDescription("Seeks through a currently playing song.");
        embed.addField("Usage","**`-seek`** `duration in seconds`",true);
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
