/*
 * Copyright (c) 2019 Breyon Gunn.
 */

package com.ninjamodding.LibAccount;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import com.ninjamodding.LibAccount.utils.DatabaseUtil;
import com.ninjamodding.LibAccount.utils.EmailUtil;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class AccountDAO {
    private static AccountDAO instance;
    private static Connection connection;
    private static EmailUtil email;
    private static DatabaseUtil database;

    public static AccountDAO setup(Connection databaseConnection, EmailUtil emailUtil, DatabaseUtil databaseUtil) {
        instance = new AccountDAO(databaseConnection, databaseUtil, emailUtil);
        return instance;
    }

    public static AccountDAO getInstance() {
        return instance;
    }

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

    private void connect() throws SQLException {
        connection = database.connectToMain(connection);
    }

    public Account authenticateUser(String email, String password, String ip) {
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
//                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//                Date date = new Date();
//
//                String addTokenSQL = "INSERT INTO tokens (accountID, token, loginToken, pub_token, date, ip) " +
//                        String.format("VALUES (%s,'%s', TRUE, '%s', '%s', '%s')", id, token, pubToken,
//                                dateFormat.format(date), ip);
//                statement.execute(addTokenSQL);


                if (Password.check(password, databasePassword) && accountActivated) {
                    return new Account(firstName, lastName, email, token, pubToken, id);
                }
            }
            statement.close();
        } catch (CommunicationsException | NullPointerException e) {
            try {
                connect();
                return authenticateUser(email, password, ip);
            } catch (SQLException | NullPointerException ex) {
                ex.printStackTrace();
            }
            return authenticateUser(email, password, ip);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | SQLException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public Account getUser(int id) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Account getUser(String email) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Account getAccount(Statement statement, String selectAccountSQL) throws SQLException {
        ResultSet resultSet = statement.executeQuery(selectAccountSQL);

        if (resultSet.next()) {
            Account account = new Account(resultSet.getString("firstName"),
                    resultSet.getString("lastName"), resultSet.getString("email"),
                    resultSet.getInt("id"));
            statement.close();
            return account;
        } else {
            statement.close();
            return null;
        }
    }

    public Account createUser(String userEmail, String password, String firstName, String lastName) {
        try {
            Statement statement = connection.createStatement();
            String securePassword = Password.getSaltedHash(password);

            // Add the account to the database
            String addAccountSQL = String.format("INSERT INTO accounts (email, password, firstName, lastName, activated) " +
                    "VALUE ('%s', '%s', '%s', '%s', FALSE)", email, securePassword, firstName, lastName);
            statement.execute(addAccountSQL);

            // Get the account id
            int accountID = getID(userEmail);
            if (accountID == -1) {
                return null;
            }

            // Create the account token
            String token = UUID.randomUUID().toString();
            String addTokenSQL = String.format("INSERT INTO tokens (accountID, token, loginToken) VALUES " +
                    "(%s, '%s', FALSE )", Integer.toString(accountID), token);
            statement.execute(addTokenSQL);
            Account account = new Account(firstName, lastName, userEmail, token, "", -1);
            email.sendVerifyEmail(account);
            statement.close();

            return account;
        } catch (CommunicationsException | NullPointerException e) {
            try {
                connect();
                return createUser(userEmail, password, firstName, lastName);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return createUser(userEmail, password, firstName, lastName);
        } catch (SQLException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int getID(String email) {
        try {
            Statement statement = connection.createStatement();
            String sql = String.format("SELECT id FROM accounts WHERE email='%s'", email);
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                statement.close();
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

}