package com.ninjamodding.LibAccount;

import com.ninjamodding.LibAccount.utils.DatabaseUtil;
import com.ninjamodding.LibAccount.utils.EmailUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

class TestAccountDAO {
    private Connection database;
    private EmailUtil emailUtil;
    private DatabaseUtil databaseUtil;

    @BeforeEach
    void setUp() {
        emailUtil = account -> {
        };
        databaseUtil = new DatabaseUtilTest();
    }

    @Test
    void authenticateUser() {
        try {
            AccountDAO.setup(databaseUtil.connectToMain(database), emailUtil, databaseUtil);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        assert AccountDAO.getInstance() != null;
        AccountDAO.getInstance().authenticateUser("testing@example.com", "testing", "");
    }
}