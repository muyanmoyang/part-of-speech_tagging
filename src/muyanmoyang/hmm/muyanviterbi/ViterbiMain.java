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

/**Viterbi算法 、中文词性标注
 * @author moyang
 * @mail 1348229670@qq.com
 */
public class ViterbiMain {
	
	public static void main(String[] args) throws IOException, InterruptedException { 
		System.out.println("开始处理训练样本数据，统计词性种类和频率并生成文本文件，请稍等......"); 
		ArrayList list = getPartOfSpeech1() ;    // 统计出训练样本中词性种类及其频率
		System.out.println("处理完毕 ! 您可以到指定目录查看文件!");
		System.out.println("开始进行状态转移概率、发射概率的计算,请稍等......");
		Hashtable hash1 = (Hashtable) list.get(1) ;
		Hashtable hash2 = (Hashtable) list.get(2) ;
		Hashtable hash3 = (Hashtable) list.get(3) ;
		String[] table_pos = (String[]) list.get(4) ;
		String[] temp1  = (String[]) list.get(5) ;
		double[][] status = computeStatusProbability(hash1, hash2, table_pos) ; //计算状态转移概率
		double[][] observe = computeObserveProb(hash1, hash3 ,table_pos) ;//计算发射概率(观察概率)
		System.out.println("计算完毕 ! 相关文件已生成 !");
		Thread.sleep(5000) ;
		System.out.println("开始进行算法准确率测试,请稍等......");
		testAlgrithm(hash1 ,table_pos , temp1 ,observe , status) ; // 测试算法
	} 
	
	/**
	 *  统计出训练样本中词性种类及其频率
	 *  @return ArrayList 包括四个String[]： word_pos ："词 ,词性".........
	 *  									hash1 ："词性(词性标注符号),频率"......
	 *  									hash2 ："每两个词的词性及频率",.......
	 *  									hash3 ："词,词性,频率".........
	 *  									table_pos： 用于存储所有不同的词性符号
	 *  									temp1 : temp1[]数组用于存储单个词的词性标注符号
	 * @throws IOException  
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList getPartOfSpeech1() throws IOException 
 	{
		ArrayList list =  new ArrayList() ; 
		String content = "" ;
		BufferedReader BR = null ; 
		BR = new BufferedReader(new FileReader("G:/&烟酒僧/文本挖掘/Project/Wordpos/199801train.txt")) ;
		String line ;  
		while((line = BR.readLine()) != null)
		{
			content += line ;
		}
		BR.close() ;
		
		String[] text  ;// text[]用于存储训练样本中的词语
		text = content.split("(/[a-z]*\\s{0,})|(][a-z]*\\s{1,})") ;// 去除词性标注
		
		String[] temp; // temp[]数组用于存储单个词的词性标注符号
		temp = content.split("[0-9|-]*/|\\s{1,}[^a-z]*|][a-z]"); // 仅保留词性标注符号
		
		String[] temp1 ;// 去除temp[0]为空的情况
		temp1 = new String[temp.length -1] ;
		for(int i=0 ; i<temp.length-1 ; i++)
		{
			temp1[i] = temp[i+1] ; 
		}
		String[] temp2  ; // temp2[]数组用于存储每两个词的词性标注符号 
		temp2 = new String[temp1.length - 1] ;
		for(int j=0 ; j<temp1.length-1 ; j++)
		{
			temp2[j] = temp1[j] + " , " + temp1[j+1] ; 
		}
		
		//以String["词,词性","","", .......]形式保存词语和词性，并写入指定文件
		String[] word_pos  ;   
		word_pos = new String[text.length] ;
		FileWriter word_posWriter = new FileWriter(new File("G:/&烟酒僧/文本挖掘/Project/Wordpos/word_pos/词 ,词性.txt")) ;
		for(int i=0 ; i<text.length ; i++)
		{
			word_pos[i] = text[i] + "," + temp1[i] ;
			word_posWriter.write(word_pos[i] + "\n") ;
		}
		
		// 创建hash1，存储单个词的词性及其频率,并写入hash1.txt
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
		System.out.println("---------词性------出现频率------------" + "\n" + hash1); 
		writeHash2Txt(hash1,"词性,频率.txt") ;
		
		
		//创建hash2，存储每两个词的词性及其频率，并写入hash2.txt
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
		writeHash2Txt(hash2,"每两个词的词性及频率.txt") ;
		
		// 创建hash3,存储词语、词性和词频，并写入hash3.txt
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
		writeHash2Txt(hash3,"词,词性,频率.txt") ;
		
		// table_pos[]用于存储所有不同的词性符号，并写入table_pos_词性符号.txt
		FileWriter posWriter = new FileWriter(new File("G:/&烟酒僧/文本挖掘/Project/Wordpos/word_pos/所有不同的词性符号.txt")) ;
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
	 * 计算状态转移概率
	 * @param hash1     "词性(词性标注符号),频率"
	 * @param hash2	    "每两个词的词性及频率"
	 * @param table_pos   用于存储所有不同的词性符号
	 * @return status[i][j]用于存储转移概率,表示由状态j转移到状态i的概率
	 * @throws IOException  
	 */
	public static double[][] computeStatusProbability(Hashtable hash1 , Hashtable hash2 , String[] table_pos) throws IOException
	{ 
		FileWriter statusProMatrixWriter = new FileWriter(new File("G:/&烟酒僧/文本挖掘/Project/Wordpos/word_pos/转移概率矩阵.txt")) ;
		
		int posSize = hash1.size() ; //统计词性个数
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
	 * 计算发射概率(观察概率)
	 * @param hash1    "词性,频率"
	 * @param hash3	   "词,词性,频率"
	 * @param table_pos   用于存储所有不同的词性符号
	 * @return observe[][] 用于存储发射概率,表示在给定一个词性序列后得到的词的组合的概率
	 * @throws IOException 
	 */
	public static double[][] computeObserveProb(Hashtable hash1 , Hashtable hash3 , String[] table_pos) throws IOException
	{
		FileWriter observeProbMatrixWriter = new FileWriter(new File("G:/&烟酒僧/文本挖掘/Project/Wordpos/word_pos/发射概率矩阵.txt")) ;
		
		String[] test ;
		test = getTestSentence().split("(/[a-z]*\\s{0,})|(][a-z]*\\s{1,})") ;//去除测试集的词性标注
		
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
				if(hash3.containsKey(tempStr))  //某个词的词性的发射概率是：某词出现这个词性的频率 / 这个词性的总频率
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
	 * Viterbi算法，进行词性标注，找出最可能的隐藏状态序列，即词性序列
	 * @param hash1
	 * @param table_pos
	 * @return 
	 * @throws IOException  
	 */
	public static String[] viterbi(Hashtable hash1, String[] table_pos , String[] temp1 , double[][] observe ,double[][] status) throws IOException
	{
		String[] test ;
		test = getTestSentence().split("(/[a-z]*\\s{0,})|(][a-z]*\\s{1,})") ;//去除测试集的词性标注
		int sizeOfTest = test.length ;  
		int posSize = hash1.size() ;
		
		double[][] path = new double[sizeOfTest][posSize] ; // path[][]存储单个词语的最大概率
		for(int i=0 ;i<sizeOfTest ; i++)
		{
			for(int j=0; j<posSize ; j++)
			{
				path[i][j] = 0.0 ;
			}
		}
		int[][] backpointer  = new int[sizeOfTest][posSize] ; //backpointer[][]记录单个词中每个词性取得最大概率时所对应的前一个词性的位置
		for(int i=0 ;i<sizeOfTest ; i++)
		{
			for(int j=0; j<posSize ; j++)
			{
				backpointer[i][j] = 0 ;
			}
		}
		// 对test[]中的第一个词，初始化在每个词性下产生该词的概率
		for(int x=0 ; x<posSize ; x++)  
		{
			path[0][x] = Math
			.log(((double) hash1.get(table_pos[x]).hashCode() / (double) temp1.length) * 100000000)
			+ observe[0][x];
		}
		// 对test[]中剩下的词，依次计算单个词性对应的最大概率并记录其位置
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
		// 回溯遍历，找出概率最大的路径,输出结果
		int maxindex = 0; // 记录测试文本中最后一个词取得最大概率的位置。
		double max = path[sizeOfTest - 1][0];
		for (int i = 1; i < posSize; i++) {
			if (path[sizeOfTest - 1][i] > max) {
				maxindex = i;
				max = path[sizeOfTest - 1][maxindex];
			}
		}
		String[] result; // 存储词性标注结果
		String[] object; // 存储结果集中的所有词性，用于计算精确度
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
			FileWriter f = new FileWriter("G:/&烟酒僧/文本挖掘/Project/Wordpos/word_pos/result.txt");
			for (int i = 0; i < result.length; i++)
				f.write(result[i] + "\n");
			f.flush();
			f.close();
		} catch (IOException e) {
			System.out.println("错误");
		}
		return object ; 
	}
	

	public static void testAlgrithm(Hashtable hash1 , String[] table_pos , String[] temp1 , double[][] observe , double[][] status) throws IOException
	{
		String[] test ;
		test = getTestSentence().split("(/[a-z]*\\s{0,})|(][a-z]*\\s{1,})") ;//去除测试集的词性标注
		int sizeOfTest = test.length ;  
		
		int correct = 0;
		double correct_rate = 0.0;
		String[] source; // source[]数组用于存储测试文本中单个词的词性标注符号
		source = getTestSentence().split("[0-9|-]*/|\\s{1,}[^a-z]*|][a-z]"); // 仅保留词性标注符号。
		String[] source1;
		source1 = new String[source.length - 1];// 去除source[0]为空的情况
		for (int i = 0; i < source.length - 1; i++)
			source1[i] = source[i + 1];

		String[] object = viterbi(hash1, table_pos, temp1, observe, status) ;
		
		for (int i = 0; i < sizeOfTest; i++) {
			if (source1[i].equals(object[i]))
				correct++;
		}
		correct_rate = (double) correct / (double) sizeOfTest * 100;
		System.out.println("测试结果如下: "); 
		System.out.println("正确标注词性数:" + correct + "  " + "标注的总词数：" + sizeOfTest);
		System.out.println("正确率：" + correct_rate + "%");
	}
	
	
	/**
	 * 读取test文本
	 * @return
	 * @throws IOException
	 */
	private static String getTestSentence() throws IOException { 
		// TODO Auto-generated method stub
		String sentence = "" ;
		FileReader testFileReader = new FileReader(new File("G:/&烟酒僧/文本挖掘/Project/Wordpos/199801test.txt")) ;
		BufferedReader BR = new BufferedReader(testFileReader) ;
		String line  ;
		while((line = BR.readLine()) != null)
		{
			sentence += line ;
		}
		return sentence ;
	}
	/**
	 * 将Hashtable写入text文件
	 * @param hash
	 * @param textName
	 * @throws IOException
	 */
	private static void writeHash2Txt(Hashtable hash, String textName) throws IOException {
		// TODO Auto-generated method stub
		FileWriter hashWriter = new FileWriter(new File("G:/&烟酒僧/文本挖掘/Project/Wordpos/word_pos/" + textName)) ;
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
