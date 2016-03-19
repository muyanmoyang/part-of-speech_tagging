package txws;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class DialogTextThread extends Thread implements ActionListener {
	private JLabel promptLabel;
	private JLabel progressLabel;
	private JButton cancelBtn;
	private JProgressBar jpb;
	private JFrame frame;
	private String prompt;
	private String title;
	private static int n = 1000;

	public DialogTextThread(JFrame parentFrame, String prompt, String title) {
		this.prompt = prompt;
		this.title = title;

		jpb = new JProgressBar();
		jpb.setMaximum(9999);
		jpb.setMinimum(0);
		jpb.setBackground(Color.red);
		n++;
		initDialog();
	}

	public void initDialog() {
		promptLabel = new JLabel("       进程   :   " + prompt);
		promptLabel.setPreferredSize(new Dimension(255, 22));
		progressLabel = new JLabel("   ");
		progressLabel.setPreferredSize(new Dimension(255, 22));

		JPanel labelPanel = new JPanel();
		labelPanel.setPreferredSize(new Dimension(260, 67));
		labelPanel.setLayout(new BorderLayout());
		labelPanel.add(promptLabel, BorderLayout.CENTER);
		labelPanel.add(progressLabel, BorderLayout.SOUTH);

		JLabel tempLabel = new JLabel();
		tempLabel.setPreferredSize(new Dimension(90, 20));
		cancelBtn = new JButton("Cancel ");

		JPanel buttonPanel = new JPanel();
		buttonPanel.setPreferredSize(new Dimension(90, 67));
		buttonPanel.add(tempLabel);
		buttonPanel.add(cancelBtn);
		cancelBtn.addActionListener(this);

		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(400, 116));
		panel.add(labelPanel);
		panel.add(buttonPanel);

		jpb.setPreferredSize(new Dimension(400, 22));
		jpb.setStringPainted(true);
		panel.add(jpb);
		frame = new JFrame("JFrame");

		frame.setVisible(true);
		frame.setBounds(100, 100, 500, 300);
		frame.add(panel);
	}

	public void updateButtonStatus(boolean flag) {
		cancelBtn.setEnabled(flag);
	}

	public void updatePrompt(String prompt) {
		promptLabel.setText("       进程   :   " + prompt);
		n++;
	}

	public void updateProgress(String progress) {
		progressLabel.setText("       " + progress);
	}

	public static void main(String[] args) {
		final DialogTextThread dialogTextThread = new DialogTextThread(null,
				" ", "Create   Progress ");
		Thread thread = new Thread() {
			public void run() {
				for (int i = 0; i < 10000; i++) {
					System.out.println(i);
					try {
						Thread.sleep(1);
					} catch (InterruptedException ie) {
					}
					dialogTextThread.jpb.setValue(n);
					dialogTextThread.updatePrompt(String.valueOf(i));
					dialogTextThread.updateProgress(String.valueOf(i));
					String str = "我w";
					System.out.println(str.length());
				}
				// dialogTextThread.dispose();
			}
		};

		dialogTextThread.start();
		thread.start();
	}

	public void actionPerformed(ActionEvent e) {
		System.exit(1);
	}
}