package Casino;

import Commands.CommandContext;
import Commands.ICommand;
import Database.SQLiteDataSource;
import Music.GuildMusicManager;
import Music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("DuplicatedCode")
public class WageCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();

    @Override
    public void handle(CommandContext ctx) {
        final GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(ctx.getGuild());

        if (getUser(ctx)) {

            if(!musicManager.trackScheduler.wageMap.containsKey(ctx.getAuthor())) {
                Timer timer = new Timer();
                musicManager.trackScheduler.wageMap.put(ctx.getAuthor(), timer);
                updateCoins(ctx);

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        musicManager.trackScheduler.wageMap.remove(ctx.getAuthor());
                    }
                }, 3600000);
            }
            else{
                embed.setAuthor(ctx.getAuthor().getName() + "'s wage", ctx.getAuthor().getAvatarUrl(), ctx.getAuthor().getAvatarUrl());
                embed.setColor(0xdd2e44);

                embed.setDescription("an hour hasn't passed yet, Have some Patience!");
                ctx.getChannel().sendMessageEmbeds(embed.build()).queue();
                embed.clear();
            }
        }

    }

    @Override
    public String getName() {
        return "wage";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Wage");
        embed.setDescription("You get `1000` wage every 1 hour");
        embed.addField("Usage","**`-wage`**",true);
        embed.setColor(0xffffff);
        return embed;
    }

    private boolean getUser(CommandContext ctx) {

        try (Connection con = SQLiteDataSource.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT User_ID FROM CASINO_DATABASE WHERE User_ID =?")) {


            ps.setString(1, ctx.getAuthor().getId());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return true;
            } else {
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
    

    private void updateCoins(CommandContext ctx) {
        embed.setAuthor(ctx.getAuthor().getName() + "'s wage", ctx.getAuthor().getAvatarUrl(), ctx.getAuthor().getAvatarUrl());
        embed.setColor(0x77b255);

        embed.setDescription("Here's your wage, `1000` has been added to your account.\n" +
                "Come back every hour to collect your wage.");

        int newCoins = getCoins(ctx) + 1000;
        update(ctx, newCoins);
        ctx.getChannel().sendMessageEmbeds(embed.build()).queue();
        embed.clear();
    }

    private void update(CommandContext ctx, int coins) {
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
