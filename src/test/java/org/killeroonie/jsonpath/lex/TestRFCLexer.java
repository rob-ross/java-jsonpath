package org.killeroonie.jsonpath.lex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.killeroonie.jsonpath.JJPEnv;
import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.RFCEnv;
import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.exception.JSONPathSyntaxException;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The RFCEnv environment uses the JJPLexer, but it uses an RFCRulesBuilder and a STRICT whitespace policy.
 */
public class TestRFCLexer {

    private JSONPathEnvironment env; // shared per-test instance

    ////////////////////////////////////////////////////////////////////
    /// FIXTURE
    ////////////////////////////////////////////////////////////////////


    @BeforeEach
    void env(){
        this.env = new RFCEnv();
    }


    static Stream<Arguments> allTestCases() {
        Stream<Arguments> ctsTests = Helper.load("cts.rfc.json").tests().stream()
                .map(c -> Arguments.of(Named.of("CTS: " + c.description(), c)));

        Stream<Arguments> pjpTests = Helper.load("pjp.lex.rfc.json").tests().stream()
                .map(c -> Arguments.of(Named.of("PJP: " + c.description(), c)));

        return Stream.concat(ctsTests, pjpTests);
    }


    ////////////////////////////////////////////////////////////////////
    /// UNIT TESTS
    ////////////////////////////////////////////////////////////////////


    @ParameterizedTest
    @MethodSource("allTestCases")
    void test_tokenize(Helper.Case testCase) {
        if ( testCase.isInvalid()) {
            assertThrows(JSONPathSyntaxException.class, () -> env.getLexer().tokenize(testCase.path()));
        } else {
            List<Token> tokens = env.getLexer().tokenize(testCase.path());
            assertEquals(testCase.want(),  tokens, "Tokenization of `%s`".formatted( testCase.path()) );
        }
    }

}
