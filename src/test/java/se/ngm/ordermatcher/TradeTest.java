package se.ngm.ordermatcher;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TradeTest {

    @Test
    public void testToString() {
        assertThat(Trade.builder().qty(100).price(5).build().toString())
            .isEqualTo("TRADE 100@5");
    }

}
