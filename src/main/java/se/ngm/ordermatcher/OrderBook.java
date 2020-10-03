package se.ngm.ordermatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;

public class OrderBook {

    private final SortedMap<Long, QueuedOrdersAtPrice> buyOrdersAtPrice = new TreeMap<>(reverseOrder());
    private final SortedMap<Long, QueuedOrdersAtPrice> sellOrdersAtPrice = new TreeMap<>(naturalOrder());

    public List<Order> getOrders(final Side side) {
        final List<Order> bidOrders = new ArrayList<>();
        final Map<Long, QueuedOrdersAtPrice> ordersBySide = getOrdersBySide(side);
        for (final QueuedOrdersAtPrice queuedOrdersAtPrice : ordersBySide.values()) {
            bidOrders.addAll(queuedOrdersAtPrice.getOrders());
        }
        return bidOrders;
    }

    public List<Trade> placeOrder(final Order order) {
        // the trades that generates from the specified active order
        final List<Trade> trades = new ArrayList<>();

        long currQty = order.getQty();
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
                currQty = match(sellOrderAtPrice, currQty, trades);
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
                currQty = match(buyOrderAtPrice, currQty, trades);
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
