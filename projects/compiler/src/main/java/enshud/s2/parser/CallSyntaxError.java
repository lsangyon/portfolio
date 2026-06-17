package enshud.s2.parser;

public class CallSyntaxError extends RuntimeException {
    public CallSyntaxError(String message) {
        super("Syntax error: line " + message);
    }
	
}