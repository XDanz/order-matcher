package com.ngm.exercise.ordermatcher;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class OrderMatcherTest2 {

    /**
     *     testMatch(
     *             asList("BUY 60@10 #1", "SELL 100@10 #2"),
     *             asList("TRADE 60@10 (#2/#1)"),
     *             asList("SELL 40@10 #2")
     *         );
     */
    @Test
    void name() {
        OrderMatcher orderMatcher = new OrderMatcher();
        orderMatcher.placeOrder(new Order(1, Side.BUY, 5, 100));
    }

    @Test
    public void testMatch1() {
        OrderMatcher orderMatcher = new OrderMatcher();
        orderMatcher.placeOrder(new Order(1, Side.BUY, 5, 100));

        List<Trade> trades = orderMatcher.placeOrder(new Order(2, Side.SELL, 5, 50));
        System.out.println(trades.size() + " trades = " + trades);
        assertThat(trades.size()).isEqualTo(1);
        assertTrade(5, 50, trades.get(0));

        List<Order> orders = orderMatcher.getOrders(Side.BUY);
        System.out.println("1 orders = " + orders);

        trades = orderMatcher.placeOrder(new Order(3, Side.SELL, 5, 20));
        System.out.println("2 trades = " + trades);

        orders = orderMatcher.getOrders(Side.BUY);
        System.out.println("2 orders = " + orders);

        trades = orderMatcher.placeOrder(new Order(4, Side.SELL, 5, 30));
        System.out.println("3 trades = " + trades);

        orders = orderMatcher.getOrders(Side.BUY);
        System.out.println("3 orders = " + orders);
    }

    @Test
    public void testMatch12() {
        OrderMatcher orderMatcher = new OrderMatcher();
        orderMatcher.placeOrder(new Order(1, Side.BUY, 5, 100));
        orderMatcher.placeOrder(new Order(2, Side.BUY, 4, 100));

        List<Trade> trades = orderMatcher.placeOrder(new Order(3, Side.SELL, 4, 300));
        System.out.println(trades.size() + " trades = " + trades);
        assertThat(trades).hasSize(2);
        assertTrade(5, 100, trades.get(0));
        assertTrade(4, 100, trades.get(1));

        List<Order> orders = orderMatcher.getOrders(Side.SELL);
        System.out.println(orders.size() + " orders = " + orders);
        assertThat(orders).hasSize(1);
        assertOrder(4, 100, orders.get(0));
    }

    @Test
    public void testMatch2() {
        OrderMatcher orderMatcher = new OrderMatcher();

        orderMatcher.placeOrder(new Order(1, Side.SELL, 5, 100));
        orderMatcher.placeOrder(new Order(2, Side.SELL, 4, 100));
        List<Order> orders = orderMatcher.getOrders(Side.SELL);
        System.out.println("orders = " + orders);

        assertThat(orders).extracting("price","quantity")
            .contains(tuple(4L,100L),tuple(5L,100L));
        assertOrder(4, 100, orders.get(0));
        assertOrder(5, 100, orders.get(1));

        List<Trade> trades = orderMatcher.placeOrder(new Order(3, Side.BUY, 5, 50));
        assertThat(trades).hasSize(1);
        assertTrade(4, 50, trades.get(0));
        System.out.println(trades.size() + " trades = " + trades);
        orders = orderMatcher.getOrders(Side.SELL);
        System.out.println("orders = " + orders);
    }

    @Test
    public void testMatch3() {
        OrderMatcher orderMatcher = new OrderMatcher();

        orderMatcher.placeOrder(new Order(1, Side.SELL, 5, 100));
        orderMatcher.placeOrder(new Order(2, Side.SELL, 4, 100));
        List<Order> orders = orderMatcher.getOrders(Side.SELL);
        assertThat(orders).hasSize(2);
    ///assertEquals(2, orders.size());
        System.out.println("orders = " + orders);

        assertOrder(4, 100, orders.get(0));
        assertOrder(5, 100, orders.get(1));

        List<Trade> trades = orderMatcher.placeOrder(new Order(3, Side.BUY, 5, 300));
        assertThat(trades).hasSize(2);
        assertTrade(4, 100, trades.get(0));
        assertTrade(5, 100, trades.get(1));
        System.out.println(trades.size() + " trades = " + trades);
        orders = orderMatcher.getOrders(Side.BUY);
        System.out.println("orders = " + orders);
        assertThat(orders).hasSize(1);
        assertOrder(5, 100, orders.get(0));
    }

    @Test
    public void test6() throws Exception {
        OrderMatcher orderMatcher = new OrderMatcher();
        orderMatcher.placeOrder(new Order(1, Side.SELL, 10, 50));
        orderMatcher.placeOrder(new Order(2, Side.SELL, 10, 100));

        List<Order> orders = orderMatcher.getOrders(Side.SELL);
        assertThat(orders).hasSize(2);

        List<Trade> trades = orderMatcher.placeOrder(new Order(2, Side.BUY, 10, 150));
        System.out.println(trades.size() + " trades = " + trades);
        assertThat(trades).hasSize(2);

    }

    private void assertOrder(long price, long qty, Order order) {
        assertThat(order.getPrice()).isEqualTo(price);
        assertThat(order.getQuantity()).isEqualTo(qty);
    }

    private void assertTrade(long price, long qty, Trade trade) {
        assertThat(trade.getPrice()).isEqualTo(price);
        assertThat(trade.getQuantity()).isEqualTo(qty);
    }

    @Test
    public void addOrderOnSamePrice() throws Exception {
        OrderMatcher orderMatcher = new OrderMatcher();
        orderMatcher.placeOrder(new Order(1, Side.SELL, 5, 50));
        orderMatcher.placeOrder(new Order(2, Side.SELL, 5, 30));
        orderMatcher.placeOrder(new Order(3, Side.SELL, 5, 20));

        orderMatcher.placeOrder(new Order(1, Side.SELL, 4, 40));


        List<Order> orders = orderMatcher.getOrders(Side.SELL);
        System.out.println("orders = " + orders);

    }

//    @Test
//    public void testToString() {
//        assertEquals("BUY 100@5 #1", new Order(1, Side.BUY, 5, 100).toString());
//        assertEquals("BUY 100@5 #0", new Order(0, Side.BUY, 5, 100).toString());
//        assertEquals("SELL 100@5 #1", new Order(1, Side.SELL, 5, 100).toString());
//    }

}
