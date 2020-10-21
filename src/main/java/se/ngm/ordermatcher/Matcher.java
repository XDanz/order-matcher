package se.ngm.ordermatcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Matcher {

    public static List<Trade> matchAtPrice(final Map.Entry<Long, QueuedOrdersAtPrice> ordersAtPrice,
                                           long availQty) {
        final List<Trade> trades = new ArrayList<>();

        final Long price = ordersAtPrice.getKey();
        final List<Order> queuedOrders = ordersAtPrice.getValue().getOrders();

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
