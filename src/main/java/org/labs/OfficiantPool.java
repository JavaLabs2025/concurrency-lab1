package org.labs;

import java.util.concurrent.Semaphore;

public class OfficiantPool {
    private final Semaphore availableOfficiant;
    private final FoodSupply foodSupply;

    public OfficiantPool (int officiantCount, FoodSupply foodSupply){
        this.availableOfficiant = new Semaphore(officiantCount,true);
        this.foodSupply = foodSupply;
    }

    public boolean requestFood() throws InterruptedException {
        availableOfficiant.acquire();
        try {
            return foodSupply.takePortion();
        } finally {
            availableOfficiant.release();
        }
    }
}
