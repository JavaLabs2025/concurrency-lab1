package org.labs;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;

class TableTest {

    @Test
    void letsProgrammerEatWhenBothNeighboursAreTalking() {
        Table table = new Table(3);

        assertTimeoutPreemptively(
                Duration.ofSeconds(1),
                () -> table.takeSpoon(0)
        );

        table.putSpoon(0);
    }

    @Test
    void blocksNeighbourUntilProgrammerReleasesSpoons() throws Exception {
        Table table = new Table(3);
        CountDownLatch neighbourStarted = new CountDownLatch(1);

        table.takeSpoon(0);

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<?> neighbour = executor.submit(() -> {
                neighbourStarted.countDown();
                table.takeSpoon(1);
                return null;
            });

            assertTrue(neighbourStarted.await(1, TimeUnit.SECONDS));
            assertThrows(
                    TimeoutException.class,
                    () -> neighbour.get(100, TimeUnit.MILLISECONDS)
            );

            table.putSpoon(0);

            neighbour.get(1, TimeUnit.SECONDS);
            table.putSpoon(1);
        }
    }

    @Test
    void letsNonNeighboursEatAtTheSameTime() throws Exception {
        Table table = new Table(4);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<?> first = executor.submit(() -> {
                ready.countDown();
                start.await();
                table.takeSpoon(0);
                return null;
            });
            Future<?> second = executor.submit(() -> {
                ready.countDown();
                start.await();
                table.takeSpoon(2);
                return null;
            });

            assertTrue(ready.await(1, TimeUnit.SECONDS));
            start.countDown();

            first.get(1, TimeUnit.SECONDS);
            second.get(1, TimeUnit.SECONDS);

            table.putSpoon(0);
            table.putSpoon(2);
        }
    }
}
