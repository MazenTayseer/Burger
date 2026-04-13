package Casino;

import CasinoMaps.GameHorse;
import Commands.CommandContext;
import Commands.ICommand;
import Database.SQLiteDataSource;
import Music.GuildMusicManager;
import Music.PlayerManager;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"DuplicatedCode", "RedundantCollectionOperation"})
public class HorsesCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();
    private final EmbedBuilder errorEmbed = new EmbedBuilder();
    private final EmbedBuilder embedBuilder = new EmbedBuilder();
    private final Map<Double, String> horseChance= new HashMap<>();
    private final List<String> currentHorses = new ArrayList<>();
    private final List<Double> availableBets = new ArrayList<>();
    private final List<String> availableBetsString = new ArrayList<>();

    //Top tier, Middle tier, Bottom tier
    private final double[] topTier = {0.5, 0.3333333333333333, 0.25, 0.2};
    private final double[] middleTier = {0.16666666666666666, 0.14285714285714285, 0.125, 0.1111111111111111, 0.1, 0.09090909090909091, 0.08333333333333333, 0.07692307692307693, 0.07142857142857142, 0.06666666666666667};
    private final double[] bottomTier = {0.0625, 0.058823529411764705, 0.05555555555555555, 0.05263157894736842, 0.05, 0.047619047619047616, 0.045454545454545456, 0.043478260869565216, 0.041666666666666664, 0.04, 0.038461538461538464, 0.037037037037037035, 0.03571428571428571, 0.034482758620689655, 0.03333333333333333};
    private final String[] horses = {"WAFFLE FRIES", "DOUBLE-DOUBLE", "CHICKEN SANDWICH", "BLIZZARD", "FROSTY", "MCFLURRY",
                                     "CHICKEN NUGGETS", "PEPPERONI PIZZA", "BIG MAC", "CHEESEBURGER", "PEPSI", "COCA-COLA",
                                     "GLAZED DOUGHNUT", "FILET-O-FISH", "WHOPPER", "FRIED CHICKEN", "QUARTER POUNDER",
                                     "PRETZEL", "TACO", "BURRITO", "BISCUITS", "APPLE PIE", "STEAK BURGER", "MCMUFFIN",
                                     "COOKIES", "CURLY FRIES", "BIG TASTY", "MASHED POTATOES"};

    private String topTierString;
    private String middleTierString;
    private String bottomTierString;
    private final Button horse1 = Button.primary("horse1","1");
    private final Button horse2 = Button.primary("horse2","2");
    private final Button horse3 = Button.primary("horse3","3");
    private final Button horse4 = Button.primary("horse4","4");
    private final Button horse5 = Button.primary("horse5","5");
    private final Button horse6 = Button.primary("horse6","6");
    private final Button Forfeit = Button.danger("forfeitHorse","Forfeit");

    private Button horse1Disabled = Button.secondary("horse1","1").asDisabled();
    private Button horse2Disabled = Button.secondary("horse2","2").asDisabled();
    private Button horse3Disabled = Button.secondary("horse3","3").asDisabled();
    private Button horse4Disabled = Button.secondary("horse4","4").asDisabled();
    private Button horse5Disabled = Button.secondary("horse5","5").asDisabled();
    private Button horse6Disabled = Button.secondary("horse6","6").asDisabled();
    private final Button horseForfeitDisabled = Button.danger("forfeitHorse","Forfeit").asDisabled();

    private final EventWaiter waiter;

    public HorsesCommand(EventWaiter waiter) {
        this.waiter = waiter;
    }


    @Override
    public void handle(CommandContext ctx) {
        String[] args = ctx.getMessage().getContentRaw().split(" ");
        final GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(ctx.getGuild());
        Random random = new Random();

        if(args.length != 2){
            errorEmbed.setTitle("❌ Oops");
            errorEmbed.setDescription("Usage:  **`-hr`** `Number of coins`");
            errorEmbed.setColor(0xdd2e44);
            ctx.getChannel().sendMessageEmbeds(errorEmbed.build()).queue();
            errorEmbed.clear();
            return;
        }


        if(!isNumber(args[1])){
            errorEmbed.setTitle("❌ Oops");
            errorEmbed.setDescription("Usage:  **`-hr`** `Number of coins`");
            errorEmbed.setColor(0xdd2e44);
            ctx.getChannel().sendMessageEmbeds(errorEmbed.build()).queue();
            errorEmbed.clear();
            return;
        }

        TextChannel channel = ctx.getChannel();

        if(getUser(ctx)) {

            User user = ctx.getAuthor();
            if(!musicManager.trackScheduler.winningHorseDouble.containsKey(user)) {

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


                    while (currentHorses.size() != 6) {
                        String horse = horses[random.nextInt(horses.length)];

                        if (!currentHorses.contains(horse)) {
                            currentHorses.add(horse);
                        }
                    }


                    while (availableBets.size() != 2) {
                        double topTierNumber = topTier[random.nextInt(topTier.length)];

                        if (!availableBets.contains(topTierNumber)) {
                            availableBets.add(topTierNumber);
                        }

                    }

                    while (availableBets.size() != 4) {
                        double middleTierNumber = middleTier[random.nextInt(middleTier.length)];

                        if (!availableBets.contains(middleTierNumber)) {
                            availableBets.add(middleTierNumber);
                        }

                    }

                    while (availableBets.size() != 6) {
                        double bottomTierNumber = bottomTier[random.nextInt(bottomTier.length)];

                        if (!availableBets.contains(bottomTierNumber)) {
                            availableBets.add(bottomTierNumber);
                        }

                    }

                    Collections.sort(availableBets);


                    switchTopTier(availableBets.get(5));
                    availableBetsString.add(topTierString);
                    switchTopTier(availableBets.get(4));
                    availableBetsString.add(topTierString);

                    switchMiddleTier(availableBets.get(3));
                    availableBetsString.add(middleTierString);
                    switchMiddleTier(availableBets.get(2));
                    availableBetsString.add(middleTierString);

                    switchBottomTier(availableBets.get(1));
                    availableBetsString.add(bottomTierString);
                    switchBottomTier(availableBets.get(0));
                    availableBetsString.add(bottomTierString);


                    horseChance.put(availableBets.get(5), "1");
                    horseChance.put(availableBets.get(4), "2");
                    horseChance.put(availableBets.get(3), "3");
                    horseChance.put(availableBets.get(2), "4");
                    horseChance.put(availableBets.get(1), "5");
                    horseChance.put(availableBets.get(0), "6");


                    double winningNumber = random.nextDouble(/*1.0*/);
                    embed.setAuthor(ctx.getAuthor().getName() + "'s Horse Racing game", ctx.getAuthor().getAvatarUrl(), ctx.getAuthor().getAvatarUrl());
                    embed.setColor(0x5865f2);
                    embed.setDescription("Choose the horse number you want to bet on.");
                    embed.addField("Horse Names", "`1 - " + currentHorses.get(0) + "`\n" +
                            "`2 - " + currentHorses.get(1) + "`\n" +
                            "`3 - " + currentHorses.get(2) + "`\n" +
                            "`4 - " + currentHorses.get(3) + "`\n" +
                            "`5 - " + currentHorses.get(4) + "`\n" +
                            "`6 - " + currentHorses.get(5) + "`", true);

                    embed.addBlankField(true);


                    String[] bet1 = availableBetsString.get(0).split(" ");
                    String[] bet2 = availableBetsString.get(1).split(" ");
                    String[] bet3 = availableBetsString.get(2).split(" ");
                    String[] bet4 = availableBetsString.get(3).split(" ");
                    String[] bet5 = availableBetsString.get(4).split(" ");
                    String[] bet6 = availableBetsString.get(5).split(" ");

                    embed.addField("Chances of winning", "`" + availableBetsString.get(0) + " - Win = " + (Integer.parseInt(args[1]) + (Integer.parseInt(args[1])*Integer.parseInt(bet1[0]))) + "`\n" +
                            "`" + availableBetsString.get(1) + " - Win = " + (Integer.parseInt(args[1]) + (Integer.parseInt(args[1])*Integer.parseInt(bet2[0]))) + "`\n" +
                            "`" + availableBetsString.get(2) + " - Win = " + (Integer.parseInt(args[1]) + (Integer.parseInt(args[1])*Integer.parseInt(bet3[0]))) + "`\n" +
                            "`" + availableBetsString.get(3) + " - Win = " + (Integer.parseInt(args[1]) + (Integer.parseInt(args[1])*Integer.parseInt(bet4[0]))) + "`\n" +
                            "`" + availableBetsString.get(4) + " - Win = " + (Integer.parseInt(args[1]) + (Integer.parseInt(args[1])*Integer.parseInt(bet5[0]))) + "`\n" +
                            "`" + availableBetsString.get(5) + " - Win = " + (Integer.parseInt(args[1]) + (Integer.parseInt(args[1])*Integer.parseInt(bet6[0]))) + "`" , true);



                    Double finalNumber = findClosest(availableBets, winningNumber);
//                    System.out.println(horseChance.get(finalNumber));


                    musicManager.trackScheduler.winningHorseDouble.put(ctx.getAuthor(), horseChance.get(finalNumber));

                    GameHorse gameHorse = new GameHorse(Integer.parseInt(args[1]), finalNumber);
                    musicManager.trackScheduler.winningHorseString.put(ctx.getAuthor(), gameHorse);

                    embedBuilder.setTitle("Burger's Casino");
                    embedBuilder.setDescription("You waited for too long, So I took your money!\n" +
                            "Deducted `" + Integer.parseInt(args[1]) + "` Coins from your balance");
                    embedBuilder.setColor(0xdd2e44);
                    embedBuilder.setThumbnail(ctx.getSelfUser().getAvatarUrl());

                    Button voteMe = Button.link("https://top.gg/bot/991196140626247702", "⬆️Vote me");

                    channel.sendMessage(ctx.getAuthor().getAsMention()).setEmbeds(embed.build())
                            .setActionRows(ActionRow.of(horse1, horse2, horse3), ActionRow.of(horse4, horse5, horse6, Forfeit))
                    .queue(message -> waiter.waitForEvent(
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

                            60, TimeUnit.SECONDS,
                            () ->
                            {
                                message.editMessageEmbeds(embedBuilder.build())
                                        .setActionRows(ActionRow.of(horse1Disabled, horse2Disabled, horse3Disabled)
                                        ,ActionRow.of(horse4Disabled, horse5Disabled, horse6Disabled, horseForfeitDisabled)).queue();

                                if(musicManager.trackScheduler.winningHorseDouble.containsKey(ctx.getAuthor())) {
                                    musicManager.trackScheduler.winningHorseDouble.remove(ctx.getAuthor());


                                    int newCoins = previousAmount(ctx) - Integer.parseInt(args[1]);

                                    if(newCoins < 0) {
                                        newCoins = 0;
                                    }

                                    update(ctx, newCoins);
                                }
                                embedBuilder.clear();
                            }
                    ));

                    embed.clear();
                    currentHorses.clear();
                    availableBets.clear();
                    availableBetsString.clear();
                    horseChance.clear();
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
        return "hr";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Horse Racing");
        embed.setDescription("6 Horses will race, each one has a percentage to win, you have to bet on one, the lower the winning chance, the higher you win back.");
        embed.addField("Usage","**`-hr`**",true);
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


    private void switchTopTier(double t) {
        String s = String.valueOf(t);


        switch (s) {
            case "0.5" -> topTierString = "2 to 1";
            case "0.3333333333333333" -> topTierString = "3 to 1";
            case "0.25" -> topTierString = "4 to 1";
            case "0.2" -> topTierString = "5 to 1";
        }
    }

    private void switchMiddleTier(double t) {
        String s = String.valueOf(t);

        switch (s) {
            case "0.16666666666666666" -> middleTierString = "6 to 1";
            case "0.14285714285714285" -> middleTierString = "7 to 1";
            case "0.125" -> middleTierString = "8 to 1";
            case "0.1111111111111111" -> middleTierString = "9 to 1";

            case "0.1" -> middleTierString = "10 to 1";
            case "0.09090909090909091" -> middleTierString = "11 to 1";
            case "0.08333333333333333" -> middleTierString = "12 to 1";
            case "0.07692307692307693" -> middleTierString = "13 to 1";

            case "0.07142857142857142" -> middleTierString = "14 to 1";
            case "0.06666666666666667" -> middleTierString = "15 to 1";
        }
    }


    private void switchBottomTier(double t) {
        String s = String.valueOf(t);

        switch (s) {
            case "0.0625" -> bottomTierString = "16 to 1";
            case "0.058823529411764705" -> bottomTierString = "17 to 1";
            case "0.05555555555555555" -> bottomTierString = "18 to 1";
            case "0.05263157894736842" -> bottomTierString = "19 to 1";

            case "0.05" -> bottomTierString = "20 to 1";
            case "0.047619047619047616" -> bottomTierString = "21 to 1";
            case "0.045454545454545456" -> bottomTierString = "22 to 1";
            case "0.043478260869565216" -> bottomTierString = "23 to 1";

            case "0.041666666666666664" -> bottomTierString = "24 to 1";
            case "0.04" -> bottomTierString = "25 to 1";
            case "0.038461538461538464" -> bottomTierString = "26 to 1";
            case "0.037037037037037035" -> bottomTierString = "27 to 1";

            case "0.03571428571428571" -> bottomTierString = "28 to 1";
            case "0.034482758620689655" -> bottomTierString = "29 to 1";
            case "0.03333333333333333" -> bottomTierString = "30 to 1";

        }
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

    private void buttonDealing(ButtonClickEvent event, GuildMusicManager musicManager) {

        //Horse
        if(event.getButton().getId().equalsIgnoreCase("horse1")) {
            if(event.getMessage().getMentionedUsers().get(0).getName().equals(event.getInteraction().getUser().getName())) {

                if(musicManager.trackScheduler.winningHorseDouble.get(event.getUser()).equals(event.getButton().getLabel())) {
                    horseWin(event, musicManager);
                }
                else{
                    horseLose(event, musicManager);
                }

                horse1Disabled = Button.primary("horse1","1").asDisabled();
                horseSendMessage(event, musicManager);

            }
            else{
                event.reply("Bruh, go play your own game " + event.getInteraction().getUser().getAsMention()).setEphemeral(true).queue();
            }
        }

        if(event.getButton().getId().equalsIgnoreCase("horse2")) {
            if(event.getMessage().getMentionedUsers().get(0).getName().equals(event.getInteraction().getUser().getName())) {

                if(musicManager.trackScheduler.winningHorseDouble.get(event.getUser()).equals(event.getButton().getLabel())) {
                    horseWin(event, musicManager);

                }
                else{
                    horseLose(event, musicManager);
                }

                horse2Disabled = Button.secondary("horse2","2").asDisabled();
                horseSendMessage(event, musicManager);
            }
            else{
                event.reply("Bruh, go play your own game " + event.getInteraction().getUser().getAsMention()).setEphemeral(true).queue();
            }
        }

        if(event.getButton().getId().equalsIgnoreCase("horse3")) {
            if(event.getMessage().getMentionedUsers().get(0).getName().equals(event.getInteraction().getUser().getName())) {

                if(musicManager.trackScheduler.winningHorseDouble.get(event.getUser()).equals(event.getButton().getLabel())) {
                    horseWin(event, musicManager);
                }
                else{
                    horseLose(event, musicManager);
                }

                horse3Disabled = Button.secondary("horse3","3").asDisabled();
                horseSendMessage(event, musicManager);

            }
            else{
                event.reply("Bruh, go play your own game " + event.getInteraction().getUser().getAsMention()).setEphemeral(true).queue();
            }
        }

        if(event.getButton().getId().equalsIgnoreCase("horse4")) {
            if(event.getMessage().getMentionedUsers().get(0).getName().equals(event.getInteraction().getUser().getName())) {

                if(musicManager.trackScheduler.winningHorseDouble.get(event.getUser()).equals(event.getButton().getLabel())) {
                    horseWin(event, musicManager);
                }
                else{
                    horseLose(event, musicManager);
                }

                horse4Disabled = Button.secondary("horse4","4").asDisabled();
                horseSendMessage(event, musicManager);

            }
            else{
                event.reply("Bruh, go play your own game " + event.getInteraction().getUser().getAsMention()).setEphemeral(true).queue();
            }
        }

        if(event.getButton().getId().equalsIgnoreCase("horse5")) {
            if(event.getMessage().getMentionedUsers().get(0).getName().equals(event.getInteraction().getUser().getName())) {

                if(musicManager.trackScheduler.winningHorseDouble.get(event.getUser()).equals(event.getButton().getLabel())) {
                    horseWin(event, musicManager);
                }
                else{
                    horseLose(event, musicManager);
                }
                horse5Disabled = Button.secondary("horse5","5").asDisabled();
                horseSendMessage(event, musicManager);
            }
            else{
                event.reply("Bruh, go play your own game " + event.getInteraction().getUser().getAsMention()).setEphemeral(true).queue();
            }
        }

        if(event.getButton().getId().equalsIgnoreCase("horse6")) {
            if(event.getMessage().getMentionedUsers().get(0).getName().equals(event.getInteraction().getUser().getName())) {

                if(musicManager.trackScheduler.winningHorseDouble.get(event.getUser()).equals(event.getButton().getLabel())) {
                    horseWin(event, musicManager);
                }
                else{
                    horseLose(event, musicManager);
                }
                horse6Disabled = Button.secondary("horse6","6").asDisabled();
                horseSendMessage(event, musicManager);

            }
            else{
                event.reply("Bruh, go play your own game " + event.getInteraction().getUser().getAsMention()).setEphemeral(true).queue();
            }
        }

        if(event.getButton().getId().equalsIgnoreCase("forfeitHorse")) {

            if(event.getMessage().getMentionedUsers().get(0).getName().equals(event.getInteraction().getUser().getName())) {
                embed.setAuthor(event.getUser().getName() + "'s Horse Racing game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());
                embed.setDescription("Race is canceled, I will give you your money back though <3.");
                embed.setColor(0xdd2e44);

                musicManager.trackScheduler.winningHorseDouble.remove(event.getUser());
                event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(horse1Disabled, horse2Disabled, horse3Disabled),
                        ActionRow.of(horse4Disabled, horse5Disabled, horse6Disabled, horseForfeitDisabled)).queue();
                embed.clear();
            }
            else{
                event.reply("Bruh, go play your own game " + event.getInteraction().getUser().getAsMention()).setEphemeral(true).queue();
            }
        }

    }



    private void horseSendMessage(@NotNull ButtonClickEvent event, GuildMusicManager musicManager) {
        event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(horse1Disabled,horse2Disabled,horse3Disabled),
                        ActionRow.of(horse4Disabled,horse5Disabled,horse6Disabled, horseForfeitDisabled))
                .queue();

        musicManager.trackScheduler.winningHorseDouble.remove(event.getUser());
        embed.clear();
    }

    private void horseWin (@NotNull ButtonClickEvent event, GuildMusicManager musicManager) {
        embed.setAuthor(event.getUser().getName() + "'s Horse Racing game",event.getUser().getAvatarUrl(),event.getUser().getAvatarUrl());
        embed.setColor(0x77b255);

        GameHorse gameHorse = musicManager.trackScheduler.winningHorseString.get(event.getUser());
        int winning = (int) (gameHorse.betAmount + (gameHorse.betAmount * Math.pow(gameHorse.odds, -1)));
        embed.addField("You won `" + winning + "` Coins.","Your horse number `" + musicManager.trackScheduler.winningHorseDouble.get(event.getUser()) + "` won the race!",false);

        int newCoins = previousAmount(event) + winning;
        update(event, newCoins);

    }

    private void horseLose (@NotNull ButtonClickEvent event, GuildMusicManager musicManager) {
        embed.setAuthor(event.getUser().getName() + "'s Horse Racing game",event.getUser().getAvatarUrl(),event.getUser().getAvatarUrl());
        embed.setColor(0xdd2e44);
        embed.setDescription("Your horse number `" + event.getButton().getLabel() + "` didn't win the race\n" +
                "Horse number `" + musicManager.trackScheduler.winningHorseDouble.get(event.getUser()) + "` won the race.");

        int newCoins = previousAmount(event) - musicManager.trackScheduler.winningHorseString.get(event.getUser()).betAmount;

        if(newCoins < 0) {
            newCoins = 0;
        }

        update(event, newCoins);
    }


    public Double findClosest(List<Double> arr, double target)
    {
        int n = arr.size();

        if (target <= arr.get(0))
            return arr.get(0);
        if (target >= arr.get(n-1))
            return arr.get(n-1);

        int i = 0, j = n, mid = 0;
        while (i < j) {
            mid = (i + j) / 2;

            if (arr.get(mid) == target)
                return arr.get(mid);

            if (target < arr.get(mid)) {

                if (mid > 0 && target > arr.get(mid))
                    return getClosest(arr.get(mid),
                            arr.get(mid), target);

                j = mid;
            }


            else {
                if (mid < n-1 && target < arr.get(mid))
                    return getClosest(arr.get(mid),
                            arr.get(mid), target);
                i = mid + 1; // update i
            }
        }

        return arr.get(mid);
    }

    public double getClosest(double val1, double val2,
                                 double target)
    {
        if (target - val1 >= val2 - target)
            return val2;
        else
            return val1;
    }

}

