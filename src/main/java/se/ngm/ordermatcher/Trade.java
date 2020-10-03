package se.ngm.ordermatcher;

import static java.util.Objects.hash;

public class Trade {
    private final long price;
    private final long qty;

    private Trade(final Builder builder) {
        this.price = builder.price;
        this.qty = builder.qty;
    }

    public static Builder builder() {
        return new Builder();
    }

    public long getPrice() {
        return price;
    }

    public long getQty() {
        return qty;
    }

    public static class Builder {
        private long price;
        private long qty;

        public Builder price(final long price) {
            this.price = price;
            return this;
        }

        public Builder qty(final long qty) {
            this.qty = qty;
            return this;
        }

        public Trade build() {
            return new Trade(this);
        }
    }

    @Override
    public String toString() {
        return String.format("TRADE %d@%d", qty, price);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Trade trade = (Trade) o;
        return price == trade.price &&
            qty == trade.qty;
    }

    @Override
    public int hashCode() {
        return hash(price, qty);
    }
}
