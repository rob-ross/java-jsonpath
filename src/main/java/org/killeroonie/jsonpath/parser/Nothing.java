package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.JSONPathNodeList;

import java.util.Objects;

/**
 * The special RFC9535 result "Nothing".
 */
public class Nothing {

    private static class Holder {
        private static final Nothing INSTANCE = new Nothing();
    }


    private Nothing() {}

    public static Nothing instance() {
        return Holder.INSTANCE;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof JSONPathNodeList nl && nl.isEmpty()) {
            return true;
        }
        return other instanceof Nothing;
    }


    @Override
    public String toString() {
        return "NOTHING";
    }
}
