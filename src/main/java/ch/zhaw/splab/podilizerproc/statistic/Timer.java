package ch.zhaw.splab.podilizerproc.statistic;

import java.util.concurrent.TimeUnit;

public class Timer {
    private static long startTime;
    private static long endTime;
    private static final String defaultFormat = "%02d min, %02d.%03d sec";

    public static void start(){
        startTime = System.currentTimeMillis();
    }
    public static void stop(){
        endTime = System.currentTimeMillis();
    }
    public static long getTime(){
        return endTime - startTime;
    }

    public static String getFormattedTime(String format, long time){
        long millis = time;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes);
        long milliseconds = millis - TimeUnit.MINUTES.toMillis(minutes) - TimeUnit.SECONDS.toMillis(seconds);
        return String.format(format, minutes,
                seconds, milliseconds);
    }
    public static String getFormatedTime(){
        long millis = getTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes);
        long milliseconds = millis - TimeUnit.MINUTES.toMillis(minutes) - TimeUnit.SECONDS.toMillis(seconds);
        return String.format(defaultFormat, minutes,
                seconds, milliseconds);
    }
}

