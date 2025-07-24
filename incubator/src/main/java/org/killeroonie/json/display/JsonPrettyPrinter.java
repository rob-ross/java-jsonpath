package org.killeroonie.json.display;

import org.killeroonie.json.JsonTypes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class JsonPrettyPrinter {

    private static final Logger logger = LoggerFactory.getLogger(JsonPrettyPrinter.class);

    static final String SPACE = " ";
    static final String COMMA = ",";
    static final String EMPTY_STRING = "";
    static final String OPEN_BRACE    = "{";
    static final String CLOSE_BRACE   = "}";
    static final String OPEN_BRACKET  = "[";
    static final String CLOSE_BRACKET = "]";

    private String spacer(FormatFlags format, int level) {
        if (format.singleLine())
            return SPACE;
        return SPACE.repeat(format.indent() * level );
    }

    /**
     *     Recurse the List or Map and return true if every nested element is either empty or contains
     *     exactly one scalar list element or one key/value pair where the value is a single scalar value.
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
     * @return true if the `value` argument is a JSON scalar (primitive) value, or a one-element List or Map whose
     * value is a scalar value. Otherwise, return false.
     */
    private boolean isEmptyOrSingleItem(JsonValue value) {
        if (value instanceof JsonPrimitive) {
            return true;
        }
        if (value instanceof JsonStructured structured && structured.isEmpty()) {
            return true;
        }
        if (value instanceof JsonStructured structured && structured.size() > 1) {
            return false;
        }
        if (value instanceof JsonArray(List<JsonValue> elements)  && elements.size() == 1) {
            return isEmptyOrSingleItem(elements.getFirst());
        }
        if (value instanceof JsonObject(Map<JsonString, JsonValue> members) && members.size() == 1) {
            return isEmptyOrSingleItem(members.values().iterator().next());
        }
        return false;
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
        
        if (object == null || object.members() == null) {
            throw new  IllegalArgumentException("`object` argument cannot be null");
        }
        if (lines.isEmpty()) {
            lines.add("");
        }
        if (instanceIDs == null) {
            // keeps track of instance ids to detect circular references
            instanceIDs = new HashMap<Integer, JsonValue>();
        }
        String indentString;
        if (EMPTY_STRING != lines.getLast()) {
            // the current line already has text, so indent is relative to the end of that text
            indentString = SPACE.repeat(format.indent() - 1 );
        }
        else if (lines.size() == 1 || level == 0) {
            indentString = EMPTY_STRING;
        }
        else {
            indentString = spacer(format, level);
        }
        
        var map = object.members();
        int mapID = id(map);
        if ( instanceIDs.containsKey(mapID) ) {
            // we have seen this list instance previously, cycle detected
            logger.warn("Cycle detected at object map: {}", map);
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
        String comma = format.omitCommas() ? EMPTY_STRING : COMMA;
        String sp    = format.singleLine() ? SPACE : EMPTY_STRING;
        appendToLastListElement(lines, "%s{".formatted(indentString));  // start of the map text: '{'
        level++;
        indentString = spacer(format, level);

        int memberIndex = 0;
        for (Map.Entry<JsonString, JsonValue> entry : map.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();

            // deal with commas
            boolean firstItem = memberIndex == 0;
            boolean lastItem  = memberIndex == lastListIndex(lines);

            String kf = formatPrimitive(key, format);  // formatted key

            if ( value instanceof JsonPrimitive<?> primitive) {
                lines.add(EMPTY_STRING);
                String vf =  formatPrimitive(primitive, format);
                appendToLastListElement(lines, "%s%s: %s".formatted(indentString, kf, vf));

            }
            else if (value instanceof JsonArray array) {
                lines.add(EMPTY_STRING);
                appendToLastListElement(lines, "%s%s:".formatted(indentString, kf));
                // special case is where the value is either an empty list or a list with one scalar element.
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
            else //noinspection DeconstructionCanBeUsed
                if (value instanceof JsonObject obj) {
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

            memberIndex++;
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
    public String prettyPrint(JsonValue value, FormatFlags format, List<String> lines, int indentLevel) {
        if (lines == null) {
            lines = new ArrayList<>();
        }
        if (lines.isEmpty()) {
            lines.add(EMPTY_STRING); // so format methods will have a new blank starting line for output
        }

        // instanceIDs are generated by System.identityHashCode(object).
        //instanceIDs: keeps track of instance ids to detect circular references
        Map<Integer, JsonValue> instanceIDs = new HashMap<>();
        switch (value) {
            case JsonPrimitive<?> primitive -> lines.set(lines.size() - 1, formatPrimitive(primitive, format));
            case JsonArray  array  -> ppArray(array, format, lines, indentLevel, instanceIDs);
            case JsonObject object -> ppObject(object,  format, lines, indentLevel, instanceIDs);
            default -> throw new IllegalArgumentException("Unsupported JsonValue type: " + value.getClass().getName());
        }

        if (format.singleLine()) {
            return String.join("", lines);
        }
        else {
            return String.join("\n", lines);
        }
    }

}
