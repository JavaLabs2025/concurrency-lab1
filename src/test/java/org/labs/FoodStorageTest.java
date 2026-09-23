package org.labs;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoodStorageTest {

    @Test
    void rejectsNegativePortions() {
        assertThrows(IllegalArgumentException.class, () -> new FoodStorage(-1));
    }

    @Test
    void takePortionSucceedsExactlyPortionsTimes() {
        FoodStorage storage = new FoodStorage(3);

        assertTrue(storage.takePortion());
        assertTrue(storage.takePortion());
        assertTrue(storage.takePortion());
        assertFalse(storage.takePortion());
    }

    @Test
    void takePortionIsAtomicUnderConcurrentAccess() {
        int portions = 200;
        FoodStorage storage = new FoodStorage(portions);

        List<Callable<Boolean>> attempts = IntStream.range(0, portions * 2)
                .<Callable<Boolean>>mapToObj(i -> storage::takePortion)
                .toList();

        long successes;
        try (ExecutorService executor = Executors.newFixedThreadPool(20)) {
            List<Future<Boolean>> futures = attempts.stream()
                    .map(executor::submit)
                    .toList();

            successes = futures.stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(Boolean::booleanValue)
                    .count();
        }

        assertEquals(portions, successes);
    }
}
