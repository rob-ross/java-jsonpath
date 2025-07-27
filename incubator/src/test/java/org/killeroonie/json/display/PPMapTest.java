package org.killeroonie.json.display;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.killeroonie.json.JsonTypes.*;
import org.killeroonie.json.display.Helper.TestCase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PPMapTest {

    @Test
    void test_emptyMap() {
        JsonPrettyPrinter pp = new JsonPrettyPrinter();
        // Empty Map
        //noinspection MismatchedQueryAndUpdateOfCollection
        Map<JsonString, JsonValue> map = new HashMap<>();
        String expected = "{ }";
        String actual = pp.prettyPrintJson(new JsonObject(map));
        assertEquals(expected, actual, "Empty map returns '[ ]'");
    }

    @Test
    void test_singlePrimitiveMap() {
        JsonPrettyPrinter pp = new JsonPrettyPrinter();
        Map<JsonString, JsonValue> map = Map.of(new JsonString("one"), new JsonNumber(1));
        String expected = "{ one: 1 }";
        String actual = pp.prettyPrintJson(new JsonObject(map));
        assertEquals(expected, actual, "Map returns '{ one:1 }'");
    }

    @ParameterizedTest
    @MethodSource("primitiveMultiElementMapTestCases")
    void testPrimitiveMultiElementMap(TestCase testCase) {
        JsonPrettyPrinter pp = new JsonPrettyPrinter();
        String actual = pp.prettyPrintJson(testCase.structured(), testCase.flags());
        assertEquals(testCase.expected(), actual, testCase.message());
        System.out.println("actual: " + actual);
    }

    static Stream<Arguments> primitiveMultiElementMapTestCases() {
        // The same fixture is used for all these tests; we only vary the FormatFlags.
        Map<String, Object> fixture = new HashMap<>();
        fixture.put("akey", "apple");
        fixture.put("key1", "one");
        fixture.put("key2", 2);
        fixture.put("key3", 3.3);
        fixture.put("key4", 4.444444444e60);
        fixture.put("key5", true);
        fixture.put("key6", false);
        fixture.put("key7", null);

        Object[][] testData = {
                {new TestCase(fixture, FormatFlags.defaults(), "{ key1: one, key2: 2, akey: apple, key5: true, key6: false, key3: 3.3, key4: 4.444444444E60, key7: null }", "List elements should match defaults()")},
                {new TestCase(fixture, FormatFlags.asJsonFormat(), "{ \"key1\": \"one\", \"key2\": 2, \"akey\": \"apple\", \"key5\": true, \"key6\": false, \"key3\": 3.3, \"key4\": 4.444444444E60, \"key7\": null }", "List elements should match asJsonFormat()")},
                {new TestCase(fixture, FormatFlags.defaults().withQuoteStrings(true), "{ \"key1\": \"one\", \"key2\": 2, \"akey\": \"apple\", \"key5\": true, \"key6\": false, \"key3\": 3.3, \"key4\": 4.444444444E60, \"key7\": null }", "List elements should match withQuoteStrings(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withSingleQuotes(true).withQuoteStrings(true), "{ 'key1': 'one', 'key2': 2, 'akey': 'apple', 'key5': true, 'key6': false, 'key3': 3.3, 'key4': 4.444444444E60, 'key7': null }", "List elements should match withSingleQuotes(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withUseRepr(true), "{ key1: one, key2: 2, akey: apple, key5: true, key6: false, key3: 3.3, key4: 4.444444444E60, key7: null }", "List elements should match withUseRepr(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withFormatJson(true), "{ key1: one, key2: 2, akey: apple, key5: true, key6: false, key3: 3.3, key4: 4.444444444E60, key7: null }", "List elements should match withFormatJson(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withIndent(4), "{ key1: one, key2: 2, akey: apple, key5: true, key6: false, key3: 3.3, key4: 4.444444444E60, key7: null }", "List elements should match withIndent(4)")},
                {new TestCase(fixture, FormatFlags.defaults().withMultipleLines(true), "{\n  key1: one,\n  key2: 2,\n  akey: apple,\n  key5: true,\n  key6: false,\n  key3: 3.3,\n  key4: 4.444444444E60,\n  key7: null\n}", "List elements should match withMultipleLines(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withOmitCommas(true), "{ key1: one key2: 2 akey: apple key5: true key6: false key3: 3.3 key4: 4.444444444E60 key7: null }", "List elements should match withOmitCommas(true)")},

        };
        return Arrays.stream(testData).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("nestedSingleElementPrimitiveMaps")
    void testNestedSingleElementPrimitiveMaps(TestCase testCase) {
        JsonPrettyPrinter pp = new JsonPrettyPrinter();
        String actual = pp.prettyPrintJson(testCase.structured(), testCase.flags());
        assertEquals(testCase.expected(), actual, testCase.message());
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    static Stream<Arguments> nestedSingleElementPrimitiveMaps() {
        Map<String, Object> map1 = Map.of("nestedKey1", 1);
        Map<String, Object> map2 = Map.of("nestedKey2", "two");
        Map<String, Object> map3 = Map.of("nestedKey3", true);
        Map<String, Object> map4 = new HashMap<>();
        Map<String, Object> fixture = Map.of( "key1", map1, "key2", map2, "key3", map3, "key4", map4);


        Object[][] testData = {
                {new TestCase(fixture, FormatFlags.defaults(), "{ key1: { nestedKey1: 1 }, key2: { nestedKey2: two }, key3: { nestedKey3: true }, key4: { } }", "List elements should match defaults()")},
                {new TestCase(fixture, FormatFlags.asJsonFormat(), "{ \"key1\": { \"nestedKey1\": 1 }, \"key2\": { \"nestedKey2\": \"two\" }, \"key3\": { \"nestedKey3\": true }, \"key4\": { } }", "List elements should match asJsonFormat()")},
                {new TestCase(fixture, FormatFlags.defaults().withQuoteStrings(true), "{ \"key1\": { \"nestedKey1\": 1 }, \"key2\": { \"nestedKey2\": \"two\" }, \"key3\": { \"nestedKey3\": true }, \"key4\": { } }", "List elements should match withQuoteStrings(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withSingleQuotes(true).withQuoteStrings(true), "{ 'key1': { 'nestedKey1': 1 }, 'key2': { 'nestedKey2': 'two' }, 'key3': { 'nestedKey3': true }, 'key4': { } }", "List elements should match withQuoteStrings(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withUseRepr(true), "{ key1: { nestedKey1: 1 }, key2: { nestedKey2: two }, key3: { nestedKey3: true }, key4: { } }", "List elements should match withUseRepr(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withFormatJson(true), "{ key1: { nestedKey1: 1 }, key2: { nestedKey2: two }, key3: { nestedKey3: true }, key4: { } }", "List elements should match withFormatJson(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withIndent(4), "{ key1: { nestedKey1: 1 }, key2: { nestedKey2: two }, key3: { nestedKey3: true }, key4: { } }", "List elements should match withIndent(4)")},
                {new TestCase(fixture, FormatFlags.defaults().withMultipleLines(true), "{\n  key1: { nestedKey1: 1 },\n  key2: { nestedKey2: two },\n  key3: { nestedKey3: true },\n  key4: { }\n}", "List elements should match withMultipleLines(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withOmitCommas(true), "{ key1: { nestedKey1: 1 } key2: { nestedKey2: two } key3: { nestedKey3: true } key4: { } }", "List elements should match withOmitCommas(true)")},

        };


        return Arrays.stream(testData).map(Arguments::of);
    }

}
