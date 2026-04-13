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
import java.util.Arrays;
import java.util.List;

public class BalanceCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();
    @Override
    public void handle(CommandContext ctx) {

        try (Connection con = SQLiteDataSource.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT User_ID FROM CASINO_DATABASE WHERE User_ID =?")) {


            ps.setString(1, ctx.getAuthor().getId());
            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                showBalance(ctx);
            } else {
                embed.setTitle("❌ Oops");
                embed.setDescription("You are not registered in the casino, please register first by typing `-register`.");
                embed.setColor(0xdd2e44);
                ctx.getChannel().sendMessageEmbeds(embed.build()).queue();
                embed.clear();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getName() {
        return "bal";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Balance");
        embed.setDescription("Shows the balance a user.");
        embed.addField("Usage","**`-bal`**",true);
        embed.addField("Aliases","`balance`",true);
        embed.setColor(0xffffff);
        return embed;
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("balance");
    }

    private void showBalance (CommandContext ctx) {
        String sql = "SELECT Coins FROM CASINO_DATABASE WHERE User_ID = ?";
        try (Connection con = SQLiteDataSource.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {


            preparedStatement.setString(1, ctx.getAuthor().getId());
            ResultSet resultSet = preparedStatement.executeQuery();

            embed.setAuthor(ctx.getAuthor().getName() + "'s Balance", ctx.getAuthor().getAvatarUrl(), ctx.getAuthor().getAvatarUrl());
            embed.setColor(Color.white);

            while (resultSet.next()) {
                embed.setDescription("You have `" + resultSet.getInt("Coins") + "` coins in your balance.");
            }

            ctx.getChannel().sendMessageEmbeds(embed.build()).queue();
            embed.clear();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
