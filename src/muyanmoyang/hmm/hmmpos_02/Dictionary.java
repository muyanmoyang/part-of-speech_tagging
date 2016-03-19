package muyanmoyang.hmm.hmmpos_02;
import java.io.*;
import java.util.*;

public class Dictionary{
	private HashMap dic = new HashMap();
	private int maxWordLength = 0;
	private long numbers=0;//�ʵ��д������
	
	public Dictionary(File file){//���ʵ����HashMap��
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
	      System.out.println("��ȡ�ʵ�ʱ�� ��"+timeUsed1);
	      System.out.println("�ʵ��д������: "+numbers);
	}
	public boolean FindWord(String word){//�ڴʵ��в���word
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
