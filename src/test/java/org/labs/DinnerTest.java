package org.labs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

// Если обед завис (например, deadlock), тест упадёт по таймауту, а не будет висеть вечно
@Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class DinnerTest {

    /** Очень короткие задержки: тесты быстрые, но потоки реально конкурируют. */
    private static Config fast(int programmers, int waiters, long food, int maxLead) {
        return new Config(programmers, waiters, food, maxLead,
                new Delay(0, 2), new Delay(0, 2), new Delay(0, 2), new Delay(1, 2));
    }

    @ParameterizedTest
    @CsvSource({"2, 1, 10000", "5, 2, 50000", "7, 3, 100000", "100, 10, 100000"})
    void allFoodIsEatenExactlyOnceWithoutDeadlock(int programmers, int waiters, long food) {
        long[] eaten = Main.run(fast(programmers, waiters, food, 2));
        assertEquals(food, Arrays.stream(eaten).sum());
    }

    @Test
    void spreadIsWithinProvableBound() {
        int n = 7;
        int maxLead = 2;
        long[] eaten = Main.run(fast(n, 2, 100_000, maxLead));
        long spread = Arrays.stream(eaten).max().orElseThrow() - Arrays.stream(eaten).min().orElseThrow();
        long bound = (long) (n / 2) * (maxLead + 2);
        assertTrue(spread <= bound, "разброс " + spread + " больше границы " + bound);
    }

    @Test
    void lessFoodThanProgrammers() {
        assertEquals(3, Arrays.stream(Main.run(fast(10, 2, 3, 2))).sum());
    }

    @Test
    void noFoodAtAll() {
        assertEquals(0, Arrays.stream(Main.run(fast(5, 2, 0, 2))).sum());
    }

    @Test
    void rejectsInvalidConfig() {
        assertThrows(IllegalArgumentException.class, () -> fast(1, 1, 10, 2));
        assertThrows(IllegalArgumentException.class, () -> fast(5, 0, 10, 2));
    }
}
