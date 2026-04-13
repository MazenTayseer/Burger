package Bot;

import Commands.CommandContext;
import Commands.ICommand;
import Music.GuildMusicManager;
import Music.PlayerManager;
import com.fasterxml.jackson.databind.JsonNode;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.concurrent.TimeUnit;


@SuppressWarnings("DuplicatedCode")
public class MemeCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();

    @Override
    public void handle(CommandContext ctx) {
        final GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(ctx.getGuild());
        if (musicManager.trackScheduler.available) {
            TextChannel channel = ctx.getChannel();
            WebUtils.ins.getJSONObject("https://apis.duncte123.me/meme").async((json) -> {
                if (!json.get("success").asBoolean()) {
                    embed.setTitle("❌ Oops");
                    embed.setDescription("An error has occurred, try again later.");
                    embed.setColor(0xdd2e44);
                    channel.sendMessage(embed.build()).queue();
                    embed.clear();
                    return;
                }

                final JsonNode data = json.get("data");
                final String title = data.get("title").asText();
                final String url = data.get("url").asText();
                final String image = data.get("image").asText();
                final EmbedBuilder embed = EmbedUtils.embedImageWithTitle(title, url, image);

                EmbedBuilder embedBuilder = new EmbedBuilder();
                Button buttonDisabled = Button.success("meme", "Next meme").asDisabled();
                Button button1Disabled = Button.secondary("end", "End").asDisabled();

                embedBuilder.setTitle("✅  Interaction stopped!");
                embedBuilder.setColor(0x77b255);

                Button button = Button.success("meme", "Next meme");
                Button button1 = Button.secondary("end", "End");
                ctx.getChannel().sendMessageEmbeds(embed.build()).setActionRows(ActionRow.of(button, button1))
                        .queue(e -> e.editMessageEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(buttonDisabled,button1Disabled)).queueAfter(90, TimeUnit.SECONDS));
                embed.clear();
            });

            musicManager.trackScheduler.available = false;
        }
    }


    @Override
    public String getName() {
        return "meme";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Meme");
        embed.setDescription("Sends a random meme.");
        embed.addField("Usage","**`-meme`**",true);
        embed.setColor(0xffffff);
        return embed;
    }
}
