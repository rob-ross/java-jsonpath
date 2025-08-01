package org.killeroonie.jsonpath;

import org.killeroonie.jsonpath.exception.RelativeJSONPointerIndexException;
import org.killeroonie.jsonpath.exception.RelativeJSONPointerSyntaxException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        if (rel == null || rel.trim().isEmpty()) {
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
        if ("#".equals(pointerStr)) {
            this.pointer = "#";
        } else {
            this.pointer = new JSONPointer(pointerStr);
        }
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

    /**
     * Applies this relative pointer to an existing absolute pointer.
     *
     * @param basePointer The absolute pointer to which this relative pointer is applied.
     * @return A new, absolute {@code JSONPointer}.
     */
    public JSONPointer to(JSONPointer basePointer) {
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
}