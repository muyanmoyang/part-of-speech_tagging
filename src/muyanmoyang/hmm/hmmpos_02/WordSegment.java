package muyanmoyang.hmm.hmmpos_02;
import java.io.File;
import java.util.*;
import java.awt.*;
import javax.swing.*;
public class WordSegment {
	Dictionary dic;
	int algorithm =1;//默认采用正向最大匹配算法
	File dataFile, dicFile;
	int MaxWordLength = 5;
	long UsefulWords = 0;
	String seperator = " ";//分词之间的分隔符
	public WordSegment(){
		
	}
	public void SetDic(File dicFile){
		this.dicFile = dicFile;
	}
	public void setAlgorithm(int type){
		algorithm = type;
	}
	public StringBuilder dataProc(String str){
		dic = new Dictionary(dicFile);
		StringBuilder strr = null;
		if(algorithm == 2)
			strr = BMM(str);
		else
			strr = FMM(str);
	    return strr;
	}
	public void setDataFile(File dataFile){
		this.dataFile = dataFile;
	}
	public StringBuilder FMM(String s1){//正向最大匹配
		StringBuilder s2 = new StringBuilder(""); //用s2存放分词结果
		while(!s1.isEmpty())
		{
			int len = s1.length(); // 取输入串长度
			if (len > MaxWordLength) // 如果输入串长度大于最大词长
			{
				len = MaxWordLength; // 只在最大词长范围内进行处理
			}
			String w = s1.substring(0,len); // 将输入串左边等于最大词长长度串取出作为候选词
			boolean b = dic.FindWord(w); // 在词典中查找相应的词
			while(len > 1 && b == false) // 如果不是词
			{
				len -= 1; // 从候选词右边减掉一个汉字，将剩下的部分作为候选词
				w = w.substring(0, len); 
				if(w.length()==1)
					break;
				b = dic.FindWord(w);
			}
			if(w.charAt(0)!='\n' && w.charAt(0)!=' ' && w!=null){
				s2 = s2.append(w + seperator);// 将匹配得到的词连同词界标记加到输出串末尾
				UsefulWords ++; //记录分出的词语个数
			}
			s1 = s1.substring(w.length()); //从s1-w处开始
		}
		
		return s2;
		}
	public StringBuilder BMM(String s1){//逆向最大匹配
		StringBuilder s2 = new StringBuilder(""); //存放分词结果
		while(!s1.isEmpty())
		{
			int len = s1.length(); // 取输入串长度
			if (len > MaxWordLength) // 如果输入串长度大于最大词长
			{
				len = MaxWordLength; // 只在最大词长范围内进行处理
			}
			String w = s1.substring(s1.length()- len, s1.length()); 
			boolean b = dic.FindWord(w); // 在词典中查找相应的词
			while(len > 1 && b == false ) // 如果不是词,如果是单个字则直接输出来
			{
				len -= 1; // 从候选词左边减掉一个汉字，将剩下的部分作为候选词
				w = s1.substring(s1.length()-len, s1.length()); 

				if(w.length()==1)
					break;
				b = dic.FindWord(w);
			}
			if(w.charAt(0)!='\n' && w.charAt(0)!=' '){
				s2 = s2.append( w + seperator);// 将匹配得到的词连同词界标记加到输出串末尾
				UsefulWords ++;
			}
			s1 = s1.substring(0, s1.length()-w.length()); // 
		}
		return s2;
	}
	public long getWords(){
		return UsefulWords;
	}
}
