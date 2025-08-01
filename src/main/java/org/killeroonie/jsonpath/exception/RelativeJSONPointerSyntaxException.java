package org.killeroonie.jsonpath.exception;

/**
 * An exception raised when we fail to parse a relative JSON Pointer.
 */
public class RelativeJSONPointerSyntaxException extends RelativeJSONPointerException {
    private final String rel;

    public RelativeJSONPointerSyntaxException(String message, String rel) {
        super(message);
        this.rel = rel;
    }

    public String getRel() {
        return rel;
    }

    @Override
    public String getMessage() {
        if (rel == null || rel.isEmpty()) {
            return super.getMessage();
        }

        String msg = (rel.length() > 7) ? rel.substring(0, 7) + ".." : rel;
        return String.format("%s '%s'", super.getMessage(), msg);
    }
}