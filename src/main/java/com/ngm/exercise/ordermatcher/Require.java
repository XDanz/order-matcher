package com.ngm.exercise.ordermatcher;

public final class Require {

    private Require() {
    }

    public static <T> T notNull(T obj, String msg) {
        if (obj == null) {
            throw new IllegalArgumentException(String.format("%s cannot be null", msg));
        }
        return obj;
    }

    public static void that(boolean condition, String msg) {
        if (!condition) {
            throw new IllegalArgumentException(msg);
        }
    }

}
