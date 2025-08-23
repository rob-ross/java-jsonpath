package org.killeroonie.jsonpath;

import java.util.List;

public abstract class FilterFunction {

    public enum ExpressionType {
        VALUE, LOGICAL, NODES;
    }

    public abstract List<ExpressionType> getArgTypes();

    public abstract ExpressionType getReturnType();

}
