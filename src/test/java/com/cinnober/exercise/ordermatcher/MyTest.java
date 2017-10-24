package com.cinnober.exercise.ordermatcher;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class MyTest {

    @Test
    public void testMatch1() {
        OrderMatcher orderMatcher = new OrderMatcher();
        orderMatcher.addOrder(new Order(1, Side.BUY, 5, 100));

        List<Trade> trades = orderMatcher.addOrder(new Order(2, Side.SELL, 5, 50));
        System.out.println(trades.size() + " trades = " + trades);
        assertEquals(1, trades.size());
        assertTrade(5, 50, trades.get(0));

        List<Order> orders = orderMatcher.getOrders(Side.BUY);
        System.out.println("1 orders = " + orders);

        trades = orderMatcher.addOrder(new Order(3, Side.SELL, 5, 20));
        System.out.println("2 trades = " + trades);

        orders = orderMatcher.getOrders(Side.BUY);
        System.out.println("2 orders = " + orders);

        trades = orderMatcher.addOrder(new Order(4, Side.SELL, 5, 30));
        System.out.println("3 trades = " + trades);

        orders = orderMatcher.getOrders(Side.BUY);
        System.out.println("3 orders = " + orders);
    }

    @Test
    public void testAdIceBergOrderMatch1() {
        OrderMatcher orderMatcher = new OrderMatcher();
        orderMatcher.addOrder(new IceBergOrder(1, Side.BUY, 5, 100, 50));
        orderMatcher.getOrders(Side.BUY);
        List<Order> orders = orderMatcher.getOrders(Side.BUY);
        System.out.println("1 orders = " + orders);
//
//        List<Trade> trades = orderMatcher.addOrder(new Order(2, Side.SELL, 5, 50));
//        System.out.println(trades.size() + " trades = " + trades);
//        assertEquals(1, trades.size());
//        assertTrade(5, 50, trades.get(0));
//
//        List<Order> orders = orderMatcher.getOrders(Side.BUY);
//        System.out.println("1 orders = " + orders);
//
//        trades = orderMatcher.addOrder(new Order(3, Side.SELL, 5, 20));
//        System.out.println("2 trades = " + trades);
//
//        orders = orderMatcher.getOrders(Side.BUY);
//        System.out.println("2 orders = " + orders);
//
//        trades = orderMatcher.addOrder(new Order(4, Side.SELL, 5, 30));
//        System.out.println("3 trades = " + trades);
//
//        orders = orderMatcher.getOrders(Side.BUY);
//        System.out.println("3 orders = " + orders);
    }

    @Test
    public void testMatch12() {
        OrderMatcher orderMatcher = new OrderMatcher();
        orderMatcher.addOrder(new Order(1, Side.BUY, 5, 100));
        orderMatcher.addOrder(new Order(2, Side.BUY, 4, 100));

        List<Trade> trades = orderMatcher.addOrder(new Order(3, Side.SELL, 4, 300));
        System.out.println(trades.size() + " trades = " + trades);
        assertEquals(2, trades.size());
        assertTrade(5, 100, trades.get(0));
        assertTrade(4, 100, trades.get(1));

        List<Order> orders = orderMatcher.getOrders(Side.SELL);
        System.out.println(orders.size() + " orders = " + orders);
        assertEquals(1, orders.size());
        assertOrder(4, 100, orders.get(0));
    }

    @Test
    public void testMatch2() {
        OrderMatcher orderMatcher = new OrderMatcher();

        orderMatcher.addOrder(new Order(1, Side.SELL, 5, 100));
        orderMatcher.addOrder(new Order(2, Side.SELL, 4, 100));
        List<Order> orders = orderMatcher.getOrders(Side.SELL);
        System.out.println("orders = " + orders);

        assertOrder(4, 100, orders.get(0));
        assertOrder(5, 100, orders.get(1));

        List<Trade> trades = orderMatcher.addOrder(new Order(3, Side.BUY, 5, 50));
        assertEquals(1, trades.size());
        assertTrade(4, 50, trades.get(0));
        System.out.println(trades.size() + " trades = " + trades);
        orders = orderMatcher.getOrders(Side.SELL);
        System.out.println("orders = " + orders);
    }

    @Test
    public void testMatch3() {
        OrderMatcher orderMatcher = new OrderMatcher();

        orderMatcher.addOrder(new Order(1, Side.SELL, 5, 100));
        orderMatcher.addOrder(new Order(2, Side.SELL, 4, 100));
        List<Order> orders = orderMatcher.getOrders(Side.SELL);
        assertEquals(2, orders.size());
        System.out.println("orders = " + orders);

        assertOrder(4, 100, orders.get(0));
        assertOrder(5, 100, orders.get(1));

        List<Trade> trades = orderMatcher.addOrder(new Order(3, Side.BUY, 5, 300));
        assertEquals(2, trades.size());
        assertTrade(4, 100, trades.get(0));
        assertTrade(5, 100, trades.get(1));
        System.out.println(trades.size() + " trades = " + trades);
        orders = orderMatcher.getOrders(Side.BUY);
        System.out.println("orders = " + orders);
        assertEquals(1, orders.size());
        assertOrder(5, 100, orders.get(0));
    }

    @Test
    public void test6() throws Exception {

        OrderMatcher orderMatcher = new OrderMatcher();
        orderMatcher.addOrder(new Order(1, Side.SELL, 10, 50));
        orderMatcher.addOrder(new Order(2, Side.SELL, 10, 100));

        List<Order> orders = orderMatcher.getOrders(Side.SELL);
        Assert.assertEquals(2, orders.size());
        List<Trade> trades = orderMatcher.addOrder(new Order(2, Side.BUY, 10, 150));
        System.out.println(trades.size() + " trades = " + trades);
        Assert.assertEquals(2, trades.size());

    }

    private void assertOrder(long price, long qty, Order order) {
        assertEquals(price, order.getPrice());
        assertEquals(qty, order.getQuantity());
    }

    private void assertTrade(long price, long qty, Trade trade) {
        assertEquals(price, trade.getPrice());
        assertEquals(qty, trade.getQuantity());
    }

    @Test
    public void addOrderOnSamePrice() throws Exception {
        OrderMatcher orderMatcher = new OrderMatcher();
        orderMatcher.addOrder(new Order(1, Side.SELL, 5, 50));
        orderMatcher.addOrder(new Order(2, Side.SELL, 5, 30));
        orderMatcher.addOrder(new Order(3, Side.SELL, 5, 20));

        orderMatcher.addOrder(new Order(1, Side.SELL, 4, 40));


        List<Order> orders = orderMatcher.getOrders(Side.SELL);
        System.out.println("orders = " + orders);

    }

    @Test
    public void testToString() {
        assertEquals("BUY 100@5 #1", new Order(1, Side.BUY, 5, 100).toString());
        assertEquals("BUY 100@5 #0", new Order(0, Side.BUY, 5, 100).toString());
        assertEquals("SELL 100@5 #1", new Order(1, Side.SELL, 5, 100).toString());
    }

}
