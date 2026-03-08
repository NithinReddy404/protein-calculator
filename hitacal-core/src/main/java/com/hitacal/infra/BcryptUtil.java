package com.hitacal.infra;

import org.mindrot.jbcrypt.BCrypt;

public class BcryptUtil {
    private BcryptUtil() {}

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    public static boolean verify(String plainPassword, String hash) {
        if (plainPassword == null || hash == null) return false;
        return BCrypt.checkpw(plainPassword, hash);
    }
}
