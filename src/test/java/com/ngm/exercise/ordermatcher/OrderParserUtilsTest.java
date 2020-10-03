package com.ngm.exercise.ordermatcher;


import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;


public class OrderParserUtilsTest {

    private static final Order BUY_ORDER_5_100_1 = Order.buyOrder().id(1).price(5).qty(100).build();
    private static final Order BUY_ORDER_5_100_0 = Order.buyOrder().id(0).price(5).qty(100).build();
    private static final Order SELL_ORDER_5_100_1 = Order.sellOrder().id(1).price(5).qty(100).build();

    private static Stream<Arguments> provider() {
        return Stream.of(
            Arguments.of(BUY_ORDER_5_100_1, "BUY 100@5 #1"),
            Arguments.of(BUY_ORDER_5_100_1, "buy 100@5 #1"),
            Arguments.of(BUY_ORDER_5_100_0, "buy 100@5"),
            Arguments.of(BUY_ORDER_5_100_0, "buy 100@5 #0"),
            Arguments.of(SELL_ORDER_5_100_1, "SELL 100@5 #1"),
            Arguments.of(SELL_ORDER_5_100_1, "sell 100@5 #1"));
    }

    @ParameterizedTest()
    @MethodSource("provider")
    public void testParse(final Order expected, final String candidate) {
        assertThat(OrderParserUtil.parseOrder(candidate)).isEqualTo(expected);
    }

}
