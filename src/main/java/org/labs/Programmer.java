package org.labs;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

public record Programmer(int id, Table table, SoupService soupService) implements Runnable {

    private static final long MIN_DELAY_MS = 1;
    private static final long MAX_DELAY_MS = 5;

    @Override
    public void run() {
        try {
            while (soupService.requestSoup(id).get()) {
                talk();
                table.takeSpoon(id);
                eat();
                table.putSpoon(id);
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            table.putSpoon(id);
        }
    }

    private void eat() throws InterruptedException {
        Thread.sleep(ThreadLocalRandom.current()
                .nextLong(MIN_DELAY_MS, MAX_DELAY_MS));
    }

    public void talk() throws InterruptedException {
        Thread.sleep(ThreadLocalRandom.current()
                .nextLong(MIN_DELAY_MS, MAX_DELAY_MS));
    }
}
