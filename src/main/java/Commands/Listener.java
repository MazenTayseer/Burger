package Commands;

import CasinoMaps.GameCrash;
import Database.SQLiteDataSource;
import Music.GuildMusicManager;
import Music.PlayerManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;


import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"DuplicatedCode", "ConstantConditions"})
public class Listener extends ListenerAdapter {
    private final CommandManager manager;
    private final EmbedBuilder embed = new EmbedBuilder();

    private static final DecimalFormat df = new DecimalFormat("0.0");

    public Listener(EventWaiter waiter) {
        manager = new CommandManager(waiter);
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        User user = event.getAuthor();

        if (user.isBot() || event.isWebhookMessage()) {
            return;
        }

        String prefix = Config.get("PREFIX");
        String raw = event.getMessage().getContentRaw();

        if (raw.startsWith(prefix)) {
            manager.handle(event);
        }


    }


    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        final GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;

        AudioManager audioManager = event.getGuild().getAudioManager();

        VoiceChannel channelLeft = event.getChannelLeft();
        List<Member> members = channelLeft.getMembers();
        Member selfMember = event.getGuild().getSelfMember();

        if (members.size() == 1) {
            if (members.contains(selfMember)) {
//                musicManager.trackScheduler.trackArrayList.clear();
//                musicManager.trackScheduler.queue.clear();
//                musicManager.trackScheduler.repeating = false;
//                musicManager.trackScheduler.player.stopTrack();
//                musicManager.trackScheduler.lofi = false;
//                audioManager.closeAudioConnection();
//                embed.setDescription("I was alone in the voice channel so I left, I'm scared of being alone.");
            }

        }

    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(event.getGuild());
        String id = event.getButton().getId();

        Button Music = Button.secondary("Music", "🎵 Music");
        Button LoFi = Button.secondary("LoFi", "🪶 Lo-Fi");
        Button Other = Button.secondary("other", "🍔 Other");
        Button casino = Button.secondary("casino", "🎰 Casino");
        Button voteMe = Button.link("https://top.gg/bot/991196140626247702", "🆙️Vote me");
        Button sourceCode = Button.link("https://www.youtube.com/watch?v=dQw4w9WgXcQ", "Source code");
        Button support = Button.link("https://www.buymeacoffee.com/MazenTayseer", "Support Me!");

        Button crashStopDisabled = Button.danger("crashStop", "Stop!").asDisabled();

        AudioPlayer audioPlayer = musicManager.audioPlayer;
        int volume = audioPlayer.getVolume();

        AudioTrack nowPlaying = musicManager.audioPlayer.getPlayingTrack();
        Button pause = Button.secondary("pause", "⏯️Pause / Resume");
        Button volumeUp = Button.secondary("volumeUp", "🔊 Volume Up");
        Button volumeDown = Button.secondary("volumeDown", "🔉 Volume down");
        Button skip = Button.secondary("skip", "⏭️Skip");
        Button stop = Button.secondary("stop", "⏹️Stop");
        Button queue = Button.secondary("queue", "#️⃣  ️Queue");
        Button loop = Button.secondary("loop", "🔁  Loop");
        Button clear = Button.secondary("clear", "🗑️  Clear");


        Button pauseDisabled = Button.secondary("pause", "⏯️Pause / Resume").asDisabled();
        Button volumeUpDisabled = Button.secondary("volumeUp", "🔊 Volume Up").asDisabled();
        Button volumeDownDisabled = Button.secondary("volumeDown", "🔉 Volume down").asDisabled();
        Button skipDisabled = Button.secondary("skip", "⏭️Skip").asDisabled();
        Button stopDisabled = Button.secondary("stop", "⏹️Stop").asDisabled();
        Button queueDisabled = Button.secondary("queue", "#️⃣  ️Queue").asDisabled();
        Button loopDisabled = Button.secondary("loop", "🔁  Loop").asDisabled();
        Button clearDisabled = Button.secondary("clear", "🗑️  Clear").asDisabled();

        TextChannel channel = event.getTextChannel();
        GuildVoiceState BotVoiceState = event.getGuild().getSelfMember().getVoiceState();
        GuildVoiceState memberVoiceState = event.getMember().getVoiceState();
        switch (id) {
            case "meme":
                WebUtils.ins.getJSONObject("https://apis.duncte123.me/meme").async((json) -> {
                    if (!json.get("success").asBoolean()) {
                        embed.setTitle("❌ Oops");
                        embed.setDescription("An error has occurred, try again later.");
                        embed.setColor(0xdd2e44);
                        channel.sendMessage(embed.build()).queue();
                        embed.clear();
                        return;
                    }

                    final JsonNode data = json.get("data");
                    final String title = data.get("title").asText();
                    final String url = data.get("url").asText();
                    final String image = data.get("image").asText();
                    final EmbedBuilder embed = EmbedUtils.embedImageWithTitle(title, url, image);

                    musicManager.trackScheduler.available = false;

                    event.editMessageEmbeds(embed.build()).queue();
                    embed.clear();
                });
                return;

            case "end":
                embed.setTitle("✅  Interaction stopped!");
                embed.setColor(0x77b255);
                Button button = event.getMessage().getButtonById("meme").asDisabled();
                Button button1 = event.getMessage().getButtonById("end").asDisabled();

                musicManager.trackScheduler.available = true;


                event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(button, button1)).queue();
                embed.clear();
                return;

            case "Music":
                embed.setTitle("Commands List");
                embed.setThumbnail(event.getGuild().getSelfMember().getAvatarUrl());

                embed.addField("🎵 Music",
                        "`Play`, `Stop`, `Pause`, `Resume`, `Queue`," +
                                " `Loop`,`SkipTo`, `Skip`, `Seek`, `Clear`," +
                                " `NowPlaying`, `Leave`, `Remove`, `Volume`, `Shuffle`, `Lyrics`", false);
                embed.setFooter("Created by Mazen#0654");

                embed.setColor(0xffffff);
                event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(Music, LoFi, casino, Other), ActionRow.of(voteMe, support, sourceCode)).queue();
                embed.clear();
                return;

            case "LoFi":
                embed.setTitle("Commands List");
                embed.setThumbnail(event.getGuild().getSelfMember().getAvatarUrl());
                embed.setFooter("Created by Mazen#0654");

                embed.addField("🪶 Lofi Mode", "`Lofi`, `Leave`, `NowPlaying`, `Skip`, `Pause`, `Resume`, `Volume`", false);
                embed.setColor(0xffffff);
                event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(Music, LoFi, casino, Other), ActionRow.of(voteMe, support, sourceCode)).queue();
                embed.clear();
                return;

            case "casino":
                embed.setTitle("Commands List");
                embed.setThumbnail(event.getGuild().getSelfMember().getAvatarUrl());
                embed.setFooter("Created by Mazen#0654");

                embed.addField("🎰  Casino", "`bj`, `crash`, `dice`, `highlow`, `hr`, `trivia`, `bal`" +
                        ", `wage`, `vote`, `leaderboard`", false);
                embed.setColor(0xffffff);
                event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(Music, LoFi, casino, Other), ActionRow.of(voteMe, support, sourceCode)).queue();
                embed.clear();
                return;


            case "other":
                embed.setTitle("Commands List");
                embed.setThumbnail(event.getGuild().getSelfMember().getAvatarUrl());
                embed.setFooter("Created by Mazen#0654");

                embed.addField("🍔 Other", "`Burger`, `ServerInfo`, `Delete`, `CoinFlip`, `Help`, `Meme`, `Stats`", false);
                embed.setColor(0xffffff);

                event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(Music, LoFi, casino, Other), ActionRow.of(voteMe, support, sourceCode)).queue();
                embed.clear();
                return;

            case "volumeDown":
                volume -= 10;
                audioPlayer.setVolume(volume);
                if (volume <= 0) {
                    volume = 0;
                }

                embed.setTitle("Now Playing");
                embed.setDescription("[" + nowPlaying.getInfo().title + "](https://www.youtube.com/watch?v=" + nowPlaying + ")\n" +
                        "`[ " + "00:00" + " / " + formatTime(nowPlaying.getInfo().length) + " ]`");
                embed.setColor(0x0d8bbe);
                embed.setThumbnail("https://img.youtube.com/vi/" + nowPlaying.getIdentifier() + "/mqdefault.jpg");
                event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(pause, skip, stop, clear), ActionRow.of(volumeDown, queue, loop, volumeUp)).queue();
                embed.clear();
                return;

            case "volumeUp":
                audioPlayer = musicManager.audioPlayer;

                volume = audioPlayer.getVolume();
                volume += 10;
                if (volume >= 100) {
                    volume = 100;
                }
                audioPlayer.setVolume(volume);

                embed.setTitle("Now Playing");
                embed.setDescription("[" + nowPlaying.getInfo().title + "](https://www.youtube.com/watch?v=" + nowPlaying + ")\n" +
                        "`[ " + "00:00" + " / " + formatTime(nowPlaying.getInfo().length) + " ]`");
                embed.setColor(0x0d8bbe);
                embed.setThumbnail("https://img.youtube.com/vi/" + nowPlaying.getIdentifier() + "/mqdefault.jpg");
                event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(pause, skip, stop, clear), ActionRow.of(volumeDown, queue, loop, volumeUp)).queue();
                embed.clear();
                return;

            case "pause":
                if (!BotVoiceState.inVoiceChannel()) {
                    embed.setTitle("❌ Oops");
                    embed.setDescription("I'm not in a voice channel for the **`pause`** Command to work.");
                    embed.setColor(0xdd2e44);
                    event.replyEmbeds(embed.build()).queue();
                    embed.clear();
                    return;
                } else if (!memberVoiceState.inVoiceChannel()) {
                    embed.setTitle("❌ Oops");
                    embed.setDescription("You need to be a voice channel for the **`pause`** Command to work.");
                    embed.setColor(0xdd2e44);
                    event.replyEmbeds(embed.build()).queue();
                    embed.clear();
                    return;
                }

                if (!memberVoiceState.getChannel().equals(BotVoiceState.getChannel())) {
                    embed.setTitle("❌ Oops");
                    embed.setDescription("You need to be in a voice channel with me for the **`pause`** command to work.");
                    embed.setColor(0xdd2e44);
                    event.replyEmbeds(embed.build()).queue();
                    embed.clear();
                    return;
                }

                audioPlayer = musicManager.audioPlayer;
                if (audioPlayer.isPaused()) {
                    audioPlayer.setPaused(false);
                } else {
                    audioPlayer.setPaused(true);
                }
                embed.setTitle("Now Playing");
                embed.setDescription("[" + nowPlaying.getInfo().title + "](https://www.youtube.com/watch?v=" + nowPlaying + ")\n" +
                        "`[ " + "00:00" + " / " + formatTime(nowPlaying.getInfo().length) + " ]`");
                embed.setColor(0x0d8bbe);
                embed.setThumbnail("https://img.youtube.com/vi/" + nowPlaying.getIdentifier() + "/mqdefault.jpg");
                event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(pause, skip, stop, clear), ActionRow.of(volumeDown, queue, loop, volumeUp)).queue();
                embed.clear();
                return;

            case "skip":
                if (!BotVoiceState.inVoiceChannel()) {
                    embed.setTitle("❌ Oops");
                    embed.setDescription("I'm not in a voice channel for the **`skip`** Command to work.");
                    embed.setColor(0xdd2e44);
                    event.replyEmbeds(embed.build()).queue();
                    embed.clear();
                    return;
                } else if (!memberVoiceState.inVoiceChannel()) {
                    embed.setTitle("❌ Oops");
                    embed.setDescription("You need to be a voice channel for the **`pause`** Command to work.");
                    embed.setColor(0xdd2e44);
                    event.replyEmbeds(embed.build()).queue();
                    embed.clear();
                    return;
                }

                if (!memberVoiceState.getChannel().equals(BotVoiceState.getChannel())) {
                    embed.setTitle("❌ Oops");
                    embed.setDescription("You need to be in a voice channel with me for the **`skip`** command to work.");
                    embed.setColor(0xdd2e44);
                    event.replyEmbeds(embed.build()).queue();
                    embed.clear();
                    return;
                }

                embed.setTitle("Now Playing");
                embed.setDescription("[" + nowPlaying.getInfo().title + "](https://www.youtube.com/watch?v=" + nowPlaying + ")\n" +
                        "`[ " + "00:00" + " / " + formatTime(nowPlaying.getInfo().length) + " ]`");
                embed.setColor(0x0d8bbe);
                embed.setThumbnail("https://img.youtube.com/vi/" + nowPlaying.getIdentifier() + "/mqdefault.jpg");
                event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(pauseDisabled, skipDisabled, stopDisabled, clearDisabled), ActionRow.of(volumeDownDisabled, queueDisabled, loopDisabled, volumeUpDisabled)).queue();

                musicManager.trackScheduler.nextTrack();


                EmbedBuilder embedBuilder1 = new EmbedBuilder();
                embedBuilder1.setDescription("✅ " + event.getMember().getUser().getAsMention() + " **skipped** `" + nowPlaying.getInfo().title + "`");
                embedBuilder1.setColor(0x77b255);
                event.getChannel().sendMessageEmbeds(embedBuilder1.build()).queue();
                embed.clear();
                embedBuilder1.clear();
                return;

            case "stop":
                if (!BotVoiceState.inVoiceChannel()) {
                    embed.setTitle("❌ Oops");
                    embed.setDescription("I'm not in a voice channel for the **`stop`** Command to work.");
                    embed.setColor(0xdd2e44);
                    event.replyEmbeds(embed.build()).queue();
                    embed.clear();
                    return;
                } else if (!memberVoiceState.inVoiceChannel()) {
                    embed.setTitle("❌ Oops");
                    embed.setDescription("You need to be a voice channel for the **`pause`** Command to work.");
                    embed.setColor(0xdd2e44);
                    event.replyEmbeds(embed.build()).queue();
                    embed.clear();
                    return;
                }

                if (!memberVoiceState.getChannel().equals(BotVoiceState.getChannel())) {
                    embed.setTitle("❌ Oops");
                    embed.setDescription("You need to be in a voice channel with me for the **`stop`** command to work.");
                    embed.setColor(0xdd2e44);
                    event.replyEmbeds(embed.build()).queue();
                    embed.clear();
                    return;
                }

                embed.setTitle("Now Playing");
                embed.setDescription("[" + nowPlaying.getInfo().title + "](https://www.youtube.com/watch?v=" + nowPlaying + ")\n" +
                        "`[ " + "00:00" + " / " + formatTime(nowPlaying.getInfo().length) + " ]`");
                embed.setColor(0x0d8bbe);
                embed.setThumbnail("https://img.youtube.com/vi/" + nowPlaying.getIdentifier() + "/mqdefault.jpg");
                event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(pauseDisabled, skipDisabled, stopDisabled, clearDisabled), ActionRow.of(volumeDownDisabled, queueDisabled, loopDisabled, volumeUpDisabled)).queue();

                musicManager.trackScheduler.player.stopTrack();
                musicManager.trackScheduler.queue.clear();
                musicManager.trackScheduler.nowPlayingList.clear();

                AudioManager audioManager = event.getGuild().getAudioManager();
                audioManager.closeAudioConnection();
                musicManager.trackScheduler.lofi = false;

                musicManager.trackScheduler.repeating = false;
                musicManager.trackScheduler.trackArrayList.clear();

                EmbedBuilder embedBuilder2 = new EmbedBuilder();
                embedBuilder2.setTitle("✅  GoodBye!");
                embedBuilder2.setDescription("**Music stopped and I left the voice channel.** \n" +
                        "\nRequested by : " + event.getMember().getUser().getAsMention());
                embedBuilder2.setColor(0x77b255);
                event.getChannel().sendMessageEmbeds(embedBuilder2.build()).queue();
                embed.clear();
                embedBuilder2.clear();
                return;

            case "queue":
                ArrayList<AudioTrack> trackList = new ArrayList<>(musicManager.trackScheduler.queue);
                final AudioTrack currentTrack = musicManager.audioPlayer.getPlayingTrack();


                if (!musicManager.trackScheduler.lofi) {
                    boolean newRepeating = musicManager.trackScheduler.repeating;
                    if (newRepeating) {

                        for (int i = 0; i < musicManager.trackScheduler.trackArrayList.size(); i++) {
                            embed.setTitle("#️⃣  Looping Queue");
                            embed.setDescription("**Now Playing: **" + nowPlaying.getInfo().title + "   " + '`' + formatTime(nowPlaying.getPosition()) + '`' + " / " + '`' + formatTime(nowPlaying.getInfo().length) + '`');
                            embed.addField("", "**" + (i + 1) + ".** " + musicManager.trackScheduler.trackArrayList.get(i).getInfo().title + "   " + '`' + formatTime(musicManager.trackScheduler.trackArrayList.get(i).getInfo().length) + '`' + "\n", false);
                            embed.setColor(0x5dadec);
                        }
                        event.replyEmbeds(embed.build()).queue();
                        embed.clear();
                        return;
                    }

                    if (trackList.isEmpty()) {
                        if (currentTrack != null) {
                            embed.setTitle("#️⃣   Queue");
                            embed.setDescription("**Now Playing: **" + nowPlaying.getInfo().title + "   " + '`' + formatTime(nowPlaying.getPosition()) + '`' + " / " + '`' + formatTime(nowPlaying.getInfo().length) + '`');
                            embed.addField("", "**Queue is empty.**", false);
                            embed.setColor(0x5dadec);
                        } else {
                            embed.setDescription("**Queue is empty.**");
                            embed.setColor(0xdd2e44);
                        }
                        event.replyEmbeds(embed.build()).queue();
                        embed.clear();
                        return;
                    }


                    final int trackCount = Math.min(trackList.size(), 15);
                    for (int i = 0; i < trackCount; i++) {
                        AudioTrack track = trackList.get(i);
                        AudioTrackInfo info = track.getInfo();


                        embed.setTitle("🔁   Queue");
                        embed.setDescription("**Now Playing: **" + nowPlaying.getInfo().title + "`[ " + formatTime(nowPlaying.getPosition()) + '`' + " / " + '`' + formatTime(nowPlaying.getInfo().length) + " ]`");
                        embed.addField("", "**" + (i + 1) + ".** " + info.title + "   " + '`' + formatTime(track.getInfo().length) + '`' + "\n", false);
                        embed.setColor(0x5dadec);

                        if (trackList.size() > trackCount) {
                            embed.setFooter("and " + (trackList.size() - trackCount) + " more... ");
                        }

                    }
                } else {
                    embed.setTitle("Lofi mode on");
                    embed.setDescription("`Loop`, `Seek`,`Shuffle`, `Play`, `Remove`, `SkipTo`, `Queue`, `Clear` commands are disabled.");
                    embed.setColor(0xffbc12);
                }
                event.replyEmbeds(embed.build()).queue();
                embed.clear();
                return;

            case "loop":
                if (!BotVoiceState.inVoiceChannel()) {
                    embed.setTitle("❌ Oops");
                    embed.setDescription("I'm not in a voice channel for the **`loop`** Command to work.");
                    embed.setColor(0xdd2e44);
                    event.replyEmbeds(embed.build()).queue();
                    embed.clear();
                    return;
                } else if (!memberVoiceState.inVoiceChannel()) {
                    embed.setTitle("❌ Oops");
                    embed.setDescription("You need to be a voice channel for the **`pause`** Command to work.");
                    embed.setColor(0xdd2e44);
                    event.replyEmbeds(embed.build()).queue();
                    embed.clear();
                    return;
                }

                if (!memberVoiceState.getChannel().equals(BotVoiceState.getChannel())) {
                    embed.setTitle("❌ Oops");
                    embed.setDescription("You need to be in a voice channel with me for the **`loop`** command to work.");
                    embed.setColor(0xdd2e44);
                    event.replyEmbeds(embed.build()).queue();
                    embed.clear();
                    return;
                }

                if (!musicManager.trackScheduler.lofi) {
                    boolean newRepeating = !musicManager.trackScheduler.repeating;

                    musicManager.trackScheduler.repeating = newRepeating;
                    if (newRepeating) {
                        embed.setDescription("**✅  Current queue is now looping.**\n" +
                                "\nRequested by: " + event.getMember().getUser().getAsMention());
                        embed.setColor(0x77b255);
                    } else {
                        embed.setDescription("**❌  Current queue stopped looping.**\n" +
                                "\nRequested by: " + event.getMember().getUser().getAsMention());
                        embed.setColor(0xdd2e44);
                    }
                } else {
                    embed.setTitle("Lofi mode on");
                    embed.setDescription("`Loop`, `Seek`,`Shuffle`, `Play`, `Remove`, `SkipTo`, `Queue`, `Clear` commands are disabled.");
                    embed.setColor(0xffbc12);
                }
                event.replyEmbeds(embed.build()).queue();
                embed.clear();
                return;

            case "clear":
                if (!musicManager.trackScheduler.lofi) {
                    if (!musicManager.trackScheduler.repeating) {
                        ArrayList<AudioTrack> trackList1 = new ArrayList<>(musicManager.trackScheduler.queue);
                        if (trackList1.isEmpty()) {
                            embed.setDescription("**Queue is empty.**");
                            event.replyEmbeds(embed.build()).queue();
                            embed.clear();
                            return;
                        }

                        musicManager.trackScheduler.queue.clear();
                    } else {
                        List<AudioTrack> trackList2 = musicManager.trackScheduler.trackArrayList;
                        if (trackList2.isEmpty()) {
                            embed.setDescription("**Queue is empty.**");
                            event.replyEmbeds(embed.build()).queue();
                            embed.clear();
                            return;
                        }

                        musicManager.trackScheduler.trackArrayList.clear();

                    }
                    embed.setDescription("✅  ** Queue cleared.**");
                    embed.setColor(0x77b255);

                } else {
                    embed.setTitle("Lofi mode on");
                    embed.setDescription("`Loop`, `Seek`,`Shuffle`, `Play`, `Remove`, `SkipTo`, `Queue`, `Clear` commands are disabled.");
                    embed.setColor(0xffbc12);
                }
                event.replyEmbeds(embed.build()).queue();
                embed.clear();
                return;

            case "crashStop":

                if (event.getMessage().getMentionedUsers().get(0).getName().equals(event.getInteraction().getUser().getName())) {
                    GameCrash gameCrash = musicManager.trackScheduler.crashMap.get(event.getUser());

                    embed.setAuthor(event.getUser().getName() + "'s Crash game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());
                    embed.setColor(0x77b255);
                    embed.setDescription("You won `" + (int) (gameCrash.betAmount * (gameCrash.multiplierStart - 0.1)) + "` Coins.\n" +
                            "**Current Multiplier**\n" +
                            df.format(gameCrash.multiplierStart - 0.1));

                    int newCoins = previousAmount(event) +
                            (int) (gameCrash.betAmount * (gameCrash.multiplierStart - 0.1));
                    update(event, newCoins);

                    musicManager.trackScheduler.crashMap.get(event.getUser()).gameEnd = true;
                    event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(crashStopDisabled)).queue();
                    embed.clear();
                } else {
                    event.reply("Bruh, go play your own game " + event.getInteraction().getUser().getAsMention()).setEphemeral(true).queue();
                }
                return;


        }

    }

    private String formatTime(long timeInMillis) {
        final long minutes = timeInMillis / TimeUnit.MINUTES.toMillis(1);
        final long seconds = timeInMillis % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);

        return String.format("%02d:%02d", minutes, seconds);
    }

    private void update(ButtonClickEvent ctx, int coins) {
        String sql = "UPDATE CASINO_DATABASE SET Coins = ? WHERE User_ID =?";

        try (Connection conn = SQLiteDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, coins);
            pstmt.setString(2, ctx.getUser().getId());
            // update
            pstmt.executeUpdate();


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private int previousAmount(ButtonClickEvent ctx) {
        try (Connection con = SQLiteDataSource.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT Coins FROM CASINO_DATABASE WHERE User_ID =?")) {


            ps.setString(1, ctx.getUser().getId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                return rs.getInt("Coins");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;

    }

}
