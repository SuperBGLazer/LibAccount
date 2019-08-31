package com.ninjamodding.LibAccount;

import com.mysql.cj.jdbc.MysqlDataSource;
import com.ninjamodding.LibAccount.utils.DatabaseUtil;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseUtilTest implements DatabaseUtil {
    @Override
    public Connection connectToMain(Connection nullConnection) throws SQLException {
        try {
            MysqlDataSource dataSource = new MysqlDataSource();
            dataSource.setUser(System.getenv("user"));
            dataSource.setPassword(System.getenv("password"));
            dataSource.setServerName(System.getenv("ip"));
            dataSource.setDatabaseName(System.getenv("database"));
            dataSource.setReadOnlyPropagatesToServer(false);
            dataSource.setServerTimezone("CST6CDT");
            nullConnection = dataSource.getConnection();
            assert nullConnection != null;
            return nullConnection;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
        return null;
    }
}
