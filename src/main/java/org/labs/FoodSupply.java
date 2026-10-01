package org.labs;

import java.util.concurrent.atomic.AtomicInteger;

public class FoodSupply {
    private final AtomicInteger remaining;

    public FoodSupply(int totalPortions){
        this.remaining = new AtomicInteger(totalPortions);
    }

    public boolean takePortion(){
        int previous = remaining.getAndUpdate(current -> current > 0 ? current - 1 : current);
        return previous > 0;
    }

    public int getRemaining(){
        return remaining.get();
    }
}
