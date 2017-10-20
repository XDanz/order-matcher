package com.cinnober.exercise.ordermatcher;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class OrderBook {

    private String securityId;
    private final NavigableMap<Long, LevelInfo> bids = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
    private final NavigableMap<Long, LevelInfo> offers = new ConcurrentSkipListMap<>(Comparator.naturalOrder());


    public static class LevelInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private int orders;
        private long volume;

        LevelInfo(long volume) {
            this.volume = volume;
        }

        long getVolume() {
            return volume;
        }

        void setVolume(long volume) {
            this.volume = volume;
        }

        int getOrders() {
            return orders;
        }

        void addOrderCount() {
            orders++;
        }

        void removeOrderCount() {
            orders--;
        }

    }

    OrderBook(String securityId) {
        this.securityId = securityId;
    }

    public List<Trade> addOrder (Order order) {
        List< Trade> trades = new ArrayList<>();
        if (order.getSide().equals(Side.BUY)) {

        } else {
            long currQty = order.getQuantity();
            Map.Entry<Long, LevelInfo> bid;


            while ( (bid = getBids().firstEntry()) != null) {
                if (order.getPrice() <= bid.getKey() && currQty > 0 ) {
                    long abs = Math.abs(bid.getValue().getVolume() - currQty);

                    if (currQty > bid.getValue().getVolume()) {
                        currQty = bid.getValue().getVolume() - abs;
                        subtract(Side.BUY, order.getPrice(), abs);
                        //Todo: fix pasiveOrderId
                        trades.add(new Trade(order.getId(), 0, bid.getKey(), abs));
                        // trade occured!
                    } else if (currQty < bid.getValue().getVolume()) {
                        currQty = bid.getValue().getVolume() - abs;
                        subtract(Side.BUY, order.getPrice(), abs);
                        // partial fill
                    } else {
                        // total fill
                        currQty = bid.getValue().getVolume() - abs;
                        subtract(Side.BUY, order.getPrice(), abs);

                    }
                } else
                    break;
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
     * @param volume The amount of volume to remove from the order
     */
    public void add(Side side, Long price, Long volume) {
        Map<Long, LevelInfo> priceBySide = getPricesBySide(side);
        if (priceBySide != null) {
            if (priceBySide.containsKey(price)) {
                LevelInfo levelInfo = priceBySide.get(price);
                levelInfo.setVolume(levelInfo.getVolume() + volume);
            } else {
                priceBySide.put(price, new LevelInfo(volume));
            }
        }
    }

    public Integer getOrders(Long price, Side ngmMdEntryType) {
        Map<Long, LevelInfo> pricesByEntryType = getPricesBySide(ngmMdEntryType);
        if (pricesByEntryType != null) {
            if (pricesByEntryType.containsKey(price)) {
                return pricesByEntryType.get(price).getOrders();
            }
        }
        return 0;
    }

    public void addOrder(Long price, Side side) {
        Map<Long, LevelInfo> pricesByEntryType = getPricesBySide(side);
        if (pricesByEntryType != null) {
            if (pricesByEntryType.containsKey(price)) {
                LevelInfo levelInfo = pricesByEntryType.get(price);
                levelInfo.addOrderCount();
            }
        }
    }

    public void removeOrder(Long price, Side side) {
        Map<Long, LevelInfo> pricesBySide = getPricesBySide(side);
        if (pricesBySide != null) {
            if (pricesBySide.containsKey(price)) {
                LevelInfo levelInfo = pricesBySide.get(price);
                levelInfo.removeOrderCount();
            }
        }
    }

    /**
     * Subtract the volume amount from the order with ngmMdEntryType (BID/OFFER)
     * to the specified price.
     *
     * @param side BID or OFFER
     * @param price The price
     * @param volume The amount of volume to remove from the order
     */
    public void subtract(Side side, Long price, Long volume) {
        Map<Long, LevelInfo> pricesByEntryType = getPricesBySide(side);
        if (pricesByEntryType != null) {
            if (pricesByEntryType.containsKey(price)) {
                LevelInfo levelInfo = pricesByEntryType.get(price);
                levelInfo.setVolume(levelInfo.getVolume()-volume);
            } else {
                pricesByEntryType.put(price, new LevelInfo(volume));
            }

            if (pricesByEntryType.get(price).getVolume() == 0) {
                pricesByEntryType.remove(price);
            } else if (pricesByEntryType.get(price).getVolume() < 0) {
                throw new IllegalStateException(" Big time error! delta volume could not be negative");
            }
        }
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
