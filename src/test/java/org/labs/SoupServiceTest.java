package org.labs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class SoupServiceTest {

    @Test
    void servesSoupWhenFoodAndPersonalPortionAreAvailable() {
        try (SoupService soupService = new SoupService(1, 1, new long[]{1})) {
            assertTrue(soupService.serveSoup(0));
        }
    }

    @Test
    void doesNotServeSoupWhenGlobalSupplyIsEmpty() {
        try (SoupService soupService = new SoupService(1, 0, new long[]{1})) {
            assertFalse(soupService.serveSoup(0));
        }
    }

    @Test
    void doesNotServeSoupWhenPersonalPortionIsEmpty() {
        try (SoupService soupService = new SoupService(1, 1, new long[]{0})) {
            assertFalse(soupService.serveSoup(0));
        }
    }

    @Test
    void doesNotServeMoreThanAvailablePersonalAndGlobalPortions() {
        try (SoupService soupService = new SoupService(1, 1, new long[]{1})) {
            assertTrue(soupService.serveSoup(0));
            assertFalse(soupService.serveSoup(0));
        }
    }

    @Test
    void doesNotServeMoreSoupThanGlobalSupplyUnderConcurrentRequests() throws Exception {
        int programmers = 4;
        CountDownLatch ready = new CountDownLatch(programmers);
        CountDownLatch start = new CountDownLatch(1);

        try (
                SoupService soupService = new SoupService(1, 3, new long[]{1, 1, 1, 1});
                ExecutorService executor = Executors.newFixedThreadPool(programmers)
        ) {
            List<Future<Boolean>> requests = new ArrayList<>();

            for (int programmerId = 0; programmerId < programmers; programmerId++) {
                int id = programmerId;
                requests.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    return soupService.serveSoup(id);
                }));
            }

            assertTrue(ready.await(1, TimeUnit.SECONDS));
            start.countDown();

            int served = 0;
            for (Future<Boolean> request : requests) {
                if (request.get(1, TimeUnit.SECONDS)) {
                    served++;
                }
            }

            assertEquals(3, served);
        }
    }
}
