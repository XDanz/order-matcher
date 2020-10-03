package com.ngm.exercise.ordermatcher;


import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;


public class OrderParserUtilsTest {

    private static final Order2 BUY_ORDER_5_100_1 = Order2.buyOrder().id(1).price(5).qty(100).build();
    private static final Order2 BUY_ORDER_5_100_0 = Order2.buyOrder().id(0).price(5).qty(100).build();
    private static final Order2 SELL_ORDER_5_100_1 = Order2.sellOrder().id(1).price(5).qty(100).build();

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
    public void testParse(final Order2 expected, final String candidate) {
        assertThat(OrderParserUtil.parseOrder2(candidate)).isEqualTo(expected);
    }

    @Test
    void name() {
        assertThat(OrderParserUtil.parseOrder2("BUY 100@5 #1"))
            .isEqualTo(Order2.buyOrder()
                .id(1)
                .price(5)
                .qty(100)
                .build());
    }

    @Test
    void name2() {
        assertThat(OrderParserUtil.parseOrder2("buy 100@5 #1"))
            .isEqualTo(Order2.buyOrder().id(1).price(5).qty(100).build());
    }

    @Test
    void name3() {
        assertThat(OrderParserUtil.parseOrder2("buy 100@5"))
            .isEqualTo(Order2.buyOrder().id(0).price(5).qty(100).build());
    }

    @Test
    void name4() {
        assertThat(OrderParserUtil.parseOrder2("sell 100@5"))
            .isEqualTo(Order2.sellOrder().id(0).price(5).qty(100).build());
    }


}
