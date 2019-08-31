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