package Casino;

import CasinoMaps.GameBlackjack;
import Commands.CommandContext;
import Commands.ICommand;
import Database.SQLiteDataSource;
import Music.GuildMusicManager;
import Music.PlayerManager;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"DuplicatedCode", "RedundantCollectionOperation"})
public class BlackJackCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();
    private final EmbedBuilder errorEmbed = new EmbedBuilder();
    private final EmbedBuilder embedBuilder = new EmbedBuilder();
    private final Random random = new Random();
    private final String[] cards = {"♠ 1", "♠ 2", "♠ 3", "♠ 4", "♠ 5", "♠ 6", "♠ 7", "♠ 8", "♠ 9", "♠ 10", "♠ J", "♠ Q", "♠ K", "♠ A",
            "♣ 1", "♣ 2", "♣ 3", "♣ 4", "♣ 5", "♣ 6", "♣ 7", "♣ 8", "♣ 9", "♣ 10", "♣ J", "♣ Q", "♣ K", "♣ A",
            "♦ 1", "♦ 2", "♦ 3", "♦ 4", "♦ 5", "♦ 6", "♦ 7", "♦ 8", "♦ 9", "♦ 10", "♦ J", "♦ Q", "♦ K", "♦ A",
            "❤ 1", "❤ 2", "❤ 3", "❤ 4", "❤ 5", "❤ 6", "❤ 7", "❤ 8", "❤ 9", "❤ 10", "❤ J", "❤ Q", "❤ K", "❤ A"};

    private final Button Hit = Button.primary("hit", "Hit");
    private final Button Stand = Button.primary("stand", "Stand");
    private final Button Forfeit = Button.danger("forfeit", "Forfeit");

    private final Button HitDisabled = Button.primary("hit", "Hit").asDisabled();
    private final Button StandDisabled = Button.primary("stand", "Stand").asDisabled();
    private final Button ForfeitDisabled = Button.danger("forfeit", "Forfeit").asDisabled();

    private int card1;
    private int card2;
    private String dealerCard1;
    private String dealerCard2;
    private int card3;
    private int card4;
    private String playerCard1;
    private String playerCard2;

    private final EventWaiter waiter;

    public BlackJackCommand(EventWaiter waiter) {
        this.waiter = waiter;
    }


    @Override
    public void handle(CommandContext ctx) {
        String[] args = ctx.getMessage().getContentRaw().split(" ");
        final GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(ctx.getGuild());


        if (args.length != 2) {
            errorEmbed.setTitle("❌ Oops");
            errorEmbed.setDescription("Usage:  **`-bj`** `Number of coins`");
            errorEmbed.setColor(0xdd2e44);
            ctx.getChannel().sendMessageEmbeds(errorEmbed.build()).queue();
            errorEmbed.clear();
            return;
        }


        if (!isNumber(args[1])) {
            errorEmbed.setTitle("❌ Oops");
            errorEmbed.setDescription("Usage:  **`-bj`** `Number of coins`");
            errorEmbed.setColor(0xdd2e44);
            ctx.getChannel().sendMessageEmbeds(errorEmbed.build()).queue();
            errorEmbed.clear();
            return;
        }


        TextChannel channel = ctx.getChannel();

        User user = ctx.getAuthor();

        if (getUser(ctx)) {

            if (!musicManager.trackScheduler.blackjackMap.containsKey(user)) {

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

                    card1 = random.nextInt(cards.length);
                    card2 = random.nextInt(cards.length);

                    dealerCard1 = cards[card1];
                    dealerCard2 = cards[card2];

                    card3 = random.nextInt(cards.length);
                    card4 = random.nextInt(cards.length);

                    playerCard1 = cards[card3];
                    playerCard2 = cards[card4];

                    highCardsConversion();

                    String[] split1 = dealerCard1.split(" ");
                    String[] split2 = dealerCard2.split(" ");

                    String[] split3 = playerCard1.split(" ");
                    String[] split4 = playerCard2.split(" ");

                    int playersTotal = Integer.parseInt(split3[1]) + Integer.parseInt(split4[1]);
                    int dealersTotal = Integer.parseInt(split1[1]) + Integer.parseInt(split2[1]);

                    GameBlackjack game = new GameBlackjack(Integer.parseInt(args[1]), card1, card2, dealerCard1, dealerCard2, dealersTotal, playersTotal, card3, card4, playerCard1, playerCard2);
                    musicManager.trackScheduler.blackjackMap.put(user, game);

                    if (blackJack(ctx, musicManager, args, dealersTotal, playersTotal)) {
                        musicManager.trackScheduler.blackjackMap.remove(user, game);
                        return;
                    }

                    Aces(ctx, musicManager);

                    embed.setAuthor(ctx.getAuthor().getName() + "'s Blackjack game", ctx.getAuthor().getAvatarUrl(), ctx.getAuthor().getAvatarUrl());
                    embed.setColor(0x5865f2);
                    embed.addField("Burger's cards (Dealer)", "Cards - `" + cards[card1] +
                            "` `?`\n" +
                            "Total: `?`", false);

                    embed.addBlankField(false);

                    embed.addField(ctx.getAuthor().getName() + "'s Cards (Player)",
                            "Cards - `" + cards[card3]
                                    + "` `" + cards[card4] +
                                    "`\n" +
                                    "Total: `" + musicManager.trackScheduler.blackjackMap.get(ctx.getMember().getUser()).playersTotal + "`", false);


                    embedBuilder.setTitle("Burger's Casino");
                    embedBuilder.setDescription("You waited for too long, So I took your money!\n" +
                            "Deducted `" + Integer.parseInt(args[1]) + "` Coins from your balance");
                    embedBuilder.setColor(0xdd2e44);
                    embedBuilder.setThumbnail(ctx.getSelfUser().getAvatarUrl());

                    Button voteMe = Button.link("https://top.gg/bot/991196140626247702", "⬆️Vote me");


                    channel.sendMessage(ctx.getAuthor().getAsMention()).setEmbeds(embed.build())
                            .setActionRows(ActionRow.of(Hit, Stand, Forfeit))
                            .queue(message -> WaitForButtonClick(ctx, musicManager, message, args));

                    embed.clear();

                } else {
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
        return "bj";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("BlackJack");
        embed.setDescription("The player who is closer to 21 wins, Button `Hit` makes you draw a card, " +
                "Button `Stand` makes you stop with the current hand you have, " +
                "Button `Forfeit` makes you exit the game.");
        embed.addField("Usage", "**`-bj`**", true);
        embed.addField("Aliases", "`blackjack`", true);
        embed.setColor(0xffffff);
        return embed;
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("blackjack");
    }

    private boolean isNumber(String message) {
        try {
            Integer.parseInt(message);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }


    private void buttonDealing(CommandContext ctx, String[] args, ButtonClickEvent event, GuildMusicManager musicManager) {
        GameBlackjack game = musicManager.trackScheduler.blackjackMap.get(event.getUser());

        //Hit 1
        String playerCard1;

        //Stand 1
        String newCard;
        if (event.getButton().getId().equalsIgnoreCase("hit")) {

            if (event.getMessage().getMentionedUsers().get(0).getName().equals(event.getInteraction().getUser().getName())) {
                int newCard1 = random.nextInt(cards.length);
                playerCard1 = cards[newCard1];


                if (playerCard1.contains("J") || playerCard1.contains("K") || playerCard1.contains("Q")) {
                    playerCard1 = "❤ 10";
                }

                if (playerCard1.contains("A")) {
                    musicManager.trackScheduler.blackjackMap.get(event.getUser()).playerAces++;
                    playerCard1 = "❤ 11";
                }


                String[] split = playerCard1.split(" ");

                musicManager.trackScheduler.blackjackMap.get(event.getUser()).playersTotal =
                        musicManager.trackScheduler.blackjackMap.get(event.getUser()).playersTotal + Integer.parseInt(split[1]);

                if (game.playersTotal > 21 && musicManager.trackScheduler.blackjackMap.get(event.getUser()).playerAces > 0) {
                    musicManager.trackScheduler.blackjackMap.get(event.getUser()).playersTotal -= 10;
                    musicManager.trackScheduler.blackjackMap.get(event.getUser()).playerAces--;
                }

                musicManager.trackScheduler.blackjackMap.get(event.getUser()).playerList.add(cards[newCard1]);

                if (musicManager.trackScheduler.blackjackMap.get(event.getUser()).playersTotal > 21) {
                    EmbedBuilder embedBuilder = new EmbedBuilder();

                    embedBuilder.setAuthor(event.getUser().getName() + "'s Blackjack game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());
                    embedBuilder.setDescription("You lost `" + musicManager.trackScheduler.blackjackMap.get(event.getUser()).betAmount + "` Coins");
                    embedBuilder.setColor(0xdd2e44);

                    embedBuilder.addField("Burger's cards (Dealer)", "Cards - `" + cards[game.card1] +
                            "` `" + cards[game.card2] + "`\n" +
                            "Total: `" + game.dealersTotal + "`", false);

                    embedBuilder.addBlankField(false);

                    embedBuilder.addField(event.getUser().getName() + "'s Cards (Player)",
                            "Cards - `" + cards[game.card3]
                                    + "` `" + cards[game.card4] +
                                    "` `" + musicManager.trackScheduler.blackjackMap.get(event.getUser()).playerList + "`\n" +
                                    "Total: `" + game.playersTotal + "`", false);

                    int newCoins = previousAmount(event) - musicManager.trackScheduler.blackjackMap.get(event.getUser()).betAmount;

                    if (newCoins < 0) {
                        newCoins = 0;
                    }

                    update(event, newCoins);

                    musicManager.trackScheduler.blackjackMap.remove(event.getUser());
                    event.editMessageEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(HitDisabled, StandDisabled, ForfeitDisabled))
                            .queue();
                    embed.clear();
                    embedBuilder.clear();
                    return;
                }


                embed.setAuthor(event.getUser().getName() + "'s Blackjack game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());
                embed.setColor(0x5865f2);

                embed.addField("Burger's cards (Dealer)", "Cards - `" + cards[game.card1] +
                        "` `?`\n" +
                        "Total: `?`", false);

                embed.addBlankField(false);

                embed.addField(event.getUser().getName() + "'s Cards (Player)",
                        "Cards - `" + cards[game.card3]
                                + "` `" + cards[game.card4] +
                                "` `" + musicManager.trackScheduler.blackjackMap.get(event.getUser()).playerList + "`\n" +
                                "Total: `" + game.playersTotal + "`", false);

                event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(Hit, Stand, Forfeit))
                        .queue(message -> WaitForButtonClickAfter(ctx, musicManager, message, args));
                embed.clear();
            } else {
                event.reply("Bruh, go play your own game " + event.getInteraction().getUser().getAsMention()).setEphemeral(true).queue();
            }

        }


        if (event.getButton().getId().equalsIgnoreCase("stand")) {

            if (event.getMessage().getMentionedUsers().get(0).getName().equals(event.getInteraction().getUser().getName())) {


                if (game.dealersTotal > game.playersTotal) {
                    embed.setColor(0xdd2e44);

                    embed.setAuthor(event.getUser().getName() + "'s Blackjack game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());
                    embed.setDescription("You lost `" + musicManager.trackScheduler.blackjackMap.get(event.getUser()).betAmount + "` Coins");

                    embed.addField("Burger's cards (Dealer)", "Cards - `" + cards[game.card1] +
                            "` `" + cards[game.card2] + "`\n" +
                            "Total: `" + game.dealersTotal + "`", false);

                    embed.addBlankField(false);

                    if (musicManager.trackScheduler.blackjackMap.get(event.getUser()).playerList.size() == 0) {
                        embed.addField(event.getUser().getName() + "'s Cards (Player)",
                                "Cards - `" + cards[game.card3]
                                        + "` `" + cards[game.card4] +
                                        "`\n" +
                                        "Total: `" + game.playersTotal + "`", false);
                    } else {
                        embed.addField(event.getUser().getName() + "'s Cards (Player)",
                                "Cards - `" + cards[game.card3]
                                        + "` `" + cards[game.card4] +
                                        "` `" + musicManager.trackScheduler.blackjackMap.get(event.getUser()).playerList + "`\n" +
                                        "Total: `" + game.playersTotal + "`", false);
                    }

                    int newCoins = previousAmount(event) - musicManager.trackScheduler.blackjackMap.get(event.getUser()).betAmount;

                    if (newCoins < 0) {
                        newCoins = 0;
                    }

                    update(event, newCoins);
                    event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(HitDisabled, StandDisabled, ForfeitDisabled)).queue();
                    embed.clear();
                } else if (game.dealersTotal == game.playersTotal) {
                    embed.setColor(Color.ORANGE);

                    embed.setAuthor(event.getUser().getName() + "'s Blackjack game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());
                    embed.setDescription("**Draw.**");

                    embed.addField("Burger's cards (Dealer)", "Cards - `" + cards[game.card1] +
                            "` `" + cards[game.card2] + "`\n" +
                            "Total: `" + game.dealersTotal + "`", false);

                    embed.addBlankField(false);

                    if (musicManager.trackScheduler.blackjackMap.get(event.getUser()).playerList.size() == 0) {
                        embed.addField(event.getUser().getName() + "'s Cards (Player)",
                                "Cards - `" + cards[game.card3]
                                        + "` `" + cards[game.card4] +
                                        "`\n" +
                                        "Total: `" + game.playersTotal + "`", false);
                    } else {
                        embed.addField(event.getUser().getName() + "'s Cards (Player)",
                                "Cards - `" + cards[game.card3]
                                        + "` `" + cards[game.card4] +
                                        "` `" + musicManager.trackScheduler.blackjackMap.get(event.getUser()).playerList + "`\n" +
                                        "Total: `" + game.playersTotal + "`", false);
                    }

                    event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(HitDisabled, StandDisabled, ForfeitDisabled)).queue();
                    embed.clear();
                } else {
                    while (game.playersTotal > game.dealersTotal) {
                        int card = random.nextInt(cards.length);
                        newCard = cards[card];


                        if (newCard.contains("J") || newCard.contains("K") || newCard.contains("Q")) {
                            newCard = "❤ 10";
                        }

                        if (newCard.contains("A")) {
                            musicManager.trackScheduler.blackjackMap.get(event.getUser()).dealerAces++;
                            newCard = "❤ 1";
                        }

                        String[] split = newCard.split(" ");
                        musicManager.trackScheduler.blackjackMap.get(event.getUser()).dealersTotal =
                                musicManager.trackScheduler.blackjackMap.get(event.getUser()).dealersTotal + Integer.parseInt(split[1]);

                        if (game.dealersTotal > 21 && musicManager.trackScheduler.blackjackMap.get(event.getUser()).dealerAces > 0) {
                            musicManager.trackScheduler.blackjackMap.get(event.getUser()).dealersTotal -= 10;
                            musicManager.trackScheduler.blackjackMap.get(event.getUser()).dealerAces--;
                        }

                        musicManager.trackScheduler.blackjackMap.get(event.getUser()).dealerList.add(cards[card]);
                    }

                    if (game.dealersTotal > 21) {
                        embed.setColor(0x77b255);
                        embed.setDescription("You won `" + musicManager.trackScheduler.blackjackMap.get(event.getUser()).betAmount + "` Coins");
                        int newCoins = previousAmount(event) + (musicManager.trackScheduler.blackjackMap.get(event.getUser()).betAmount);
                        update(event, newCoins);
                    } else if (game.dealersTotal > game.playersTotal) {
                        embed.setColor(0xdd2e44);
                        embed.setDescription("You lost `" + musicManager.trackScheduler.blackjackMap.get(event.getUser()).betAmount + "` Coins");
                        int newCoins = previousAmount(event) - musicManager.trackScheduler.blackjackMap.get(event.getUser()).betAmount;

                        if (newCoins < 0) {
                            newCoins = 0;
                        }

                        update(event, newCoins);
                    } else {
                        embed.setColor(Color.ORANGE);
                        embed.setDescription("**Draw.**");
                    }
                    embed.setAuthor(event.getUser().getName() + "'s Blackjack game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());

                    embed.addField("Burger's cards (Dealer)", "Cards - `" + cards[game.card1] +
                            "` `" + cards[game.card2] + "` `" + musicManager.trackScheduler.blackjackMap.get(event.getUser()).dealerList + "`\n" +
                            "Total: `" + game.dealersTotal + "`", false);

                    embed.addBlankField(false);

                    if (musicManager.trackScheduler.blackjackMap.get(event.getUser()).playerList.size() == 0) {
                        embed.addField(event.getUser().getName() + "'s Cards (Player)",
                                "Cards - `" + cards[game.card3]
                                        + "` `" + cards[game.card4] +
                                        "`\n" +
                                        "Total: `" + game.playersTotal + "`", false);
                    } else {
                        embed.addField(event.getUser().getName() + "'s Cards (Player)",
                                "Cards - `" + cards[game.card3]
                                        + "` `" + cards[game.card4] +
                                        "` `" + musicManager.trackScheduler.blackjackMap.get(event.getUser()).playerList + "`\n" +
                                        "Total: `" + game.playersTotal + "`", false);
                    }
                    event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(HitDisabled, StandDisabled, ForfeitDisabled)).queue();
                    embed.clear();
                }

                musicManager.trackScheduler.blackjackMap.remove(event.getUser());
            } else {
                event.reply("Bruh, go play your own game " + event.getInteraction().getUser().getAsMention()).setEphemeral(true).queue();
            }

        }


        if (event.getButton().getId().equalsIgnoreCase("forfeit")) {

            if (event.getMessage().getMentionedUsers().get(0).getName().equals(event.getInteraction().getUser().getName())) {
                embed.setAuthor(event.getUser().getName() + "'s Blackjack game", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());
                embed.setDescription("You decided to forfeit, so i I will take your money 💸.\n`" +
                        musicManager.trackScheduler.blackjackMap.get(event.getUser()).betAmount +
                        "` was deducted from your account.");
                embed.setColor(0xdd2e44);

                int newCoins = previousAmount(event) - musicManager.trackScheduler.blackjackMap.get(event.getUser()).betAmount;

                if (newCoins < 0) {
                    newCoins = 0;
                }

                update(event, newCoins);

                musicManager.trackScheduler.blackjackMap.remove(event.getUser());
                event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(HitDisabled, StandDisabled, ForfeitDisabled)).queue();
                embed.clear();
            } else {
                event.reply("Bruh, go play your own game " + event.getInteraction().getUser().getAsMention()).setEphemeral(true).queue();
            }

        }
    }

    private void WaitForButtonClick(CommandContext ctx, GuildMusicManager musicManager, Message message, String[] args) {
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
                    buttonDealing(ctx, args, event, musicManager);
                },

                15, TimeUnit.SECONDS,
                () ->
                {
                    message.editMessageEmbeds(embedBuilder.build())
                            .setActionRows().queue();

                    if (musicManager.trackScheduler.blackjackMap.containsKey(ctx.getAuthor())) {
                        musicManager.trackScheduler.blackjackMap.remove(ctx.getAuthor());


                        int newCoins = previousAmount(ctx) - Integer.parseInt(args[1]);

                        if (newCoins < 0) {
                            newCoins = 0;
                        }

                        update(ctx, newCoins);
                    }
                    embedBuilder.clear();
                }
        );
    }

    private void WaitForButtonClickAfter(CommandContext ctx, GuildMusicManager musicManager, InteractionHook message, String[] args) {
        this.waiter.waitForEvent(
                ButtonClickEvent.class,
                e -> {
                    if (!e.getMessage().getMentionedUsers().get(0).getName().equals(e.getInteraction().getUser().getName())) {
                        e.reply("Bruh, go play your own game " + e.getInteraction().getUser().getAsMention()).setEphemeral(true).queue();
                        return false;
                    }

                    return !e.isAcknowledged();
                },
                e -> {
                    buttonDealing(ctx, args, e, musicManager);
                },

                15, TimeUnit.SECONDS,
                () ->
                {
                    message.editOriginalEmbeds(embedBuilder.build())
                            .setActionRows().queue();

                    if (musicManager.trackScheduler.blackjackMap.containsKey(ctx.getAuthor())) {
                        musicManager.trackScheduler.blackjackMap.remove(ctx.getAuthor());


                        int newCoins = previousAmount(ctx) - Integer.parseInt(args[1]);

                        if (newCoins < 0) {
                            newCoins = 0;
                        }

                        update(ctx, newCoins);
                    }
                    embedBuilder.clear();
                }
        );

    }


    private void highCardsConversion() {

        if (dealerCard1.contains("J") || dealerCard1.contains("K") || dealerCard1.contains("Q")) {
            dealerCard1 = "❤ 10";
        }

        if (dealerCard2.contains("J") || dealerCard2.contains("K") || dealerCard2.contains("Q")) {
            dealerCard2 = "❤ 10";
        }

        if (dealerCard1.contains("A")) {
            dealerCard1 = "❤ 11";

        }

        if (dealerCard2.contains("A")) {
            dealerCard2 = "❤ 11";
        }


        if (playerCard1.contains("J") || playerCard1.contains("K") || playerCard1.contains("Q")) {
            playerCard1 = "❤ 10";
        }

        if (playerCard2.contains("J") || playerCard2.contains("K") || playerCard2.contains("Q")) {
            playerCard2 = "❤ 10";
        }


        if (playerCard1.contains("A")) {
            playerCard1 = "❤ 11";

        }

        if (playerCard2.contains("A")) {
            playerCard2 = "❤ 11";
        }

    }

    private boolean blackJack(CommandContext ctx, GuildMusicManager musicManager, String[] args, int dealersTotal, int playersTotal) {
        if (cards[card1].contains("A") && (cards[card2].contains("K") || cards[card2].contains("J") || cards[card2].contains("Q") || cards[card2].contains("10"))
                || (cards[card1].contains("K") || cards[card1].contains("J") || cards[card1].contains("Q") || cards[card1].contains("10")) && cards[card2].contains("A")) {
            embed.setAuthor(ctx.getAuthor().getName() + "'s Blackjack game", ctx.getAuthor().getAvatarUrl(), ctx.getAuthor().getAvatarUrl());
            embed.setDescription("BLACKJACK! \n" + "You lost `" + Integer.parseInt(args[1]) + "` Coins.");
            embed.setColor(0xdd2e44);

            int newCoins = previousAmount(ctx) - musicManager.trackScheduler.blackjackMap.get(ctx.getAuthor()).betAmount;

            if (newCoins < 0) {
                newCoins = 0;
            }

            update(ctx, newCoins);

            embed.addField("Burger's cards (Dealer)", "Cards - `" + cards[card1] +
                    "` `" + cards[card2] + "`\n" +
                    "Total: `" + dealersTotal + "`", false);

            embed.addBlankField(false);

            embed.addField(ctx.getAuthor().getName() + "'s Cards (Player)",
                    "Cards - `" + cards[card3]
                            + "` `" + cards[card4] +
                            "`\n" +
                            "Total: `" + playersTotal + "`", false);


            ctx.getChannel().sendMessageEmbeds(embed.build()).setActionRows(ActionRow.of(HitDisabled, StandDisabled, ForfeitDisabled)).queue();
            embed.clear();
            return true;

        } else if (cards[card3].contains("A") && (cards[card4].contains("K") || cards[card4].contains("J") || cards[card4].contains("Q") || cards[card4].contains("10"))
                || (cards[card3].contains("K") || cards[card3].contains("J") || cards[card3].contains("Q") || cards[card3].contains("10")) && cards[card4].contains("A")) {
            embed.setAuthor(ctx.getAuthor().getName() + "'s Blackjack game", ctx.getAuthor().getAvatarUrl(), ctx.getAuthor().getAvatarUrl());
            embed.setDescription("BLACKJACK! \n" + "You won `" + (Integer.parseInt(args[1]) * 1.5) + "` Coins.");
            embed.setColor(0x77b255);

            int newCoins = (int) (previousAmount(ctx) + (musicManager.trackScheduler.blackjackMap.get(ctx.getAuthor()).betAmount) * 1.5);
            update(ctx, newCoins);

            embed.addField("Burger's cards (Dealer)", "Cards - `" + cards[card1] +
                    "` `" + cards[card2] + "`\n" +
                    "Total: `" + dealersTotal + "`", false);

            embed.addBlankField(false);

            embed.addField(ctx.getAuthor().getName() + "'s Cards (Player)",
                    "Cards - `" + cards[card3]
                            + "` `" + cards[card4] +
                            "`\n" +
                            "Total: `" + playersTotal + "`", false);


            ctx.getChannel().sendMessageEmbeds(embed.build()).setActionRows(ActionRow.of(HitDisabled, StandDisabled, ForfeitDisabled)).queue();
            embed.clear();
            return true;
        }

        return false;

    }

    private void Aces(CommandContext ctx, GuildMusicManager musicManager) {

        if (cards[card1].contains("A")) {
            musicManager.trackScheduler.blackjackMap.get(ctx.getMember().getUser()).dealerAces++;

        }

        if (cards[card2].contains("A")) {
            musicManager.trackScheduler.blackjackMap.get(ctx.getMember().getUser()).dealerAces++;
        }

        if (cards[card3].contains("A")) {
            musicManager.trackScheduler.blackjackMap.get(ctx.getMember().getUser()).playerAces++;

        }

        if (cards[card4].contains("A")) {
            musicManager.trackScheduler.blackjackMap.get(ctx.getMember().getUser()).playerAces++;
        }

        if (musicManager.trackScheduler.blackjackMap.get(ctx.getMember().getUser()).dealersTotal > 21) {
            musicManager.trackScheduler.blackjackMap.get(ctx.getMember().getUser()).dealersTotal -= 10;
            musicManager.trackScheduler.blackjackMap.get(ctx.getMember().getUser()).dealerAces--;
        }

        if (musicManager.trackScheduler.blackjackMap.get(ctx.getMember().getUser()).playersTotal > 21) {
            musicManager.trackScheduler.blackjackMap.get(ctx.getMember().getUser()).playersTotal -= 10;
            musicManager.trackScheduler.blackjackMap.get(ctx.getMember().getUser()).playerAces--;
        }


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

    private void update(ButtonClickEvent ctx, int coins) {
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


}
