package com.socketble.util.Logger;


import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by e573227 on 22/06/2017.
 */

public class CurrentTime {
    public static String get() {
        SimpleDateFormat formatter = null;
        formatter = new SimpleDateFormat("HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate) + "  ";
    }
}
