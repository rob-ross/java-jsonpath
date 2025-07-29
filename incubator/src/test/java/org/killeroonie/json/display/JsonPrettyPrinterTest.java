package org.killeroonie.json.display;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.killeroonie.json.JsonTypes;
import org.killeroonie.json.JsonTypes.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.killeroonie.json.display.Helper.toJsonPrimitive;

class JsonPrettyPrinterTest {

    @Test
    void testIsEmptyOrSingleItem() {
        // You would need to use reflection to test this private method
        // or consider making it package-private for testing
    }

    // Add more tests for other methods and edge cases
}