package org.killeroonie.jsonpath.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.RFCEnv;
import org.killeroonie.jsonpath.Token;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Port of pyton-jsonpath-rfc9535 test_parse.py.
 * This tests an RFC compliant parser.
 * todo - python-jsonpath test_parser tests the full suite of features of the extended JSONPath query impl.
 * Once RFC compliant parser is working and tested, migrate this test accordingly.
 */
@SuppressWarnings("NewClassNamingConvention")
public class test_parse {
    private JSONPathEnvironment env; // shared per-test instance
    public static final String DATA_FILENAME = "test_parse.json"; // Assumes it is in the same package as this class.

    public record TestCases(String description, List<Case> tests){}
    public record Case(String description, String query, String want){};


    ////////////////////////////////////////////////////////////////////
    /// FIXTURE
    ////////////////////////////////////////////////////////////////////


    @BeforeEach
    void env(){
        this.env = new RFCEnv();
    }

    static Stream<Arguments> testCases() {
       // List<Case> tests = load(DATA_FILENAME).tests();
        return load(DATA_FILENAME).tests().stream()
                .map(c -> Arguments.of(Named.of(c.description(), c)));
    }

    public static TestCases load(String filename) {
        final ObjectMapper mapper = new ObjectMapper();

/*        // Register only for this test mapper
        SimpleModule mod = new SimpleModule();
        mod.addDeserializer(TokenKind.class, jsonDeserializer);
        mapper.registerModule(mod);

        // Mix-in to ignore only the derived getters on Token
        abstract class TokenIgnoreDerivedProps {
            @com.fasterxml.jackson.annotation.JsonIgnore abstract int getLineNumber();
            @com.fasterxml.jackson.annotation.JsonIgnore abstract int getColumnNumber();
        }
        mapper.addMixIn(Token.class, TokenIgnoreDerivedProps.class);*/

        TestCases testCases;

        try(InputStream inputStream = test_parse.class.getResourceAsStream(DATA_FILENAME)) {
            testCases = mapper.readValue(inputStream, new TypeReference<TestCases>() { });
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load " + DATA_FILENAME, e);
        }

        return testCases;
    }

    ////////////////////////////////////////////////////////////////////
    /// UNIT TESTS
    ////////////////////////////////////////////////////////////////////
    @ParameterizedTest
    @MethodSource("testCases")
    void test_parser(Case testCase) {
        var path = env.compile(testCase.query());
        assertEquals(testCase.want(), path.toString(), testCase.query() );
    }

}
