package Bot;

import Commands.Config;
import Commands.Listener;
import Database.SQLiteDataSource;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;
import java.sql.SQLException;

public class Main {
    public static JDA jda;

    public static void main(String[] args) throws LoginException, SQLException {
        SQLiteDataSource.getConnection();

        EventWaiter waiter = new EventWaiter();

        jda = JDABuilder.createDefault(Config.get("TOKEN"))
                .setActivity(Activity.playing("-help"))
                .addEventListeners(new Listener(waiter), waiter)
                .build();

        jda.getPresence().setStatus(OnlineStatus.ONLINE);


    }
}

