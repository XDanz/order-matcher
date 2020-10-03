package com.ngm.exercise.ordermatcher;

import java.util.List;
import java.util.Scanner;

public class OrderMatcher {

    private final OrderBook orderBook = new OrderBook();

    public List<Trade> placeOrder(final Order order) {
        return orderBook.placeOrder(order);
    }

    public List<Order> getOrders(final Side side) {
        return orderBook.getOrders(side);
    }

    public static void main(final String... args) {
        OrderMatcher matcher = new OrderMatcher();
        System.out.println("Order matcher. Type 'help' for a list of commands. To quit hit 'Ctrl+d' or 'QUIT'");
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
