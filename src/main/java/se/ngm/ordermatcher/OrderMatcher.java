package se.ngm.ordermatcher;

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

    public static void main(final String[] args) {
        OrderMatcher matcher = new OrderMatcher();
        System.out.println("Order matcher. To quit hit 'Ctrl+d' or 'QUIT'");
        System.out.println();
        Scanner scanner = new Scanner(System.in);
        String line;
        while (scanner.hasNext()) {
            line = scanner.nextLine();
            line = line.trim();
            try {
                switch (line.toUpperCase()) {
                    case "":
                        break;
                    case "QUIT":
                        return;
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
                System.err.println(e.getMessage());
            }
            catch (final Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private static void print(final List<?> orders) {
        for (final Object order : orders) {
            System.out.println(order);
        }
    }
}
