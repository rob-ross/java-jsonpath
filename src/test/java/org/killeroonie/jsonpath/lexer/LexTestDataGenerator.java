package org.killeroonie.jsonpath.lexer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.killeroonie.jsonpath.*;
import org.killeroonie.jsonpath.exception.JSONPathException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class used to build test case data for Lexing
 */
public class LexTestDataGenerator {



    ////////////////////////////////////////////////////////////////////
    /// HELPERS
    ////////////////////////////////////////////////////////////////////

    static void printTokenDetails(Token t) {
        String msg = "kind=%s, index=%d, value=%s".formatted(t.kind(), t.index(), t.value());
        System.out.println(msg);
    }

    private static String escapeControlChars(String text) {
        return text.replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\f", "\\f");
    }


    // TestCases is the top-level JSON object
    record TestCases(String description, List<Case> tests){}
    record Case(String description, String path, Boolean isInvalid, String exception, List<Token> want) {

        /**
         * Convenience constructor for a valid test case that is expected to pass.
         */
        public Case(String description, String path, List<Token> want) {
            this(description, path, null, null, want);
        }

        /**
         * Convenience constructor for a test case that is expected to fail with an exception.
         */
        public Case(String description, String path, String exception) {
            this(description, path, true, exception, java.util.Collections.emptyList());
        }
    }

    ////////////////////////////////////////////////////////////////////
    /// LEXER TEST CASE GENERATION
    ///  Source - cts.json
    ////////////////////////////////////////////////////////////////////

    // generate lexer test file from
    // source -> cts
    // env -> JJPEnv
    // lexer -> JJPLexer

    /**
     * Generate cts.jjplexer.json file
     * This captures the current state of the Lexer as the "correct" output in all cases, so only run this when
     * the Lexer is stable and working correctly.
     * @param env
     */
    static void generateLexerTestCasesFromCTS(JSONPathEnvironment env, String fileName, String msg) {
        RulesBuilder rb = env.getRulesBuilder();
        Lexer lexer = env.getLexer();

        List<Case> newCases = forCTSTestCases(test_cts.test_load_ctsFile(), lexer);
/*        System.out.printf("size of TestCases: %d%n", tc.tests().size()); // This will print correctly
        for (Case c: tc.tests()) {
            System.out.println("test = " + c);
        }*/
        TestCases tc = new TestCases(
                "Generated from JSONPath Compliance Test Suite to test tokenization of " + msg , newCases);
        writeJsonData(tc, fileName);
    }

    /**
     * Convert the CTSTestCases object graph in the argument to a TestCases object graph.
     * @param ctsTestCases
     * @return
     */
    static List<Case> forCTSTestCases(test_cts.CTSTestCases ctsTestCases, Lexer lexer) {
        List<Case> newCases = new ArrayList<>(ctsTestCases.tests().size());
         for (test_cts.CTSTestCase c: ctsTestCases.tests()) {
            // skip invalid tests for now
            if (c.isInvalid()) {
                // For invalid paths, we expect an exception.
                // We'll try to tokenize and record the exception class name.
                try {
                    List<Token> tokens = lexer.tokenize(c.jsonPath());
                    // Lexing succeeds if any valid grammar tokens are scanned, so most cases that fail parsing
                    // still pass lexing.
                    newCases.add(new Case(c.testName(), c.jsonPath(), tokens));
                } catch (JSONPathException e) {
                    // This is the expected outcome. Record the exception class name.
                    newCases.add(new Case(c.testName(), c.jsonPath(), e.getClass().getCanonicalName()));
                }
            } else {
                List<Token> tokens = new ArrayList<>(lexer.tokenize(c.jsonPath()));
                newCases.add(new Case(c.testName(), c.jsonPath(), tokens));
            }
         }
         return newCases;
    }


    ////////////////////////////////////////////////////////////////////
    /// LEXER TEST CASE GENERATION
    ///  Source - PJP test_lex.json
    ////////////////////////////////////////////////////////////////////

    /**
     * Generate pjp.lexer.jjplexer.json file
     * @param env
     */
    static void generateLexerTestCasesFromPJP(JSONPathEnvironment env, String fileName, String msg) {
        RulesBuilder rb = env.getRulesBuilder();
        Lexer lexer = env.getLexer();

        TestCases  testCases = loadPJPTestLexJsonFile("test_lex.json");
        List<Case> newCases = forLexer(testCases, lexer);

        TestCases newTestCases = new TestCases(
                "Generated from test_lex.json to test tokenization of " + msg, newCases);
        //System.out.println("New test cases:");
        //prettyPrint(tc);
        writeJsonData(newTestCases, fileName);
    }

    /**
     * Generate new test cases for the Lexer argument and original TestCases.
     * TestCases are loaded from an existing Lexer test case JSON file. For each existing test case, we get the
     * path and description of the test and run tokenize() on the argument Lexer with that path. Then we write the
     * results into a new TestCases object graph.
     * @param originalCases
     * @param lexer
     * @return
     */
    static List<Case> forLexer(TestCases originalCases, Lexer lexer) {
        List<Case> newCases = new ArrayList<>();
        // first insert the illegal symbol test
        newCases.add(new Case("illegal symbol", "%", "org.killeroonie.jsonpath.exception.JSONPathSyntaxException"));

        for (Case c: originalCases.tests()) {
            try {
                List<Token> tokens = lexer.tokenize(c.path);
                // Lexing succeeds if any valid grammar tokens are scanned, so most cases that fail parsing
                // still pass lexing.
                newCases.add(new Case(c.description(), c.path(), tokens));
            } catch (JSONPathException e) {
                // Record the exception class name.
                newCases.add(new Case(c.description(), c.path(), e.getClass().getCanonicalName()));
            }
        }
        return newCases;
    }

    static TestCases loadPJPTestLexJsonFile(final String fileName) {
        final ObjectMapper mapper = new ObjectMapper();

        JsonDeserializer<TokenKind> jsonDeserializer = new JsonDeserializer<TokenKind>() {
            @Override
            public TokenKind deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String name = p.getText();
                // Here we can tweak differences between the name of the TOKEN_XXX string constants in the Python version
                // (from where the test data originates) and our Enum TokenKind implementation.
                // Strip "TOKEN" prefix from the serialized name.
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
        try(InputStream inputStream = test_lex.class.getResourceAsStream(fileName)) {
            testCases = mapper.readValue(inputStream, new TypeReference<TestCases>() { });
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load " + fileName, e);
        }

        return testCases;
    }


    static void writeJsonData(TestCases testCases, String fileName) {
        System.out.println("writeJsonData: Writing to " + fileName);
        final ObjectMapper mapper = new ObjectMapper();

        // Mix-in to ignore only the derived getters on Token
        abstract class TokenIgnoreDerivedProps {
            @com.fasterxml.jackson.annotation.JsonIgnore abstract int getLineNumber();
            @com.fasterxml.jackson.annotation.JsonIgnore abstract int getColumnNumber();
        }
        mapper.addMixIn(Token.class, TokenIgnoreDerivedProps.class);
        // Custom serializer for TokenKind to output names like "TOKEN_ROOT" and handle historical aliases
        JsonSerializer<TokenKind> tokenKindSerializer = new JsonSerializer<>() {
            @Override
            public void serialize(TokenKind value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                String name = value.name();
                // Historical alias used by existing JSON test data
                if ("KEY_SELECTOR".equals(name)) {
                    name = "KEYS"; // Will be prefixed to TOKEN_KEYS
                }
                // If you want to output LIST_START instead of LBRACKET, add:
                // if ("LBRACKET".equals(name)) name = "LIST_START";
                gen.writeString("TOKEN_" + name);
            }
        };

        // Register serializers in a module used only for this write
        SimpleModule mod = new SimpleModule();
        mod.addSerializer(TokenKind.class, tokenKindSerializer);
        mapper.registerModule(mod);

        // Optional niceties
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // pretty-print
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // drop nulls if you want

        try (java.io.OutputStream os = java.nio.file.Files.newOutputStream(java.nio.file.Path.of(fileName))) {
            mapper.writeValue(os, testCases);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't write JSON to " + fileName, e);
        }
    }


    static void prettyPrint(TestCases testCases) {
          // pretty print to console
        final ObjectMapper mapper = new ObjectMapper();
        // Mix-in to ignore only the derived getters on Token
        abstract class TokenIgnoreDerivedProps {
            @com.fasterxml.jackson.annotation.JsonIgnore abstract int getLineNumber();
            @com.fasterxml.jackson.annotation.JsonIgnore abstract int getColumnNumber();
        }
        mapper.addMixIn(Token.class, TokenIgnoreDerivedProps.class);

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
    }

    public static void main(String[] args) {
//        generateLexerTestCasesFromPJP(new JJPEnv(), "pjp.lexer.jjplexer.json", "JJPLexer");
//        generateLexerTestCasesFromCTS(new JJPEnv(), "cts.jjplexer.json", "JJPLexer");

        generateLexerTestCasesFromPJP(new RFCEnv(), "pjp.lexer.rfc.json", "RFC lexer");
        generateLexerTestCasesFromCTS(new RFCEnv(), "cts.rfc.json", "RFC lexer");

    }


}
