package enshud.s4.compiler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import enshud.s2.parser.CallSementicError;
import enshud.s2.parser.CallSyntaxError;

import enshud.s2.parser.CreateSymbolTable;
import enshud.s4.compiler.ParseStructure;
import enshud.casl.CaslSimulator;

public class Compiler {

	/**
	 * サンプルmainメソッド．
	 * 単体テストの対象ではないので自由に改変しても良い．
	 */
	public static void main(final String[] args) {
		// Compilerを実行してcasを生成する
		//System.out.println(new Compiler().run("tmp/testXX.ts", "tmp/test.cas"));
		System.out.println(new Compiler().run("data/ts/normal20.ts", "tmp/out.cas"));

		// 上記casを，CASLアセンブラ & COMETシミュレータで実行する
		CaslSimulator.run("tmp/out.cas", "tmp/out.ans");
	}
	String maincaslCode;
	/**
	 * TODO
	 * 
	 * 開発対象となるCompiler実行メソッド．
	 * 以下の仕様を満たすこと．
	 * 
	 * 仕様:
	 * 第一引数で指定されたtsファイルを読み込み，CASL IIプログラムにコンパイルする．
	 * コンパイル結果のCASL IIプログラムは第二引数で指定されたcasファイルに書き出し"OK"という文字列を返すこと．
	 * 構文的もしくは意味的なエラーを発見した場合はエラーメッセージを返すこと．
	 * （エラーメッセージの内容はChecker.run()の出力に準ずる．）
	 * 入力ファイルが見つからない場合は"File not found"を返すこと．
	 * 
	 * @param inputFileName 入力tsファイル名
	 * @param outputFileName 出力casファイル名
	 */
	public String run(final String inputFileName, final String outputFileName) {
	    List<String> data = readFile(inputFileName);
	    String caslcodepara;
	    String content;
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append("CASL\tSTART\tBEGIN\t;\n");
	    sb.append("BEGIN\tLAD\tGR6, 0\t;\n");
	    sb.append("\tLAD\tGR7, LIBBUF\t;");
	    String BASIC_STATEMENT = sb.toString();

	    // outputFileName에 기본 내용을 먼저 작성
	    writeFile(outputFileName, BASIC_STATEMENT, false);
	    
	    if (data == null) {
	        return "File not found";
	    }

	    try {
	        caslcodepara = checkFileData(data);
	        //System.out.println(caslcodepara);
	    } catch (CallSyntaxError e) {
	        return e.getMessage();
	    } catch (CallSementicError e) {
	        return e.getMessage();
	    }



	    // append 메서드의 로직을 직접 구현
	    writeFile(outputFileName, caslcodepara, true);
	    //content = readFileContents(outputFileName);

	    //String fileContents = readFileContents(outputFileName);
	    //System.out.println("Output File Content: \n" + fileContents);
	    CaslSimulator.appendLibcas(outputFileName);
	    
	    return "OK";
	}

	// 파일 쓰기 함수
	private void writeFile(String outputFileName, String content, boolean append) {
	    try (BufferedWriter writingfile = new BufferedWriter(new FileWriter(outputFileName, append))) {
	    	writingfile.write(content);
	    	writingfile.newLine();
	    } catch (IOException error) {
	        System.err.println("Error writing to file: " + error.getMessage());
	    }
	}

	private String readFileContents(final String fileName) {
	    StringBuilder content = new StringBuilder();
	    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
	        String line;
	        while ((line = reader.readLine()) != null) {
	            content.append(line).append(System.lineSeparator());
	        }
	    } catch (IOException e) {
	        System.err.println("Error reading file: " + e.getMessage());
	    }
	    return content.toString();
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

    private String checkFileData(List<String> data) {
    	
        // 1. 데이터에서 각 줄을 탭으로 나누어 2차원 배열로 변환
    	List<String[]> wordsList = new ArrayList<>();

        for (String line : data) {
            // 각 줄을 탭으로 나누어 배열로 변환 후 리스트에 추가
            wordsList.add(line.split("\t"));
        }

        // 2. ParseStructure 객체 생성
        ParseStructure parseStructure = new ParseStructure(wordsList.toArray(new String[0][]));
        
        // 3. 프로그램 처리 시작
        maincaslCode = parseStructure.program();
        
        return maincaslCode;
    }

}
