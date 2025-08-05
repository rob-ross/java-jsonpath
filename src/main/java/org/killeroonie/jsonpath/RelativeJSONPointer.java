package org.killeroonie.jsonpath;

import org.killeroonie.jsonpath.exception.JSONPointerTypeException;
import org.killeroonie.jsonpath.exception.RelativeJSONPointerIndexException;
import org.killeroonie.jsonpath.exception.RelativeJSONPointerSyntaxException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.killeroonie.jsonpath.JSONPointer.HASH;

/**
 * A Relative JSON Pointer, as per the IETF draft.
 * See <a href="https://www.ietf.org/id/draft-hha-relative-json-pointer-00.html">here</a>
 */
public class RelativeJSONPointer {

    private static final Pattern RE_RELATIVE_POINTER =
            Pattern.compile(
                    "^(?<ORIGIN>\\d+)(?:(?<SIGN>[+\\-])(?<INDEX>\\d+))?(?<POINTER>.*)$",
                    Pattern.DOTALL
            );

    private final int origin;
    private final int indexOffset;
    private final Object pointer; // Can be JSONPointer or "#"

    /**
     * Creates a RelativeJSONPointer from its string representation.
     *
     * @param rel The string representation of the relative pointer.
     */
    public RelativeJSONPointer(String rel) {
        this(rel, true, false);
    }

    public RelativeJSONPointer(String rel, boolean unicodeEscape, boolean uriDecode) {
        if (rel == null || rel.stripLeading().isEmpty()) {
            throw new RelativeJSONPointerSyntaxException("relative pointer cannot be empty", rel);
        }

        Matcher matcher = RE_RELATIVE_POINTER.matcher(rel.trim());
        if (!matcher.matches()) {
            throw new RelativeJSONPointerSyntaxException("invalid relative pointer syntax", rel);
        }

        this.origin = parseZeroOrPositive(matcher.group("ORIGIN"), rel);

        int offset = 0;
        if (matcher.group("SIGN") != null) {
            offset = parseZeroOrPositive(matcher.group("INDEX"), rel);
            if (offset == 0) {
                throw new RelativeJSONPointerSyntaxException("index offset can't be zero", rel);
            }
            if ("-".equals(matcher.group("SIGN"))) {
                offset = -offset;
            }
        }
        this.indexOffset = offset;

        String pointerStr = matcher.group("POINTER").trim();
        if (HASH.equals(pointerStr)) {
            this.pointer = "#";
        } else {
            this.pointer = new JSONPointer(pointerStr);
        }
    }

    public int getOrigin() {
        return origin;
    }

    public int getIndexOffset() {
        return indexOffset;
    }

    public Object getPointer() {
        return pointer;
    }

    @Override
    public String toString() {
        String indexStr = "";
        if (indexOffset != 0) {
            indexStr = (indexOffset > 0) ? "+" + indexOffset : String.valueOf(indexOffset);
        }
        return String.format("%d%s%s", origin, indexStr, pointer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelativeJSONPointer that = (RelativeJSONPointer) o;
        return origin == that.origin && indexOffset == that.indexOffset && pointer.equals(that.pointer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, indexOffset, pointer);
    }

    private int parseZeroOrPositive(String s, String rel) {
        if (s.length() > 1 && s.startsWith("0")) {
            throw new RelativeJSONPointerSyntaxException("unexpected leading zero", rel);
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException _) {
            throw new RelativeJSONPointerSyntaxException("expected positive int or zero", rel);
        }
    }

    private boolean isIntLike(Object value) {
        if (value instanceof Integer) {
            return true;
        }
        try {
            int i =  Integer.parseInt(value.toString());
        } catch (NumberFormatException _) {
            return false;
        }
        return true;
    }

    /**
     * Applies this relative pointer to an existing absolute pointer.
     *
     * @param basePointer The absolute pointer to which this relative pointer is applied.
     * @return A new, absolute {@code JSONPointer}.
     */
    public JSONPointer to_previous(JSONPointer basePointer) {
        List<Object> baseParts = basePointer.getParts();

        if (this.origin > baseParts.size()) {
            throw new RelativeJSONPointerIndexException(String.format(
                    "origin (%d) exceeds root (%d)", this.origin, baseParts.size()
            ));
        }

        List<Object> newParts = new ArrayList<>(baseParts.subList(0, baseParts.size() - this.origin));

        if (this.indexOffset != 0 && !newParts.isEmpty()) {
            int lastIndex = newParts.size() - 1;
            Object lastPart = newParts.get(lastIndex);
            if (lastPart instanceof Integer) {
                long newIndex = (long) (Integer) lastPart + this.indexOffset;
                if (newIndex < 0) {
                    throw new RelativeJSONPointerIndexException("index offset out of range: " + newIndex);
                }
                newParts.set(lastIndex, (int) newIndex);
            }
        }

        if (this.pointer instanceof JSONPointer) {
            newParts.addAll(((JSONPointer) this.pointer).getParts());
        } else if ("#".equals(this.pointer)) {
            if (newParts.isEmpty()) {
                throw new RelativeJSONPointerIndexException("cannot get key of root");
            }
            int lastIndex = newParts.size() - 1;
            Object lastPart = newParts.get(lastIndex);
            newParts.set(lastIndex, String.valueOf(lastPart)); // In Python, this becomes the key, not index
        }

        return JSONPointer.fromParts(newParts);
    }

    public JSONPointer to(JSONPointer basePointer) {
        return toImpl(basePointer, true, false);
    }

    /**
     * Returns a new JSONPointer relative to {@code pointer}.
     * @param basePointer a {@code JSONPointer} instance following JSON Pointer syntax.
     * @param unicodeEscape if `true`, UTF-16 escape sequences will be decoded before parsing the pointer.
     * @param uriDecode if `true`, the pointer will be unescaped before being parsed.
     * @return a new JSONPointer relative to {@code basePointer} argument.
     */
    public JSONPointer to(JSONPointer basePointer, boolean unicodeEscape, boolean uriDecode) {
        return toImpl(basePointer, unicodeEscape, uriDecode);
    }

    public JSONPointer to(String pointerString) {
        return toImpl(pointerString, true, false);
    }

    public JSONPointer to(String pointerString,boolean unicodeEscape, boolean uriDecode ) {
        return toImpl(pointerString, unicodeEscape, uriDecode);
    }

    /**
     * Returns a new JSONPointer relative to {@code pointer}.
     * Canonical implementation.
     *
     */
    private JSONPointer toImpl( Object pointerOrString, boolean unicodeEscape, boolean uriDecode) {
        JSONPointer pointer;
        if (pointerOrString instanceof String s) {
            pointer = new JSONPointer(s,  unicodeEscape, uriDecode);
        } else if (pointerOrString instanceof JSONPointer jp) {
            pointer = jp;
        } else {
            throw new JSONPointerTypeException(String.format("Expected JSONPointer or String, got %s", pointerOrString));
        }
        // Move to origin
        List<Object> baseParts = pointer.getParts();
        if ( this.origin > baseParts.size() ) {
            throw new RelativeJSONPointerIndexException(String.format(
                    "origin (%d) exceeds root (%d)", this.origin, baseParts.size()
            ));
        }

        List<Object> parts;
        if (origin < 1) {
            parts = new ArrayList<>(baseParts);  // need a mutable copy
        } else {
            parts = new ArrayList<>(baseParts.subList(0, baseParts.size() - this.origin));
        }

        // Array index offset
        if ( this.indexOffset != 0 && !parts.isEmpty() && isIntLike(parts.getLast())) {
            int newIndex;
            try {
                int i = Integer.parseInt(parts.getLast().toString());
                newIndex = i + this.indexOffset;
                if (newIndex < 0) {
                    throw new RelativeJSONPointerIndexException("index offset out of range: " + newIndex);
                }
                parts.set(parts.size() - 1, newIndex);
            } catch (NumberFormatException _) {
                // the successful call to isIntLike() above means this should not be a valid code path.
                throw new IllegalStateException("Should never get here. Bad int: %s".formatted(parts.getLast().toString()));
            }
        }

        // Pointer or index/property
        if (this.pointer instanceof JSONPointer) {
            parts.addAll(((JSONPointer) this.pointer).getParts());
        } else {
            assert this.pointer == HASH;
            parts.set(parts.size() - 1, "#%s".formatted(parts.getLast().toString()));
        }

        return JSONPointer.fromParts(parts, unicodeEscape, uriDecode);
    }


    /*
    def to(
        self,
        pointer: Union[JSONPointer, str],
        *,
        unicode_escape: bool = True,
        uri_decode: bool = False,
    ) -> JSONPointer:
        """Return a new JSONPointer relative to _pointer_.

        Args:
            pointer: A `JSONPointer` instance or a string following JSON
                Pointer syntax.
            unicode_escape: If `True`, UTF-16 escape sequences will be decoded
                before parsing the pointer.
            uri_decode: If `True`, the pointer will be unescaped using _urllib_
                before being parsed.
        """
        _pointer = (
            JSONPointer(pointer, unicode_escape=unicode_escape, uri_decode=uri_decode)
            if isinstance(pointer, str)
            else pointer
        )

        # Move to origin
        if self.origin > len(_pointer.parts):
            raise RelativeJSONPointerIndexError(
                f"origin ({self.origin}) exceeds root ({len(_pointer.parts)})"
            )

        if self.origin < 1:
            parts = list(_pointer.parts)
        else:
            parts = list(_pointer.parts[: -self.origin])

        # Array index offset
        if self.index and parts and self._int_like(parts[-1]):
            new_index = int(parts[-1]) + self.index
            if new_index < 0:
                raise RelativeJSONPointerIndexError(
                    f"index offset out of range {new_index}"
                )
            parts[-1] = int(parts[-1]) + self.index

        # Pointer or index/property
        if isinstance(self.pointer, JSONPointer):
            parts.extend(self.pointer.parts)
        else:
            assert self.pointer == "#"
            parts[-1] = f"#{parts[-1]}"

        return JSONPointer.from_parts(
            parts, unicode_escape=unicode_escape, uri_decode=uri_decode
        )

     */
}