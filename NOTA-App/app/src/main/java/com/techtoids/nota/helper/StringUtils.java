package com.techtoids.nota.helper;

public class StringUtils {
    public static final String EMPTY = "";

    private StringUtils() {
    }

    public static boolean isNotNullOrEmpty(String string) {
        return string != null && string.isEmpty() == false;
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }
}
