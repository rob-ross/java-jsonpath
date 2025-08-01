package org.killeroonie.json.jackson_parsing;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AnnotationExample1 {

    static void deserializeStringToCar() throws JsonProcessingException {
        String jsonString = """
                { "brandName": "Honda", "type": "Accord", "color": "red" }""";
        System.out.println("jsonString=" + jsonString);
        ObjectMapper mapper = new ObjectMapper();
        Car car = mapper.readValue(jsonString, Car.class);
        System.out.println("deserializeStringToCar:");
        System.out.println(car);
    }


    static void serializeToStringWithJsonIgnore() throws JsonProcessingException {
        Apple apple = new Apple();
        apple.countryOfOrigin = "United States";
        apple.color = "red";
        apple.quantity = 10;

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(apple);
        // { "color": "red", "quantity": 10 }
        // notice how the JSON output string has no `"country":"United States"`
        System.out.println("serializeToStringWithJsonIgnore:");
        System.out.println(jsonString);
    }

    static void deserializeToStringWithJsonIgnore() throws JsonProcessingException {
        String jsonString = """
                { "countryOfOrigin": "Brazil", "color": "yellow", "quantity": 20}""";
        ObjectMapper mapper = new ObjectMapper();
        Banana banana = mapper.readValue(jsonString, Banana.class);
        System.out.println("deserializeToStringWithJsonIgnore:");
        System.out.println(banana);
        // Notice how the quantity field did not get updated to 20.

    }

    public static void main(String[] args) throws JsonProcessingException {
//        deserializeStringToCar();
//        serializeToStringWithJsonIgnore();
        deserializeToStringWithJsonIgnore();
    }
}

class Banana {
    public String countryOfOrigin;
    public String color;
    @JsonIgnore
    public int quantity = 4;
    public boolean inSeason = true;

    @Override
    public String toString() {
        return "Banana{" +
                "countryOfOrigin='" + countryOfOrigin + '\'' +
                ", color='" + color + '\'' +
                ", quantity=" + quantity +
                ", inSeason=" + inSeason +
                '}';
    }
}

class Apple {
    @JsonIgnore
    public String countryOfOrigin;
    public String color;
    public int quantity;
}


class Car {
    // Map this field to the JSON key-value pair that has
    // "brand", "brandName", "brand_name", or "myBrandName" as its key.
    @JsonAlias({"brandName", "brand_name", "myBrandName"})
    public String brand;
    // Map this field to the JSON key-value pair that has
    // "model" or "type" as its key.
    @JsonAlias({"type"})
    public String model;
    public String color;

    @Override
    public String toString() {
        return "Car{" +
                "brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
