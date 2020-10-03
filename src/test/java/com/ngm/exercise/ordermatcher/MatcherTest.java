package com.ngm.exercise.ordermatcher;


import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static java.util.List.of;

class MatcherTest {
    private final static long PRICE_100 = 100L;
    private Map.Entry<Long, QueuedOrdersAtPrice> entry;

    private static Stream<Arguments> provider() {
        return Stream.of(
            Arguments.of(3, 3,
                of(Trade.builder().actOrdId(3).price(PRICE_100).queuedOrdId(1).qty(3).build())),
            Arguments.of(75, 3,
                of(Trade.builder().actOrdId(3).price(PRICE_100).queuedOrdId(1).qty(50).build(),
                    Trade.builder().actOrdId(3).price(PRICE_100).queuedOrdId(2).qty(25).build())),
            Arguments.of(100, 3,
                of(Trade.builder().actOrdId(3).price(PRICE_100).queuedOrdId(1).qty(50).build(),
                    Trade.builder().actOrdId(3).price(PRICE_100).queuedOrdId(2).qty(50).build())),
            Arguments.of(150, 3,
                of(Trade.builder().actOrdId(3).price(PRICE_100).queuedOrdId(1).qty(50).build(),
                    Trade.builder().actOrdId(3).price(PRICE_100).queuedOrdId(2).qty(50).build())));
    }

    @BeforeEach
    void setUp() {
        QueuedOrdersAtPrice ordersAtPrice = new QueuedOrdersAtPrice();
        ordersAtPrice.addOrder(OrderTestBuilder.buyOrder()
            .id(1).qty(50).build());
        ordersAtPrice.addOrder(OrderTestBuilder.buyOrder()
            .id(2).qty(50).build());
        entry =
            MapEntry.entry(PRICE_100, ordersAtPrice);
    }

    @ParameterizedTest()
    @MethodSource("provider")
    void matchAtPrice(final long qty, final long activeOrdId, final List<Trade> expected) {
        List<Trade> trades = Matcher.matchAtPrice(entry, qty, activeOrdId);
        Assertions.assertThat(trades).isEqualTo(expected);
    }
}