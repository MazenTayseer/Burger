package Casino;

import Commands.CommandContext;
import Commands.ICommand;
import Database.SQLiteDataSource;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class DiceCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();
    private final EmbedBuilder errorEmbed = new EmbedBuilder();
    @Override
    public void handle(CommandContext ctx) {
        String[] args = ctx.getMessage().getContentRaw().split(" ");
        Random random = new Random();

        if(args.length != 2){
            errorEmbed.setTitle("❌ Oops");
            errorEmbed.setDescription("Usage:  **`-dice`** `Number of coins`");
            errorEmbed.setColor(0xdd2e44);
            ctx.getChannel().sendMessageEmbeds(errorEmbed.build()).queue();
            errorEmbed.clear();
            return;
        }


        if(!isNumber(args[1])){
            errorEmbed.setTitle("❌ Oops");
            errorEmbed.setDescription("Usage:  **`-dice`** `Number of coins`");
            errorEmbed.setColor(0xdd2e44);
            ctx.getChannel().sendMessageEmbeds(errorEmbed.build()).queue();
            errorEmbed.clear();
            return;
        }


        if(getUser(ctx)) {

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

                int dealersDice1 = random.nextInt(5) + 1;
                int dealersDice2 = random.nextInt(5) + 1;
                int dealersTotal = dealersDice1 + dealersDice2;

                int playersDice1 = random.nextInt(5) + 1;
                int playersDice2 = random.nextInt(5) + 1;
                int playersTotal =playersDice1 + playersDice2;

                embed.setAuthor(ctx.getAuthor().getName() + "'s Dice game", ctx.getAuthor().getAvatarUrl(), ctx.getAuthor().getAvatarUrl());
                embed.addField("Dealer's Dice","Total = `" + dealersTotal + "`",true);
                embed.addField("Player's Dice","Total = `" + playersTotal + "`",true);

                if(playersTotal > dealersTotal) {
                    embed.setDescription("**You won `" + Integer.parseInt(args[1]) + "` Coins.**");
                    embed.setColor(0x77b255);

                    int newCoins = previousAmount(ctx) + Integer.parseInt(args[1]);
                    update(ctx, newCoins);
                }
                else if(playersTotal < dealersTotal) {
                    embed.setDescription("**You lost `" + Integer.parseInt(args[1]) + "` Coins.**");
                    embed.setColor(0xdd2e44);
                    int newCoins = previousAmount(ctx) - Integer.parseInt(args[1]);

                    if(newCoins < 0) {
                        newCoins = 0;
                    }

                    update(ctx, newCoins);
                }
                else {
                    embed.setDescription("**Draw**");
                    embed.setColor(Color.ORANGE);
                }

                ctx.getChannel().sendMessageEmbeds(embed.build()).queue();
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

    @Override
    public String getName() {
        return "dice";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Dice");
        embed.setDescription("Each player gets 2 random dice numbers, the one with the higher total wins.");
        embed.addField("Usage","**`-dice`**",true);
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
}
