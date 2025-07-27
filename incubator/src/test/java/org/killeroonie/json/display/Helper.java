package org.killeroonie.json.display;

import org.killeroonie.json.JsonTypes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper methods for JsonPrettyPrinter unit tests.
 */
public class Helper {

    
    public static JsonPrimitive<?> toJsonPrimitive(Object value) {
        if (value instanceof JsonStructured<?>){
            throw new IllegalArgumentException("Expected a JSON primitive but got " + value.getClass());
        }
        return switch (value) {
            case JsonPrimitive<?> jp -> jp;
            case String s -> new JsonString(s);
            case Number n -> new JsonNumber(n);
            case Boolean b -> JsonBoolean.forBoolean(b);
            case null -> JsonNull.getInstance();
            default -> throw new IllegalArgumentException("Expected a JSON primitive but got " + value.getClass());
        };
    }
    @SuppressWarnings("unchecked")
    public static JsonObject toJsonObject(Map<String, Object> map) {
        Map<JsonString, JsonValue> jmap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            JsonString key = new JsonString(entry.getKey());
            if (entry.getValue() == null) {
                jmap.put(key, JsonNull.getInstance());
            }
            else {
                switch (entry.getValue()) {
                    case JsonValue jv -> jmap.put(key, jv);
                    case Map<?, ?> m -> jmap.put(key, toJsonObject((Map<String, Object>) m));  // type: ignore
                    case Object[] array -> jmap.put(key, toJsonArray(array));
                    case Object o -> jmap.put(key, toJsonPrimitive(o));
                }
            }
        }
        return new JsonObject(jmap);
    }

    /**
     * Helper method that converts the elements in the argument array into a list of JsonValue wrapper objects.
     */
    public static JsonArray toJsonArray(Object[] array) {
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
                case Object[] arr -> result.add(toJsonArray(arr));

                // todo - deal with Maps
                default -> throw new IllegalArgumentException("Unsupported value " + o);
            }
        }
        return new JsonArray(result);
    }

    /**
     * Encapsulates data for the parameterized tests in this file.
     * @param fixture simple Java Object[] which makes inline initialization simple.
     * @param flags the FormatFlag settings used in a particular test.
     * @param expected the String we expect to be returned from the JSON pretty printer.
     * @param message the message to be displayed for a failed test.
     * @param structured the actual argument passed to the prettyPrintJson method.
     * This is created by the TestCase constructor by converting all Java primitives or Lists and Maps to their JsonValues.
     */
    public record TestCase(Object fixture, FormatFlags flags, String expected, String message, JsonStructured<?> structured) {
        /**
         * Automatically converts fixture to a {@link JsonArray}. This makes it easier to write inline test case data
         * in the source file with less boilerplate.
         */
        TestCase(Object[] fixture, FormatFlags flags, String expected, String message) {
            this(fixture, flags, expected, message, toJsonArray(fixture));
        }

        TestCase(Map<String, Object> fixture, FormatFlags flags, String expected, String message) {
            this(fixture, flags, expected, message, toJsonObject(fixture));
        }
    }
}
