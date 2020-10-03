package se.ngm.ordermatcher;


import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;


public class OrderParserUtilsTest {

    private static final Order BUY_100_at_5 = Order.buyOrder().price(5).qty(100).build();
    private static final Order SELL_100_at_5 = Order.sellOrder().price(5).qty(100).build();

    private static Stream<Arguments> provider() {
        return Stream.of(
            Arguments.of(BUY_100_at_5, "BUY 100@5"),
            Arguments.of(BUY_100_at_5, "buy 100@5"),
            Arguments.of(BUY_100_at_5, "bUy 100@5"),
            Arguments.of(SELL_100_at_5, "SELL 100@5"),
            Arguments.of(SELL_100_at_5, "SeLl 100@5"),
            Arguments.of(SELL_100_at_5, "sell 100@5"));
    }

    @ParameterizedTest()
    @MethodSource("provider")
    public void testParse(final Order expected, final String candidate) {
        assertThat(OrderParserUtil.parseOrder(candidate)).isEqualTo(expected);
    }

}
