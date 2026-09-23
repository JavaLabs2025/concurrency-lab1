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
            "8, 3, 800"
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
    }
}
