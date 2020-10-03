package com.ngm.exercise.ordermatcher;

import static java.util.Objects.hash;

public class Order {
    private final Side side;
    private long price;
    private long qty;

    private Order(final BuyOrderBuilder builder) {
        validate(builder);
        set(builder);
        this.side = builder.side;
    }

    public Order(final Builder builder) {
        validate(builder);
        set(builder);
        this.side = Require.notNull(builder.side, "side");
    }

    private void validate(final AbstractBuilder<?, ?> builder) {
        Require.that(builder.price >= 0, "price must be >=0");
        Require.that(builder.qty > 0, "qty must be > 0");
    }

    private Order(final SellOrderBuilder builder) {
        validate(builder);
        set(builder);
        this.side = builder.side;
    }

    private void set(final AbstractBuilder<?, ?> builder) {
        //this.id = builder.id;
        this.price = builder.price;
        this.qty = builder.qty;
    }

    public Side getSide() {
        return side;
    }

    public long getPrice() {
        return price;
    }

    public long getQty() {
        return qty;
    }

    public void setQty(long qty) {
        Require.that(qty >= 0, "quantity must be >= 0");
        this.qty = qty;
    }

    public abstract static class AbstractBuilder<B extends AbstractBuilder<B, T>, T> {
        protected long price;
        protected long qty;

        protected abstract B self();

        public abstract T build();

        public B price(final long price) {
            this.price = price;
            return self();
        }

        public B qty(final long qty) {
            this.qty = qty;
            return self();
        }
    }

    public static class BuyOrderBuilder extends AbstractBuilder<BuyOrderBuilder, Order> {
        final Side side = Side.BUY;

        @Override
        protected BuyOrderBuilder self() {
            return this;
        }

        @Override
        public Order build() {
            return new Order(this);
        }
    }

    public static class SellOrderBuilder extends AbstractBuilder<SellOrderBuilder, Order> {
        final Side side = Side.SELL;

        @Override
        protected SellOrderBuilder self() {
            return this;
        }

        @Override
        public Order build() {
            return new Order(this);
        }
    }

    public static class Builder extends AbstractBuilder<Builder, Order> {
        private Side side;

        public Builder side(final Side side) {
            this.side = side;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public Order build() {
            return new Order(this);
        }
    }

    public static SellOrderBuilder sellOrder() {
        return new SellOrderBuilder();
    }

    public static BuyOrderBuilder buyOrder() {
        return new BuyOrderBuilder();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return side + " " + qty + "@" + price;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Order order = (Order) o;
        return price == order.price &&
            qty == order.qty &&
            side == order.side;
    }

    @Override
    public int hashCode() {
        return hash(side, price, qty);
    }
}
