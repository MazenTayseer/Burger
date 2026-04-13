package MusicCommands;

import Commands.CommandContext;
import Commands.ICommand;
import Music.GuildMusicManager;
import Music.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.TextChannel;

public class VolumeCommand implements ICommand {
    EmbedBuilder embed = new EmbedBuilder();
    @SuppressWarnings({"DuplicatedCode", "ConstantConditions"})
    @Override
    public void handle(CommandContext ctx) {
        String[] args = ctx.getMessage().getContentRaw().split(" ");
        final TextChannel channel = ctx.getChannel();

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

        int volume = Integer.parseInt(args[1]);
        if(volume > 100 || volume < 0){
            embed.setTitle("❌ Oops");
            embed.setDescription("**Volume must be between `0` and `100`.**");
            embed.setColor(0xdd2e44);
            channel.sendMessage(embed.build()).queue();
            embed.clear();
            return;
        }
        audioPlayer.setVolume(volume);
        embed.setDescription("**✅ Volume changed to **`"+ volume + "`.");
        embed.setColor(0x77b255);
        channel.sendMessage(embed.build()).queue();
        embed.clear();

    }

    @Override
    public String getName() {
        return "volume";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Volume");
        embed.setDescription("Adjusts the volume of the bot.");
        embed.addField("Usage","**`-volume`** `volume between 0 and 100`",true);
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
