package com.ngm.exercise.ordermatcher;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderTest {

    @Test
    void test_order_values() {
        final Order order = Order.builder().id(1).price(10).qty(100)
            .side(Side.BUY)
            .build();
        assertThat(order).extracting("id", "price", "qty", "side")
            .containsExactly(1L, 10L, 100L, Side.BUY);
    }

    @Test
    void test_buy_order_values() {
        final Order order = Order.buyOrder().id(1).price(10).qty(100)
            .build();
        assertThat(order).extracting("id", "price", "qty", "side")
            .containsExactly(1L, 10L, 100L, Side.BUY);
    }

    @Test
    void test_sell_order_values() {
        final Order order = Order.sellOrder().id(1).price(10).qty(100)
            .build();
        assertThat(order).extracting("id", "price", "qty", "side")
            .containsExactly(1L, 10L, 100L, Side.SELL);
    }
}
