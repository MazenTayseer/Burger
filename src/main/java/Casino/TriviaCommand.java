package Casino;

import CasinoMaps.GameTrivia;
import Commands.CommandContext;
import Commands.ICommand;
import Database.SQLiteDataSource;
import Music.GuildMusicManager;
import Music.PlayerManager;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("DuplicatedCode")
public class TriviaCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();
    private final EmbedBuilder errorEmbed = new EmbedBuilder();
    private final EmbedBuilder embedBuilder = new EmbedBuilder();
    private final EventWaiter waiter;
    private Button answer1Disabled = Button.secondary("answer1Disabled", "A").asDisabled();
    private Button answer2Disabled = Button.secondary("answer2Disabled", "B").asDisabled();
    private Button answer3Disabled = Button.secondary("answer3Disabled", "C").asDisabled();
    private Button answer4Disabled = Button.secondary("answer4Disabled", "D").asDisabled();

    public TriviaCommand(EventWaiter waiter) {
        this.waiter = waiter;
    }

    @Override
    public void handle(CommandContext ctx) {
        final GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(ctx.getGuild());
        TextChannel channel = ctx.getChannel();

        String[] args = ctx.getMessage().getContentRaw().split(" ");

        if(args.length != 2){
            errorEmbed.setTitle("❌ Oops");
            errorEmbed.setDescription("Usage:  **`-trivia`** `Number of coins`");
            errorEmbed.setColor(0xdd2e44);
            ctx.getChannel().sendMessageEmbeds(errorEmbed.build()).queue();
            errorEmbed.clear();
            return;
        }


        if(!isNumber(args[1])){
            errorEmbed.setTitle("❌ Oops");
            errorEmbed.setDescription("Usage:  **`-trivia`** `Number of coins`");
            errorEmbed.setColor(0xdd2e44);
            ctx.getChannel().sendMessageEmbeds(errorEmbed.build()).queue();
            errorEmbed.clear();
            return;
        }

        if(getUser(ctx)) {

            if(!musicManager.trackScheduler.triviaMap.containsKey(ctx.getAuthor())) {


                if (Integer.parseInt(args[1]) <= 0) {
                    errorEmbed.setTitle("❌ Oops");
                    errorEmbed.setDescription("You cant bet `" + Integer.parseInt(args[1]) + "`, You fool.");
                    errorEmbed.setColor(0xdd2e44);
                    ctx.getChannel().sendMessageEmbeds(errorEmbed.build()).queue();
                    errorEmbed.clear();
                    return;
                }

                if (getCoins(ctx) >= Integer.parseInt(args[1])) {

                    if (Integer.parseInt(args[1]) > 10000) {
                        errorEmbed.setTitle("❌ Oops");
                        errorEmbed.setDescription("You cant bet more than `10000`.");
                        errorEmbed.setColor(0xdd2e44);
                        ctx.getChannel().sendMessageEmbeds(errorEmbed.build()).queue();
                        errorEmbed.clear();
                        return;
                    }

                    WebUtils.ins.getJSONObject("https://opentdb.com/api.php?amount=1&type=multiple").async((json) -> {

//            https://opentdb.com/api.php?amount=1&category=9&type=multiple - General Knowledge.

                    String data = json.get("results").toString();
                    data = data.replace("&#039;", "`");
                    data = data.replace("&quot;", "`");
                    String substring = data.substring(2, data.length() - 2);



                    String[] split = substring.split(",");
                    String str = split[2].substring(14, split[2].length() - 1);
                    String difficulty = str.substring(0, 1).toUpperCase() + str.substring(1);

                    int questionIndex = data.indexOf("question");
                    int correct_answerIndex = data.indexOf("correct_answer");

                    int incorrect_answersIndex = data.indexOf("incorrect_answers");

                    String question = substring.substring(questionIndex + 9, correct_answerIndex - 5);
                    String correctAnswer = substring.substring(correct_answerIndex + 15, incorrect_answersIndex - 5);
                    String[] incorrectAnswers = substring.substring(incorrect_answersIndex + 18, data.length() - 5).split("\",");

                    String incorrect1 = incorrectAnswers[0].substring(1);
                    String incorrect2 = incorrectAnswers[1].substring(1);
                    String incorrect3 = incorrectAnswers[2].substring(1, incorrectAnswers[2].length() - 1);

                    embed.setAuthor(ctx.getAuthor().getName() + "'s Trivia game", ctx.getAuthor().getAvatarUrl(), ctx.getAuthor().getAvatarUrl());
                    embed.setColor(0x5865f2);
                    embed.setDescription("**" + question + "**\n" +
                            "You have 20 seconds to answer.");

                    List<String> answers = new ArrayList<>();
                    answers.add(correctAnswer);
                    answers.add(incorrect1);
                    answers.add(incorrect2);
                    answers.add(incorrect3);
                    Collections.shuffle(answers);

                    embed.addField("Answers", "A - `" + answers.get(0) + "`\n" +
                            "B - `" + answers.get(1) + "`\n" +
                            "C - `" + answers.get(2) + "`\n" +
                            "D - `" + answers.get(3) + "`", false);


                    Button answer1 = Button.primary(answers.get(0), "A");
                    Button answer2 = Button.primary(answers.get(1), "B");
                    Button answer3 = Button.primary(answers.get(2), "C");
                    Button answer4 = Button.primary(answers.get(3), "D");


                    GameTrivia gameTrivia = new GameTrivia(Integer.parseInt(args[1]), question, answers, correctAnswer);
                    musicManager.trackScheduler.triviaMap.put(ctx.getAuthor(), gameTrivia);

                    embedBuilder.setAuthor(ctx.getAuthor().getName() + "'s Trivia game", ctx.getAuthor().getAvatarUrl(), ctx.getAuthor().getAvatarUrl());
                    embedBuilder.setColor(0xdd2e44);
                    embedBuilder.setDescription("**" + question + "**\n" +
                                    "Time ran out!\n" +
                                    "Deducted `" + Integer.parseInt(args[1]) + "` Coins from your balance.\n" +
                                    "The correct answer was `" + correctAnswer + "`");


                    embedBuilder.addField("Answers", "A - `" + answers.get(0) + "`\n" +
                            "B - `" + answers.get(1) + "`\n" +
                            "C - `" + answers.get(2) + "`\n" +
                            "D - `" + answers.get(3) + "`", false);

//                    Button voteMe = Button.link("https://top.gg/bot/991196140626247702", "⬆️Vote me");
//
//                    System.out.println(correctAnswer);

                    embedBuilder.setFooter("Difficulty - " + difficulty);


                    channel.sendMessage(ctx.getAuthor().getAsMention()).setEmbeds(embed.build())
                            .setActionRows(ActionRow.of(answer1, answer2, answer3, answer4)).queue(
                                    message -> WaitForButtonClick(ctx, args, musicManager, message)
                            );



                    embed.clear();

                    });


                }
                else {
                    embed.setTitle("❌ Oops");
                    embed.setDescription("You don't have enough coins,\n Check your balance using `-bal` Command.");
                    embed.setColor(0xdd2e44);
                    ctx.getChannel().sendMessageEmbeds(embed.build()).queue();
                    embed.clear();
                }

            }

        }



    }

    @Override
    public String getName() {
        return "trivia";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Trivia");
        embed.setDescription("One question is asked, you have 4 answers to choose from A, B, C, D.");
        embed.addField("Usage","**`-trivia`**",true);
        embed.setColor(0xffffff);
        return embed;
    }

    private void update(CommandContext ctx ,int coins) {
        String sql = "UPDATE CASINO_DATABASE SET Coins = ? WHERE User_ID =?";

        try (Connection conn = SQLiteDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, coins);
            pstmt.setString(2, ctx.getAuthor().getId());
            // update
            pstmt.executeUpdate();



        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private int previousAmount(CommandContext ctx) {
        try (Connection con = SQLiteDataSource.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT Coins FROM CASINO_DATABASE WHERE User_ID =?")) {


            ps.setString(1, ctx.getAuthor().getId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                return rs.getInt("Coins");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;

    }

    private boolean isNumber(String message){
        try{
            Integer.parseInt(message);
            return true;
        } catch (NumberFormatException exception){
            return false;
        }
    }

    private int getCoins(CommandContext ctx) {
        String sql = "SELECT Coins FROM CASINO_DATABASE WHERE User_ID = ?";
        try (Connection connection = SQLiteDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {


            preparedStatement.setString(1, ctx.getAuthor().getId());
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                return rs.getInt("Coins");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private void update(ButtonClickEvent event ,int coins) {
        String sql = "UPDATE CASINO_DATABASE SET Coins = ? WHERE User_ID =?";

        try (Connection conn = SQLiteDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, coins);
            pstmt.setString(2, event.getUser().getId());
            // update
            pstmt.executeUpdate();



        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private int previousAmount(ButtonClickEvent event) {
        try (Connection con = SQLiteDataSource.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT Coins FROM CASINO_DATABASE WHERE User_ID =?")) {


            ps.setString(1, event.getUser().getId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                return rs.getInt("Coins");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;

    }


    private boolean getUser(CommandContext ctx) {

        try (Connection con = SQLiteDataSource.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT User_ID FROM CASINO_DATABASE WHERE User_ID =?")) {


            ps.setString(1, ctx.getAuthor().getId());
            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                return true;
            }
            else{
                embed.setTitle("❌ Oops");
                embed.setDescription("You are not registered in the casino,\n Please register first by typing `-register`.");
                embed.setColor(0xdd2e44);
                ctx.getChannel().sendMessageEmbeds(embed.build()).queue();
                embed.clear();
                return false;
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void buttonDealing(ButtonClickEvent event, GuildMusicManager musicManager) {

        if(event.getButton().getLabel().equals("A")) {

            if(event.getMessage().getMentionedUsers().get(0).getName().equals(event.getInteraction().getUser().getName())) {

                if(event.getButton().getId().equals(musicManager.trackScheduler.triviaMap.get(event.getUser()).correctAnswer)) {
                    answer1Disabled = Button.success("answer1Disabled","A").asDisabled();
                    embed.setAuthor(event.getUser().getName() + "'s Trivia game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());
                    embed.setColor(0x77b255);

                    embed.setDescription("**" + musicManager.trackScheduler.triviaMap.get(event.getUser()).question + "**\n"+
                            "Correct answer!\n" +
                            "You won `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).betAmount*2 + "` Coins.");


                    embed.addField("Answers", "A - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(0) + "`\n" +
                            "B - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(1) + "`\n" +
                            "C - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(2) + "`\n" +
                            "D - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(3) + "`",false);

                    int newCoins = previousAmount(event) + (musicManager.trackScheduler.triviaMap.get(event.getUser()).betAmount*2);
                    update(event, newCoins);

                }
                else{
                    answer1Disabled = Button.danger("answer1Disabled","A").asDisabled();
                    embed.setAuthor(event.getUser().getName() + "'s Trivia game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());
                    embed.setColor(0xdd2e44);
                    embed.setAuthor(event.getUser().getName() + "'s Trivia game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());

                    embed.setDescription("**" + musicManager.trackScheduler.triviaMap.get(event.getUser()).question + "**\n"+
                            "Wrong answer.\n" +
                            "You lost `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).betAmount + "` Coins.\n" +
                            "`" + musicManager.trackScheduler.triviaMap.get(event.getUser()).correctAnswer + "` was the correct answer.");


                    embed.addField("Answers", "A - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(0) + "`\n" +
                            "B - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(1) + "`\n" +
                            "C - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(2) + "`\n" +
                            "D - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(3) + "`",false);

                    int newCoins = previousAmount(event) - musicManager.trackScheduler.triviaMap.get(event.getUser()).betAmount;

                    if(newCoins < 0) {
                        newCoins = 0;
                    }

                    update(event, newCoins);

                }
                event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(answer1Disabled, answer2Disabled, answer3Disabled, answer4Disabled)).queue();
                embed.clear();
                normalDisabledButtons();
                musicManager.trackScheduler.triviaMap.remove(event.getUser());
            }
            else{
                event.reply("Bruh, go play your own game " + event.getInteraction().getUser().getAsMention()).setEphemeral(true).queue();
            }

        }

        if(event.getButton().getLabel().equals("B")) {

            if(event.getMessage().getMentionedUsers().get(0).getName().equals(event.getInteraction().getUser().getName())) {

                if(event.getButton().getId().equals(musicManager.trackScheduler.triviaMap.get(event.getUser()).correctAnswer)) {
                    answer2Disabled = Button.success("answer2Disabled","B").asDisabled();
                    embed.setAuthor(event.getUser().getName() + "'s Trivia game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());
                    embed.setColor(0x77b255);

                    embed.setDescription("**" + musicManager.trackScheduler.triviaMap.get(event.getUser()).question + "**\n"+
                            "Correct answer!\n" +
                            "You won `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).betAmount*2 + "` Coins.");


                    embed.addField("Answers", "A - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(0) + "`\n" +
                            "B - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(1) + "`\n" +
                            "C - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(2) + "`\n" +
                            "D - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(3) + "`",false);

                    int newCoins = previousAmount(event) + (musicManager.trackScheduler.triviaMap.get(event.getUser()).betAmount*2);
                    update(event, newCoins);
                }
                else{
                    answer2Disabled = Button.danger("answer2Disabled","B").asDisabled();
                    embed.setAuthor(event.getUser().getName() + "'s Trivia game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());
                    embed.setColor(0xdd2e44);
                    embed.setAuthor(event.getUser().getName() + "'s Trivia game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());

                    embed.setDescription("**" + musicManager.trackScheduler.triviaMap.get(event.getUser()).question + "**\n"+
                            "Wrong answer.\n" +
                            "You lost `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).betAmount + "` Coins.\n" +
                            "`" + musicManager.trackScheduler.triviaMap.get(event.getUser()).correctAnswer + "` was the correct answer.");


                    embed.addField("Answers", "A - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(0) + "`\n" +
                            "B - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(1) + "`\n" +
                            "C - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(2) + "`\n" +
                            "D - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(3) + "`",false);

                    int newCoins = previousAmount(event) - musicManager.trackScheduler.triviaMap.get(event.getUser()).betAmount;

                    if(newCoins < 0) {
                        newCoins = 0;
                    }

                    update(event, newCoins);

                }
                event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(answer1Disabled, answer2Disabled, answer3Disabled, answer4Disabled)).queue();
                embed.clear();
                normalDisabledButtons();
                musicManager.trackScheduler.triviaMap.remove(event.getUser());
            }
            else{
                event.reply("Bruh, go play your own game " + event.getInteraction().getUser().getAsMention()).setEphemeral(true).queue();
            }

        }

        if(event.getButton().getLabel().equals("C")) {

            if(event.getMessage().getMentionedUsers().get(0).getName().equals(event.getInteraction().getUser().getName())) {

                if(event.getButton().getId().equals(musicManager.trackScheduler.triviaMap.get(event.getUser()).correctAnswer)) {
                    answer3Disabled = Button.success("answer3Disabled","C").asDisabled();
                    embed.setAuthor(event.getUser().getName() + "'s Trivia game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());
                    embed.setColor(0x77b255);

                    embed.setDescription("**" + musicManager.trackScheduler.triviaMap.get(event.getUser()).question + "**\n"+
                            "Correct answer!\n" +
                            "You won `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).betAmount*2 + "` Coins.");


                    embed.addField("Answers", "A - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(0) + "`\n" +
                            "B - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(1) + "`\n" +
                            "C - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(2) + "`\n" +
                            "D - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(3) + "`",false);

                    int newCoins = previousAmount(event) + (musicManager.trackScheduler.triviaMap.get(event.getUser()).betAmount*2);
                    update(event, newCoins);
                }
                else{
                    answer3Disabled = Button.danger("answer3Disabled","C").asDisabled();
                    embed.setAuthor(event.getUser().getName() + "'s Trivia game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());
                    embed.setColor(0xdd2e44);
                    embed.setAuthor(event.getUser().getName() + "'s Trivia game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());

                    embed.setDescription("**" + musicManager.trackScheduler.triviaMap.get(event.getUser()).question + "**\n"+
                            "Wrong answer.\n" +
                            "You lost `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).betAmount + "` Coins.\n" +
                            "`" + musicManager.trackScheduler.triviaMap.get(event.getUser()).correctAnswer + "` was the correct answer.");


                    embed.addField("Answers", "A - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(0) + "`\n" +
                            "B - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(1) + "`\n" +
                            "C - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(2) + "`\n" +
                            "D - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(3) + "`",false);

                    int newCoins = previousAmount(event) - musicManager.trackScheduler.triviaMap.get(event.getUser()).betAmount;

                    if(newCoins < 0) {
                        newCoins = 0;
                    }

                    update(event, newCoins);
                }
                event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(answer1Disabled, answer2Disabled, answer3Disabled, answer4Disabled)).queue();
                embed.clear();
                normalDisabledButtons();
                musicManager.trackScheduler.triviaMap.remove(event.getUser());
            }
            else{
                event.reply("Bruh, go play your own game " + event.getInteraction().getUser().getAsMention()).setEphemeral(true).queue();
            }

        }

        if(event.getButton().getLabel().equals("D")) {

            if(event.getMessage().getMentionedUsers().get(0).getName().equals(event.getInteraction().getUser().getName())) {

                if(event.getButton().getId().equals(musicManager.trackScheduler.triviaMap.get(event.getUser()).correctAnswer)) {
                    answer4Disabled = Button.success("answer4Disabled","D").asDisabled();
                    embed.setAuthor(event.getUser().getName() + "'s Trivia game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());
                    embed.setColor(0x77b255);

                    embed.setDescription("**" + musicManager.trackScheduler.triviaMap.get(event.getUser()).question + "**\n"+
                            "Correct answer!\n" +
                            "You won `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).betAmount*2 + "` Coins.");


                    embed.addField("Answers", "A - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(0) + "`\n" +
                            "B - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(1) + "`\n" +
                            "C - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(2) + "`\n" +
                            "D - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(3) + "`",false);

                    int newCoins = previousAmount(event) + (musicManager.trackScheduler.triviaMap.get(event.getUser()).betAmount*2);
                    update(event, newCoins);
                }
                else{
                    answer4Disabled = Button.danger("answer4Disabled","D").asDisabled();
                    embed.setAuthor(event.getUser().getName() + "'s Trivia game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());
                    embed.setColor(0xdd2e44);
                    embed.setAuthor(event.getUser().getName() + "'s Trivia game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());

                    embed.setDescription("**" + musicManager.trackScheduler.triviaMap.get(event.getUser()).question + "**\n"+
                            "Wrong answer.\n" +
                            "You lost `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).betAmount + "` Coins.\n" +
                            "`" + musicManager.trackScheduler.triviaMap.get(event.getUser()).correctAnswer + "` was the correct answer.");


                    embed.addField("Answers", "A - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(0) + "`\n" +
                            "B - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(1) + "`\n" +
                            "C - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(2) + "`\n" +
                            "D - `" + musicManager.trackScheduler.triviaMap.get(event.getUser()).answers.get(3) + "`",false);
                    int newCoins = previousAmount(event) - musicManager.trackScheduler.triviaMap.get(event.getUser()).betAmount;

                    if(newCoins < 0) {
                        newCoins = 0;
                    }

                    update(event, newCoins);
                }
                event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(answer1Disabled, answer2Disabled, answer3Disabled, answer4Disabled)).queue();
                embed.clear();
                normalDisabledButtons();
                musicManager.trackScheduler.triviaMap.remove(event.getUser());
            }
            else{
                event.reply("Bruh, go play your own game " + event.getInteraction().getUser().getAsMention()).setEphemeral(true).queue();
            }

        }

    }

    private void WaitForButtonClick(CommandContext ctx, String[] args, GuildMusicManager musicManager, Message message) {

        this.waiter.waitForEvent(
                ButtonClickEvent.class,
                e -> {
                    if (e.getMessageIdLong() != message.getIdLong()) {
                        return false;
                    }

                    if (!e.getMessage().getMentionedUsers().get(0).getName().equals(e.getInteraction().getUser().getName())) {
                        e.reply("Bruh, go play your own game " + e.getInteraction().getUser().getAsMention()).setEphemeral(true).queue();
                        return false;
                    }

                    return !e.isAcknowledged();
                },
                event -> {
                    buttonDealing(event, musicManager);
                },

                20, TimeUnit.SECONDS,
                () ->
                {
                    message.editMessageEmbeds(embedBuilder.build())
                            .setActionRows(ActionRow.of(answer1Disabled,answer2Disabled,answer3Disabled, answer4Disabled)).queue();

                    if(musicManager.trackScheduler.triviaMap.containsKey(ctx.getAuthor())) {
                        musicManager.trackScheduler.triviaMap.remove(ctx.getAuthor());


                        int newCoins = previousAmount(ctx) - Integer.parseInt(args[1]);

                        if(newCoins < 0) {
                            newCoins = 0;
                        }

                        update(ctx, newCoins);
                    }
                    embedBuilder.clear();
                }

        );

    }

    private void normalDisabledButtons () {
        answer1Disabled = Button.secondary("answer1Disabled", "A").asDisabled();
        answer2Disabled = Button.secondary("answer2Disabled", "B").asDisabled();
        answer3Disabled = Button.secondary("answer3Disabled", "C").asDisabled();
        answer4Disabled = Button.secondary("answer4Disabled", "D").asDisabled();
    }

}
