package com.ninjamodding.LibAccount.exceptions;

public class DatabaseException extends Exception {
    private String message;

    public DatabaseException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Database error! " + message;
    }
}
