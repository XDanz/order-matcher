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
public class Order2 {
    private long id;
    private Side side;
    private long price;
    private long quantity;

//    /**
//     * Create a new order.
//     * @param id the client assigned id.
//     * @param side the side (buy/sell), not null.
//     * @param price the price, must be &gt;= 0.
//     * @param quantity the quantity, must be &gt;= 0.
//     */
//    public Order2(long id, Side side, long price, long quantity) {
//        this.id = id;
//        this.side = Objects.requireNonNull(side);
//        if (price < 0) {
//            throw new IllegalArgumentException("price must be >= 0");
//        }
//        this.price = price;
//        if (quantity <= 0) {
//            throw new IllegalArgumentException("quantity must be > 0");
//        }
//        this.quantity = quantity;
//    }

    private Order2(final BuyOrderBuilder builder) {
        validate(builder);
        set(builder);
        this.side = builder.side;
    }

    private void validate(final Builder<?,?> builder) {
        Require.that(builder.price >= 0, "price must be >=0");
        Require.that(builder.qty >0, "qty must be > 0");
    }

    public Order2(final SellOrderBuilder builder) {
        validate(builder);
        set(builder);
        this.side = builder.side;
    }

    private void set(final Builder<?,?> builder) {
        this.id = builder.id;
        this.price = builder.price;
        this.quantity = builder.qty;
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

    public abstract static class Builder<B extends Builder<B, T>, T> {
        protected long id;
        protected long price;
        protected long qty;
        protected abstract B self();
        public abstract T build();
        public B id(final long id) {
            this.id = id;
            return self();
        }

        public B price(final long price) {
            this.price = price;
            return self();
        }

        public B qty(final long qty) {
            this.qty = qty;
            return self();
        }
    }

    public static class BuyOrderBuilder extends Builder<BuyOrderBuilder,Order2> {
        final Side side = Side.BUY;

        @Override
        protected BuyOrderBuilder self() {
            return this;
        }

        @Override
        public Order2 build() {
            return new Order2(this);
        }
    }

    public static class SellOrderBuilder extends Builder<SellOrderBuilder,Order2> {
        final Side side = Side.SELL;

        @Override
        protected SellOrderBuilder self() {
            return this;
        }

        @Override
        public Order2 build() {
            return new Order2(this);
        }
    }

    public static SellOrderBuilder sellOrder() {
        return new SellOrderBuilder();
    }

    public static BuyOrderBuilder buyOrder() {
        return new BuyOrderBuilder();
    }


    @Override
    public String toString() {
        return side + " " + quantity + "@" + price + " #" + id;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Order2 order = (Order2) o;
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
