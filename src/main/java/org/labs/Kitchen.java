package org.labs;

import java.util.concurrent.atomic.AtomicLong;

/** Кухня с ограниченным запасом порций. */
final class Kitchen {

    private final AtomicLong portions;

    Kitchen(long food) {
        this.portions = new AtomicLong(food);
    }

    /**
     * Взять одну порцию. Возвращает false, если еда кончилась.
     * Уменьшение атомарно: одну порцию не получат двое, и остаток не уйдёт в минус.
     */
    boolean takePortion() {
        // getAndUpdate возвращает значение ДО изменения: если оно было > 0, порция наша
        return portions.getAndUpdate(p -> p > 0 ? p - 1 : 0) > 0;
    }

    boolean isEmpty() {
        return portions.get() == 0;
    }
}
