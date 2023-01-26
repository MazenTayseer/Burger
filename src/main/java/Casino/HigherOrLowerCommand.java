package Casino;

import CasinoMaps.GameHighLow;
import Commands.CommandContext;
import Commands.ICommand;
import Database.SQLiteDataSource;
import Music.GuildMusicManager;
import Music.PlayerManager;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("DuplicatedCode")
public class HigherOrLowerCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();
    private final EmbedBuilder errorEmbed = new EmbedBuilder();
    private final EmbedBuilder embedBuilder = new EmbedBuilder();
    private final Button higher = Button.primary("higher", "Higher");
    private final Button lower = Button.primary("lower", "Lower");
    private final Button ThatsIt = Button.primary("thatsit", "That's it!");

    private Button higherDisabled = Button.secondary("higher", "Higher").asDisabled();
    private Button lowerDisabled = Button.secondary("lower", "Lower").asDisabled();
    private Button ThatsItDisabled = Button.secondary("thatsit", "That's it!").asDisabled();

    private final EventWaiter waiter;

    public HigherOrLowerCommand(EventWaiter waiter) {
        this.waiter = waiter;
    }


    @Override
    public void handle(CommandContext ctx) {
        String[] args = ctx.getMessage().getContentRaw().split(" ");
        final GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(ctx.getGuild());

        if(args.length != 2){
            errorEmbed.setTitle("❌ Oops");
            errorEmbed.setDescription("Usage:  **`-highlow`** `Number of coins`");
            errorEmbed.setColor(0xdd2e44);
            ctx.getChannel().sendMessageEmbeds(errorEmbed.build()).queue();
            errorEmbed.clear();
            return;
        }


        if(!isNumber(args[1])){
            errorEmbed.setTitle("❌ Oops");
            errorEmbed.setDescription("Usage:  **`-highlow`** `Number of coins`");
            errorEmbed.setColor(0xdd2e44);
            ctx.getChannel().sendMessageEmbeds(errorEmbed.build()).queue();
            errorEmbed.clear();
            return;
        }


        if(getUser(ctx)) {

                if(!musicManager.trackScheduler.highLowGame.containsKey(ctx.getAuthor())) {

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

                        Random random = new Random();
                        int targetNumber = random.nextInt(100);
                        int firstNumber = random.nextInt(100);

                        GameHighLow highLowGame = new GameHighLow(Integer.parseInt(args[1]) ,targetNumber, firstNumber);
                        musicManager.trackScheduler.highLowGame.put(ctx.getAuthor(), highLowGame);

                        embed.setAuthor(ctx.getAuthor().getName() + "'s Higher or Lower game", ctx.getAuthor().getAvatarUrl(), ctx.getAuthor().getAvatarUrl());
                        embed.setColor(0x5865f2);
                        embed.setDescription("I picked a number between 1 to 100,\n" +
                                "Is the number higher or lower than `" + firstNumber + "` ?");

                        embed.setFooter("Press That's it if you think numbers are the same!\n" +
                                "You will win x10 your bet if you choose That's it, Think smart.");


                        embedBuilder.setTitle("Burger's Casino");
                        embedBuilder.setDescription("Hope you had fun playing Higher or Lower game!\n" +
                                "The number I picked was `" + targetNumber + "`");
                        embedBuilder.setColor(Color.white);
                        embedBuilder.setThumbnail(ctx.getSelfUser().getAvatarUrl());

                        Button voteMe = Button.link("https://top.gg/bot/991196140626247702", "⬆️Vote me");

                        ctx.getChannel().sendMessage(ctx.getAuthor().getAsMention()).setEmbeds(embed.build())
                                .setActionRows(ActionRow.of(higher, ThatsIt, lower))
                                .queue(message -> WaitForButtonClick(ctx,musicManager, message, args));

                        embed.clear();

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
        return "highlow";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Higher or Lower");
        embed.setDescription("I generate a random number between 1 and 100, then you have to guess from the given number wether the number that i guessed is higher, lower or if it is as the same number that i Guessed!");
        embed.addField("Usage","**`-highlow`**",true);
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

    private void buttonDealing(ButtonClickEvent event, GuildMusicManager musicManager) {


        if(event.getButton().getId().equalsIgnoreCase("higher")) {

            if(event.getMessage().getMentionedUsers().get(0).getName().equals(event.getInteraction().getUser().getName())) {
                GameHighLow highLowGame = musicManager.trackScheduler.highLowGame.get(event.getUser());
                higherDisabled = Button.primary("higher", "Higher").asDisabled();

                if(highLowGame.firstNumber >= highLowGame.targetNumber) {
                    embed.setAuthor(event.getUser().getName() + "'s Higher or Lower game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());
                    embed.setColor(0xdd2e44);
                    embed.setDescription("You lost `" + musicManager.trackScheduler.highLowGame.get(event.getUser()).betAmount + "` Coins.\n" +
                            "The number was `" + highLowGame.targetNumber + "`");

                    int newCoins = previousAmount(event) - musicManager.trackScheduler.highLowGame.get(event.getUser()).betAmount;

                    if(newCoins < 0) {
                        newCoins = 0;
                    }

                    update(event, newCoins);

                }
                else{
                    embed.setAuthor(event.getUser().getName() + "'s Higher or Lower game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());
                    embed.setColor(0x77b255);
                    embed.setDescription("You won `" + musicManager.trackScheduler.highLowGame.get(event.getUser()).betAmount/2 + "` Coins.\n" +
                            "The number was `" + highLowGame.targetNumber + "`");

                    int newCoins = previousAmount(event) + (musicManager.trackScheduler.highLowGame.get(event.getUser()).betAmount/2);
                    update(event, newCoins);
                }

                event.editMessageEmbeds(embed.build())
                        .setActionRows(ActionRow.of(higherDisabled, ThatsItDisabled, lowerDisabled)).queue();
                musicManager.trackScheduler.highLowGame.remove(event.getUser());
                embed.clear();

            }
            else{
                event.reply("Bruh, go play your own game " + event.getInteraction().getUser().getAsMention()).setEphemeral(true).queue();
            }

        }


        if(event.getButton().getId().equalsIgnoreCase("lower")) {

            if(event.getMessage().getMentionedUsers().get(0).getName().equals(event.getInteraction().getUser().getName())) {
                GameHighLow highLowGame = musicManager.trackScheduler.highLowGame.get(event.getUser());
                lowerDisabled = Button.primary("lower", "Lower").asDisabled();

                if(highLowGame.firstNumber <= highLowGame.targetNumber) {
                    embed.setDescription("You lost `" + musicManager.trackScheduler.highLowGame.get(event.getUser()).betAmount + "` Coins.\n" +
                            "The number was `" + highLowGame.targetNumber + "`");

                    int newCoins = previousAmount(event) - musicManager.trackScheduler.highLowGame.get(event.getUser()).betAmount;

                    if(newCoins < 0) {
                        newCoins = 0;
                    }

                    update(event, newCoins);

                }
                else{
                    embed.setAuthor(event.getUser().getName() + "'s Higher or Lower game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());
                    embed.setColor(0x77b255);
                    embed.setDescription("You won `" + musicManager.trackScheduler.highLowGame.get(event.getUser()).betAmount/2 + "` Coins.\n" +
                            "The number was `" + highLowGame.targetNumber + "`");
                    int newCoins = previousAmount(event) + (musicManager.trackScheduler.highLowGame.get(event.getUser()).betAmount/2);
                    update(event, newCoins);
                }

                event.editMessageEmbeds(embed.build())
                        .setActionRows(ActionRow.of(higherDisabled, ThatsItDisabled, lowerDisabled)).queue();
                musicManager.trackScheduler.highLowGame.remove(event.getUser());
                embed.clear();

            }
            else{
                event.reply("Bruh, go play your own game " + event.getInteraction().getUser().getAsMention()).setEphemeral(true).queue();
            }

        }

        if(event.getButton().getId().equalsIgnoreCase("thatsit")) {

            if(event.getMessage().getMentionedUsers().get(0).getName().equals(event.getInteraction().getUser().getName())) {
                GameHighLow highLowGame = musicManager.trackScheduler.highLowGame.get(event.getUser());
                ThatsItDisabled = Button.primary("thatsit", "That's it!").asDisabled();

                if(highLowGame.firstNumber == highLowGame.targetNumber) {
                    embed.setAuthor(event.getUser().getName() + "'s Higher or Lower game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());
                    embed.setColor(0x77b255);
                    embed.setDescription("You won `" + musicManager.trackScheduler.highLowGame.get(event.getUser()).betAmount*10 + "` Coins.\n" +
                            "The number was `" + highLowGame.targetNumber + "`");

                    int newCoins = previousAmount(event) + (musicManager.trackScheduler.highLowGame.get(event.getUser()).betAmount*10);
                    update(event, newCoins);

                }
                else{
                    embed.setAuthor(event.getUser().getName() + "'s Higher or Lower game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());
                    embed.setColor(0xdd2e44);
                    embed.setDescription("You lost `" + musicManager.trackScheduler.highLowGame.get(event.getUser()).betAmount + "` Coins.\n" +
                            "The number was `" + highLowGame.targetNumber + "`");

                    int newCoins = previousAmount(event) - musicManager.trackScheduler.highLowGame.get(event.getUser()).betAmount;

                    if(newCoins < 0) {
                        newCoins = 0;
                    }

                    update(event, newCoins);

                }

                event.editMessageEmbeds(embed.build())
                        .setActionRows(ActionRow.of(higherDisabled, ThatsItDisabled, lowerDisabled)).queue();
                musicManager.trackScheduler.highLowGame.remove(event.getUser());
                embed.clear();

            }
            else{
                event.reply("Bruh, go play your own game " + event.getInteraction().getUser().getAsMention()).setEphemeral(true).queue();
            }

        }
    }

    private void WaitForButtonClick (CommandContext ctx , GuildMusicManager musicManager, Message message, String[] args) {
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
                            .setActionRows().queue();

                    if(musicManager.trackScheduler.highLowGame.containsKey(ctx.getAuthor())) {
                        musicManager.trackScheduler.highLowGame.remove(ctx.getAuthor());


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

    private void update(ButtonClickEvent ctx ,int coins) {
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
}



