package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.Token;

public class StringLiteral extends FilterExpressionLiteral<String> {

    public StringLiteral(Token token, String value) {
        super(token, value);
    }

    public static String canonicalString(String string) {
        String singlqQuoted = string.substring(0, string.length()).
                replaceAll("\\\"", "\"").
                replaceAll("'", "\\'");
        return "'" + singlqQuoted + "'";
    }


    /*

        def __str__(self) -> str:
        return canonical_string(self.value)

    def canonical_string(value: str) -> str:
    """Return _value_ as a canonically formatted string literal."""
    single_quoted = (
        json.dumps(value, ensure_ascii=False)[1:-1]
        .replace('\\"', '"')
        .replace("'", "\\'")
    )
    return f"'{single_quoted}'"
     */
}
