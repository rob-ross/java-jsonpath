package org.killeroonie.json.display;

import org.jetbrains.annotations.NotNull;
import org.killeroonie.json.JsonTypes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class JsonPrettyPrinter {

    private static final Logger logger = LoggerFactory.getLogger(JsonPrettyPrinter.class);

    static final String SPACE = " ";
    static final String COMMA = ",";
    static final String COLON = ":";
    static final String EMPTY_STRING = "";
    static final String OPEN_BRACE    = "{";
    static final String CLOSE_BRACE   = "}";
    static final String OPEN_BRACKET  = "[";
    static final String CLOSE_BRACKET = "]";
    static final String NEW_LINE = "\n";

    private String spacer(FormatFlags format, int level) {
        if (format.singleLine())
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
            case JsonPrimitive<?> p -> true;
            case JsonStructured<?> s when s.isEmpty() -> true;
            case JsonArray a when a.size() == 1 -> isEmptyOrSingleItem(a.getFirst());
            case JsonObject o when o.size() == 1 -> isEmptyOrSingleItem(o.members().values().iterator().next());
            // This default handles cases like a JsonStructured with size > 1
            default -> false;
        };

//        if (value instanceof JsonPrimitive) {
//            return true;
//        }
//        if (value instanceof JsonStructured<?> structured && structured.isEmpty()) {
//            return true;
//        }
//        if (value instanceof JsonStructured<?> structured && structured.size() > 1) {
//            return false;
//        }
//        if (value instanceof JsonArray(List<JsonValue> elements)  && elements.size() == 1) {
//            return isEmptyOrSingleItem(elements.getFirst());
//        }
//        if (value instanceof JsonObject(Map<JsonString, JsonValue> members) && members.size() == 1) {
//            return isEmptyOrSingleItem(members.values().iterator().next());
//        }
//        return false;
    }

    private String formatPrimitive(JsonPrimitive<?> primitive, FormatFlags format) {

        if (primitive == null){
            return "null";
        }
        if (primitive instanceof JsonBoolean(Boolean value)) {
            return value.toString();
        }
        char quoteChar = format.singleQuotes() ? '\'': '"';
        /*
        In Python, objects have two string representation methods, str() and repr(). Java doesn't have this distinction.
        In Python, repr will put single quotes around the string; unless there are embedded single quotes,
        then it uses double quotes. It also escapes any literal backslash characters.  format.useRepr() may be unneeded
        in this Java implementation. For now, we'll ignore the flag.
        if (format.useRepr()){
            ...
        }

        */
        if (primitive instanceof JsonString string && format.quoteStrings()) {
            return String.format("%c%s%c",quoteChar, string,  quoteChar  );
        }
        else {
            return primitive.toString();
        }
    }
    
    private int lastListIndex(final List<?> list) {
        int index = 0;
        if (list != null && !list.isEmpty()) {
            index = list.size() - 1;
        }
        return index;
    }

    /**
     * Return a unique identifier for the Object argument.
     * @param object a Java Object instance
     * @return object identifier as an int
     */
    private int id(Object object) {
        return System.identityHashCode(object);    
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
            case JsonArray a -> {
                javaTypeName = "List";
                jsonTypeName = "Array";
                LD = "[";
                RD = "]";
            }
            case JsonObject o -> {
                javaTypeName = "Map";
                jsonTypeName = "Object";
                LD = "{";
                RD = "}";
            }
            default -> { throw new IllegalStateException("Unreachable"); }
        }

        String indentString;
        // EMPTY_STRING is an interned constant. The empty string is always added to `lines` via EMPTY_STRING,
        // so the identity comparison is correct and at O(1), more efficient than equals() at O(N).
        //noinspection StringEquality
        if ( lines.getLast() != EMPTY_STRING ) {
            // the current line already has text, so indent is relative to the end of that text
            indentString = SPACE.repeat(format.indent() - 1 );
        }
        else if (lines.size() == 1 || level == 0) {
            indentString = EMPTY_STRING;
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
            appendToLastListElement(lines, "%s%s %s".formatted( LD, indentString, RD ));
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
                String vf =  formatPrimitive(primitive, format);
                switch (structured) {
                    case JsonArray  _ -> {
                        appendToLastListElement(lines, "%s[ %s ]".formatted(indentString, vf ));
                    }
                    case JsonObject _ -> {
                        String kf = formatPrimitive(key, format);
                        appendToLastListElement(lines, "%s{ %s: %s }".formatted( indentString, kf, vf ));
                    }
                }
                return lines;
            }
        }
        String comma = format.omitCommas() ? EMPTY_STRING : COMMA;
        String sp    = format.singleLine() ? SPACE : EMPTY_STRING;
        appendToLastListElement(lines, "%s%s".formatted(indentString, LD));  // start of the Map/List text: '{' or '['
        //same
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
                kf =  formatPrimitive(key, format);
            }
            if ( value instanceof JsonPrimitive<?> primitive) {
                lines.add(EMPTY_STRING);
                String vf =  formatPrimitive(primitive, format);
                String template = switch (structured) {
                    case JsonObject o -> "%s%s".formatted(indentString, vf);
                    case JsonArray  a -> "%s%s: %s".formatted(indentString, kf, vf);
                };
                appendToLastListElement(lines, template);
            }
            else if (structured instanceof JsonArray) {
                if (! firstItem) {
                    // If `structured` is a List, and we are starting a new List or Map as the first element
                    // of the `structured` List, then the open brackets/braces can go on the same line.
                    // We don't add a new line if `value` is the first List element.
                    lines.add(EMPTY_STRING);
                }
                // process the child value recursively
                formatStructured((JsonStructured<?>) value, format, lines, level, instanceIDs);
            }
            else if (structured instanceof JsonObject) {
                lines.add(EMPTY_STRING);
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
            indentString = format.singleLine() ? sp : spacer(format, level);
            appendToLastListElement(lines, "%s%s".formatted(indentString, RD));
        }
        return lines;
    }


    /**
     * 
     * @param object
     * @param format
     * @param lines
     * @param level
     * @param instanceIDs keeps track of instance ids to detect circular references
     * @return
     */
    @SuppressWarnings("UnusedReturnValue")
    List<String> ppObject(final JsonObject object,
                           final FormatFlags format, 
                           final List<String> lines, 
                           int level,
                           Map<Integer, JsonValue> instanceIDs) {

        Objects.requireNonNull(object, "`object` cannot be null");
        if (lines.isEmpty()) {
            lines.add(EMPTY_STRING);
        }
        if (instanceIDs == null) {
            // keeps track of instance ids to detect circular references
            instanceIDs = new HashMap<Integer, JsonValue>();
        }
        String indentString;
        if ( lines.getLast() != EMPTY_STRING ) {
            // the current line already has text, so indent is relative to the end of that text
            indentString = SPACE.repeat(format.indent() - 1 );
        }
        else if (lines.size() == 1 || level == 0) {
            indentString = EMPTY_STRING;
        }
        else {
            indentString = spacer(format, level);
        }

        /*
         * METHOD DIFFERENCE
         */
        var map = object.members();
        int mapID = id(map);

        if ( instanceIDs.containsKey(mapID) ) {
            // we have seen this Map instance previously, cycle detected
            logger.warn("Cycle detected at object Map: {}", map);
            appendToLastListElement(lines, "%s{...}".formatted(indentString));
            return lines;
        }
        else {
            // save for future cycle detection
            instanceIDs.put(mapID, object);
        }
        if (map.isEmpty()) {
            appendToLastListElement(lines, "%s{ }".formatted( indentString ));
            return lines;
        }
        // * * * The above differences are messages and delimiter characters { vs [
        //Differences below in navigating Map vs List.
        if (map.size() == 1) {
            var entry = map.entrySet().iterator().next();
            var k = entry.getKey();
            var v = entry.getValue();
            if (v instanceof JsonPrimitive<?> primitive) {
                String kf = formatPrimitive(k, format);
                String vf = formatPrimitive(primitive, format);
                appendToLastListElement(lines, "%s{ %s: %s }".formatted( indentString, kf, vf ));
                return lines;
            }
        }
        // these lines the same
        String comma = format.omitCommas() ? EMPTY_STRING : COMMA;
        String sp    = format.singleLine() ? SPACE : EMPTY_STRING;
        //differs by delimiter
        appendToLastListElement(lines, "%s{".formatted(indentString));  // start of the Map text: '{'
        //same
        level++;
        indentString = spacer(format, level);

        int index = 0;
        //Outer loop differs by Map vs List iterator
        for (Map.Entry<JsonString, JsonValue> entry : map.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();

            // deal with commas
            boolean firstItem = index == 0;
            boolean lastItem  = index == map.size() - 1;   // no comma after the last member

            String kf = formatPrimitive(key, format);  // formatted key

            if ( value instanceof JsonPrimitive<?> primitive) {
                lines.add(EMPTY_STRING);
                String vf =  formatPrimitive(primitive, format);
                // The value for the key is a primitive, so display key and value on the same line.
                appendToLastListElement(lines, "%s%s: %s".formatted(indentString, kf, vf));
            }
            else if (value instanceof JsonArray array) {
                lines.add(EMPTY_STRING);
                appendToLastListElement(lines, "%s%s:".formatted(indentString, kf));
                // special case is where the value is either an empty list or a list with one primitive element.
                // we can display this value on the same line as the key name.
                if (array.size() > 1) {
                    lines.add(EMPTY_STRING);
                }
                else if (array.size() == 1) {
                    // if there is only one single element or key/value pair, we print it on the same line.
                    if (! isEmptyOrSingleItem(value)) {
                        lines.add(EMPTY_STRING);
                    }
                }
                ppArray(array, format, lines, level, instanceIDs);
            }

            else if (value instanceof JsonObject obj) {
                lines.add(EMPTY_STRING);
                appendToLastListElement(lines, "%s%s:".formatted(indentString, kf));
                // Special case is where the value is either an empty Map or a Map with one key with a primitive value.
                // We can display the nested Map on the same line as the key name of the parent Map.
                if (obj.size() > 1) {
                    lines.add(EMPTY_STRING);
                }
                else if (obj.size() == 1) {
                    var nv = obj.members().values().iterator().next();
                    if (! (nv instanceof  JsonPrimitive<?> primitive)) {
                        lines.add(EMPTY_STRING);
                    }
                }
                ppObject(obj, format, lines, level, instanceIDs);
            }

            if (! lastItem) {
                appendToLastListElement(lines, comma);
            }

            index++;
        }
        if (isEmptyOrSingleItem(object)) {
            // this was a single item dict, so display closing brace on same line
            appendToLastListElement(lines, " }");
        }
        else {
            level--;
            indentString = format.singleLine() ? sp : spacer(format, level);
            appendToLastListElement(lines, "%s}".formatted(indentString));
        }
        return lines;
    }

    @SuppressWarnings("UnusedReturnValue")
    List<String> ppArray(final JsonArray array,
                         final FormatFlags format,
                         final List<String> lines,
                         int level,
                         Map<Integer, JsonValue> instanceIDs) {

        Objects.requireNonNull(array, "Array cannot be null");
        if (lines.isEmpty()) {
            lines.add(EMPTY_STRING);
        }
        if (instanceIDs == null) {
            // keeps track of instance ids to detect circular references
            instanceIDs = new HashMap<Integer, JsonValue>();
        }
        String indentString;
        if ( lines.getLast() != EMPTY_STRING ) {
            // the current line already has text, so indent is relative to the end of that text
            indentString = SPACE.repeat(format.indent() - 1 );
        }
        else if (lines.size() == 1 || level == 0) {
            indentString = EMPTY_STRING;
        }
        else {
            indentString = spacer(format, level);
        }

        /*
         * METHOD DIFFERENCE
         */
        var list = array.elements();
        int listID = id(list);

        if ( instanceIDs.containsKey(listID) ) {
            // we have seen this List instance previously, cycle detected
            logger.warn("Cycle detected at array List: {}", list);
            appendToLastListElement(lines, "%s[...]".formatted(indentString));
            return lines;
        }
        else {
            // save for future cycle detection
            instanceIDs.put(listID, array);
        }
        if (list.isEmpty()) {
            appendToLastListElement(lines, "%s[ ]".formatted( indentString ));
            return lines;
        }
        // * * * The above differences are messages and delimiter characters { vs [
        //Differences below in navigating Map vs List.
        if (list.size() == 1) {
            var v =  list.getFirst();
            if (v instanceof JsonPrimitive<?> primitive) {
                var s =  formatPrimitive(primitive, format);
                // "{indent_str}{OPEN_BRACKET}{SPACE}{s}{SPACE}{CLOSE_BRACKET}"
                appendToLastListElement(lines, "%s[ %s ]".formatted(indentString, s ));
                return lines;
            }
        }
        // these lines the same
        String comma = format.omitCommas() ? EMPTY_STRING : COMMA;
        String sp    = format.singleLine() ? SPACE : EMPTY_STRING;
        //differs by delimiter
        appendToLastListElement(lines, "%s[".formatted(indentString));  // start of the List text: '['
        // same
        level++;
        indentString = spacer(format, level);

        int index = 0;
        //Outer loop differs by Map vs. List iterator
        for (JsonValue value : list) {
            // deal with commas
            boolean firstItem = index == 0;
            boolean lastItem  = index == lastListIndex(list);   // no comma after the last member

            if ( value instanceof JsonPrimitive<?> primitive) {
                lines.add(EMPTY_STRING);
                String vf =  formatPrimitive(primitive, format);
                appendToLastListElement(lines, "%s%s".formatted(indentString, vf));
            }
            else if (value instanceof JsonStructured) {
                if (! firstItem) {
                    // if this is a new Map or List starting inside the list,
                    // open brackets/braces can go on the same line
                    lines.add(EMPTY_STRING);
                }
                switch (value) {
                    case JsonObject o -> ppObject(o, format, lines, level, instanceIDs);
                    case JsonArray  a -> ppArray(a,  format, lines, level, instanceIDs);
                    default -> throw new IllegalArgumentException("Unsupported JsonStructured type: " + value.getClass().getName());
                }
            }

            if (! lastItem) {
                appendToLastListElement(lines, comma);
            }

            index++;
        } // end for (JsonValue value : list)
        if (isEmptyOrSingleItem(array)) {
            // this was a single item List, so display closing bracket on same line
            appendToLastListElement(lines, " ]");
        }
        else {
            level--;
            indentString = format.singleLine() ? sp : spacer(format, level);
            appendToLastListElement(lines, "%s]".formatted(indentString));
        }
        return lines;
    }

    /**
     * Return the JSON value formatted as a str according to the flags in the format_ argument.
     * </p>
     * Typically, an empty list is passed to this method. Each generated line of formatted output is appended
     * to the `lines` List argument.
     * When this method returns, the `lines` argument will contain each line in the formatted str, or a single new
     * element if format.singleLine is true. These lines are then joined() and returned.
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
            case JsonPrimitive<?>  primitive  -> lines.set(lines.size() - 1, formatPrimitive(primitive, format));
            case JsonStructured<?> structured -> formatStructured(structured, format, lines, indentLevel, instanceIDs);
        }

        if (format.singleLine()) {
            return String.join(EMPTY_STRING, lines);
        }
        else {
            return String.join(NEW_LINE, lines);
        }
    }

}
