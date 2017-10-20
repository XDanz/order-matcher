package com.cinnober.exercise.ordermatcher;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class OrderBook {

    private String securityId;
    private final NavigableMap<Long, LevelInfo> bids = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
    private final NavigableMap<Long, LevelInfo> offers = new ConcurrentSkipListMap<>(Comparator.naturalOrder());


    public static class LevelInfo implements Serializable {
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
        NavigableMap<Long, LevelInfo> pricesBySide = getPricesBySide(side);
        for (LevelInfo levelInfo : pricesBySide.values()) {
            bidOrders.addAll(levelInfo.getOrders());
        }
        return bidOrders;
    }

    public List<Trade> addOrder (Order order) {
        List< Trade> trades = new ArrayList<>();

        if (order.getSide().equals(Side.BUY)) {
            long currQty = order.getQuantity();

            Set<Map.Entry<Long, LevelInfo>> entries = getOffers().entrySet();

            for (Map.Entry<Long, LevelInfo> offer : entries) {
                if ((offer.getKey() <= order.getPrice()) && currQty > 0) {

                    List<Trade> tradesAtPrice;
                    tradesAtPrice = matchAtPrice(Side.SELL, offer.getKey(), currQty, order.getId());
                    trades.addAll(tradesAtPrice);
                    currQty -= tradesAtPrice.stream().mapToLong(Trade::getQuantity).sum();
                } else
                    break;
            }

            if (currQty > 0) {
                add(order.getSide(),order.getPrice(), currQty,  order.getId());
            }

        } else if (order.getSide().equals(Side.SELL)) {
            long currQty = order.getQuantity();
            Set<Map.Entry<Long, LevelInfo>> entries = getBids().entrySet();

            for (Map.Entry<Long, LevelInfo> bid : entries) {
                if ((bid.getKey() >= order.getPrice()) && currQty > 0) {

                    List<Trade> tradesAtPrice;
                    tradesAtPrice = matchAtPrice(Side.BUY, bid.getKey(), currQty, order.getId());
                    trades.addAll(tradesAtPrice);
                    currQty -= tradesAtPrice.stream().mapToLong(Trade::getQuantity).sum();
                } else
                    break;
            }

            if (currQty > 0) {

                add(order.getSide(),order.getPrice(), currQty,  order.getId());
            }

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
        Map<Long, LevelInfo> priceBySide = getPricesBySide(side);
        if (priceBySide != null) {
            Order order = new Order(orderId, side, price, qty);
            if (priceBySide.containsKey(price)) {
                LevelInfo levelInfo = priceBySide.get(price);
                levelInfo.addOrder(order);
            } else {
                LevelInfo levelInfo = new LevelInfo();
                levelInfo.addOrder(order);
                priceBySide.put(price, levelInfo);
            }
        }
    }

    /**
     * Subtract the volume amount from the order with (BID/OFFER)
     * to the specified price.
     *
     * @param side BID or OFFER
     * @param price The price
     * @param volume The amount of volume to remove from the order
     */
    private List<Trade> matchAtPrice(Side side, Long price, Long volume, Long activeOrderId) {
        List<Trade> affected = new ArrayList<>();

        Map<Long, LevelInfo> ordersByPrice = getPricesBySide(side);
        if (ordersByPrice.containsKey(price)) {
            LevelInfo levelInfo = ordersByPrice.get(price);
            List<Order> queueOrders = levelInfo.getOrders();

            Long availVolume = volume;

            for (Iterator<Order> it = queueOrders.iterator(); it.hasNext();) {
                Order order = it.next();
                if (availVolume > order.getQuantity()) {
                    availVolume = availVolume - order.getQuantity();
                    affected.add(new Trade(activeOrderId, order.getId(), price, order.getQuantity()));
                    order.setQuantity(0);
                    it.remove();
                } else if (availVolume < order.getQuantity()) {
                    // Partial fill of order
                    long diff = order.getQuantity() - availVolume;
                    order.setQuantity(diff);
                    availVolume = diff;
                    affected.add(new Trade(activeOrderId, order.getId(), price, volume));
                } else {
                    affected.add(new Trade(activeOrderId, order.getId(), price, order.getQuantity()));
                    availVolume = 0L;
                    order.setQuantity(0L);
                    it.remove();
                }

                if (availVolume <= 0)
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

        return affected;
    }

    public NavigableMap<Long,LevelInfo> getPricesBySide(Side side) {
        switch (side) {
            case BUY:
                return getBids();
            case SELL:
                return getOffers();
            default:
                return null;
        }
    }

    public NavigableMap<Long, LevelInfo> getBids() {
        return bids;
    }


    public NavigableMap<Long, LevelInfo> getOffers() {
        return offers;
    }


}
