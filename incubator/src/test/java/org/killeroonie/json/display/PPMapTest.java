package org.killeroonie.json.display;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.killeroonie.json.JsonTypes.*;
import org.killeroonie.json.display.Helper.TestCase;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.killeroonie.json.display.Helper.mapForArray;

public class PPMapTest {

    @Test
    void test_emptyMap() {
        JsonPrettyPrinter pp = new JsonPrettyPrinter();
        // Empty Map
        //noinspection MismatchedQueryAndUpdateOfCollection
        Map<JsonString, JsonValue> map = new LinkedHashMap<>();
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
    }

    static Stream<Arguments> primitiveMultiElementMapTestCases() {
        // The same fixture is used for all these tests; we only vary the FormatFlags.
        Map<String, Object> fixture = new LinkedHashMap<>();
        fixture.put("akey", "apple");
        fixture.put("key1", "one");
        fixture.put("key2", 2);
        fixture.put("key3", 3.3);
        fixture.put("key4", 4.444444444e60);
        fixture.put("key5", true);
        fixture.put("key6", false);
        fixture.put("key7", null);

        Object[][] testData = {
                {new TestCase(fixture, FormatFlags.defaults(), "{ akey: apple, key1: one, key2: 2, key3: 3.3, key4: 4.444444444E60, key5: true, key6: false, key7: null }", "Map members should match defaults()")},
                {new TestCase(fixture, FormatFlags.asJsonFormat(), "{ \"akey\": \"apple\", \"key1\": \"one\", \"key2\": 2, \"key3\": 3.3, \"key4\": 4.444444444E60, \"key5\": true, \"key6\": false, \"key7\": null }", "Map members should match asJsonFormat()")},
                {new TestCase(fixture, FormatFlags.defaults().withQuoteStrings(true), "{ \"akey\": \"apple\", \"key1\": \"one\", \"key2\": 2, \"key3\": 3.3, \"key4\": 4.444444444E60, \"key5\": true, \"key6\": false, \"key7\": null }", "Map members should match withQuoteStrings(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withSingleQuotes(true).withQuoteStrings(true), "{ 'akey': 'apple', 'key1': 'one', 'key2': 2, 'key3': 3.3, 'key4': 4.444444444E60, 'key5': true, 'key6': false, 'key7': null }", "Map members should match withSingleQuotes(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withUseRepr(true), "{ akey: apple, key1: one, key2: 2, key3: 3.3, key4: 4.444444444E60, key5: true, key6: false, key7: null }", "Map members should match withUseRepr(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withFormatJson(true), "{ akey: apple, key1: one, key2: 2, key3: 3.3, key4: 4.444444444E60, key5: true, key6: false, key7: null }", "Map members should match withFormatJson(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withIndent(4), "{ akey: apple, key1: one, key2: 2, key3: 3.3, key4: 4.444444444E60, key5: true, key6: false, key7: null }", "Map members should match withIndent(4)")},
                {new TestCase(fixture, FormatFlags.defaults().withMultipleLines(true), "{\n  akey: apple,\n  key1: one,\n  key2: 2,\n  key3: 3.3,\n  key4: 4.444444444E60,\n  key5: true,\n  key6: false,\n  key7: null\n}", "Map members should match withMultipleLines(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withOmitCommas(true), "{ akey: apple key1: one key2: 2 key3: 3.3 key4: 4.444444444E60 key5: true key6: false key7: null }", "Map members should match withOmitCommas(true)")},

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
        Map<String, Object> map1 = mapForArray(new Object[][]{ {"nestedKey1", 1} });
        Map<String, Object> map2 = mapForArray(new Object[][]{ {"nestedKey2", "two"} });
        Map<String, Object> map3 = mapForArray(new Object[][]{ {"nestedKey3", true} });
        Map<String, Object> map4 = new LinkedHashMap<>();  // Empty
        Map<String, Object> fixture = mapForArray( new Object[][]{ {"key1", map1}, {"key2", map2}, {"key3", map3}, {"key4", map4} });


        Object[][] testData = {
                {new TestCase(fixture, FormatFlags.defaults(), "{ key1: { nestedKey1: 1 }, key2: { nestedKey2: two }, key3: { nestedKey3: true }, key4: { } }", "Map members should match defaults()")},
                {new TestCase(fixture, FormatFlags.asJsonFormat(), "{ \"key1\": { \"nestedKey1\": 1 }, \"key2\": { \"nestedKey2\": \"two\" }, \"key3\": { \"nestedKey3\": true }, \"key4\": { } }", "Map members should match asJsonFormat()")},
                {new TestCase(fixture, FormatFlags.defaults().withQuoteStrings(true), "{ \"key1\": { \"nestedKey1\": 1 }, \"key2\": { \"nestedKey2\": \"two\" }, \"key3\": { \"nestedKey3\": true }, \"key4\": { } }", "Map members should match withQuoteStrings(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withSingleQuotes(true).withQuoteStrings(true), "{ 'key1': { 'nestedKey1': 1 }, 'key2': { 'nestedKey2': 'two' }, 'key3': { 'nestedKey3': true }, 'key4': { } }", "Map members should match withQuoteStrings(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withUseRepr(true), "{ key1: { nestedKey1: 1 }, key2: { nestedKey2: two }, key3: { nestedKey3: true }, key4: { } }", "Map members should match withUseRepr(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withFormatJson(true), "{ key1: { nestedKey1: 1 }, key2: { nestedKey2: two }, key3: { nestedKey3: true }, key4: { } }", "Map members should match withFormatJson(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withIndent(4), "{ key1: { nestedKey1: 1 }, key2: { nestedKey2: two }, key3: { nestedKey3: true }, key4: { } }", "Map members should match withIndent(4)")},
                {new TestCase(fixture, FormatFlags.defaults().withMultipleLines(true), "{\n  key1: { nestedKey1: 1 },\n  key2: { nestedKey2: two },\n  key3: { nestedKey3: true },\n  key4: { }\n}", "Map members should match withMultipleLines(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withOmitCommas(true), "{ key1: { nestedKey1: 1 } key2: { nestedKey2: two } key3: { nestedKey3: true } key4: { } }", "Map members should match withOmitCommas(true)")},

        };


        return Arrays.stream(testData).map(Arguments::of);
    }


    @ParameterizedTest
    @MethodSource("nestedPrimitiveElementMapsTestCases")
    void testNestedPrimitiveElementMaps(TestCase testCase) {
        JsonPrettyPrinter pp = new JsonPrettyPrinter();
        String actual = pp.prettyPrintJson(testCase.structured(), testCase.flags());
        assertEquals(testCase.expected(), actual, testCase.message());
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    static Stream<Arguments> nestedPrimitiveElementMapsTestCases() {
        Map<String, Object> map1 = mapForArray(new Object[][]{ {"one", 1}, {"two", 2.2}, {"three", 3.14159} });
        Map<String, Object> map2 = mapForArray(new Object[][]{ {"flag1", true}, {"flag2", false} });
        Map<String, Object> map3 = mapForArray(new Object[][]{ {"color", "blue"} });
        Map<String, Object> map4 = mapForArray(new Object[][]{ {"null", null}, {"empty", ""} });
        Map<String, Object> map5 = new LinkedHashMap<>();  // Empty Map
        Map<String, Object> fixture = mapForArray( new Object[][]{ {"map1", map1}, {"map2", map2}, {"map3", map3}, {"map4", map4}, {"map5", map5} });


        Object[][] testData = {
                {new TestCase(fixture, FormatFlags.defaults(), "{ map1: { one: 1, two: 2.2, three: 3.14159 }, map2: { flag1: true, flag2: false }, map3: { color: blue }, map4: { null: null, empty:  }, map5: { } }", "Map members should match defaults()")},
                {new TestCase(fixture, FormatFlags.asJsonFormat(), "{ \"map1\": { \"one\": 1, \"two\": 2.2, \"three\": 3.14159 }, \"map2\": { \"flag1\": true, \"flag2\": false }, \"map3\": { \"color\": \"blue\" }, \"map4\": { \"null\": null, \"empty\": \"\" }, \"map5\": { } }", "Map members should match asJsonFormat()")},
                {new TestCase(fixture, FormatFlags.defaults().withQuoteStrings(true), "{ \"map1\": { \"one\": 1, \"two\": 2.2, \"three\": 3.14159 }, \"map2\": { \"flag1\": true, \"flag2\": false }, \"map3\": { \"color\": \"blue\" }, \"map4\": { \"null\": null, \"empty\": \"\" }, \"map5\": { } }", "Map members should match withQuoteStrings(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withSingleQuotes(true).withQuoteStrings(true), "{ 'map1': { 'one': 1, 'two': 2.2, 'three': 3.14159 }, 'map2': { 'flag1': true, 'flag2': false }, 'map3': { 'color': 'blue' }, 'map4': { 'null': null, 'empty': '' }, 'map5': { } }", "Map members should match withQuoteStrings(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withUseRepr(true), "{ map1: { one: 1, two: 2.2, three: 3.14159 }, map2: { flag1: true, flag2: false }, map3: { color: blue }, map4: { null: null, empty:  }, map5: { } }", "Map members should match withUseRepr(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withFormatJson(true), "{ map1: { one: 1, two: 2.2, three: 3.14159 }, map2: { flag1: true, flag2: false }, map3: { color: blue }, map4: { null: null, empty:  }, map5: { } }", "Map members should match withFormatJson(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withIndent(4), "{ map1: { one: 1, two: 2.2, three: 3.14159 }, map2: { flag1: true, flag2: false }, map3: { color: blue }, map4: { null: null, empty:  }, map5: { } }", "Map members should match withIndent(4)")},
                {new TestCase(fixture, FormatFlags.defaults().withMultipleLines(true), "{\n  map1:\n  {\n    one: 1,\n    two: 2.2,\n    three: 3.14159\n  },\n  map2:\n  {\n    flag1: true,\n    flag2: false\n  },\n  map3: { color: blue },\n  map4:\n  {\n    null: null,\n    empty: \n  },\n  map5: { }\n}", "Map members should match withMultipleLines(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withOmitCommas(true), "{ map1: { one: 1 two: 2.2 three: 3.14159 } map2: { flag1: true flag2: false } map3: { color: blue } map4: { null: null empty:  } map5: { } }", "Map members should match withOmitCommas(true)")},

        };


        return Arrays.stream(testData).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("nestedPrimitiveElementMultiMemberMapsTestCases")
    void testNestedPrimitiveElementMultiMemberMaps(TestCase testCase) {
        JsonPrettyPrinter pp = new JsonPrettyPrinter();
        String actual = pp.prettyPrintJson(testCase.structured(), testCase.flags());
        assertEquals(testCase.expected(), actual, testCase.message());
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    static Stream<Arguments> nestedPrimitiveElementMultiMemberMapsTestCases() {
        Map<String, Object> map1 = mapForArray(new Object[][]{ {"one", 1}, {"two", 2.2}, {"three", 3.14159} });
        Map<String, Object> map2 = mapForArray(new Object[][]{ {"flag1", true}, {"flag2", false} });
        Map<String, Object> map3 = mapForArray(new Object[][]{ {"color", "blue"} });
        Map<String, Object> map4 = mapForArray(new Object[][]{ {"null", null}, {"empty", ""} });
        Map<String, Object> map5 = new LinkedHashMap<>();  // Empty Map
        Map<String, Object> fixture = mapForArray( new Object[][]{ {"map1", map1}, {"map2", map2}, {"map3", map3}, {"map4", map4}, {"map5", map5} });

        Object[][] testData = {
                {new TestCase(fixture, FormatFlags.defaults().withMultipleLines(true), "{\n  map1:\n  {\n    one: 1,\n    two: 2.2,\n    three: 3.14159\n  },\n  map2:\n  {\n    flag1: true,\n    flag2: false\n  },\n  map3: { color: blue },\n  map4:\n  {\n    null: null,\n    empty: \n  },\n  map5: { }\n}", "Map elements should match withMultipleLines(true)")},
                {new TestCase(fixture, FormatFlags.asJsonFormat().withMultipleLines(true), "{\n  \"map1\":\n  {\n    \"one\": 1,\n    \"two\": 2.2,\n    \"three\": 3.14159\n  },\n  \"map2\":\n  {\n    \"flag1\": true,\n    \"flag2\": false\n  },\n  \"map3\": { \"color\": \"blue\" },\n  \"map4\":\n  {\n    \"null\": null,\n    \"empty\": \"\"\n  },\n  \"map5\": { }\n}", "Map elements should match asJsonFormat()")},
                {new TestCase(fixture, FormatFlags.defaults().withMultipleLines(true).withQuoteStrings(true), "{\n  \"map1\":\n  {\n    \"one\": 1,\n    \"two\": 2.2,\n    \"three\": 3.14159\n  },\n  \"map2\":\n  {\n    \"flag1\": true,\n    \"flag2\": false\n  },\n  \"map3\": { \"color\": \"blue\" },\n  \"map4\":\n  {\n    \"null\": null,\n    \"empty\": \"\"\n  },\n  \"map5\": { }\n}", "Map elements should match withQuoteStrings(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withMultipleLines(true).withQuoteStrings(true).withSingleQuotes(true), "{\n  'map1':\n  {\n    'one': 1,\n    'two': 2.2,\n    'three': 3.14159\n  },\n  'map2':\n  {\n    'flag1': true,\n    'flag2': false\n  },\n  'map3': { 'color': 'blue' },\n  'map4':\n  {\n    'null': null,\n    'empty': ''\n  },\n  'map5': { }\n}", "Map elements should match withSingleQuotes(true)")},
                {new TestCase(fixture, FormatFlags.defaults().withMultipleLines(true).withIndent(4), "{\n    map1:\n    {\n        one: 1,\n        two: 2.2,\n        three: 3.14159\n    },\n    map2:\n    {\n        flag1: true,\n        flag2: false\n    },\n    map3: { color: blue },\n    map4:\n    {\n        null: null,\n        empty: \n    },\n    map5: { }\n}", "Map elements should match withIndent(4)")},
                {new TestCase(fixture, FormatFlags.defaults().withMultipleLines(true).withOmitCommas(true), "{\n  map1:\n  {\n    one: 1\n    two: 2.2\n    three: 3.14159\n  }\n  map2:\n  {\n    flag1: true\n    flag2: false\n  }\n  map3: { color: blue }\n  map4:\n  {\n    null: null\n    empty: \n  }\n  map5: { }\n}", "Map elements should match withOmitCommas(true)")},
        };
        return Arrays.stream(testData).map(Arguments::of);
    }
}
