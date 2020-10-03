package com.ngm.exercise.ordermatcher;

import java.util.function.Supplier;

/**
 * @author Roger Forsberg
 * @since 0.0.1
 */
public final class Require {

    private Require() {
    }

    /**
     * Checks that obj is not null.
     *
     * @param obj instance that will be checked for null
     * @param msg message that will be set in {@link NullPointerException} if obj is null
     * @return parameter obj if not null
     * @throws NullPointerException if obj is null
     */
    public static <T> T notNull(T obj, String msg) {
        if (obj == null) {
            throw new IllegalArgumentException(String.format("%s cannot be null", msg));
        }
        return obj;
    }

    public static <T> T notNull(T obj, String msg, Object... parameters) {
        if (obj == null) {
            throw new IllegalArgumentException(String.format(msg, parameters));
        }
        return obj;
    }

    public static void onlyOneIsNotNull(String msg, Object... objects) {
        int counter = 0;
        for (Object obj : objects) {
            counter += obj != null ? 1 : 0;
        }

        if (counter != 1) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static <T> T notNull(T obj, String msg, boolean template) {
        if (!template && obj == null) {
            throw new IllegalArgumentException(String.format("%s cannot be null", msg));
        }
        return obj;
    }


    public static <T> T isInstance(Object obj, Class<T> clazz, String msg) {
        if (!clazz.isInstance(obj)) {
            throw new IllegalArgumentException(String.format("%s is not an instance of %s, but and instance of %s", msg, clazz.getSimpleName(), obj.getClass().getSimpleName()));
        }
        return clazz.cast(obj);
    }

    public static void that(boolean condition, String msg) {
        if (!condition) {
            throw new IllegalArgumentException(msg);
        }
    }


    public static void that(boolean condition, Supplier<String> msg) {
        if (!condition) {
            throw new IllegalArgumentException(msg.get());
        }
    }

    public static void thatOr(boolean condition1, boolean condition2, String msg) {

    }

    public static void that(boolean condition, String msg, Object... parameters) {
        if (!condition) {
            throw new IllegalArgumentException(String.format(msg, parameters));
        }
    }
}
