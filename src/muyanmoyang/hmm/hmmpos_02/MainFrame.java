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
	private JRadioButtonMenuItem fmmItem, bmmItem;// 正向最大匹配菜单和逆向最大匹配菜单
	private JMenuItem openTrainFileItem, aboutItem;
	private JButton btSeg;
	private JTextField tfInput;
	private JTextArea taOutputData, taOutputResult;
	private static int progress = 0;
	Thread thread;
	JLabel infoDic, infoAlgo, infoTrainFile;
	JProgressBar jpProgress = null;
	WordSegment seger;// 分词对象
	PosTag posTag;// 词性标注对象
	File dataFile;
	int flag = 0;

	public MainFrame() {
		frame = new JFrame();
		dataFile = null;
		seger = new WordSegment();
		frame.setTitle("CSAPT中文自动分词与词性标注");
		frame.setDefaultCloseOperation(0);
		frame.setJMenuBar(menuBar);

		fileMenu = new JMenu("文件");
		algorithmMenu = new JMenu("分词算法");
		trainMenu = new JMenu("语料");
		helpMenu = new JMenu("帮助");
		openDicItem = fileMenu.add("载入词典");
		openDataItem = fileMenu.add("载入数据");
		fileMenu.addSeparator();
		closeItem = fileMenu.add("退出");

		algorithmMenu.add(fmmItem = new JRadioButtonMenuItem("正向最大匹配", true));
		algorithmMenu.add(bmmItem = new JRadioButtonMenuItem("逆向最大匹配", false));
		ButtonGroup algorithms = new ButtonGroup();
		algorithms.add(fmmItem);
		algorithms.add(bmmItem);

		openTrainFileItem = trainMenu.add("载入语料");
		aboutItem = helpMenu.add("关于CSAPT");

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
		infoAlgo = new JLabel("分词算法：正向最大匹配");

		bottomPanel.add(infoDic);
		bottomPanel.add(infoAlgo);
		jpProgress = new JProgressBar();
		jpProgress.setStringPainted(true);// 设置进度条上是否显示进度具体进度如50%
		jpProgress.setMaximum(100);// 设置最大值
		jpProgress.setMinimum(0);// 设置最小值
		jpProgress.setValue(0);// 设置初始值
		jpProgress.setPreferredSize(new Dimension(400, 22));
		// bottomPanel.add(jpProgress);
		btSeg.addActionListener(this);
		frame.setBounds(300, 100, 770, 500);
		frame.setVisible(true);

		// seger.SetDic(new File("pos//dic.txt"));//默认的分词词典文件
	}

	private File openFile()// 打开文件（数据、词典或者语料库）
	{
		JFileChooser chooser = new JFileChooser();// 文件选择对话框
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

	private void procData() throws Exception {// 将数据文件读入内存
		long fileLength = 0;// 文件大小
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
			int bufSize = 3 * 1024 * 1024;// 缓存大小，会影响到分词速度
			byte[] bs = new byte[bufSize];
			timeBegin3 = System.currentTimeMillis();
			double speedSeg = 0.0, speedPos = 0.0;
			// 这里是分配缓存大小。也就是用来存放从硬盘中读出来的文件
			// 什么叫一次把文件读出来？其实就是当缓存大小和在硬盘中文件大小一样，
			// 只通过一个read指令把整个文件都扔到缓存里面。例如要一次读一个2G的文件，把缓存设为2G就能一次读出来。
			// 不过当分配空间的时候，这个缓存根本是分配不出来的，因为内存不足。
			ByteBuffer byteBuf = ByteBuffer.allocate(bufSize);
			FileChannel channel = new RandomAccessFile(dataFile, "r")
					.getChannel();
			int size;
			// 因为这里缓存大小是1K，所以每个channel.read()指令最多只会读到文件的1K的内容。
			// 如果文件有1M大小，这里for会循环1024次，把文件分开1024次读取出来
			while ((size = channel.read(byteBuf)) != -1) {

				strr = new StringBuilder("");
				byteBuf.rewind();
				byteBuf.get(bs);
				str = new String(bs, 0, size);
				byteBuf.clear();

				taOutputData.append(str);
				timeEnd3 = System.currentTimeMillis();
				timeUsed3 = timeEnd3 - timeBegin3;
				System.out.println("原文显示时间：" + timeUsed3);

				word = word + str.length();
				timeBegin1 = System.currentTimeMillis();

				strr = seger.dataProc(str);// 进行分词

				// System.out.println(str);
				timeEnd1 = System.currentTimeMillis();
				timeUsed1 = timeUsed1 + (timeEnd1 - timeBegin1);// 分词结束，计算分词所用时间

				timeBegin2 = System.currentTimeMillis();

				posTag = new PosTag(strr);
				posTag.Viterbi();// 进行词性标注
				// outputResult(strr);

				timeEnd2 = System.currentTimeMillis();
				timeUsed2 = timeUsed2 + (timeEnd2 - timeBegin2);// 词性标注结束，计算所用时间
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

			showResult();// 需要大量时间，需要进一步优化
			JOptionPane.showMessageDialog(frame, "数据大小：" + fileLength / 1024
					+ " KB" + "\n字数：" + word + " 个" + "\n分词个数："
					+ seger.getWords() + " 个" + "\n分词时间：" + timeUsed1 / 1000.0
					+ " 秒" + "\n分词速度: " + df.format(speedSeg) + "KB/S"
					+ "\n词性标注时间：" + timeUsed2 / 1000.0 + " 秒" + "\n词性标注速度: "
					+ df.format(speedPos) + "KB/S", "completely！",
					JOptionPane.INFORMATION_MESSAGE);
			channel.close();
		} else {
			StringBuilder s[];
			sentence = taOutputData.getText();
			// System.out.println(sentence);
			strr = seger.dataProc(sentence);// 进行分词
			posTag = new PosTag(strr);
			s = posTag.Viterbi();// 进行词性标注
			for (int i = 0; i < s.length; i++)
				taOutputResult.append(s[i].toString());
		}

		btSeg.setEnabled(false);
	}

	private void outputResult(StringBuilder result) throws IOException {
		// 把分词的结果写入外部文件，测试专用
		File fileResult = new File("pos//result.txt");
		fileResult.delete();
		fileResult.createNewFile();
		int bufSize = 2 * 1024 * 1024;// 缓存大小
		ByteBuffer byteBuf = ByteBuffer.allocate(bufSize);
		FileChannel channel1 = new RandomAccessFile(fileResult, "rw")
				.getChannel();
		channel1.position(channel1.size());
		channel1.write(ByteBuffer.wrap(result.toString().getBytes()));
		channel1.close();
	}

	private void showResult() throws IOException {
		// 把词性标注存放在文件中的结果取出显示出来
		long timeBegin1, timeEnd1, timeBegin2, timeEnd2, timeBegin3, timeEnd3;
		double timeUsed1, timeUsed2, timeUsed3;

		int bufSize = 3 * 1024 * 1024;// 缓存大小，会影响到分词速度
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
		System.out.println("读取结果 " + (timeEnd1 - timeBegin1));
	}

	private void loadDic(File dicFile) {
		seger.SetDic(dicFile);
		infoDic.setText("词典 " + dicFile.getName() + "已载入");
	}

	private void trainDic(File f) {

	}

	private void setAlgo(int type)// 选择算法
	{
		String algo = null;
		switch (type) {
		case ALGO_FMM:
			seger.setAlgorithm(ALGO_FMM);
			algo = "正向最大匹配";
			break;
		case ALGO_BMM:
			seger.setAlgorithm(ALGO_BMM);
			algo = "逆向最大匹配";
			break;
		}
		infoAlgo.setText("分词算法：" + algo);
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
			JOptionPane.showMessageDialog(frame, "作者：MVRP小组",
					"关于CSAPT中文自动分词与词性标注", JOptionPane.INFORMATION_MESSAGE);

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
						"数据不能为空，请从文件菜单选择数据或者在左侧窗口输入数据！", "警告！",
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
