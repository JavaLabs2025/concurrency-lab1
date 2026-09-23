package org.labs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public final class DiningTable {

    private final List<Lock> spoons = new ArrayList<>();

    public DiningTable(int programmers) {
        if (programmers < 2) {
            throw new IllegalArgumentException("At least 2 programmers are required");
        }

        IntStream.range(0, programmers).forEach(i -> spoons.add(new ReentrantLock()));
    }

    public EatingSession tryStartEating(int programmerId) throws InterruptedException {
        Lock left = null;
        Lock right = null;
        try {
            left = spoons.get(programmerId);
            right = spoons.get((programmerId + 1) % spoons.size());

            left.lock();

            boolean rightAcquired = right.tryLock(ThreadLocalRandom.current().nextLong(50), TimeUnit.MILLISECONDS);
            if (!rightAcquired) {
                left.unlock();
                return null;
            }

            return new EatingSession(left, right);
        } catch (InterruptedException e) {
            if (left != null) {
                left.unlock();
            }

            if (right != null) {
                right.unlock();
            }

            Thread.currentThread().interrupt();
            throw e;
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