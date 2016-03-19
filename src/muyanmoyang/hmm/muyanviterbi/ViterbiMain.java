package muyanmoyang.hmm.muyanviterbi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**Viterbi�㷨 �����Ĵ��Ա�ע
 * @author moyang
 * @mail 1348229670@qq.com
 */
public class ViterbiMain {
	
	public static void main(String[] args) throws IOException, InterruptedException { 
		System.out.println("��ʼ����ѵ���������ݣ�ͳ�ƴ��������Ƶ�ʲ������ı��ļ������Ե�......"); 
		ArrayList list = getPartOfSpeech1() ;    // ͳ�Ƴ�ѵ�������д������༰��Ƶ��
		System.out.println("������� ! �����Ե�ָ��Ŀ¼�鿴�ļ�!");
		System.out.println("��ʼ����״̬ת�Ƹ��ʡ�������ʵļ���,���Ե�......");
		Hashtable hash1 = (Hashtable) list.get(1) ;
		Hashtable hash2 = (Hashtable) list.get(2) ;
		Hashtable hash3 = (Hashtable) list.get(3) ;
		String[] table_pos = (String[]) list.get(4) ;
		String[] temp1  = (String[]) list.get(5) ;
		double[][] status = computeStatusProbability(hash1, hash2, table_pos) ; //����״̬ת�Ƹ���
		double[][] observe = computeObserveProb(hash1, hash3 ,table_pos) ;//���㷢�����(�۲����)
		System.out.println("������� ! ����ļ������� !");
		Thread.sleep(5000) ;
		System.out.println("��ʼ�����㷨׼ȷ�ʲ���,���Ե�......");
		testAlgrithm(hash1 ,table_pos , temp1 ,observe , status) ; // �����㷨
	} 
	
	/**
	 *  ͳ�Ƴ�ѵ�������д������༰��Ƶ��
	 *  @return ArrayList �����ĸ�String[]�� word_pos ��"�� ,����".........
	 *  									hash1 ��"����(���Ա�ע����),Ƶ��"......
	 *  									hash2 ��"ÿ�����ʵĴ��Լ�Ƶ��",.......
	 *  									hash3 ��"��,����,Ƶ��".........
	 *  									table_pos�� ���ڴ洢���в�ͬ�Ĵ��Է���
	 *  									temp1 : temp1[]�������ڴ洢�����ʵĴ��Ա�ע����
	 * @throws IOException  
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList getPartOfSpeech1() throws IOException 
 	{
		ArrayList list =  new ArrayList() ; 
		String content = "" ;
		BufferedReader BR = null ; 
		BR = new BufferedReader(new FileReader("G:/&�̾�ɮ/�ı��ھ�/Project/Wordpos/199801train.txt")) ;
		String line ;  
		while((line = BR.readLine()) != null)
		{
			content += line ;
		}
		BR.close() ;
		
		String[] text  ;// text[]���ڴ洢ѵ�������еĴ���
		text = content.split("(/[a-z]*\\s{0,})|(][a-z]*\\s{1,})") ;// ȥ�����Ա�ע
		
		String[] temp; // temp[]�������ڴ洢�����ʵĴ��Ա�ע����
		temp = content.split("[0-9|-]*/|\\s{1,}[^a-z]*|][a-z]"); // ���������Ա�ע����
		
		String[] temp1 ;// ȥ��temp[0]Ϊ�յ����
		temp1 = new String[temp.length -1] ;
		for(int i=0 ; i<temp.length-1 ; i++)
		{
			temp1[i] = temp[i+1] ; 
		}
		String[] temp2  ; // temp2[]�������ڴ洢ÿ�����ʵĴ��Ա�ע���� 
		temp2 = new String[temp1.length - 1] ;
		for(int j=0 ; j<temp1.length-1 ; j++)
		{
			temp2[j] = temp1[j] + " , " + temp1[j+1] ; 
		}
		
		//��String["��,����","","", .......]��ʽ�������ʹ��ԣ���д��ָ���ļ�
		String[] word_pos  ;   
		word_pos = new String[text.length] ;
		FileWriter word_posWriter = new FileWriter(new File("G:/&�̾�ɮ/�ı��ھ�/Project/Wordpos/word_pos/�� ,����.txt")) ;
		for(int i=0 ; i<text.length ; i++)
		{
			word_pos[i] = text[i] + "," + temp1[i] ;
			word_posWriter.write(word_pos[i] + "\n") ;
		}
		
		// ����hash1���洢�����ʵĴ��Լ���Ƶ��,��д��hash1.txt
		Hashtable hash1 = new Hashtable() ;
		for(String word : temp1)
		{
			if(hash1.containsKey(word))
			{
				hash1.put(word,hash1.get(word).hashCode() + 1) ;
			}else
			{
				hash1.put(word,1);
			}
		}
		System.out.println("---------����------����Ƶ��------------" + "\n" + hash1); 
		writeHash2Txt(hash1,"����,Ƶ��.txt") ;
		
		
		//����hash2���洢ÿ�����ʵĴ��Լ���Ƶ�ʣ���д��hash2.txt
		Hashtable hash2 = new Hashtable() ;
		for(String word : temp2)
		{
			if(hash2.containsKey(word))
			{
				hash2.put(word,hash2.get(word).hashCode() + 1) ;
			}else
			{
				hash2.put(word, 1) ;
			}
		}
		writeHash2Txt(hash2,"ÿ�����ʵĴ��Լ�Ƶ��.txt") ;
		
		// ����hash3,�洢������Ժʹ�Ƶ����д��hash3.txt
		Hashtable hash3 = new Hashtable() ;
		for(String word : word_pos)
		{
			if(hash3.containsKey(word))
			{
				hash3.put(word,hash3.get(word).hashCode() + 1) ;
			}else
			{
				hash3.put(word,1) ;
			}
		}
		writeHash2Txt(hash3,"��,����,Ƶ��.txt") ;
		
		// table_pos[]���ڴ洢���в�ͬ�Ĵ��Է��ţ���д��table_pos_���Է���.txt
		FileWriter posWriter = new FileWriter(new File("G:/&�̾�ɮ/�ı��ھ�/Project/Wordpos/word_pos/���в�ͬ�Ĵ��Է���.txt")) ;
		String[] table_pos ;
		table_pos = new String[hash1.size()] ; 
		Enumeration key = hash1.keys() ;
		for(int i=0 ;i<table_pos.length ; i++)
		{
			String str = (String) key.nextElement();
			table_pos[i] = str ;
			posWriter.write(table_pos[i] + "\n") ;
		}
		
		word_posWriter.flush();
		posWriter.flush() ;
		word_posWriter.close() ;
		posWriter.close() ;
		
		list.add(word_pos) ;
		list.add(hash1) ;
		list.add(hash2) ;
		list.add(hash3) ;
		list.add(table_pos) ;
		list.add(temp1) ;
		return list ;
	}
	
	/**
	 * ����״̬ת�Ƹ���
	 * @param hash1     "����(���Ա�ע����),Ƶ��"
	 * @param hash2	    "ÿ�����ʵĴ��Լ�Ƶ��"
	 * @param table_pos   ���ڴ洢���в�ͬ�Ĵ��Է���
	 * @return status[i][j]���ڴ洢ת�Ƹ���,��ʾ��״̬jת�Ƶ�״̬i�ĸ���
	 * @throws IOException  
	 */
	public static double[][] computeStatusProbability(Hashtable hash1 , Hashtable hash2 , String[] table_pos) throws IOException
	{ 
		FileWriter statusProMatrixWriter = new FileWriter(new File("G:/&�̾�ɮ/�ı��ھ�/Project/Wordpos/word_pos/ת�Ƹ��ʾ���.txt")) ;
		
		int posSize = hash1.size() ; //ͳ�ƴ��Ը���
		double status[][]  ; 
		status = new double[posSize][posSize] ;
		for(int i=0 ; i<posSize ; i++)
		{
			for(int j=0 ; j<posSize ; j++)
			{
				status[i][j] = 0.0 ;
			}
		}
		for(int i=0 ; i<posSize ;i++)
		{
			for(int j=0 ; j<posSize ; j++)
			{
				String word = table_pos[j] ;
				String str = word + " , " + table_pos[i] ;
				if(hash2.containsKey(str))
				{
//					System.out.println(hash2.get(str).hashCode()); 
//					System.out.println(hash1.get(word).hashCode());
					
					status[i][j] = Math
					.log(((double) hash2.get(str).hashCode() / (double) hash1
							.get(word).hashCode()) * 100000000);
				}else
				{
					status[i][j] = Math.log((1 / ((double) hash1.get(word)
							.hashCode() * 1000)) * 100000000);
				}
			}
		}
		
		for(int i=0 ; i<posSize ; i++)
		{
			if(i != 0)
			{
				statusProMatrixWriter.write("\n") ;
			}
			for(int j=0 ; j<posSize ; j++)
			{
				statusProMatrixWriter.write(status[i][j] + "    ") ;
			}
		}
		statusProMatrixWriter.flush() ;
		statusProMatrixWriter.close() ;
		return status ;
	}

	/**
	 * ���㷢�����(�۲����)
	 * @param hash1    "����,Ƶ��"
	 * @param hash3	   "��,����,Ƶ��"
	 * @param table_pos   ���ڴ洢���в�ͬ�Ĵ��Է���
	 * @return observe[][] ���ڴ洢�������,��ʾ�ڸ���һ���������к�õ��Ĵʵ���ϵĸ���
	 * @throws IOException 
	 */
	public static double[][] computeObserveProb(Hashtable hash1 , Hashtable hash3 , String[] table_pos) throws IOException
	{
		FileWriter observeProbMatrixWriter = new FileWriter(new File("G:/&�̾�ɮ/�ı��ھ�/Project/Wordpos/word_pos/������ʾ���.txt")) ;
		
		String[] test ;
		test = getTestSentence().split("(/[a-z]*\\s{0,})|(][a-z]*\\s{1,})") ;//ȥ�����Լ��Ĵ��Ա�ע
		
		int sizeOfTest = test.length ;  
		int posSize = hash1.size() ;
		
		double observe[][] = new double[sizeOfTest][posSize]  ; 
		for(int i=0 ; i<sizeOfTest ; i++)
		{
			for(int j=0 ; j<posSize ; j++)
			{
				observe[i][j] = 0.0 ;
			}
		}
		
		for(int i=0 ; i<sizeOfTest ; i++)
		{
			for(int j=0 ; j<posSize ; j++)
			{
				String testWord = test[i];
				String word_pos = table_pos[j] ; 
				String tempStr = testWord + "," + word_pos ;
				if(hash3.containsKey(tempStr))  //ĳ���ʵĴ��Եķ�������ǣ�ĳ�ʳ���������Ե�Ƶ�� / ������Ե���Ƶ��
				{
					observe[i][j] = Math
					.log(((double) hash3.get(tempStr).hashCode() / (double) hash1
							.get(word_pos).hashCode()) * 100000000); 
				}else
				{
					observe[i][j] = Math.log((1 / ((double) hash1.get(word_pos)
							.hashCode() * 1000)) * 100000000);
				}
			}
		}
		for(int i=0; i<sizeOfTest ; i++)
		{
			if(i != 0)
			{
				observeProbMatrixWriter.write("\n") ; 
			}
			for(int j=0 ; j<posSize ; j++)
			{
				observeProbMatrixWriter.write(observe[i][j] + "    ") ;
			}
		}
		observeProbMatrixWriter.flush() ;
		observeProbMatrixWriter.close() ;
		return observe ;
	}
	
	/**
	 * Viterbi�㷨�����д��Ա�ע���ҳ�����ܵ�����״̬���У�����������
	 * @param hash1
	 * @param table_pos
	 * @return 
	 * @throws IOException  
	 */
	public static String[] viterbi(Hashtable hash1, String[] table_pos , String[] temp1 , double[][] observe ,double[][] status) throws IOException
	{
		String[] test ;
		test = getTestSentence().split("(/[a-z]*\\s{0,})|(][a-z]*\\s{1,})") ;//ȥ�����Լ��Ĵ��Ա�ע
		int sizeOfTest = test.length ;  
		int posSize = hash1.size() ;
		
		double[][] path = new double[sizeOfTest][posSize] ; // path[][]�洢���������������
		for(int i=0 ;i<sizeOfTest ; i++)
		{
			for(int j=0; j<posSize ; j++)
			{
				path[i][j] = 0.0 ;
			}
		}
		int[][] backpointer  = new int[sizeOfTest][posSize] ; //backpointer[][]��¼��������ÿ������ȡ��������ʱ����Ӧ��ǰһ�����Ե�λ��
		for(int i=0 ;i<sizeOfTest ; i++)
		{
			for(int j=0; j<posSize ; j++)
			{
				backpointer[i][j] = 0 ;
			}
		}
		// ��test[]�еĵ�һ���ʣ���ʼ����ÿ�������²����ôʵĸ���
		for(int x=0 ; x<posSize ; x++)  
		{
			path[0][x] = Math
			.log(((double) hash1.get(table_pos[x]).hashCode() / (double) temp1.length) * 100000000)
			+ observe[0][x];
		}
		// ��test[]��ʣ�µĴʣ����μ��㵥�����Զ�Ӧ�������ʲ���¼��λ��
		for(int i=1; i<sizeOfTest ; i++)
		{
			for(int j=0; j<posSize ;j++)
			{
				double maxProb = path[i-1][0] + status[j][0] + observe[i][j];
				int index = 0 ;
				for(int k=0 ; k<posSize ;k++)
				{
					path[i][j] = path[i-1][k] + status[j][k] + observe[i][j];
					if(path[i][j] > maxProb)
					{
						index = k ;
						maxProb = path[i][j] ;
					}
				}
				backpointer[i][j] = index ;
				path[i][j] = maxProb;
			}
		}
		// ���ݱ������ҳ���������·��,������
		int maxindex = 0; // ��¼�����ı������һ����ȡ�������ʵ�λ�á�
		double max = path[sizeOfTest - 1][0];
		for (int i = 1; i < posSize; i++) {
			if (path[sizeOfTest - 1][i] > max) {
				maxindex = i;
				max = path[sizeOfTest - 1][maxindex];
			}
		}
		String[] result; // �洢���Ա�ע���
		String[] object; // �洢������е����д��ԣ����ڼ��㾫ȷ��
		result = new String[sizeOfTest];
		object = new String[sizeOfTest];
		result[sizeOfTest - 1] = test[sizeOfTest - 1] + '/' + table_pos[maxindex];
		object[sizeOfTest - 1] = table_pos[maxindex];
		int t = 0;
		int front = maxindex;
		for (int i = sizeOfTest - 2; i >= 0; i--) {
			t = backpointer[i + 1][front];
			result[i] = test[i] + '/' + table_pos[t] + "  ";
			object[i] = table_pos[t];
			front = t;
		}
		try {
			FileWriter f = new FileWriter("G:/&�̾�ɮ/�ı��ھ�/Project/Wordpos/word_pos/result.txt");
			for (int i = 0; i < result.length; i++)
				f.write(result[i] + "\n");
			f.flush();
			f.close();
		} catch (IOException e) {
			System.out.println("����");
		}
		return object ; 
	}
	

	public static void testAlgrithm(Hashtable hash1 , String[] table_pos , String[] temp1 , double[][] observe , double[][] status) throws IOException
	{
		String[] test ;
		test = getTestSentence().split("(/[a-z]*\\s{0,})|(][a-z]*\\s{1,})") ;//ȥ�����Լ��Ĵ��Ա�ע
		int sizeOfTest = test.length ;  
		
		int correct = 0;
		double correct_rate = 0.0;
		String[] source; // source[]�������ڴ洢�����ı��е����ʵĴ��Ա�ע����
		source = getTestSentence().split("[0-9|-]*/|\\s{1,}[^a-z]*|][a-z]"); // ���������Ա�ע���š�
		String[] source1;
		source1 = new String[source.length - 1];// ȥ��source[0]Ϊ�յ����
		for (int i = 0; i < source.length - 1; i++)
			source1[i] = source[i + 1];

		String[] object = viterbi(hash1, table_pos, temp1, observe, status) ;
		
		for (int i = 0; i < sizeOfTest; i++) {
			if (source1[i].equals(object[i]))
				correct++;
		}
		correct_rate = (double) correct / (double) sizeOfTest * 100;
		System.out.println("���Խ������: "); 
		System.out.println("��ȷ��ע������:" + correct + "  " + "��ע���ܴ�����" + sizeOfTest);
		System.out.println("��ȷ�ʣ�" + correct_rate + "%");
	}
	
	
	/**
	 * ��ȡtest�ı�
	 * @return
	 * @throws IOException
	 */
	private static String getTestSentence() throws IOException { 
		// TODO Auto-generated method stub
		String sentence = "" ;
		FileReader testFileReader = new FileReader(new File("G:/&�̾�ɮ/�ı��ھ�/Project/Wordpos/199801test.txt")) ;
		BufferedReader BR = new BufferedReader(testFileReader) ;
		String line  ;
		while((line = BR.readLine()) != null)
		{
			sentence += line ;
		}
		return sentence ;
	}
	/**
	 * ��Hashtableд��text�ļ�
	 * @param hash
	 * @param textName
	 * @throws IOException
	 */
	private static void writeHash2Txt(Hashtable hash, String textName) throws IOException {
		// TODO Auto-generated method stub
		FileWriter hashWriter = new FileWriter(new File("G:/&�̾�ɮ/�ı��ھ�/Project/Wordpos/word_pos/" + textName)) ;
		Set<Map.Entry<String,Integer>> hash_set1 = hash.entrySet() ;
		Iterator it = hash_set1.iterator() ;
		
		while(it.hasNext())
		{
			hashWriter.write(it.next().toString() + "\n") ;
		}
		hashWriter.flush() ;
		hashWriter.close() ;
	}
	
}
