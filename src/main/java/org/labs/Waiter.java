package org.labs;

import java.util.concurrent.BlockingQueue;

/** Официант: берёт заказ со стойки, забирает порцию на кухне и несёт её. */
final class Waiter implements Runnable {

    private final BlockingQueue<Order> orders;
    private final Kitchen kitchen;
    private final Delay serve;

    Waiter(BlockingQueue<Order> orders, Kitchen kitchen, Delay serve) {
        this.orders = orders;
        this.kitchen = kitchen;
        this.serve = serve;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Order order = orders.take();            // ждём заказ, процессор не тратим
                boolean hasFood = kitchen.takePortion();
                if (hasFood) {
                    serve.sleep();                      // несём тарелку
                }
                order.plate().complete(hasFood);        // будим программиста
            }
        } catch (InterruptedException e) {
            // Main прервал нас через shutdownNow(): все программисты ушли, смена окончена
        }
    }
}
