package com.vehiclerental;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MainTest {

    @Test
    void mainShouldRunWithoutExceptions() {
        assertDoesNotThrow(() -> Main.main(new String[]{}));
    }
}