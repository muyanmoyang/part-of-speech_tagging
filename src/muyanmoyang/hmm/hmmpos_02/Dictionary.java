package muyanmoyang.hmm.hmmpos_02;
import java.io.*;
import java.util.*;

public class Dictionary{
	private HashMap dic = new HashMap();
	private int maxWordLength = 0;
	private long numbers=0;//词典中词语个数
	
	public Dictionary(File file){//将词典读入HashMap中
		long timeBegin1, timeEnd1;
		double timeUsed1;
		String str;
		timeBegin1 = System.currentTimeMillis();
		try{
		InputStreamReader  in = new InputStreamReader (new FileInputStream(file),"UTF-8");
		BufferedReader reader=new BufferedReader(in); 
		while((str=reader.readLine())!= null){
			if(str.length() > maxWordLength)
				maxWordLength = str.length();
			dic.put(str,new Integer(1));
			numbers++;
		}
		}catch(IOException e){
			e.printStackTrace();
		}
	      timeEnd1 = System.currentTimeMillis();
	      timeUsed1 = timeEnd1 - timeBegin1;
	      System.out.println("获取词典时间 ："+timeUsed1);
	      System.out.println("词典中词语个数: "+numbers);
	}
	public boolean FindWord(String word){//在词典中查找word
		if (dic.get(word) == null)
			return false;
		else
			return true;
	}
	public HashMap getDict(){
		return dic;
	}
	public int getMaxWordLength(){
		return maxWordLength;
	}
	public long getNumbers(){
		return numbers;
	}
}
