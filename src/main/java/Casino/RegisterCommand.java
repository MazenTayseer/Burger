package Casino;

import Commands.CommandContext;
import Commands.ICommand;
import Database.SQLiteDataSource;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();
    @Override
    public void handle(CommandContext ctx) {
        User user = ctx.getAuthor();

        String sql = "INSERT INTO CASINO_DATABASE(Username, User_Tag, User_ID, Coins) VALUES (?, ?, ?, ?)";
        try (final PreparedStatement insertStatement = SQLiteDataSource
                .getConnection()
                .prepareStatement(sql)) {

            Connection con = SQLiteDataSource.getConnection();
            PreparedStatement ps =
                    con.prepareStatement
                            ("SELECT User_ID FROM CASINO_DATABASE WHERE User_ID =?");

            ps.setString (1, ctx.getAuthor().getId());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                embed.setTitle("❌ Oops");
                embed.setDescription("You are registered in the casino.");
                embed.setColor(0xdd2e44);
                ctx.getChannel().sendMessage(ctx.getAuthor().getAsMention()).setEmbeds(embed.build()).queue();
                embed.clear();
            } else {
                insertStatement.setString(1, user.getName());

                insertStatement.setString(2, ctx.getAuthor().getAsTag());

                insertStatement.setString(3, ctx.getAuthor().getId());

                insertStatement.setInt(4, 2000);

                insertStatement.execute();

                embed.setTitle("Welcome To Burger's Casino, " + ctx.getAuthor().getName() + "!");
                embed.setDescription("You have been granted `2000` Coins to start playing with.");
                embed.setThumbnail(ctx.getSelfUser().getAvatarUrl());
                embed.setColor(Color.white);

                embed.addField("Available games", """
                        BlackJack, `-bj` `Coins`
                        Crash, `-crash` `Coins`
                        Dice Rolling, `-dice` `Coins`
                        Higher or Lower, `-highlow` `Coins`
                        Horse Racing, `-hr` `Coins`
                        Trivia Questions, `-trivia` `Coins`
                        """,true);

                embed.addBlankField(true);

                embed.addField("Also,", """
                        `-Wage` for collecting `1000` every hour.
                        `-vote`, Vote for me on top.gg to get `15000` every 12 hours.
                        `-bal`, to check your current balance.
                        `-leaderboard`, to see the current richest users.
                        """,true);

                ctx.getChannel().sendMessage(ctx.getAuthor().getAsMention()).setEmbeds(embed.build()).queue();
                embed.clear();
            }

            con.close();
            ps.close();
            insertStatement.close();
            rs.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }


    @Override
    public String getName() {
        return "register";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Register");
        embed.setDescription("You register to play Casino games, and win money!");
        embed.addField("Usage","**`-register`**",true);
        embed.setColor(0xffffff);
        return embed;
    }
}
