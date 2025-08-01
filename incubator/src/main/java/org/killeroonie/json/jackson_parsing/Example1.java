package org.killeroonie.json.jackson_parsing;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Example1 {

    static class FootballPlayer {
        private String name;
        private int number;

        public FootballPlayer() { }

        public FootballPlayer(String name, int number) {
            this.name = name;
            this.number = number;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        @Override
        public String toString() {
            return "FootballPlayer{" +
                    "name='" + name + '\'' +
                    ", number=" + number +
                    '}';
        }
    }

    static void serializeToString() throws JsonProcessingException {
        FootballPlayer footballPlayer = new FootballPlayer("Joe", 10);
        // we have a Java Object (POJO) and want to serialize it to the JSON text:
        //  { \"name\":\"Joe\", \"number\":10}
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(footballPlayer);
        // The above is the equivalent of Python: jsonString = json.dumps(footballPlayer)

        // The field names of the objects become the JSON keys
        System.out.println("Serialized football player: " + footballPlayer);
        System.out.println(jsonString);

    }

    static void serializeToFile() throws Exception {
        // We have a POJO and want to serialize it to a `.json` file
        FootballPlayer player =  new FootballPlayer("Tom", 30);
        System.out.println(player); // Tom, 30

        URL resourceUrl = Example1.class.getResource("");
        String savePath = (resourceUrl != null)
                ? resourceUrl.getPath() + "footballPlayer.json"
                : "footballPlayer.json";  // Fallback to the current directory.

        ObjectMapper mapper = new ObjectMapper();
        System.out.println("Writing to " + savePath);
        mapper.writeValue(new File(savePath), player);

        // mapper.writeValue() is equivalent to Python:
        //   with open(outfile_path, "x", encoding=UTF8, buffering=ONE_MEBIBYTE) as outfile:
        //      json.dump(test_file_dict, outfile, ensure_ascii=False, indent=4)

    }

    static class VolleyballPlayer {
        // Jackson requirements:
        //   these fields need to be public, or they need to have public getters/setters.
        public String name;
        public int number;
    }

    static void deserializeFromStringToPOJO() throws JsonParseException, JsonMappingException, IOException {
        // We have a JSON text and want to deserialize it to a POJO
        String jsonString = """
                { "name": "Sally", "number":20 }""";
        ObjectMapper mapper = new ObjectMapper();
        VolleyballPlayer player = mapper.readValue(jsonString, VolleyballPlayer.class);
        System.out.println("name="+player.name+", number=" + player.number);
        // like Python's json.loads(jsonString)
    }

    static void deserializeFromFileToPOJO() throws JsonParseException, JsonMappingException, IOException {
        URL resourceUrl = Example1.class.getResource("");
        String loadPath = (resourceUrl != null)
                ? resourceUrl.getPath() + "footballPlayer.json"
                : "footballPlayer.json";  // Fallback to the current directory.
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(loadPath);
        if (!file.exists()) {
            System.out.println("Whoa boy, file does not exist! " + file);
        }
        FootballPlayer player = mapper.readValue(new File(loadPath), FootballPlayer.class);
        System.out.println("deserializeFromFileToPOJO:");
        System.out.println(player);
    }

    static void htmlTest() {
        String html1 = """
              <p>First paragraph</p>
            <p>Second paragraph</p>
                <p>Third paragraph</p>
              """;
        System.out.println("html1=\n"+html1);
        String html2 = """
              <p>First paragraph</p>
            <p>Second paragraph</p>
                <p>Third paragraph</p>
            """;
        System.out.println("html2=\n"+html2);

        String html3 = """
              <p>First paragraph</p>
            <p>Second paragraph</p>
                <p>Third paragraph</p>
          """;
        System.out.println("html3=\n"+html3);

        String html4 = """
              <p>First paragraph</p>
            <p>Second paragraph</p>
                <p>Third paragraph</p>
                """;
        System.out.println("html4=\n"+html4);

    }

    static void serializeListToString() throws JsonProcessingException {
        List<String> list = new ArrayList<>();
        list.add("milk");
        list.add("oranges");
        list.add("light bulbs");

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(list);
        System.out.println("Serialized shopping list: " + jsonString);
        //console:
        //Serialized shopping list: ["milk","oranges","light bulbs"]
    }

    static void  serializeMapToString() throws JsonProcessingException {
        Map<String, String> capitalsMap = new HashMap<>(); // key=country, val=capital
        capitalsMap.put("France", "Paris");
        capitalsMap.put("Spain", "Madrid");
        capitalsMap.put("Japan", "Tokyo");

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(capitalsMap);
        System.out.println("Capitals map: " + jsonString);
        // console:
        // Capitals map: {"Japan":"Tokyo","France":"Paris","Spain":"Madrid"}
    }

    static void deserializeStringToList() throws JsonParseException, JsonMappingException, IOException {
        String jsonString = """
                ["milk","oranges","light bulbs"]""";

        ObjectMapper mapper = new ObjectMapper();
        List<String> list = mapper.readValue(jsonString, new TypeReference<List<String>>() {});
        System.out.println("deserializeStringToList:");
        System.out.println(list);
    }

    static void deserializeStringToMap() throws JsonParseException, JsonMappingException, IOException {
        String jsonString = """
                {
                  "model" : "Ford F150",
                  "engine" : {
                    "cylinders" : 8,
                    "horsepower" : 275
                  }
                }""";
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        System.out.println("deserializeStringToMap:");
        System.out.println(map);
        // console:
        // {model=Ford F150, engine={cylinders=8, horsepower=275}}

    }

    public static void main(String[] args) throws Exception {
//        serializeToString();
//        serializeToFile();
//        htmlTest();
//        deserializeFromStringToPOJO();
//        deserializeFromFileToPOJO();
//        serializeListToString();
//        serializeMapToString();
//        deserializeStringToList();
        deserializeStringToMap();
    }


}
