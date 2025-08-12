package org.killeroonie.jsonpath;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.killeroonie.jsonpath.exception.JSONPathSyntaxException;

import java.io.IOException;
import java.util.Arrays;
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

    ////////////////////////////////////////////////////////////////////
    /// FIXTURE
    ////////////////////////////////////////////////////////////////////


    @BeforeEach
    void env(){
        this.env = new PJPEnv();
    }

    static Stream<Arguments> defaultsTestCases() {
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
    @MethodSource("defaultsTestCases")
    void test_default_lexer(Case testCase) {
        List<Token> tokens = env.getLexer().tokenize(testCase.path());
//        System.out.println("Expected tokens: " + testCase.want());
//        for (Token token : testCase.want()) {
//            printTokenDetails(token);
//        }
//        System.out.println("Actual tokens: " + tokens);
//        for (Token token : tokens) {
//            printTokenDetails(token);
//        }
        assertEquals(testCase.want(),  tokens, "Tokenization of `%s`".formatted( testCase.path) );
    }

    void printTokenDetails(Token t) {
        String msg = "kind=%s, index=%d, value=%s".formatted(t.kind(), t.index(), t.value());
        System.out.println(msg);
    }

    ////////////////////////////////////////////////////////////////////
    /// HELPERS
    ////////////////////////////////////////////////////////////////////

    // TestCases is the top-level JSON object
    record TestCases(String description, List<Case> tests){}
    record Case(String description,
                String path,
                @JsonDeserialize(contentUsing = TokenWrapperDeserializer.class)
                List<Token> want) { }

    // In the JSON file, the members of each Token object are wrapped in a parent object(Map/dict) with keyname "Token"
    // This deserializer handles skipping past the outer object wrapper to get the values of each Token object
    static class TokenWrapperDeserializer extends JsonDeserializer<Token> {
        @Override
        public Token deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);

            // If it’s the wrapped form: { "Token": { ... } }
            if (node.isObject() && node.size() == 1 && node.has("Token")) {
                JsonNode inner = node.get("Token");
                // Delegate to the default binding for Token using the inner node
                JsonParser innerParser = inner.traverse(p.getCodec());
                innerParser.nextToken();
                return ctxt.readValue(innerParser, Token.class);
            }

            // If it’s already the direct form (in case you change JSON later): { ...fields... }
            JsonParser directParser = node.traverse(p.getCodec());
            directParser.nextToken();
            return ctxt.readValue(directParser, Token.class);
        }
    }

    // Here we can tweak differences between the name of the TOKEN_XXX string constants in the Python version
    // (from where the test data originates) and our Enum TokenKind implementation.
    static class TokenKindDeserializer extends JsonDeserializer<TokenKind> {
        @Override
        public TokenKind deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String raw = p.getText();
            String name = raw;
            if (name.startsWith("TOKEN_")) name = name.substring(6);
            // Map historical names to current enum constants
            //if ("LIST_START".equals(name)) name = "LBRACKET";
            // Add other aliases as needed
            try {
                return TokenKind.valueOf(name);
            } catch (IllegalArgumentException ex) {
                // Fallback if unknown: you may prefer to throw
                return TokenKind.UNKNOWN;
            }
        }
    }

    // Loads the JSON test data file into a TestCases instance. Data for each unit test is then available
    // in the TestCase's `tests` variable as a List<Case>.
    //
    static TestCases load() {
        final String fileName = DATA_FILENAME;
        final ObjectMapper mapper = new ObjectMapper();
        // Register only for this test mapper
        SimpleModule mod = new SimpleModule();
        mod.addDeserializer(TokenKind.class, new TokenKindDeserializer());
        mapper.registerModule(mod);

        // Mix-in to ignore only the derived getters on Token
        abstract class TokenIgnoreDerivedProps {
            @com.fasterxml.jackson.annotation.JsonIgnore abstract int getLineNumber();
            @com.fasterxml.jackson.annotation.JsonIgnore abstract int getColumnNumber();
        }
        mapper.addMixIn(Token.class, TokenIgnoreDerivedProps.class);

        TestCases testCases = null;
        try(var inputStream = test_lex.class.getResourceAsStream(fileName)) {
            testCases = mapper.readValue(inputStream, new TypeReference<TestCases>() { });
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load " + fileName, e);
        }
        String prettyJson = null;
        System.out.println("Test Cases loaded: ");
        System.out.println("num test cases: " + testCases.tests.size());

        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            prettyJson = mapper.writeValueAsString(testCases);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        System.out.println(prettyJson);
        System.out.println("num test cases: " + testCases.tests.size());
        System.out.println("tests: " + testCases.tests);

        return testCases;
    }

    public static void main(String[] args) {
        load();
    }


}
