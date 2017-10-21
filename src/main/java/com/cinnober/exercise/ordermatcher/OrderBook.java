package com.cinnober.exercise.ordermatcher;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 *
 * @author Daniel Terranova
 */
public class OrderBook {

    private final String securityId;
    private final NavigableMap<Long, OrdersAtPrice> bids = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
    private final NavigableMap<Long, OrdersAtPrice> offers = new ConcurrentSkipListMap<>(Comparator.naturalOrder());

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

    public static class OrdersAtPrice {
        private final List<Order> orders = new ArrayList<>();

        long getVolume() {
            Long sum = 0L;
            for (Order order : orders) {
                sum += order.getQuantity();
            }
            return sum;
        }

        List<Order> getOrders() {
            return orders;
        }

        void addOrder(Order order) {
            orders.add(order);
        }
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
    List<Order> getOrders(Side side) {
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
    List<Trade> addOrder(Order order) {
        List< Trade> trades = new ArrayList<>();

        long currQty = order.getQuantity();

        if (order.getSide().equals(Side.BUY)) {

            Set<Map.Entry<Long, OrdersAtPrice>> entries = getOffers().entrySet();

            for (Map.Entry<Long, OrdersAtPrice> offer : entries) {
                if ((offer.getKey() <= order.getPrice()) && currQty > 0) {
                    // sell order exist at a lower price or equal to a buy order in the order book.

                    List<Trade> tradesAtPrice;
                    tradesAtPrice = matchAtPrice(Side.SELL, offer.getKey(), currQty, order.getId());
                    trades.addAll(tradesAtPrice);
                    currQty -= tradesAtPrice.stream().mapToLong(Trade::getQuantity).sum();
                } else {
                    break;
                }
            }

        } else if (order.getSide().equals(Side.SELL)) {

            Set<Map.Entry<Long, OrdersAtPrice>> entries = getBids().entrySet();


            for (Map.Entry<Long, OrdersAtPrice> bid : entries) {
                if ((bid.getKey() >= order.getPrice()) && currQty > 0) {
                    // buy order exist at a higher price or equal to a sell order in the order book.

                    List<Trade> tradesAtPrice;
                    tradesAtPrice = matchAtPrice(Side.BUY, bid.getKey(), currQty, order.getId());
                    trades.addAll(tradesAtPrice);
                    currQty -= tradesAtPrice.stream().mapToLong(Trade::getQuantity).sum();
                } else {
                    break;
                }
            }

        }

        if (currQty > 0) {
            add(order.getSide(), order.getPrice(), currQty,  order.getId());
        }
        return trades;
    }

    /**
     * Add the volume (qty) amount at the given price to the order book.
     *
     * @param side BID or SELL
     * @param price The price
     * @param qty The amount of volume (qty) to add to the order book
     */
    private void add(Side side, Long price, Long qty, Long orderId) {
        Map<Long, OrdersAtPrice> orderBookSide = getOrderBookSide(side);

        Order order = new Order(orderId, side, price, qty);

        if (orderBookSide.containsKey(price)) {
            OrdersAtPrice ordersAtPrice = orderBookSide.get(price);
            ordersAtPrice.addOrder(order);
        } else {
            OrdersAtPrice ordersAtPrice = new OrdersAtPrice();
            ordersAtPrice.addOrder(order);
            orderBookSide.put(price, ordersAtPrice);
        }
    }

    /**
     * Match the activeOrderId with qty (volume) and price with orders at the given
     * side.
     *
     * Matching is done in priority order, first come first serve. When a passive
     * order has been filled it is removed from the order queue and when all
     * orders has been filled the price at the the side is removed from the order book.
     *
     * @param side BID or OFFER
     * @param price The price
     * @param qty The amount of volume to remove from the orders
     * @param activeOrderId The active orderId
     * @return List of matching trades @ given price
     */
    private List<Trade> matchAtPrice(Side side, Long price, Long qty, Long activeOrderId) {
        List<Trade> trades = new ArrayList<>();

        Map<Long, OrdersAtPrice> orderBookSide = getOrderBookSide(side);
        if (orderBookSide.containsKey(price)) {
            OrdersAtPrice ordersAtPrice = orderBookSide.get(price);
            List<Order> queueOrders = ordersAtPrice.getOrders();

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
                    availQty = diff;
                } else {
                    trades.add(new Trade(activeOrderId, order.getId(), price, order.getQuantity()));
                    availQty = 0L;
                    it.remove();
                }

                if (availQty <= 0)
                    break;
            }

        } else {
            throw new IllegalStateException(" Try to match @ non existing price: " + price);
        }

        if (orderBookSide.get(price).getVolume() == 0) {
            orderBookSide.remove(price);
        }

        return trades;
    }

    private Map<Long,OrdersAtPrice> getOrderBookSide(Side side) {
        if (side.equals(Side.BUY)) {
            return getBids();
        } else {
            return getOffers();
        }
    }

    private Map<Long, OrdersAtPrice> getBids() {
        return bids;
    }

    private Map<Long, OrdersAtPrice> getOffers() {
        return offers;
    }

}
