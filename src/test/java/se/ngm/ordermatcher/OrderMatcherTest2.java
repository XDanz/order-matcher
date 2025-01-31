package se.ngm.ordermatcher;

import java.util.List;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class OrderMatcherTest2 {

    private static final Order BUY_100_at_5_ID_1 = Order.buyOrder().qty(100).price(5).build();
    private static final Order BUY_100_at_4_ID_2 = Order.buyOrder().qty(100).price(4).build();

    private static final Order BUY_300_at_5_ID_3 = Order.buyOrder().qty(300).price(5).build();
    private static final Order BUY_50_at_5_ID_3 = Order.buyOrder().qty(50).price(5).build();
    private static final Order BUY_150_at_10_ID_3 = Order.buyOrder().qty(150).price(10).build();

    private static final Order SELL_100_at_5_ID_1 = Order.sellOrder().qty(100).price(5).build();
    private static final Order SELL_100_at_4_ID_2 = Order.sellOrder().qty(100).price(4).build();
    private static final Order SELL_300_at_4_ID_2 = Order.sellOrder().qty(300).price(4).build();

    private static final Order SELL_50_at_5_ID_2 = Order.sellOrder().qty(50).price(5).build();
    private static final Order SELL_20_at_5_ID_3 = Order.sellOrder().qty(20).price(5).build();
    private static final Order SELL_30_at_5_ID_4 = Order.sellOrder().qty(30).price(5).build();

    private static final Order SELL_50_at_10_ID_1 = Order.sellOrder().qty(50).price(10).build();
    private static final Order SELL_100_at_10_ID_2 = Order.sellOrder().qty(100).price(10).build();

    @DisplayName("Match 1 queued order 3 times and check removal from order book")
    @Test
    public void test_match_all_qty() {
        final OrderMatcher orderMatcher = new OrderMatcher();
        orderMatcher.placeOrder(BUY_100_at_5_ID_1);

        List<Trade> trades = orderMatcher.placeOrder(SELL_50_at_5_ID_2);
        assertThat(trades).extracting("qty", "price")
            .contains(Tuple.tuple(50L, 5L));
        List<Order> orders = orderMatcher.getOrders(Side.BUY);
        assertThat(orders).extracting("qty", "price")
            .contains(Tuple.tuple(50L, 5L));

        trades = orderMatcher.placeOrder(SELL_20_at_5_ID_3);
        assertThat(trades).extracting("qty", "price")
            .contains(Tuple.tuple(20L, 5L));
        orders = orderMatcher.getOrders(Side.BUY);
        assertThat(orders).extracting("qty", "price")
            .contains(Tuple.tuple(30L, 5L));

        trades = orderMatcher.placeOrder(SELL_30_at_5_ID_4);
        assertThat(trades).extracting("qty", "price")
            .contains(Tuple.tuple(30L, 5L));
        orders = orderMatcher.getOrders(Side.BUY);
        assertThat(orders).hasSize(0);
    }

    @DisplayName("Match 2 orders with different price and has remaining qty (placed in order book)")
    @Test
    public void test_match_2_orders() {
        OrderMatcher orderMatcher = new OrderMatcher();
        orderMatcher.placeOrder(BUY_100_at_5_ID_1);
        orderMatcher.placeOrder(BUY_100_at_4_ID_2);
        List<Order> orders = orderMatcher.getOrders(Side.BUY);
        assertThat(orders).extracting("qty", "price")
            .contains(Tuple.tuple(100L, 5L), Tuple.tuple(100L, 4L));

        List<Trade> trades = orderMatcher.placeOrder(SELL_300_at_4_ID_2);

        assertThat(trades).extracting("qty", "price")
            .containsExactly(Tuple.tuple(100L, 5L), Tuple.tuple(100L, 4L));

        orders = orderMatcher.getOrders(Side.SELL);

        assertThat(orders).extracting("qty", "price")
            .containsExactly(Tuple.tuple(100L, 4L));
    }

    @DisplayName("2 queued orders match partial with better price")
    @Test
    public void test_match_partial() {
        OrderMatcher orderMatcher = new OrderMatcher();

        orderMatcher.placeOrder(SELL_100_at_5_ID_1);
        orderMatcher.placeOrder(SELL_100_at_4_ID_2);
        List<Order> orders = orderMatcher.getOrders(Side.SELL);
        assertThat(orders).extracting("price", "qty")
            .contains(tuple(4L, 100L), tuple(5L, 100L));

        List<Trade> trades = orderMatcher.placeOrder(BUY_50_at_5_ID_3);
        assertThat(trades).extracting("qty", "price")
            .containsExactly(Tuple.tuple(50L, 4L));

        orders = orderMatcher.getOrders(Side.SELL);
        assertThat(orders).extracting("price", "qty")
            .contains(tuple(4L, 50L), tuple(5L, 100L));
    }

    @DisplayName("2 queued orders match all with remaining (placed in order book)")
    @Test
    public void test_match_orders_with_remaining() {
        OrderMatcher orderMatcher = new OrderMatcher();
        orderMatcher.placeOrder(SELL_100_at_5_ID_1);
        orderMatcher.placeOrder(SELL_100_at_4_ID_2);
        List<Order> orders = orderMatcher.getOrders(Side.SELL);
        assertThat(orders).extracting("price", "qty")
            .contains(tuple(4L, 100L), tuple(5L, 100L));

        List<Trade> trades = orderMatcher.placeOrder(BUY_300_at_5_ID_3);
        assertThat(trades).extracting("qty", "price")
            .containsExactly(Tuple.tuple(100L, 4L), Tuple.tuple(100L, 5L));

        orders = orderMatcher.getOrders(Side.BUY);
        assertThat(orders).extracting("price", "qty")
            .contains(tuple(5L, 100L));
    }

    @DisplayName("2 queued orders match all none remaining")
    @Test
    public void test_match_all() {
        OrderMatcher orderMatcher = new OrderMatcher();
        orderMatcher.placeOrder(SELL_50_at_10_ID_1);
        orderMatcher.placeOrder(SELL_100_at_10_ID_2);

        List<Order> orders = orderMatcher.getOrders(Side.SELL);
        assertThat(orders).hasSize(2);

        List<Trade> trades = orderMatcher.placeOrder(BUY_150_at_10_ID_3);
        assertThat(trades).extracting("qty", "price")
            .containsExactly(Tuple.tuple(50L, 10L), Tuple.tuple(100L, 10L));

        orders = orderMatcher.getOrders(Side.SELL);
        assertThat(orders).isEmpty();
        orders = orderMatcher.getOrders(Side.BUY);
        assertThat(orders).isEmpty();
    }

}
