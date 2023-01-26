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

        config.setJdbcUrl("jdbc:postgresql://ec2-34-251-115-141.eu-west-1.compute.amazonaws.com:5432/d6efktd567teoc?password=b527307bda2ecf17ee01b6ae442bc3a77f43c6c857fdbe458102af8e1c545848&sslmode=require&user=mscavspcihtgbh");
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