package enshud.s2.parser;
 

 
public class ParseStructure {
	
	// CASL 코드 생성 메서드


    private String[][] words; // 단어 배열
    int id = 0; // ID 값
 
	boolean avdFlag = false;
	boolean asFlag = false;
	boolean sphFlag = false;
	boolean pcsFlag = false;
	
	CreateSymbolTable cst = new CreateSymbolTable();
    
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
	        if (avdFlag) {
	        	cst.checkAndAddVariable(varName, false, null, words[id][3]);
	        } else if (asFlag) {
	        	cst.checkVariableExists(varName, true, words[id][3], null);
	        }
	    }
	    
	    public void handleProcedureDeclarationOrAssignment(String varName) throws CallSyntaxError, CallSementicError {
	    	if (sphFlag) {
	    		cst.checkAndAddVariable(varName, false, "procedure", words[id][3]);
			}
			if (pcsFlag) {
				cst.checkVariableExists(varName, true, words[id][3], null);
			}
	    }

	    public void incrementId() {
	        id++;
	    }

 
	    //프로그램
	  public void program() throws CallSyntaxError {
	        require("SPROGRAM");
	        programName();
	        require("SSEMICOLON");
	        block();
	        compoundStatement();
	        require("SDOT");
	        

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
	    	avdFlag = true;
		do {
			arrangeVariableName();
            require("SCOLON");
            type();
            require("SSEMICOLON");
        } while (checkCurrentToken("SIDENTIFIER"));
		avdFlag = false;
	}
	    
	public void type() throws CallSyntaxError, CallSementicError {
	// type() 메서드 다시 분리
	    if (checkCurrentToken("SARRAY")) {
	    	arrayType();
	    	//varType = "Array";
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
    		throw new CallSyntaxError(words[id][3]);
    	}
		cst.updateSymbolType(words[id-1][0]);
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
	        standardType();
	    } else {
	        throw new CallSyntaxError(words[id][3]);
	    }
	}
	private int arrayMin;
	private int arrayMax;
	 //최소
    public void MinimumId() throws CallSyntaxError, CallSementicError {
    	checkSign();
    	arrayMin = Integer.parseInt(words[id-1][0]);
    	System.out.println(arrayMin);
    }
    //최대
    public void MaximumId() throws CallSyntaxError , CallSementicError{
    	checkSign();

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
    // 부프로그램 정의
    public void subProgramDeclarement() throws CallSyntaxError, CallSementicError {
    	subProgramHeader();
        varDeclarement(); // 변수 선언
        compoundStatement(); // 복합 문장
        cst.exitScope();
	}
    

    //부프로그램대가리 메서드 역할 명확히
    public void subProgramHeader() throws CallSyntaxError , CallSementicError{
		require("SPROCEDURE");
		sphFlag = true;
		procedureName();
		cst.enterScope();
		sphFlag = false;
		TemporalParameter();      
		require("SSEMICOLON");
	}
    //과정이름
    public void procedureName() throws CallSyntaxError, CallSementicError {
		require("SIDENTIFIER");
		String varName = words[id - 1][0];
		handleProcedureDeclarationOrAssignment(varName);
	}
  //임시파라미터
    public void TemporalParameter() throws CallSyntaxError, CallSementicError {
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
        while (matchtoken("SSEMICOLON")) {
        	arrangeTemporalParameterSequence();
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
    
  //if문
    public void ifStatement() throws CallSyntaxError, CallSementicError {
    	cst.enterScope();
		String ifMeaning;
		ifMeaning = expression();
		require("STHEN");
			CheckoutType(ifMeaning, "boolean", words[id][3]);
		compoundStatement();
		if(matchtoken("SELSE")) {
			compoundStatement();
		}
		cst.exitScope();
	}
	
    // while문
    public void whileStatement() throws CallSyntaxError, CallSementicError {
    	cst.enterScope();
		 String whileMeaning;
		 whileMeaning = expression();
		 	CheckoutType(whileMeaning, "boolean", words[id][3]);
			
			if(!matchtoken("SDO")) {
				throw new CallSyntaxError(words[id][3]);
			}
			compoundStatement();
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
		//System.out.println(words[id+1][1]);
        String wordEx = words[id+1][1];
        if (wordEx.equals("SLPAREN") || wordEx.equals("SSEMICOLON")) { // 문의 나열, 대입문
            procedureCallStatement();
        } else {
            assignmentStatement(); // 대입문 :=
        }
    }
	
	//대입문
    public void assignmentStatement() throws CallSyntaxError, CallSementicError {    
		String varType, eqType;
		asFlag = true;
		varType = variable();
		asFlag = false;
		if(checkCurrentToken("SLBRACKET")) {
			subscriptedvariable();
		}
		require("SASSIGN");
		eqType = expression();
		CheckoutType(varType, eqType, words[id][3]);
	}

    public String variable() throws CallSyntaxError, CallSementicError {
        String RealType = variableName();
        String SubscriptType = subscriptedvariable();
        
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


    //첨자변수확인
    public String subscriptedvariable() throws CallSyntaxError, CallSementicError {
    	cst.enterScope();
		String subscriptidType = null;
		 if (matchtoken("SLBRACKET")) {
			 subscriptidType = id();
			require("SRBRACKET");
		}
		 cst.exitScope();
		return subscriptidType;
	}
    
	//添字
	public String id() throws CallSyntaxError, CallSementicError{
		String newexpression = expression();
		CheckoutType(newexpression, "integer", words[id][3]);
		return newexpression;
	}
	//콜변수
    public void procedureCallStatement() throws CallSyntaxError, CallSementicError {
    	pcsFlag = true;
		procedureName();
		pcsFlag = false;
		 if (matchtoken("SLPAREN")){
			 arrangeExpressionSequence();
			require("SRPAREN");
		}
	}
  //식 나열
    public void arrangeExpressionSequence() throws CallSyntaxError , CallSementicError {
        do {
        	expression();
        } while (matchtoken("SCOMMA"));
    }
	//식
	public String expression() throws CallSyntaxError, CallSementicError {
		String HeadRelationalOperator;
		String BackRelationalOperator;
		
		HeadRelationalOperator = simpleExpression();
		if (OperatorClassCheckout.isRelationalOperator(words[id][1])) {
            relationalOperator();
            BackRelationalOperator = simpleExpression();
			CheckoutType(HeadRelationalOperator, BackRelationalOperator, words[id][3]);
			return "boolean";
		}
		return HeadRelationalOperator;
	}
	
	// 관계 연산자
    public void relationalOperator() throws CallSyntaxError , CallSementicError {
    	if(OperatorClassCheckout.isRelationalOperator(words[id][1])) {
    		incrementId();
    	}else{
    		throw new CallSyntaxError(words[id][3]);
    	}
    }

    //단순식
    public String simpleExpression() throws CallSyntaxError , CallSementicError {
		String HeadAdditionOperator, BackAdditionOperator;
	    	if ("+".equals(words[id][0])) {
	    		require("SPLUS");
	    	}else if ("-".equals(words[id][0])) {
	    		require("SMINUS");
	    	}
	    	HeadAdditionOperator = term();
		while(OperatorClassCheckout.isAdditionOperator(words[id][1])) {
			String addOperator = CreateSymbolTable.additionOperator(words, id);
			incrementId();
			BackAdditionOperator = term();
			//System.out.println(addOperator);
			if (OperatorClassCheckout.isNumericAdditionOperator(addOperator)) {
				//System.out.println(term1);
				//System.out.println(term2);
			    CheckoutType("integer", HeadAdditionOperator, words[id][3]);
			    CheckoutType("integer", BackAdditionOperator, words[id][3]);
			} else if (OperatorClassCheckout.isLogicAdditionOperator(addOperator)) {
			    CheckoutType("boolean", HeadAdditionOperator, words[id][3]);
			    CheckoutType("boolean", BackAdditionOperator, words[id][3]);
			}

		}
		return HeadAdditionOperator;
	}
    //행
    public String term() throws CallSyntaxError , CallSementicError {
		String HeadMultiplicationOperator, BackMultiplicationOperator;
		HeadMultiplicationOperator = factor();
        while (OperatorClassCheckout.isMultipleOperator(words[id][1])) {
        	//System.out.println(words[id][0]);
            String multiOperator = CreateSymbolTable.multiplicationOperator(words,id);
            incrementId();
            //System.out.println(words[id][0]);
            BackMultiplicationOperator = factor();
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
            return variable();
        } else if (matchtoken("SLPAREN")) {
            return checkoutParenthesizedExpression();
        } else {
            return constant();
        }
    }

    // NOT 처리
    private String checkoutNotFactor() throws CallSyntaxError, CallSementicError {
        String checkfactor = factor();
        CheckoutType(checkfactor, "boolean", words[id][3]);
        return checkfactor;
    }


    // 괄호로 감싼 표현식 처리
    private String checkoutParenthesizedExpression() throws CallSyntaxError, CallSyntaxError {
        String checkexpression = expression();
        if (!matchtoken("SRPAREN")) {
            throw new CallSyntaxError(words[id][3]);
        }
        return checkexpression;
    }
    
 // 상수 처리
    public String constant() throws CallSyntaxError, CallSementicError {
        if (matchtoken("SCONSTANT")) {
            return handleIntegerConstant();
        } else if (matchtoken("SSTRING")) {
            return "char";
        } else if (matchtoken("SFALSE") || matchtoken("STRUE")) {
            return "boolean";
        } else {
            throw new CallSyntaxError(words[id][3]);
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

	  //입출력문
    public void inputOutputStatement() throws CallSyntaxError , CallSementicError {
        if (matchtoken("SREADLN")) {
            if (matchtoken("SLPAREN")) {
            	arrangeVariableSequence();
                require("SRPAREN");
            }
        } else if (matchtoken("SWRITELN")) {
            if (matchtoken("SLPAREN")) {
            	arrangeExpressionSequence();
                require("SRPAREN");
            }
        }
    }
	// 변수 나열
    public void arrangeVariableSequence() throws CallSyntaxError , CallSementicError{
    	variable(); //variable
        while (matchtoken("SCOMMA")) {
        	variable(); // 중복된 변수 처리 //variable
        }
    }
}