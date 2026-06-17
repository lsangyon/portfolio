package enshud.s2.parser;

public class CallSementicError extends RuntimeException {
    public CallSementicError(String message) {
        super("Semantic error: line " + message);
    }
private static final long serialVersionUID = 1L;

	
}