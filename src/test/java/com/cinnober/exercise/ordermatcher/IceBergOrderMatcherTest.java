package com.cinnober.exercise.ordermatcher;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class IceBergOrderMatcherTest {


    public IceBergOrderMatcherTest() {
    }

    // --- BASIC ORDER HANDLING ---

    @Test
    public void testEmpty() {
        testMatch(
                Arrays.asList(),
                Arrays.asList(),
                Arrays.asList()
        );
    }

    @Test
    public void testSingleBuyOrderNoMatch1() {
        testMatch(
                Arrays.asList("BUY 50/100@10 #1"),
                Arrays.asList(),
                Arrays.asList("BUY 50/100@10 #1")
        );
    }

    @Test
    public void testSingleSellOrderNoMatch1() {
        testMatch(
                Arrays.asList("SELL 30/100@10 #1"),
                Arrays.asList(),
                Arrays.asList("SELL 30/100@10 #1")
        );
    }

    @Test
    public void testSingleSellOrderAndSingleBuyOrderNoMatch1() {
        testMatch(
                Arrays.asList("BUY 10/100@10 #1", "SELL 10/100@20 #1"),
                Arrays.asList(),
                Arrays.asList("BUY 10/100@10 #1", "SELL 10/100@20 #1")
        );
    }

    // --- ORDER PRIORITIES ---

    @Test
    public void testPassiveOrderPriority1() {
        testMatch(
                Arrays.asList("BUY 10/100@10 #1", "BUY 10/100@10 #2", "BUY 10/100@11 #3", "BUY 10/100@9 #4"),
                Arrays.asList(),
                Arrays.asList("BUY 10/100@11 #3", "BUY 10/100@10 #1", "BUY 10/100@10 #2", "BUY 10/100@9 #4")
        );
    }

    @Test
    public void testPassiveOrderPriority2() {
        testMatch(
                Arrays.asList("SELL 10/100@100 #5", "SELL 10/100@100 #6", "SELL 10/100@101 #7", "SELL 10/100@99 #8"),
                Arrays.asList(),
                Arrays.asList("SELL 10/100@99 #8", "SELL 10/100@100 #5", "SELL 10/100@100 #6", "SELL 10/100@101 #7")
        );
    }

    @Test
    public void testPassiveOrderPriority3() {
        testMatch(
                Arrays.asList("BUY 100@10 #1", "BUY 100@10 #2", "BUY 100@11 #3", "BUY 100@9 #4",
                        "SELL 100@100 #5", "SELL 100@100 #6", "SELL 100@101 #7", "SELL 100@99 #8"),
                Arrays.asList(),
                Arrays.asList("BUY 100@11 #3", "BUY 100@10 #1", "BUY 100@10 #2", "BUY 100@9 #4",
                        "SELL 100@99 #8", "SELL 100@100 #5", "SELL 100@100 #6", "SELL 100@101 #7")
        );
    }

    // --- MATCHING AND TRADES ---

    @Test
    public void testMatchPassiveSetsThePrice1() {
        testMatch(
                Arrays.asList("BUY 10/100@10 #1", "SELL 100@9 #2"),
                Arrays.asList("TRADE 100@10 (#2/#1)"),
                Arrays.asList()
        );
    }

    @Test
    public void testMatchPassiveSetsThePrice2() {
        testMatch(
                Arrays.asList("SELL 100@10 #1", "BUY 100@11 #2"),
                Arrays.asList("TRADE 100@10 (#2/#1)"),
                Arrays.asList()
        );
    }

    @Test
    public void testMatchTimePriority1() {
        testMatch(
                Arrays.asList("BUY 50@10 #1", "BUY 10/100@10 #2", "SELL 150@10 #3"),
                Arrays.asList("TRADE 50@10 (#3/#1)", "TRADE 100@10 (#3/#2)"),
                Arrays.asList()
        );
    }

    @Test
    public void testMatchTimePriority2() {
        testMatch(
                Arrays.asList("SELL 50@10 #1", "SELL 100@10 #2", "BUY 150@10 #3"),
                Arrays.asList("TRADE 50@10 (#3/#1)", "TRADE 100@10 (#3/#2)"),
                Arrays.asList()
        );
    }

    @Test
    public void testMatchPricePriority1() {
        testMatch(
                Arrays.asList("SELL 50@11 #1", "SELL 100@10 #2", "BUY 150@10 #3"),
                Arrays.asList("TRADE 100@10 (#3/#2)"),
                Arrays.asList("BUY 50@10 #3", "SELL 50@11 #1")
        );
    }

    @Test
    public void testMatchPricePriority2() {
        testMatch(
                Arrays.asList("BUY 50@9 #1", "BUY 100@10 #2", "SELL 150@10 #3"),
                Arrays.asList("TRADE 100@10 (#3/#2)"),
                Arrays.asList("BUY 50@9 #1", "SELL 50@10 #3")
        );
    }

    @Test
    public void testMatchPartialPassive1() {
        testMatch(
                Arrays.asList("BUY 100@10 #1", "SELL 60@10 #2"),
                Arrays.asList("TRADE 60@10 (#2/#1)"),
                Arrays.asList("BUY 40@10 #1")
        );
    }

    @Test
    public void testMatchPartialActive1() {
        testMatch(
                Arrays.asList("BUY 60@10 #1", "SELL 100@10 #2"),
                Arrays.asList("TRADE 60@10 (#2/#1)"),
                Arrays.asList("SELL 40@10 #2")
        );
    }

    /**
     * Test order matcher.
     *
     * @param inputOrders the orders to add to the order book, not null.
     * @param expTrades the expected trades, in match order.
     * @param expRemainingOrders the expected remaining orders after matching all orders.
     * First buy orders then sell orders, both in priority order.
     */
    private void testMatch(List<String> inputOrders, List<String> expTrades, List<String> expRemainingOrders) {
        OrderMatcher matcher = new OrderMatcher();
        ArrayList<Trade> trades = new ArrayList<>();
        // add orders
        inputOrders.stream().map(s ->  { try {return IceBergOrder.parse(s);} catch (Exception e) { return Order.parse(s);}}  ).forEach(o -> trades.addAll(matcher.addOrder(o)));

        // verify trades
        assertEquals(expTrades, trades.stream().map(Trade::toString).collect(Collectors.toList()));
//
//        // verify remaining orders
        assertEquals(expRemainingOrders,
                Stream.concat(matcher.getOrders(Side.BUY).stream(), matcher.getOrders(Side.SELL).stream()).
                        map(Order::toString).collect(Collectors.toList()));
    }

}
