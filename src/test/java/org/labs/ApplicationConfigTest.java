package org.labs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

class ApplicationConfigTest {

    @Test
    void distributesFoodAsEvenlyAsPossible() {
        ApplicationConfig config = new ApplicationConfig(7, 1, 10);

        assertArrayEquals(
                new long[]{2, 2, 2, 1, 1, 1, 1},
                config.portionsPerProgrammer()
        );
    }
}
