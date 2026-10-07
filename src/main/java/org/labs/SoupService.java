package org.labs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

public class SoupService implements AutoCloseable {
    private final ExecutorService waiters;
    private long foodCnt;
    private final long[] remaining;

    private final ReentrantLock lock = new ReentrantLock();

    public SoupService(int waitersCnt, long foodCnt, long[] remaining) {
        this.waiters = Executors.newFixedThreadPool(waitersCnt);
        this.foodCnt = foodCnt;
        this.remaining = remaining;
    }

    public Future<Boolean> requestSoup(int programmerId) {
        return waiters.submit(() -> serveSoup(programmerId));
    }

    public Boolean serveSoup(int programmerId) {
        try {
            lock.lock();
            if (foodCnt <= 0 || remaining[programmerId] <= 0) {
                return false;
            }

            foodCnt--;
            remaining[programmerId]--;
        } finally {
            lock.unlock();
        }
        return true;
    }

    @Override
    public void close() {
        waiters.close();
    }
}
