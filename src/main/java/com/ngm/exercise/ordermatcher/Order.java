package com.ngm.exercise.ordermatcher;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.hash;

/**
 * Limit order.
 *
 * <p>An order has a side (buy/sell), quantity and price. For example: buy 10 units @ 5 SEK each (or better price).
 */
public class Order {
    private static final Pattern PATTERN =
            Pattern.compile("(?<side>([bB][uU][yY])|([sS][eE][lL][lL]))[ ]+(?<qty>[0-9]+)[ ]*@[ ]*(?<px>[0-9]+)([ ]+#(?<id>[0-9]+))?");
    private static final String GROUP_ID = "id";
    private static final String GROUP_SIDE = "side";
    private static final String GROUP_QUANTITY = "qty";
    private static final String GROUP_PRICE = "px";

    private final long id;
    private final Side side;
    private final long price;
    private long quantity;

    /**
     * Create a new order.
     * @param id the client assigned id.
     * @param side the side (buy/sell), not null.
     * @param price the price, must be &gt;= 0.
     * @param quantity the quantity, must be &gt;= 0.
     */
    public Order(long id, Side side, long price, long quantity) {
        this.id = id;
        this.side = Objects.requireNonNull(side);
        if (price < 0) {
            throw new IllegalArgumentException("price must be >= 0");
        }
        this.price = price;
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
        this.quantity = quantity;
    }

    /**
     * Returns the client assigned order id.
     * @return the client assigned order id.
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the side.
     * @return the side, not null.
     */
    public Side getSide() {
        return side;
    }

    /**
     * Returns the price.
     * @return the price.
     */
    public long getPrice() {
        return price;
    }

    /**
     * Returns the quantity.
     * @return the quantity.
     */
    public long getQuantity() {
        return quantity;
    }

    /**
     * Set the quantity.
     * @param quantity the new quantity, must be &gt;= 0.
     */
    public void setQuantity(long quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity must be >= 0");
        }
        this.quantity = quantity;
    }

    /**
     * Returns true if the quantity is zero.
     * @return true if the quantity is zero, otherwise false.
     */
    public boolean isEmpty() {
        return quantity == 0;
    }

    @Override
    public String toString() {
        return side + " " + quantity + "@" + price + " #" + id;
    }

    public static Order parse(String str) {
        final Matcher m = PATTERN.matcher(str);
        if (!m.matches()) {
            throw new IllegalArgumentException("Illegal order format. Expected #id buy|sell quantity@price");
        }
        final String idStr = m.group(GROUP_ID);
        final long id = idStr != null ? Long.parseLong(idStr) : 0L;
        final Side side = Side.valueOf(m.group(GROUP_SIDE).toUpperCase());
        final long price = Long.parseLong(m.group(GROUP_PRICE));
        final long quantity = Long.parseLong(m.group(GROUP_QUANTITY));

        return new Order(id, side, price, quantity);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Order order = (Order) o;
        return id == order.id &&
            price == order.price &&
            quantity == order.quantity &&
            side == order.side;
    }

    @Override
    public int hashCode() {
        return hash(id, side, price, quantity);
    }
}
