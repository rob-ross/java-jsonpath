package org.killeroonie.json.display;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.killeroonie.json.JsonTypes;
import org.killeroonie.json.JsonTypes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonPrettyPrinterTest {

    @Test
    void testFormatPrimitiveString() {
        JsonPrettyPrinter printer = new JsonPrettyPrinter();
        JsonString jsonString = new JsonString("test");
        FormatFlags format = new FormatFlags(true, false, false,false,2, false, false);

        String result = printer.prettyPrintJson(jsonString, format, new ArrayList<>(), 0);
        System.out.println(result);

        assertEquals("\"test\"", result);
    }




    @ParameterizedTest
    @MethodSource("provideJsonStructures")
    void testPrettyPrintJsonStructured(JsonStructured<?> jsonValue, FormatFlags format, String expected) {
        JsonPrettyPrinter printer = new JsonPrettyPrinter();

        String result = printer.prettyPrintJson(jsonValue, format, new ArrayList<>(), 0);

        assertEquals(expected, result);
    }

    static Stream<Arguments> provideJsonStructures() {
        // Add test cases with different JSON structures and expected formatting
        return Stream.of(
                // Empty array case
                Arguments.of(
                        new JsonArray(List.of()),
                        new FormatFlags(true, false, false, false,2, false, false),
                        "[ ]"
                ),
                // Simple object case
                Arguments.of(
                        new JsonObject(Map.of(new JsonString("key"), new JsonString("value"))),
                        new FormatFlags(true, false, false, false,2, false, false),
                        "{ \"key\": \"value\" }"
                )
                // Add more test cases as needed
        );
    }

    @Test
    void testIsEmptyOrSingleItem() {
        // You would need to use reflection to test this private method
        // or consider making it package-private for testing
    }

    // Add more tests for other methods and edge cases
}