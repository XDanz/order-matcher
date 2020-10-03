package com.ngm.exercise.ordermatcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Matcher {

    /**
     * Match the activeOrderId with qty (volume) and price with orders at the given
     * side.
     *
     * Matching is done in priority order, first come first serve. When a passive
     * order has been filled it is removed from the order queue and when all
     * orders has been filled the price at the the side is removed from the order book.
     *
     * @param ordersAtPrice BID or OFFER
     * @param qty The amount of volume to remove from the orders
     * @param activeOrderId The active orderId
     * @return List of matching trades @ given price
     */
    public static List<Trade> matchAtPrice(final Map.Entry<Long, QueuedOrdersAtPrice> ordersAtPrice,
                                     final Long qty, final Long activeOrderId) {
        final List<Trade> trades = new ArrayList<>();

        final Long price = ordersAtPrice.getKey();
        final List<Order> queuedOrders = ordersAtPrice.getValue().getOrders();

        Long availQty = qty;

        for (final Iterator<Order> it = queuedOrders.iterator(); it.hasNext();) {
            final Order queuedOrder = it.next();
            if (availQty > queuedOrder.getQuantity()) {
                availQty -= queuedOrder.getQuantity();
                trades.add(new Trade(activeOrderId, queuedOrder.getId(), price, queuedOrder.getQuantity()));
                it.remove(); // passive order has been filled!, removed from the order queue
            } else if (availQty < queuedOrder.getQuantity()) {
                final long diff = queuedOrder.getQuantity() - availQty;
                queuedOrder.setQuantity(diff);
                trades.add(new Trade(activeOrderId, queuedOrder.getId(), price, availQty));
                availQty = 0L;
            } else {
                trades.add(new Trade(activeOrderId, queuedOrder.getId(), price, queuedOrder.getQuantity()));
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
