package enshud.s4.compiler;

import java.util.HashSet;
import java.util.Set;

public class OperatorClassCheckout {
    private static final Set<String> NUMERIC_ADDITION_OPERATORS = new HashSet<>(Set.of("SPLUS", "SMINUS","+","-"));
    private static final Set<String> LOGIC_ADDITION_OPERATORS = new HashSet<>(Set.of("SOR","or"));
    private static final Set<String> RELATIONAL_OPERATORS = new HashSet<>(Set.of("SEQUAL", "SNOTEQUAL", "SLESS", "SLESSEQUAL", "SGREATEQUAL", "SGREAT"));
  
    private static final Set<String> STATEMENTS = new HashSet<>(Set.of("SIF", "SWHILE", "SIDENTIFIER", "SREADLN", "SWRITELN", "SBEGIN"));
    private static final Set<String> NUMERIC_MULTIPLE_OPERATORS = new HashSet<>(Set.of("SSTAR", "SDIVD", "SMOD", "*", "/", "div", "mod"));
    private static final Set<String> LOGIC_MULTIPLE_OPERATORS = new HashSet<>(Set.of("and", "SAND"));
    private static final Set<String> STANDARDTYPE = new HashSet<>(Set.of("SINTEGER", "SCHAR", "SBOOLEAN"));
    private static final Set<String> CONSTANT = new HashSet<>(Set.of("STRUE", "SFALSE", "SCONSTANT", "SSTRING"));
    public static final Set<String> WORDTYPE = new HashSet<>(Set.of("SINTEGER", "SCHAR", "SBOOLEAN"));
    

    // 주어진 토큰이 특정 연산자 집합에 포함되는지 체크
    private static boolean isOperator(String token, Set<String> operators) {
        return operators.contains(token);
    }

    // 덧셈 연산자 체크
    public static boolean isAdditionOperator(String token) {
    	 return NUMERIC_ADDITION_OPERATORS.contains(token) || LOGIC_ADDITION_OPERATORS.contains(token);
    }

 
    public static boolean isNumericAdditionOperator(String token) {
    	 return NUMERIC_ADDITION_OPERATORS.contains(token);
    }
    
    public static boolean isLogicAdditionOperator(String token) {
   	 return LOGIC_ADDITION_OPERATORS.contains(token);
   }
    
    // 관계 연산자 체크
    public static boolean isRelationalOperator(String token) {
        return isOperator(token, RELATIONAL_OPERATORS);
    }

    // 유효한 문장 체크
    public static boolean isValidStatement(String token) {
        return isOperator(token, STATEMENTS);
    }

    // 곱셈 연산자 체크
    public static boolean isMultipleOperator(String token) {
    	return NUMERIC_MULTIPLE_OPERATORS.contains(token) || LOGIC_MULTIPLE_OPERATORS.contains(token);
    }
    
    public static boolean isNumericMultipleOperator(String token) {
    	return NUMERIC_MULTIPLE_OPERATORS.contains(token);
    }
    
    public static boolean isLogicMultipleOperator(String token) {
    	return LOGIC_MULTIPLE_OPERATORS.contains(token);
    }
    
    // 표준형체크
    public static boolean isStandardType(String token) {
        return isOperator(token, STANDARDTYPE);
    }
    // 정수체크
    public static boolean isConstatnt(String token) {
        return isOperator(token, CONSTANT);
    }
    
    public static boolean isWordType(String token) {
        return isOperator(token, WORDTYPE);
    }
}

