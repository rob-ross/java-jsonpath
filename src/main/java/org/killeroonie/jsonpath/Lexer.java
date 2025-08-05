package org.killeroonie.jsonpath;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Tokenize a JSONPath string.
 *
 * Some customization can be achieved by subclassing Lexer and setting
 * class attributes. Then setting lexer_class on a JSONPathEnvironment.
 *
 * Class attributes:
 *     key_pattern: The regular expression pattern used to match mapping
 *         keys/properties.
 *     logical_not_pattern: The regular expression pattern used to match
 *         logical negation tokens. By default, not and ! are
 *         equivalent.
 *     logical_and_pattern: The regular expression pattern used to match
 *         logical and tokens. By default, and and && are equivalent.
 *     logical_or_pattern: The regular expression pattern used to match
 *         logical or tokens. By default, or and || are equivalent.
 */
public class Lexer {

    // Class attributes (static fields)
    public static final String key_pattern = "[\\u0080-\\uFFFFA-Za-z_][\\u0080-\\uFFFFA-Za-z0-9_-]*";

    // not or !
    public static final String logical_not_pattern = "(?:not\\b)|!";

    // && or and
    public static final String logical_and_pattern = "&&|(?:and\\b)";

    // || or or
    public static final String logical_or_pattern = "\\|\\||(?:or\\b)";

    // Instance attributes
    private JSONPathEnvironment env;
    private String double_quote_pattern;
    private String single_quote_pattern;
    private String dot_property_pattern;
    private String slice_list_pattern;
    private String re_pattern;
    private String function_pattern;
    private Pattern rules;

    /**
     * Constructor for Lexer.
     *
     * @param env The JSONPathEnvironment configuration
     */
    public Lexer(JSONPathEnvironment env) {
        this.env = env;

        this.double_quote_pattern = "\"(?<GDQUOTE>(?:(?!(?<!\\\\)\").)*?)\"";
        this.single_quote_pattern = "'(?<GSQUOTE>(?:(?!(?<!\\\\)').)*?)'";

        // .thing
        this.dot_property_pattern = "\\.(?<GPROP>" + key_pattern + ")";

        this.slice_list_pattern =
                "(?<GLSLICESTART>-?\\d*)\\s*" +
                        ":\\s*(?<GLSLICESTOP>-?\\d*)\\s*" +
                        "(?::\\s*(?<GLSLICESTEP>-?\\d*))?";

        // /pattern/ or /pattern/flags
        this.re_pattern = "/(?<GRE>.+?)/(?<GREFLAGS>[aims]*)";

        // func(
        this.function_pattern = "(?<GFUNC>[a-z][a-z_0-9]+)\\(\\s*";

        this.rules = compileRules();
    }

    /**
     * Prepare regular expression rules.
     *
     * @return Compiled Pattern for all lexer rules
     */
    private Pattern compileRules() {
        // Environment tokens
        List<TokenPatternPair> envTokens = new ArrayList<>();
        envTokens.add(new TokenPatternPair(TokenKind.ROOT, env.rootToken));
        envTokens.add(new TokenPatternPair(TokenKind.FAKE_ROOT, env.fakeRootToken));
        envTokens.add(new TokenPatternPair(TokenKind.SELF, env.selfToken));
        envTokens.add(new TokenPatternPair(TokenKind.KEY, env.keyToken));
        envTokens.add(new TokenPatternPair(TokenKind.UNION, env.unionToken));
        envTokens.add(new TokenPatternPair(TokenKind.INTERSECTION, env.intersectionToken));
        envTokens.add(new TokenPatternPair(TokenKind.FILTER_CONTEXT, env.filterContextToken));
        envTokens.add(new TokenPatternPair(TokenKind.KEY_SELECTOR, env.keysSelectorToken));

        List<TokenPatternPair> rules = new ArrayList<>();
        rules.add(new TokenPatternPair(TokenKind.DOUBLE_QUOTE_STRING, double_quote_pattern));
        rules.add(new TokenPatternPair(TokenKind.SINGLE_QUOTE_STRING, single_quote_pattern));
        rules.add(new TokenPatternPair(TokenKind.RE_PATTERN, re_pattern));
        rules.add(new TokenPatternPair(TokenKind.LIST_SLICE, slice_list_pattern));
        rules.add(new TokenPatternPair(TokenKind.FUNCTION, function_pattern));
        rules.add(new TokenPatternPair(TokenKind.DOT_PROPERTY, dot_property_pattern));
        rules.add(new TokenPatternPair(TokenKind.FLOAT, TokenKind.FLOAT.pattern));
        rules.add(new TokenPatternPair(TokenKind.INT, TokenKind.INT.pattern));
        rules.add(new TokenPatternPair(TokenKind.DDOT, TokenKind.DDOT.pattern));
        rules.add(new TokenPatternPair(TokenKind.AND, logical_and_pattern));
        rules.add(new TokenPatternPair(TokenKind.OR, logical_or_pattern));

        // Sort environment tokens by length (reverse order) and add non-empty ones
        envTokens.sort((a, b) -> Integer.compare(b.pattern.length(), a.pattern.length()));
        for (TokenPatternPair pair : envTokens) {
            if (!pair.pattern.isEmpty()) {
                rules.add(new TokenPatternPair(pair.tokenKind, Pattern.quote(pair.pattern)));
            }
        }

        rules.add(new TokenPatternPair(TokenKind.WILD, TokenKind.WILD.pattern));
        rules.add(new TokenPatternPair(TokenKind.FILTER, TokenKind.FILTER.pattern));
        rules.add(new TokenPatternPair(TokenKind.IN, TokenKind.IN.pattern));
        rules.add(new TokenPatternPair(TokenKind.TRUE, TokenKind.TRUE.pattern));
        rules.add(new TokenPatternPair(TokenKind.FALSE, TokenKind.FALSE.pattern));
        rules.add(new TokenPatternPair(TokenKind.NIL, TokenKind.NIL.pattern));
        rules.add(new TokenPatternPair(TokenKind.NULL, TokenKind.NULL.pattern));
        rules.add(new TokenPatternPair(TokenKind.NONE, TokenKind.NONE.pattern));
        rules.add(new TokenPatternPair(TokenKind.CONTAINS, TokenKind.CONTAINS.pattern));
        rules.add(new TokenPatternPair(TokenKind.UNDEFINED, TokenKind.UNDEFINED.pattern));
        rules.add(new TokenPatternPair(TokenKind.MISSING, TokenKind.MISSING.pattern));
        rules.add(new TokenPatternPair(TokenKind.LIST_START, TokenKind.LIST_START.pattern));
        rules.add(new TokenPatternPair(TokenKind.RBRACKET, TokenKind.RBRACKET.pattern));
        rules.add(new TokenPatternPair(TokenKind.COMMA, TokenKind.COMMA.pattern));
        rules.add(new TokenPatternPair(TokenKind.EQ, TokenKind.EQ.pattern));
        rules.add(new TokenPatternPair(TokenKind.NE, TokenKind.NE.pattern));
        rules.add(new TokenPatternPair(TokenKind.LG, TokenKind.LG.pattern));
        rules.add(new TokenPatternPair(TokenKind.LE, TokenKind.LE.pattern));
        rules.add(new TokenPatternPair(TokenKind.GE, TokenKind.GE.pattern));
        rules.add(new TokenPatternPair(TokenKind.RE, TokenKind.RE.pattern));
        rules.add(new TokenPatternPair(TokenKind.LT, TokenKind.LT.pattern));
        rules.add(new TokenPatternPair(TokenKind.GT, TokenKind.GT.pattern));
        rules.add(new TokenPatternPair(TokenKind.NOT, logical_not_pattern));
        rules.add(new TokenPatternPair(TokenKind.BARE_PROPERTY, key_pattern));
        rules.add(new TokenPatternPair(TokenKind.LPAREN, TokenKind.LPAREN.pattern));
        rules.add(new TokenPatternPair(TokenKind.RPAREN, TokenKind.RPAREN.pattern));
        rules.add(new TokenPatternPair(TokenKind.SKIP, TokenKind.SKIP.pattern));
        rules.add(new TokenPatternPair(TokenKind.ILLEGAL, TokenKind.ILLEGAL.pattern));

        StringBuilder patternBuilder = new StringBuilder();
        for (int i = 0; i < rules.size(); i++) {
            if (i > 0) {
                patternBuilder.append("|");
            }
            patternBuilder.append("(?<").append(rules.get(i).tokenKind.name()).append(">")
                    .append(rules.get(i).pattern).append(")");
        }

        return Pattern.compile(patternBuilder.toString(), Pattern.DOTALL);
    }

    /**
     * Generate a sequence of tokens from a JSONPath string.
     *
     * @param path The JSONPath string to tokenize
     * @return Iterator of Token objects
     */
    public Iterator<Token> tokenize(String path) {
        List<Token> tokens = new ArrayList<>();
        Matcher matcher = rules.matcher(path);

        while (matcher.find()) {
            TokenKind kind = null;
            String groupName = "";
            int groupNumber = -1;

            // Find which named group matched
            var namedGroups = matcher.namedGroups();
            System.out.println("namedGroups.size() = " + namedGroups.size() + ", matcher.toMatchResult().namedGroups()=" + matcher.toMatchResult().namedGroups());

            if (namedGroups.size() == 1) {
                var entry = namedGroups.entrySet().iterator().next();
                kind = TokenKind.valueOf(entry.getKey());
                groupNumber =  entry.getValue();
            } else if (namedGroups.size() > 1) {
                Map<Integer, String> reverseLookup = new HashMap<>();
                for (Map.Entry<String, Integer> entry  : namedGroups.entrySet()) {
                    reverseLookup.put(entry.getValue(), entry.getKey());
                }
                // look for the largest group number with a name
                for (int groupIndex = matcher.groupCount(); groupIndex > 0; groupIndex--) {
                    if (reverseLookup.containsKey(groupIndex)) {
                        groupName = reverseLookup.get(groupIndex);
                        groupNumber = groupIndex;
                        kind = TokenKind.valueOf(groupName);
                        break;
                    }
                }
            }
            assert kind != null;
            System.out.printf("group(groupname)= %s, kind=%s, groupName=%s, groupNumber=%d%n", matcher.group(groupName), kind, groupName, groupNumber);

//            for (int i = 1; i <= matcher.groupCount(); i++) {
//                if (matcher.group(i) != null) {
//                    // Get the group name - this is a simplified approach
//                    // In practice, you'd need to map group indices to names
//                    kind = TokenKind.valueOf(getGroupNameByIndex(i));
//                    break;
//                }
//            }

            if (kind.equals(TokenKind.DOT_PROPERTY)) {
                String propValue = matcher.group("GPROP");
                int propStart = matcher.start("GPROP");
                tokens.add(new Token(TokenKind.PROPERTY, propValue, propStart, path));

            } else if (kind.equals(TokenKind.BARE_PROPERTY)) {
                tokens.add(new Token(TokenKind.BARE_PROPERTY, matcher.group(), matcher.start(), path));

            } else if (kind.equals(TokenKind.LIST_SLICE)) {
                String startValue = matcher.group("GLSLICESTART");
                String stopValue = matcher.group("GLSLICESTOP");
                String stepValue = matcher.group("GLSLICESTEP");

                tokens.add(new Token(TokenKind.SLICE_START, startValue, matcher.start("GLSLICESTART"), path));
                tokens.add(new Token(TokenKind.SLICE_STOP, stopValue, matcher.start("GLSLICESTOP"), path));
                tokens.add(new Token(TokenKind.SLICE_STEP, stepValue != null ? stepValue : "",
                        matcher.start("GLSLICESTEP"), path));

            } else if (kind.equals(TokenKind.DOUBLE_QUOTE_STRING)) {
                String quotedValue = matcher.group("GDQUOTE");
                tokens.add(new Token(TokenKind.DOUBLE_QUOTE_STRING, quotedValue,
                        matcher.start("GDQUOTE"), path));

            } else if (kind.equals(TokenKind.SINGLE_QUOTE_STRING)) {
                String quotedValue = matcher.group("GSQUOTE");
                tokens.add(new Token(TokenKind.SINGLE_QUOTE_STRING, quotedValue,
                        matcher.start("GSQUOTE"), path));

            } else if (kind.equals(TokenKind.INT)) {
                String expGroup = matcher.group("GEXP");
                if (expGroup != null) {
                    tokens.add(new Token(TokenKind.FLOAT, matcher.group(), matcher.start(), path));
                } else {
                    tokens.add(new Token(TokenKind.INT, matcher.group(), matcher.start(), path));
                }

            } else if (kind.equals(TokenKind.RE_PATTERN)) {
                String reValue = matcher.group("GRE");
                String flagsValue = matcher.group("GREFLAGS");

                tokens.add(new Token(TokenKind.RE_PATTERN, reValue, matcher.start("GRE"), path));
                tokens.add(new Token(TokenKind.RE_FLAGS, flagsValue, matcher.start("GREFLAGS"), path));

            } else if (kind.equals(TokenKind.FUNCTION)) {
                String funcValue = matcher.group("GFUNC");
                tokens.add(new Token(TokenKind.FUNCTION, funcValue, matcher.start("GFUNC"), path));

            } else if (!kind.equals(TokenKind.SKIP)) {
                tokens.add(new Token(kind, matcher.group(), matcher.start(), path));
            }
        }

        return tokens.iterator();
    }

    // Helper classes and methods
    private static class TokenPatternPair {
        TokenKind tokenKind;
        String pattern;

        TokenPatternPair(TokenKind tokenKind, String pattern) {
            this.tokenKind = tokenKind;
            this.pattern = pattern;
        }
    }

    // This is a placeholder - you'd need to implement proper group name mapping
    private String getGroupNameByIndex(int index) {
        // This would need to be implemented based on your specific regex group mapping
        // For now, returning a placeholder
        return "UNKNOWN";
    }

    public static void main(String[] args) {
        JSONPathEnvironment env = new JSONPathEnvironment();
        Lexer lexer = new Lexer(env);
        String jsonpath = """
                $.foo.bar..[?@baz, 1, 3:5, *]""";
        var tokens = lexer.tokenize(jsonpath);
        for (Iterator<Token> it = tokens; it.hasNext(); ) {
            var token = it.next();
            System.out.println(token);


        }
    }

}