package com.ngm.exercise.ordermatcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Matcher {

    /**
     * Match the activeOrderId with qty (volume) and price with orders at the given
     * side.
     * <p>
     * Matching is done in priority order, first come first serve. When a passive
     * order has been filled it is removed from the order queue and when all
     * orders has been filled the price at the the side is removed from the order book.
     *
     * @param ordersAtPrice BID or OFFER
     * @param qty           The amount of volume to remove from the orders
     * @return List of matching trades @ given price
     */
    public static List<Trade> matchAtPrice(final Map.Entry<Long, QueuedOrdersAtPrice> ordersAtPrice,
                                           final Long qty) {
        final List<Trade> trades = new ArrayList<>();

        final Long price = ordersAtPrice.getKey();
        final List<Order> queuedOrders = ordersAtPrice.getValue().getOrders();

        Long availQty = qty;

        for (final Iterator<Order> it = queuedOrders.iterator(); it.hasNext(); ) {
            final Order queuedOrder = it.next();
            if (availQty > queuedOrder.getQty()) {
                availQty -= queuedOrder.getQty();
                trades.add(Trade.builder()
                    .price(price)
                    .qty(queuedOrder.getQty())
                    .build());
                it.remove(); // passive order has been filled!, removed from the order queue
            } else if (availQty < queuedOrder.getQty()) {
                final long diff = queuedOrder.getQty() - availQty;
                queuedOrder.setQty(diff);
                trades.add(Trade.builder()
                    .price(price)
                    .qty(availQty)
                    .build());
                availQty = 0L;
            } else {
                trades.add(Trade.builder()
                    .price(price)
                    .qty(queuedOrder.getQty())
                    .build());
                availQty = 0L;
                it.remove();
            }

            if (availQty <= 0) {
                break;
            }
        }

        return trades;
    }
}
