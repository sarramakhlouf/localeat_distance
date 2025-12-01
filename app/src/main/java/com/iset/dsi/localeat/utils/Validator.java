package com.iset.dsi.localeat.utils;

public class Validator {

    public static boolean isEmailValid(String email) {
        return email != null &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isPasswordValid(String pwd) {
        return pwd != null && pwd.length() >= 6;
    }

    public static boolean isFieldNotEmpty(String field) {
        return field != null && !field.trim().isEmpty();
    }
}
