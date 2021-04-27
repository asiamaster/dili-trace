package com.dili.trace.routing;

public class RoutingContextHolder {
    private static final ThreadLocal<String> tanentLocal = new ThreadLocal<>();

    public static void put(String tanent) {
        tanentLocal.set(tanent);
    }

    public static String get() {
        return tanentLocal.get();
    }

    public static void remove() {
        tanentLocal.remove();
    }
}
