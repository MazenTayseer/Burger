package Casino;

import CasinoMaps.GameCrash;
import Commands.CommandContext;
import Commands.ICommand;
import Database.SQLiteDataSource;
import Music.GuildMusicManager;
import Music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class CrashCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();
    private final EmbedBuilder errorEmbed = new EmbedBuilder();
    private final Button crashStop = Button.danger("crashStop", "Stop!");
    private final Button crashStopDisabled = Button.danger("crashStop", "Stop!").asDisabled();

    private static final DecimalFormat df = new DecimalFormat("0.0");



    @Override
    public void handle(CommandContext ctx) {
        String[] args = ctx.getMessage().getContentRaw().split(" ");
        final GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(ctx.getGuild());
        Random random = new Random();

        if(args.length != 2){
            errorEmbed.setTitle("❌ Oops");
            errorEmbed.setDescription("Usage:  **`-crash`** `Number of coins`");
            errorEmbed.setColor(0xdd2e44);
            ctx.getChannel().sendMessageEmbeds(errorEmbed.build()).queue();
            errorEmbed.clear();
            return;
        }


        if(!isNumber(args[1])){
            errorEmbed.setTitle("❌ Oops");
            errorEmbed.setDescription("Usage:  **`-crash`** `Number of coins`");
            errorEmbed.setColor(0xdd2e44);
            ctx.getChannel().sendMessageEmbeds(errorEmbed.build()).queue();
            errorEmbed.clear();
            return;
        }


        if(getUser(ctx)) {

            if(!musicManager.trackScheduler.crashMap.containsKey(ctx.getAuthor())) {

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

                    double multiplierStart = 1.0;
                    double multiplierEnd = random.nextDouble(/*9.0*/) + 1;
                    boolean gameEnd = false;

                    System.out.println(df.format(multiplierEnd));

                    GameCrash gameCrash = new GameCrash(gameEnd ,Integer.parseInt(args[1]), multiplierStart, multiplierEnd);
                    musicManager.trackScheduler.crashMap.put(ctx.getAuthor(), gameCrash);

                    embed.setAuthor(ctx.getAuthor().getName() + "'s Crash game", ctx.getAuthor().getAvatarUrl(), ctx.getAuthor().getAvatarUrl());
                    embed.setColor(0x5865f2);
                    embed.setDescription("**Current Multiplier**\n" +
                            multiplierStart);

                    ctx.getChannel().sendMessage(ctx.getAuthor().getAsMention()).setEmbeds(embed.build()).setActionRows(ActionRow.of(crashStop))
                            .queue(message -> {
                                gameLoop(message, musicManager, ctx);
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
        return "crash";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Crash");
        embed.setDescription("I generate a random number between 1.0 and 10.0 which the multiplier will stop at eventually, " +
                "use the button `stop` before it to win!");
        embed.addField("Usage","**`-crash`**",true);
        embed.setColor(0xffffff);
        return embed;
    }

    private void gameLoop (Message message, GuildMusicManager musicManager, CommandContext ctx)  {
        GameCrash gameCrash = musicManager.trackScheduler.crashMap.get(ctx.getAuthor());
        double i = 0.1;
        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(gameCrash.gameEnd) {
                    musicManager.trackScheduler.crashMap.remove(ctx.getAuthor());
                    timer.cancel();
                    return;
                }


                if(gameCrash.multiplierEnd < gameCrash.multiplierStart) {
                    embed.setAuthor(ctx.getAuthor().getName() + "'s Crash game", ctx.getAuthor().getAvatarUrl(), ctx.getAuthor().getAvatarUrl());
                    embed.setColor(0xdd2e44);
                    embed.setDescription("You lost `" + (int)(gameCrash.betAmount*(gameCrash.multiplierStart-0.1))  + "` Coins\n" +
                            "**Current Multiplier**\n" +
                            df.format(musicManager.trackScheduler.crashMap.get(ctx.getAuthor()).multiplierStart-0.1));

                    int newCoins = previousAmount(ctx) - (int)(gameCrash.betAmount*(gameCrash.multiplierStart-0.1));

                    if(newCoins < 0) {
                        newCoins = 0;
                    }

                    update(ctx, newCoins);

                    message.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(crashStopDisabled)).queue();
                    musicManager.trackScheduler.crashMap.remove(ctx.getAuthor());
                    embed.clear();
                    timer.cancel();
                    return;
                }

                embed.setDescription("**Current Multiplier**\n" +
                        df.format(musicManager.trackScheduler.crashMap.get(ctx.getAuthor()).multiplierStart));

                message.editMessageEmbeds(embed.build()).queue();

                musicManager.trackScheduler.crashMap.get(ctx.getAuthor()).multiplierStart += i;

            }
        }, 0, 1500);



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
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                return resultSet.getInt("Coins");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
