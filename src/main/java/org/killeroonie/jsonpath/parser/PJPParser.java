package org.killeroonie.jsonpath.parser;

import org.jetbrains.annotations.NotNull;
import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.parser.selector.JSONPathSelector;
import org.killeroonie.jsonpath.parser.selector.PropertySelector;
import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.TokenKind;
import org.killeroonie.jsonpath.exception.JSONPathSyntaxException;
import org.killeroonie.jsonpath.parser.segment.JSONPathSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class PJPParser extends BaseParser {

    // we may need an environment parameter here
    public PJPParser(JSONPathEnvironment env) {
        super(env);
    }

    static final EnumSet<TokenKind> START_KINDS  = EnumSet.of(TokenKind.ROOT, TokenKind.PSEUDO_ROOT);

    private TokenStream stream;


    /**
     * Parses a JSONPath from a stream of tokens.
     * @param stream the stream of Tokens
     * @return the List of JSONPathSegment
     */
    @Override
    public List<JSONPathSegment> parse(final @NotNull TokenStream stream) {
        this.stream = stream;
        stream.expect(START_KINDS);
        stream.next();
        List<JSONPathSegment> segments = new ArrayList<>(parsePath(false));

        if ( stream.current() != Token.EOF ) {
            throw new JSONPathSyntaxException(
                    "unexpected token %s".formatted(stream.current().value()), stream.current());
        }
        return segments;
    }

    private static final EnumSet<TokenKind> parseQuerySet =
            EnumSet.of(TokenKind.LIST_START, TokenKind.PROPERTY, TokenKind.WILD);
    /**
     * Parse a top-level JSONPath expression or a filter query.
     * @param inFilter true if this method should parse the current position as a filter query
     * @return
     */
    protected List<JSONPathSegment> parsePath(boolean inFilter) {
        List<JSONPathSegment> segments = new ArrayList<>();



        return segments;
    }

    /*
        while True:
            if stream.current.type_ == TokenType.DOUBLE_DOT:
                tok = stream.next_token()
                selectors = self.parse_selectors(stream)
                yield JSONPathRecursiveDescentSegment(
                    env=self.env,
                    token=tok,
                    selectors=selectors,
                )
            elif stream.current.type_ in {
                TokenType.LBRACKET,
                TokenType.PROPERTY,
                TokenType.WILD,
            }:
                tok = stream.current
                selectors = self.parse_selectors(stream)
                yield JSONPathChildSegment(
                    env=self.env,
                    token=tok,
                    selectors=selectors,
                )
            else:
                if in_filter:
                    stream.push(stream.current)
                break

            stream.next_token()


     */


}
