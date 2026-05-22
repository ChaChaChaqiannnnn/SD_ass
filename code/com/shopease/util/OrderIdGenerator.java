package com.shopease.util;

import com.shopease.dao.OrderDAO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Order IDs: dd/MM/yy-NNN (date prefix + daily sequence), e.g. 21/05/26-001.
 */
public final class OrderIdGenerator {
    private static final DateTimeFormatter DATE_PREFIX = DateTimeFormatter.ofPattern("dd/MM/yy");

    private OrderIdGenerator() {}

    public static String nextOrderId(OrderDAO orderDAO) {
        String datePrefix = LocalDate.now().format(DATE_PREFIX);
        int sequence = orderDAO.nextSequenceForDatePrefix(datePrefix);
        return datePrefix + "-" + String.format("%03d", sequence);
    }
}
