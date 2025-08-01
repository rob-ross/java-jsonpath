package org.killeroonie.stream_tests;

import java.util.ArrayList;
import java.util.List;

public class Example1 {

    static void t1() {
        List<Integer> numbers = List.of(1, 2, 3, 4);
        numbers.forEach(System.out::println);
        List<String> a = List.of("a", "b", "c");
        a.forEach(System.out::println);
        List<String> foo = List.of("a", "b", "c");
    }

    public static void main(String[] args) {
        t1();
    }

}
