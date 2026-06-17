package enshud.s1.lexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Lexer {

	/**
	 * サンプルmainメソッド．
	 * 単体テストの対象ではないので自由に改変しても良い．
	 */
	public static void main(final String[] args) {
		// normalの確認
		new Lexer().run("data/pas/normal01.pas", "tmp/out1.ts");
		new Lexer().run("data/pas/normal02.pas", "tmp/out2.ts");
		new Lexer().run("data/pas/normal03.pas", "tmp/out3.ts");
		new Lexer().run("data/pas/testXX.pas", "tmp/testXX.ts");
	}


	/**
	 * TODO
	 * 
	 * 開発対象となるLexer実行メソッド．
	 * 以下の仕様を満たすこと．
	 * 
	 * 仕様:
	 * 第一引数で指定されたpasファイルを読み込み，トークン列に分割する．
	 * トークン列は第二引数で指定されたtsファイルに書き出すこと．
	 * 正常に処理が終了した場合は"OK"を，入力ファイルが見つからない場合は"File not found"を返す．
	 * 
	 * @param inputFileName 入力pasファイル名
	 * @param outputFileName 出力tsファイル名
	 * @return 
	 */
	public String run(final String inputFileName, final String outputFileName) {

        try {
            FileReader fileReader = new FileReader(inputFileName);
            BufferedReader BuffereReader = new BufferedReader(fileReader);
            String linetokens = null;
            String contenttokens = null;
            StringBuilder contentTokens = new StringBuilder(); // 결과를 누적할 StringBuilder
            int lineCount = 0;
            while ((linetokens = BuffereReader.readLine()) != null) {
            	lineCount++;
            	ConverterPro convert = new ConverterPro(linetokens,lineCount);
            	if(linetokens.isEmpty() == false && contenttokens == null) {
            		contenttokens = convert.convert();
            	}else if(linetokens.isEmpty() == false) {
            		contenttokens += convert.convert();
            		//contentTokens.append(convert.convert()); // 변환된 결과를 추가
            	}
            	
            	
            }
            BuffereReader.close();
            BufferedWriter lexerwriter = new BufferedWriter(new FileWriter(outputFileName));
            lexerwriter.write(contenttokens);
            
            //lexerwriter.write(contentTokens.toString()); // StringBuilder의 내용을 파일에 작성

            lexerwriter.close();
            //System.out.println("OK");
            return "OK";
        } catch (IOException e) {
            //System.err.println("File not found");
            return "File not found"; // 파일을 찾지 못했을 경우 반환
        }
	}
}
