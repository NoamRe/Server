package com.example.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class Endpoints {
    private int m_RequestCounter = 1;
    private final String REQUEST_LOGGER_NAME = "request-logger";
    private final String STACK_LOGGER_NAME = "stack-logger";
    private final String INDEPENDENT_LOGGER_NAME = "independent-logger";
    private final String DATE_FORMAT = "dd-MM-yyyy hh:mm:ss.sss";
    private final String REQUEST_FILEPATH = "/requests.log";
    private final String STACK_FILEPATH = "/stack.log";
    private final String INDEPENDENT_FILEPATH = "/independent.log";
    private final String INFO_LEVEL = "INFO";
    private final String DEBUG_LEVEL = "DEBUG";
    private final String ERROR_LEVEL = "ERROR";
    private final String INDEPENDENT_ENDPOINT = "/independent/calculate";
    private final String SIZE_ENDPOINT = "/stack/size";
    private final String ARGUMENTS_ENDPOINT = "/stack/arguments";
    private final String OPERATE_ENDPOINT = "/stack/operate";
    private final String LEVEL_ENDPOINT = "/logs/level";


    private String getLogMessage(String i_Loglevel, String i_LogMessage) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
        String dateTime = simpleDateFormat.format(new Date());

        return dateTime + " " + i_Loglevel + ": " + i_LogMessage + " | request #" + m_RequestCounter;
    }

    private void getStackLog(String i_Endpoint, boolean i_IsDelete,
                             StringBuilder i_InfoMessage, StringBuilder i_DebugMessage, String[] i_Args) {
        switch (i_Endpoint) {
            case SIZE_ENDPOINT -> {
                i_InfoMessage.append("Stack size is ").append(i_Args[0]);
                i_DebugMessage.append("Stack content (first == top): [").append(i_Args[1]).append("]");
            }
            case ARGUMENTS_ENDPOINT -> {
                if (i_IsDelete) {
                    i_InfoMessage.append("Removing total ").
                            append(i_Args[0]).append(" argument(s) from the stack | Stack size: ").
                            append(i_Args[1]);
                } else {
                    i_InfoMessage.append("Adding total of ").append(i_Args[0]).
                            append(" argument(s) to the stack | Stack size: ").append(i_Args[3]);
                    i_DebugMessage.append("Adding arguments: ").
                            append(i_Args[2]).
                            append(" | Stack size before ").
                            append(i_Args[1]).append(" | stack size after ").append(i_Args[3]);
                }
            }
            case OPERATE_ENDPOINT -> {
                i_InfoMessage.append("Performing operation ").
                        append(i_Args[0]).
                        append(". Result is ").
                        append(i_Args[1]).append(" | stack size: ").append(i_Args[2]);
                i_DebugMessage.append("Performing operation: ").
                        append(i_Args[0]).
                        append("(").
                        append(i_Args[3]).append(") = ").append(i_Args[1]);
            }
        }
    }

    private void requestLogger(String i_Endpoint, String i_RequestType, long i_RequestTime) throws IOException {
        Logger logger = Logger.getLogger(REQUEST_LOGGER_NAME);
        String infoMessage = "Incoming request | #" + m_RequestCounter + " | resource: " + i_Endpoint +
                " | HTTP Verb " + i_RequestType;
        String debugMessage = "request #" + m_RequestCounter + " duration: " + i_RequestTime + "ms";

        logger.addHandler(new FileHandler(REQUEST_FILEPATH));
        logger.setLevel(Level.INFO);
        logger.log(Level.INFO, getLogMessage(INFO_LEVEL, infoMessage));
        logger.log(Level.FINE, getLogMessage(DEBUG_LEVEL, debugMessage));
    }

    private void stackLogger(String i_Endpoint, boolean i_IsFailure,
                             boolean i_IsDelete, String[] i_Args) throws IOException {
        Logger logger = Logger.getLogger(STACK_LOGGER_NAME);

        logger.addHandler(new FileHandler(STACK_FILEPATH));
        logger.setLevel(Level.INFO);
        if (i_IsFailure) {
            String errorMessage = "Server encountered an error ! message: " + Operations.GetErrorMessage();
            logger.log(Level.SEVERE, getLogMessage(ERROR_LEVEL, errorMessage));
        } else {
            StringBuilder infoMessage = new StringBuilder();
            StringBuilder debugMessage = new StringBuilder();
            getStackLog(i_Endpoint, i_IsDelete, infoMessage, debugMessage, i_Args);
            logger.log(Level.INFO, getLogMessage(INFO_LEVEL, String.valueOf(infoMessage)));
            logger.log(Level.FINE, getLogMessage(DEBUG_LEVEL, String.valueOf(debugMessage)));
        }
    }

    private void independentLogger(String[] i_Args, boolean i_IsFailure) throws IOException {
        Logger logger = Logger.getLogger(INDEPENDENT_LOGGER_NAME);

        logger.addHandler(new FileHandler(INDEPENDENT_FILEPATH));
        logger.setLevel(Level.FINE);
        if (i_IsFailure) {
            String errorMessage = "Server encountered an error ! message: " + Operations.GetErrorMessage();
            logger.log(Level.SEVERE, getLogMessage(ERROR_LEVEL, errorMessage));
        } else {
            String infoMessage = "Performing operation " + i_Args[0] + ". Result is " + i_Args[1];
            String debugMessage = "Performing operation: " + i_Args[0] + "(" + i_Args[2] + ") = " + i_Args[1];
            logger.log(Level.INFO, getLogMessage(INFO_LEVEL, infoMessage));
            logger.log(Level.FINE, getLogMessage(DEBUG_LEVEL, debugMessage));
        }
    }

    @PostMapping(INDEPENDENT_ENDPOINT)
    public ResponseEntity<String> IndependentCalculation(@RequestBody String i_Json) throws IOException {
        long startTime = System.nanoTime();
        boolean isFailure;
        String[] args = new String[3];
        Gson gson = new Gson();
        String operation = gson.fromJson(i_Json, JsonObject.class).get("operation").getAsString();
        JsonArray jsonArray = gson.fromJson(i_Json, JsonObject.class).get("arguments").getAsJsonArray();
        int[] arguments = gson.fromJson(jsonArray, int[].class);
        int res;

        m_RequestCounter++;
        Operations.SetOperation(operation);
        switch (operation.toLowerCase()) {
            case "plus" -> res = Operations.Plus(arguments);
            case "minus" -> res = Operations.Minus(arguments);
            case "times" -> res = Operations.Times(arguments);
            case "divide" -> res = Operations.Divide(arguments);
            case "pow" -> res = Operations.Pow(arguments);
            case "abs" -> res = Operations.Abs(arguments);
            case "fact" -> res = Operations.Fact(arguments);
            default -> res = Operations.UnknownOperation();
        }

        isFailure = Operations.GetResponseCode() == 409;
        args[0] = operation;
        args[1] = String.valueOf(res);
        args[2] = Arrays.toString(arguments);
        requestLogger(INDEPENDENT_ENDPOINT, "POST", System.nanoTime() - startTime);
        independentLogger(args, isFailure);

        return new ResponseEntity<>(Operations.GetJson(), HttpStatusCode.valueOf(Operations.GetResponseCode()));
    }

    @GetMapping(SIZE_ENDPOINT)
    public ResponseEntity<String> GetSize() throws IOException {
        long startTime = System.nanoTime();
        boolean isFailure;
        String[] args = new String[2];

        m_RequestCounter++;
        args[0] = String.valueOf(Operations.GetStackSize());
        args[1] = Operations.GetStackArguments();
        isFailure = Operations.GetResponseCode() == 409;
        requestLogger(SIZE_ENDPOINT, "GET", System.nanoTime() - startTime);
        stackLogger(SIZE_ENDPOINT, isFailure, false, args);

        return new ResponseEntity<>(Operations.GetJson(), HttpStatusCode.valueOf(Operations.GetResponseCode()));
    }

    @PutMapping(ARGUMENTS_ENDPOINT)
    public ResponseEntity<String> AddArguments(@RequestBody String i_Json) throws IOException {
        long startTime = System.nanoTime();
        boolean isFailure;
        Gson gson = new Gson();
        JsonArray jsonArray = gson.fromJson(i_Json, JsonObject.class).get("arguments").getAsJsonArray();
        int[] arguments = gson.fromJson(jsonArray, int[].class);
        String[] args = new String[4];

        args[0] = String.valueOf(arguments.length);
        args[1] = String.valueOf(Operations.StackSize());
        args[2] = Arrays.toString(arguments);
        m_RequestCounter++;
        Operations.AddToStack(arguments);
        args[3] = args[1] = String.valueOf(Operations.StackSize());
        isFailure = Operations.GetResponseCode() == 409;
        requestLogger(ARGUMENTS_ENDPOINT, "PUT", System.nanoTime() - startTime);
        stackLogger(ARGUMENTS_ENDPOINT, isFailure, false, args);

        return new ResponseEntity<>(Operations.GetJson(), HttpStatusCode.valueOf(Operations.GetResponseCode()));
    }

    @GetMapping(OPERATE_ENDPOINT)
    public ResponseEntity<String> PerformOperation(@RequestParam String operation) throws IOException {
        long startTime = System.nanoTime();
        boolean isFailure;
        int[] operationArguments = null;
        String[] args = new String[4];
        int res;

        m_RequestCounter++;
        Operations.SetOperation(operation);
        switch (operation.toLowerCase()) {
            case "plus" -> res = Operations.Plus(operationArguments);
            case "minus" -> res = Operations.Minus(operationArguments);
            case "times" -> res = Operations.Times(operationArguments);
            case "divide" -> res = Operations.Divide(operationArguments);
            case "pow" -> res = Operations.Pow(operationArguments);
            case "abs" -> res = Operations.Abs(operationArguments);
            case "fact" -> res = Operations.Fact(operationArguments);
            default -> res = Operations.UnknownOperation();
        }

        args[0] = operation;
        args[1] = String.valueOf(res);
        args[2] = String.valueOf(Operations.StackSize());
        args[3] = Arrays.toString(operationArguments);
        isFailure = Operations.GetResponseCode() == 409;
        requestLogger(OPERATE_ENDPOINT, "GET", System.nanoTime() - startTime);
        stackLogger(OPERATE_ENDPOINT, isFailure, false, args);

        return new ResponseEntity<>(Operations.GetJson(), HttpStatusCode.valueOf(Operations.GetResponseCode()));
    }

    @DeleteMapping(ARGUMENTS_ENDPOINT)
    public ResponseEntity<String> DeleteArgument(@RequestParam int count) throws IOException {
        long startTime = System.nanoTime();
        boolean isFailure;
        String[] args = new String[2];

        m_RequestCounter++;
        Operations.DeleteFromStack(count);
        isFailure = Operations.GetResponseCode() == 409;
        args[0] = String.valueOf(count);
        args[1] = String.valueOf(Operations.StackSize());
        requestLogger(ARGUMENTS_ENDPOINT, "DELETE", System.nanoTime() - startTime);
        stackLogger(ARGUMENTS_ENDPOINT, isFailure, true, args);

        return new ResponseEntity<>(Operations.GetJson(), HttpStatusCode.valueOf(Operations.GetResponseCode()));
    }

    @GetMapping(LEVEL_ENDPOINT)
    public ResponseEntity<String> GetLogLevel(@RequestParam("logger-name") String name) {
        return new ResponseEntity<>(String.valueOf(Logger.getLogger(name).getLevel()).toUpperCase(),
                HttpStatusCode.valueOf(200));
    }

    @PutMapping(LEVEL_ENDPOINT)
    public ResponseEntity<String> GetLogLevel(@RequestParam("logger-name") String name,
                                              @RequestParam("logger-level") String level) {
        Level newLevel = null;

        switch (level) {
            case INFO_LEVEL -> newLevel = Level.INFO;
            case DEBUG_LEVEL -> newLevel = Level.FINE;
            case ERROR_LEVEL -> newLevel = Level.SEVERE;
        }

        Logger.getLogger(name).setLevel(newLevel);

        return new ResponseEntity<>(String.valueOf(Logger.getLogger(name).getLevel()).toUpperCase(),
                HttpStatusCode.valueOf(200));
    }
}