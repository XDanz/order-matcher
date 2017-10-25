package com.cinnober.exercise.ordermatcher;

import java.util.*;

/**
 *
 * @author Daniel Terranova
 *
 * An order book contains two sides a sell and a buy side. The bid/sell side is represented as a
 * SortedMap where the key is the price and value contains orders at the given price. The bid side
 * is sorted (key i.e price) in reverse natural order and the sell side is sorted (key i.e price) in
 * natural order.
 *
 * Orders are placed in the order book at a given price in insertion order. When active order
 * is added and matched at a given price the first order is processed and removed if the active
 * order has higher quantity than the current processed (passive) order.
 *
 * If additional orders at the price exists those orders are processed until all orders is removed
 * from the price and that price is removed from the order book.
 *
 * @version 1.0
 */
public class OrderBook {

    private final String securityId;
    private final SortedMap<Long, OrdersAtPrice> bids = new TreeMap<>(Comparator.reverseOrder());
    private final SortedMap<Long, OrdersAtPrice> offers = new TreeMap<>(Comparator.naturalOrder());

    OrderBook(String securityId) {
        this.securityId = Objects.requireNonNull(securityId, "securityId cannot be null");
    }

    /**
     *
     * @return The market identifier of the order book
     */
    public String getSecurityId() {
        return securityId;
    }

    /**
     * Returns all remaining orders in the order book, in priority order, for the specified side.
     *
     * <p>Priority for buy orders is defined as highest price, followed by time priority (first come, first served).
     * For sell orders lowest price comes first, followed by time priority (same as for buy orders).
     *
     * @param side the side, not null.
     * @return all remaining orders in the order book, in priority order, for the specified side, not null.
     */
    public List<Order> getOrders(Side side) {
        List<Order> bidOrders = new ArrayList<>();
        Map<Long, OrdersAtPrice> pricesBySide = getOrderBookSide(side);
        for (OrdersAtPrice ordersAtPrice : pricesBySide.values()) {
            bidOrders.addAll(ordersAtPrice.getOrders());
        }
        return bidOrders;
    }

    /**
     * Add the specified order to the order book.
     *
     * @param order the order to be added, not null. The order will not be modified by the caller after this call.
     * @return any trades that were created by this order, not null.
     */
    public List<Trade> addOrder(Order order) {
        List<Trade> trades = new ArrayList<>();

        long currQty = order.getQuantity();

        if (order.getSide().equals(Side.BUY)) {
            currQty = matchBuyOrder(order, currQty, trades);
        } else if (order.getSide().equals(Side.SELL)) {
            currQty = matchSellOrder(order, currQty, trades);
        }

        if (currQty > 0) {
            add(order, currQty);
        }

        getOffers().entrySet().removeIf(entry -> entry.getValue().getTotalQuantity() == 0);
        getBids().entrySet().removeIf(entry -> entry.getValue().getTotalQuantity() == 0);
        return trades;
    }

    private Long matchBuyOrder(Order order, Long currQty, List<Trade> trades) {

        Set<Map.Entry<Long, OrdersAtPrice>> entries = getOffers().entrySet();

        for (Map.Entry<Long, OrdersAtPrice> offer : entries) {
            if ((offer.getKey() <= order.getPrice()) && currQty > 0) {
                // sell order exist at a lower price or equal to a buy order in the order book.
                currQty = match(offer, currQty, trades, order);
            } else {
                break;
            }
        }
        return currQty;
    }

    private Long matchSellOrder(Order order, Long currQty, List<Trade> trades) {

        Set<Map.Entry<Long, OrdersAtPrice>> entries = getBids().entrySet();

        for (Map.Entry<Long, OrdersAtPrice> bid : entries) {
            if ((bid.getKey() >= order.getPrice()) && currQty > 0) {
                // buy order exist at a higher price or equal to a sell order in the order book.
                currQty = match(bid, currQty, trades, order);
            } else {
                break;
            }
        }
        return currQty;
    }

    private Long match(Map.Entry<Long, OrdersAtPrice> entry, Long currQty, List<Trade> trades, Order order) {
        List<Trade> tradesAtPrice;
        tradesAtPrice = matchAtPrice(entry, currQty, order.getId());
        trades.addAll(tradesAtPrice);
        currQty -= tradesAtPrice.stream().mapToLong(Trade::getQuantity).sum();
        return currQty;
    }

    private static class OrdersAtPrice {
        private final List<Order> orders = new ArrayList<>();

        long getTotalQuantity() {
            return orders.stream().mapToLong(Order::getQuantity).sum();
        }

        List<Order> getOrders() {
            return orders;
        }

        void addOrder(Order order) {
            orders.add(order);
        }
    }

    /**
     * Add the volume (qty) amount at the given price to the order book.
     *
     * @param qty The amount of volume (qty) to add to the order book
     */
    private void add(Order order, Long qty) {
        Map<Long, OrdersAtPrice> orderBookSide = getOrderBookSide(order.getSide());

        Order remaingOrder = new Order(order.getId(), order.getSide(), order.getPrice(), qty);

        orderBookSide.computeIfPresent(order.getPrice(), (key, ordersAtPrice) -> { ordersAtPrice.addOrder(remaingOrder); return ordersAtPrice;});

        orderBookSide.computeIfAbsent(order.getPrice(), key -> {
            OrdersAtPrice ordersAtPrice = new OrdersAtPrice();
            ordersAtPrice.addOrder(remaingOrder);
            return ordersAtPrice;
        });
    }

    /**
     * Match the activeOrderId with qty (volume) and price with orders at the given
     * side.
     *
     * Matching is done in priority order, first come first serve. When a passive
     * order has been filled it is removed from the order queue and when all
     * orders has been filled the price at the the side is removed from the order book.
     *
     * @param entry BID or OFFER
     * @param qty The amount of volume to remove from the orders
     * @param activeOrderId The active orderId
     * @return List of matching trades @ given price
     */
    private List<Trade> matchAtPrice(Map.Entry<Long, OrdersAtPrice> entry, Long qty, Long activeOrderId) {
        List<Trade> trades = new ArrayList<>();

        Long price = entry.getKey();
        List<Order> queueOrders = entry.getValue().getOrders();

        Long availQty = qty;

        for (Iterator<Order> it = queueOrders.iterator(); it.hasNext();) {
            Order order = it.next();
            if (availQty > order.getQuantity()) {
                availQty -= order.getQuantity();
                trades.add(new Trade(activeOrderId, order.getId(), price, order.getQuantity()));
                it.remove();
            } else if (availQty < order.getQuantity()) {
                long diff = order.getQuantity() - availQty;
                order.setQuantity(diff);
                trades.add(new Trade(activeOrderId, order.getId(), price, availQty));
                availQty = 0L;
            } else {
                trades.add(new Trade(activeOrderId, order.getId(), price, order.getQuantity()));
                availQty = 0L;
                it.remove();
            }

            if (availQty <= 0)
                break;
        }

        return trades;
    }

    private SortedMap<Long,OrdersAtPrice> getOrderBookSide(Side side) {
        if (side.equals(Side.BUY)) {
            return getBids();
        } else {
            return getOffers();
        }
    }

    private SortedMap<Long, OrdersAtPrice> getBids() {
        return bids;
    }

    private SortedMap<Long, OrdersAtPrice> getOffers() {
        return offers;
    }

}
