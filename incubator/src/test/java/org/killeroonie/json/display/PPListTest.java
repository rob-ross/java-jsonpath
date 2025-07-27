package org.killeroonie.json.display;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.killeroonie.json.JsonTypes.*;
import org.killeroonie.json.display.Helper.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PPListTest {


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
        String actual = pp.prettyPrintJson(testCase.structured(), testCase.flags());
        assertEquals(testCase.expected(), actual, testCase.message());
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

    @ParameterizedTest
    @MethodSource("nestedSingleElementPrimitiveLists")
    void testNestedSingleElementPrimitiveLists(TestCase testCase) {
        JsonPrettyPrinter pp = new JsonPrettyPrinter();
        String actual = pp.prettyPrintJson(testCase.structured(), testCase.flags());
        assertEquals(testCase.expected(), actual, testCase.message());
    }

    static Stream<Arguments> nestedSingleElementPrimitiveLists() {
        Object[] array1 = { 1 };
        Object[] array2 = { "two" };
        Object[] array3 = { true };
        Object[] array4 = {};  // empty array
        Object[] fixture = { array1, array2, array3, array4 };
        Object[][] testData = {
                {new TestCase(fixture, FormatFlags.defaults(), "[ [ 1 ], [ two ], [ true ], [ ] ]", "List elements should match defaults()")},
                {new TestCase(fixture, FormatFlags.asJsonFormat(), "[ [ 1 ], [ \"two\" ], [ true ], [ ] ]", "List elements should match asJsonFormat()")},
                {new TestCase(fixture, FormatFlags.defaults().withQuoteStrings(true), "[ [ 1 ], [ \"two\" ], [ true ], [ ] ]", "List elements should match withQuoteStrings(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withUseRepr(true), "[ [ 1 ], [ two ], [ true ], [ ] ]", "List elements should match withUseRepr(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withFormatJson(true), "[ [ 1 ], [ two ], [ true ], [ ] ]", "List elements should match withFormatJson(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withIndent(4), "[ [ 1 ], [ two ], [ true ], [ ] ]", "List elements should match withIndent(4)")},
                {new TestCase(fixture, FormatFlags.defaults().withMultipleLines(true), "[\n  [ 1 ],\n  [ two ],\n  [ true ],\n  [ ]\n]", "List elements should match withMultipleLines(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withMultipleLines(true).withIndent(4), "[\n    [ 1 ],\n    [ two ],\n    [ true ],\n    [ ]\n]", "List elements should match withMultipleLines(true).withIndent(4)")},

                {new TestCase(fixture, FormatFlags.defaults().withOmitCommas(true), "[ [ 1 ] [ two ] [ true ] [ ] ]", "List elements should match withOmitCommas(true)")},




        };
        return Arrays.stream(testData).map(Arguments::of);
    }


    @ParameterizedTest
    @MethodSource("nestedPrimitiveElementListsTestCases")
    void testNestedPrimitiveElementLists(TestCase testCase) {
        JsonPrettyPrinter pp = new JsonPrettyPrinter();
        String actual = pp.prettyPrintJson(testCase.structured(), testCase.flags());
        assertEquals(testCase.expected(), actual, testCase.message());
        System.out.println("actual: " + actual);
    }

    static Stream<Arguments> nestedPrimitiveElementListsTestCases() {
        Object[] array1 = { 1, 2, 3 };
        Object[] array2 = { "four", "five", "six" };
        Object[] array3 = { true, false, null };
        Object[] fixture = { array1, array2, array3 };
        Object[][] testData = {
                {new TestCase(fixture, FormatFlags.defaults(), "[ [ 1, 2, 3 ], [ four, five, six ], [ true, false, null ] ]", "List elements should match defaults()")},
                {new TestCase(fixture, FormatFlags.asJsonFormat(), "[ [ 1, 2, 3 ], [ \"four\", \"five\", \"six\" ], [ true, false, null ] ]", "List elements should match asJsonFormat()")},
                {new TestCase(fixture, FormatFlags.defaults().withQuoteStrings(true), "[ [ 1, 2, 3 ], [ \"four\", \"five\", \"six\" ], [ true, false, null ] ]", "List elements should match withQuoteStrings(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withQuoteStrings(true).withSingleQuotes(true), "[ [ 1, 2, 3 ], [ 'four', 'five', 'six' ], [ true, false, null ] ]", "List elements should match withSingleQuotes(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withMultipleLines(true), "[\n  [\n    1,\n    2,\n    3\n  ],\n  [\n    four,\n    five,\n    six\n  ],\n  [\n    true,\n    false,\n    null\n  ]\n]", "List elements should match withMultipleLines(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withOmitCommas(true), "[ [ 1 2 3 ] [ four five six ] [ true false null ] ]", "List elements should match withOmitCommas(true)")},
        };
        return Arrays.stream(testData).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("nestedPrimitiveElementMultiLineListsTestCases")
    void testNestedPrimitiveElementMultiLineLists(TestCase testCase) {
        JsonPrettyPrinter pp = new JsonPrettyPrinter();
        String actual = pp.prettyPrintJson(testCase.structured(), testCase.flags());
        assertEquals(testCase.expected(), actual, testCase.message());
        //System.out.println("actual: " + actual);
    }

    static Stream<Arguments> nestedPrimitiveElementMultiLineListsTestCases() {
        Object[] array1 = {1, 2, 3};
        Object[] array2 = {"four", "five", "six"};
        Object[] array3 = {true, false, null};
        Object[] fixture = {array1, array2, array3};
        Object[][] testData = {
                {new TestCase(fixture, FormatFlags.defaults().withMultipleLines(true), "[\n  [\n    1,\n    2,\n    3\n  ],\n  [\n    four,\n    five,\n    six\n  ],\n  [\n    true,\n    false,\n    null\n  ]\n]", "List elements should match withMultipleLines(true)")},
                {new TestCase(fixture, FormatFlags.asJsonFormat().withMultipleLines(true), "[\n  [\n    1,\n    2,\n    3\n  ],\n  [\n    \"four\",\n    \"five\",\n    \"six\"\n  ],\n  [\n    true,\n    false,\n    null\n  ]\n]", "List elements should match asJsonFormat()")},
                {new TestCase(fixture, FormatFlags.defaults().withMultipleLines(true).withQuoteStrings(true), "[\n  [\n    1,\n    2,\n    3\n  ],\n  [\n    \"four\",\n    \"five\",\n    \"six\"\n  ],\n  [\n    true,\n    false,\n    null\n  ]\n]", "List elements should match withQuoteStrings(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withMultipleLines(true).withQuoteStrings(true).withSingleQuotes(true), "[\n  [\n    1,\n    2,\n    3\n  ],\n  [\n    'four',\n    'five',\n    'six'\n  ],\n  [\n    true,\n    false,\n    null\n  ]\n]", "List elements should match withSingleQuotes(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withMultipleLines(true).withIndent(4), "[\n    [\n        1,\n        2,\n        3\n    ],\n    [\n        four,\n        five,\n        six\n    ],\n    [\n        true,\n        false,\n        null\n    ]\n]", "List elements should match withIndent(4)")},
                {new TestCase(fixture, FormatFlags.defaults().withMultipleLines(true).withOmitCommas(true), "[\n  [\n    1\n    2\n    3\n  ]\n  [\n    four\n    five\n    six\n  ]\n  [\n    true\n    false\n    null\n  ]\n]", "List elements should match withOmitCommas(true)")},
        };
        return Arrays.stream(testData).map(Arguments::of);
    }
}