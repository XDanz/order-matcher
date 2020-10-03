package com.ngm.exercise.ordermatcher;

import java.util.ArrayList;
import java.util.List;

public class QueuedOrdersAtPrice {
    private final List<Order> orders = new ArrayList<>();

    long getTotalQuantity() {
        return orders.stream()
            .mapToLong(Order::getQty)
            .sum();
    }

    List<Order> getOrders() {
        return orders;
    }

    void addOrder(final Order order) {
        orders.add(order);
    }

}
