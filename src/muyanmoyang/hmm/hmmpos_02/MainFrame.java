package muyanmoyang.hmm.hmmpos_02;

import java.awt.*;
import java.lang.*;
import java.awt.event.*;
import java.awt.ActiveEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.Vector;
import javax.swing.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.io.*;
import javax.swing.event.*;

public class MainFrame extends Thread implements ActionListener, FocusListener {
	final static int ALGO_FMM = 1;
	final static int ALGO_BMM = 2;
	private JFrame frame;
	private JMenuBar menuBar = new JMenuBar();
	private JMenu fileMenu, algorithmMenu, trainMenu, helpMenu;
	private JMenuItem openDicItem, closeItem, openDataItem;
	private JRadioButtonMenuItem fmmItem, bmmItem;// �������ƥ��˵����������ƥ��˵�
	private JMenuItem openTrainFileItem, aboutItem;
	private JButton btSeg;
	private JTextField tfInput;
	private JTextArea taOutputData, taOutputResult;
	private static int progress = 0;
	Thread thread;
	JLabel infoDic, infoAlgo, infoTrainFile;
	JProgressBar jpProgress = null;
	WordSegment seger;// �ִʶ���
	PosTag posTag;// ���Ա�ע����
	File dataFile;
	int flag = 0;

	public MainFrame() {
		frame = new JFrame();
		dataFile = null;
		seger = new WordSegment();
		frame.setTitle("CSAPT�����Զ��ִ�����Ա�ע");
		frame.setDefaultCloseOperation(0);
		frame.setJMenuBar(menuBar);

		fileMenu = new JMenu("�ļ�");
		algorithmMenu = new JMenu("�ִ��㷨");
		trainMenu = new JMenu("����");
		helpMenu = new JMenu("����");
		openDicItem = fileMenu.add("����ʵ�");
		openDataItem = fileMenu.add("��������");
		fileMenu.addSeparator();
		closeItem = fileMenu.add("�˳�");

		algorithmMenu.add(fmmItem = new JRadioButtonMenuItem("�������ƥ��", true));
		algorithmMenu.add(bmmItem = new JRadioButtonMenuItem("�������ƥ��", false));
		ButtonGroup algorithms = new ButtonGroup();
		algorithms.add(fmmItem);
		algorithms.add(bmmItem);

		openTrainFileItem = trainMenu.add("��������");
		aboutItem = helpMenu.add("����CSAPT");

		menuBar.add(fileMenu);
		menuBar.add(algorithmMenu);
		menuBar.add(trainMenu);
		menuBar.add(helpMenu);
		openDicItem.addActionListener(this);
		openDataItem.addActionListener(this);
		closeItem.addActionListener(this);
		openTrainFileItem.addActionListener(this);
		aboutItem.addActionListener(this);
		fmmItem.addActionListener(this);
		bmmItem.addActionListener(this);

		taOutputData = new JTextArea();
		taOutputData.setLineWrap(true);
		taOutputData.setAutoscrolls(true);
		taOutputData.setBorder(BorderFactory.createLineBorder(Color.blue, 1));
		taOutputData.setText("");
		taOutputData.addFocusListener(this);
		taOutputResult = new JTextArea();
		taOutputResult.setLineWrap(true);
		taOutputResult.setEditable(false);
		taOutputResult.setBorder(BorderFactory.createLineBorder(Color.blue, 1));

		JScrollPane js1 = new JScrollPane(taOutputData);
		JScrollPane js2 = new JScrollPane(taOutputResult);
		js1
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		js2
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		JPanel topPanel = new JPanel();
		JPanel centerPanel = new JPanel();
		topPanel.setLayout(new FlowLayout());
		centerPanel.setLayout(new GridLayout(1, 2, 10, 10));
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout());
		frame.add(topPanel, BorderLayout.NORTH);
		frame.add(centerPanel, BorderLayout.CENTER);
		frame.add(bottomPanel, BorderLayout.SOUTH);
		centerPanel.add(js1);
		centerPanel.add(js2);

		btSeg = new JButton(" start ");
		btSeg.setEnabled(false);
		tfInput = new JTextField("", 40);
		tfInput.setEditable(false);

		taOutputResult.setAutoscrolls(true);
		topPanel.add(tfInput);
		topPanel.add(btSeg);

		infoDic = new JLabel("");
		infoAlgo = new JLabel("�ִ��㷨���������ƥ��");

		bottomPanel.add(infoDic);
		bottomPanel.add(infoAlgo);
		jpProgress = new JProgressBar();
		jpProgress.setStringPainted(true);// ���ý��������Ƿ���ʾ���Ⱦ��������50%
		jpProgress.setMaximum(100);// �������ֵ
		jpProgress.setMinimum(0);// ������Сֵ
		jpProgress.setValue(0);// ���ó�ʼֵ
		jpProgress.setPreferredSize(new Dimension(400, 22));
		// bottomPanel.add(jpProgress);
		btSeg.addActionListener(this);
		frame.setBounds(300, 100, 770, 500);
		frame.setVisible(true);

		// seger.SetDic(new File("pos//dic.txt"));//Ĭ�ϵķִʴʵ��ļ�
	}

	private File openFile()// ���ļ������ݡ��ʵ�������Ͽ⣩
	{
		JFileChooser chooser = new JFileChooser();// �ļ�ѡ��Ի���
		int ret = chooser.showOpenDialog(frame);

		if (ret != JFileChooser.APPROVE_OPTION) {
			return null;
		}

		File f = chooser.getSelectedFile();
		if (f.isFile() && f.canRead()) {
			return f;
		} else {
			JOptionPane.showMessageDialog(frame, "Could not open file: " + f,
					"Error opening file", JOptionPane.ERROR_MESSAGE);
			return null;
		}

	}

	private void procData() throws Exception {// �������ļ������ڴ�
		long fileLength = 0;// �ļ���С
		long timeBegin1, timeEnd1, timeBegin2, timeEnd2, timeBegin3, timeEnd3;
		double timeUsed1, timeUsed2, timeUsed3;
		String sentence;
		StringBuilder strr = null;
		StringBuilder ss[];
		String str = null;
		timeUsed1 = 0;
		timeUsed2 = 0;
		long word = 0;
		jpProgress.setMinimum(0);

		if (dataFile != null) {
			fileLength = dataFile.length();
			// jpProgress.setMaximum((int)fileLength/1024);
			int bufSize = 3 * 1024 * 1024;// �����С����Ӱ�쵽�ִ��ٶ�
			byte[] bs = new byte[bufSize];
			timeBegin3 = System.currentTimeMillis();
			double speedSeg = 0.0, speedPos = 0.0;
			// �����Ƿ��仺���С��Ҳ����������Ŵ�Ӳ���ж��������ļ�
			// ʲô��һ�ΰ��ļ�����������ʵ���ǵ������С����Ӳ�����ļ���Сһ����
			// ֻͨ��һ��readָ��������ļ����ӵ��������档����Ҫһ�ζ�һ��2G���ļ����ѻ�����Ϊ2G����һ�ζ�������
			// ����������ռ��ʱ�������������Ƿ��䲻�����ģ���Ϊ�ڴ治�㡣
			ByteBuffer byteBuf = ByteBuffer.allocate(bufSize);
			FileChannel channel = new RandomAccessFile(dataFile, "r")
					.getChannel();
			int size;
			// ��Ϊ���ﻺ���С��1K������ÿ��channel.read()ָ�����ֻ������ļ���1K�����ݡ�
			// ����ļ���1M��С������for��ѭ��1024�Σ����ļ��ֿ�1024�ζ�ȡ����
			while ((size = channel.read(byteBuf)) != -1) {

				strr = new StringBuilder("");
				byteBuf.rewind();
				byteBuf.get(bs);
				str = new String(bs, 0, size);
				byteBuf.clear();

				taOutputData.append(str);
				timeEnd3 = System.currentTimeMillis();
				timeUsed3 = timeEnd3 - timeBegin3;
				System.out.println("ԭ����ʾʱ�䣺" + timeUsed3);

				word = word + str.length();
				timeBegin1 = System.currentTimeMillis();

				strr = seger.dataProc(str);// ���зִ�

				// System.out.println(str);
				timeEnd1 = System.currentTimeMillis();
				timeUsed1 = timeUsed1 + (timeEnd1 - timeBegin1);// �ִʽ���������ִ�����ʱ��

				timeBegin2 = System.currentTimeMillis();

				posTag = new PosTag(strr);
				posTag.Viterbi();// ���д��Ա�ע
				// outputResult(strr);

				timeEnd2 = System.currentTimeMillis();
				timeUsed2 = timeUsed2 + (timeEnd2 - timeBegin2);// ���Ա�ע��������������ʱ��
				// progress++;
				// System.out.println(progress);
				// Dimension d = jpProgress.getSize();
				// Rectangle rect = new Rectangle(0,0, d.width, d.height);
				// jpProgress.setValue(progress*bufSize/1024);
				// jpProgress.paintImmediately(rect);
			}
			double filelengthMB = 0;
			filelengthMB = fileLength / (1024.0);
			speedSeg = (filelengthMB / timeUsed1) * 1000.0;
			speedPos = (filelengthMB / timeUsed2) * 1000.0;

			DecimalFormat df = new DecimalFormat("0.00 ");

			showResult();// ��Ҫ����ʱ�䣬��Ҫ��һ���Ż�
			JOptionPane.showMessageDialog(frame, "���ݴ�С��" + fileLength / 1024
					+ " KB" + "\n������" + word + " ��" + "\n�ִʸ�����"
					+ seger.getWords() + " ��" + "\n�ִ�ʱ�䣺" + timeUsed1 / 1000.0
					+ " ��" + "\n�ִ��ٶ�: " + df.format(speedSeg) + "KB/S"
					+ "\n���Ա�עʱ�䣺" + timeUsed2 / 1000.0 + " ��" + "\n���Ա�ע�ٶ�: "
					+ df.format(speedPos) + "KB/S", "completely��",
					JOptionPane.INFORMATION_MESSAGE);
			channel.close();
		} else {
			StringBuilder s[];
			sentence = taOutputData.getText();
			// System.out.println(sentence);
			strr = seger.dataProc(sentence);// ���зִ�
			posTag = new PosTag(strr);
			s = posTag.Viterbi();// ���д��Ա�ע
			for (int i = 0; i < s.length; i++)
				taOutputResult.append(s[i].toString());
		}

		btSeg.setEnabled(false);
	}

	private void outputResult(StringBuilder result) throws IOException {
		// �ѷִʵĽ��д���ⲿ�ļ�������ר��
		File fileResult = new File("pos//result.txt");
		fileResult.delete();
		fileResult.createNewFile();
		int bufSize = 2 * 1024 * 1024;// �����С
		ByteBuffer byteBuf = ByteBuffer.allocate(bufSize);
		FileChannel channel1 = new RandomAccessFile(fileResult, "rw")
				.getChannel();
		channel1.position(channel1.size());
		channel1.write(ByteBuffer.wrap(result.toString().getBytes()));
		channel1.close();
	}

	private void showResult() throws IOException {
		// �Ѵ��Ա�ע������ļ��еĽ��ȡ����ʾ����
		long timeBegin1, timeEnd1, timeBegin2, timeEnd2, timeBegin3, timeEnd3;
		double timeUsed1, timeUsed2, timeUsed3;

		int bufSize = 3 * 1024 * 1024;// �����С����Ӱ�쵽�ִ��ٶ�
		File fileResult = new File("pos//result.txt");
		byte[] bs1 = new byte[bufSize];
		ByteBuffer byteBuf = ByteBuffer.allocate(bufSize);
		FileChannel channel = new RandomAccessFile(fileResult, "r")
				.getChannel();
		int size1;
		size1 = channel.read(byteBuf);
		byteBuf.rewind();
		byteBuf.get(bs1);
		String str = new String(bs1, 0, size1);

		timeBegin1 = System.currentTimeMillis();
		taOutputResult.append(str);
		channel.close();
		timeEnd1 = System.currentTimeMillis();
		System.out.println("��ȡ��� " + (timeEnd1 - timeBegin1));
	}

	private void loadDic(File dicFile) {
		seger.SetDic(dicFile);
		infoDic.setText("�ʵ� " + dicFile.getName() + "������");
	}

	private void trainDic(File f) {

	}

	private void setAlgo(int type)// ѡ���㷨
	{
		String algo = null;
		switch (type) {
		case ALGO_FMM:
			seger.setAlgorithm(ALGO_FMM);
			algo = "�������ƥ��";
			break;
		case ALGO_BMM:
			seger.setAlgorithm(ALGO_BMM);
			algo = "�������ƥ��";
			break;
		}
		infoAlgo.setText("�ִ��㷨��" + algo);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == openDicItem) {
			File dicFile = openFile();
			if (dicFile == null)
				return;
			loadDic(dicFile);
			return;
		}
		if (e.getSource() == closeItem) {
			frame.dispose();
			System.exit(0);
			return;
		}
		if (e.getSource() == openTrainFileItem) {
			File trainFile = openFile();
			if (trainFile == null)
				return;
			else
				trainDic(trainFile);
			return;
		}
		if (e.getSource() == openDataItem) {
			File dataFile = openFile();
			if (dataFile == null)
				return;
			else {
				tfInput.setText(dataFile.getAbsolutePath());
				this.dataFile = dataFile;
				taOutputData.setEditable(false);
				taOutputData.setText("");
				taOutputResult.setText("");
				flag = 1;
			}
			btSeg.setEnabled(true);
			return;
		}
		if (e.getSource() == aboutItem) {
			JOptionPane.showMessageDialog(frame, "���ߣ�MVRPС��",
					"����CSAPT�����Զ��ִ�����Ա�ע", JOptionPane.INFORMATION_MESSAGE);

			return;
		}
		if (e.getSource() == fmmItem) {
			setAlgo(ALGO_FMM);
			return;
		}
		if (e.getSource() == bmmItem) {
			setAlgo(ALGO_BMM);
			return;
		}
		if (e.getSource() == btSeg) {
			if ((taOutputData.getText().length() > 2) || (flag == 1)) {
				try {
					taOutputResult.setText("");
					procData();
				} catch (Exception ee) {
					ee.printStackTrace();
				}
				return;
			} else {
				JOptionPane.showMessageDialog(null,
						"���ݲ���Ϊ�գ�����ļ��˵�ѡ�����ݻ�������ര���������ݣ�", "���棡",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	public static void main(String[] args) {
		final MainFrame window = new MainFrame();
		window.start();
	}

	public void focusGained(FocusEvent e) {
		// TODO Auto-generated method stub
		btSeg.setEnabled(true);
		taOutputData.setEnabled(true);
		taOutputData.setEditable(true);
	}

	public void focusLost(FocusEvent e) {
		// TODO Auto-generated method stub

	}
}
