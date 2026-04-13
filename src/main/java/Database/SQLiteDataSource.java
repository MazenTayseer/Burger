package Database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteDataSource {
    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource ds;

    static {

        config.setJdbcUrl("...");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(config);

    }

    private SQLiteDataSource() { }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}