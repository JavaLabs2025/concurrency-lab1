package org.labs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public final class DiningTable {

    private final List<Lock> spoons = new ArrayList<>();
    private final Lock tableLock = new ReentrantLock(true);

    public DiningTable(int programmers) {
        if (programmers < 2) {
            throw new IllegalArgumentException("At least 2 programmers are required");
        }

        IntStream.range(0, programmers).forEach(i -> spoons.add(new ReentrantLock()));
    }

    public EatingSession tryStartEating(Programmer programmer) {
        tableLock.lock();
        try {
            Lock left = spoons.get(programmer.id());
            Lock right = spoons.get((programmer.id() + 1) % spoons.size());

            left.lock();

            boolean rightAcquired = right.tryLock(new Random().nextLong(50), TimeUnit.MILLISECONDS);
            if (!rightAcquired) {
                left.unlock();
                return null;
            }

            return new EatingSession(left, right);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            tableLock.unlock();
        }
    }

    public static final class EatingSession implements AutoCloseable {

        private final Lock left;
        private final Lock right;

        private EatingSession(Lock left, Lock right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public void close() {
            right.unlock();
            left.unlock();
        }
    }
}