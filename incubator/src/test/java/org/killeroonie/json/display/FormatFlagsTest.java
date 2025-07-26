package org.killeroonie.json.display;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// JUnit uses assertEquals(expected, actual) like Python unittest,
// this is the opposite of Pytest assertEquals(actual, expected)
public class FormatFlagsTest {

    @Test
    void test_asJsonFormat() {
        FormatFlags flags = FormatFlags.asJsonFormat();
        assertTrue(flags.quoteStrings(), "quoteStrings should be true");
        assertFalse(flags.singleQuotes(), "singleQuotes should be false");
        assertTrue(flags.useRepr(), "useRepr should be true");
        assertTrue(flags.formatJson(), "formatJson should be true");
        assertEquals(2, flags.indent(), "indent should be 2");
        assertFalse(flags.multiLine(),"multiLine should be false");
        assertFalse(flags.omitCommas(), "omitCommas should be false");
    }

    @Test
    void test_defaults() {
        FormatFlags flags = FormatFlags.defaults();
        assertFalse(flags.quoteStrings(), "quoteStrings should be false");
        assertFalse(flags.singleQuotes(), "singleQuotes should be false");
        assertFalse(flags.useRepr(), "useRepr should be false");
        assertFalse(flags.formatJson(), "formatJson should be false");
        assertEquals(2, flags.indent(), "indent should be 2");
        assertFalse(flags.multiLine(),"multiLine should be false");
        assertFalse(flags.omitCommas(), "omitCommas should be false");
    }

    @Test
    void test_withQuoteStrings() {
        FormatFlags flags = FormatFlags.defaults().withQuoteStrings(true);
        assertTrue(flags.quoteStrings(), "quoteStrings should be true");
        assertFalse(flags.singleQuotes(), "singleQuotes should be false");
        assertFalse(flags.useRepr(), "useRepr should be false");
        assertFalse(flags.formatJson(), "formatJson should be false");
        assertEquals(2, flags.indent(), "indent should be 2");
        assertFalse(flags.multiLine(),"multiLine should be false");
        assertFalse(flags.omitCommas(), "omitCommas should be false");

    }

    @Test
    void test_withSingleQuotes() {
        FormatFlags flags = FormatFlags.defaults().withSingleQuotes(true);
        assertFalse(flags.quoteStrings(), "quoteStrings should be false");
        assertTrue(flags.singleQuotes(), "singleQuotes should be true");
        assertFalse(flags.useRepr(), "useRepr should be false");
        assertFalse(flags.formatJson(), "formatJson should be false");
        assertEquals(2, flags.indent(), "indent should be 2");
        assertFalse(flags.multiLine(),"multiLine should be false");
        assertFalse(flags.omitCommas(), "omitCommas should be false");
    }

    @Test
    void test_withUseRepr() {
        FormatFlags flags = FormatFlags.defaults().withUseRepr(true);
        assertFalse(flags.quoteStrings(), "quoteStrings should be false");
        assertFalse(flags.singleQuotes(), "singleQuotes should be false");
        assertTrue(flags.useRepr(), "useRepr should be true");
        assertFalse(flags.formatJson(), "formatJson should be false");
        assertEquals(2, flags.indent(), "indent should be 2");
        assertFalse(flags.multiLine(),"multiLine should be false");
        assertFalse(flags.omitCommas(), "omitCommas should be false");
    }

    @Test
    void test_withFormatJson() {
        FormatFlags flags = FormatFlags.defaults().withFormatJson(true);
        assertFalse(flags.quoteStrings(), "quoteStrings should be false");
        assertFalse(flags.singleQuotes(), "singleQuotes should be false");
        assertFalse(flags.useRepr(), "useRepr should be false");
        assertTrue(flags.formatJson(), "formatJson should be true");
        assertEquals(2, flags.indent(), "indent should be 2");
        assertFalse(flags.multiLine(),"multiLine should be false");
        assertFalse(flags.omitCommas(), "omitCommas should be false");
    }

    @Test
    void test_withIndent() {
        FormatFlags flags = FormatFlags.defaults().withIndent(4);
        assertFalse(flags.quoteStrings(), "quoteStrings should be false");
        assertFalse(flags.singleQuotes(), "singleQuotes should be false");
        assertFalse(flags.useRepr(), "useRepr should be false");
        assertFalse(flags.formatJson(), "formatJson should be false");
        assertEquals(4, flags.indent(), "indent should be 4");
        assertFalse(flags.multiLine(),"multiLine should be false");
        assertFalse(flags.omitCommas(), "omitCommas should be false");
    }

    @Test
    void test_withMultipleLines() {
        FormatFlags flags = FormatFlags.defaults().withMultipleLines(true);
        assertFalse(flags.quoteStrings(), "quoteStrings should be false");
        assertFalse(flags.singleQuotes(), "singleQuotes should be false");
        assertFalse(flags.useRepr(), "useRepr should be false");
        assertFalse(flags.formatJson(), "formatJson should be false");
        assertEquals(2, flags.indent(), "indent should be 2");
        assertTrue(flags.multiLine(),"multiLine should be true");
        assertFalse(flags.omitCommas(), "omitCommas should be false");
    }

    @Test
    void test_withOmitCommas() {
        FormatFlags flags = FormatFlags.defaults().withOmitCommas(true);
        assertFalse(flags.quoteStrings(), "quoteStrings should be false");
        assertFalse(flags.singleQuotes(), "singleQuotes should be false");
        assertFalse(flags.useRepr(), "useRepr should be false");
        assertFalse(flags.formatJson(), "formatJson should be false");
        assertEquals(2, flags.indent(), "indent should be 2");
        assertFalse(flags.multiLine(),"multiLine should be false");
        assertTrue(flags.omitCommas(), "omitCommas should be true");
    }
}
