package se.ngm.ordermatcher;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class OrderTest {

    @Test
    void test_order_values() {
        final Order order = Order.builder().price(10).qty(100)
            .side(Side.BUY)
            .build();
        assertThat(order).extracting("price", "qty", "side")
            .containsExactly(10L, 100L, Side.BUY);
    }

    @Test
    void test_buy_order_values() {
        final Order order = Order.buyOrder().price(10).qty(100)
            .build();
        assertThat(order).extracting("price", "qty", "side")
            .containsExactly(10L, 100L, Side.BUY);
    }

    @Test
    void test_sell_order_values() {
        final Order order = Order.sellOrder().price(10).qty(100)
            .build();
        assertThat(order).extracting("price", "qty", "side")
            .containsExactly(10L, 100L, Side.SELL);
    }

    @Test
    void test_zero() {
        Throwable throwable = catchThrowable(() -> Order.sellOrder()
            .price(0)
            .qty(100)
            .build());
        assertThat(throwable).isInstanceOf(IllegalArgumentException.class)
            .hasMessage("price must be >0");

        throwable = catchThrowable(() -> Order.buyOrder()
            .price(0)
            .qty(100)
            .build());
        assertThat(throwable).isInstanceOf(IllegalArgumentException.class)
            .hasMessage("price must be >0");
    }
}
