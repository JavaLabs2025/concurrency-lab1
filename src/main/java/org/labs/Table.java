package org.labs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class Table {

    private final int programmersCnt;
    private final List<Semaphore> semaphoreList;
    private final List<State> stateList;
    private final ReentrantLock lock = new ReentrantLock();

    public Table(int programmerCnt) {
        this.programmersCnt = programmerCnt;

        this.semaphoreList = new ArrayList<>();
        this.stateList = new ArrayList<>();
        for (int i = 0; i < programmerCnt; ++i) {
            this.semaphoreList.add(new Semaphore(0));
            this.stateList.add(State.TALKING);
        }
    }

    public void takeSpoon(int programmerId) throws InterruptedException {
        try {
            lock.lock();
            stateList.set(programmerId, State.HUNGRY);
            test(programmerId);
        } finally {
            lock.unlock();
        }

        semaphoreList.get(programmerId).acquire();
    }

    public void putSpoon(int programmerId) {
        try {
            lock.lock();
            stateList.set(programmerId, State.TALKING);
            test(left(programmerId));
            test(right(programmerId));
        } finally {
            lock.unlock();
        }
    }

    private void test(int programmerId) {
        if (stateList.get(programmerId) == State.HUNGRY &&
                stateList.get(left(programmerId)) != State.EATING &&
                stateList.get(right(programmerId)) != State.EATING
        ) {
            stateList.set(programmerId, State.EATING);
            semaphoreList.get(programmerId).release();
        }
    }

    private int left(int programmerId) {
        return (programmerId - 1 + programmersCnt) % programmersCnt;
    }

    private int right(int programmerId) {
        return (programmerId + 1) % programmersCnt;
    }
}
