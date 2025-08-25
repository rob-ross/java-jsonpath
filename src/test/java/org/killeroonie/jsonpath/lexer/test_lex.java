package org.killeroonie.jsonpath.lexer;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.killeroonie.jsonpath.Helper;
import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.PJPEnv;
import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.exception.JSONPathSyntaxException;
import org.killeroonie.jsonpath.Helper.Case;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.killeroonie.jsonpath.Helper.load;

/**
 * Port of test_lex.py from python-jsonpath
 */
@SuppressWarnings("NewClassNamingConvention")
public class test_lex {

    private JSONPathEnvironment env; // shared per-test instance
    //public static final String DATA_FILENAME = "src/test/resources/org/killeroonie/jsonpath/lexer/test_lex.json"; // Assumes it is in the same package as this class.
    public static final String DATA_FILENAME = "lexer/test_lex.json"; // Assumes it is in the same package as this class.



    ////////////////////////////////////////////////////////////////////
    /// FIXTURE
    ////////////////////////////////////////////////////////////////////


    @BeforeEach
    void env(){
        this.env = new PJPEnv();
    }

    static Stream<Arguments> testCases() {
        List<Case> tests;
        try(InputStream inputStream = Helper.class.getResourceAsStream(DATA_FILENAME)) {
            tests = load(inputStream).tests();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load " + DATA_FILENAME, e);
        }
        return tests.stream()
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
        // `test_lex.json` is shared with the python-jsonpath project. That lexer does not
        // include EOF in the token stream, but our Java implementation does. So we fix the missing EOF
        // in the test data here.
        testCase.want().add(Token.EOF);
        assertEquals(testCase.want(),  tokens, "Tokenization of `%s`".formatted( testCase.path()) );
    }
}
