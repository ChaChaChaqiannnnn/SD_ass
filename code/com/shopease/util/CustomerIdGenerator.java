package com.shopease.util;

import java.util.concurrent.atomic.AtomicInteger;

/** Unique customer IDs for registration (avoids millis collisions on double-click). */
public final class CustomerIdGenerator {
    private static final AtomicInteger SEQ = new AtomicInteger();

    private CustomerIdGenerator() {}

    public static String nextId() {
        return "CUST-" + System.currentTimeMillis() + "-" + SEQ.incrementAndGet();
    }
}
