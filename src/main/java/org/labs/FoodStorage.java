package org.labs;

import java.util.concurrent.atomic.AtomicInteger;

public class FoodStorage {

    private final AtomicInteger portions;

    public FoodStorage(int portions) {
        if (portions < 0) {
            throw new IllegalArgumentException("portions must be a positive");
        }

        this.portions = new AtomicInteger(portions);
    }

    public boolean takePortion() {
        return portions.getAndDecrement() > 0;
    }

    public int remaining() {
        return portions.get();
    }
}
