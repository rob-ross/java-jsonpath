package org.killeroonie.jsonpath;


import org.killeroonie.jsonpath.lex.Lexer;

import java.io.InputStream;
import java.util.regex.*;
import java.util.*;

public class TempImplementations {

    private static final Pattern _RE_PROBABLY_MALFORMED = Pattern.compile("[{}\\[\\]]");

    /**
     * Used by Path, Pointer, Patch to load JSON data for further processing
     *
     * @param data the InputStream from which to read the JSON data.
     * @return todo - what do we return? Will probably depend on what Path, Poiner, and Patch are doing with this data.
     * We may need to provide an overloaded version that takes a method reference or lambda to give caller more control
     * over what we return (Map, List, scalar, JsonNode, POJO, etc.)
     */
    Object loadData(InputStream data) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    Object loadData(String data) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

}

/*
_RE_PROBABLY_MALFORMED = re.compile(r"[\{\}\[\]]")


def load_data(data: object) -> Any:
    if isinstance(data, str):
        try:
            return json.loads(data)
        except json.JSONDecodeError:
            # Overly simple way to detect a malformed JSON document vs a
            # top-level string only document
            if _RE_PROBABLY_MALFORMED.search(data):
                raise
            return data
    if isinstance(data, IOBase):
        return json.loads(data.read())
    return data
 */


class Parser {
    public Parser(JSONPathEnvironment env) {}
    Token tokenize() { return null; };
}

class JSONPathMatch {
    public final FilterContextVars filterContext;
    public final Object obj;
    public final JSONPathMatch parent;
    public final String path;
    public final List<Object> parts;
    public final Object root;

    public JSONPathMatch(FilterContextVars filterContext, Object obj, JSONPathMatch parent,
                         String path, List<Object> parts, Object root) {
        this.filterContext = filterContext;
        this.obj = obj;
        this.parent = parent;
        this.path = path;
        this.parts = parts;
        this.root = root;
    }

    public String getParts() { return ""; }
}

class FilterContextVars {}


class Regex {
    public static Pattern compile(String pattern) {
        return Pattern.compile(pattern);
    }

    public static Pattern compile(String pattern, int flags) {
        return Pattern.compile(pattern, flags);
    }

    public static Matcher match(String pattern, String text) {
        return compile(pattern).matcher(text);
    }

    public static boolean fullMatch(String pattern, String text) {
        return match(pattern, text).matches();
    }

    public static boolean search(String pattern, String text) {
        return match(pattern, text).find();
    }

    public static String sub(String pattern, String replacement, String text) {
        return match(pattern, text).replaceAll(replacement);
    }

    public static List<String> findAll(String pattern, String text) {
        Matcher matcher = match(pattern, text);
        List<String> results = new ArrayList<>();
        while (matcher.find()) {
            results.add(matcher.group());
        }
        return results;
    }

    public static List<List<String>> findAllGroups(String pattern, String text) {
        Matcher matcher = match(pattern, text);
        List<List<String>> allMatches = new ArrayList<>();
        while (matcher.find()) {
            List<String> groups = new ArrayList<>();
            for (int i = 1; i <= matcher.groupCount(); i++) {
                groups.add(matcher.group(i));
            }
            allMatches.add(groups);
        }
        return allMatches;
    }
}

class LexerChild extends Lexer {

    /**
     * Constructor for Lexer.
     *
     * @param env The JSONPathEnvironment configuration
     */
    public LexerChild(JSONPathEnvironment env) {
        super(env);
    }

    @Override
    protected void processIdentifier(String group) {
        super.processIdentifier(group);
    }
}
