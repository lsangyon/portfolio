package enshud.s1.lexer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConverterPro {
	
	
	private void initializeTokenMap() {
        tokenMap = new LinkedHashMap<>();
        tokenMap.put("and", "SAND");
        tokenMap.put("array", "SARRAY");
        tokenMap.put("begin", "SBEGIN");
        tokenMap.put("boolean", "SBOOLEAN");
        tokenMap.put("char", "SCHAR");
        tokenMap.put("div", "SDIVD");
        tokenMap.put("do", "SDO");
        tokenMap.put("else", "SELSE");
        tokenMap.put("end", "SEND");
        tokenMap.put("false", "SFALSE");
        tokenMap.put("if", "SIF");
        tokenMap.put("integer", "SINTEGER");
        tokenMap.put("mod", "SMOD");
        tokenMap.put("not", "SNOT");
        tokenMap.put("of", "SOF");
        tokenMap.put("or", "SOR");
        tokenMap.put("procedure", "SPROCEDURE");
        tokenMap.put("program", "SPROGRAM");
        tokenMap.put("readln", "SREADLN");
        tokenMap.put("then", "STHEN");
        tokenMap.put("true", "STRUE");
        tokenMap.put("var", "SVAR");
        tokenMap.put("while", "SWHILE");
        tokenMap.put("writeln", "SWRITELN");
        tokenMap.put("=", "SEQUAL");
        tokenMap.put("<>", "SNOTEQUAL");
        tokenMap.put("<", "SLESS");
        tokenMap.put("<=", "SLESSEQUAL");
        tokenMap.put(">=", "SGREATEQUAL");
        tokenMap.put(">", "SGREAT");
        tokenMap.put("+", "SPLUS");
        tokenMap.put("-", "SMINUS");
        tokenMap.put("*", "SSTAR");
        tokenMap.put("(", "SLPAREN");
        tokenMap.put(")", "SRPAREN");
        tokenMap.put("[", "SLBRACKET");
        tokenMap.put("]", "SRBRACKET");
        tokenMap.put(";", "SSEMICOLON");
        tokenMap.put(":", "SCOLON");
        tokenMap.put("..", "SRANGE");
        tokenMap.put(":=", "SASSIGN");
        tokenMap.put(",", "SCOMMA");
        tokenMap.put(".", "SDOT");
        tokenMap.put("/", "SDIVD");
    }
	private Map<String, String> tokenMap;
	private String inputString; 
	private int lineNumber;
	private SpecailizeFunc specializeFunc = new SpecailizeFunc();
	
	public ConverterPro(String input, int lineNum) {
        this.inputString = input;
        this.lineNumber = lineNum;
        initializeTokenMap();  // Map 초기화
    }
	
	
	public String convert() {
	    char[] Input = inputString.toCharArray();
	    int index = 1;
	    char currentChar = 0, nextChar = 0;
	    SpecailizeFunc.State currentState = SpecailizeFunc.State.START;
	    SpecailizeFunc.State pastState = currentState;
	    StringBuilder tsStrings = new StringBuilder(); // StringBuilder 사용
	    StringBuilder tmp = new StringBuilder(); // StringBuilder 사용

	    if (inputString.length() == 0) {
	        return null; // 입력 문자열이 비어 있는 경우 null 반환
	    }

	    currentChar = Input[0]; // 첫 번째 문자 할당
	    currentState = specializeFunc.stateTransition(currentChar, currentState); // 상태 전이 수행

	    // 입력 문자열이 1자 이상인 경우 다음 문자 할당
	    if (inputString.length() > 1) {
	        nextChar = Input[1]; // 두 번째 문자 할당
	    }
	    
	    

	    while (true) {
	    	// 현재 상태가 중요하고 현재 문자가 공백인 동안 계속 반복
	    	while (isSignificantState(currentState) && isWhitespace(currentChar)) {
	    	    // 다음 문자 가져오기
	    	    currentChar = Input[index];
	    	    // 상태 전환 수행
	    	    currentState = specializeFunc.stateTransition(currentChar, SpecailizeFunc.State.START);	    	    
	    	    // 현재 상태를 이전 상태에 저장
	    	    pastState = currentState;
	    	    // 인덱스 증가
	    	    index++;
	    	    // 다음 문자 계산 (문자열의 끝을 고려)
	    	    if (index < inputString.length()) {
	    	        nextChar = Input[index];
	    	    } else {
	    	        nextChar = 0; // 다음 문자가 없을 경우 처리
	    	    }
	    	}

	    	// 상태 전환 및 문자열 처리
	    	currentState = specializeFunc.stateTransition(nextChar, currentState);
	    	tmp.append(currentChar); // StringBuilder에 추가

	    	// 상태가 COMPLETE일 경우 처리
	    	if (currentState == SpecailizeFunc.State.COMPLETE) {
	    	    tsStrings.append(printOut(tmp.toString(), pastState)); // 출력 처리
	    	    currentState = SpecailizeFunc.State.START; // 상태 초기화
	    	    tmp.setLength(0); // StringBuilder 초기화
	    	}

	    	// 다음 문자 가져오기
	    	currentChar = Input[index++];
	    	    
	    	// 상태가 COMMENT_END일 경우 과거 상태 업데이트
	    	if (currentState == SpecailizeFunc.State.COMMENT_END) {
	    	    pastState = currentState;
	    	}

	    	if (index == inputString.length()) {
	    	    // 입력 문자열이 끝난 경우 처리
	    	    if (tmp.length() == 0) {
	    	        currentState = specializeFunc.stateTransition(currentChar, SpecailizeFunc.State.START);
	    	        tsStrings.append(printOut(Character.toString(currentChar), currentState)); // StringBuilder에 추가
	    	    } else {
	    	        // tmp에 현재 문자 추가 후 출력 처리
	    	        tmp.append(currentChar); 
	    	        tsStrings.append(printOut(tmp.toString(), pastState)); // StringBuilder에 추가
	    	    }
	    	    break; // 반복문 종료
	    	}

	    	// 다음 문자 가져오기
	    	nextChar = Input[index];

	    	// tmp가 비어있으면 상태 전환 수행
	    	if (tmp.length() == 0) {
	    	    currentState = specializeFunc.stateTransition(currentChar, currentState);
	    	}

	    	// 현재 상태를 과거 상태로 저장
	    	pastState = currentState;
	    }
	    return tsStrings.toString(); // StringBuilder의 내용을 문자열로 변환하여 반환
	}

	
	private boolean isSignificantState(SpecailizeFunc.State state) {
	    return state != SpecailizeFunc.State.STRING_START && state != SpecailizeFunc.State.COMMENT_START;
	}

	private boolean isWhitespace(char c) {
	    return c == ' ' || c == '\t';
	}
	
	private String printOut(String str, SpecailizeFunc.State state) {
	    String code = null;
	    int index;

	    switch (state) {
	        case INTEGER:
	            code = String.format("%s\tSCONSTANT\t44\t%d", str, lineNumber);
	            break;
	        case IDENTIFIER:
	            index = isReserved(str);
	            if (index != -1) {
	                code = String.format("%s\t%s\t%d\t%d", str, tokenMap.get(str), index, lineNumber);
	            } else {
	                code = String.format("%s\tSIDENTIFIER\t43\t%d", str, lineNumber);
	            }
	            break;
	        case STRING_END:
	            code = String.format("%s\tSSTRING\t45\t%d", str, lineNumber);
	            break;
	        case COMMENT_END:
	            return ""; 
	        default:
	            index = findSymbol(str);
	            if (index != -1) {
	                code = String.format("%s\t%s\t%d\t%d", str, tokenMap.get(str), index, lineNumber);
	            }
	            break;
	    }

	    if (code != null) {
	        code += "\n";
	    }

	    return code;
	}

	 private int isReserved(String target) {
	        // tokenMap에서 target이 키로 존재하는지 확인
	        if (tokenMap.containsKey(target)) {
	            // 입력된 순서에 따라 인덱스 반환
	            return new ArrayList<>(tokenMap.keySet()).indexOf(target);
	        }
	        return -1; // 예약어가 아닌 경우 -1 반환
	    }

	 private int findSymbol(String target) { 
		    // '/' 기호를 따로 처리
		    if (target.equals("/")) {
		        return 5; // '/'에 대해 5번 반환
		    }

		    // tokenMap에서 기호 검색
		    if (tokenMap.containsKey(target)) {
		        return new ArrayList<>(tokenMap.keySet()).indexOf(target); // 인덱스 반환
		    }

		    return -1; // 기호가 없을 경우 -1 반환
		}

	
	// SpecailizeFunc 내부 클래스
	public class SpecailizeFunc {
		public enum State {
	        START, IDENTIFIER, INTEGER, STRING_START,STRING_END,COMMENT_START,
	        COMMENT_END, LESS_THAN, LESS_OR_EQUAL, GREATER_THAN, GREAT_OR_EQUAL, NOT_EQUAL,
	        COLON, ASSIGNMENT, DOT_PERIOD, RANGE, COMPLETE
	    }
		
		 public State stateTransition(char c, State currentState) {
		        switch (currentState) {
		            case START:
		                return handleStartState(c);
		            case IDENTIFIER:
		            case INTEGER:
		                return handleIdentifierOrInteger(c, currentState);
		            case STRING_START:
		            case STRING_END:
		                return handleStringState(c, currentState);
		            case COMMENT_START:
		            case COMMENT_END:
		                return handleCommentState(c, currentState);
		            case LESS_THAN:
		            case GREATER_THAN:
		            case COLON:
		            case DOT_PERIOD:
		                return handleComparisonState(c, currentState);
		            default:
		                return State.COMPLETE;
		        }
		    }

		    private State handleStartState(char c) {
		        if (Character.isLetter(c)) return State.IDENTIFIER;
		        if (Character.isDigit(c)) return State.INTEGER;
		        switch (c) {
		            case '<': return State.LESS_THAN;
		            case '>': return State.GREATER_THAN;
		            case ':': return State.COLON;
		            case '.': return State.DOT_PERIOD;
		            case '\'': return State.STRING_START;
		            case '{': return State.COMMENT_START;
		            default: return State.COMPLETE;
		        }
		    }

		    private State handleIdentifierOrInteger(char c, State currentState) {
		        if (Character.isLetterOrDigit(c)) return currentState;
		        return State.COMPLETE;
		    }

		    private State handleStringState(char c, State currentState) {
		        if (currentState == State.STRING_START && c == '\'') return State.STRING_END;
		        return currentState == State.STRING_START ? State.STRING_START : State.COMPLETE;
		    }

		    private State handleCommentState(char c, State currentState) {
		        if (currentState == State.COMMENT_START && c == '}') return State.COMMENT_END;
		        return currentState == State.COMMENT_START ? State.COMMENT_START : State.COMPLETE;
		    }
		    
		    private State handleComparisonState(char c, State currentState) {
		        switch (currentState) {
		        case LESS_THAN:
		            if (c == '=') {
		                return State.LESS_OR_EQUAL;
		            } else if (c == '>') {
		                return State.NOT_EQUAL;
		            } else {
		                return State.COMPLETE;
		            }
		        case GREATER_THAN:
		            if (c == '=') return State.GREAT_OR_EQUAL;
		            return State.COMPLETE;       
		        case COLON:
		            if (c == '=') return State.ASSIGNMENT;
		            return State.COMPLETE;
		        case DOT_PERIOD:
		            if (c == '.') return State.RANGE;
		                return State.COMPLETE;
		        default:
		            return State.COMPLETE;

		        }
		    }
	}
}
