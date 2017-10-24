package com.cinnober.exercise.ordermatcher;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IceBergOrder extends Order {
    private static final Pattern PATTERN =
            Pattern.compile("(?<side>([bB][uU][yY])|([sS][eE][lL][lL]))[ ]+(?<dqty>[0-9]+)\\/+(?<qty>[0-9]+)+[ ]*@[ ]*(?<px>[0-9]+)([ ]+#(?<id>[0-9]+))?");

    protected static final String GROUP_DISPLAY_QUANTITY = "dqty";
    private long displayQuantity;
    /**
     * Create a new order.
     *
     * @param id       the client assigned id.
     * @param side     the side (buy/sell), not null.
     * @param price    the price, must be &gt;= 0.
     * @param quantity the quantity, must be &gt;= 0.
     */
    public IceBergOrder(long id, Side side, long price, long quantity, long displayQuantity) {
        super(id, side, price, quantity);
        if (quantity <= 0) {
            throw new IllegalArgumentException("display quantity must be > 0");
        }
        this.displayQuantity = Objects.requireNonNull(displayQuantity);
    }

    public long getDisplayQuantity() {
        return displayQuantity;
    }

    public static IceBergOrder parse(String str) {
        Matcher m = PATTERN.matcher(str);
        if (!m.matches()) {
            throw new IllegalArgumentException("Illegal order format. Expected #id buy|sell display/quantity@price");
        }
        String idStr = m.group(GROUP_ID);
        long id = idStr != null ? Long.valueOf(idStr) : 0L;
        Side side = Side.valueOf(m.group(GROUP_SIDE).toUpperCase());
        long price = Long.valueOf(m.group(GROUP_PRICE));
        long quantity = Long.valueOf(m.group(GROUP_QUANTITY));

        long displayQuantity = Long.valueOf(m.group(GROUP_DISPLAY_QUANTITY));

        return new IceBergOrder(id, side, price, quantity, displayQuantity);
    }

    @Override
    public String toString() {
        return getSide() + " " + displayQuantity + "/" + getQuantity() + "@" + getPrice() + " #" + getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        IceBergOrder that = (IceBergOrder) o;

        return displayQuantity == that.displayQuantity;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (displayQuantity ^ (displayQuantity >>> 32));
        return result;
    }
}
