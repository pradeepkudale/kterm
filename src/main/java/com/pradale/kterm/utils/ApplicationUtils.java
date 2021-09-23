package com.pradale.kterm.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class ApplicationUtils {

    public static final Character[] INVALID_WINDOWS_SPECIFIC_CHARS = {'"', '*', ':', '<', '>', '?', '\\', '|', 0x7F};
    public static final Character[] INVALID_UNIX_SPECIFIC_CHARS = {'\000'};
    public static final String FILE_NAME_REGEX_PATTERN = "^[A-za-z0-9._-]{1,255}$";

    public static String randomId(String prefix) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdds");
        return String.format("%s-%s%s", prefix, sdf.format(new Date()), new Random().nextInt(10));
    }

    public static boolean validateFileName(String filename) {
        if (StringUtils.isBlank(filename)) {
            return false;
        }
        return filename.matches(FILE_NAME_REGEX_PATTERN);
    }

    public static Character[] getInvalidCharsByOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return INVALID_WINDOWS_SPECIFIC_CHARS;
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            return INVALID_UNIX_SPECIFIC_CHARS;
        } else {
            return new Character[]{};
        }
    }
}