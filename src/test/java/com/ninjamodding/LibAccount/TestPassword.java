package com.ninjamodding.LibAccount;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.*;

class TestPassword {

    @Test
    void getSaltedHash() {
        try {
            String password = Password.getSaltedHash("1234");
            assert password != null;
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Test
    void check() {
        try {
            assert Password.check("1234",
                    "+EYC8IYXhyEBQO7HJZK32/hMQ0HrUbZVJp7N3/L6sKI=$P4qtgUjNtedFUir35UW5uHxg9kw4Jmb650W2erz37z4=");
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}