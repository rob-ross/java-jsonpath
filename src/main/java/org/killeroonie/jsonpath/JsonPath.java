package org.killeroonie.jsonpath;

import org.jetbrains.annotations.NotNull;
import org.killeroonie.jsonpath.exception.JSONPointerIndexException;
import org.killeroonie.jsonpath.exception.JSONPointerKeyException;
import org.killeroonie.jsonpath.exception.JSONPointerResolutionException;
import org.killeroonie.jsonpath.exception.JSONPointerTypeException;

import java.util.Objects;

import static org.killeroonie.jsonpath.JSONPointer.UNDEFINED;

/**
 * Convenience and utility methods for the jsonpath library.
 */
public class JsonPath {

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
        return JsonPath.resolveImpl(pointer, data, defaultValue, unicodeEscape, uriDecode);
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
        return JsonPath.resolveImpl(parts, data, defaultValue, unicodeEscape, uriDecode);
    }

    public static Object resolve( String pointer, Object data ) {
        return JsonPath.resolve(pointer, data, UNDEFINED, true, false);
    }

    public static Object resolve( String pointer, Object data, Object defaultValue) {
        return JsonPath.resolve(pointer, data, defaultValue, true, false);
    }

    public static Object resolve( String pointer, Object data, boolean unicodeEscape, boolean uriDecode) {
        return JsonPath.resolve(pointer, data, UNDEFINED, true, false);
    }

    public static Object resolve( Iterable<?> parts, Object data ) {
        return JsonPath.resolve(parts, data, UNDEFINED, true, false);
    }

    public static Object resolve( Iterable<?> parts, Object data, Object defaultValue) {
        return JsonPath.resolve(parts, data, defaultValue, true, false);
    }

    public static Object resolve( Iterable<?> parts, Object data, boolean unicodeEscape, boolean uriDecode) {
        return JsonPath.resolve(parts, data, UNDEFINED, true, false);
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
