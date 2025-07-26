package org.killeroonie.json.display;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.killeroonie.json.JsonTypes.*;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrimitivesTest {

    @ParameterizedTest
    @MethodSource("defaultsTestCases")
    void test_defaultsFormat(JsonPrimitive<?> primitive, String expected, String message) {
        FormatFlags flags = FormatFlags.defaults();
        JsonPrettyPrinter printer = new JsonPrettyPrinter();

        String actual = printer.prettyPrintJson(primitive, flags);

        assertEquals(expected, actual, message);
    }

    static Stream<Arguments> defaultsTestCases() {
        Object[][] testData = {
                {new JsonString("foo"), "foo", "String primitive should not be quoted with default format"},
                {JsonBoolean.TRUE, "true", "Boolean true should be formatted as true"},
                {JsonBoolean.FALSE, "false", "Boolean false should be formatted as false"},
                {JsonNull.getInstance(), "null", "Null should be formatted as null"},
                {new JsonNumber((byte) 42), "42", "Byte should be formatted as its string representation"},
                {new JsonNumber((short) 1234), "1234", "Short should be formatted as its string representation"},
                {new JsonNumber(42), "42", "Integer should be formatted as its string representation"},
                {new JsonNumber(9876543210L), "9876543210", "Long should be formatted as its string representation"},
                {new JsonNumber(3.14f), "3.14", "Float should be formatted as its string representation"},
                {new JsonNumber(2.71828), "2.71828", "Double should be formatted as its string representation"}
        };
        return Arrays.stream(testData).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("asJsonTestCases")
    void test_asJsonFormat(JsonPrimitive<?> primitive, String expected, String message) {
        FormatFlags flags = FormatFlags.asJsonFormat();
        JsonPrettyPrinter printer = new JsonPrettyPrinter();

        String actual = printer.prettyPrintJson(primitive, flags);

        assertEquals(expected, actual, message);
    }

    static Stream<Arguments> asJsonTestCases() {
        Object[][] testData = {
                {new JsonString("foo"), "\"foo\"", "String primitive should be quoted with asJson format"},
                {JsonBoolean.TRUE, "true", "Boolean true should be formatted as true"},
                {JsonBoolean.FALSE, "false", "Boolean false should be formatted as false"},
                {JsonNull.getInstance(), "null", "Null should be formatted as null"},
                {new JsonNumber((byte) 42), "42", "Byte should be formatted as its string representation"},
                {new JsonNumber((short) 1234), "1234", "Short should be formatted as its string representation"},
                {new JsonNumber(42), "42", "Integer should be formatted as its string representation"},
                {new JsonNumber(9876543210L), "9876543210", "Long should be formatted as its string representation"},
                {new JsonNumber(3.14f), "3.14", "Float should be formatted as its string representation"},
                {new JsonNumber(2.71828), "2.71828", "Double should be formatted as its string representation"}
        };
        return Arrays.stream(testData).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("withQuoteStringsTestCases")
    void test_withQuoteStrings(JsonPrimitive<?> primitive, String expected, String message) {
        FormatFlags flags = FormatFlags.defaults().withQuoteStrings(true);
        JsonPrettyPrinter printer = new JsonPrettyPrinter();

        String actual = printer.prettyPrintJson(primitive, flags);

        assertEquals(expected, actual, message);
    }

    static Stream<Arguments> withQuoteStringsTestCases() {
        Object[][] testData = {
                {new JsonString("foo"), "\"foo\"", "String primitive should be quoted by withQuoteStrings format"},
                {JsonBoolean.TRUE, "true", "Boolean true should be formatted as true"},
                {JsonBoolean.FALSE, "false", "Boolean false should be formatted as false"},
                {JsonNull.getInstance(), "null", "Null should be formatted as null"},
                {new JsonNumber((byte) 42), "42", "Byte should be formatted as its string representation"},
                {new JsonNumber((short) 1234), "1234", "Short should be formatted as its string representation"},
                {new JsonNumber(42), "42", "Integer should be formatted as its string representation"},
                {new JsonNumber(9876543210L), "9876543210", "Long should be formatted as its string representation"},
                {new JsonNumber(3.14f), "3.14", "Float should be formatted as its string representation"},
                {new JsonNumber(2.71828), "2.71828", "Double should be formatted as its string representation"}
        };
        return Arrays.stream(testData).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("withSingleQuotesTestCases")
    void test_withSingleQuotes(JsonPrimitive<?> primitive, String expected, String message) {
        FormatFlags flags = FormatFlags.defaults().withSingleQuotes(true).withQuoteStrings(true);
        JsonPrettyPrinter printer = new JsonPrettyPrinter();

        String actual = printer.prettyPrintJson(primitive, flags);

        assertEquals(expected, actual, message);
    }

    static Stream<Arguments> withSingleQuotesTestCases() {
        Object[][] testData = {
                {new JsonString("foo"), "'foo'", "String primitive should be quoted by withSingleQuotes format"},
                {JsonBoolean.TRUE, "true", "Boolean true should be formatted as true"},
                {JsonBoolean.FALSE, "false", "Boolean false should be formatted as false"},
                {JsonNull.getInstance(), "null", "Null should be formatted as null"},
                {new JsonNumber((byte) 42), "42", "Byte should be formatted as its string representation"},
                {new JsonNumber((short) 1234), "1234", "Short should be formatted as its string representation"},
                {new JsonNumber(42), "42", "Integer should be formatted as its string representation"},
                {new JsonNumber(9876543210L), "9876543210", "Long should be formatted as its string representation"},
                {new JsonNumber(3.14f), "3.14", "Float should be formatted as its string representation"},
                {new JsonNumber(2.71828), "2.71828", "Double should be formatted as its string representation"}
        };
        return Arrays.stream(testData).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("withUseReprTestCases")
    void test_withUseRepr(JsonPrimitive<?> primitive, String expected, String message) {
        FormatFlags flags = FormatFlags.defaults().withUseRepr(true);
        JsonPrettyPrinter printer = new JsonPrettyPrinter();

        String actual = printer.prettyPrintJson(primitive, flags);

        assertEquals(expected, actual, message);
    }

    static Stream<Arguments> withUseReprTestCases() {
        Object[][] testData = {
                {new JsonString("foo"), "foo", "String primitive should not be quoted by withUseRepr format"},
                {JsonBoolean.TRUE, "true", "Boolean true should be formatted as true"},
                {JsonBoolean.FALSE, "false", "Boolean false should be formatted as false"},
                {JsonNull.getInstance(), "null", "Null should be formatted as null"},
                {new JsonNumber((byte) 42), "42", "Byte should be formatted as its string representation"},
                {new JsonNumber((short) 1234), "1234", "Short should be formatted as its string representation"},
                {new JsonNumber(42), "42", "Integer should be formatted as its string representation"},
                {new JsonNumber(9876543210L), "9876543210", "Long should be formatted as its string representation"},
                {new JsonNumber(3.14f), "3.14", "Float should be formatted as its string representation"},
                {new JsonNumber(2.71828), "2.71828", "Double should be formatted as its string representation"}
        };
        return Arrays.stream(testData).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("withFormatJsonTestCases")
    void test_withFormatJson(JsonPrimitive<?> primitive, String expected, String message) {
        FormatFlags flags = FormatFlags.defaults().withFormatJson(true);
        JsonPrettyPrinter printer = new JsonPrettyPrinter();

        String actual = printer.prettyPrintJson(primitive, flags);

        assertEquals(expected, actual, message);
    }

    static Stream<Arguments> withFormatJsonTestCases() {
        Object[][] testData = {
                {new JsonString("foo"), "foo", "String primitive should not be quoted by withFormatJson format"},
                {JsonBoolean.TRUE, "true", "Boolean true should be formatted as true"},
                {JsonBoolean.FALSE, "false", "Boolean false should be formatted as false"},
                {JsonNull.getInstance(), "null", "Null should be formatted as null"},
                {new JsonNumber((byte) 42), "42", "Byte should be formatted as its string representation"},
                {new JsonNumber((short) 1234), "1234", "Short should be formatted as its string representation"},
                {new JsonNumber(42), "42", "Integer should be formatted as its string representation"},
                {new JsonNumber(9876543210L), "9876543210", "Long should be formatted as its string representation"},
                {new JsonNumber(3.14f), "3.14", "Float should be formatted as its string representation"},
                {new JsonNumber(2.71828), "2.71828", "Double should be formatted as its string representation"}
        };
        return Arrays.stream(testData).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("withIndentTestCases")
    void test_withIndent(JsonPrimitive<?> primitive, String expected, String message) {
        FormatFlags flags = FormatFlags.defaults().withIndent(4);
        JsonPrettyPrinter printer = new JsonPrettyPrinter();

        String actual = printer.prettyPrintJson(primitive, flags);

        assertEquals(expected, actual, message);
    }

    static Stream<Arguments> withIndentTestCases() {
        Object[][] testData = {
                {new JsonString("foo"), "foo", "String primitive should not be quoted by withIndent format"},
                {JsonBoolean.TRUE, "true", "Boolean true should be formatted as true"},
                {JsonBoolean.FALSE, "false", "Boolean false should be formatted as false"},
                {JsonNull.getInstance(), "null", "Null should be formatted as null"},
                {new JsonNumber((byte) 42), "42", "Byte should be formatted as its string representation"},
                {new JsonNumber((short) 1234), "1234", "Short should be formatted as its string representation"},
                {new JsonNumber(42), "42", "Integer should be formatted as its string representation"},
                {new JsonNumber(9876543210L), "9876543210", "Long should be formatted as its string representation"},
                {new JsonNumber(3.14f), "3.14", "Float should be formatted as its string representation"},
                {new JsonNumber(2.71828), "2.71828", "Double should be formatted as its string representation"}
        };
        return Arrays.stream(testData).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("withMultipleLinesTestCases")
    void test_withSingleLine(JsonPrimitive<?> primitive, String expected, String message) {
        FormatFlags flags = FormatFlags.defaults().withMultipleLines(true);
        JsonPrettyPrinter printer = new JsonPrettyPrinter();

        String actual = printer.prettyPrintJson(primitive, flags);

        assertEquals(expected, actual, message);
    }

    static Stream<Arguments> withMultipleLinesTestCases() {
        Object[][] testData = {
                {new JsonString("foo"), "foo", "String primitive should not be quoted by withMultipleLines format"},
                {JsonBoolean.TRUE, "true", "Boolean true should be formatted as true"},
                {JsonBoolean.FALSE, "false", "Boolean false should be formatted as false"},
                {JsonNull.getInstance(), "null", "Null should be formatted as null"},
                {new JsonNumber((byte) 42), "42", "Byte should be formatted as its string representation"},
                {new JsonNumber((short) 1234), "1234", "Short should be formatted as its string representation"},
                {new JsonNumber(42), "42", "Integer should be formatted as its string representation"},
                {new JsonNumber(9876543210L), "9876543210", "Long should be formatted as its string representation"},
                {new JsonNumber(3.14f), "3.14", "Float should be formatted as its string representation"},
                {new JsonNumber(2.71828), "2.71828", "Double should be formatted as its string representation"}
        };
        return Arrays.stream(testData).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("withOmitCommasTestCases")
    void test_withOmitCommas(JsonPrimitive<?> primitive, String expected, String message) {
        FormatFlags flags = FormatFlags.defaults().withOmitCommas(true);
        JsonPrettyPrinter printer = new JsonPrettyPrinter();

        String actual = printer.prettyPrintJson(primitive, flags);

        assertEquals(expected, actual, message);
        System.out.printf("primitive=%s, expected=%s, actual=%s, message=%s%n", primitive, expected, actual, message);
    }

    static Stream<Arguments> withOmitCommasTestCases() {
        Object[][] testData = {
                {new JsonString("foo"), "foo", "String primitive should not be quoted by withOmitCommas format"},
                {JsonBoolean.TRUE, "true", "Boolean true should be formatted as true"},
                {JsonBoolean.FALSE, "false", "Boolean false should be formatted as false"},
                {JsonNull.getInstance(), "null", "Null should be formatted as null"},
                {new JsonNumber((byte) 42), "42", "Byte should be formatted as its string representation"},
                {new JsonNumber((short) 1234), "1234", "Short should be formatted as its string representation"},
                {new JsonNumber(42), "42", "Integer should be formatted as its string representation"},
                {new JsonNumber(9876543210L), "9876543210", "Long should be formatted as its string representation"},
                {new JsonNumber(3.14f), "3.14", "Float should be formatted as its string representation"},
                {new JsonNumber(2.71828), "2.71828", "Double should be formatted as its string representation"}
        };
        return Arrays.stream(testData).map(Arguments::of);
    }
}
