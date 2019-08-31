package com.ninjamodding.LibAccount.utils;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseUtil {
    Connection connectToMain(Connection nullConnection) throws SQLException;
}
