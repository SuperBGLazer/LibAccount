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

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import com.ninjamodding.LibAccount.exceptions.AccountAlreadyExistException;
import com.ninjamodding.LibAccount.utils.DatabaseUtil;
import com.ninjamodding.LibAccount.utils.EmailUtil;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.UUID;

public class AccountDAO {
    private static AccountDAO instance;
    private static Connection connection;
    private static EmailUtil email;
    private static DatabaseUtil database;

    private static final String CREATE_ACCOUNT_TABLE =
            "CREATE TABLE IF NOT EXISTS `accounts` (\n" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `firstName` varchar(255) DEFAULT NULL,\n" +
                    "  `lastName` varchar(255) DEFAULT NULL,\n" +
                    "  `email` varchar(255) NOT NULL,\n" +
                    "  `password` varchar(255) DEFAULT NULL,\n" +
                    "  `activated` tinyint(1) DEFAULT NULL,\n" +
                    "  `developer` tinyint(1) DEFAULT '0',\n" +
                    "  PRIMARY KEY (`id`),\n" +
                    "  UNIQUE KEY `accounts_email_uindex` (`email`),\n" +
                    "  KEY `id` (`id`)\n" +
                    ") ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1\n";

    private static final String CREATE_TOKEN_TABLE =
            "CREATE TABLE IF NOT EXISTS `tokens` (\n" +
                    "                          `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "                          `accountID` int(11) NOT NULL,\n" +
                    "                          `token` varchar(255) NOT NULL,\n" +
                    "                          PRIMARY KEY (`id`),\n" +
                    "                          KEY `tokens_ibfk_1` (`accountID`),\n" +
                    "                          CONSTRAINT `tokens_ibfk_1` FOREIGN KEY (`accountID`) REFERENCES `accounts` (`id`) ON DELETE CASCADE ON UPDATE CASCADE\n" +
                    ") ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=latin1\n";

    @Deprecated
    public AccountDAO(Connection databaseConnection, DatabaseUtil databaseUtil, EmailUtil emailUtil) {
        if (databaseConnection == null) {
            try {
                connect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            connection = databaseConnection;
        }
        email = emailUtil;
        database = databaseUtil;
    }

    /**
     * @param databaseConnection The connection to the MySQL database
     * @param emailUtil          The email utility class that will be used to send email regarding account information
     * @param databaseUtil       The database utility is used to restore a database connection if LibAccount lost connection.
     * @return The AccountDAO object
     */
    public static AccountDAO setup(Connection databaseConnection, EmailUtil emailUtil, DatabaseUtil databaseUtil) {
        instance = new AccountDAO(databaseConnection, databaseUtil, emailUtil);
        return instance;
    }

    /**
     *
     * @return The current instance of AccountDAO
     */
    public static AccountDAO getInstance() {
        return instance;
    }

    /**
     * This will connect to the database
     * @throws SQLException If something goes wrong while connecting
     */
    private void connect() throws SQLException {
        connection = database.connectToMain(connection);
    }

    /**
     *
     * @param credentials User credentials
     * @return A account if the authentaction was successful. Returns null if the account doesn't exist or if the password was wrong
     */
    public UserAccount authenticateUser(Credentials credentials) {
        String sql = String.format("SELECT * FROM accounts WHERE email='%s'", email);
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String firstName = resultSet.getString("firstName");
                String lastName = resultSet.getString("lastName");
                String databasePassword = resultSet.getString("password");
                boolean accountActivated = resultSet.getBoolean("activated");
                int id = resultSet.getInt("id");
                String token = UUID.randomUUID().toString();
                String pubToken = UUID.randomUUID().toString();


                if (Password.check(credentials.getPassword(), databasePassword) && accountActivated) {
                    return new UserAccount(firstName, lastName, credentials.getEmail(), token, pubToken, id);
                }
            }
            statement.close();
        } catch (CommunicationsException | NullPointerException e) {
            try {
                connect();
                return authenticateUser(credentials);
            } catch (SQLException | NullPointerException ex) {
                ex.printStackTrace();
            }
            return authenticateUser(credentials);
        } catch (SQLSyntaxErrorException e) {
            createTable();
            return authenticateUser(credentials);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | SQLException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     *
     * @param id Account id
     * @return A account if it exist.
     */
    public UserAccount getUser(int id) {
        try {
            Statement statement = connection.createStatement();

            String selectAccountSQL = String.format("SELECT * FROM accounts WHERE id=%s", Integer.toString(id));
            return getAccount(statement, selectAccountSQL);

        } catch (CommunicationsException | NullPointerException e) {
            try {
                connect();
                return getUser(id);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return getUser(id);
        } catch (SQLSyntaxErrorException e) {
            createTable();
            return getUser(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param email User email
     * @return A account if it exist
     */
    public UserAccount getUser(String email) {
        try {
            Statement statement = connection.createStatement();

            String selectAccountSQL = String.format("SELECT * FROM accounts WHERE email='%s'", email);
            return getAccount(statement, selectAccountSQL);

        } catch (CommunicationsException | NullPointerException e) {
            try {
                connect();
                return getUser(email);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return getUser(email);
        } catch (SQLSyntaxErrorException e) {
            createTable();
            return getUser(email);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param statement The database statement used to select the account
     * @param selectAccountSQL The SQL syntax used to select the account
     * @return A account if it exist
     * @throws SQLException If something went wrong while selecting the account
     */
    private UserAccount getAccount(Statement statement, String selectAccountSQL) throws SQLException {
        ResultSet resultSet = statement.executeQuery(selectAccountSQL);

        if (resultSet.next()) {
            UserAccount userAccount = new UserAccount(resultSet.getString("firstName"),
                    resultSet.getString("lastName"), resultSet.getString("email"),
                    resultSet.getInt("id"));
            statement.close();
            return userAccount;
        } else {
            statement.close();
            return null;
        }
    }

    /**
     *
     * @param credentials User credentials
     * @param firstName User first name
     * @param lastName User last name
     * @return A account if it was successfully created
     * @throws AccountAlreadyExistException
     */
    public UserAccount createUser(Credentials credentials, String firstName, String lastName) throws AccountAlreadyExistException {
        try {
            Statement statement = connection.createStatement();
            String securePassword = Password.getSaltedHash(credentials.getPassword());

            // Add the account to the database
            String addAccountSQL = String.format("INSERT INTO accounts (email, password, firstName, lastName, activated) " +
                    "VALUE ('%s', '%s', '%s', '%s', FALSE)", credentials.getEmail(), securePassword, firstName, lastName);
            statement.execute(addAccountSQL);

            // Get the account id
            int accountID = getID(credentials.getEmail());
            if (accountID == -1) {
                return null;
            }

            // Create the account token
            String token = UUID.randomUUID().toString();
            String addTokenSQL = String.format("INSERT INTO tokens (accountID, token) VALUES " +
                    "(%s, '%s')", Integer.toString(accountID), token);
            statement.execute(addTokenSQL);
            UserAccount userAccount = new UserAccount(firstName, lastName, credentials.getEmail(), token, "", -1);
            email.sendVerifyEmail(userAccount);
            statement.close();

            return userAccount;
        } catch (CommunicationsException | NullPointerException e) {
            try {
                connect();
                return createUser(credentials, firstName, lastName);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return createUser(credentials, firstName, lastName);
        } catch (SQLIntegrityConstraintViolationException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                throw new AccountAlreadyExistException("Account with the email " + credentials.getEmail() + " already exist!");
            }
        } catch (SQLSyntaxErrorException e) {
            createTable();
            return createUser(credentials, firstName, lastName);
        } catch (SQLException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * This will activate the account
     * @param token the token that was sent in the email
     * @return True if the account was activated
     */
    public boolean activateAccount(String token) {
        try {
            Statement statement = connection.createStatement();

            // Load token
            String getTokenSQL = String.format("SELECT * FROM tokens WHERE token='%s'", token);
            ResultSet resultSet = statement.executeQuery(getTokenSQL);

            if (resultSet.next()) {
                // Get the account ID
                int accountID = resultSet.getInt("accountID");
                // Delete the token
                String deleteTokenSQL = String.format("DELETE FROM tokens WHERE token='%s'", token);
                statement.execute(deleteTokenSQL);

                String updateAccountSQL = String.format("UPDATE accounts SET activated=TRUE WHERE id=%s",
                        Integer.toString(accountID));
                statement.execute(updateAccountSQL);
                statement.close();
                return true;

            }
            statement.close();
        } catch (CommunicationsException | NullPointerException e) {
            try {
                connect();
                return activateAccount(token);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return activateAccount(token);
        } catch (SQLSyntaxErrorException e) {
            createTable();
            activateAccount(token);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     *
     * @param email User email
     * @return The account id
     */
    private int getID(String email) {
        try {
            Statement statement = connection.createStatement();
            String sql = String.format("SELECT id FROM accounts WHERE email='%s'", email);
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
            statement.close();
        } catch (CommunicationsException | NullPointerException e) {
            try {
                connect();
                return getID(email);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return getID(email);
        } catch (SQLSyntaxErrorException e) {
            createTable();
            return getID(email);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Creates a new account table
     */
    void createTable() {
        try {
            Statement statement = connection.createStatement();
            statement.execute(CREATE_ACCOUNT_TABLE);
            statement.execute(CREATE_TOKEN_TABLE);
            statement.close();
        } catch (CommunicationsException | NullPointerException e) {
            try {
                connect();
                createTable();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}