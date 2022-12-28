package com.example.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class Endpoints {
    private int requestCounter = 1;

    private String getLogMessage(String i_Loglevel, String i_LogMessage) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss.sss");
        String dateTime = simpleDateFormat.format(new Date());

        return dateTime + " " + i_Loglevel + ": " + i_LogMessage + " | request #" + requestCounter;
    }

    private void requestLogger(String i_Endpoint, String i_RequestType, long i_RequestTime) throws IOException {
        Logger logger = Logger.getLogger("request-logger");
        String infoMessage = "Incoming request | #" + requestCounter + " | resource: " + i_Endpoint +
                " | HTTP Verb " + i_RequestType;
        String debugMessage = "request #" + requestCounter + " duration: " + i_RequestTime + "ms";

        logger.addHandler(new FileHandler("/requests.log"));
        logger.setLevel(Level.INFO);
        logger.log(Level.INFO, getLogMessage("INFO", infoMessage));
        logger.log(Level.FINE, getLogMessage("DEBUG", debugMessage));
    }

    @PostMapping("/independent/calculate")
    public ResponseEntity<String> IndependentCalculation(@RequestBody String i_Json) throws IOException {
        long startTime = System.nanoTime();
        Gson gson = new Gson();
        String operation = gson.fromJson(i_Json, JsonObject.class).get("operation").getAsString();
        JsonArray jsonArray = gson.fromJson(i_Json, JsonObject.class).get("arguments").getAsJsonArray();
        int[] arguments = gson.fromJson(jsonArray, int[].class);

        requestCounter++;
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

        requestLogger("/independent/calculate", "POST", System.nanoTime() - startTime);

        return new ResponseEntity<>(Operations.GetJson(), HttpStatusCode.valueOf(Operations.GetResponseCode()));
    }

    @GetMapping("/stack/size")
    public ResponseEntity<String> GetSize() throws IOException {
        long startTime = System.nanoTime();

        requestCounter++;
        Operations.GetStackSize();
        requestLogger("/stack/size", "GET", System.nanoTime() - startTime);

        return new ResponseEntity<>(Operations.GetJson(), HttpStatusCode.valueOf(Operations.GetResponseCode()));
    }

    @PutMapping("/stack/arguments")
    public ResponseEntity<String> AddArguments(@RequestBody String i_Json) throws IOException {
        long startTime = System.nanoTime();
        Gson gson = new Gson();
        JsonArray jsonArray = gson.fromJson(i_Json, JsonObject.class).get("arguments").getAsJsonArray();
        int[] arguments = gson.fromJson(jsonArray, int[].class);

        requestCounter++;
        Operations.AddToStack(arguments);
        requestLogger("/stack/arguments", "PUT", System.nanoTime() - startTime);

        return new ResponseEntity<>(Operations.GetJson(), HttpStatusCode.valueOf(Operations.GetResponseCode()));
    }

    @GetMapping("/stack/operate")
    public ResponseEntity<String> PerformOperation(@RequestParam String operation) throws IOException {
        long startTime = System.nanoTime();
        requestCounter++;
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

        requestLogger("/stack/operate", "GET", System.nanoTime() - startTime);

        return new ResponseEntity<>(Operations.GetJson(), HttpStatusCode.valueOf(Operations.GetResponseCode()));
    }

    @DeleteMapping("/stack/arguments")
    public ResponseEntity<String> DeleteArgument(@RequestParam int count) throws IOException {
        long startTime = System.nanoTime();

        requestCounter++;
        Operations.DeleteFromStack(count);
        requestLogger("/stack/arguments", "DELETE", System.nanoTime() - startTime);

        return new ResponseEntity<>(Operations.GetJson(), HttpStatusCode.valueOf(Operations.GetResponseCode()));
    }
}