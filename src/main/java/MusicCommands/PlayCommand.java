package MusicCommands;

import Commands.CommandContext;
import Commands.ICommand;
import Lofi.LofiCommand;
import Music.GuildMusicManager;
import Music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;


@SuppressWarnings("ConstantConditions")
public class PlayCommand implements ICommand {
    private static EmbedBuilder embed = new EmbedBuilder();
    @SuppressWarnings("DuplicatedCode")
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        final Member member = ctx.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();

        final Member Bot = ctx.getSelfMember();
        final GuildVoiceState BotVoiceState = Bot.getVoiceState();

        final AudioManager audioManager = ctx.getGuild().getAudioManager();
        audioManager.setSelfDeafened(true);

        final GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(ctx.getGuild());

        if(ctx.getArgs().isEmpty()){
            embed.setTitle("❌ Oops");
            embed.setDescription("Usage:  **-play** `Song name or URL`");
            embed.setColor(0xdd2e44);
            channel.sendMessage(embed.build()).queue();
            embed.clear();
            return;
        }

        if (!memberVoiceState.inVoiceChannel()) {
            embed.setTitle("❌ Oops");
            embed.setDescription("You need to be in a voice channel for the **`" + this.getName() + "`** command to work.");
            embed.setColor(0xdd2e44);
            channel.sendMessage(embed.build()).queue();
            embed.clear();
            return;
        }

        if(!BotVoiceState.inVoiceChannel()){
            final VoiceChannel memberChannel = memberVoiceState.getChannel();
            audioManager.openAudioConnection(memberChannel);
        }
        else if(!memberVoiceState.getChannel().equals(BotVoiceState.getChannel())){
            embed.setTitle("❌ Oops");
            embed.setDescription("You need to be in a voice channel with me for this command to work.");
            embed.setColor(0xdd2e44);
            channel.sendMessage(embed.build()).queue();
            embed.clear();
            return;
        }

        if(!musicManager.trackScheduler.lofi){
            String link = String.join(" ", ctx.getArgs());


            musicManager.trackScheduler.playlist = link.contains("playlist") && isUrl(link);

            if (!isUrl(link)) {
                link = "ytsearch:" + link;
            }

            musicManager.trackScheduler.channel = channel;
            musicManager.trackScheduler.guild = ctx.getGuild();
            PlayerManager.getINSTANCE().loadAndPlay(channel,link);
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
        return "play";
    }


    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Play");
        embed.setDescription("Plays a song.");
        embed.addField("Usage","**`-play`** `Song name or URL`",true);
        embed.addField("Aliases","`p`",true);
        embed.setColor(0xffffff);
        return embed;
    }

    private boolean isUrl(String url){
        try{
            new URL(url);
            return true;
        } catch (MalformedURLException e ){
            return false;
        }
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("p");
    }
}




