package Casino;

import CasinoMaps.GameVote;
import Commands.CommandContext;
import Commands.ICommand;
import Database.SQLiteDataSource;
import Music.GuildMusicManager;
import Music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.discordbots.api.client.DiscordBotListAPI;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("DuplicatedCode")
public class VoteCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();

    @Override
    public void handle(CommandContext ctx) {
        final GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(ctx.getGuild());


        if (getUser(ctx)) {

            if(!musicManager.trackScheduler.voteMap.containsKey(ctx.getAuthor())) {


                DiscordBotListAPI api = new DiscordBotListAPI.Builder()
                        .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6Ijk5MTE5NjE0MDYyNjI0NzcwMiIsImJvdCI6dHJ1ZSwiaWF0IjoxNjU5OTMxNjU1fQ.FvEoacmtyDWCFH3gJRKMLM3tRhJiCgWpz5sElxoWwlI")
                        .botId("991196140626247702")
                        .build();

                String userId = ctx.getAuthor().getId();

                GameVote gameVote = new GameVote(false);

                musicManager.trackScheduler.voteMap.put(ctx.getAuthor(), gameVote);
                Button voteMe = Button.link("https://top.gg/bot/991196140626247702", "🆙️Vote me");

                api.hasVoted(userId).whenComplete((hasVoted, e) -> {

                    if(hasVoted){
                        embed.setAuthor("Vote for burger", ctx.getSelfUser().getAvatarUrl(), ctx.getSelfUser().getAvatarUrl());
                        embed.setDescription("Thanks for voting!, `15000` have been added to your account,\n" +
                                "Come back every 12 hours!");
                        embed.setColor(Color.white);

                        updateCoins(ctx);
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                musicManager.trackScheduler.voteMap.remove(ctx.getAuthor());
                            }
                        }, 43200000);

                    }
                    else{
                        embed.setAuthor("Vote for burger", ctx.getSelfUser().getAvatarUrl(), ctx.getSelfUser().getAvatarUrl());
                        embed.setDescription("Vote for burger every 12 hours to get a big reward!\n" +
                                "When you're voted successfully, claim your reward by sending `-vote`.");
                        embed.setFooter("You are eligible to earn your reward right now!");
                        embed.setColor(Color.white);
                        musicManager.trackScheduler.voteMap.remove(ctx.getAuthor());
                    }

                    ctx.getChannel().sendMessageEmbeds(embed.build()).setActionRows(ActionRow.of(voteMe)).queue();
                    embed.clear();
                });

            }
            else{
                embed.setAuthor("Vote for burger", ctx.getSelfUser().getAvatarUrl(), ctx.getSelfUser().getAvatarUrl());
                embed.setDescription("Vote for burger every 12 hours to get a big reward!\n" +
                        "When you're voted successfully, claim your reward by sending `-vote`.");
                embed.setColor(Color.white);
                embed.setFooter("12 hours hasn't passed yet!");

                ctx.getChannel().sendMessageEmbeds(embed.build()).queue();
                embed.clear();
            }

        }


    }

    @Override
    public String getName() {
        return "vote";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Vote");
        embed.setDescription("You get `15000` if you voted for me on Top.gg!");
        embed.addField("Usage","**`-vote`**",true);
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
        int newCoins = getCoins(ctx) + 15000;
        update(ctx, newCoins);
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
