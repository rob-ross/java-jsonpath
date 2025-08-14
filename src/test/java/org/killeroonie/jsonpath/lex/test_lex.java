package org.killeroonie.jsonpath.lex;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.PJPEnv;
import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.TokenKind;
import org.killeroonie.jsonpath.exception.JSONPathSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Port of test_lex.py from python-jsonpath
 */
@SuppressWarnings("NewClassNamingConvention")
public class test_lex {

    private JSONPathEnvironment env; // shared per-test instance
    public static final String DATA_FILENAME = "test_lex.json"; // Assumes it is in the same package as this class.

    // TestCases is the top-level JSON object
    record TestCases(String description, List<Case> tests){}
    record Case(String description,
                String path,
                //@JsonDeserialize(contentUsing = TokenWrapperDeserializer.class)
                List<Token> want) { }

    ////////////////////////////////////////////////////////////////////
    /// FIXTURE
    ////////////////////////////////////////////////////////////////////


    @BeforeEach
    void env(){
        this.env = new PJPEnv();
    }

    static Stream<Arguments> testCases() {
        return load().tests().stream()
                .map(c -> Arguments.of(Named.of(c.description(), c)));
    }

    ////////////////////////////////////////////////////////////////////
    /// UNIT TESTS
    ////////////////////////////////////////////////////////////////////

    @Test
    void test_illegal_token() {
        assertThrows(JSONPathSyntaxException.class, () -> env.getLexer().tokenize("%"));
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void test_default_lexer(Case testCase) {
        List<Token> tokens = env.getLexer().tokenize(testCase.path());
        assertEquals(testCase.want(),  tokens, "Tokenization of `%s`".formatted( testCase.path()) );
    }


    ////////////////////////////////////////////////////////////////////
    /// HELPERS
    ////////////////////////////////////////////////////////////////////



    static TestCases load() {
        final ObjectMapper mapper = new ObjectMapper();

        JsonDeserializer<TokenKind> jsonDeserializer = new JsonDeserializer<>() {
            @Override
            public TokenKind deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String name = p.getText();
                // strip "TOKEN" prefix from the serialized name
                if (name.startsWith("TOKEN_")) name = name.substring(6);
                // Map historical names to current enum constants
                if ("KEYS".equals(name)) name = "KEY_SELECTOR";
                //if ("LIST_START".equals(name)) name = "LBRACKET";
                // Add other aliases as needed
                try {
                    return TokenKind.valueOf(name);
                } catch (IllegalArgumentException ex) {
                    // Fallback if unknown: you may prefer to throw
                    return TokenKind.UNKNOWN;
                }
            }
        };
        // Register only for this test mapper
        SimpleModule mod = new SimpleModule();
        mod.addDeserializer(TokenKind.class, jsonDeserializer);
        mapper.registerModule(mod);

        // Mix-in to ignore only the derived getters on Token
        abstract class TokenIgnoreDerivedProps {
            @com.fasterxml.jackson.annotation.JsonIgnore abstract int getLineNumber();
            @com.fasterxml.jackson.annotation.JsonIgnore abstract int getColumnNumber();
        }
        mapper.addMixIn(Token.class, TokenIgnoreDerivedProps.class);

        TestCases testCases;
        try(InputStream inputStream = test_lex.class.getResourceAsStream(DATA_FILENAME)) {
            testCases = mapper.readValue(inputStream, new TypeReference<>() { });
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load " + DATA_FILENAME, e);
        }

        return testCases;
    }
}
