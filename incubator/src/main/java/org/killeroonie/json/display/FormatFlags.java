package org.killeroonie.json.display;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Flags for various pretty printing options for nested JSON objects.
 *     The default flags are designed for debugging small nested dicts, and asJsonFormat() is useful for initializing
 *     flags for printing in a JSON-compatible format.
 * <p>
 *     The various "withXxx()" methods make a copy of this instance's flags and allow you to set a specific flag.
 * @param quoteStrings when true, wrap strings in quotes, when false omits quotes
 * @param singleQuotes when true, use single quotes instead of double quotes
 * @param useRepr when true, format strings with str() instead of repr()
 * @param formatJson when true, use "null" for `null` and "true"/"false" for `true`/`false`
 * @param indent number of spaces to indent each level of nesting
 * @param singleLine when true, format output as a single line, when false format as multiple lines
 * @param omitCommas when true, do not insert commas after List and Map item elements.
 *                   note: when printing with singleLine = true, if omitCommas is also true, output may be confusing
 *                   since List and Map elements will have no obvious visual separation in the string,
 *                   and parsing will be more complicated
 */
@SuppressWarnings("unused")
public record FormatFlags(boolean quoteStrings,
                          boolean singleQuotes,
                          // useRepr:
                          // in Python this cased strings to be displayed using repr().
                          boolean useRepr,
                          // formatJson:
                          // in Python this affected the string value of True/False and displayed as lowercase true/false.
                          // since Java uses the same letter casing as JavaScript, this distinction is not needed.
                          // however, we may want to use this flag to escape certain sequences for JSON.
                          boolean formatJson,
                          int indent,
                          boolean singleLine,
                          boolean omitCommas) {

    @Contract(" -> new")
    public static @NotNull FormatFlags defaults() {
        return new FormatFlags(
                false,
                false,
                false,
                false,
                2,
                true,
                false
        );
    }

    @Contract(" -> new")
    public static @NotNull FormatFlags asJsonFormat() {
        // to properly serialize to JSON also requires proper escaping of certain characters in the JSON text
        return new FormatFlags(
                true,
                false,
                true,
                true,
                2,
                false,
                false
        );
    }

    public FormatFlags withQuoteStrings(boolean quoteStrings) {
        return new FormatFlags(quoteStrings, this.singleQuotes, this.useRepr, this.formatJson, this.indent,
                this.singleLine, this.omitCommas);
    }

    public FormatFlags withSingleQuotes(boolean singleQuotes) {
        return new FormatFlags(this.quoteStrings, singleQuotes, this.useRepr, this.formatJson, this.indent,
                this.singleLine, this.omitCommas);
    }

    public FormatFlags withUseRepr(boolean useRepr) {
        return new FormatFlags(this.quoteStrings, this.singleQuotes, useRepr, this.formatJson, this.indent,
                this.singleLine, this.omitCommas);
    }

    public FormatFlags withFormatJson(boolean formatJson) {
        return new FormatFlags(this.quoteStrings, this.singleQuotes, this.useRepr, formatJson, this.indent,
                this.singleLine, this.omitCommas);
    }

    public FormatFlags withIndent(int indent) {
        return new FormatFlags(this.quoteStrings, this.singleQuotes, this.useRepr, this.formatJson, indent,
                this.singleLine, this.omitCommas);
    }

    public FormatFlags withSingleLine(boolean singleLine) {
        return new FormatFlags(this.quoteStrings, this.singleQuotes, this.useRepr, this.formatJson, this.indent,
                singleLine, this.omitCommas);
    }

    public FormatFlags withOmitCommas(boolean omitCommas) {
        return new FormatFlags(this.quoteStrings, this.singleQuotes, this.useRepr, this.formatJson, this.indent,
                this.singleLine, omitCommas);
    }
}

/*
self -> this
re:     (\(|(?:, )).*?=self
replace: $1this

isinstance -> instanceof
re:      isinstance\((.*),\w*(.*)\)
replace: ($1 instanceof $2)



 */
