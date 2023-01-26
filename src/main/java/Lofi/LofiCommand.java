package Lofi;

import Commands.CommandContext;
import Commands.ICommand;
import Music.GuildMusicManager;
import Music.PlayerManager;
import Music.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;


public class LofiCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();
    @SuppressWarnings({"DuplicatedCode", "ConstantConditions"})
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

        String[] args = ctx.getMessage().getContentRaw().split(" ");

        if (!memberVoiceState.inVoiceChannel()) {
            embed.setTitle("❌ Oops");
            embed.setDescription("You need to be in a voice channel for the **`" + this.getName() + "`** command to work.");
            embed.setColor(0xdd2e44);
            channel.sendMessage(embed.build()).queue();
            embed.clear();
            return;
        }

        if(ctx.getArgs().isEmpty()){
            embed.setDescription("Lofi mode is currently set to `" + musicManager.trackScheduler.lofi + "`");
            embed.addField("Usage", "**`-lofi`** on / off",false);
            embed.setColor(0xffbc12);
            ctx.getChannel().sendMessage(embed.build()).queue();
            embed.clear();
            return;
        }
        else if(!args[1].equalsIgnoreCase("on") && !args[1].equalsIgnoreCase("off")){
            embed.setDescription("Lofi mode is currently set to `" + musicManager.trackScheduler.lofi + "`");
            embed.addField("Usage", "**`-lofi`** on / off",false);
            embed.setColor(0xffbc12);
            ctx.getChannel().sendMessage(embed.build()).queue();
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


        if (args[1].equalsIgnoreCase("on")) {
            if(!musicManager.trackScheduler.lofi){
                final AudioPlayer audioPlayer = musicManager.audioPlayer;
                final AudioTrack track = audioPlayer.getPlayingTrack();
                if(musicManager.trackScheduler.trackArrayList.isEmpty() && musicManager.trackScheduler.queue.isEmpty() && track == null){
                    embed.setDescription("**Lofi mode** `on`.");
                    embed.setColor(0xffbc12);
                    ctx.getChannel().sendMessage(embed.build()).queue();
                    musicManager.trackScheduler.lofi = true;
                    embed.clear();
                    PlayerManager.getINSTANCE().loadAndPlay(channel,"https://www.youtube.com/playlist?list=PL6NdkXsPL07IOu1AZ2Y2lGNYfjDStyT6O");
                }
                else{
                    embed.setTitle("❌ Oops");
                    embed.setDescription("I cannot turn on lofi when there is a song playing or the queue is not empty.");
                    embed.setColor(0xdd2e44);
                    channel.sendMessage(embed.build()).queue();
                    embed.clear();
                }

            }
            else{
                embed.setDescription("Lofi mode is already set to `" + musicManager.trackScheduler.lofi + "`");
                embed.setColor(0xffbc12);
                ctx.getChannel().sendMessage(embed.build()).queue();
                embed.clear();
            }

        }
        else if(args[1].equalsIgnoreCase("off")){
            if(musicManager.trackScheduler.lofi){
                embed.setDescription("**Lofi mode** `off`.");
                embed.setColor(0xffbc12);
                ctx.getChannel().sendMessage(embed.build()).queue();

                musicManager.trackScheduler.player.stopTrack();
                musicManager.trackScheduler.queue.clear();
                audioManager.closeAudioConnection();
                musicManager.trackScheduler.repeating = false;
                musicManager.trackScheduler.trackArrayList.clear();
                musicManager.trackScheduler.lofi = false;
            }
            else{
                embed.setDescription("Lofi mode is already set to `" + musicManager.trackScheduler.lofi + "`");
                embed.setColor(0xffbc12);
                ctx.getChannel().sendMessage(embed.build()).queue();
                embed.clear();
            }

        }
        else{
            embed.setDescription("Lofi mode is currently set to `" + musicManager.trackScheduler.lofi + "`");
            embed.addField("Usage", "**`-lofi`** on / off",false);
            embed.setColor(0xffbc12);
            ctx.getChannel().sendMessage(embed.build()).queue();
            embed.clear();
        }
    }

    @Override
    public String getName() {
        return "lofi";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Lofi");
        embed.setDescription("Plays lofi music.");
        embed.addField("Usage","**`-lofi`** `on / off`",true);
        embed.setColor(0xffffff);
        return embed;
    }

}
