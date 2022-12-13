package com.example.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Endpoints {
    @PostMapping("/independent/calculate")
    public ResponseEntity<String> IndependentCalculation(@RequestBody String i_Json) {
        Gson gson = new Gson();
        String operation = gson.fromJson(i_Json, JsonObject.class).get("operation").getAsString();
        JsonArray jsonArray = gson.fromJson(i_Json, JsonObject.class).get("arguments").getAsJsonArray();
        int[] arguments = gson.fromJson(jsonArray, int[].class);
        Operations.SetOperation(operation);
        switch (operation.toLowerCase()) {
            case "plus" -> Operations.Plus(arguments);
            case "minus" -> Operations.Minus(arguments);
            case "times" -> Operations.Times(arguments);
            case "divide" -> Operations.Divide(arguments);
            case "pow" -> Operations.Pow(arguments);
            case "abs" -> Operations.Abs(arguments);
            case "fact" -> Operations.Fact(arguments);
            default -> Operations.UnkownOperation();
        }
        return new ResponseEntity<>(Operations.Json(), HttpStatusCode.valueOf(Operations.ResponseCode()));
    }
}
