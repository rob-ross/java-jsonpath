package org.killeroonie.json.display;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.killeroonie.json.JsonTypes.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PPListTest {



    /**
     * Helper method that converts the elements in the argument array into a list of JsonValue wrapper objects.
     */
    static JsonArray convertToJsonArray(Object[] array) {
        List<JsonValue> result = new ArrayList<>();
        for (Object o : array) {
            switch (o) {
                case JsonValue jv -> result.add(jv);
                case String s -> result.add(new JsonString(s));
                case Byte b -> result.add(new JsonNumber(b));
                case Short s -> result.add(new JsonNumber(s));
                case Integer i -> result.add(new JsonNumber(i));
                case Long l -> result.add(new JsonNumber(l));
                case Float f -> result.add(new JsonNumber(f));
                case Double d -> result.add(new JsonNumber(d));
                case Boolean b -> result.add(JsonBoolean.forBoolean(b));
                case null -> result.add(JsonNull.getInstance());
                case Object[] arr -> result.add(convertToJsonArray(arr));

                // todo - deal with Maps
                default -> throw new IllegalArgumentException("Unsupported value " + o);
            }
        }
        return new JsonArray(result);
    }

    /**
     * Encapsulates data for the parameterized tests in this file.
     * @param objArray simple Java Object[] which makes inline initialization simple.
     * @param flags the FormatFlag settings used in a particular test.
     * @param expected the String we expect to be returned from the JSON pretty printer.
     * @param message the message to be displayed for a failed test.
     * @param jsonArray the actual argument passed to the prettyPrintJson method.
     * This is created by the TestCase constructor by converting all Java primitives or Lists and Maps to their JsonValues.
     */
    record TestCase(Object[] objArray, FormatFlags flags, String expected, String message, JsonArray jsonArray) {
        /**
         * Automatically converts objArray to a {@link JsonArray}. This makes it easier to write inline test case data
         * in the source file with less boilerplate.
         */
        TestCase(Object[] objArray, FormatFlags flags, String expected, String message) {
            this(objArray, flags, expected, message, convertToJsonArray(objArray));
        }
    }

    @Test
    void test_emptyList() {
        JsonPrettyPrinter pp = new JsonPrettyPrinter();
        // Empty list
        //noinspection MismatchedQueryAndUpdateOfCollection
        List<JsonValue> list = new ArrayList<>();
        String expected = "[ ]";
        String actual = pp.prettyPrintJson(new JsonArray(list));
        assertEquals(expected, actual, "Empty list returns '[ ]'");
    }

    @Test
    void test_singlePrimitiveList() {
        JsonPrettyPrinter pp = new JsonPrettyPrinter();
        List<JsonValue> list = new ArrayList<>(List.of(new JsonNumber(1)));
        String expected = "[ 1 ]";
        String actual = pp.prettyPrintJson(new JsonArray(list));
        assertEquals(expected, actual, "List returns '[ 1 ]'");
    }

    @ParameterizedTest
    @MethodSource("primitiveMultiElementListTestCases")
    void testPrimitiveMultiElementList(TestCase testCase) {
        JsonPrettyPrinter pp = new JsonPrettyPrinter();
        String actual = pp.prettyPrintJson(testCase.jsonArray, testCase.flags);
        assertEquals(testCase.expected, actual, testCase.message);
        System.out.println("actual: " + actual);
    }

    static Stream<Arguments> primitiveMultiElementListTestCases() {
        // The same fixture is used for all these tests; we only vary the FormatFlags.
        Object[] fixture = new Object[]{"apple", "one", 2, 3.3, 4.444444444e60, true, false, null};
        Object[][] testData = {
                {new TestCase(fixture, FormatFlags.defaults(), "[ apple, one, 2, 3.3, 4.444444444E60, true, false, null ]", "List elements should match defaults()")},
                {new TestCase(fixture, FormatFlags.asJsonFormat(), "[ \"apple\", \"one\", 2, 3.3, 4.444444444E60, true, false, null ]", "List elements should match asJsonFormat()")},
                {new TestCase(fixture, FormatFlags.defaults().withQuoteStrings(true), "[ \"apple\", \"one\", 2, 3.3, 4.444444444E60, true, false, null ]", "List elements should match withQuoteStrings(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withQuoteStrings(true).withSingleQuotes(true), "[ 'apple', 'one', 2, 3.3, 4.444444444E60, true, false, null ]", "List elements should match withSingleQuotes(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withUseRepr(true), "[ apple, one, 2, 3.3, 4.444444444E60, true, false, null ]", "List elements should match withUseRep(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withFormatJson(true), "[ apple, one, 2, 3.3, 4.444444444E60, true, false, null ]", "List elements should match withFormatJson(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withIndent(4), "[ apple, one, 2, 3.3, 4.444444444E60, true, false, null ]", "List elements should match withIndent(4)")},
                {new TestCase(fixture, FormatFlags.defaults().withMultipleLines(true), "[\n  apple,\n  one,\n  2,\n  3.3,\n  4.444444444E60,\n  true,\n  false,\n  null\n]", "List elements should match withMultipleLines(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withOmitCommas(true), "[ apple one 2 3.3 4.444444444E60 true false null ]", "List elements should match withOmitCommas(true)")},
        };
        return Arrays.stream(testData).map(Arguments::of);
    }


}
