package com.ngm.exercise.ordermatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static com.ngm.exercise.ordermatcher.OrderParserUtil.parseOrder;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;

public class OrderMatcherTest1 {

    private static Stream<Arguments> provider() {
        return Stream.of(
            Arguments.of(of("BUY 100@10", "SELL 60@10"), of("TRADE 60@10")),
            Arguments.of(of("BUY 50@9", "BUY 100@10", "SELL 150@10"), of("TRADE 100@10")),
            Arguments.of(of("SELL 50@11", "SELL 100@10", "BUY 150@10"), of("TRADE 100@10")),
            Arguments.of(of("SELL 50@10", "SELL 100@10", "BUY 150@10"), of("TRADE 50@10", "TRADE 100@10")),
            Arguments.of(of("SELL 100@10", "BUY 100@11"), of("TRADE 100@10")),
            Arguments.of(of("BUY 100@10", "SELL 100@9"), of("TRADE 100@10")),
            Arguments.of(of("BUY 100@10", "BUY 100@10", "BUY 100@11", "BUY 100@9 #4", "SELL 100@100", "SELL 100@100", "SELL 100@101", "SELL 100@99"), of()),
            Arguments.of(of("BUY 100@10", "BUY 100@10", "BUY 100@11", "BUY 100@9"), of()));

    }

    @ParameterizedTest()
    @MethodSource("provider")
    public void testMatch(final List<String> orders, final List<String> expectedTrades) {
        final OrderMatcher orderMatcher = new OrderMatcher();

        List<String> trades = new ArrayList<>();
        for (String order : orders) {
            List<Trade> tradeList = orderMatcher.placeOrder(parseOrder(order));
            for (Trade trade : tradeList) {
                trades.add(trade.toString());
            }
        }

        assertThat(trades).isEqualTo(expectedTrades);

    }
}
