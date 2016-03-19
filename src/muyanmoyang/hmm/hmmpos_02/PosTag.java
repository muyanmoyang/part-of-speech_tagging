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
	public StringBuilder[] Viterbi()throws Exception{//ͳ��
		 long timeBegin1, timeEnd1,timeBegin2,timeEnd2;
		 //ͳ�Ƴ�ѵ�������д������༰��Ƶ��
		  String content = "";//���Ͽ�ȫ���ַ���
		  String tagResult = "";//��Ŵ��Ա�ע���
		  int bufSize = 11*1024*1024;//�����С����Ӱ�쵽�ִ��ٶ�
		  byte[] bs = new byte[bufSize];

		  timeBegin1 = System.currentTimeMillis();
		  //����ͨ��+����ķ�ʽ��ȡ���Ͽ�
		  ByteBuffer byteBuf = ByteBuffer.allocate(bufSize);
		  File file=new File("pos//train2.txt");
		  FileInputStream fileInputStream = new FileInputStream(file);  
          //���ڶ�ȡ��д�롢ӳ��Ͳ����ļ���ͨ����  
          FileChannel fileChannel = fileInputStream.getChannel();  
          //�����ַ������ַ����뷽�������,���ڴ�������,���Ը���  
          Charset charset = Charset.forName("UTF-8");  
          int size=fileChannel.read(byteBuf);
          byteBuf.flip();  
          CharBuffer charBuffer = charset.decode(byteBuf);  
          content=charBuffer.toString(); 
          fileInputStream.close();  
          fileChannel.close();
		  timeEnd1 = System.currentTimeMillis();

		  System.out.println("����Ƶ��ͳ��ʱ��: "+(timeEnd1-timeBegin1));
		  timeBegin1 = System.currentTimeMillis();
		  
		  String[] text;  //text[]���ڴ洢ѵ�������еĴ���
		  text=content.split("(/[a-z]*\\s{0,})|(][a-z]*\\s{1,})"); //ȥ�����Ա�ע
		  System.out.println("���Ͽ��д���������" + text.length);
		  
		  String[] temp;  //temp[]�������ڴ洢�����ʵĴ��Ա�ע����
		  temp=content.split("[0-9|-]*/|\\s{1,}[^a-z]*|][a-z]"); //���������Ա�ע����
		  
		  
		  String[] temp1;
		  temp1=new String[temp.length-1];//ȥ��temp[0]Ϊ�յ����
		  for(int i=0;i<temp.length-1;i++)
			  temp1[i]=temp[i+1];
	      
	      String[] temp2;  //temp2[]�������ڴ洢ÿ�����ʵĴ��Ա�ע����
	      temp2=new String[temp1.length-1];
	      for(int i=0;i<temp1.length-1;i++){
	    	  temp2[i]=temp1[i]+','+temp1[i+1];
	    	  //System.out.println(temp1[i]);
	      }
		  String[] word_pos;//word_pos���ÿ���ʼ�����ԣ����硰����,n��
		  word_pos=new String[text.length];
		  for(int i=0;i<text.length;i++)
		  {
			  word_pos[i]=text[i]+','+temp1[i];
		  }
		  Hashtable hash1=new Hashtable();//����hash1���洢ÿ�����Լ����ֵĴ���
		  for(int i=0;i<temp1.length;i++)
		  {
		    if(hash1.containsKey(temp1[i]))
		    	hash1.put(temp1[i],new Integer(hash1.get(temp1[i]).hashCode()+1));
		    else
		    	hash1.put(temp1[i],new Integer(1));
		  }
		  //System.out.println(hash1);//Ҳ����ֱ�����hash1�еļ���ֵ
		  
		  int sp=hash1.size();  //ͳ��ÿ�����Գ��ֵĴ���
     
		  /*
		  for(Iterator it=hash1.keySet().iterator();it.hasNext();){ //����hash1
		     String key =(String)it.next(); 
		     Integer value  =(Integer)hash1.get(key);
		     System.out.print(key + value + " ");
		  }
		  */
		  Hashtable hash2=new Hashtable();  //����hash2���洢ÿ�����ʵĴ��Լ���Ƶ��
		  for(int i=0;i<temp2.length;i++)
		  {
		    if(hash2.containsKey(temp2[i]))
		    	hash2.put(temp2[i],new Integer(hash2.get(temp2[i]).hashCode()+1));
		    else
		    	hash2.put(temp2[i],new Integer(1));
		  }
		  
		   Hashtable hash3=new Hashtable();  //����hash3,�洢������Ժʹ�Ƶ
			  for(int i=0;i<word_pos.length;i++)
			  {
			    if(hash3.containsKey(word_pos[i]))
			    	hash3.put(word_pos[i],new Integer(hash3.get(word_pos[i]).hashCode()+1));
			    else
			    	hash3.put(word_pos[i],new Integer(1));
			  }
			  

	     String[] table_pos;  //table_pos[]���ڴ洢���в�ͬ�Ĵ��Է���,ͬhash1
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
		  System.out.println("���Ͽ�ӹ��� "+(timeEnd1-timeBegin1));
		  timeBegin1 = System.currentTimeMillis();
	     /* ����״̬ת�Ƹ���
	     1��״̬ת��(���Ե����Ե�ת��)���ʾ��󡣴���֮���ת�Ƹ��ʣ�
	     P(ti|ti-1) = ѵ��������ti������ti-1֮��Ĵ��� / ѵ��������ti-1���ֵĴ��� 
	     */
	     double[][] status; //  status[i][j]���ڴ洢ת�Ƹ���,��ʾ��״̬jת�Ƶ�״̬i�ĸ��ʡ�
	     status=new double[sp][sp];
	     for(int i=0;i<sp;i++)  //��ʼ��
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
	     

		  
	     /*Viterbi�㷨�����д��Ա�ע���ҳ�the best path
	      */

		     /*���㷢�����
		      2����״̬(����)�۲쵽�������(����)�ĸ��ʷֲ�������֪���Ա��ti���������wi�ĸ��ʣ�
				P(wi|ti) = ѵ��������wi�Ĵ��Ա����Ϊti�Ĵ��� / ѵ��������ti���ֵ��ܴ���  
		     */

		     String[] test;
		     test=sentence.split(" ");
		     int sw=0;  //��¼test.txt�д��������
		     sw=test.length;
		     double[][] observe;  //observe[i][j]��ʾ�ڴ���״̬Sj�£��������Oi�ĸ��ʡ�
		     observe=new double[sw][sp];
		     for(int i=0;i<sw;i++)  //��ʼ��
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
			  System.out.println("����״̬ת�Ƹ��ʺͷ������ "+(timeEnd1-timeBegin1));
			  timeBegin1 = System.currentTimeMillis();

	     double[][] path;  //path[][]�洢ÿ�������ÿ�����Ե�������
	     path=new double[sw][sp];
	     for(int i=0;i<sw;i++)
	     	for(int j=0;j<sp;j++)
	     	  path[i][j]=0.0;

	     int[][] backpointer;  //backpointer[][]��¼��������ÿ������ȡ��������ʱ����Ӧ��ǰһ�����Ե�λ��
	     backpointer=new int[sw][sp];
	     for(int i=0;i<sw;i++)
	     	for(int j=0;j<sp;j++)
	     	  backpointer[i][j]=0;
	     
	     for(int s=0;s<sp;s++) //��test[]�еĵ�һ���ʣ���ʼ����ÿ�������²����ôʵĸ��ʡ� 
	     {   
	       path[0][s]=Math.log(((double)hash1.get(table_pos[s]).hashCode()/(double)temp1.length)*100000000)+observe[0][s];
	     }
	     //for(int s=0;s<sp;s++)
	       //System.out.println(path[0][s]);    
	     for(int i=1;i<sw;i++)  //��test[]��ʣ�µĴʣ����μ��㵥�����Զ�Ӧ�������ʲ���¼��λ��
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
		  System.out.println("���������� "+(timeEnd1-timeBegin1));

		  timeBegin1 = System.currentTimeMillis();
	     //���ݱ������ҳ���������·��,������ 
	     int maxindex=0;   //��¼�����ı������һ����ȡ�������ʵ�λ�á�
	     double max=path[sw-1][0];
	     for(int i=1;i<sp;i++)
	     {      
	       if(path[sw-1][i]>max)
	       {
	         maxindex=i;
	         max=path[sw-1][maxindex]; 
	       }   
	     }

	     StringBuilder[] result;  //�洢���Ա�ע���
	     String[] object;  //�洢������е����д��ԣ����ڼ��㾫ȷ��
	     result=new StringBuilder[sw];
	     object=new String[sw];
	     result[sw-1]=new StringBuilder(test[sw-1]+'/'+table_pos[maxindex]);
	     object[sw-1]=table_pos[maxindex];
	     int t=0;
	     int front=maxindex;
	     for(int i=sw-2;i>=0;i--)//result�д�ŵ��Ǵ��Ա�ע���ս���������Ҫ�Ļ����Խ������鷵��
	     {
	     	t=backpointer[i+1][front];
	     	result[i]=new StringBuilder(test[i]+'/'+table_pos[t]+"\t");
	     	object[i]=table_pos[t];
	     	front=t;
	     }
		  timeEnd1 = System.currentTimeMillis();
		  System.out.println("���ݱ��� "+(timeEnd1-timeBegin1));
		  
		 timeBegin1 = System.currentTimeMillis();
		 
	     outputResult(result);   //���ִʽ��д���ļ�
	     
		 timeEnd1 = System.currentTimeMillis();
		 System.out.println("������ "+(timeEnd1-timeBegin1));
		 
		 return(result);
	}
	private void outputResult(StringBuilder[] result) throws IOException{
		//�Ѵ��Ա�ע�Ľ��д���ⲿ�ļ�
		File fileResult = new File("pos//result.txt");
		if(fileResult.exists())
			fileResult.delete();	
		fileResult.createNewFile();
		int bufSize = 2*1024*1024;//�����С
	    ByteBuffer byteBuf = ByteBuffer.allocate(bufSize);
	    FileChannel channel1 = new RandomAccessFile(fileResult,"rw").getChannel();
	    for(int i=0;i<result.length;i++){	
	    	channel1.position(channel1.size());
	    	channel1.write(ByteBuffer.wrap(result[i].toString().getBytes()));
	    }
	    //channel1.close();
	}
	
}
