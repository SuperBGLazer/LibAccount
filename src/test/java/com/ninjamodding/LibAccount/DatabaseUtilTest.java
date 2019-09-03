/*
 *    Copyright 2019 Breyon Gunn
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
            dataSource.setAutoReconnect(true);
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
//        Connection connection = DriverManager.getConnection(
//                String.format("jdbc:mariadb://%s:3306/%s?user=%s&password=%s", System.getenv("ip"),
//                        System.getenv("database"), System.getenv("user"),
//                        System.getenv("password")));
//        return connection;
        return nullConnection;
    }
}
