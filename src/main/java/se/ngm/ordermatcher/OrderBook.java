package se.ngm.ordermatcher;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;

/**
 *
 * @author Daniel Terranova
 *
 * An order book contains two sides a sell and a buy side. The buy and sell side is represented as a
 * SortedMap where the key is the price (long) and value contains orders at the given price in queue order.
 *
 * The buy side is sorted in reverse natural order by its price.
 * for example:
 *
 *   BUY SIDE
 *   Qty @ Price
 *  +---------+
 *  | 10@100  |
 *  |  9@50   |
 *  |  8@30   |
 *  +-------- +
 *
 * The sell side is sorted (key i.e price) in natural order.
 *
 *    SELL SIDE
 *    Qty @ price
 *   +---------+
 *   |  30@8   |
 *   |  50@9   |
 *   | 100@10  |
 *   +-------- +
 *
 * Orders are placed in the order book at a given price in insertion order.
 *
 * When active order is added and matched at a given price the first order (passive order in queue) is processed and
 * removed if the active order has higher quantity than the current processed (passive) order. If additional orders at
 * the price exists those orders are processed until all orders is removed from the price and that price is removed from
 * the order book.
 *
 * @version 1.0
 */
public class OrderBook {

    private final SortedMap<Long, QueuedOrdersAtPrice> buyOrdersAtPrice = new TreeMap<>(reverseOrder());
    private final SortedMap<Long, QueuedOrdersAtPrice> sellOrdersAtPrice = new TreeMap<>(naturalOrder());
    private final Comparator<Long> cmp = Long::compareTo;

    public List<Order> getOrders(final Side side) {
        final List<Order> currentOrders = new ArrayList<>();
        final Map<Long, QueuedOrdersAtPrice> ordersBySide = getOrdersBySide(side);
        for (final QueuedOrdersAtPrice queuedOrdersAtPrice : ordersBySide.values()) {
            currentOrders.addAll(queuedOrdersAtPrice.getOrders());
        }
        return currentOrders;
    }

    public List<Trade> placeOrder(final Order order) {
        // the trades that generates from the specified active order
        final List<Trade> trades = new ArrayList<>();

        long currQty = order.getQty();
        final Side currSide = order.getSide();

        if (Side.BUY.equals(currSide)) {
            currQty = compare(order.getPrice(), currQty, getSellOrdersAtPrice().entrySet(), cmp, trades);
        } else if (Side.SELL.equals(currSide)) {
            currQty = compare(order.getPrice(), currQty, getBuyOrdersAtPrice().entrySet(), cmp.reversed(), trades);
        }

        if (currQty > 0) {
            add(order, currQty);
        }

        getSellOrdersAtPrice().entrySet()
            .removeIf(entry -> entry.getValue()
                .getTotalQuantity() == 0);

        getBuyOrdersAtPrice().entrySet()
            .removeIf(entry -> entry.getValue()
                .getTotalQuantity() == 0);
        return trades;
    }

    private long compare(final long activeOrderPrice, long currQty,
                         final Set<Map.Entry<Long, QueuedOrdersAtPrice>> sideOrders, final Comparator<Long> cmp,
                         final List<Trade> trades) {
        for (final Map.Entry<Long, QueuedOrdersAtPrice> queueOrdersAtPrice: sideOrders) {
            final long passiveOrderPrice = queueOrdersAtPrice.getKey();
            if ( (cmp.compare(passiveOrderPrice, activeOrderPrice) <= 0) && currQty > 0) {
                currQty = match(queueOrdersAtPrice, currQty, trades);
            } else {
                break;
            }
        }
        return currQty;
    }

    private Long match(final Map.Entry<Long, QueuedOrdersAtPrice> ordersAtPrice,
                       Long currQty, final List<Trade> trades) {
        final List<Trade> tradesAtPrice = Matcher.matchAtPrice(ordersAtPrice, currQty);
        trades.addAll(tradesAtPrice);
        currQty -= tradesAtPrice.stream()
            .mapToLong(Trade::getQty)
            .sum();
        return currQty;
    }

    private void add(final Order order, final Long qty) {
        final Map<Long, QueuedOrdersAtPrice> ordersBySide = getOrdersBySide(order.getSide());

        final Order remainingOrder =
            Order.builder()
                .side(order.getSide())
                .price(order.getPrice())
                .qty(qty).build();

        ordersBySide.computeIfPresent(order.getPrice(),
            (key, queuedOrdersAtPrice) -> {
                queuedOrdersAtPrice.addOrder(remainingOrder);
                return queuedOrdersAtPrice;
            });

        ordersBySide.computeIfAbsent(order.getPrice(), key -> {
            QueuedOrdersAtPrice queuedOrdersAtPrice = new QueuedOrdersAtPrice();
            queuedOrdersAtPrice.addOrder(remainingOrder);
            return queuedOrdersAtPrice;
        });
    }

    private SortedMap<Long, QueuedOrdersAtPrice> getOrdersBySide(final Side side) {
        if (Side.BUY.equals(side)) {
            return getBuyOrdersAtPrice();
        } else {
            return getSellOrdersAtPrice();
        }
    }

    private SortedMap<Long, QueuedOrdersAtPrice> getBuyOrdersAtPrice() {
        return buyOrdersAtPrice;
    }

    private SortedMap<Long, QueuedOrdersAtPrice> getSellOrdersAtPrice() {
        return sellOrdersAtPrice;
    }

}
