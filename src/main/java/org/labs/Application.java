package org.labs;

import java.util.Arrays;

public class Application {
    private final SoupService soupService;
    private final Thread[] programmers;
    private final long[] remaining;

    public Application(ApplicationConfig config) {
        Table table = new Table(config.programmersCount());
        remaining = config.portionsPerProgrammer();
        soupService = new SoupService(config.waitersCount(), config.foodCount(), remaining);
        programmers = new Thread[config.programmersCount()];
        for (int i = 0; i < config.programmersCount(); ++i) {
            Programmer programmer = new Programmer(i, table, soupService);
            programmers[i] = new Thread(programmer);
        }
    }

    public void start() {
        for (Thread programmer : programmers) {
            programmer.start();
        }
    }

    public void stop() throws InterruptedException {
        try {
            for (Thread programmer : programmers) {
                programmer.join();
            }
        } finally {
            soupService.close();
        }
    }

    public long[] remainingPortions() {
        return Arrays.copyOf(remaining, remaining.length);
    }
}
