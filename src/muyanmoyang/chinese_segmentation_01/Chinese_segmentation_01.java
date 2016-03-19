package muyanmoyang.chinese_segmentation_01;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.lucene.IKAnalyzer;

/**
 * 基于lucene的IK Analyzer实现的中文分词
 * 
 * @author hadoop
 * 
 */
public class Chinese_segmentation_01 {
	public static void main(String[] args) throws IOException {

		FileWriter fileWriter = new FileWriter(new File("D:/￥烟酒僧/文本挖掘/Project/Chinese_Segmentation_result.txt")) ;
		FileReader fileReader = new FileReader(new File(
				"D:/￥烟酒僧/文本挖掘/Project/Chinese_Segmentation.txt"));
		BufferedReader BR = new BufferedReader(fileReader);
		StringReader reader= null ;
		String line;
		String text = "";
		while ((line = BR.readLine()) != null) {
			text = "" ;
			text += line + "\n";
			// 创建分词对象
			Analyzer anal = new IKAnalyzer(true);
			reader = new StringReader(text);
			// 分词
			TokenStream ts = anal.tokenStream("", reader);
			CharTermAttribute term = ts.getAttribute(CharTermAttribute.class);
			// 遍历分词数据
			while (ts.incrementToken()) {
				System.out.print(term.toString() + "|");
				fileWriter.write(term.toString() + "|") ;
			}
			fileWriter.write("\n") ;
			fileWriter.flush() ;
		}
		reader.close();
	}
}
