package org.killeroonie.json.jackson_parsing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class NestedParsingExample {

    static void serializeCarToString() throws JsonProcessingException {

        Car car = new Car();
        car.model = "Ford F150";
        car.engine.cylinders = 8;
        car.engine.horsepower = 275;

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // for "pretty printing"
        String jsonString = mapper.writeValueAsString(car);
        System.out.println("serializeToString:");
        System.out.println(jsonString);
    }

    static void deserializeStringToCar() throws JsonProcessingException {
        String jsonString = """
                {
                  "model" : "Ford F150",
                  "engine" : {
                    "cylinders" : 8,
                    "horsepower" : 275
                  }
                }""";
        ObjectMapper mapper = new ObjectMapper();
        Car car = mapper.readValue(jsonString, Car.class);
        System.out.println("deserializeStringToCar:");
        System.out.println(car);
        // consoled:

    }


    public static void main(String[] args) throws JsonProcessingException {
        serializeCarToString();
        deserializeStringToCar();
    }


    static class Car {
        public String model;
        public Engine engine =  new Engine();

        @Override
        public String toString() {
            return "Car{" +
                    "model='" + model + '\'' +
                    ", engine=" + engine +
                    '}';
        }
    }
    static class Engine {
        public int cylinders;
        public int horsepower;

        @Override
        public String toString() {
            return "Engine{" +
                    "cylinders='" + cylinders + '\'' +
                    ", horsepower=" + horsepower +
                    '}';
        }
    }

}
