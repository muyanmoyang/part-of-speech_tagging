package muyanmoyang.hmm.hmmpos_02;

import java.util.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
public class PosTag
{
	private String sentence = "";
	long timeUsed;
	
	public PosTag(StringBuilder str){
		sentence = str.toString();
	}
	public StringBuilder[] Viterbi()throws Exception{//统计
		 long timeBegin1, timeEnd1,timeBegin2,timeEnd2;
		 //统计出训练样本中词性种类及其频率
		  String content = "";//语料库全部字符串
		  String tagResult = "";//存放词性标注结果
		  int bufSize = 11*1024*1024;//缓存大小，会影响到分词速度
		  byte[] bs = new byte[bufSize];

		  timeBegin1 = System.currentTimeMillis();
		  //采用通道+缓存的方式读取语料库
		  ByteBuffer byteBuf = ByteBuffer.allocate(bufSize);
		  File file=new File("pos//train2.txt");
		  FileInputStream fileInputStream = new FileInputStream(file);  
          //用于读取、写入、映射和操作文件的通道。  
          FileChannel fileChannel = fileInputStream.getChannel();  
          //编码字符集和字符编码方案的组合,用于处理中文,可以更改  
          Charset charset = Charset.forName("UTF-8");  
          int size=fileChannel.read(byteBuf);
          byteBuf.flip();  
          CharBuffer charBuffer = charset.decode(byteBuf);  
          content=charBuffer.toString(); 
          fileInputStream.close();  
          fileChannel.close();
		  timeEnd1 = System.currentTimeMillis();

		  System.out.println("词性频率统计时间: "+(timeEnd1-timeBegin1));
		  timeBegin1 = System.currentTimeMillis();
		  
		  String[] text;  //text[]用于存储训练样本中的词语
		  text=content.split("(/[a-z]*\\s{0,})|(][a-z]*\\s{1,})"); //去除词性标注
		  System.out.println("语料库中词语总数：" + text.length);
		  
		  String[] temp;  //temp[]数组用于存储单个词的词性标注符号
		  temp=content.split("[0-9|-]*/|\\s{1,}[^a-z]*|][a-z]"); //仅保留词性标注符号
		  
		  
		  String[] temp1;
		  temp1=new String[temp.length-1];//去除temp[0]为空的情况
		  for(int i=0;i<temp.length-1;i++)
			  temp1[i]=temp[i+1];
	      
	      String[] temp2;  //temp2[]数组用于存储每两个词的词性标注符号
	      temp2=new String[temp1.length-1];
	      for(int i=0;i<temp1.length-1;i++){
	    	  temp2[i]=temp1[i]+','+temp1[i+1];
	    	  //System.out.println(temp1[i]);
	      }
		  String[] word_pos;//word_pos存放每个词及其词性，比如“读者,n”
		  word_pos=new String[text.length];
		  for(int i=0;i<text.length;i++)
		  {
			  word_pos[i]=text[i]+','+temp1[i];
		  }
		  Hashtable hash1=new Hashtable();//创建hash1，存储每个词性及出现的次数
		  for(int i=0;i<temp1.length;i++)
		  {
		    if(hash1.containsKey(temp1[i]))
		    	hash1.put(temp1[i],new Integer(hash1.get(temp1[i]).hashCode()+1));
		    else
		    	hash1.put(temp1[i],new Integer(1));
		  }
		  //System.out.println(hash1);//也可以直接输出hash1中的键和值
		  
		  int sp=hash1.size();  //统计每个词性出现的次数
     
		  /*
		  for(Iterator it=hash1.keySet().iterator();it.hasNext();){ //遍历hash1
		     String key =(String)it.next(); 
		     Integer value  =(Integer)hash1.get(key);
		     System.out.print(key + value + " ");
		  }
		  */
		  Hashtable hash2=new Hashtable();  //创建hash2，存储每两个词的词性及其频率
		  for(int i=0;i<temp2.length;i++)
		  {
		    if(hash2.containsKey(temp2[i]))
		    	hash2.put(temp2[i],new Integer(hash2.get(temp2[i]).hashCode()+1));
		    else
		    	hash2.put(temp2[i],new Integer(1));
		  }
		  
		   Hashtable hash3=new Hashtable();  //创建hash3,存储词语、词性和词频
			  for(int i=0;i<word_pos.length;i++)
			  {
			    if(hash3.containsKey(word_pos[i]))
			    	hash3.put(word_pos[i],new Integer(hash3.get(word_pos[i]).hashCode()+1));
			    else
			    	hash3.put(word_pos[i],new Integer(1));
			  }
			  

	     String[] table_pos;  //table_pos[]用于存储所有不同的词性符号,同hash1
	     table_pos=new String[sp];
	     Enumeration key=hash1.keys();    
	     for(int i=0;i<sp;i++)
	     {       
	       String str=(String)key.nextElement();
	       table_pos[i]=str;
	       //System.out.print(str+" /");
	     }
	      //System.out.println("/n");
	     
		  timeEnd1 = System.currentTimeMillis();
		  System.out.println("语料库加工： "+(timeEnd1-timeBegin1));
		  timeBegin1 = System.currentTimeMillis();
	     /* 计算状态转移概率
	     1、状态转移(词性到词性的转移)概率矩阵。词性之间的转移概率：
	     P(ti|ti-1) = 训练语料中ti出现在ti-1之后的次数 / 训练语料中ti-1出现的次数 
	     */
	     double[][] status; //  status[i][j]用于存储转移概率,表示由状态j转移到状态i的概率。
	     status=new double[sp][sp];
	     for(int i=0;i<sp;i++)  //初始化
	       for(int j=0;j<sp;j++)
	         status[i][j]=0;
	     
	     for(int i=0;i<sp;i++)
	     {
	       for(int j=0;j<sp;j++)
	       {
	       	 String wd=table_pos[j];
	         String str=wd+','+table_pos[i];
	         if(hash2.containsKey(str))
	           status[i][j]=Math.log(((double)hash2.get(str).hashCode()/(double)hash1.get(wd).hashCode())*100000000);
	         else
	           status[i][j]=Math.log((1/((double)hash1.get(wd).hashCode()*1000))*100000000);
	       }
	     }    
	     

		  
	     /*Viterbi算法，进行词性标注。找出the best path
	      */

		     /*计算发射概率
		      2、从状态(词性)观察到输出符号(单词)的概率分布矩阵。已知词性标记ti下输出词语wi的概率：
				P(wi|ti) = 训练语料中wi的词性被标记为ti的次数 / 训练语料中ti出现的总次数  
		     */

		     String[] test;
		     test=sentence.split(" ");
		     int sw=0;  //记录test.txt中词语的总数
		     sw=test.length;
		     double[][] observe;  //observe[i][j]表示在词性状态Sj下，输出词语Oi的概率。
		     observe=new double[sw][sp];
		     for(int i=0;i<sw;i++)  //初始化
		       for(int j=0;j<sp;j++)
		         observe[i][j]=0;
		     
		     for(int i=0;i<sw;i++)
		     {
		       for(int j=0;j<sp;j++)
		       {
		       	String wd=test[i];
		       	String ws=table_pos[j];
		       	String str=wd+','+ws;
		       	if(hash3.containsKey(str))
		       	  observe[i][j]=Math.log(((double)hash3.get(str).hashCode()/(double)hash1.get(ws).hashCode())*100000000);
		       	else
		       	  observe[i][j]=Math.log((1/((double)hash1.get(ws).hashCode()*1000))*100000000);
		       }
		     }
		         
			  timeEnd1 = System.currentTimeMillis();
			  System.out.println("计算状态转移概率和发射概率 "+(timeEnd1-timeBegin1));
			  timeBegin1 = System.currentTimeMillis();

	     double[][] path;  //path[][]存储每个词语的每个词性的最大概率
	     path=new double[sw][sp];
	     for(int i=0;i<sw;i++)
	     	for(int j=0;j<sp;j++)
	     	  path[i][j]=0.0;

	     int[][] backpointer;  //backpointer[][]记录单个词中每个词性取得最大概率时所对应的前一个词性的位置
	     backpointer=new int[sw][sp];
	     for(int i=0;i<sw;i++)
	     	for(int j=0;j<sp;j++)
	     	  backpointer[i][j]=0;
	     
	     for(int s=0;s<sp;s++) //对test[]中的第一个词，初始化在每个词性下产生该词的概率。 
	     {   
	       path[0][s]=Math.log(((double)hash1.get(table_pos[s]).hashCode()/(double)temp1.length)*100000000)+observe[0][s];
	     }
	     //for(int s=0;s<sp;s++)
	       //System.out.println(path[0][s]);    
	     for(int i=1;i<sw;i++)  //对test[]中剩下的词，依次计算单个词性对应的最大概率并记录其位置
	     {
	     	for(int j=0;j<sp;j++)
	     	{
	     		double maxp=path[i-1][0]+status[j][0]+observe[i][j];  
	     		int index=0;
	     		for(int k=1;k<sp;k++)
	     		{
	     		  path[i][j]=path[i-1][k]+status[j][k]+observe[i][j];
	     		  if(path[i][j]>maxp)
	     		  {
	     		    index=k;
	     		    maxp=path[i][j];
	     		  }
	     		}
	     		backpointer[i][j]=index;
	     		path[i][j]=maxp;
	     	} 
	     }
	     /*for(int i=0;i<sw;i++)
	       for(int j=0;j<sp;j++)
	         System.out.println(backpointer[sw-2][j]); */     
	     
		  timeEnd1 = System.currentTimeMillis();
		  System.out.println("计算最大概率 "+(timeEnd1-timeBegin1));

		  timeBegin1 = System.currentTimeMillis();
	     //回溯遍历，找出概率最大的路径,输出结果 
	     int maxindex=0;   //记录测试文本中最后一个词取得最大概率的位置。
	     double max=path[sw-1][0];
	     for(int i=1;i<sp;i++)
	     {      
	       if(path[sw-1][i]>max)
	       {
	         maxindex=i;
	         max=path[sw-1][maxindex]; 
	       }   
	     }

	     StringBuilder[] result;  //存储词性标注结果
	     String[] object;  //存储结果集中的所有词性，用于计算精确度
	     result=new StringBuilder[sw];
	     object=new String[sw];
	     result[sw-1]=new StringBuilder(test[sw-1]+'/'+table_pos[maxindex]);
	     object[sw-1]=table_pos[maxindex];
	     int t=0;
	     int front=maxindex;
	     for(int i=sw-2;i>=0;i--)//result中存放的是词性标注最终结果，如果需要的话可以将该数组返回
	     {
	     	t=backpointer[i+1][front];
	     	result[i]=new StringBuilder(test[i]+'/'+table_pos[t]+"\t");
	     	object[i]=table_pos[t];
	     	front=t;
	     }
		  timeEnd1 = System.currentTimeMillis();
		  System.out.println("回溯遍历 "+(timeEnd1-timeBegin1));
		  
		 timeBegin1 = System.currentTimeMillis();
		 
	     outputResult(result);   //将分词结果写入文件
	     
		 timeEnd1 = System.currentTimeMillis();
		 System.out.println("保存结果 "+(timeEnd1-timeBegin1));
		 
		 return(result);
	}
	private void outputResult(StringBuilder[] result) throws IOException{
		//把词性标注的结果写入外部文件
		File fileResult = new File("pos//result.txt");
		if(fileResult.exists())
			fileResult.delete();	
		fileResult.createNewFile();
		int bufSize = 2*1024*1024;//缓存大小
	    ByteBuffer byteBuf = ByteBuffer.allocate(bufSize);
	    FileChannel channel1 = new RandomAccessFile(fileResult,"rw").getChannel();
	    for(int i=0;i<result.length;i++){	
	    	channel1.position(channel1.size());
	    	channel1.write(ByteBuffer.wrap(result[i].toString().getBytes()));
	    }
	    //channel1.close();
	}
	
}
