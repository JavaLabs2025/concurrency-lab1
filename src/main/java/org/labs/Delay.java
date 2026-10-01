package org.labs;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

/** Диапазон задержки в микросекундах. Имитирует неидеальные реальные тайминги. */
record Delay(long minMicros, long maxMicros) {

    Delay {
        if (minMicros < 0 || maxMicros < minMicros) {
            throw new IllegalArgumentException("Неверный диапазон задержки: " + minMicros + ".." + maxMicros);
        }
    }

    /** Заснуть на случайное время из диапазона. Виртуальный поток при этом освобождает поток ОС. */
    void sleep() throws InterruptedException {
        // ThreadLocalRandom: у каждого потока свой генератор, общей точки конкуренции нет
        long micros = ThreadLocalRandom.current().nextLong(minMicros, maxMicros + 1);
        if (micros > 0) {
            Thread.sleep(Duration.of(micros, ChronoUnit.MICROS));
        }
    }
}
