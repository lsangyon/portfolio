package enshud.s2.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    public String[][] words;

    public static void main(final String[] args) {
        // 정상적인 파일 테스트
         new Parser().run("data/ts/normal01.ts");
        // new Parser().run("data/ts/normal02.ts");

        // 구문 오류 테스트
        new Parser().run("data/ts/synerr01.ts");
         new Parser().run("data/ts/synerr02.ts");
    }


    /**
     * Parser 실행 메서드
     * @param inputFileName 입력 ts 파일 이름
     */
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
