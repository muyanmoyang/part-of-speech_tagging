package muyanmoyang.hmm.Viterbi;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.*;


public class Viterbi {
	public static void main(String[] args) {

		// ----------------------------------------------------------------------------------
		// ͳ�Ƴ�ѵ�������д������༰��Ƶ��
		String content = "";
		BufferedReader reader = null;
		try { // ��ȡ199801train.txt�ı��е����ݣ���������content���ַ�����
			reader = new BufferedReader(new FileReader("D:/���̾�ɮ/�ı��ھ�/Project/Wordpos/199801train.txt"));
			String line;
			while ((line = reader.readLine()) != null)
				content += line;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}

		String[] text; // text[]���ڴ洢ѵ�������еĴ���
		text = content.split("(/[a-z]*\\s{0,})|(][a-z]*\\s{1,})"); // ȥ�����Ա�ע
		// for(String wd:text)
		// System.out.println(wd);

		String[] temp; // temp[]�������ڴ洢�����ʵĴ��Ա�ע����
		temp = content.split("[0-9|-]*/|\\s{1,}[^a-z]*|][a-z]"); // ���������Ա�ע���š�
		String[] temp1;
		temp1 = new String[temp.length - 1];// ȥ��temp[0]Ϊ�յ����
		for (int i = 0; i < temp.length - 1; i++)
			temp1[i] = temp[i + 1];
		// for(String wd:temp1)
		// System.out.print(wd+"  ");

		String[] temp2; // temp2[]�������ڴ洢ÿ�����ʵĴ��Ա�ע����
		temp2 = new String[temp1.length - 1];
		for (int i = 0; i < temp1.length - 1; i++)
			temp2[i] = temp1[i] + ',' + temp1[i + 1];
		// for(String wd:temp2)
		// System.out.println(wd);

		String[] word_pos;
		word_pos = new String[text.length];
		for (int i = 0; i < text.length; i++)
			word_pos[i] = text[i] + ',' + temp1[i];
		// for(String wd:word_pos)
		// System.out.println(wd);

		Hashtable hash1 = new Hashtable(); // ����hash1���洢�����ʵĴ��Լ���Ƶ��
		for (String wd : temp1) {
			if (hash1.containsKey(wd))
				hash1.put(wd, hash1.get(wd).hashCode() + 1);
			else
				hash1.put(wd, 1);
		}
		int sp = hash1.size(); // ͳ�ƴ��Ը���
		// System.out.println(hash1);

		Hashtable hash2 = new Hashtable(); // ����hash2���洢ÿ�����ʵĴ��Լ���Ƶ��
		for (String wd : temp2) {
			if (hash2.containsKey(wd))
				hash2.put(wd, hash2.get(wd).hashCode() + 1);
			else
				hash2.put(wd, 1);
		}
		// System.out.println(hash2);

		Hashtable hash3 = new Hashtable(); // ����hash3,�洢������Ժʹ�Ƶ
		for (String wd : word_pos) {
			if (hash3.containsKey(wd))
				hash3.put(wd, hash3.get(wd).hashCode() + 1);
			else
				hash3.put(wd, 1);
		}
		// System.out.println(hash3);

		String[] table_pos; // table_pos[]���ڴ洢���в�ͬ�Ĵ��Է���
		table_pos = new String[sp];
		Enumeration key = hash1.keys();
		for (int i = 0; i < sp; i++) {
			String str = (String) key.nextElement();
			table_pos[i] = str;
		}
		// for(String wd:table_pos)
		// System.out.println(wd);

		
		
		
		// --------------------------------------------------------------------------------------
		// ����״̬ת�Ƹ���
		double[][] status; // status[i][j]���ڴ洢ת�Ƹ���,��ʾ��״̬jת�Ƶ�״̬i�ĸ��ʡ�
		status = new double[sp][sp];
		for (int i = 0; i < sp; i++) // ��ʼ��
		{
			for (int j = 0; j < sp; j++)
				status[i][j] = 0;
		}

		for (int i = 0; i < sp; i++) {
			for (int j = 0; j < sp; j++) {
				String wd = table_pos[j];
				String str = wd + ',' + table_pos[i];
				if (hash2.containsKey(str))
					status[i][j] = Math
							.log(((double) hash2.get(str).hashCode() / (double) hash1
									.get(wd).hashCode()) * 100000000);
				else
					status[i][j] = Math.log((1 / ((double) hash1.get(wd)
							.hashCode() * 1000)) * 100000000);
			}
		}
		/*
		 * for(int i=0;i<sp;i++) { System.out.println('\n'); for(int
		 * j=0;j<sp;j++) System.out.print(status[j][0]+"  "); }
		 */

		// -----------------------------------------------------------------------------------------
		// ���㷢�����
		String sentence = "";
		try { // ��ȡtest.txt�ı��е����ݣ���������sentence���ַ����С�
			BufferedReader str = new BufferedReader(new FileReader(
					"D:/���̾�ɮ/�ı��ھ�/Project/Wordpos/199801test.txt"));
			String line;
			while ((line = str.readLine()) != null)
				sentence += line;
		} catch (IOException e) {
			e.printStackTrace();
		}

		String[] test;
		test = sentence.split("(/[a-z]*\\s{0,})|(][a-z]*\\s{1,})");// ȥ�����Ա�ע
		int sw = 0; // ��¼test.txt�д��������
		sw = test.length;
		// for(String wd:test)
		// System.out.println(wd);
		// System.out.print(sw);

		double[][] observe; // observe[i][j]��ʾ�ڴ���״̬Sj�£��������Oi�ĸ��ʡ�
		observe = new double[sw][sp];
		for (int i = 0; i < sw; i++) // ��ʼ��
		{
			for (int j = 0; j < sp; j++)
				observe[i][j] = 0;
		}

		for (int i = 0; i < sw; i++) {
			for (int j = 0; j < sp; j++) {
				String wd = test[i];
				String ws = table_pos[j];
				String str = wd + ',' + ws;
				if (hash3.containsKey(str))
					observe[i][j] = Math
							.log(((double) hash3.get(str).hashCode() / (double) hash1
									.get(ws).hashCode()) * 100000000);
				else
					observe[i][j] = Math.log((1 / ((double) hash1.get(ws)
							.hashCode() * 1000)) * 100000000);
			}
		}
		/*
		 * for(int i=0;i<sw;i++) { for(int j=0;j<sp;j++)
		 * System.out.println(observe[j][0]); }
		 */

		// -----------------------------------------------------------------------------------------------
		// Viterbi�㷨�����д��Ա�ע���ҳ�the best path
		double[][] path; // path[][]�洢���������������
		path = new double[sw][sp];
		for (int i = 0; i < sw; i++) {
			for (int j = 0; j < sp; j++)
				path[i][j] = 0.0;
		}

		int[][] backpointer; // backpointer[][]��¼��������ÿ������ȡ��������ʱ����Ӧ��ǰһ�����Ե�λ��
		backpointer = new int[sw][sp];
		for (int i = 0; i < sw; i++) {
			for (int j = 0; j < sp; j++)
				backpointer[i][j] = 0;
		}

		for (int s = 0; s < sp; s++) // ��test[]�еĵ�һ���ʣ���ʼ����ÿ�������²����ôʵĸ��ʡ�
		{
			path[0][s] = Math
					.log(((double) hash1.get(table_pos[s]).hashCode() / (double) temp1.length) * 100000000)
					+ observe[0][s];
		}
		// for(int s=0;s<sp;s++)
		// System.out.println(path[0][s]);
		for (int i = 1; i < sw; i++) // ��test[]��ʣ�µĴʣ����μ��㵥�����Զ�Ӧ�������ʲ���¼��λ��
		{
			for (int j = 0; j < sp; j++) {
				double maxp = path[i - 1][0] + status[j][0] + observe[i][j];
				int index = 0;
				for (int k = 1; k < sp; k++) {
					path[i][j] = path[i - 1][k] + status[j][k] + observe[i][j];
					if (path[i][j] > maxp) {
						index = k;
						maxp = path[i][j];
					}
				}
				backpointer[i][j] = index;
				path[i][j] = maxp;
			}
		}
		/*
		 * for(int i=0;i<sw;i++) for(int j=0;j<sp;j++)
		 * System.out.println(backpointer[sw-2][j]);
		 */

		// ���ݱ������ҳ���������·��,������
		int maxindex = 0; // ��¼�����ı������һ����ȡ�������ʵ�λ�á�
		double max = path[sw - 1][0];
		for (int i = 1; i < sp; i++) {
			if (path[sw - 1][i] > max) {
				maxindex = i;
				max = path[sw - 1][maxindex];
			}
		}
		// System.out.println(max);

		String[] result; // �洢���Ա�ע���
		String[] object; // �洢������е����д��ԣ����ڼ��㾫ȷ��
		result = new String[sw];
		object = new String[sw];
		result[sw - 1] = test[sw - 1] + '/' + table_pos[maxindex];
		object[sw - 1] = table_pos[maxindex];
		int t = 0;
		int front = maxindex;
		for (int i = sw - 2; i >= 0; i--) {
			t = backpointer[i + 1][front];
			result[i] = test[i] + '/' + table_pos[t] + "  ";
			object[i] = table_pos[t];
			front = t;
		}

		try {
			FileWriter f = new FileWriter("E:/Desktop/result.txt");
			for (int i = 0; i < result.length; i++)
				f.write(result[i] + "");
			f.flush();
			f.close();
		} catch (IOException e) {
			System.out.println("����");
		}

		// --------------------------------------------------------------------------------------------------------
		// �����㷨Ч��
		int correct = 0;
		double correct_rate = 0.0;
		String[] source; // source[]�������ڴ洢�����ı��е����ʵĴ��Ա�ע����
		source = sentence.split("[0-9|-]*/|\\s{1,}[^a-z]*|][a-z]"); // ���������Ա�ע���š�
		String[] source1;
		source1 = new String[source.length - 1];// ȥ��source[0]Ϊ�յ����
		for (int i = 0; i < source.length - 1; i++)
			source1[i] = source[i + 1];

		for (int i = 0; i < sw; i++) {
			if (source1[i].equals(object[i]))
				correct++;
		}
		correct_rate = (double) correct / (double) sw * 100;
		System.out.println("��ȷ��ע������:" + correct + "  " + "��ע���ܴ�����" + sw);
		System.out.println("��ȷ�ʣ�" + correct_rate + "%");
	}
}
