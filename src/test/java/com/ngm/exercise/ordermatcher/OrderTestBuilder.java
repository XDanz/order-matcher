package com.ngm.exercise.ordermatcher;

public class OrderTestBuilder {

    public static Order.SellOrderBuilder sellOrder() {
        return Order.sellOrder()
            .price(5)
            .qty(100);
    }

    public static Order.BuyOrderBuilder buyOrder() {
        return Order.buyOrder()
            .price(5)
            .qty(100);
    }
}
