package com.ngm.exercise.ordermatcher;

import java.util.List;
import java.util.Scanner;

/**
 * Order book with continuous matching of limit orders with time priority.
 *
 * <p>In an electronic exchange an order book is kept: All
 * buy and sell orders are entered into this order book and the prices are
 * set according to specific rules. Bids and asks are matched and trades
 * occur.
 *
 * <p>This class keeps an order book, that can determine in real-time the
 * current market price and combine matching orders to trades. Each order
 * has a quantity and a price.
 *
 * <p><b>The trading rules:</b>
 * It is a match if a buy order exist at a higher price or equal to a sell
 * order in the order book. The quantity of both orders is reduced as much as
 * possible. When an order has a quantity of zero it is removed. An order can
 * match several other orders if the quantity is large enough and the price is
 * correct. The price of the trade is computed as the order that was in the
 * order book first (the passive party).
 *
 * <p>The priority of the orders to match is based on the following:
 * <ol>
 * <li> On the price that is best for the active order (the one just entered)
 * <li> On the time the order was entered (first come first served)
 * </ol>
 *
 * <p><b>Note:</b> some methods are not yet implemented. This is your job!
 * See {@link #placeOrder(Order)} and {@link #getOrders(Side)}.
 */
public class OrderMatcher {

    private final OrderBook orderBook = new OrderBook();

    /**
     * Place the specified order to the order book.
     *
     * @param order the order to be added.
     *              The order will not be modified by the caller after this call.
     * @return any trades that were created by this order, not null.
     */
    public List<Trade> placeOrder(final Order order) {
        return orderBook.placeOrder(order);
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
    public List<Order> getOrders(final Side side) {
        return orderBook.getOrders(side);
    }

    public static void main(final String... args) {
        OrderMatcher matcher = new OrderMatcher();
        System.out.println("Welcome to the order matcher. Type 'help' for a list of commands. To quit hit 'Ctrl+d' or 'QUIT'");
        System.out.println();
        Scanner scanner = new Scanner(System.in);
        String line;
        LOOP:
        while (scanner.hasNext()) {
            line = scanner.nextLine();
            line = line.trim();
            try {
                switch (line) {
                    case "HELP":
                        System.out.println("Available commands: \n"
                            + "  BUY|SELL <quantity>@<price>  - Enter an order.\n"
                            + "  PRINT                         - List all remaining orders.\n"
                            + "  QUIT                         - Quit.\n"
                            + "  HELP                         - Show help (this message).\n");
                        break;
                    case "":
                        // ignore
                        break;
                    case "QUIT":
                        break LOOP;
                    case "PRINT":
                        System.out.println("--- BUY ---");
                        print(matcher.getOrders(Side.BUY));
                        System.out.println("--- SELL ---");
                        print(matcher.getOrders(Side.SELL));
                        break;
                    default:
                        Order order = OrderParserUtil.parseOrder(line);
                        List<Trade> trades = matcher.placeOrder(order);
                        print(trades);
                        break;
                }
            } catch (final IllegalArgumentException e) {
                System.err.println("Bad input: " + e.getMessage());
            } catch (final UnsupportedOperationException e) {
                System.err.println("Sorry, this command is not supported yet: " + e.getMessage());
            }
        }
        System.out.println("Good bye!");
    }

    private static void print(final List<?> orders) {
        for (final Object order : orders) {
            System.out.println(order);
        }
    }
}
