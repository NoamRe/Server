package com.example.server;

import java.util.Stack;

public class Operations {
    private static final int RESULT_OK = 200;
    private static final int RESULT_CONFLICT = 409;
    private static final int REQUIRED_ARGUMENTS_BINARY = 2;
    private static final int REQUIRED_ARGUMENTS_UNARY = 1;
    private static String m_Json;
    private static int m_ResponseCode;
    private static String m_Operation;
    private static final Stack<Integer> m_Stack = new Stack<>();

    public static String Json() {
        return m_Json;
    }

    public static int ResponseCode() {
        return m_ResponseCode;
    }

    private static void setJsonResult(int i_Result) {
        m_Json = String.format("""
                {
                "result": %d
                }
                """, i_Result);
    }

    private static void setJsonError(String i_ErrorMessage) {
        m_Json = String.format("""
                {
                "error-message": "%s"
                }
                """, i_ErrorMessage);
    }

    private static void setResponseCode(int i_ResponseCode) {
        m_ResponseCode = i_ResponseCode;
    }

    public static void SetOperation(String i_Operation) {
        m_Operation = i_Operation;
    }

    private static boolean amountOfArgumentsCheck(int i_AmountOfArguments, int i_RequiredAmount, boolean i_IsStack) {
        boolean isLegalResult = false;

        if (i_AmountOfArguments < i_RequiredAmount) {
            if (i_IsStack) {
                setJsonError("Error: cannot implement operation " + m_Operation + ". It requires " + i_RequiredAmount + " arguments and the stack has only " + m_Stack.size() + " arguments");
            } else {
                setJsonError("Error: Not enough arguments to perform the operation " + m_Operation);
            }
        } else if (i_AmountOfArguments > i_RequiredAmount && !i_IsStack) {
            setJsonError("Error: Too many arguments to perform the operation " + m_Operation);
        } else {
            isLegalResult = true;
        }

        return isLegalResult;
    }

    private static int[] getStackArguments(int i_RequiredArguments) {
        int i = 0;
        int[] arguments = new int[i_RequiredArguments];

        for (; i < i_RequiredArguments; i++) {
            arguments[i] = m_Stack.pop();
        }

        return arguments;
    }

    public static void Plus(int[] i_Arguments) {
        boolean isStack = false;
        int length;

        if (i_Arguments == null) {
            length = m_Stack.size();
            isStack = true;
        } else {
            length = i_Arguments.length;
        }

        if (amountOfArgumentsCheck(length, REQUIRED_ARGUMENTS_BINARY, isStack)) {
            if (isStack) {
                i_Arguments = getStackArguments(REQUIRED_ARGUMENTS_BINARY);
            }

            int sum = 0;
            for (int argument : i_Arguments) {
                sum += argument;
            }
            setJsonResult(sum);
            setResponseCode(RESULT_OK);
        } else {
            setResponseCode(RESULT_CONFLICT);
        }
    }

    public static void Minus(int[] i_Arguments) {
        boolean isStack = false;
        int length;

        if (i_Arguments == null) {
            length = m_Stack.size();
            isStack = true;
        } else {
            length = i_Arguments.length;
        }

        if (amountOfArgumentsCheck(length, REQUIRED_ARGUMENTS_BINARY, isStack)) {
            if (isStack) {
                i_Arguments = getStackArguments(REQUIRED_ARGUMENTS_BINARY);
            }

            int sum = i_Arguments[0] * 2;
            for (int argument : i_Arguments) {
                sum -= argument;
            }
            setJsonResult(sum);
            setResponseCode(RESULT_OK);
        } else {
            setResponseCode(RESULT_CONFLICT);
        }
    }

    public static void Times(int[] i_Arguments) {
        boolean isStack = false;
        int length;

        if (i_Arguments == null) {
            length = m_Stack.size();
            isStack = true;
        } else {
            length = i_Arguments.length;
        }

        if (amountOfArgumentsCheck(length, REQUIRED_ARGUMENTS_BINARY, isStack)) {
            if (isStack) {
                i_Arguments = getStackArguments(REQUIRED_ARGUMENTS_BINARY);
            }

            int sum = 1;
            for (int argument : i_Arguments) {
                sum *= argument;
            }
            setJsonResult(sum);
            setResponseCode(RESULT_OK);
        } else {
            setResponseCode(RESULT_CONFLICT);
        }
    }

    public static void Divide(int[] i_Arguments) {
        boolean isStack = false;
        int length;

        if (i_Arguments == null) {
            length = m_Stack.size();
            isStack = true;
        } else {
            length = i_Arguments.length;
        }

        if (amountOfArgumentsCheck(length, REQUIRED_ARGUMENTS_BINARY, isStack)) {
            if (isStack) {
                i_Arguments = getStackArguments(REQUIRED_ARGUMENTS_BINARY);
            }

            boolean isDividedByZero = false;
            for (int i = 1; i < i_Arguments.length; i++) {
                if (i_Arguments[i] == 0) {
                    setJsonError("Error while performing operation Divide: division by 0");
                    setResponseCode(RESULT_CONFLICT);
                    isDividedByZero = true;
                    break;
                }
            }

            if (!isDividedByZero) {
                int sum = i_Arguments[0] * i_Arguments[0];

                for (int argument : i_Arguments) {
                    if (argument != 0) {
                        sum /= argument;
                    }
                }
                setJsonResult(sum);
                setResponseCode(RESULT_OK);
            }
        } else {
            setResponseCode(RESULT_CONFLICT);
        }
    }

    public static void Pow(int[] i_Arguments) {
        boolean isStack = false;
        int length;

        if (i_Arguments == null) {
            length = m_Stack.size();
            isStack = true;
        } else {
            length = i_Arguments.length;
        }

        if (amountOfArgumentsCheck(length, REQUIRED_ARGUMENTS_BINARY, isStack)) {
            if (isStack) {
                i_Arguments = getStackArguments(REQUIRED_ARGUMENTS_BINARY);
            }

            int sum = 0;
            for (int i = 0; i < i_Arguments.length - 1; i++) {
                sum = (int) Math.pow(i_Arguments[i], i_Arguments[i + 1]);
            }
            setJsonResult(sum);
            setResponseCode(RESULT_OK);
        } else {
            setResponseCode(RESULT_CONFLICT);
        }
    }

    public static void Abs(int[] i_Arguments) {
        boolean isStack = false;
        int length;

        if (i_Arguments == null) {
            length = m_Stack.size();
            isStack = true;
        } else {
            length = i_Arguments.length;
        }

        if (amountOfArgumentsCheck(length, REQUIRED_ARGUMENTS_UNARY, isStack)) {
            if (isStack) {
                i_Arguments = getStackArguments(REQUIRED_ARGUMENTS_UNARY);
            }

            setJsonResult(Math.abs(i_Arguments[0]));
            setResponseCode(RESULT_OK);
        } else {
            setResponseCode(RESULT_CONFLICT);
        }
    }

    public static void Fact(int[] i_Arguments) {
        boolean isStack = false;
        int length;

        if (i_Arguments == null) {
            length = m_Stack.size();
            isStack = true;
        } else {
            length = i_Arguments.length;
        }

        if (amountOfArgumentsCheck(length, REQUIRED_ARGUMENTS_UNARY, isStack)) {
            if (isStack) {
                i_Arguments = getStackArguments(REQUIRED_ARGUMENTS_UNARY);
            }

            int fact = 1;
            if (i_Arguments[0] < 0) {
                setJsonError("Error while performing operation Factorial: not supported for the negative number");
                setResponseCode(RESULT_CONFLICT);
            } else {
                for (int i = 1; i <= i_Arguments[0]; i++) {
                    fact *= i;
                }

                setJsonResult(fact);
                setResponseCode(RESULT_OK);
            }
        } else {
            setResponseCode(RESULT_CONFLICT);
        }
    }

    public static void UnknownOperation() {
        setJsonError("Error: unknown operation: " + m_Operation);
        setResponseCode(RESULT_CONFLICT);
    }

    public static void AddToStack(int[] i_Arguments) {
        for (int argument : i_Arguments) {
            m_Stack.push(argument);
        }
        setJsonResult(m_Stack.size());
        setResponseCode(RESULT_OK);
    }

    public static void GetStackSize() {
        setJsonResult(m_Stack.size());
        setResponseCode(RESULT_OK);
    }

    public static void DeleteFromStack(int i_AmountToDelete) {
        if (i_AmountToDelete > m_Stack.size()) {
            setJsonError("Error: cannot remove " + i_AmountToDelete + " from the stack. It has only " + m_Stack.size() + " arguments");
            setResponseCode(RESULT_CONFLICT);
        } else {
            for (int i = 0; i < i_AmountToDelete; i++) {
                m_Stack.pop();
            }

            setJsonResult(m_Stack.size());
            setResponseCode(RESULT_OK);
        }

    }
}