import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class MainGUI {
	static Font font;
	static String str;
	static JTextField answer;
	static File file = new File("а4.txt");
	static FileReader myReader;
	static BufferedReader bufReader;
	static int doneRemoving = 0;
	static ThreadForWord myThread;
	static Color myColor = new Color(32,77,128);

	public static void main(String[] args) throws IOException, FontFormatException {
		JFrame mainFrame = new JFrame("!!Игра!!");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		font = Font.createFont(Font.TRUETYPE_FONT, new File(System.getProperty("user.dir") + "/ds_digital/DS-DIGIB.TTF")); //шрифт
		font = new Font("Serif", Font.BOLD, 12);
		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment(); // объект для регистрации шрифта
		genv.registerFont(font); // регистрируем шрифт
		font = font.deriveFont(82f); // задаем ему размер
		answer = new JTextField();
		answer.setForeground(Color.white);
		file = new File("а4.txt");
		System.out.println("file = " + file.getAbsolutePath());
		myReader = new FileReader(file);
		bufReader = new BufferedReader(myReader);
		JButton nextWord = new JButton("Next");
		JButton startBtn = new JButton("Старт!");
		JPanel centerPanel = new JPanel();
		startBtn.addActionListener(e->{
			try {
				str = bufReader.readLine();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
//			System.out.println("string = "+str);
			myThread = new ThreadForWord(centerPanel, str, answer, nextWord);
			myThread.start();
			startBtn.setEnabled(false);
		});
		
		JButton loadBtn = new JButton("Load file");
		loadBtn.addActionListener(action -> {
			FileDialog fdlg = new FileDialog(mainFrame, "LoadFile");
			fdlg.setDirectory(System.getProperty("user.dir"));
			fdlg.setMode(FileDialog.LOAD);
			fdlg.setVisible(true);
			file = new File(fdlg.getDirectory() + fdlg.getFile());
			System.out.println("file = " + file.getAbsolutePath());
			try {
				myReader = new FileReader(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			bufReader = new BufferedReader(myReader);
			nextWord.setEnabled(true);
			startBtn.setEnabled(true);
		});

		nextWord.addActionListener(action -> {
			try {
				String oldStr = str;
				str = bufReader.readLine();
				if (str != null) {
					System.out.println("Panel comps ="+centerPanel.getComponents().length);
					myThread = new ThreadForWord(centerPanel, str, answer, nextWord);
					myThread.start();
					if (oldStr!=null) {
						System.out.println("oldStr.length = " + oldStr.length());
						System.out.println("doneRemoving = "+doneRemoving);
					}
//					mainFrame.repaint();
				} else
					nextWord.setEnabled(false);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		Box northBox = new Box(BoxLayout.X_AXIS);

		ImageIcon nvsuLogo = new ImageIcon("nvsu_logo_small.png");
		Image image = nvsuLogo.getImage(); // transform it 
		Image newimg = image.getScaledInstance(100, 100,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
		nvsuLogo = new ImageIcon(newimg);
	
		JLabel nvsuLabel = new JLabel(nvsuLogo);
//		nvsuLabel.setBorder(BorderFactory.createLineBorder(myColor, 5));

		ImageIcon fitimLogo= new ImageIcon("fitim_logo_small.png");
		image = fitimLogo.getImage(); // transform it 
		newimg = image.getScaledInstance(100, 100,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
		fitimLogo = new ImageIcon(newimg);
		JLabel fitimLabel = new JLabel(fitimLogo);
		Box logoPanel = new Box(BoxLayout.X_AXIS);
		JPanel leftPlayer = createSideBox("1st player"); 
		JPanel rightPlayer = createSideBox("2nd player"); 
		Box leftBox = new Box(BoxLayout.Y_AXIS);
//		leftBox.setAlignmentX(Box.CENTER_ALIGNMENT);
		leftBox.add(leftPlayer);
//		leftBox.add(nvsuLabel);
		
		Box rightBox = new Box(BoxLayout.Y_AXIS); 
		rightBox.add(rightPlayer);
//		rightBox.add(fitimLabel);
		
		logoPanel.add(leftBox);
		logoPanel.add(Box.createHorizontalGlue());
		logoPanel.add(startBtn);
		logoPanel.add(Box.createHorizontalGlue());
		logoPanel.add(rightBox);
		
//		Box centerBox = new Box(BoxLayout.Y_AXIS);
		JPanel centerBox = new JPanel();
//		centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
		centerBox.setLayout(new GridLayout(2, 1));
		
//		centerBox.setBorder(BorderFactory.createLineBorder(myColor, 7));
		JPanel labelPanel = new JPanel();
		labelPanel.add(nvsuLabel);
//		centerBox.add(labelPanel);
		JPanel upperPanel = new JPanel(new BorderLayout());
		upperPanel.add(labelPanel, BorderLayout.NORTH);
		upperPanel.add(centerPanel, BorderLayout.CENTER);
		centerPanel.setBorder(BorderFactory.createLineBorder(myColor, 5));
		centerBox.add(upperPanel);
		centerBox.add(logoPanel);
		
		northBox.add(loadBtn);
		northBox.add(Box.createHorizontalGlue());
		northBox.add(nextWord);
		
		
		mainFrame.add(northBox, BorderLayout.NORTH);
		mainFrame.add(centerBox, BorderLayout.CENTER);
		mainFrame.add(answer, BorderLayout.SOUTH);
//		mainFrame.add(leftBox, BorderLayout.WEST);
//		mainFrame.add(rightBox, BorderLayout.EAST);

		mainFrame.setSize(800, 600);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setMinimumSize(mainFrame.getSize());
		mainFrame.pack();
		mainFrame.setVisible(true);
	}
	private static JPanel createSideBox(String string) {
//		Font font = new Font("Serif", Font.BOLD, 12);
		Font font = null;
		try {
			font = Font.createFont(Font.TRUETYPE_FONT, new File(System.getProperty("user.dir") + "/Fonts/RobotoMedium/Roboto-Medium.ttf"));
		} catch (FontFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment(); // объект для регистрации шрифта
		genv.registerFont(font); // регистрируем шрифт
		font = font.deriveFont(41f); // задаем ему размер

//		Box sideBox = new Box(BoxLayout.Y_AXIS);
		JPanel sideBox = new JPanel(new GridLayout(2,1));
//		sideBox.setLayout(new BoxLayout(sideBox, BoxLayout.Y_AXIS));
		JLabel player = new JLabel(string);
		player.setHorizontalAlignment(JLabel.CENTER);
		player.setFont(font);
//		player.setAlignmentX(Component.CENTER_ALIGNMENT);
		sideBox.add(player);
		JLabel count = new JLabel("0");
		Font countFont = player.getFont();
		countFont = countFont.deriveFont(100f);
		count.setFont(countFont);
		count.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton()==MouseEvent.BUTTON1) countUp((JLabel)e.getSource());
				else countDown((JLabel)e.getSource());
			}
		});
		
		count.setHorizontalAlignment(JLabel.CENTER);
		sideBox.add(count);
//		Color myColor = new Color(32,77,128);
		sideBox.setBorder(BorderFactory.createLineBorder(myColor, 5));
		return sideBox;
	}
	
	protected static void countDown(JLabel source) {
		System.out.println("count down");
		int count = Integer.parseInt(source.getText());
		count--;
		source.setText(""+count);
	}
	protected static void countUp(JLabel source) {
		System.out.println("count up");
		int count = Integer.parseInt(source.getText());
		count++;
		source.setText(""+count);
	}
}
