package com.ngm.exercise.ordermatcher;

import java.util.Scanner;

public class OrderParserUtil {

    public static Order parseOrder(final String order) {
        try {
            final Scanner sc = new Scanner(order);
            sc.useDelimiter(" ");

            final Side sid = getSide(sc);
            final long[] qtyAndPrice = getQtyAndPrice(sc);
            long qty = qtyAndPrice[0];
            long price = qtyAndPrice[1];

            if (Side.SELL.equals(sid)) {
                return Order.sellOrder().price(price).qty(qty).build();
            } else {
                return Order.buyOrder().price(price).qty(qty).build();
            }
        } catch (final Exception e) {
            throw new IllegalArgumentException(
                String.format(
                    "Illegal format. Expected: <buy|sell> <quantity>@<price>, where <quantity>,<price> is numeric ('%s')", order));
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
