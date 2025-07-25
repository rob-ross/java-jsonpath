package org.killeroonie.json;

import org.jetbrains.annotations.NotNull;

import java.util.*;

// note to myself:
// Javadoc comments start with an implicit "This method..." and uses descriptive not imperative voice.
// i.e., "This method calculates..., This method finds...", rather than Python's "Calculate the foo..., Find the foo..."
// The @return tag answers the question "What is returned?" and is a Noun phrase.

public interface JsonTypes {

    sealed interface JsonValue
            permits JsonPrimitive, JsonStructured {
    }
    sealed interface JsonPrimitive<T> extends JsonValue
            permits JsonBoolean, JsonNull, JsonNumber, JsonString {

        T value();
    }

    sealed interface JsonStructured<T> extends JsonValue
            permits JsonArray, JsonObject {

        int size();
        boolean isEmpty();
        int identityHashCode();
        T value();
        /**
         * Returns the first element of this JsonStructured type.<p>
         * For a {@link JsonArray}, this is the first element in the {@link List}.<p>
         * For a {@link JsonObject}, this is the first key in the {@link Map}, based on the Map's iteration order.
         *
         * @return the first {@link JsonValue} in this JsonStructured instance.
         * @throws java.util.NoSuchElementException if the Map is empty.
         */
        JsonValue getFirst();
        Iterator<? extends  JsonValue> iterator();
    }

    // --- Concrete Implementations ---
    record JsonArray(List<JsonValue> elements) implements JsonStructured<List<JsonValue>> {

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

        /**
         * Returns a unique identifier for the {@code elements} {@link List} instance.<p>
         * This implementation delegates to {@link System#identityHashCode(Object)} to generate the identifier.
         *
         * @return the unique identifier for the {@code elements} List instance as an int.
         */
        @Override
        public int identityHashCode() {  return System.identityHashCode(elements); }

        @Override
        public List<JsonValue> value() {  return elements; }

        /**
         * {@inheritDoc}
         */
        @Override
        public JsonValue getFirst() { return elements.getFirst(); }

        @Override
        @NotNull
        public Iterator<JsonValue> iterator() { return elements.iterator(); }
    }

    record JsonObject(Map<JsonString, JsonValue> members) implements JsonStructured<Map<JsonString, JsonValue>> {

        public JsonObject {
            Objects.requireNonNull(members, "`members` cannot be null");
        }

        @Override
        public int size() {
            return members.size();
        }

        @Override
        public boolean isEmpty() {
            return members.isEmpty();
        }

        /**
         * Returns a unique identifier for the {@code members} {@link Map} instance.<p>
         * This implementation delegates to {@link System#identityHashCode(Object)} to generate the identifier.
         *
         * @return the unique identifier for the {@code members} Map instance as an int.
         */
        @Override
        public int identityHashCode() {  return System.identityHashCode(members); }

        @Override
        public Map<JsonString, JsonValue> value() {  return members; }

        /**
         * {@inheritDoc}
         */
        @Override
        public JsonString getFirst() {  return getFirstKey(); }

        /**
         * Returns the first key in the {@link Map}, based on the Map's iteration order.
         *
         * @return the first {@link JsonString} key in the Map.
         * @throws java.util.NoSuchElementException if the Map is empty.
         */
        public JsonString getFirstKey() {
            if (members.isEmpty()) { throw new NoSuchElementException("Map is  empty"); }
            return members.keySet().iterator().next(); }

        /**
         * Retrieves the {@link JsonValue} associated with the first key in the {@link Map} based on the Map's
         * iteration order.
         *
         * @return the JsonValue for the first key in the Map, determined by the iteration order of the keys.
         * @throws java.util.NoSuchElementException if the Map is empty.
         */
        public JsonValue getFirstValue() { return members.get(getFirstKey()); }

        @Override
        @NotNull
        public Iterator<JsonString> iterator() { return members.keySet().iterator(); }

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
