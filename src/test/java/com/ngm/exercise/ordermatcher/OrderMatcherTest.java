package com.ngm.exercise.ordermatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderMatcherTest {

    public OrderMatcherTest() {
    }

    // --- BASIC ORDER HANDLING ---

    @Test
    public void testEmpty() {
        testMatch(emptyList(),emptyList(),emptyList());
    }

    @Test
    public void testSingleBuyOrderNoMatch1() {
        testMatch(
            asList("BUY 100@10 #1"),
            asList(),
            asList("BUY 100@10 #1")
        );
    }

    @Test
    public void testSingleSellOrderNoMatch1() {
        testMatch(
            asList("SELL 100@10 #1"),
            asList(),
            asList("SELL 100@10 #1")
        );
    }

    @Test
    public void testSingleSellOrderAndSingleBuyOrderNoMatch1() {
        testMatch(
            asList("BUY 100@10 #1", "SELL 100@20 #1"),
            asList(),
            asList("BUY 100@10 #1", "SELL 100@20 #1")
        );
    }

    // --- ORDER PRIORITIES ---

    @Test
    public void testPassiveOrderPriority1() {
        testMatch(
            asList("BUY 100@10 #1", "BUY 100@10 #2", "BUY 100@11 #3", "BUY 100@9 #4"),
            asList(),
            asList("BUY 100@11 #3", "BUY 100@10 #1", "BUY 100@10 #2", "BUY 100@9 #4")
        );
    }

    @Test
    public void testPassiveOrderPriority2() {
        testMatch(
            asList("SELL 100@100 #5", "SELL 100@100 #6", "SELL 100@101 #7", "SELL 100@99 #8"),
            asList(),
            asList("SELL 100@99 #8", "SELL 100@100 #5", "SELL 100@100 #6", "SELL 100@101 #7")
        );
    }

    @Test
    public void testPassiveOrderPriority3() {
        testMatch(
            asList("BUY 100@10 #1", "BUY 100@10 #2", "BUY 100@11 #3", "BUY 100@9 #4",
                          "SELL 100@100 #5", "SELL 100@100 #6", "SELL 100@101 #7", "SELL 100@99 #8"),
            asList(),
            asList("BUY 100@11 #3", "BUY 100@10 #1", "BUY 100@10 #2", "BUY 100@9 #4",
                          "SELL 100@99 #8", "SELL 100@100 #5", "SELL 100@100 #6", "SELL 100@101 #7")
        );
    }

    // --- MATCHING AND TRADES ---

    @Test
    public void testMatchPassiveSetsThePrice1() {
        testMatch(
            asList("BUY 100@10 #1", "SELL 100@9 #2"),
            asList("TRADE 100@10 (#2/#1)"),
            asList()
        );
    }

    @Test
    public void testMatchPassiveSetsThePrice2() {
        testMatch(
            asList("SELL 100@10 #1", "BUY 100@11 #2"),
            asList("TRADE 100@10 (#2/#1)"),
            asList()
        );
    }

    @Test
    public void testMatchTimePriority1() {
        testMatch(
            asList("BUY 50@10 #1", "BUY 100@10 #2", "SELL 150@10 #3"),
            asList("TRADE 50@10 (#3/#1)", "TRADE 100@10 (#3/#2)"),
            asList()
        );
    }

    @Test
    public void testMatchTimePriority2() {
        testMatch(
            asList("SELL 50@10 #1", "SELL 100@10 #2", "BUY 150@10 #3"),
            asList("TRADE 50@10 (#3/#1)", "TRADE 100@10 (#3/#2)"),
            asList()
        );
    }

    @Test
    public void testMatchPricePriority1() {
        testMatch(
            asList("SELL 50@11 #1", "SELL 100@10 #2", "BUY 150@10 #3"),
            asList("TRADE 100@10 (#3/#2)"),
            asList("BUY 50@10 #3", "SELL 50@11 #1")
        );
    }

    @Test
    public void testMatchPricePriority2() {
        testMatch(
            asList("BUY 50@9 #1", "BUY 100@10 #2", "SELL 150@10 #3"),
            asList("TRADE 100@10 (#3/#2)"),
            asList("BUY 50@9 #1", "SELL 50@10 #3")
        );
    }

    @Test
    public void testMatchPartialPassive1() {
        testMatch(
            asList("BUY 100@10 #1", "SELL 60@10 #2"),
            asList("TRADE 60@10 (#2/#1)"),
            asList("BUY 40@10 #1")
        );
    }

    @Test
    public void testMatchPartialActive1() {
        testMatch(
            asList("BUY 60@10 #1", "SELL 100@10 #2"),
            asList("TRADE 60@10 (#2/#1)"),
            asList("SELL 40@10 #2")
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
    private void testMatch(final List<String> inputOrders, final List<String> expTrades, List<String> expRemainingOrders) {
        OrderMatcher matcher = new OrderMatcher();
        ArrayList<Trade> trades = new ArrayList<>();
        // add orders
        inputOrders.stream().map(Order::parse).forEach(o -> trades.addAll(matcher.placeOrder(o)));

        // verify trades
        assertEquals(expTrades, trades.stream().map(Trade::toString).collect(Collectors.toList()));

        // verify remaining orders
        assertEquals(expRemainingOrders,
                     Stream.concat(matcher.getOrders(Side.BUY).stream(), matcher.getOrders(Side.SELL).stream()).
                             map(Order::toString).collect(Collectors.toList()));
    }

}
