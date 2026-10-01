package org.labs;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.locks.ReentrantLock;

/** Программист: ест двумя ложками, заказывает добавку, обсуждает преподавателей. */
final class Programmer implements Runnable {

    private final int id;
    private final int leftNeighbour;
    private final int rightNeighbour;
    private final ReentrantLock firstSpoon;   // ложка с меньшим номером
    private final ReentrantLock secondSpoon;  // ложка с большим номером
    private final AtomicLongArray eaten;
    private final Kitchen kitchen;
    private final BlockingQueue<Order> orders;
    private final Config cfg;

    Programmer(int id, ReentrantLock[] spoons, AtomicLongArray eaten,
               Kitchen kitchen, BlockingQueue<Order> orders, Config cfg) {
        int n = spoons.length;
        this.id = id;
        this.leftNeighbour = (id + n - 1) % n;   // у нулевого левый сосед — последний
        this.rightNeighbour = (id + 1) % n;

        // У программиста i ложки i и (i+1) % n.
        // Первой ВСЕГДА берём ложку с меньшим номером: это исключает deadlock.
        int a = id;
        int b = (id + 1) % n;
        this.firstSpoon = spoons[Math.min(a, b)];
        this.secondSpoon = spoons[Math.max(a, b)];

        this.eaten = eaten;
        this.kitchen = kitchen;
        this.orders = orders;
        this.cfg = cfg;
    }

    @Override
    public void run() {
        try {
            boolean plateFull = kitchen.takePortion();    // первая тарелка уже стоит на столе
            while (plateFull) {
                waitForHungryNeighbour();
                eat();

                // Просим добавку сразу после еды...
                Order order = new Order(id, new CompletableFuture<>());
                orders.put(order);
                // ...и разговариваем, пока официант идёт: два ожидания идут параллельно
                cfg.think().sleep();

                plateFull = order.plate().join();         // ждём ответ официанта
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();           // сохраняем флаг прерывания
        }
    }

    /**
     * Справедливость: если я обогнал более голодного соседа больше чем на maxLead порций,
     * жду, пока он догонит. Когда еда кончилась, ждать бессмысленно.
     */
    private void waitForHungryNeighbour() throws InterruptedException {
        while (!kitchen.isEmpty() && aheadOfNeighbour() > cfg.maxLead()) {
            cfg.backoff().sleep();
        }
    }

    private long aheadOfNeighbour() {
        long mine = eaten.get(id);
        long hungriest = Math.min(eaten.get(leftNeighbour), eaten.get(rightNeighbour));
        return mine - hungriest;
    }

    /** Взять обе ложки, поесть, положить ложки. finally возвращает их даже при ошибке. */
    private void eat() throws InterruptedException {
        firstSpoon.lock();
        try {
            secondSpoon.lock();
            try {
                cfg.eat().sleep();
                eaten.incrementAndGet(id);
            } finally {
                secondSpoon.unlock();
            }
        } finally {
            firstSpoon.unlock();
        }
    }
}
