package com.cinnober.exercise.ordermatcher;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class OrderBook {

    private final String securityId;
    private final NavigableMap<Long, OrdersAtPrice> bids = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
    private final NavigableMap<Long, OrdersAtPrice> offers = new ConcurrentSkipListMap<>(Comparator.naturalOrder());

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

    OrderBook(String securityId) {
        this.securityId = securityId;
    }

    List<Order> getOrders(Side side) {
        List<Order> bidOrders = new ArrayList<>();
        NavigableMap<Long, OrdersAtPrice> pricesBySide = getPricesBySide(side);
        for (OrdersAtPrice ordersAtPrice : pricesBySide.values()) {
            bidOrders.addAll(ordersAtPrice.getOrders());
        }
        return bidOrders;
    }

    List<Trade> addOrder(Order order) {
        List< Trade> trades = new ArrayList<>();

        long currQty = order.getQuantity();
        if (order.getSide().equals(Side.BUY)) {

            Set<Map.Entry<Long, OrdersAtPrice>> entries = getOffers().entrySet();

            for (Map.Entry<Long, OrdersAtPrice> offer : entries) {
                if ((offer.getKey() <= order.getPrice()) && currQty > 0) {

                    List<Trade> tradesAtPrice;
                    tradesAtPrice = matchAtPrice(Side.SELL, offer.getKey(), currQty, order.getId());
                    trades.addAll(tradesAtPrice);
                    currQty -= tradesAtPrice.stream().mapToLong(Trade::getQuantity).sum();
                } else
                    break;
            }

        } else if (order.getSide().equals(Side.SELL)) {
            Set<Map.Entry<Long, OrdersAtPrice>> entries = getBids().entrySet();

            for (Map.Entry<Long, OrdersAtPrice> bid : entries) {
                if ((bid.getKey() >= order.getPrice()) && currQty > 0) {

                    List<Trade> tradesAtPrice;
                    tradesAtPrice = matchAtPrice(Side.BUY, bid.getKey(), currQty, order.getId());
                    trades.addAll(tradesAtPrice);
                    currQty -= tradesAtPrice.stream().mapToLong(Trade::getQuantity).sum();
                } else
                    break;
            }

        }

        if (currQty > 0) {
            add(order.getSide(),order.getPrice(), currQty,  order.getId());
        }
        return trades;
    }

    /**
     * Add the volume amount from the order with ngmMdEntryType (BID/OFFER)
     * to the specified price.
     *
     * @param side BID or OFFER
     * @param price The price
     * @param qty The amount of volume to remove from the order
     */
    private void add(Side side, Long price, Long qty, Long orderId) {
        Map<Long, OrdersAtPrice> priceBySide = getPricesBySide(side);
        if (priceBySide != null) {
            Order order = new Order(orderId, side, price, qty);
            if (priceBySide.containsKey(price)) {
                OrdersAtPrice ordersAtPrice = priceBySide.get(price);
                ordersAtPrice.addOrder(order);
            } else {
                OrdersAtPrice ordersAtPrice = new OrdersAtPrice();
                ordersAtPrice.addOrder(order);
                priceBySide.put(price, ordersAtPrice);
            }
        }
    }

    /**
     * Subtract the volume amount from the order with (BID/OFFER)
     * to the specified price.
     *
     * @param side BID or OFFER
     * @param price The price
     * @param qty The amount of volume to remove from the orders
     * @return List of matching trades @ price
     */
    private List<Trade> matchAtPrice(Side side, Long price, Long qty, Long activeOrderId) {
        List<Trade> trades = new ArrayList<>();

        Map<Long, OrdersAtPrice> ordersByPrice = getPricesBySide(side);
        if (ordersByPrice.containsKey(price)) {
            OrdersAtPrice ordersAtPrice = ordersByPrice.get(price);
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
            throw new IllegalStateException(" Try to match at non existing price" + price);
        }

        if (ordersByPrice.get(price).getVolume() == 0) {
            ordersByPrice.remove(price);
        } else if (ordersByPrice.get(price).getVolume() < 0) {
            throw new IllegalStateException(" Big time error! delta volume could not be negative");
        }

        return trades;
    }

    private NavigableMap<Long,OrdersAtPrice> getPricesBySide(Side side) {
        if (side.equals(Side.BUY)) {
            return getBids();
        } else {
            return getOffers();
        }
    }

    private NavigableMap<Long, OrdersAtPrice> getBids() {
        return bids;
    }

    private NavigableMap<Long, OrdersAtPrice> getOffers() {
        return offers;
    }

}
