package org.labs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class Programmers implements AutoCloseable {

    private final List<Programmer> programmers;
    private final ExecutorService executor;
    private final CyclicBarrier roundBarrier;
    private final AtomicBoolean simulationOver = new AtomicBoolean(false);

    private final DiningTable diningTable;
    private final Waiters waiters;

    private final long discussionDelay;
    private final long eatingDelay;

    public Programmers(
            DiningTable diningTable,
            Waiters waiters,
            int programmerCount,
            long eatingDelay,
            long discussionDelay
    ) {
        this.programmers = new ArrayList<>(programmerCount);
        this.executor = Executors.newFixedThreadPool(programmerCount);
        this.roundBarrier = new CyclicBarrier(programmerCount, this::endRound);
        this.diningTable = diningTable;
        this.waiters = waiters;
        this.discussionDelay = discussionDelay;
        this.eatingDelay = eatingDelay;

        start(programmerCount);
    }

    private void start(int count) {
        for (int i = 0; i < count; i++) {
            var programmer = new Programmer(i);

            programmers.add(programmer);
            executor.execute(programmer);
        }
    }

    private void endRound() {
        boolean anyoneAte = programmers.stream().anyMatch(Programmer::gotFoodThisRound);
        if (!anyoneAte) {
            simulationOver.set(true);
        }
    }

    public List<Programmer> list() {
        return Collections.unmodifiableList(programmers);
    }

    @Override
    public void close() {
        executor.close();
    }

    public class Programmer implements Runnable {

        private final int id;
        private volatile boolean gotFoodThisRound;
        private int eaten;

        public Programmer(int id) {
            this.id = id;
        }

        public int eaten() {
            return eaten;
        }

        public int id() {
            return id;
        }

        private boolean gotFoodThisRound() {
            return gotFoodThisRound;
        }

        @Override
        public void run() {
            try {
                while (!simulationOver.get()) {
                    gotFoodThisRound = waiters.getPortion().get();

                    if (gotFoodThisRound) {
                        try (DiningTable.EatingSession session = diningTable.startEating(id)) {
                            eat();
                        }
                    }

                    discuss();
                    roundBarrier.await();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (BrokenBarrierException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        private void discuss() throws InterruptedException {
            Thread.sleep(discussionDelay);
        }

        private void eat() throws InterruptedException {
            Thread.sleep(eatingDelay);
            eaten++;
        }
    }
}
