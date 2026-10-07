package org.labs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ApplicationTest {

    @Test
    void completesDefaultSimulationWithoutDeadlock() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Application application = new Application(new ApplicationConfig(2, 1, 2));

            application.start();
            application.stop();
        });
    }

    @ParameterizedTest
    @MethodSource("simulationConfigs")
    void givesEveryProgrammerTheirConfiguredPortionWhenFoodIsEnough(
            ApplicationConfig config
    ) {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Application application = new Application(config);

            application.start();
            application.stop();

            assertArrayEquals(
                    new long[config.programmersCount()],
                    application.remainingPortions()
            );
        });
    }

    private static Stream<ApplicationConfig> simulationConfigs() {
        return Stream.of(
                new ApplicationConfig(2, 1, 3),
                new ApplicationConfig(3, 2, 6),
                new ApplicationConfig(7, 1, 10)
        );
    }
}
