package org.killeroonie.json.display;

import com.fasterxml.jackson.databind.node.ValueNode;
import org.jetbrains.annotations.NotNull;
import org.killeroonie.json.JsonTypes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Experimental pretty-printer for a JSON value graph. <p>
 *
 *     The main entry point is {@code prettyPrintJson()}. This requires the argument to be a
 *     {@link org.killeroonie.json.JsonTypes.JsonValue} wrapper. This ensures adherence to the JSON spec when
 *     formatting a JSON value. It returns a String containing the pretty-printed output.  <p>
 *
 *     Todo - implement a general purpose pretty-printer for nested data.
 *
 */
public class JsonPrettyPrinter {

    private static final Logger logger = LoggerFactory.getLogger(JsonPrettyPrinter.class);

    static final String SPACE = " ";
    static final String COMMA = ",";
    static final String EMPTY_STRING = "";
    static final String NEW_LINE = "\n";

    private String spacer(FormatFlags format, int level) {
        if (! format.multiLine())
            return SPACE;
        return SPACE.repeat(format.indent() * level );
    }

    /**
     *     Recurse the List or Map and return true if every nested element is either empty or contains
     *     exactly one primitive list element or one key/value pair where the value is a single primitive value.
     *     Another way to think of this is, if the structure does not require a comma, this method will return true
     *     E.g.
     *     [ [ [ ] ] ] ,  [ [ [ "one" ] ] ]  - both return true
     *     { "key: "one" },  { "key": [ [ "one"  ] ] } - both return true
     *     [ [ { "key": [ [ "foo"  ] ] } ] ]  - returns true
     *     { "key1": { "key2": { "key3": "foo }}} - returns true
     *     [ [ [ "one", "two" ] ] ] - returns false
     *     { "key": [ [ "one", "two"  ] ] }  - returns false
     *     { "key": [ [ { "one":"foo", "two":"bar" } ] ] } - returns false
     * 
     * @param value the JsonValue to check
     * @return true if the `value` argument is a JSON primitive value, or a one-element List or Map whose
     * value is a primitive value. Otherwise, return false.
     */
    private boolean isEmptyOrSingleItem(JsonValue value) {
        return switch (value) {
            case JsonPrimitive<?> _ -> true;
            case JsonStructured<?> s when s.isEmpty() -> true;
            case JsonArray a when a.size() == 1 -> isEmptyOrSingleItem(a.getFirst());
            case JsonObject o when o.size() == 1 -> isEmptyOrSingleItem(o.members().values().iterator().next());
            // This default handles cases where JsonStructured size > 1
            default -> false;
        };
    }

    private String formatString(String string,  FormatFlags format) {
        char quoteChar = format.singleQuotes() ? '\'': '"';
        if (format.quoteStrings()) {
            return String.format("%c%s%c",quoteChar, string,  quoteChar  );
        }
        else {
            return string;
        }
    }

    private String formatScalar(Object o,  FormatFlags format) {
        if (o == null) { return "null"; }
        if (o instanceof String) {
            return formatString((String) o, format);
        }
        switch (o) {
            case ValueNode vn -> { return formatValueNode(vn, format); }
            case JsonPrimitive<?> pv -> { return formatJsonPrimitive(pv, format); }
            default ->  { return o.toString(); }
        }
    }

    private String formatValueNode(ValueNode node, FormatFlags format) {
        Objects.requireNonNull(node, "node is null");
        if (! node.isValueNode()) { throw new IllegalArgumentException("node is not a value node"); }
        if (node.isTextual()) { return formatString(node.textValue(), format); }
        return node.asText();
    }

    private String formatJsonPrimitive(JsonPrimitive<?> primitive, FormatFlags format) {
        if (primitive == null || primitive.value() == null) {
            return "null";
        }
        Object o = primitive.value();
        if (o instanceof String s) {
            return formatString(s, format);
        }
        return o.toString();
    }
    
    private int lastListIndex(final List<?> list) {
        int index = 0;
        if (list != null && !list.isEmpty()) {
            index = list.size() - 1;
        }
        return index;
    }

    private void appendToLastListElement(List<String> list, String stringToAppend) {
        String line = list.getLast() + stringToAppend;
        list.set(lastListIndex(list), line);
    }


    @SuppressWarnings("UnusedReturnValue")
    List<String> formatStructured(final @NotNull JsonStructured<?> structured,
                                  final @NotNull FormatFlags  format,
                                  final @NotNull List<String> lines,
                                  int level,
                                  final @NotNull Map<Integer, JsonValue> instanceIDs) {

        Objects.requireNonNull(structured, "`structured` cannot be null");
        Objects.requireNonNull(format, "`format` cannot be null");
        Objects.requireNonNull(lines, "`lines` cannot be null");
        Objects.requireNonNull(instanceIDs, "`instanceIDs` cannot be null");

        if (lines.isEmpty()) {
            lines.add(EMPTY_STRING);
        }

        //initialize structure specific variables
        String javaTypeName;
        String jsonTypeName;
        String LD, RD; // Left Delimiter, Right Delimiter
        switch (structured){
            case JsonArray _ -> {
                javaTypeName = "List";
                jsonTypeName = "Array";
                LD = "[";
                RD = "]";
            }
            case JsonObject _ -> {
                javaTypeName = "Map";
                jsonTypeName = "Object";
                LD = "{";
                RD = "}";
            }
        }

        String indentString;
        // EMPTY_STRING is an interned constant. The empty string is always added to `lines` via EMPTY_STRING,
        // so the identity comparison is correct and at O(1), more efficient than equals() at O(N).
        //noinspection StringEquality
        if ( lines.getLast() != EMPTY_STRING ) {
            // the current line already has text, so indent is relative to the end of that text
            indentString = SPACE;
        }
        else if (lines.size() == 1 || level == 0) {
            indentString = EMPTY_STRING;  // The top level or very first line doesn't get indented
        }
        else {
            indentString = spacer(format, level);
        }
        int id = structured.identityHashCode();
        if ( instanceIDs.containsKey(id) ) {
            // we have seen this instance previously, cycle detected
            logger.warn("Cycle detected at object {}: {}", javaTypeName, structured.value());
            appendToLastListElement(lines, "%s%s...%s".formatted(indentString, LD, RD));
            return lines;
        }
        else {
            // save for future cycle detection
            instanceIDs.put(id, structured);
        }
        if (structured.isEmpty()) {
            appendToLastListElement(lines, "%s%s %s".formatted( indentString, LD , RD ));
            return lines;
        }
        if (structured.size() == 1) {
            JsonValue v = structured.getFirst(); // first list element or first Map key
            JsonString key = null;
            JsonValue value = v;
            if (structured instanceof JsonObject(Map<JsonString, JsonValue> members)) {
                key = (JsonString) v;
                value = members.get(key);
            }

            if (value instanceof JsonPrimitive<?> primitive) {
                String vf =  formatScalar(primitive, format);
                switch (structured) {
                    case JsonArray  _ -> appendToLastListElement(lines, "%s[ %s ]".formatted(indentString, vf ));
                    case JsonObject _ -> {
                        String kf = formatScalar(key, format);
                        appendToLastListElement(lines, "%s{ %s: %s }".formatted( indentString, kf, vf ));
                    }
                }
                return lines;
            }
        }
        String comma = format.omitCommas() ? EMPTY_STRING : COMMA;
        String sp    = format.multiLine()  ? EMPTY_STRING : SPACE;
        appendToLastListElement(lines, "%s%s".formatted(indentString, LD));  // start of the Map/List text: '{' or '['
        level++;
        indentString = spacer(format, level);

        int index = 0;

        for (Iterator<? extends JsonValue> it = structured.iterator(); it.hasNext(); ) {
            var item = it.next();
            // for dealing with commas
            boolean firstItem = index == 0;
            boolean lastItem  = index == structured.size() - 1;   // no comma after the last member

            JsonValue v = item;
            JsonString key = null;
            JsonValue value = v;
            String kf = null;
            if (structured instanceof JsonObject(Map<JsonString, JsonValue> members)) {
                key = (JsonString) v;
                value = members.get(key);
                kf =  formatScalar(key, format);
            }
            lines.add(EMPTY_STRING);
            if ( value instanceof JsonPrimitive<?> primitive) {
                String vf =  formatScalar(primitive, format);
                String template = switch (structured) {
                    case JsonObject _ -> "%s%s: %s".formatted(indentString, kf, vf);
                    case JsonArray  _ -> "%s%s".formatted(indentString, vf);
                };
                appendToLastListElement(lines, template);
            }
            else if (structured instanceof JsonArray) {
                // process the child value recursively
                formatStructured((JsonStructured<?>) value, format, lines, level, instanceIDs);
            }
            else if (structured instanceof JsonObject) {
                appendToLastListElement(lines, "%s%s:".formatted(indentString, kf));
                // Here, `value` is not JsonPrimitive due to the check above. Therefore, it's a JsonStructured
                JsonStructured<?> nestedStructured = (JsonStructured<?>) value;
                // Special case where the value is either an empty List, or a one-element List with a primitive element,
                // or where the value is an empty Map or a Map with one key with a primitive value.
                // We can display the nested value on the same line as the key name of the parent Map.
                // Otherwise, we display the value on the next line.
                if (nestedStructured.size() > 1) {
                    lines.add(EMPTY_STRING);
                }
                else if (nestedStructured.size() == 1) {
                    switch (nestedStructured) {
                        case JsonArray a -> {
                            // If there is only a single primitive element, we print it on the same line.
                            if (! isEmptyOrSingleItem(a)) {
                                lines.add(EMPTY_STRING);
                            }
                        }
                        case JsonObject o -> {
                            var nv = o.getFirstValue();
                            if (! (nv instanceof  JsonPrimitive<?> _)) {
                                // value for the key is not a primitive, so display on next line
                                lines.add(EMPTY_STRING);
                            }
                        }
                    }
                }
                // process the child value recursively
                formatStructured(nestedStructured, format, lines, level, instanceIDs);
            }

            if (! lastItem) {
                appendToLastListElement(lines, comma);
            }

            index++;
        } // end  for (Iterator<? extends JsonValue> it  ...
        if (isEmptyOrSingleItem(structured)) {
            // This was a single item List or Map, so display the closing delimiter on the same line without indenting.
            appendToLastListElement(lines, " %s".formatted(RD));
        }
        else {
            level--;
            indentString = format.multiLine() ? spacer(format, level) : sp ;
            lines.add("%s%s".formatted(indentString, RD));
        }
        return lines;
    }

    public String prettyPrintJson(final JsonValue value) {
        return prettyPrintJson(value, FormatFlags.defaults(), new ArrayList<>(), 0);
    }

    public String prettyPrintJson(final JsonValue value, final FormatFlags flags) {
        return prettyPrintJson(value, flags, new ArrayList<>(), 0);
    }

    public String prettyPrintJson(final JsonValue value, final FormatFlags flags, final List<String> lines) {
        return prettyPrintJson(value, flags, lines, 0);
    }

    /**
     * Return the JSON value formatted as a str according to the flags in the format_ argument.
     * </p>
     * Typically, an empty list is passed to this method. Each generated line of formatted output is appended
     * to the `lines` List argument.
     * When this method returns, the `lines` argument will contain each line in the formatted str, or a single new
     * element if format.multiLine is true. These lines are then joined() and returned.
     * @param value
     * @param format
     * @param lines
     * @param indentLevel
     * @return
     */
    public String prettyPrintJson(JsonValue value, FormatFlags format, List<String> lines, int indentLevel) {
        if (lines == null) {
            lines = new ArrayList<>();
        }
        if (lines.isEmpty()) {
            lines.add(EMPTY_STRING); // so format methods will have a new blank starting line for output
        }

        //instanceIDs: keeps track of instance ids to detect circular references
        Map<Integer, JsonValue> instanceIDs = new HashMap<>();
        switch (value) {
            case JsonPrimitive<?>  primitive  -> lines.set(lines.size() - 1, formatScalar(primitive, format));
            case JsonStructured<?> structured -> formatStructured(structured, format, lines, indentLevel, instanceIDs);
        }

        if (format.multiLine()) {
            return String.join(NEW_LINE, lines);
        } else {
            return String.join(EMPTY_STRING, lines);
        }
    }
}
