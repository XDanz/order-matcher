package com.ngm.exercise.ordermatcher;

import static java.util.Objects.hash;

public class Trade {
    private final long activeOrdId;
    private final long queuedOrdId;
    private final long price;
    private final long qty;

    public Trade(long activeOrdId, long queuedOrdId, long price, long qty) {
        this.activeOrdId = activeOrdId;
        this.queuedOrdId = queuedOrdId;
        this.price = price;
        this.qty = qty;
    }

    public Trade(final Builder builder) {
        this.activeOrdId = builder.activeOrderId;
        this.queuedOrdId = builder.queuedOrderId;
        this.price = builder.price;
        this.qty = builder.qty;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the active client assigned order id.
     *
     * @return the active client assigned order id.
     */
    public long getActiveOrdId() {
        return activeOrdId;
    }

    /**
     * Returns the passive client assigned order id.
     *
     * @return the passive client assigned order id.
     */
    public long getQueuedOrdId() {
        return queuedOrdId;
    }

    /**
     * Returns the price.
     *
     * @return the price.
     */
    public long getPrice() {
        return price;
    }

    /**
     * Returns the quantity.
     *
     * @return the quantity.
     */
    public long getQty() {
        return qty;
    }


    public static class Builder {
        private long activeOrderId;
        private long queuedOrderId;
        private long price;
        private long qty;

        public Builder actOrdId(final long actOrdId) {
            this.activeOrderId = actOrdId;
            return this;
        }

        public Builder queuedOrdId(final long passOrdId) {
            this.queuedOrderId = passOrdId;
            return this;
        }

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
        return "TRADE " + qty + "@" + price + " (#" + activeOrdId + "/#" + queuedOrdId + ")";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Trade trade = (Trade) o;
        return activeOrdId == trade.activeOrdId &&
            queuedOrdId == trade.queuedOrdId &&
            price == trade.price &&
            qty == trade.qty;
    }

    @Override
    public int hashCode() {
        return hash(activeOrdId, queuedOrdId, price, qty);
    }
}
