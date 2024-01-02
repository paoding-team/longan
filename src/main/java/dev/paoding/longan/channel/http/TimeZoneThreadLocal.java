package dev.paoding.longan.channel.http;

public class TimeZoneThreadLocal {
    private final static ThreadLocal<String> timeZoneThreadLocal = new ThreadLocal<>();

    public static void put(String timeZone) {
        timeZoneThreadLocal.set(timeZone);
    }

    public static String get() {
        String timeZone = timeZoneThreadLocal.get();
        if (timeZone == null) {
            return "default";
        }
        return timeZone;
    }

    public static void remove() {
        timeZoneThreadLocal.remove();
    }
}
