package Bot;

import Commands.CommandContext;
import Commands.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class DeleteCommand implements ICommand {
    EmbedBuilder embed = new EmbedBuilder();
    @Override
    public void handle(CommandContext ctx) {
        String[] args = ctx.getMessage().getContentRaw().split(" ");
        if(args.length != 2){
            embed.setTitle("🍔 Clear Instruction");
            embed.setDescription("Usage: **-Clear** `number of messages`");
            embed.setColor(0xB00808);
            ctx.getChannel().sendMessage(embed.build()).queue();
            embed.clear();
            return;
        }

        if(!isNumber(args[1])){
            embed.setTitle("🍔 Clear Instruction");
            embed.setDescription("Usage: **-Clear** `number of messages`");
            embed.setColor(0xB00808);
            ctx.getChannel().sendMessage(embed.build()).queue();
            embed.clear();
            return;
        }

        try{
            List<Message> messageList = ctx.getChannel().getHistory()
                    .retrievePast(Integer.parseInt(args[1]) + 1).complete();
            ctx.getChannel().deleteMessages(messageList).queue();
        } catch(IllegalArgumentException e){

            if(args[1].equals("0")){
                embed.setTitle("❌ Oops!");
                embed.setDescription("You silly, I cannot delete 0 messages.");
                embed.setColor(0xB00808);
                ctx.getChannel().sendMessage(embed.build()).queue();
                embed.clear();
            }
            else{
                embed.setTitle("❌ Oops!");
                embed.setDescription("I can't delete more than 99 messages, I know I'm not that great");
                embed.setColor(0xB00808);
                ctx.getChannel().sendMessage(embed.build()).queue();
                embed.clear();
            }

        }
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public EmbedBuilder getHelp() {
        embed.setTitle("Delete");
        embed.setDescription("Deletes past messages in a text channel.");
        embed.addField("Usage","**`-delete`** `Number of messages`",true);
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
}
