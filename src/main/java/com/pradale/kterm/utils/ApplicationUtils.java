package com.pradale.kterm.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class ApplicationUtils {

    public static String randomId(String prefix) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdds");
        return String.format("%s-%s%s", prefix, sdf.format(new Date()), new Random().nextInt(10));
    }
}