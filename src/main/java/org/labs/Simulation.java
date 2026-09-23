package org.labs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public final class Simulation {

    private final int programmerCount;
    private final int waiterCount;
    private final int foodAmount;

    private final long discussionDelay;
    private final long eatingDelay;
    private final long waiterDelay;

    public Simulation(
            int programmerCount,
            int waiterCount,
            int foodAmount,
            long discussionDelay,
            long eatingDelay,
            long waiterDelay
    ) {
        if (programmerCount < 2) {
            throw new IllegalArgumentException(
                    "At least 2 programmers required"
            );
        }

        if (waiterCount < 1) {
            throw new IllegalArgumentException(
                    "At least 1 waiter required"
            );
        }

        this.programmerCount = programmerCount;
        this.waiterCount = waiterCount;
        this.foodAmount = foodAmount;
        this.discussionDelay = discussionDelay;
        this.eatingDelay = eatingDelay;
        this.waiterDelay = waiterDelay;
    }

    public Result run() throws InterruptedException {

        FoodStorage storage =
                new FoodStorage(foodAmount);

        DiningTable table =
                new DiningTable(programmerCount);

        ExecutorService waiterExecutor = Executors.newFixedThreadPool(waiterCount);

        List<Programmer> programmers =
                new ArrayList<>();

        for (int i = 0; i < programmerCount; i++) {
            programmers.add(new Programmer(
                    i,
                    table,
                    waiterExecutor,
                    waiterDelay,
                    storage,
                    discussionDelay,
                    eatingDelay
            ));
        }

        ExecutorService programmerExecutor = Executors.newFixedThreadPool(programmerCount);
        for (Programmer programmer : programmers) {
            programmerExecutor.execute(programmer);
        }

        var result = new Result(programmers);

        waitForSimulation(storage);

        programmerExecutor.shutdownNow();
        waiterExecutor.shutdownNow();
        programmerExecutor.close();
        waiterExecutor.close();

        return result;
    }

    private void waitForSimulation(FoodStorage storage) throws InterruptedException {
        while (storage.remaining() > 0) {
            Thread.sleep(10);
        }

        Thread.sleep(waiterDelay * 2L + 100);
    }

    public record Result(List<Programmer> programmers) {
        public int totalEaten() {
            return programmers.stream()
                    .mapToInt(Programmer::eaten)
                    .sum();
        }

        public int minEaten() {
            return programmers.stream()
                    .mapToInt(Programmer::eaten)
                    .min()
                    .orElse(0);
        }

        public int maxEaten() {
            return programmers.stream()
                    .mapToInt(Programmer::eaten)
                    .max()
                    .orElse(0);
        }
    }
}
