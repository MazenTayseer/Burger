package Casino;

import Commands.CommandContext;
import Commands.ICommand;
import Database.SQLiteDataSource;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@SuppressWarnings("DuplicatedCode")
public class LeaderBoardsCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();
    @Override
    public void handle(CommandContext ctx) {

        String sql = "SELECT User_Tag, Coins FROM CASINO_DATABASE ORDER BY Coins DESC ";
        try (Connection conn = SQLiteDataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {

                    int i =0;
                    while (rs.next()) {
                        embed.setAuthor("Burger's Casino Leaderboard", ctx.getSelfUser().getAvatarUrl(), ctx.getSelfUser().getAvatarUrl());
                        embed.setColor(Color.white);
                        embed.setDescription("**Top #10**");
                        embed.addField("", (i+1) + " - `" + rs.getString("User_Tag") + "`, Net worth: `" + rs.getInt("Coins") + "`\n",false);

                        i++;
                        if(i >=10) {
                            break;
                        }
                    }

                    ctx.getChannel().sendMessageEmbeds(embed.build()).queue();
                    embed.clear();

                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "leaderboard";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Leaderboards");
        embed.setDescription("It shows the richest people in the casino!");
        embed.addField("Usage","**`-leaderboard`**",true);
        embed.setColor(0xffffff);
        return embed;
    }
}
