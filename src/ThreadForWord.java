import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ThreadForWord extends Thread {
	private boolean suspendFlag = false;
	private JPanel centerPanel;
	private String str;
	private JTextField answer;
	private Font font;
	private JButton nextWord;

	public ThreadForWord(JPanel centerPanel, String str, JTextField answer, JButton nextWord) {
		super();
		font = new Font("Serif", Font.BOLD, 12);
//		Font font = null;
		try {
			font = Font.createFont(Font.TRUETYPE_FONT,
					new File(System.getProperty("user.dir") + "/Fonts/RobotoMedium/Roboto-Medium.ttf"));
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}

		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment(); // объект для регистрации шрифта
		genv.registerFont(font); // регистрируем шрифт
		font = font.deriveFont(82f); // задаем ему размер

		this.centerPanel = centerPanel;
		this.str = str;
		this.answer = answer;
		this.nextWord = nextWord;
	}

	@Override
	public void run() {
//		int doneRemoving = 0;
		nextWord.setEnabled(false);
		Component[] panelElems = centerPanel.getComponents();
//		System.out.println("centerPanelComponents = ");

		for (int i = 0; i < panelElems.length; i++) {
			Component component = panelElems[i];
//			System.out.println("\t" + component);
			try {
				sleep(40);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
//			doneRemoving++;
//			System.out.println("doneRemoving = "+doneRemoving);
			centerPanel.remove(component);
			centerPanel.repaint();
		}
		System.out.println("Done removing");
		answer.setText(str);
		System.out.println(str);
		str = messStr(str);
		Random rand = new Random();
		ArrayList<JLabel> arrayOfCharLabels = new ArrayList<JLabel>();
		Color myColor = new Color(32, 77, 128);
		for (int i = 0; i < str.length(); i++) {
			JLabel tempLabel = new JLabel(String.valueOf(str.charAt(i)));
			arrayOfCharLabels.add(tempLabel);
			arrayOfCharLabels.get(i).setFont(font);

//			arrayOfCharLabels.get(i).setBorder(BorderFactory.createLineBorder(myColor, 5));
			arrayOfCharLabels.get(i).setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 5));
			centerPanel.add(arrayOfCharLabels.get(i));
			for (int k = 1; k < 10; k++) {
//				tempLabel.setText(String.valueOf((char) rand.nextInt(20)+47));
				tempLabel.setText(Character.toString((char) (rand.nextInt(20) + 47)));
				try {
					sleep(40);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				synchronized (this) {
					// если suspendFlag установлен - прекращаем действие потока
					while (suspendFlag) {
						try {
							wait(); // приостанавливает поток
						} catch (InterruptedException ex) {
							System.out.println("Произошла ошибка в потоке\n");
						}
					}
				}
			}
			tempLabel.setText(String.valueOf(str.charAt(i)));
			try {
				sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
		nextWord.setEnabled(true);
		super.run();
	}

	private static String messStr(String str2) {
		char[] strMessed = str2.toCharArray();
		for (int i = 0; i < strMessed.length / 2; i++) {
			char c = strMessed[i];
			strMessed[i] = strMessed[strMessed.length - i - 1];
			strMessed[strMessed.length - i - 1] = c;
		}
		for (int i = 0; i < strMessed.length - 2; i += 2) {
			char c = strMessed[i];
			strMessed[i] = strMessed[i + 2];
			strMessed[i + 2] = c;
		}
		Random rand = new Random();
		for (int i = 0; i < strMessed.length - 1; i++) {
			char c = strMessed[i];
			int r = rand.nextInt(strMessed.length);
			strMessed[i] = strMessed[r];
			strMessed[r] = c;
		}
		str2 = String.valueOf(strMessed);

		return str2;
	}

	synchronized void suspendThread() { // синхронизированный метод для приостановки потока
		// для остановки потока достаточно взвести флаг - в коде потока он проверится и
		// вызовется метод wait()
		// данный метод будем вызывать из главного класса
		suspendFlag = true;
		System.out.println("Остановка потока \n");
	}

	synchronized void resumeThread() {// синхронизированный метод для возобновления потока
		// данный метод будем вызывать из главного класса
		suspendFlag = false; // сбрасываем флаг
		System.out.println("Запуск потока \n");
		notify(); // запускаем поток, который был остановлен (то есть сам себя)
	}

}
