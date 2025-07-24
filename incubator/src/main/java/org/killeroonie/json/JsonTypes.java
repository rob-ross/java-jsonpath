package org.killeroonie.json;

import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface JsonTypes {

    sealed interface JsonValue
            permits JsonPrimitive, JsonStructured {
    }
    sealed interface JsonPrimitive<T> extends JsonValue
            permits JsonBoolean, JsonNull, JsonNumber, JsonString {

        T value();


    }

    sealed interface JsonStructured extends JsonValue
            permits JsonArray, JsonObject {

        int size();
        boolean isEmpty();
    }

    // --- Concrete Implementations ---
    record JsonArray(List<JsonValue> elements) implements JsonStructured {

        public JsonArray {
            Objects.requireNonNull(elements, "elements cannot be null");
        }
        @Override
        public int size() {
            return elements.size();
        }

        @Override
        public boolean isEmpty() {
            return elements.isEmpty();
        }
    }
    record JsonObject(Map<JsonString, JsonValue> members) implements JsonStructured {

        public JsonObject {
            Objects.requireNonNull(members, "members cannot be null");
        }

        @Override
        public int size() {
            return members.size();
        }

        @Override
        public boolean isEmpty() {
            return members.isEmpty();
        }
    }

    record JsonString(String value) implements JsonPrimitive<String> {

        public JsonString {
            Objects.requireNonNull(value, "value cannot be null");
        }
        @Override
        public @NotNull String toString() {
            return value();
        }
    }
    record JsonNumber(Number value)   implements JsonPrimitive<Number> {
        public JsonNumber {
            Objects.requireNonNull(value, "value cannot be null");
        }
        @NotNull
        @Override
        public  String toString() {
            return value().toString();
        }
    }

    record JsonBoolean(Boolean value) implements JsonPrimitive<Boolean> {
        public JsonBoolean {
            Objects.requireNonNull(value, "value cannot be null");
        }
        @Override
        @NotNull
        public  String toString() {
            return value().toString();
        }
    }

    /** Represents the JSON null value using a singleton pattern. */
    final class JsonNull implements JsonPrimitive<JsonNull> {
        private static final JsonNull INSTANCE = new JsonNull();
        private JsonNull() {}
        static JsonNull getInstance() {
            return INSTANCE;
        }

        @Override
        public JsonNull value() {
            return this;
        }

        @Override
        public String toString() {
            return "null";
        }
    }

}
