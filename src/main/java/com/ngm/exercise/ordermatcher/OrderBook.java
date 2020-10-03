package com.ngm.exercise.ordermatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;

/**
 * @author Daniel Terranova
 * <p>
 * An order book contains two sides a sell and a buy side. The buy/sell side is represented as a
 * SortedMap where the key is the price (Long) and value contains orders at the given price in queue order.
 * <p>
 * The bid (buy) side is sorted (key i.e price) in reverse natural order.
 * for example:
 * <p>
 * BUY SIDE
 * Qty @ price
 * +---------+
 * |100 @10  |
 * | 50 @ 9  |
 * | 30 @ 8  |
 * +-------- +
 * <p>
 * The sell side is sorted (key i.e price) in natural order.
 * <p>
 * SELL SIDE
 * Qty @ price
 * +---------+
 * | 30 @ 8  |
 * | 50 @ 9  |
 * |100 @10  |
 * +-------- +
 * <p>
 * Orders are placed in the order book at a given price in insertion order.
 * <p>
 * When active order is added and matched at a given price the first order is processed and removed if the active
 * order has higher quantity than the current processed (passive) order.
 * <p>
 * If additional orders at the price exists those orders are processed until all orders is removed
 * from the price and that price is removed from the order book.
 * @version 1.0
 */
public class OrderBook {

    private final SortedMap<Long, QueuedOrdersAtPrice> buyOrdersAtPrice = new TreeMap<>(reverseOrder());
    private final SortedMap<Long, QueuedOrdersAtPrice> sellOrdersAtPrice = new TreeMap<>(naturalOrder());

    /**
     * Returns all current orders in this order book, in priority order for a given Side.
     *
     * <p>Priority for buy orders is defined as highest price, followed by time priority.
     * For sell orders lowest price comes first, followed by time priority (same as for buy orders).
     *
     * @param side the side BUY/SELL
     * @return all current orders in the order book, in priority order, for the specified side.
     */
    public List<Order> getOrders(final Side side) {
        final List<Order> bidOrders = new ArrayList<>();
        final Map<Long, QueuedOrdersAtPrice> ordersBySide = getOrdersBySide(side);
        for (final QueuedOrdersAtPrice queuedOrdersAtPrice : ordersBySide.values()) {
            bidOrders.addAll(queuedOrdersAtPrice.getOrders());
        }
        return bidOrders;
    }

    /**
     * Place an order on the order book.
     *
     * @param order the order to be added.
     *              The order will not be modified by the caller after this call.
     * @return A list of trades that were created by this order, empty if no trades was created.
     */
    public List<Trade> placeOrder(final Order order) {
        // the trades that generates from the specified active order
        final List<Trade> trades = new ArrayList<>();

        long currQty = order.getQuantity();
        final Side currSide = order.getSide();

        if (Side.BUY.equals(currSide)) {
            currQty = matchBuyOrder(order, currQty, trades);
        } else if (Side.SELL.equals(currSide)) {
            currQty = matchSellOrder(order, currQty, trades);
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

    private Long matchBuyOrder(final Order buyOrder, long currQty, final List<Trade> trades) {
        final Set<Map.Entry<Long, QueuedOrdersAtPrice>> sellOrdersAtPrice =
            getSellOrdersAtPrice().entrySet();

        for (final Map.Entry<Long, QueuedOrdersAtPrice> sellOrderAtPrice : sellOrdersAtPrice) {
            if ((sellOrderAtPrice.getKey() <= buyOrder.getPrice()) && currQty > 0) {
                // sell order exist at a lower price or equal to a buy order in the order book.
                currQty = match(buyOrder, sellOrderAtPrice, currQty, trades);
            } else {
                break;
            }
        }
        return currQty;
    }

    private Long matchSellOrder(final Order sellOrder, Long currQty, final List<Trade> trades) {
        final Set<Map.Entry<Long, QueuedOrdersAtPrice>> buyOrdersAtPrice =
            getBuyOrdersAtPrice().entrySet();

        for (final Map.Entry<Long, QueuedOrdersAtPrice> buyOrderAtPrice : buyOrdersAtPrice) {
            if ((buyOrderAtPrice.getKey() >= sellOrder.getPrice()) && currQty > 0) {
                // buy order exist at a higher price or equal to a sell order in the order book.
                currQty = match(sellOrder, buyOrderAtPrice, currQty, trades);
            } else {
                break;
            }
        }
        return currQty;
    }

    private Long match(final Order order, final Map.Entry<Long, QueuedOrdersAtPrice> ordersAtPrice,
                       Long currQty, final List<Trade> trades) {
        final List<Trade> tradesAtPrice = Matcher.matchAtPrice(ordersAtPrice, currQty, order.getId());
        trades.addAll(tradesAtPrice);
        currQty -= tradesAtPrice.stream()
            .mapToLong(Trade::getQuantity)
            .sum();
        return currQty;
    }

    /**
     * Add the order volume (qty) amount at the given price to the order book.
     *
     * @param qty The amount of volume (qty) to add to the order book
     */
    private void add(final Order order, final Long qty) {
        final Map<Long, QueuedOrdersAtPrice> ordersBySide = getOrdersBySide(order.getSide());

        final Order remainingOrder =
            new Order(order.getId(), order.getSide(), order.getPrice(), qty);

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
