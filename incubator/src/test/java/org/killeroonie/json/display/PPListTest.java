package org.killeroonie.json.display;

import org.junit.jupiter.api.Test;
import org.killeroonie.json.JsonTypes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PPListTest
{
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
        List<JsonValue> list = new ArrayList<>( List.of( new JsonNumber(1) ));
        String expected = "[ 1 ]";
        String actual = pp.prettyPrintJson(new JsonArray(list));
        assertEquals(expected, actual, "List returns '[ 1 ]'");
    }

    @Test
    void test_multiPrimitiveList() {
        JsonPrettyPrinter pp = new JsonPrettyPrinter();
        Object[] array = new Object[]{ "one", 2, 3.3, 4.444444444e60, true, false, null };
        List<JsonValue> list = convertToList( array );
        System.out.println("list: " + list);
        String expected = "[ one, 2, 3.3, 4.444444444E60, true, false, null ]";
        String actual = pp.prettyPrintJson(new JsonArray(list));
        System.out.println("actual: " + actual);
        assertEquals(expected, actual, "List elements should match");
    }

    /**
     * Converts the elements in the argument array into a list of JsonValue wrapper objects.
     */
    List<JsonValue> convertToList(Object[] array) {
        List<JsonValue> result = new ArrayList<>();
        for ( Object o : array ) {
            switch (o) {
                case String s -> result.add(new JsonString(s));
                case Byte b -> result.add(new JsonNumber(b));
                case Short s -> result.add(new JsonNumber(s));
                case Integer i -> result.add(new JsonNumber(i));
                case Long l -> result.add(new JsonNumber(l));
                case Float f -> result.add(new JsonNumber(f));
                case Double d -> result.add(new JsonNumber(d));
                case Boolean b -> result.add(JsonBoolean.forBoolean(b));
                case null -> result.add(JsonNull.getInstance());
                case Object[] arr -> {
                    List<JsonValue> nestedList = convertToList(arr);
                    result.add(new JsonArray(nestedList));
                }
                // todo - deal with Maps
                default -> throw new IllegalArgumentException("Unsupported value " + o);
            }
        }
        return result;
    }
}
