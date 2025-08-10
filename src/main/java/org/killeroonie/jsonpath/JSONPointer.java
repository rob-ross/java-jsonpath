package org.killeroonie.jsonpath;

import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.NotNull;
import org.killeroonie.jsonpath.exception.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Identifies a single, specific value in JSON-like data, as per RFC 6901.
 * <p>
 * This class is immutable. Operations that modify a pointer, such as {@code join()},
 * return a new {@code JSONPointer} instance.
 */
@SuppressWarnings("unused")
public class JSONPointer {

    /**
     * A sentinel object used to represent an undefined value, distinct from {@code null}.
     * This is used as a default return value when a pointer cannot be resolved.
     */
    public static final Object UNDEFINED = new Object() {
        @Override
        public String toString() {
            return "<UNDEFINED>";
        }
    };


    private final String pointerString;
    private final List<Object> parts;


    /**
     * Creates a JSONPointer from a JSONPathMatch instance.
     * <p>
     * The parts from a match are considered to be in their final, canonical form,
     * so no further decoding or parsing is performed.
     *
     * @param match The match object containing the path information.
     * @return A new JSONPointer instance pointing to the match's location.
     */
    public static JSONPointer fromMatch(JSONPathMatch match) {
        // The private constructor takes a list of already-parsed parts and
        // handles encoding the string representation. This is the most direct
        // and efficient translation of the Python version's intent.
        return new JSONPointer(match.getParts());
    }
    /**
     * Builds a JSONPointer from an iterable of path parts, with options for decoding.
     * <p>
     * This method processes each part by optionally applying URI and Unicode decoding
     * before constructing the final pointer.
     *
     * @param parts          The keys and indices that make up a JSONPointer.
     * @param uriDecode      If {@code true}, each part will be unescaped using URL decoding rules.
     * @param unicodeEscape  If {@code true}, UTF-16 escape sequences (e.g., \u20ac) within each part will be decoded.
     * @return A new {@code JSONPointer} built from the processed parts.
     */
    @SuppressWarnings("UnnecessaryUnicodeEscape")
    public static JSONPointer fromParts(Iterable<?> parts, boolean uriDecode, boolean unicodeEscape) {
        List<Object> processedParts = new ArrayList<>();
        for (Object part : parts) {
            String sPart = String.valueOf(part);

            if (uriDecode) {
                sPart = URLDecoder.decode(sPart, StandardCharsets.UTF_8);
            }
            if (unicodeEscape) {
                sPart = unicodeEscape(sPart);
            }

            // Todo - re-examine the need to keep parts separate as Strings and Integers
            // Unlike the Python version which keeps all parts as strings, we convert
            // numeric-looking parts back to Integers. This ensures internal consistency
            // with pointers parsed from a string and works correctly with the Java
            // resolve() logic, which expects integer indices for lists.
            //processedParts.add(toPart(sPart));
            processedParts.add(sPart);
        }
        String pointer = encode(processedParts);
        // The private constructor takes a list of already-parsed parts.
        return new JSONPointer(processedParts);
    }

    /**
     * Builds a JSONPointer from an {@link Iterable} of path parts using default decoding options.
     * <p>
     * By default, Unicode escape sequences are decoded, but URI-style percent-encoding is not.
     * This matches the default behavior in the Python implementation.
     *
     * @param parts The keys and indices that make up a JSONPointer.
     * @return A new {@code JSONPointer} built from the parts.
     */
    public static JSONPointer fromParts(Iterable<?> parts) {
        // In Python, the defaults are unicode_escape=True, uri_decode=False
        return fromParts(parts, false, true);
    }

    private static List<Object> parse(String pointer, boolean uriDecode, boolean unicodeEscape) {
        if (pointer == null) {
            return Collections.emptyList();
        }

        String p = pointer;
        if (uriDecode) {
            p = URLDecoder.decode(p, StandardCharsets.UTF_8);
        }
        if (unicodeEscape) {
            p = unicodeEscape(p);
        }

        p = p.stripLeading();
        if (p.isEmpty()) {
            return Collections.emptyList();
        }

        if (!p.startsWith("/")) {
            throw new JSONPointerException("pointer must start with a slash or be the empty string");
        }

        List<Object> parts = new ArrayList<>();
        // Split and skip the initial empty string from the leading "/"
        for (String part : p.substring(1).split("/", -1)) {
            String unescaped = part.replace("~1", "/").replace("~0", "~");
            parts.add(toIndex(unescaped));
        }
        return List.copyOf(parts);
    }

    private static String encode(Iterable<Object> parts) {
        StringBuilder sb = new StringBuilder();
        for (Object part : parts) {
            sb.append("/");
            sb.append(String.valueOf(part).replace("~", "~0").replace("/", "~1"));
        }
        return sb.toString();
    }


    private static String unicodeEscape(String s) {
        if (s == null || !s.contains("\\u")) {
            return s;
        }

        // The Python version also unescapes slashes
        String unescapedSlashes = s.replace("\\/", "/");

        Matcher matcher = JsonPathUtils.UNICODE_ESCAPE_PATTERN.matcher(unescapedSlashes);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            // Parse the hex code and append the corresponding character
            int charCode = Integer.parseInt(matcher.group(1), 16);
            matcher.appendReplacement(sb, Character.toString((char) charCode));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


    /**
     * A simple generic Pair class.
     *
     * @param <L> Type of the parent element.
     * @param <R> Type of the obj element.
     */
    public record Pair<L, R>(L parent, R obj) {}


    /**
     * Creates a JSONPointer from a string representation.
     *
     * @param pointer      A string representation of a JSON Pointer.
     * @param uriDecode    If {@code true}, the pointer will be unescaped using
     *                     URL decoding rules before being parsed.
     */
    public JSONPointer(String pointer, boolean unicodeEscape, boolean uriDecode) {
        this(pointer, null, unicodeEscape, uriDecode);
    }

    public JSONPointer(String pointer) {
        this(pointer, null, true, false);
    }

    public JSONPointer(String pointer, List<Object> parts) {
        this(pointer, parts, true, false);
    }

    private JSONPointer(@NotNull List<Object> parts) {
        this(encode(parts), parts, true, false);
    }

    public JSONPointer(String pointer, List<Object> parts, boolean unicodeEscape, boolean uriDecode) {
        this.parts =
                    parts != null ?
                        List.copyOf(parts) :
                        parse(pointer, unicodeEscape, uriDecode);
        this.pointerString = encode(this.parts);
    }

    public String getPointerString() {
        return pointerString;
    }

    public List<Object> getParts() {
        return parts;
    }

    /**
     * Checks if this pointer points to a child of another pointer.
     *
     * @param other The potential parent pointer.
     * @return {@code true} if this pointer's path starts with all the parts of the
     *         {@code other} pointer and is longer.
     */
    public boolean isRelativeTo(JSONPointer other) {
        if (other == null || other.getParts().size() >= this.getParts().size()) {
            return false;
        }
        return this.getParts().subList(0, other.getParts().size()).equals(other.getParts());
    }

    /**
     * Resolves this pointer against the given data.
     *
     * @param data The target JSON "document" or equivalent Java objects (Map, List, etc.).
     *             If a String or InputStream is provided, it will be parsed as JSON.
     * @return The object in {@code data} pointed to by this pointer.
     * @throws JSONPointerResolutionException if the pointer cannot be resolved.
     */
    public Object resolve(Object data) throws JSONPointerResolutionException {
        return resolveImpl(JsonLoader.load(data));
    }

    /**
     * Resolves this pointer against the given data, returning a default value on failure.
     *
     * @param data         The target JSON "document" or equivalent Java objects.
     * @param defaultValue A default value to return if the pointer can't be resolved.
     * @return The object in {@code data} pointed to by this pointer, or {@code defaultValue}.
     */
    public Object resolve(Object data, Object defaultValue) {
        try {
            return resolveImpl(JsonLoader.load(data));
        } catch (JSONPointerResolutionException e) {
            return defaultValue;
        }
    }

    private Object resolveImpl(Object data) {
        Object current = data;
        for (Object part : parts) {
            current = getItem(current, part);
        }
        return current;
    }

    /**
     * Resolves this pointer against data, returning the object and its immediate parent.
     *
     * @param data The target JSON "document" or equivalent Java objects.
     * @return A {@code Pair} containing the parent and the resolved object. The parent
     *         can be {@code null} if the pointer targets the root. The object can be
     *         {@code UNDEFINED} if the final part of the pointer does not exist.
     * @throws JSONPointerResolutionException if an intermediate part of the path does not exist.
     */
    public Pair<Object, Object> resolveParent(Object data) {
        if (parts.isEmpty()) {
            return new Pair<>(null, resolve(data));
        }
        Object parent = JsonLoader.load(data);
        for (int i = 0; i < parts.size() - 1; i++) {
            parent = getItem(parent, parts.get(i));
        }

        try {
            Object child = getItem(parent, parts.getLast());
            return new Pair<>(parent, child);
        } catch (JSONPointerIndexException | JSONPointerKeyException e) {
            return new Pair<>(parent, UNDEFINED);
        }
    }

    /**
     * Checks if this pointer can be resolved against the given data.
     *
     * @param data The target JSON "document" or equivalent Java objects.
     * @return {@code true} if the path exists, {@code false} otherwise.
     */
    public boolean exists(Object data) {
        return resolve(data, UNDEFINED) != UNDEFINED;
    }

    /**
     * Returns the parent of this pointer.
     *
     * @return A new {@code JSONPointer} pointing to the parent element. If this
     *         pointer is already at the root, it returns itself.
     */
    public JSONPointer parent() {
        if (parts.isEmpty()) {
            return this;
        }
        return new JSONPointer(parts.subList(0, parts.size() - 1));
    }

    /**
     * Join this pointer with {@code other}.

     * @param other a JSON Pointer string, possibly without a leading slash. If {@code other} does have a leading slash,
     *              the previous pointer is ignored and a new JSONPath is returned from {@code other}.
     * @return this pointer joined with {@code other}, or {@code other} if it begins with a leading slash.
     */
    JSONPointer joinImpl(String other) {
        String escaped = unicodeEscape(other).trim();
        if (escaped.startsWith("/")) {
            return new JSONPointer(escaped, false, false);
        }
        var parts = new ArrayList<>(this.parts);
        String[] otherParts = escaped.split("/");
        for (String s :  otherParts) {
            parts.add(s.replace("~1", "/").replace("~0", "~"));
        }
        return new JSONPointer(encode(parts), parts, false, false);
    }


    /**
     * Join this pointer with {@code parts}
     *
     * Each part is expected to be a JSON Pointer string, possibly without a
     * leading slash. If a part does have a leading slash, the previous
     * pointer is ignored, and a new {@code JSONPointer} is created, and processing of
     * remaining parts continues.
     *
     * @param parts One or more strings representing pointer segments. If any segment starts with
     *              "/", it replaces this pointer entirely, and subsequent segments are ignored.
     * @return a new {@code JSONPointer} representing the combined path.
     */
    public JSONPointer join(String... parts) {
        if (parts == null || parts.length == 0) {
            return this;
        }
        JSONPointer pointer = this;
        for (var part : parts){
            pointer = pointer.joinImpl(part);
        }
        return pointer;
    }

    /**
     * Applies a relative pointer to this pointer.
     *
     * @param rel The relative pointer to apply.
     * @return A new {@code JSONPointer} instance.
     */
    public JSONPointer to(RelativeJSONPointer rel) {
//        return rel.to(this);
        return toImpl( rel, true, false);
    }

    public JSONPointer to(String rel) {
        return toImpl( rel, false, false);
    }

    private JSONPointer toImpl(Object relPointerOrString, boolean unicodeEscape,  boolean uriDecode) {
        RelativeJSONPointer relative_pointer;
        switch (relPointerOrString) {
            case RelativeJSONPointer rp -> relative_pointer = rp;
            case String s -> relative_pointer = new RelativeJSONPointer(s, unicodeEscape, uriDecode);
            default -> throw new IllegalArgumentException("Expected relative pointer or String, got " + relPointerOrString);
        }
        return relative_pointer.to(this);
    }

    /**
     * Rejects non-zero ints that start with a zero.
     * @param s String that may be an int, with or without a leading zero.
     * @return the int value of the String if convertable to an int. If not convertible to int, returns the argument.
     */
    private static Object toIndex(String s) {
        if (s.length() > 1 && s.startsWith("0")) {
            return s;
        }
        try {
            long index = Long.parseLong(s);
            if (index < JsonPathUtils.MIN_INT_INDEX || index > JsonPathUtils.MAX_INT_INDEX) {
                throw new JSONPointerIndexException("Index out of range: " + index);
            }
            return (int)index;
        } catch (NumberFormatException e){
            return s;
        }
    }

    private Object getItem(Object obj, Object keyOrIndex) {
        if ( !(keyOrIndex instanceof String || keyOrIndex instanceof Integer) ) {
            throw new JSONPointerTypeException("Argument to getItem() must be String or Integer, got '%s'".formatted(keyOrIndex));
        }

        // Handle hash references on the key/index
        if (keyOrIndex instanceof String s && s.startsWith(JsonPathUtils.HASH)) {
            String hashRemoved = s.substring(1);
            if (obj instanceof Map<?,?> m) {
                if (m.containsKey(s))
                {
                    // special case where the map key contains a "#" prefix.
                    return getFromMap(m, s);
                }
                if(m.containsKey(hashRemoved)){
                    return hashRemoved;
                } else {
                    throw new JSONPointerKeyException("'%s' does not exist at: %s".formatted(hashRemoved, obj));
                }
            } else if (obj instanceof List<?> l) {
                Object maybeInteger = toIndex(hashRemoved);
                if (maybeInteger instanceof String) {
                    throw new JSONPointerTypeException("List indices must be integers, got '%s'".formatted(keyOrIndex));
                }
                // toIndex() returns either a String or Integer. We know it's not a String here, so it must be Integer
                int index = (int) maybeInteger;
                if ( index >= l.size() ) {
                    throw new JSONPointerIndexException(String.format("Index %d out of range for List of size %d", index, l.size()));
                } else {
                    return index;
                }
            } else {
                // Obj is not a Map nor a List, so obj is not subscriptable.
                throw new JSONPointerTypeException("'%s': '%s' object is not subscriptable".formatted(keyOrIndex, obj.getClass().getSimpleName()));
            }
//            return toIndex(s.substring(1));
        }

        if (JsonPathUtils.HYPHEN.equals(keyOrIndex)) {
            // "-" is a valid index when appending to a JSON array
            // with JSON Patch, but not when resolving a JSON Pointer.
            throw new JSONPointerIndexException("index out of range: '-'");
        }

        if (obj instanceof List) {
            int index;
            if (keyOrIndex instanceof String s) {
                Object maybeInteger = toIndex(s);
                if (maybeInteger instanceof String) {
                    throw new JSONPointerTypeException("List indices must be integers, got '%s'".formatted(keyOrIndex));
                }
                index = (int) maybeInteger;
            } else {
                index = (int) keyOrIndex;
            }
            return getFromList((List<?>) obj, index);
        }

        if (obj instanceof Map) {
            return getFromMap((Map<?, ?>) obj, keyOrIndex);
        }
        if (obj instanceof JsonNode && ((JsonNode) obj).isObject()) {
            return getFromJsonNodeObject((JsonNode) obj, keyOrIndex);
        }
        if (obj instanceof JsonNode && ((JsonNode) obj).isArray()) {
            return getFromJsonNodeArray((JsonNode) obj, keyOrIndex);
        }
        throw new JSONPointerTypeException(String.format(
                "can't resolve key '%s' on object of type %s", keyOrIndex, obj.getClass().getSimpleName()
        ));
    }

    private Object getFromList(List<?> list, int index) {
        int listIndex = index;
        //normalize negative index value
        if (listIndex < 0) {
            listIndex = list.size() + listIndex;
        }
        if (listIndex < 0 || listIndex >= list.size()) {
            throw new JSONPointerIndexException(String.format("Index %s out of range for List of size %d", index, list.size()));
        }
        return list.get(listIndex);
    }

    private Object getFromMap(Map<?, ?> map, Object key) {
        if ( key instanceof String s && s.startsWith(JsonPathUtils.KEYS_SELECTOR) ) {
            String k = s.substring(1);
            if ( map.containsKey( k )) {
                return map.get(k);
            }
        }
        if (map.containsKey(key)) {
            return map.get(key);
        }
        // Python version tries int as a string key
        if (key instanceof Integer && map.containsKey(String.valueOf(key))) {
            return map.get(String.valueOf(key));
        }
        throw new JSONPointerKeyException(String.format("key not found: '%s'", key));
    }

    // Overloads for Jackson JsonNode
    private Object getFromJsonNodeArray(JsonNode node, Object key) {
        if (!(key instanceof Integer)) {
            throw new JSONPointerTypeException(String.format("array index must be an integer, not '%s'", key));
        }
        int index = (Integer) key;
        if (index < 0 || index >= node.size()) {
            throw new JSONPointerIndexException(String.format("index out of range: %d", index));
        }
        return JsonLoader.unpack(node.get(index));
    }

    private Object getFromJsonNodeObject(JsonNode node, Object key) {
        String sKey = String.valueOf(key);
        if (node.has(sKey)) {
            return JsonLoader.unpack(node.get(sKey));
        }
        throw new JSONPointerKeyException(String.format("key not found: '%s'", key));
    }

    @Override
    public String toString() {
        return pointerString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JSONPointer that = (JSONPointer) o;
        return parts.equals(that.parts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parts);
    }
}