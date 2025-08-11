package org.killeroonie.jsonpath;

import org.jetbrains.annotations.NotNull;

/**
 * A token, as returned from {@link Lexer}.{@code tokenize()}
 */
public record Token(TokenKind kind, String value, int index, String path) {

    @Override
    public @NotNull String toString() {
        String msg;
        if (kind == TokenKind.LIST_SLICE) {
            msg = kind + ":[" + value + "]";
        } else if ( kind.isIdentifier() || kind.isKeyword() || kind.isLiteral()) {
            msg = kind + ":" + value;
        } else {
            msg = kind + "";
        }
        return msg;
    }

    public static int countOccurrences(String str, String substr, int startIndex, int endIndex) {
        String subString = str.substring(startIndex, endIndex);
        int count = 0;
        int lastIndex = 0;

        while (lastIndex != -1) {
            lastIndex = subString.indexOf(substr, lastIndex);
            if (lastIndex != -1) {
                count++;
                lastIndex += substr.length();
            }
        }
        return count;
    }
    /**
     * Return the line and column number for the start of this token.
     * todo - is there a better return type than int[] here?
     * @return a two-element int array where [0] is the line number and [1] is the column.
     */
    public int[] position(){

        /*
        todo - test
        original python:
                line_number = self.value.count("\n", 0, self.index) + 1
                column_number = self.index - self.value.rfind("\n", 0, self.index)
         */
        int lineNumber = countOccurrences(path, "\n", 0, index ) + 1;
        int columnNumber = index - path.substring(0, index).lastIndexOf("\n");

        return new int[]{ lineNumber, columnNumber - 1};
    }

    /**
     * Returns the line number for the start of this token.
     * @return the line number for the start of this token.
     */
    public int getLineNumber() {
        return countOccurrences(path, "\n", 0, index ) + 1;
    }

    /**
     * Returns the column number for the start of this token.
     * @return the column number for the start of this token.
     */
    public int getColumnNumber() {
        return index - path.substring(0, index).lastIndexOf("\n");
    }

}
