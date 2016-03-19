package muyanmoyang.hmm.hmmpos_02;
import java.io.File;
import java.util.*;
import java.awt.*;
import javax.swing.*;
public class WordSegment {
	Dictionary dic;
	int algorithm =1;//Ĭ�ϲ����������ƥ���㷨
	File dataFile, dicFile;
	int MaxWordLength = 5;
	long UsefulWords = 0;
	String seperator = " ";//�ִ�֮��ķָ���
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
	public StringBuilder FMM(String s1){//�������ƥ��
		StringBuilder s2 = new StringBuilder(""); //��s2��ŷִʽ��
		while(!s1.isEmpty())
		{
			int len = s1.length(); // ȡ���봮����
			if (len > MaxWordLength) // ������봮���ȴ������ʳ�
			{
				len = MaxWordLength; // ֻ�����ʳ���Χ�ڽ��д���
			}
			String w = s1.substring(0,len); // �����봮��ߵ������ʳ����ȴ�ȡ����Ϊ��ѡ��
			boolean b = dic.FindWord(w); // �ڴʵ��в�����Ӧ�Ĵ�
			while(len > 1 && b == false) // ������Ǵ�
			{
				len -= 1; // �Ӻ�ѡ���ұ߼���һ�����֣���ʣ�µĲ�����Ϊ��ѡ��
				w = w.substring(0, len); 
				if(w.length()==1)
					break;
				b = dic.FindWord(w);
			}
			if(w.charAt(0)!='\n' && w.charAt(0)!=' ' && w!=null){
				s2 = s2.append(w + seperator);// ��ƥ��õ��Ĵ���ͬ�ʽ��Ǽӵ������ĩβ
				UsefulWords ++; //��¼�ֳ��Ĵ������
			}
			s1 = s1.substring(w.length()); //��s1-w����ʼ
		}
		
		return s2;
		}
	public StringBuilder BMM(String s1){//�������ƥ��
		StringBuilder s2 = new StringBuilder(""); //��ŷִʽ��
		while(!s1.isEmpty())
		{
			int len = s1.length(); // ȡ���봮����
			if (len > MaxWordLength) // ������봮���ȴ������ʳ�
			{
				len = MaxWordLength; // ֻ�����ʳ���Χ�ڽ��д���
			}
			String w = s1.substring(s1.length()- len, s1.length()); 
			boolean b = dic.FindWord(w); // �ڴʵ��в�����Ӧ�Ĵ�
			while(len > 1 && b == false ) // ������Ǵ�,����ǵ�������ֱ�������
			{
				len -= 1; // �Ӻ�ѡ����߼���һ�����֣���ʣ�µĲ�����Ϊ��ѡ��
				w = s1.substring(s1.length()-len, s1.length()); 

				if(w.length()==1)
					break;
				b = dic.FindWord(w);
			}
			if(w.charAt(0)!='\n' && w.charAt(0)!=' '){
				s2 = s2.append( w + seperator);// ��ƥ��õ��Ĵ���ͬ�ʽ��Ǽӵ������ĩβ
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
