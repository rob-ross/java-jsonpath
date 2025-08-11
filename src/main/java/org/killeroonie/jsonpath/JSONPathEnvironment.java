package org.killeroonie.jsonpath;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.Optional;


/**
 * JSONPath configuration.
 *
 *     This class contains settings for path tokenization, parsing and resolution
 *     behavior, plus convenience methods for matching an unparsed path to some
 *     data.
 *
 *     Most applications will want to create a single `JSONPathEnvironment`, or
 *     use `jsonpath.compile()`, `jsonpath.findall()`, etc. from the package-level
 *     default environment.
 *
 *     ## Environment customization
 *
 *     Environment customization is achieved by subclassing `JSONPathEnvironment`
 *     and overriding class attributes and/or methods. Some of these
 *     customizations include:
 *
 *     - Changing the root (`$`), self (`@`) or filter context (`_`) token with
 *       class attributes `root_token`, `self_token` and `filter_context_token`.
 *     - Registering a custom lexer or parser with the class attributes
 *       `lexer_class` or `parser_class`. `lexer_class` must be a subclass of
 *       [`Lexer`]() and `parser_class` must be a subclass of [`Parser`]().
 *     - Setup built-in function extensions by overriding
 *       `setup_function_extensions()`
 *     - Hook in to mapping and sequence item getting by overriding `getitem()`.
 *     - Change filter comparison operator behavior by overriding `compare()`.
 *
 *     Arguments:
 *         filter_caching (bool): If `True`, filter expressions will be cached
 *             where possible.
 *         unicode_escape: If `True`, decode UTF-16 escape sequences found in
 *             JSONPath string literals.
 *         well_typed: Control well-typedness checks on filter function expressions.
 *             If `True` (the default), JSONPath expressions are checked for
 *             well-typedness as compile time.
 *
 *             **New in version 0.10.0**
 *
 *     ## Class attributes
 *
 *     Attributes:
 *         fake_root_token (str): The pattern used to select a "fake" root node, one level
 *             above the real root node.
 *         filter_context_token (str): The pattern used to select extra filter context
 *             data. Defaults to `"_"`.
 *         intersection_token (str): The pattern used as the intersection operator.
 *             Defaults to `"&"`.
 *         key_token (str): The pattern used to identify the current key or index when
 *             filtering a, mapping or sequence. Defaults to `"#"`.
 *         keys_selector_token (str): The pattern used as the "keys" selector. Defaults to
 *             `"~"`.
 *         lexer_class: The lexer to use when tokenizing path strings.
 *         max_int_index (int): The maximum integer allowed when selecting array items by
 *             index. Defaults to `(2**53) - 1`.
 *         min_int_index (int): The minimum integer allowed when selecting array items by
 *             index. Defaults to `-(2**53) + 1`.
 *         parser_class: The parser to use when parsing tokens from the lexer.
 *         root_token (str): The pattern used to select the root node in a JSON document.
 *             Defaults to `"$"`.
 *         self_token (str): The pattern used to select the current node in a JSON
 *             document. Defaults to `"@"`
 *         union_token (str): The pattern used as the union operator. Defaults to `"|"`.
 */
public class JSONPathEnvironment {

    // These should be unescaped strings. `re.escape` will be called
    // on them automatically when compiling lexer rules.
    public String intersectionToken = "&";
    public String rootToken = "$";
    public String unionToken = "|";

    private final boolean cacheFilters;
    private final boolean unicodeEscape;
    private final boolean wellTyped;


    // Override these to customize path tokenization and parsing.
    Class<Lexer> lexer_class = Lexer.class;
    Class<Parser> parser_class = Parser.class;
    Class<JSONPathMatch> match_class= JSONPathMatch.class;

    private final Lexer lexer;
    private final Parser parser;

    private final EnumMap<TokenKind, RulesBuilder.LexerRule> customRules;

    public JSONPathEnvironment() {
        this(true, true, true);
    }

    /**
     *
     * @param cacheFilters Enable or disable filter expression caching.
     * @param unicodeEscape Enable or disable decoding of UTF-16 escape sequences found in JSONPath string literals.
     * @param wellTyped Control well-typedness checks on filter function expressions.
     */
    public JSONPathEnvironment(boolean cacheFilters, boolean unicodeEscape, boolean wellTyped) {
        this.cacheFilters = cacheFilters;
        this.unicodeEscape = unicodeEscape;
        this.wellTyped = wellTyped;

        customRules = buildCustomRules();
        // The lexer bound to this environment.
        lexer = factoryMethod( lexer_class, this);
        // The parser bound to this environment.
        parser = factoryMethod( parser_class, this);


        // A list of function extensions available to filters.

        /*
        self.function_extensions: Dict[str, Callable[..., Any]] = {}
        self.setup_function_extensions()
         */
    }

    /**
     * The default {@code JSONPathEnvironment} implementation provides no custom rules.
     * Subclasses can override {@code buildCustomRules()} to specify custom matching and {@code TokenKind} emitting rules.
     * When {@code buildRules} is called from this class' constructor, any custom rules specified here for a TokenKind
     * will be used instead of the default values as defined in TokenKind. <p>
     * Custom rules defined here override any custom rules defined in the Lexer to which this instance is attached.
     * Note this implementation should be interpreted as an example of how subclasses could implement this method. The
     * actual `custom rules` used here are exactly the same as the default rules for each TokenKind, so no new behavior
     * is implemented.
     *
     * @return the Map of {@link TokenKind} to custom {@link RulesBuilder.LexerRule}s that will be used by Lexer class.
     */
    public EnumMap<TokenKind, RulesBuilder.LexerRule> buildCustomRules() {
        // These should be unescaped strings. `re.escape` will be called
        // on them automatically when compiling lexer rules.
        final String pseudoRootToken = "^";
        final String filterContextToken = "_";
        final String intersectionToken = "&";
        final String keyToken = "#";
        final String keysSelectorToken = "~";
        final String unionToken = "|";
        EnumMap<TokenKind, RulesBuilder.LexerRule> rules = new EnumMap<>(TokenKind.class);
        rules.put(TokenKind.PSEUDO_ROOT,
                new RulesBuilder.LexemeRule(pseudoRootToken, TokenKind.PSEUDO_ROOT)
        );
        rules.put(TokenKind.FILTER_CONTEXT,
                new RulesBuilder.LexemeRule(filterContextToken, TokenKind.FILTER_CONTEXT)
        );
        rules.put(TokenKind.INTERSECTION,
                new RulesBuilder.LexemeRule(intersectionToken, TokenKind.INTERSECTION)
        );
        rules.put(TokenKind.KEY,
                new RulesBuilder.LexemeRule(keyToken, TokenKind.KEY)
        );
        rules.put(TokenKind.KEY_SELECTOR,
                new RulesBuilder.LexemeRule(keysSelectorToken, TokenKind.KEY_SELECTOR)
        );
        rules.put(TokenKind.UNION,
                new RulesBuilder.LexemeRule(unionToken, TokenKind.UNION)
        );
        return rules;
    }

    /**
     * Prepare a path string ready for repeated matching against different data.
     *
     *         Arguments:
     *             path: A JSONPath as a string.
     *
     *         Returns:
     *             A `JSONPath` or `CompoundJSONPath`, ready to match against some data.
     *                 Expect a `CompoundJSONPath` if the path string uses the _union_ or
     *                 _intersection_ operators.
     *
     *         Raises:
     *             JSONPathSyntaxError: If _path_ is invalid.
     *             JSONPathTypeError: If filter functions are given arguments of an
     *                 unacceptable type.
     * @param path
     * @return
     */
    public JSONPath compile( String path){
        return null;
    }

    public Iterable<JSONPathMatch> findIter(String path, Object data) {
        return findIter(path, data, null);
    }

    /**
     * Generate `JSONPathMatch` objects for each match of _path_ in _data_.
     *
     *         If _data_ is a string or a file-like objects, it will be loaded using
     *         `json.loads()` and the default `JSONDecoder`.
     *
     *         Arguments:
     *             path: The JSONPath as a string.
     *             data: A JSON document or Python object implementing the `Sequence`
     *                 or `Mapping` interfaces.
     *             filter_context: Arbitrary data made available to filters using
     *                 the _filter context_ selector.
     *
     *         Returns:
     *             An iterator yielding `JSONPathMatch` objects for each match.
     *
     *         Raises:
     *             JSONPathSyntaxError: If the path is invalid.
     *             JSONPathTypeError: If a filter expression attempts to use types in
     *                 an incompatible way.
     * @param path
     * @param data
     * @param filterContext
     * @return
     */
    public Iterable<JSONPathMatch> findIter(String path, Object data, FilterContextVars filterContext) {
        return compile(path).finditer(data, filterContext);
    }


    /**
     * Creates a new T (Lexer or Parser) instance with {@code env} as the sole constructor argument, and
     * returns the new instance to the caller.
     * @param clazz the class object for the new instance, either {@code Lexer.class} or {@code Parser.class}, or
     *              subclasses of these.
     * @param env the {@code JSONPathEnvironment} argument for the constructor. Usually, just {@code this}
     * @return a new instance of the parameter type T.
     * @param <T> {@link Lexer} or {@link Parser}
     */
    protected <T> T factoryMethod(@NotNull Class<T> clazz, JSONPathEnvironment env) {
        Constructor<T> constructor;
        try {
            constructor = clazz.getDeclaredConstructor(JSONPathEnvironment.class);
        } catch (NoSuchMethodException e) {
            // this is a programming error, so an RTE is appropriate.
            throw new RuntimeException(e);
        }
        T instance;
        try {
            instance = constructor.newInstance(env);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return instance;
    }

    /**
     * Attempt to locate a custom LexerRule for the argument
     * @param kind the TokenKind for which to locate a custom LexerRule.
     * @return an Optional<LexerRule> with the custom rule if found, or an empty Optional otherwise.
     */
    public Optional<RulesBuilder.LexerRule> findRule(TokenKind kind) {
        RulesBuilder.LexerRule rule = customRules.getOrDefault(kind, null);
        return  Optional.ofNullable(rule);
    }
}
