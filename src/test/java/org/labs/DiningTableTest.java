package org.labs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DiningTableTest {

    @Test
    void rejectsFewerThanTwoProgrammers() {
        assertThrows(IllegalArgumentException.class, () -> new DiningTable(1));
    }
}
