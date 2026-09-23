package org.labs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Programmers implements AutoCloseable {

    private final List<Programmer> programmers;
    private final ExecutorService executor;

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
        this.diningTable = diningTable;
        this.waiters = waiters;
        this.discussionDelay = discussionDelay;
        this.eatingDelay = eatingDelay;

        start(programmerCount);
    }

    private void start(int count) {
        for (int i = 0; i < count; i++) {
            var programmer = new Programmer(i);

            executor.execute(programmer);
            programmers.add(programmer);
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
        private boolean hasSoup;
        private boolean alreadyDiscussed;
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

                    try (DiningTable.EatingSession session = diningTable.tryStartEating(id)) {
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
            Future<Boolean> future = waiters.getPortion();

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
}
