package enshud.s4.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CaslTable {

		private static final Map<String, String> OPERATOR_MAP = new HashMap<>();
		
		static {
		    OPERATOR_MAP.put("+", "\tADDA\tGR1, GR2\t;\n\tPUSH\t0, GR1\t;\n");
		    OPERATOR_MAP.put("-", "\tSUBA\tGR1, GR2\t;\n\tPUSH\t0, GR1\t;\n");
		    OPERATOR_MAP.put("or", "\tOR\tGR1, GR2\t;\n\tPUSH\t0, GR1\t;\n");
		    OPERATOR_MAP.put("*", "\tCALL\tMULT\t;\n\tPUSH\t0, GR2\t;\n");
		    OPERATOR_MAP.put("/", "\tCALL\tDIV\t;\n\tPUSH\t0, GR2\t;\n");
		    OPERATOR_MAP.put("div", "\tCALL\tDIV\t;\n\tPUSH\t0, GR2\t;\n");
		    OPERATOR_MAP.put("mod", "\tCALL\tDIV\t;\n\tPUSH\t0, GR1\t;\n");
		    OPERATOR_MAP.put("and", "\tAND\tGR1, GR2\t;\n\tPUSH\t0, GR1\t;\n");
		}
		
		String OperatorClassCheck(String op) {
		    String code = "\tPOP\tGR2\t;\n\tPOP\tGR1\t;\n";
		
		    if (OPERATOR_MAP.containsKey(op)) {
		        code += OPERATOR_MAP.get(op);
		    } else {
		        return op + " is not operator\n";
		    }
		
		    code += "\n";
		    return code;
		}
		
		String generateCode(String command, Object... Stringname) {
		    StringBuilder code = new StringBuilder();
		
		    switch (command) {
		        case "WRTSTR":
		        	if (Stringname.length == 2 && Stringname[0] instanceof String && Stringname[1] instanceof Integer) {
		                String string = (String) Stringname[0];
		                int charCounter = (int) Stringname[1];
		                // 문자열의 첫 번째와 마지막 문자를 제외한 길이 계산
		                int actualLength = string.substring(1, string.length() - 1).length();
		
		                code.append("\tLD\tGR1, =").append(actualLength).append("\t;\n")
		                    .append("\tPUSH\t0, GR1\t;\n")
		                    .append("\tLAD\tGR2, CHAR").append(charCounter).append("\t;\n")
		                    .append("\tPUSH\t0, GR2\t;\n")
		                    .append("\tPOP\tGR2\t;\n")
		                    .append("\tPOP\tGR1\t;\n")
		                    .append("\tCALL\tWRTSTR\t;\n");
		            }
		            break;
		
		        case "WRTLN":
		            code.append("\tCALL\tWRTLN\t;\n")
		            	.append("\n");
		            break;
		
		        case "WRTINT":
		            code.append("\tPOP\tGR2\t;\n")
		                .append("\tCALL\tWRTINT\t;\n");
		            break;
		
		        case "WRTCH": // 추가된 명령
		            code.append("\tPOP\tGR2\t;\n")
		                .append("\tCALL\tWRTCH\t;\n");
		            break;
		
		        default:
		            throw new IllegalArgumentException("Invalid command: " + command);
		            
		    }
		
		    return code.toString();
		}
		
		String load(String type, int value) {
		    StringBuilder code = new StringBuilder();
		
		    switch (type) {
		        case "VARIABLE":
		            code.append("\tLD\tGR2, =").append(value).append("\t;\n");
		            break;
		
		            
		        case "ARRAY":
		        	value--; // 배열은 1 감소 처리
		            code.append("\tPOP\tGR2\t;\n")
		                .append("\tADDA\tGR2, =").append(value).append("\t;\n");
		            break;
		
		        default:
		            throw new IllegalArgumentException("Unsupported load type: " + type);
		    }
		
		    return code.toString();
		}
		
		String generateStateLabel(String stateType, String codeNumber, int index) {
		    StringBuilder code = new StringBuilder();
		
		    switch (stateType) {
		        case "While":
		            switch (codeNumber) {
		                case "Start":
		                    code.append("LOOP").append(index).append("\tNOP\t;\n");
		                    break;
		                case "End":
		                    code.append("\tJUMP\tLOOP").append(index).append("\t;\n")
		                        .append("ENDLP").append(index).append("\tNOP\t;\n");
		                    break;
		                default:
		                    return null;
		            }
		            break;
		
		        case "If":
		            switch (codeNumber) {
		                case "ElseEnd":
		                    code.append("ELSE").append(index).append("\tNOP\t;\n");
		                    break;
		                case "ElseAndJump":
		                    code.append("\tJUMP\tENDIF").append(index).append("\t;\n")
		                        .append("ELSE").append(index).append("\tNOP\t;\n");
		                    break;
		                case "EndIf":
		                    code.append("ENDIF").append(index).append("\tNOP\t\t;\n");
		                    break;
		                case "ElseJump":
		                    code.append("\tPOP\tGR1\t;\n")
		                        .append("\tCPA\tGR1, =#0000\t;\n")
		                        .append("\tJZE\tELSE").append(index).append("\t;\n")
		                        .append("\n");
		                    break;
		                default:
		                    return null;
		            }
		            break;
		
		        default:
		            return null;
		    }
		
		    return code.toString();
		}
		
		
		
		String caslcompareoperater(String op, int num) {
		    StringBuilder code = new StringBuilder();

		    // 공통된 초기 코드
		    code.append("\tPOP\tGR2\t;\n")
		        .append("\tPOP\tGR1\t;\n")
		        .append("\tCPA\tGR1, GR2\t;\n");

		    // 조건에 따른 분기
		    if (op.equals("<=")) {
		        code.append("\tJMI\tTRUE").append(num).append("\t;\n")
		            .append("\tJZE\tTRUE").append(num).append("\t;\n");
		    } else if (op.equals(">=")) {
		        code.append("\tJPL\tTRUE").append(num).append("\t;\n")
		            .append("\tJZE\tTRUE").append(num).append("\t;\n");
		    } else if (op.equals("=")) {
		        code.append("\tJZE\tTRUE").append(num).append("\t;\n");
		    } else if (op.equals("<>")) {
		        code.append("\tJNZ\tTRUE").append(num).append("\t;\n");
		    } else if (op.equals("<")) {
		        code.append("\tJMI\tTRUE").append(num).append("\t;\n");
		    } else if (op.equals(">")) {
		        code.append("\tJPL\tTRUE").append(num).append("\t;\n");
		    } else {
		        return op + " is not a relational operator DEBUG\n";
		    }

		    // 공통된 후속 코드
		    code.append("\tLD\tGR1, =#0000\t;\n")
		        .append("\tJUMP\tBOTH").append(num).append("\t;\n")
		        .append("TRUE").append(num).append("\tNOP\n")
		        .append("\tLD\tGR1, =#FFFF\t;\n")
		        .append("BOTH").append(num).append("\tNOP\n")
		        .append("\tPUSH\t0, GR1\t;\n");

		    return code.toString();
		}

		
		String casllogicalcompareoperator(String op, int num) {
		    StringBuilder code = new StringBuilder();
		    
		    // 공통된 초기 코드
		    code.append("\tPOP\tGR2\t;\n")
		        .append("\tPOP\tGR1\t;\n")
		        .append("\tCPL\tGR1, GR2\t;\n");
		    
		    // 조건에 따른 분기
		    if (op.equals("<=")) {
		        code.append("\tJMI\tTRUE").append(num).append("\t;\n")
		            .append("\tJZE\tTRUE").append(num).append("\t;\n");
		    } else if (op.equals(">=")) {
		        code.append("\tJPL\tTRUE").append(num).append("\t;\n")
		            .append("\tJZE\tTRUE").append(num).append("\t;\n");
		    } else if (op.equals("=")) {
		        code.append("\tJZE\tTRUE").append(num).append("\t;\n");
		    } else if (op.equals("<>")) {
		        code.append("\tCPA\tGR1, GR2\t;\n")
		            .append("\tJNZ\tTRUE").append(num).append("\t;\n");
		    } else if (op.equals("<")) {
		        code.append("\tJMI\tTRUE").append(num).append("\t;\n");
		    } else if (op.equals(">")) {
		        code.append("\tJPL\tTRUE").append(num).append("\t;\n");
		    } else {
		        return op + " is not a relational operator DEBUG\n";
		    }
		    
		    // 공통된 후속 코드
		    code.append("\tLD\tGR1, =#0000\t;\n")
		        .append("\tJUMP\tBOTH").append(num).append("\t;\n")
		        .append("TRUE").append(num).append("\tNOP\n")
		        .append("\tLD\tGR1, =#FFFF\t;\n")
		        .append("BOTH").append(num).append("\tNOP\n")
		        .append("\tPUSH\t0, GR1\t;\n");
		    
		    return code.toString();
		}

		
		String generateLoopEndJump(int index) {
		    StringBuilder code = new StringBuilder();
		    code.append("\tPOP\tGR1\t;\n")
		        .append("\t").append("CPL").append("\tGR1, =#0000\t;\n")
		        .append("\tJZE\t").append("ENDLP").append(index).append("\t;\n");
		    code.append("\n");
		    return code.toString();
		}

		// 문자열 결합을 StringBuilder로 개선
		String caslend(ArrayList<String> charList, int varNumber) {
		    StringBuilder code = new StringBuilder();
		    
		    code.append("VAR\tDS\t").append(varNumber).append("\t;\n");
		
		    for (int i = 0; i < charList.size(); i++) {
		        code.append("CHAR").append(i).append("\tDC\t").append(charList.get(i)).append("\t;\n");
		    }
		
		    code.append("LIBBUF\tDS\t256\t;\n");
		    code.append("\tEND\t\t;\n");
		    
		    return code.toString();
		}
		
		// assignLeft 함수도 StringBuilder를 사용하여 리팩토링
		String caslassignstart(int varNumber) {
		    StringBuilder code = new StringBuilder();
		    
		    code.append("\tLD\tGR2, =").append(varNumber).append("\t;\n");
		    code.append("\tPOP\tGR1\t;\n");
		    code.append("\tST\tGR1, VAR, GR2\t;\n");
		    code.append("\n");
		    
		    return code.toString();
		}
		
		String pushMethod(String type, String value) {
		    StringBuilder code = new StringBuilder();
		
		    switch (type) {
		        case "VAR":
		            code.append("\tLD\tGR1, VAR, GR2\t;\n");
		            break;
		
		        case "CHAR":
		            code.append("\tLD\tGR1, =").append(value).append("\t;\n");
		            break;
		
		        case "CONST":
		            String constant = value.equals("false") ? "#0000" : value.equals("true") ? "#FFFF" : value;
		            code.append("\tPUSH\t").append(constant).append("\t;\n");
		            return code.toString();
		
		        default:
		            throw new IllegalArgumentException("Unsupported push type: " + type);
		    }
		
		    code.append("\tPUSH\t0, GR1\t;\n");
		    return code.toString();
		}
		
		String subStateMethod(String stateType, int value) {
		    StringBuilder code = new StringBuilder();
		    
		    switch (stateType) {
		        case "Start":
		            code.append("PROC").append(value).append("\tNOP\t\t;\n");
		            break;
		        case "Call":
		            code.append("\tCALL\tPROC").append(value).append("\t;\n");
		            break;
		        case "VariableStart":
		            code.append("\tLD\tGR1, GR8\t;\n")
		                .append("\tADDA\tGR1, =").append(value).append("\t;\n");
		            break;
		        default:
		            code.append("Unknown state: ").append(stateType);
		            break;
		    }
		    
		    return code.toString();
		}
		// 공통적인 레지스터 작업 수행
			String generateRegisterCode(String operation, int value) {
			    StringBuilder code = new StringBuilder();
		
			    switch (operation) {
			        case "SUBSTATEPARAMETER":
			            code.append("\tLD\tGR2, 0, GR1\t;\n")
			                .append("\tLD\tGR3, =").append(value).append("\t;\n")
			                .append("\tST\tGR2, VAR, GR3\t;\n")
			                .append("\tSUBA\tGR1, =1\t;\n");
			            break;
		
			        case "SUBPARAMETER":
			            code.append("\tLD\tGR1, 0, GR8\t;\n")
			                .append("\tADDA\tGR8, =").append(value).append("\t;\n")
			                .append("\tST\tGR1, 0, GR8\t;\n");
			            break;
		
			        default:
			            throw new IllegalArgumentException("Invalid operation: " + operation);
			    }
		
			    return code.toString();
			}
			
		
		String caslnot() {
		    return new StringBuilder()
		            .append("\tPOP\tGR1\t;\n")
		            .append("\tXOR\tGR1, =#FFFF\t;\n")
		            .append("\tPUSH\t0, GR1\t;\n")
		            .toString();
		}
		
		String caslminus() {
		    return new StringBuilder()
		            .append("\tPOP\tGR2\t;\n")
		            .append("\tLD\tGR1, =0\t;\n")
		            .append("\tSUBA\tGR1, GR2\t;\n")
		            .append("\tPUSH\t0, GR1\t;\n")
		            .toString();
		}
		
		String caslassignEnd() {
		    return new StringBuilder()
		            .append("\tPOP\tGR1\t;\n")
		            .append("\tST\tGR1, VAR, GR2\t;\n")
		            .toString();
		}

}