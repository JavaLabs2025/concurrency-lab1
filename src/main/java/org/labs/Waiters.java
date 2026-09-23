package org.labs;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Waiters implements AutoCloseable {

    private final FoodStorage foodStorage;
    private final long waiterDelay;
    private final ExecutorService executor;

    public Waiters(
            FoodStorage foodStorage,
            long waiterDelay,
            int waiters
    ) {
        this.foodStorage = foodStorage;
        this.waiterDelay = waiterDelay;
        this.executor = Executors.newFixedThreadPool(waiters);
    }

    public Future<Boolean> getPortion() {
        return executor.submit(new Waiter());
    }

    @Override
    public void close() {
        executor.close();
    }

    private class Waiter implements Callable<Boolean> {

        @Override
        public Boolean call() throws Exception {
            Thread.sleep(waiterDelay);
            return foodStorage.takePortion();
        }
    }
}
