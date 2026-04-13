package MusicCommands;

import Commands.CommandContext;
import Commands.ICommand;
import core.GLA;
import net.dv8tion.jda.api.EmbedBuilder;

import java.io.IOException;

public class LyricsCommand implements ICommand {
    private final EmbedBuilder embed = new EmbedBuilder();
    @Override
    public void handle(CommandContext ctx) {
        String[] args = ctx.getMessage().getContentRaw().split(" ");
        StringBuilder songTitle = new StringBuilder();

        for(int i=1;i<args.length;i++) {
            songTitle.append(args[i]).append(" ");
        }



        if(args.length < 2){
            embed.setTitle("❌ Oops");
            embed.setDescription("Usage:  **`-lyrics`** `song name`");
            embed.setColor(0xdd2e44);
            ctx.getChannel().sendMessageEmbeds(embed.build()).queue();
            embed.clear();
            return;
        }

        try{
            GLA gla = new GLA();
            String lyrics = gla.search(String.valueOf(songTitle)).getHits().get(0).fetchLyrics();
            lyrics = lyrics.replace("&amp;","&");
            lyrics = lyrics.replace("[","**[");
            lyrics = lyrics.replace("]","]**");

            int lyricsLength = lyrics.length();

            embed.setTitle("`" + gla.search(String.valueOf(songTitle)).getHits().get(0).getTitle() + "` Lyrics");
            embed.setDescription("By `" + gla.search(String.valueOf(songTitle)).getHits().get(0).getArtist().getName() + "`");
            embed.setThumbnail(gla.search(String.valueOf(songTitle)).getHits().get(0).getThumbnailUrl());
            embed.setColor(0x9370DB);

            int i = 0;
            int j = 1023;

            while(lyricsLength > 0) {
                embed.addField("", lyrics.substring(i, j), false);

                i+= 1023;
                j+= 1023;

                if(j > lyrics.length()) {
                    j = lyrics.length();
                }

                lyricsLength -= 1024;

            }

            ctx.getChannel().sendMessageEmbeds(embed.build()).queue();
            embed.clear();

        } catch (Exception e) {
            embed.clear();
            embed.setTitle("❌ Oops");
            embed.setDescription("**Song `" + args[1] + "` cannot be found, " +
                    "or I cannot display its lyrics as I cannot display more than 6000 character.**");
            embed.setColor(0xdd2e44);
            ctx.getChannel().sendMessageEmbeds(embed.build()).queue();
            embed.clear();
        }


    }

    @Override
    public String getName() {
        return "lyrics";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("lyrics");
        embed.setDescription("Shows the lyrics to a song.");
        embed.addField("Usage","**`-remove`** `song name`",true);
        embed.setColor(0xffffff);
        return embed;
    }
}
