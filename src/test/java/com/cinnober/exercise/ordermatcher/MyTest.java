package com.cinnober.exercise.ordermatcher;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MyTest {


    @Test
    public void testParse() {
        OrderBook o = new OrderBook("2x7");
        o.addOrder(new Order(1, Side.BUY, 5, 100));

    }

    @Test
    public void testToString() {
        assertEquals("BUY 100@5 #1", new Order(1, Side.BUY, 5, 100).toString());
        assertEquals("BUY 100@5 #0", new Order(0, Side.BUY, 5, 100).toString());
        assertEquals("SELL 100@5 #1", new Order(1, Side.SELL, 5, 100).toString());
    }

}
