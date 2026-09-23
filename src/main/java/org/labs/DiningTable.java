package org.labs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public final class DiningTable {

    private final int programmers;
    private final List<Lock> spoons = new ArrayList<>();

    public DiningTable(int programmers) {
        if (programmers < 2) {
            throw new IllegalArgumentException("At least 2 programmers are required");
        }

        this.programmers = programmers;
        IntStream.range(0, programmers).forEach(i -> spoons.add(new ReentrantLock()));
    }

    public EatingSession startEating(int programmerId) {
        Lock left = spoons.get(programmerId);
        Lock right = spoons.get((programmerId + 1) % spoons.size());

        if (programmerId + 1 == programmers) {
            right.lock();
            left.lock();
        } else {
            left.lock();
            right.lock();
        }

        return new EatingSession(left, right);
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