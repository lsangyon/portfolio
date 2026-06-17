package enshud.s2.parser;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class CreateSymbolTable {
    // 각 스코프에서의 심볼(변수) 테이블을 관리하기 위한 스택
    private Deque<Map<String, SymbolStorage>> symbolStack;

    // 생성자: 심볼 테이블 초기화 및 전역 스코프 추가
    public CreateSymbolTable(){
        symbolStack = new LinkedList<>();
        // 전역 스코프 초기화
        enterScope();
    }

   

 // 심볼(변수) 정보를 저장하는 내부 클래스
    public static class SymbolStorage {
        private String symbolName; // 변수 이름
        private String symbolType; // 변수 타입

        // 생성자: 변수 이름과 타입 초기화
        public SymbolStorage(String name, String type) {
            this.symbolName = name;
            this.symbolType = type;
        }

        // Getter 메서드 추가
        public String getSymbolName() {
            return symbolName;
        }

        public String getSymbolType() {
            return symbolType;
        }

        // 심볼 정보를 문자열로 반환
        @Override
        public String toString() {
            return String.format("name: %s, type: %s", symbolName, symbolType);
        }
    }

    // 심볼 타입을 업데이트 (타입이 없거나 배열 타입인 경우만 업데이트)
    public void updateSymbolType(String t) {
        for (SymbolStorage entry : symbolStack.peek().values()) {
            if (entry.symbolType == null) {
                entry.symbolType = t; // 타입이 없으면 새 타입 설정
            } else if (entry.symbolType.equals("Array")) {
                entry.symbolType = "Array " + t; // 배열 타입일 경우 타입 정보 추가
            }
        }
    }


    // 새로운 심볼(변수)을 현재 스코프에 등록
    public SymbolStorage registerSymbol(String n, String t){
        SymbolStorage newSymbol = new SymbolStorage(n, t); // 새 심볼 생성
        Map<String, SymbolStorage> currentScope = symbolStack.peek();
        if (currentScope != null) {
            currentScope.put(n, newSymbol); // 현재 스코프에 추가
        }
        return newSymbol;
    }
    
 // 모든 스코프에 있는 변수 이름과 타입 나열
    public void listAllVariablesWithType() {
        System.out.println("Variables and types in all scopes:");
        for (Map<String, SymbolStorage> scope : symbolStack) {
            for (SymbolStorage symbol : scope.values()) {
                System.out.println("- " + symbol.symbolName + " : " + symbol.symbolType);
            }
        }
    }

    // 현재 스코프의 변수 이름과 타입 나열
    public void listCurrentScopeVariablesWithType() {
        System.out.println("Variables and types in the current scope:");
        Map<String, SymbolStorage> currentScope = symbolStack.peek();
        if (currentScope != null) {
            for (SymbolStorage symbol : currentScope.values()) {
                System.out.println("- " + symbol.symbolName + " : " + symbol.symbolType);
            }
        }
    }
   
    // 변수 이름으로 심볼 타입 찾기 (스코프를 거슬러 올라가면서 검색)
    public String findSymbolType(String varName) {
        for (Map<String, SymbolStorage> scope : symbolStack) {
            SymbolStorage entry = scope.get(varName);
            if (entry != null) {
                return entry.symbolType; // 심볼 타입 반환
            }
        }
        return null; // 변수 이름이 없으면 null 반환
    }

    // 변수 이름이 현재 또는 전역 스코프에서 중복되는지 확인
    public boolean checkVariableDuplication(String varName, boolean checkGlobal){
        if (checkGlobal) { // 전역 스코프까지 확인할 경우
            for (Map<String, SymbolStorage> scope : symbolStack) {
                if (scope.containsKey(varName)) return true; // 변수 이름이 발견되면 true 반환
            }
        } else { // 현재 스코프에서만 확인할 경우
            Map<String, SymbolStorage> currentScope = symbolStack.peek();
            if (currentScope != null && currentScope.containsKey(varName)) return true;
        }
        return false; // 중복되지 않으면 false 반환
    }
    
    // 새로운 스코프를 추가
    public void enterScope() {
        symbolStack.push(new HashMap<>()); // 빈 맵을 스택에 추가
    }

    // 현재 스코프를 제거
    public void exitScope() {
        if (!symbolStack.isEmpty()) {
            symbolStack.pop(); // 스택에서 현재 스코프 제거
        }
    }

    // 변수를 중복 여부 확인 후 추가
    public void checkAndAddVariable(String varName, boolean isAssignment, String varType, String errorline) throws CallSementicError {
        if (checkVariableDuplication(varName, isAssignment)) {
        	
            throw new CallSementicError(errorline); // 중복 시 에러 발생
        }
        
        if (!isAssignment) {
            registerSymbol(varName, varType); // 중복이 아니면 변수 등록
        }
    }

    public void checkVariableExists(String varName, boolean isAssignment, String errorline, String parameterName) throws CallSementicError {
        
        boolean exists = checkVariableDuplication(varName, isAssignment);
        
        if (!exists) {
        	if (parameterName != null && parameterName.equals(varName)) {
        	    return;
        	}

            throw new CallSementicError(errorline); // 존재하지 않을 시 에러 발생
        }

    }


    // 관계 연산자 확인
    public static void relationOperator(String[][] words, int id) throws CallSyntaxError {
        if (OperatorClassCheckout.isRelationalOperator(words[id][1])) {
            return; // 관계 연산자면 정상 처리
        } else {
            throw new CallSyntaxError(words[id][3]); // 아니면 문법 오류 발생
        }
    }

    // 덧셈 연산자 확인
    public static String additionOperator(String[][] words, int id) throws CallSyntaxError {
        if (OperatorClassCheckout.isAdditionOperator(words[id][1])) {
            id++;
            return words[id - 1][0]; // 덧셈 연산자 반환
        } else {
            throw new CallSyntaxError(words[id][3]); // 문법 오류 발생
        }
    }

    // 곱셈 연산자 확인
    public static String multiplicationOperator(String[][] words, int id) throws CallSyntaxError {
        if (OperatorClassCheckout.isMultipleOperator(words[id][1])) {
            id++;
            return words[id - 1][0]; // 곱셈 연산자 반환
        } else {
            throw new CallSyntaxError(words[id][3]); // 문법 오류 발생
        }
    }
}
