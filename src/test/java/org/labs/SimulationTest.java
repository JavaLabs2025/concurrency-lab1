package org.labs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationTest {

    @Test
    void rejectsFewerThanTwoProgrammers() {
        assertThrows(IllegalArgumentException.class,
                () -> new Simulation(1, 1, 10, 1, 1, 1));
    }

    @Test
    void rejectsFewerThanOneWaiter() {
        assertThrows(IllegalArgumentException.class,
                () -> new Simulation(2, 0, 10, 1, 1, 1));
    }

    @ParameterizedTest(name = "{0} programmers, {1} waiters, {2} portions")
    @CsvSource({
            "2, 1, 50",
            "5, 2, 500",
            "8, 3, 803"
    })
    void servesExactlyAllFoodWithoutStarvationOrDeadlock(
            int programmerCount,
            int waiterCount,
            int foodAmount
    ) {
        Simulation simulation = new Simulation(programmerCount, waiterCount, foodAmount, 1, 1, 1);

        Simulation.Result result = assertTimeoutPreemptively(
                Duration.ofSeconds(15),
                simulation::run
        );

        assertEquals(foodAmount, result.totalEaten());
        assertTrue(result.minEaten() > 0, "every programmer should get to eat at least once");
        // Programmers eat in lockstep rounds gated by a CyclicBarrier: spoon
        // acquisition always blocks until it succeeds (deadlock-free by lock
        // ordering), so every programmer's eaten count increases by exactly 1 each
        // completed round. The spread is provably 0 between rounds, and at most 1
        // overall (from the final round, where food can run out mid-round after
        // some but not all programmers were served) — not just an emergent trend.
        assertTrue(
                result.maxEaten() - result.minEaten() <= 1,
                "eaten-count spread should never exceed 1, was " + (result.maxEaten() - result.minEaten())
        );
    }
}
