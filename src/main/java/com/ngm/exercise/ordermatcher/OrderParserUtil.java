package com.ngm.exercise.ordermatcher;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderParserUtil {
    private static final Pattern PATTERN =
        Pattern.compile("(?<side>([bB][uU][yY])|([sS][eE][lL][lL]))[ ]+(?<qty>[0-9]+)[ ]*@[ ]*(?<px>[0-9]+)([ ]+#(?<id>[0-9]+))?");
    private static final String GROUP_ID = "id";
    private static final String GROUP_SIDE = "side";
    private static final String GROUP_QUANTITY = "qty";
    private static final String GROUP_PRICE = "px";

    public OrderParserUtil() {
    }

    public static Order2 parseOrder(final String order) {
        final Matcher m = PATTERN.matcher(order);
        if (!m.matches()) {
            throw new IllegalArgumentException("Illegal order format. Expected #id buy|sell quantity@price");
        }
        final String idStr = m.group(GROUP_ID);
        final long id = idStr != null ? Long.parseLong(idStr) : 0L;
        final Side side = Side.valueOf(m.group(GROUP_SIDE).toUpperCase());
        final long price = Long.parseLong(m.group(GROUP_PRICE));
        final long quantity = Long.parseLong(m.group(GROUP_QUANTITY));

        if (Side.SELL.equals(side)) {
            return Order2.sellOrder().id(id).price(price).qty(quantity).
                build();
        } else {
            return Order2.buyOrder().id(id).price(price).qty(quantity).build();
        }
    }

    public static Order2 parseOrder2(final String order) {
        try {
            final Scanner sc = new Scanner(order);
            sc.useDelimiter(" ");

            final Side sid = getSide(sc);
            final long[] qtyAndPrice = getQtyAndPrice(sc);
            long qty = qtyAndPrice[0];
            long price = qtyAndPrice[1];

            long id = getId(sc);

            if (Side.SELL.equals(sid)) {
                return Order2.sellOrder().id(id).price(price).qty(qty).build();
            } else {
                return Order2.buyOrder().id(id).price(price).qty(qty).build();
            }
        } catch (final Exception e) {
            throw new IllegalArgumentException(
                String.format(
                    "Illegal format. Expected: <buy|sell> <quantity>@<price> #<id>, where <quantity>,<price> and id is numeric ('%s')", order));
        }
    }

    private static Side getSide(final Scanner sc) {
        final String side = sc.next();
        if (side.equalsIgnoreCase("buy")) {
            return Side.BUY;
        } else if (side.equalsIgnoreCase("sell")) {
            return Side.SELL;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static long getId(final Scanner scanner) {
        long id = 0;
        if (scanner.hasNext()) {
            String dashAndId = scanner.next();
            String idStr = dashAndId.replace("#", "");
            id = Long.parseLong(idStr);
        }
        return id;
    }

    private static long[] getQtyAndPrice(final Scanner scanner) {
        final long[] priceAndQty = new long[2];
        final String qtyAndPrice = scanner.next();
        final String[] split = qtyAndPrice.split("@");
        priceAndQty[0] = Long.parseLong(split[0]);
        priceAndQty[1] = Long.parseLong(split[1]);
        return priceAndQty;
    }
}
