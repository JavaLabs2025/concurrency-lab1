package org.labs;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.locks.ReentrantLock;

public final class Main {

    public static void main(String[] args) {
        Config cfg;
        try {
            cfg = Config.fromArgs(args);
        } catch (IllegalArgumentException e) {
            // NumberFormatException (не число в аргументах) тоже наследник IllegalArgumentException
            System.err.println("Ошибка в параметрах: " + e.getMessage());
            System.err.println("Использование: ./gradlew run --args=\"[программисты] [официанты] [еда] [maxLead]\"");
            System.exit(2);
            return;
        }

        System.out.printf("Программистов: %d, официантов: %d, еды: %d, max-lead: %d%n",
                cfg.programmers(), cfg.waiters(), cfg.food(), cfg.maxLead());

        long start = System.nanoTime();
        long[] eaten = run(cfg);
        long millis = (System.nanoTime() - start) / 1_000_000;

        printReport(eaten, cfg.food(), millis);
    }

    /** Проводит обед и возвращает, сколько съел каждый программист. */
    static long[] run(Config cfg) {
        int n = cfg.programmers();

        // Ложка i лежит между программистами i-1 и i
        ReentrantLock[] spoons = new ReentrantLock[n];
        for (int i = 0; i < n; i++) {
            spoons[i] = new ReentrantLock();
        }

        AtomicLongArray eaten = new AtomicLongArray(n);            // кто сколько съел
        Kitchen kitchen = new Kitchen(cfg.food());                 // запас еды
        BlockingQueue<Order> orders = new LinkedBlockingQueue<>(); // стойка заказов

        try (ExecutorService waiters = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int w = 0; w < cfg.waiters(); w++) {
                waiters.execute(new Waiter(orders, kitchen, cfg.serve()));
            }

            // close() в конце этого блока ждёт, пока все программисты закончат обед
            try (ExecutorService programmers = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < n; i++) {
                    programmers.execute(new Programmer(i, spoons, eaten, kitchen, orders, cfg));
                }
            }

            // Все ушли из-за стола. Официанты ждут заказ в take(), прерываем их: смена окончена
            waiters.shutdownNow();
        } // здесь ждём, пока официанты завершатся

        long[] result = new long[n];
        for (int i = 0; i < n; i++) {
            result[i] = eaten.get(i);
        }
        return result;
    }

    private static void printReport(long[] eaten, long food, long millis) {
        long total = 0;
        long min = Long.MAX_VALUE;
        long max = 0;
        for (long e : eaten) {
            total += e;
            min = Math.min(min, e);
            max = Math.max(max, e);
        }
        if (eaten.length <= 20) {
            System.out.println("Съедено по программистам: " + Arrays.toString(eaten));
        }
        System.out.printf("Всего съедено: %d из %d%n", total, food);
        System.out.printf("min: %d, max: %d, разброс: %d%n", min, max, max - min);
        System.out.printf("Время: %d мс, скорость: %.0f порций/с%n",
                millis, total * 1000.0 / Math.max(millis, 1));
    }
}
