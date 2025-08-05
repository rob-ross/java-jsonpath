package org.killeroonie.jsonpath;

import org.jetbrains.annotations.NotNull;
import org.killeroonie.jsonpath.exception.JSONPointerIndexException;
import org.killeroonie.jsonpath.exception.JSONPointerKeyException;
import org.killeroonie.jsonpath.exception.JSONPointerResolutionException;
import org.killeroonie.jsonpath.exception.JSONPointerTypeException;

import java.util.Objects;
import java.util.regex.Pattern;

import static org.killeroonie.jsonpath.JSONPointer.UNDEFINED;

/**
 * Constants, and convenience and utility methods for the jsonpath library.
 */
public class JsonPathUtils {

    // The JSON spec allows positive and negative array indices.
    // Java Lists only allow the range 0 - Integer.MAX_VALUE.
    // These ranges are smaller than the JSON Spec allows.
    // We can support List indexes from Integer.MIN_VALUE - Integer.MAX_VALUE
    // to allow for negative indexing. This is just an alternate way of specifying
    // an index relative to the end of the list, as in Python and JavaScript.
    // We normalize this index before we try to get items, so only non-negative indices are actually used.
    public static final long JSON_MAX_INT_INDEX = (1L << 53) - 1;
    public static final long JSON_MIN_INT_INDEX = -(1L << 53) + 1;
    public static final int MAX_INT_INDEX = Integer.MAX_VALUE;
    public static final int MIN_INT_INDEX = Integer.MIN_VALUE;
    public static final String HYPHEN = "-";
    public static final String HASH = "#";

    public static final String KEYS_SELECTOR = "~";
    static final Pattern UNICODE_ESCAPE_PATTERN = Pattern.compile("\\\\u([0-9a-fA-F]{4})");

    /**
     * Resolve JSON Pointer {@code pointer} against {@code data}.
     * @param pointer a string representation of a JSON Pointer or an iterable of JSON Pointer parts.
     * @param data the target JSON "document" or equivalent Java objects.
     * @param defaultValue a default value to return if the pointer can't be resolved against the given data.
     * @param unicodeEscape if `true`, UTF-16 escape sequences will be decoded before parsing the pointer.
     * @param uriDecode if `true`, the pointer will be unescaped using _urllib_ before being parsed.
     * @return the object in @{code data} pointed to by this pointer.
     * @throws JSONPointerIndexException when attempting to access a sequence by an out-of-range index, unless a default is given.
     * @throws JSONPointerKeyException if any mapping object along the path does not contain a specified key, unless a default is given.
     * @throws JSONPointerTypeException : when attempting to resolve a non-index string path part against a sequence, unless a default is given.
     */
    public static Object resolve(
            @NotNull String pointer,
            Object data,
            Object defaultValue,
            boolean unicodeEscape,
            boolean uriDecode) {

        Objects.requireNonNull(pointer, "pointer must not be null");
        return JsonPathUtils.resolveImpl(pointer, data, defaultValue, unicodeEscape, uriDecode);
    }

    // ###########################################################################
    // # OVERLOADS OF RESOLVE
    // ###########################################################################

    /**
     * Overloaded version which takes an Iterable of String or Integer as the first argument.
     * @param parts an Iterable of Strings or Integers
     */
    public static Object resolve(
            @NotNull Iterable<?> parts,
            Object data,
            Object defaultValue,
            boolean unicodeEscape,
            boolean uriDecode) {

        Objects.requireNonNull(parts, "parts must not be null");
        return JsonPathUtils.resolveImpl(parts, data, defaultValue, unicodeEscape, uriDecode);
    }

    public static Object resolve( String pointer, Object data ) {
        return JsonPathUtils.resolve(pointer, data, UNDEFINED, true, false);
    }

    public static Object resolve( String pointer, Object data, Object defaultValue) {
        return JsonPathUtils.resolve(pointer, data, defaultValue, true, false);
    }

    public static Object resolve( String pointer, Object data, boolean unicodeEscape, boolean uriDecode) {
        return JsonPathUtils.resolve(pointer, data, UNDEFINED, true, false);
    }

    public static Object resolve( Iterable<?> parts, Object data ) {
        return JsonPathUtils.resolve(parts, data, UNDEFINED, true, false);
    }

    public static Object resolve( Iterable<?> parts, Object data, Object defaultValue) {
        return JsonPathUtils.resolve(parts, data, defaultValue, true, false);
    }

    public static Object resolve( Iterable<?> parts, Object data, boolean unicodeEscape, boolean uriDecode) {
        return JsonPathUtils.resolve(parts, data, UNDEFINED, true, false);
    }

    /**
     * Canonical {@code resolve} implementation.
     */
    private static Object resolveImpl(@NotNull Object pointerOrParts,
                                      Object data,
                                      Object defaultValue,
                                      boolean unicodeEscape,
                                      boolean uriDecode) {
        try {
            switch (pointerOrParts) {
                case String       s -> { return (new JSONPointer(s, unicodeEscape, uriDecode)).resolve(data); }
                case Iterable<?> it -> { return JSONPointer.fromParts(it, unicodeEscape, uriDecode).resolve(data);}
                default -> throw new JSONPointerTypeException("Expected String or Iterable<?>, got " + pointerOrParts);
            }
        } catch (JSONPointerResolutionException e) {
            if (defaultValue != UNDEFINED) {
                return defaultValue;
            }
            throw e;
        }
    }

}
