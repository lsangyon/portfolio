package enshud.s4.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import enshud.s2.parser.CallSementicError;
import enshud.s2.parser.CallSyntaxError;
import enshud.s2.parser.CreateSymbolTable;

public class ParseStructure {
	
	// CASL 코드 생성 메서드


    private String[][] words; // 단어 배열
    int id = 0; // ID 값
 
	boolean avdFlag = false;
	boolean asFlag = false;
	boolean sphFlag = false;
	boolean pcsFlag = false;
	ArrayList<String> subProgramname = new ArrayList<>();
	CreateSymbolTable cst = new CreateSymbolTable();
	private String casl2Code = "";
	ArrayList<String> StringCharList = new ArrayList<>();
	private ArrayList<String> varList = new ArrayList<>();
	ArrayList<String> subCodeFinal = new ArrayList<>();
	CaslTable newcasl = new CaslTable();
	ArrayList<String> subProgramProcedureList = new ArrayList<>();

	private String subCode = "";
	private HashMap<String, String> subMap = new HashMap<>();
	ArrayList<String> subList = new ArrayList<>();

	public ParseStructure(String[][] words) {
        this.words = words;
    }

	
    public boolean matchtoken(String expectedTokenType) {
        if (checkCurrentToken(expectedTokenType)) {
        	incrementId();
            return true;
        }
        return false;
    }
	    
	    public boolean checkCurrentToken(String target) {
	        return words[id][1].equals(target);
	    }
	    
	    public void require(String expectedToken) {
	        if (checkCurrentToken(expectedToken)) {
	        	incrementId();
	        } else {
	            throw new CallSyntaxError(words[id][3]);
	        }
	    }
	    
	    // 타입 검증 메서드: 두 타입이 일치하지 않으면 예외를 던짐
	    private void CheckoutType(String expectedType, String actualType, String errorLine) throws CallSementicError {
	        if (!expectedType.equals(actualType)) {
	            throw new CallSementicError(errorLine);
	        }
	    }
	    
	    public void handleVariableDeclarationOrAssignment(String varName) throws CallSyntaxError, CallSementicError {
	    	String vartype;
	        if (avdFlag) {
	        	cst.checkAndAddVariable(varName, false, null, words[id][3]);
	        	vartype = cst.findSymbolType(varName);
	        	if (vartype == null) {
	        	    varList.add(varName + "[" + 0 + "]");
	        	}
	        } else if (asFlag) {
	        	cst.checkVariableExists(varName, true, words[id][3], null);
	        	vartype = cst.findSymbolType(varName);
	        }
	    }
	    
	    
	    public void handleProcedureDeclarationOrAssignment(String varName) throws CallSyntaxError, CallSementicError {
	    	if (sphFlag) {
	    		cst.checkAndAddVariable(varName, false, "procedure", words[id][3]);
			}
			if (pcsFlag) {
				cst.checkVariableExists(varName, true, words[id][3], subprogramName);

			}
			return;
	    }
	    private int arrayidMinimum;
	    private int arrayidMaximum;
	    private int subprogramCounter = 0;

	 // 배열 변수 테이블 생성
	    private void createArrayTable(String type) throws CallSementicError {
	        if (varList.isEmpty()) {
	            return; // varList가 비어있으면 처리하지 않음
	        }
	        
	        String lastElement = varList.get(varList.size() - 1); // 마지막 요소 가져오기
	        String varName = lastElement.split("\\[")[0]; // '[' 앞부분 추출
	        
	        for (int j = arrayidMinimum; j <= arrayidMaximum; j++) {
	            varList.add(varName + "[" + j + "]");
	        }
	    }

	    private void manageSubTable(String type) throws CallSementicError {
	        for (String varName : subProgramname) {
	            // 중복 선언 처리
	            if (subMap.containsKey(varName)) {
	                throw new CallSementicError(words[id][3]); // 중복 선언 처리
	            }

	            // 변수 등록
	            subMap.put(varName, type);

	            // 서브 변수 처리
	            if (sphFlag) {
	            	//System.out.println("subProgramProcedureList: " + subProgramProcedureList);
	                String paramName = subProgramProcedureList.get(subprogramCounter); // 파라미터 이름 가져오기
	                subList.add(paramName + " : " + varName); // 파라미터 리스트에 추가
	            }
	        }
	    }



	    public void incrementId() {
	        id++;
	    }

 
	    //프로그램
	  public String program() throws CallSyntaxError {
	        require("SPROGRAM");
	        programName();
	        require("SSEMICOLON");
	        block();
	        compoundStatement();
	        casl2Code += "\tRET\t\t;\n";
	        require("SDOT");
			for(String subcode : subCodeFinal) {
				casl2Code += subcode;
			}
			casl2Code += newcasl.caslend(StringCharList, varList.size());
			return casl2Code;
	    }	 
	  
	  //프로그램 이름
	   public void programName() throws CallSyntaxError {
		require("SIDENTIFIER");
	  }
		    
	  //블록
	  public void block() throws CallSyntaxError {
	  	varDeclarement();
	     subProgramDefinition();
	  }

		    //변수선언
	  	public void varDeclarement() throws CallSyntaxError, CallSementicError {
	     if (matchtoken("SVAR")) {
	      	arrangeVariableDeclarations();
	     }
		    }
		 // 변수 선언 나열
	 	public void arrangeVariableDeclarations() throws CallSyntaxError, CallSementicError {
		    	avdFlag = true; ///declaration
			do {
				arrangeVariableName();
	            require("SCOLON");
	            type();
	            require("SSEMICOLON");
	        } while (checkCurrentToken("SIDENTIFIER"));
			avdFlag = false;
		}
		    
		public void type() throws CallSyntaxError, CallSementicError {
		    if (checkCurrentToken("SARRAY")) {
		    	arrayType();
		    } else {
		    	standardType();
		    }
		}
		//변수명 나열
	    public void arrangeVariableName() throws CallSyntaxError, CallSementicError{
	        do {
	        	variableName();
	        } while (matchtoken("SCOMMA"));
	    }
		//변수명
	    public String variableName() throws CallSyntaxError, CallSementicError {
	        require("SIDENTIFIER");
	        String varName = words[id - 1][0];//주소이동을 했기때문에 주소를 하나 뒤로
	        // 선언 또는 대입에서 중복 체크 및 처리
	        handleVariableDeclarationOrAssignment(varName);
	        return cst.findSymbolType(varName);
	    }

	   

		//표준형
		public void standardType() throws CallSyntaxError {
			if(OperatorClassCheckout.isStandardType(words[id][1])) {
				incrementId();
	    	}else{
	    		//System.out.println("standardTypeError");
	    		throw new CallSyntaxError(words[id][3]);
	    	}
			cst.updateSymbolType(words[id-1][0]);
			return;
		}
		public void arrayType() throws CallSyntaxError {
		    if (matchtoken("SARRAY") && matchtoken("SLBRACKET")) {
		        MinimumId();
		        require("SRANGE");
		        MaximumId();
		        if (!(matchtoken("SRBRACKET") && matchtoken("SOF"))) {
		            throw new CallSyntaxError(words[id][3]);
		        }
		        cst.updateSymbolType("Array");
		        createArrayTable(words[id][0]);
		        standardType();
		    } else {
		        throw new CallSyntaxError(words[id][3]);
		    }
		}
		 //최소
	    public void MinimumId() throws CallSyntaxError, CallSementicError {
	    	checkSign();
	    	arrayidMinimum = Integer.parseInt(words[id-1][0]);
	    }
	    //최대
	    public void MaximumId() throws CallSyntaxError , CallSementicError{
	    	checkSign();
	    	arrayidMaximum = Integer.parseInt(words[id-1][0]);
	    }
	    
	 // 플러스/마이너스 부호
	    public void checkSign() throws CallSyntaxError , CallSementicError{
	        if (OperatorClassCheckout.isAdditionOperator(words[id][1])) {
	        	incrementId(); //+-부호존재여부 확인
	        } 
	         else {
	        	 unsignedInteger(); // 부호가 없을 경우 unsignedInteger 호출
	        }
	    }
		//정수
		public void unsignedInteger() {
	        require("SCONSTANT");
		}

		 //부프로그램정의군
	    public void subProgramDefinition() throws CallSyntaxError , CallSementicError{
	    	cst.enterScope();
	        while (checkCurrentToken("SPROCEDURE")) {
	            subProgramDeclarement();
	            require("SSEMICOLON");
	        }
	    }
	    public String subprogramName = "";
	    
	    // 부프로그램 정의
	    public void subProgramDeclarement() throws CallSyntaxError, CallSementicError {
	        // 부프로그램 초기화
	        sphFlag = true;
	        
	        subCode += newcasl.subStateMethod("Start", subprogramCounter);
	        // 부프로그램 이름 설정
	        subprogramName = words[id + 1][0];
	        // 부프로그램 헤더 처리
	        subProgramHeader();

	        // 변수 선언 처리
	        varDeclarement();
	        // 매개변수 처리 및 변수 수 계산
	        int subprogramvar = calculateParameterList(subProgramProcedureList.get(subprogramCounter));
	        subCode += newcasl.subStateMethod("VariableStart", subprogramvar);
	        // 매개변수 코드를 생성
	        generateParameterCode();

	        if (subprogramvar != 0) {
	            subCode += newcasl.generateRegisterCode("SUBPARAMETER", subprogramvar);
	        }
	        // 복합 문장 처리
	        compoundStatement();

	        // 부프로그램 종료 처리
	        subCode += "\tRET\t\t;\n";
	        subCodeFinal.add(subCode);
	        
	        // 리소스 정리
	        finalizeSubProgram();
	    }

	    // 매개변수 처리 및 코드 생성
	    private void generateParameterCode() {
	        for (String entry : subList) {
	            String[] parts = entry.split(" : ");
	            // 현재 부프로그램과 매칭되는 매개변수 코드 생성
	            if (parts[0].equals(subProgramProcedureList.get(subprogramCounter))) {
	                int index = varList.lastIndexOf(parts[1] + "[0]");
	                subCode += newcasl.generateRegisterCode("SUBSTATEPARAMETER", index);
	            }
	        }
	    }

	    // 리소스 정리
	    private void finalizeSubProgram() {
	    	subprogramCounter++;
	        subCode = "";
	        subMap.clear();
	        subProgramname.clear();
	        sphFlag = false;
	        cst.exitScope();
	    }

	    // 매개변수 개수 계산
	    private int calculateParameterList(String paramName) {
	        int count = 0;
	        for (String entry : subList) {
	            String[] parts = entry.split(" : ");
	            if (parts[0].equals(paramName)) {
	                count++;
	            }
	        }
	        return count;
	    }

	    // 부프로그램 헤더 처리
	    public void subProgramHeader() throws CallSyntaxError, CallSementicError {
	        cst.enterScope();
	        require("SPROCEDURE");
	        procedureName();
	        TemporalParameter();
	        require("SSEMICOLON");
	    }

	    
	    
	    //과정이름
	    public void procedureName() throws CallSyntaxError, CallSementicError {
			require("SIDENTIFIER");
			String varName = words[id - 1][0];
			if(subProgramProcedureList.contains(varName)) {
				subprogramName = varName;
			}
			subProgramProcedureList.add(words[id-1][0]);
			handleProcedureDeclarationOrAssignment(varName);
		}
	  //임시파라미터
	    public void TemporalParameter() throws CallSyntaxError, CallSementicError {
	    	//System.out.println("TemporalParameter");
	        if (matchtoken("SLPAREN")) {
	        	
	        	arrangeTemporalParameterSequence();
	            require("SRPAREN");
	        }
	    }
	    //임시파라미터나열
	    public void arrangeTemporalParameterSequence() throws CallSyntaxError, CallSementicError {
	        TemporalParameterNames();
	        require("SCOLON");
	        standardType(); 
	        manageSubTable(words[id-1][0]);
	        while (matchtoken("SSEMICOLON")) {
	        	arrangeTemporalParameterSequence();
	        	manageSubTable(words[id-1][0]);
	        }
	    }
	    //임시파라미터명
	    public void TemporalParameterNames() throws CallSyntaxError, CallSementicError {
	        TemporalParameterName();
	        while (matchtoken("SCOMMA")) {
	            TemporalParameterName();
	        }
	    }
	  //임시파라미터명조합
	    public void TemporalParameterName() throws CallSyntaxError, CallSementicError {
	        require("SIDENTIFIER");
	        subProgramname.add(words[id-1][0]);
	        varList.add(words[id-1][0] + "[0]");
			if (cst.checkVariableDuplication(words[id-1][0], false)) {
			    throw new CallSementicError(words[id][3]);
			} else {
				cst.registerSymbol(words[id-1][0], null);
			}

		}
	  //복합문
	    public void compoundStatement() throws CallSyntaxError, CallSementicError {
	        require("SBEGIN");
	        arrangeStatementSequence();
	        require("SEND");
	    }
	    //문나열
	    public void arrangeStatementSequence() throws CallSyntaxError , CallSementicError{
	        do {
	        	//VariableTable(words[id][0]);
	            sentencestructure();
	            if (!matchtoken("SSEMICOLON")) {
	                throw new CallSyntaxError(words[id][3]);
	            }
	        } while (OperatorClassCheckout.isValidStatement(words[id][1]));	        
	    }
	    //문장
	    public void sentencestructure() throws CallSyntaxError , CallSementicError {
			 if(matchtoken("SWHILE")) {
				 whileStatement();
			} else if(matchtoken("SIF")) {
				ifStatement();
			} else {
				basicStatement();
			}
		}
	    
		
		private int ifCounter = 0; // ifCounter를 0으로 시작

		// if문
		public void ifStatement() throws CallSyntaxError, CallSementicError {
		    // Map을 사용하여 if 상태를 관리
		    Map<Integer, Boolean> ifMap = new HashMap<>();
		    int currentIfCounter = ifCounter; // 현재 ifCounter 값을 사용
		    ifCounter++; // 이후에 증가

		    // ifMap에 현재 ifCounter를 키로, true 값을 설정
		    ifMap.put(currentIfCounter, true);
		    cst.enterScope();
		    String ifMeaning;
		    ifMeaning = expression();
		    require("STHEN");

		    // Map에서 첫 번째 true 값을 찾고 처리
		    Integer index = findFirstTrueIndex(ifMap, currentIfCounter);
		    if (index != null) {
		        if (!sphFlag) casl2Code += newcasl.generateStateLabel("If", "ElseJump", index);
		        else subCode += newcasl.generateStateLabel("If", "ElseJump", index);
		    }

		    CheckoutType(ifMeaning, "boolean", words[id][3]);

		    compoundStatement();

		    // "SELSE"를 찾은 경우 처리
		    if (matchtoken("SELSE")) {
		        index = findFirstTrueIndex(ifMap, currentIfCounter);
		        if (index != null) {
		            if (!sphFlag) casl2Code += newcasl.generateStateLabel("If", "ElseAndJump", index);
		            else subCode += newcasl.generateStateLabel("If", "ElseAndJump", index);
		        }

		        compoundStatement();

		        // "EndIf" 상태 설정
		        if (index != null) {
		            if (!sphFlag) casl2Code += newcasl.generateStateLabel("If", "EndIf", index);
		            else subCode += newcasl.generateStateLabel("If", "EndIf", index);
		            ifMap.put(index, false);  // 해당 인덱스를 false로 설정
		        }
		        return;
		    } else {
		        // "ElseEnd" 처리
		        index = findFirstTrueIndex(ifMap, currentIfCounter);
		        if (index != null) {
		            if (!sphFlag) casl2Code += newcasl.generateStateLabel("If", "ElseEnd", index);
		            else subCode += newcasl.generateStateLabel("If", "ElseEnd", index);
		        }
		    }

		    cst.exitScope();
		}


		private Integer findFirstTrueIndex(Map<Integer, Boolean> map, int counter) {
		    // map에서 true 값을 가진 첫 번째 인덱스를 찾음
		    for (int i = counter; i >= 0; i--) {
		        if (map.getOrDefault(i, false)) {
		            return i;
		        }
		    }
		    return null;  // 없으면 null을 반환
		}

		private int whileCounter = 0; // whileCounter를 0으로 초기화

		public void whileStatement() throws CallSyntaxError, CallSementicError {
		    cst.enterScope();

		    // Map을 사용하여 while 상태를 관리
		    Map<Integer, Boolean> whileMap = new HashMap<>();

		    int currentWhileCounter = whileCounter; // 현재 whileCounter 값을 사용
		    whileCounter++; // 이후에 증가

		    // whileCounter를 키로, true 값을 설정
		    whileMap.put(currentWhileCounter, true);

		    String whileMeaning;
		    if (!sphFlag) {
		    	casl2Code += newcasl.generateStateLabel("While", "Start", currentWhileCounter);
		    } else {
		        subCode += newcasl.generateStateLabel("While", "Start", currentWhileCounter);
		    }

		    whileMeaning = expression();
		    CheckoutType(whileMeaning, "boolean", words[id][3]);

		    // whileMap에서 첫 번째 true 값을 찾고 처리
		    int index = findFirstTrueIndex(whileMap, currentWhileCounter);
		    if (index != -1) {
		        if (!sphFlag) {
		        	casl2Code += newcasl.generateLoopEndJump(index);
		        } else {
		            subCode += newcasl.generateLoopEndJump(index);
		        }
		    }

		    if (!matchtoken("SDO")) {
		        throw new CallSyntaxError(words[id][3]);
		    }
		    compoundStatement();

		    // whileMap에서 첫 번째 true 값을 처리하고 종료
		    if (index != -1) {
		        if (!sphFlag) {
		        	casl2Code += newcasl.generateStateLabel("While", "End", index);
		        } else {
		            subCode += newcasl.generateStateLabel("While", "End", index);
		        }
		        whileMap.put(index, false); // 해당 인덱스를 false로 설정
		    }

		    cst.exitScope();
		}


		//기본문
	    public void basicStatement() throws CallSyntaxError , CallSementicError {
	        if (checkCurrentToken("SREADLN") || checkCurrentToken("SWRITELN")) {
	            inputOutputStatement();
		        } else if (checkCurrentToken("SIDENTIFIER")) {
		            sentenceType();
		        } else {
		            compoundStatement();
		        }
		    }
		
		public void sentenceType() throws CallSyntaxError , CallSementicError {
	        String wordnext = words[id+1][1];
	        if (wordnext.equals("SLPAREN") || wordnext.equals("SSEMICOLON")) { // 문의 나열, 대입문
	            procedureCallStatement();
	        } else {
	            assignmentStatement(); // 대입문 :=
	        }
	    }
		private String leftAssignmentCode = ""; 

		
		private boolean assignmentcheck = false;
		private boolean doubleassigncheck= false;
		//대입문
	    public void assignmentStatement() throws CallSyntaxError, CallSementicError { 
			String varType, eqType;
			assignmentcheck = true;
			String wordleft = words[id][0];
			String arraychecker = words[id+2][1];
			asFlag = true;
			varType = variable("assign");
			asFlag = false;
			if (checkCurrentToken("SLBRACKET")) {
		        id();  // id() 메서드를 통해 서브스크립트 ID 타입을 얻어옴
		        require("SRBRACKET");
		    }else if(doubleassigncheck){
				String symboltype = cst.findSymbolType(wordleft);
				if(symboltype.contains("Array") && arraychecker.equals("SIDENTIFIER")) {
					if(leftcalflag) {
						leftcalflag = false;
					}else {
						leftAssignmentCode += newcasl.pushMethod("VAR", null);
					}
				}
				loadArrayForAssignment(wordleft);
				doubleassigncheck = false;
			}
			assignmentcheck = false;
			require("SASSIGN");

			eqType = expression();
			CheckoutType(varType, eqType, words[id][3]);
			if (leftAssignmentCode == null) {
				leftAssignmentCode = "";
			}

			// 코드 추가 로직 분리
			addCode(leftAssignmentCode + newcasl.caslassignEnd(), sphFlag);

			leftAssignmentCode = ""; // 초기화
			}

			// 공통 코드를 처리하는 메서드 추가
			private void addCode(String code, boolean isSubCode) {
			    if (!isSubCode) {
			    	casl2Code += code;
			    } else {
			        subCode += code;
			    }
			}
			
			private Boolean findoperator(String words) {
				 if(OperatorClassCheckout.isNumericAdditionOperator(words) || OperatorClassCheckout.isNumericMultipleOperator(words)) {
					 return true;
				 }else {
					 return false;
				 }
			}
			
		public Boolean leftcalflag = false;
		public Boolean untilleft = false;
	    public String variable(String type) throws CallSyntaxError, CallSementicError {	    	
			String variableword = words[id][0];
			String wordarraychecker = words[id+2][1];
			String pastwordarray = words[id-2][1];
			if ("assign".equals(type) && asFlag) {
			    if (!assignmentcheck) {
			        loadVar(variableword);
			    } else {
			        String typesymbol = cst.findSymbolType(variableword);
			        if (typesymbol != null && !typesymbol.trim().contains("Array")) {
			            loadVarForAssignment(variableword);
			        }
			    }
			}

            
	        String RealType = variableName();
	        String SubscriptType = subscriptedvariable();
	        if (type.equals("identifier")) {
	        	String arraytype = cst.findSymbolType(variableword);
	        	Boolean arrayFlag = false;
	        	if(arraytype.contains("Array") && wordarraychecker.equals("SIDENTIFIER")) {
	        		arrayFlag = true;
	        	}
	        	
	            if (!arrayFlag && !arraytype.contains("Array")) {
	            	handleVar(variableword);
	            }else if (arrayFlag) {
	            	handleArray(variableword);
	            }else if(pastwordarray.equals("SIDENTIFIER") && wordarraychecker.equals("SCONSTANT")) {
	            	handleArray(variableword);
	            }
	        }
	        
	        if(SubscriptType == "integer" && writelnFlag) {
	        	handleArray(variableword);
	        }
	        if (SubscriptType == null) {   	
	            return RealType;
	        }
	        if (RealType != null && RealType.contains("Array")) {
	            int lastSpaceIndex = RealType.lastIndexOf(" ");
	            SubscriptType = (lastSpaceIndex != -1) 
	                ? RealType.substring(lastSpaceIndex + 1) 
	                : RealType;  // 공백이 없으면 전체 문자열을 반환
	        }
		
	        return SubscriptType;
	    }
	    
	    private void handleArray(String varname) {
	        if (!assignmentcheck) {
	            loadArray(varname);
	        } else {
		        if (words[id][1].equals("SRBRACKET")) {
		        	leftAssignmentCode += newcasl.pushMethod("VAR", null);
		        }
		        handleAssignchecker(varname, "Array");
	        }
	    }
	    
	    private void handleVar(String varname) {
	        if (!assignmentcheck) {
	        	loadVar(varname);
	        } else {
	        	handleAssignchecker(varname, "Var");
	        }
	    }
	    
	    private void loadVar(String varname) {
	        if (!sphFlag) {
	        	casl2Code += newcasl.load("VARIABLE", varList.indexOf(varname + "[0]"));
	        	casl2Code += newcasl.pushMethod("VAR", null);
	        } else {
	            subCode += newcasl.load("VARIABLE", varList.lastIndexOf(varname + "[0]"));
	            subCode += newcasl.pushMethod("VAR", null);
	        }
	    }
	    
	    private void loadArray(String varname) {
	        if (!sphFlag) {
	        	casl2Code += newcasl.load("ARRAY", varList.indexOf(varname + "[1]"));
	        	casl2Code += newcasl.pushMethod("VAR", null);
	        } else {
	            subCode += newcasl.load("ARRAY", varList.indexOf(varname + "[1]"));
	            subCode += newcasl.pushMethod("VAR", null);
	        }
	    }

	    private void handleAssignchecker(String varname, String type) {
	        if(type.equals("Array")) {
	        	loadArrayForAssignment(varname);
	        }else if(type.equals("Var")) {
	        	loadVarForAssignment(varname);
	        }

	        boolean operatorcheckout1 = findoperator(words[id][1]);
	        boolean operatorcheckout2 = findoperator(words[id - 2][1]);

	        if (words[id - 2][1].equals("SLBRACKET") && words[id - 1][1].equals("SIDENTIFIER") && operatorcheckout1) {
	        	leftAssignmentCode += newcasl.pushMethod("VAR", null);
	            leftcalflag = true;
	            untilleft = true;
	        } else if (untilleft && !words[id][1].equals("SRBRACKET")) {
	        	leftAssignmentCode += newcasl.pushMethod("VAR", null);
	        } else if (untilleft && words[id][1].equals("SRBRACKET") && operatorcheckout2) {
	        	leftAssignmentCode += newcasl.pushMethod("VAR", null);
	            untilleft = false;
	        }
	    }

	    private void loadArrayForAssignment(String varname) {
	        if (!sphFlag) {
	        	leftAssignmentCode += newcasl.load("ARRAY", varList.indexOf(varname + "[1]"));
	        } else {
	        	leftAssignmentCode += newcasl.load("ARRAY", varList.lastIndexOf(varname + "[1]"));
	        }
	    }
	    
	    private void loadVarForAssignment(String varname) {
	        if (!sphFlag) {
	        	leftAssignmentCode += newcasl.load("VARIABLE", varList.indexOf(varname + "[0]"));
	        } else {
	        	leftAssignmentCode += newcasl.load("VARIABLE", varList.lastIndexOf(varname + "[0]"));
	        }
	    }
	    public Boolean endArray = true;
	    //첨자변수확인
	    public String subscriptedvariable() throws CallSyntaxError, CallSementicError {   	
	    	cst.enterScope();
			String subscriptidType = null;
			 if (matchtoken("SLBRACKET")) {
				 arraystate();
				 subscriptidType = id();
				 if(assignmentcheck) {
					 doubleassigncheck = true;
				 }
				require("SRBRACKET");
				arraystate();
			}
			cst.exitScope();
			return subscriptidType;
		}
	    
	    public Boolean arraystate() {
	    	endArray = !endArray;
			return endArray;
	    }
	    
		//添字
		public String id() throws CallSyntaxError, CallSementicError{
			
			String newexpression = expression();
			
			CheckoutType(newexpression, "integer", words[id][3]);
			return newexpression;
		}
		public void procedureCallStatement() throws CallSyntaxError, CallSementicError {
		    pcsFlag = true; // 프로시저 호출 플래그 설정
		    procedureName(); // 프로시저 이름 처리
		    pcsFlag = false; // 플래그 해제

		    String subwordName = words[id - 1][0];

		    if (matchtoken("SLPAREN")) {
			    arrangeExpressionSequence();
			    require("SRPAREN");
		        handleProcedureWithParentheses(subwordName);
		    } else {
		    	int subwordIndex = subProgramProcedureList.indexOf(subwordName);
		    	casl2Code += newcasl.subStateMethod("Call", subwordIndex);
		    }
		}

		// 괄호가 있는 프로시저 호출 처리
		private void handleProcedureWithParentheses(String subwordName) throws CallSyntaxError, CallSementicError {
			if(!sphFlag) {
				int subwordIndex = subProgramProcedureList.indexOf(subwordName);
				casl2Code += newcasl.subStateMethod("Call", subwordIndex);

			}else {
				subCode += newcasl.subStateMethod("Call", subprogramCounter);
			}
		}

	  //식 나열
	    public void arrangeExpressionSequence() throws CallSyntaxError , CallSementicError {
	        do {
	        	expression();
	        } while (matchtoken("SCOMMA"));
	    }
		//식
	    private int roCounter = 0;
	    private Map<Integer, Boolean> roMap = new HashMap<>();

	    public String expression() throws CallSyntaxError, CallSementicError {   
	        String headRelationalOperator = simpleExpression();
	        if (OperatorClassCheckout.isRelationalOperator(words[id][1])) {
	            String op = words[id][0];
	            relationalOperator();
	            String backRelationalOperator = simpleExpression();

	            CheckoutType(headRelationalOperator, backRelationalOperator, words[id][3]);
	            roMap.put(roCounter, true);

	            int activeCounter = findLastTrueKey();
	            if (activeCounter != -1) {
	                handleRelationalOperation(op, headRelationalOperator, backRelationalOperator, activeCounter);
	                // 사용한 상태를 비활성화
	                roMap.put(activeCounter, false);
	            }
	            roCounter++;
	            return "boolean";
	        }

	        return headRelationalOperator;
	    }

	    // 관계 연산 처리
	    private void handleRelationalOperation(String op, String headRelationalOperator, String backRelationalOperator, int activeCounter) {


	        // 비교 연산자에 따른 코드 추가
	        String code = (headRelationalOperator.equals("boolean") && backRelationalOperator.equals("boolean"))
	                ? newcasl.casllogicalcompareoperator(op, activeCounter)
	                : newcasl.caslcompareoperater(op, activeCounter);

	        if (!sphFlag) {
	        	casl2Code += code;
	        } else {
	            subCode += code;
	        }
	    }


	    // Map에서 가장 최근 true 상태를 가진 키를 찾는 메서드
	    private int findLastTrueKey() {
	        for (int i = roCounter; i >= 0; i--) {
	            if (roMap.getOrDefault(i, false)) {
	                return i;
	            }
	        }
	        return -1; // true 상태가 없으면 -1 반환
	    }
		
		// 관계 연산자
	    public void relationalOperator() throws CallSyntaxError , CallSementicError {
	    	if(OperatorClassCheckout.isRelationalOperator(words[id][1])) {
	    		incrementId();
	    	}else{
	    		throw new CallSyntaxError(words[id][3]);
	    	}
	    }

	    private Boolean minusFlag = false;
	    private int SLPARENSTACK = 0;
	  
	    //단순식
	    public String simpleExpression() throws CallSyntaxError , CallSementicError {
			String HeadAdditionOperator, BackAdditionOperator;
		    	if ("+".equals(words[id][0])) {
		    		require("SPLUS");
		    	}else if ("-".equals(words[id][0])) {
		    		require("SMINUS");
		    		if(words[id+1][1].equals("SLPAREN")) {
		    			SLPARENSTACK = 0;
		    		}
		    		minusFlag =true;
		    	}
		    	HeadAdditionOperator = term();
		    	String wordtype = cst.findSymbolType(words[id+1][0]);
		    	boolean isMinusExpressionValid = isValidMinusExpression(wordtype);
		        boolean isMinusConstantInParentheses = isMinusConstantInParentheses();
		        boolean isStandaloneMinus = isStandaloneMinus();
		        boolean isVariableDuplicationDetected = isVariableDuplicationDetected();
		        if (isMinusExpressionValid) {
		            if (!isVariableDuplicationDetected) {
		                appendCaslMinus();
		            }
		        } else if (isMinusConstantInParentheses) {
		            appendCaslMinus();
		        } else if (isStandaloneMinus) {
		            appendCaslMinus();
		            minusFlag = false; // 플래그 초기화
		        }

		    	
			while(OperatorClassCheckout.isAdditionOperator(words[id][1])) {
				String op = words[id][0];
				String addOperator = CreateSymbolTable.additionOperator(words, id);
				incrementId();
				BackAdditionOperator = term();
				if(!sphFlag) {
					if(!asFlag) casl2Code += newcasl.OperatorClassCheck(op);
					else leftAssignmentCode += newcasl.OperatorClassCheck(op);
					
				} else {
					if(!asFlag) subCode += newcasl.OperatorClassCheck(op);
					else leftAssignmentCode += newcasl.OperatorClassCheck(op);
				}
				if (OperatorClassCheckout.isNumericAdditionOperator(addOperator)) {
				    CheckoutType("integer", HeadAdditionOperator, words[id][3]);
				    CheckoutType("integer", BackAdditionOperator, words[id][3]);
				} else if (OperatorClassCheckout.isLogicAdditionOperator(addOperator)) {
				    CheckoutType("boolean", HeadAdditionOperator, words[id][3]);
				    CheckoutType("boolean", BackAdditionOperator, words[id][3]);
				}
			}
			
			if (writelnFlag) {
			    if (isStringToWrite()) {
			        handleStringWrite();
			    } else if (isIdentifierToWrite() && endArray) {
			        handleIdentifierWrite();

			    } else if (isArrayElementToWrite()) {
			        handleArrayWrite();
			    }
			}
			
			return HeadAdditionOperator;
		}
	    
	 // 마이너스 표현식 유효성 검사
	    private boolean isValidMinusExpression(String wordtype) {
	        return words[id][1].equals("SMINUS") &&
	               !"SCONSTANT".equals(words[id + 1][1]) &&
	               !"SLBRACKET".equals(words[id + 2][1]) &&
	               wordtype != null && 
	               "integer".equals(wordtype);
	    }

	    // 마이너스 상수 처리 (괄호 안에 있을 경우)
	    private boolean isMinusConstantInParentheses() {
	        return "SLPAREN".equals(words[id - 3][1]) &&
	               "SMINUS".equals(words[id - 2][1]) &&
	               "SCONSTANT".equals(words[id - 1][1]) &&
	               "SRPAREN".equals(words[id][1]);
	    }

	    // 독립적인 마이너스 처리
	    private boolean isStandaloneMinus() {
	        return SLPARENSTACK == 0 &&
	               minusFlag &&
	               !"SLBRACKET".equals(words[id - 2][1]);
	    }

	    // 변수 중복 체크
	    private boolean isVariableDuplicationDetected() {
	        return cst.checkVariableDuplication(words[id + 1][0], true) &&
	               ("SLPAREN".equals(words[id - 2][1]) || "SASSIGN".equals(words[id - 2][1]));
	    }
	 // 문자열 출력 처리
	    private boolean isStringToWrite() {
	        return "SSTRING".equals(words[id - 1][1]);
	    }

	    private void handleStringWrite() {
	        String stringword = words[id - 1][0];
	        String caslstringcode = stringword.length() != 3 ? newcasl.generateCode("WRTSTR", stringword, StringCharList.size() - 1) : newcasl.generateCode("WRTCH");
	        appendGeneratedCode(caslstringcode);
	    }

	    // 식별자 출력 처리
	    private boolean isIdentifierToWrite() {
	        return "SIDENTIFIER".equals(words[id - 1][1]);
	    }

	    private void handleIdentifierWrite() {
	        String symbolType = cst.findSymbolType(words[id - 1][0]);
	        String generatedCode = switch (symbolType) {
	            case "integer", "Array integer" -> newcasl.generateCode("WRTINT");
	            case "char", "Array char" -> newcasl.generateCode("WRTCH");
	            default -> null;
	        };
	        if (generatedCode != null) {
	            appendGeneratedCode(generatedCode);
	        }
	    }

	    // 배열 요소 출력 처리
	    private boolean isArrayElementToWrite() {
	        return "SRBRACKET".equals(words[id - 1][1]);
	    }

	    private void handleArrayWrite() {
	        String name = findArrayName();
	        String symbolType = cst.findSymbolType(name);
	        String generatedCode = switch (symbolType) {
	            case "integer", "Array integer" -> newcasl.generateCode("WRTINT");
	            case "char", "Array char" -> newcasl.generateCode("WRTCH");
	            default -> null;
	        };
	        if (generatedCode != null) {
	            appendGeneratedCode(generatedCode);
	        }
	    }

	 // 배열 이름 찾기
	    private String findArrayName() {
	        String arrayName = "";
	        for (int i = id - 1; i > 1; i--) { // id 바로 앞에서 시작
	            if ("SLBRACKET".equals(words[i][1])) {
	                arrayName = words[i - 1][0]; // SLBRACKET 바로 앞의 값을 가져옴
	                break; // 원하는 결과를 찾았으면 루프 종료
	            }
	        }
	        return arrayName;
	    }


	    // 생성된 코드 추가
	    private void appendGeneratedCode(String code) {
	        if (!sphFlag) {
	        	casl2Code += code;
	        } else {
	            subCode += code;
	        }
	    }
	    
	 // CASL 마이너스 코드 추가
	    private void appendCaslMinus() {
	        if (!sphFlag) {
	        	casl2Code += newcasl.caslminus();
	        } else {
	            subCode += newcasl.caslminus();
	        }
	    }
	    
	    //행
	    public String term() throws CallSyntaxError , CallSementicError {
			String HeadMultiplicationOperator, BackMultiplicationOperator;
			HeadMultiplicationOperator = factor();
	        while (OperatorClassCheckout.isMultipleOperator(words[id][1])) {
	        	String op = words[id][0];
	            String multiOperator = CreateSymbolTable.multiplicationOperator(words,id);
	            incrementId();
	            BackMultiplicationOperator = factor();
	            if (!asFlag) {
	                if (!sphFlag) {
	                    casl2Code += newcasl.OperatorClassCheck(op);
	                } else {
	                    subCode += newcasl.OperatorClassCheck(op);
	                }
	            } else {
	                leftAssignmentCode += newcasl.OperatorClassCheck(op);
	            }

		   			
	            if (OperatorClassCheckout.isNumericMultipleOperator(multiOperator)) {
	            	    CheckoutType("integer", HeadMultiplicationOperator, words[id][3]);
	            	    CheckoutType("integer", BackMultiplicationOperator, words[id][3]);
	            } else if (OperatorClassCheckout.isLogicMultipleOperator(multiOperator)) {
	            	    CheckoutType("boolean", HeadMultiplicationOperator, words[id][3]);
	            	    CheckoutType("boolean", BackMultiplicationOperator, words[id][3]);
	            }
	        }
	   
	        return HeadMultiplicationOperator;
	        
		}
	 // 상수 정의
	    private static final int MAX_INTEGER_VALUE = 32767;
	    // 인자 처리
	    public String factor() throws CallSyntaxError, CallSementicError {
	        if (matchtoken("SNOT")) {     	
	            return checkoutNotFactor();
	        } else if (checkCurrentToken("SIDENTIFIER")) {
	            return variable("identifier");
	        } else if (matchtoken("SLPAREN")) {
	        	SLPARENSTACK++;
	            return checkoutParenthesizedExpression();
	        } else{    		
	            return constant();
	        }
	    }

	    // NOT 처리
	    private String checkoutNotFactor() throws CallSyntaxError, CallSementicError {
	        String checkfactor = factor();
	        CheckoutType(checkfactor, "boolean", words[id][3]);
	        if(!sphFlag) casl2Code += newcasl.caslnot();
			else subCode += newcasl.caslnot();
	        return checkfactor;
	    }


	    // 괄호로 감싼 표현식 처리
	    private String checkoutParenthesizedExpression() throws CallSyntaxError, CallSyntaxError {
	        String checkexpression = expression();
	        if (!matchtoken("SRPAREN")) {
	            throw new CallSyntaxError(words[id][3]);
	        }
	        SLPARENSTACK--;
	        return checkexpression;
	    }
	 // 상수 처리
	    public String constant() throws CallSyntaxError, CallSementicError {
	        if (matchtoken("SCONSTANT")) {
	            handleCaslCode("CONST", words[id - 1][0]);
	            return handleIntegerConstant();
	        } 
	        if (matchtoken("SSTRING")) {
	            return handleStringConstant();
	        }
	        if (matchtoken("SFALSE") || matchtoken("STRUE")) {
	            handleCaslCode("CONST", words[id - 1][0]);
	            return "boolean";
	        }
	        
	        throw new CallSyntaxError(words[id][3]);
	    }

	    // CASL 코드 처리
	    private void handleCaslCode(String type, String tokenValue) {
	        String caslcode = newcasl.pushMethod(type, tokenValue);
	        if (!assignmentcheck) {
	            appendToCaslOrSubCode(caslcode);
	        } else {
	        	leftAssignmentCode += caslcode;
	        }
	    }

	    // 문자열 상수 처리
	    private String handleStringConstant() {
	        String Stringcode = words[id - 1][0];
	        if (Stringcode.length() != 3) {
	        	StringCharList.add(Stringcode);
	        } else {
	            appendToCaslOrSubCode(newcasl.pushMethod("CHAR", Stringcode));
	        }
	        return "char";
	    }

	    // CASL 코드 또는 서브 코드에 추가
	    private void appendToCaslOrSubCode(String code) {
	        if (!sphFlag) {
	        	casl2Code += code;
	        } else {
	            subCode += code;
	        }
	    }
	    
	    private String handleIntegerConstant() throws CallSementicError {
	        int integerValue = Integer.parseInt(words[id - 1][0]);
	        boolean isWithinRange = integerValue >= 0 && integerValue <= MAX_INTEGER_VALUE;
	        if (!isWithinRange) {
	            throw new CallSementicError(words[id][3]);
	        }
	        return "integer";
	    }
	    public Boolean writelnFlag = false;
		  //입출력문
	    public void inputOutputStatement() throws CallSyntaxError , CallSementicError {
	        if (matchtoken("SREADLN")) {
	            if (matchtoken("SLPAREN")) {
	            	arrangeVariableSequence();
	                require("SRPAREN");
	            }
	        } else if (matchtoken("SWRITELN")) {
	        	writelnFlag = true;
	            if (matchtoken("SLPAREN")) {
	            	arrangeExpressionSequence();
	                require("SRPAREN");
	                if(!sphFlag) casl2Code += newcasl.generateCode("WRTLN");
					else subCode += newcasl.generateCode("WRTLN");
	            }
                writelnFlag = false;
	        }
	    }
		// 변수 나열
	    public void arrangeVariableSequence() throws CallSyntaxError , CallSementicError{
	    	variable("arrange"); //variable
	        while (matchtoken("SCOMMA")) {
	        	variable("arrange"); // 중복된 변수 처리 //variable
	        }
	    }
	    

	}