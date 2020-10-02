package com.ngm.exercise.ordermatcher;


import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static com.ngm.exercise.ordermatcher.Order.parse;
import static org.assertj.core.api.Assertions.assertThat;


public class OrderTest {

    private static Stream<Arguments> provider() {
        return Stream.of(
            Arguments.of(new Order(1, Side.BUY, 5, 100), "BUY 100@5 #1"),
            Arguments.of(new Order(1, Side.BUY, 5, 100), "buy 100@5 #1"),
            Arguments.of(new Order(0, Side.BUY, 5, 100), "buy 100@5"),
            Arguments.of(new Order(0, Side.BUY, 5, 100), "buy 100@5 #0"),
            Arguments.of(new Order(1, Side.SELL, 5, 100), "SELL 100@5 #1"),
            Arguments.of(new Order(1, Side.SELL, 5, 100), "sell 100@5 #1"));
    }

    @ParameterizedTest
    @MethodSource("provider")
    public void testParse(final Order expected, final String candidate) {

        assertThat(expected).isEqualTo(parse(candidate));
//        assertEquals(new Order(1, Side.BUY, 5, 100), parse("buy 100@5 #1"));
//        assertEquals(new Order(0, Side.BUY, 5, 100), parse("buy 100@5"));
//        assertEquals(new Order(0, Side.BUY, 5, 100), parse("buy 100@5 #0"));
//        assertEquals(new Order(1, Side.SELL, 5, 100), parse("SELL 100@5 #1"));
//        assertEquals(new Order(1, Side.SELL, 5, 100), parse("sell 100@5 #1"));
    }

//    @Test
//    public void testToString() {
//        assertEquals("BUY 100@5 #1", new Order(1, Side.BUY, 5, 100).toString());
//        assertEquals("BUY 100@5 #0", new Order(0, Side.BUY, 5, 100).toString());
//        assertEquals("SELL 100@5 #1", new Order(1, Side.SELL, 5, 100).toString());
//    }


}
