package org.labs;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class Programmer implements Runnable {

    private final int id;

    private final DiningTable table;

    private final ExecutorService waiters;
    private final long waiterDelay;
    private final FoodStorage foodStorage;

    private final long discussionDelay;
    private final long eatingDelay;

    private volatile boolean hasSoup;
    private boolean alreadyDiscussed;
    private int eaten;

    public Programmer(
            int id,
            DiningTable table,
            ExecutorService waiters,
            long waiterDelay,
            FoodStorage foodStorage,
            long discussionDelay,
            long eatingDelay
    ) {
        this.id = id;
        this.table = table;
        this.waiters = waiters;
        this.waiterDelay = waiterDelay;
        this.foodStorage = foodStorage;
        this.discussionDelay = discussionDelay;
        this.eatingDelay = eatingDelay;
    }

    public int eaten() {
        return eaten;
    }

    public int id() {
        return id;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (!alreadyDiscussed) {
                    discuss();
                }

                if (!hasSoup) {
                    if (!getPortion()) {
                        break;
                    }
                }

                try (DiningTable.EatingSession session = table.tryStartEating(this)) {
                    if (session == null) {
                        continue;
                    }
                    eat();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean getPortion() throws InterruptedException {
        Future<Boolean> future = waiters.submit(this::servePortion);

        try {
            boolean received = future.get();

            if (received) {
                hasSoup = true;
            }

            return received;
        } catch (ExecutionException e) {
            throw new RuntimeException("Waiter failed to serve portion", e.getCause());
        }
    }

    private boolean servePortion() throws InterruptedException {
        Thread.sleep(waiterDelay);
        return foodStorage.takePortion();
    }

    private void discuss() throws InterruptedException {
        Thread.sleep(discussionDelay);
        alreadyDiscussed = true;
    }

    private void eat() throws InterruptedException {
        Thread.sleep(eatingDelay);
        hasSoup = false;
        alreadyDiscussed = false;
        eaten++;
    }
}
