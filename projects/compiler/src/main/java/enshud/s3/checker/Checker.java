package enshud.s3.checker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import enshud.s2.parser.CallSementicError;
import enshud.s2.parser.CallSyntaxError;
import enshud.s2.parser.ParseStructure;
import enshud.s2.parser.CreateSymbolTable;
import enshud.s2.parser.OperatorClassCheckout;



public class Checker {

	public static void main(final String[] args) {
		// 정상파일테스트
		//new Checker().run("data/ts/normal17.ts");
		new Checker().run("data/ts/normal08.ts");

		// 구문오류
		//new Checker().run("data/ts/synerr03.ts");
		//new Checker().run("data/ts/synerr02.ts");

		// 의미오류
		//new Checker().run("data/ts/semerr08.ts");
		//new Checker().run("data/ts/semerr06.ts");
	}
	

	  public String run(final String inputFileName) {
	        List<String> data = readFile(inputFileName);
	        if (data == null) {
	            return "File not found";
	        }

	        try {
	            checkFileData(data);
	            return "OK";
	        } catch (CallSyntaxError e) {
	            return e.getMessage();
	        }catch (CallSementicError e) {
	        	return (e.getMessage());
	        }
	    }

	  private List<String> readFile(String inputFileName) {
	        List<String> data = new ArrayList<>();
	        try (BufferedReader reader = new BufferedReader(new FileReader(inputFileName))) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                data.add(line); // 각 줄을 ArrayList에 저장
	            }
	        } catch (IOException e) {
	            return null; // 파일 읽기 오류 발생 시 null 반환
	        }
	        return data;
	    }

	    private void checkFileData(List<String> data) {
	        // 1. 데이터에서 각 줄을 탭으로 나누어 2차원 배열로 변환
	    	List<String[]> wordsList = new ArrayList<>();

	        for (String line : data) {
	            // 각 줄을 탭으로 나누어 배열로 변환 후 리스트에 추가
	            wordsList.add(line.split("\t"));
	        }

	        // 2. ParseStructure 객체 생성
	        ParseStructure parseStructure = new ParseStructure(wordsList.toArray(new String[0][]));
	        
	        // 3. 프로그램 처리 시작
	        parseStructure.program();
	    }

	}
