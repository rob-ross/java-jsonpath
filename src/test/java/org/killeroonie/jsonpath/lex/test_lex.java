package org.killeroonie.jsonpath.lex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.PJPEnv;
import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.exception.JSONPathSyntaxException;
import org.killeroonie.jsonpath.lex.Helper.Case;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.killeroonie.jsonpath.lex.Helper.load;

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

    static Stream<Arguments> testCases() {
        return load(DATA_FILENAME).tests().stream()
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
}
