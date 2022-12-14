package com.example.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            default -> Operations.UnknownOperation();
        }

        return new ResponseEntity<>(Operations.Json(), HttpStatusCode.valueOf(Operations.ResponseCode()));
    }

    @GetMapping("/stack/size")
    public ResponseEntity<String> GetSize() {
        Operations.GetStackSize();

        return new ResponseEntity<>(Operations.Json(), HttpStatusCode.valueOf(Operations.ResponseCode()));
    }

    @PutMapping("/stack/arguments")
    public ResponseEntity<String> AddArguments(@RequestBody String i_Json) {
        Gson gson = new Gson();
        JsonArray jsonArray = gson.fromJson(i_Json, JsonObject.class).get("arguments").getAsJsonArray();
        int[] arguments = gson.fromJson(jsonArray, int[].class);

        Operations.AddToStack(arguments);

        return new ResponseEntity<>(Operations.Json(), HttpStatusCode.valueOf(Operations.ResponseCode()));
    }

    @GetMapping("/stack/operate")
    public ResponseEntity<String> PerformOperation(@RequestParam String operation) {
        Operations.SetOperation(operation);
        switch (operation.toLowerCase()) {
            case "plus" -> Operations.Plus(null);
            case "minus" -> Operations.Minus(null);
            case "times" -> Operations.Times(null);
            case "divide" -> Operations.Divide(null);
            case "pow" -> Operations.Pow(null);
            case "abs" -> Operations.Abs(null);
            case "fact" -> Operations.Fact(null);
            default -> Operations.UnknownOperation();
        }

        return new ResponseEntity<>(Operations.Json(), HttpStatusCode.valueOf(Operations.ResponseCode()));
    }

    @DeleteMapping("/stack/arguments")
    public ResponseEntity<String> DeleteArgument(@RequestParam int count) {
        Operations.DeleteFromStack(count);

        return new ResponseEntity<>(Operations.Json(), HttpStatusCode.valueOf(Operations.ResponseCode()));
    }
}
