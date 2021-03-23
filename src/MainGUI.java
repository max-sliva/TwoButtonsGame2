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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class MainGUI {
	static Font font;
	static String str;
	static JTextField answer;  //ответ внизу окна
	static File file = new File("а4.txt"); //файл со словами по умолчанию
	static FileReader myReader;
	static BufferedReader bufReader;
//	static int doneRemoving = 0;
	static ThreadForWord myThread; //поток для анимации букв
	static Color myColor = new Color(32,77,128); //цвет НВГУ 
	static SerialPort serialPort = null;
	
	public static void main(String[] args) throws IOException, FontFormatException {
		JFrame mainFrame = new JFrame("!!!!!Игра!!");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		font = Font.createFont(Font.TRUETYPE_FONT, new File(System.getProperty("user.dir") + "/ds_digital/DS-DIGIB.TTF")); //шрифт
		font = new Font("Serif", Font.BOLD, 12);
		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment(); // объект для регистрации шрифта
		genv.registerFont(font); // регистрируем шрифт
		font = font.deriveFont(82f); // задаем ему размер
		answer = new JTextField();
		answer.setForeground(Color.white); //цвет шрифта белый
		JPanel leftPlayer = createSideBox("1st player"); //панели со счетом игроков
		JPanel rightPlayer = createSideBox("2nd player"); 

		String[] portNames = SerialPortList.getPortNames(); // получаем список портов
		JComboBox<String> comPorts = new JComboBox<>(portNames); // создаем комбобокс с этим списком
		comPorts.setSelectedIndex(-1); // чтоб не было выбрано ничего в комбобоксе
		comPorts.addActionListener(arg -> { // слушатель выбора порта в комбобоксе
			String choosenPort = comPorts.getItemAt(comPorts.getSelectedIndex()); // получаем название выбранного порта
			//если serialPort еще не связана с портом или текущий порт не равен выбранному в комбо-боксе 
			if (serialPort == null || !serialPort.getPortName().contains(choosenPort)) {
				serialPort = new SerialPort(choosenPort); //задаем выбранный порт
				try { //тут секция с try...catch для работы с портом 
					serialPort.openPort(); //открываем порт
					serialPort.setParams(9600, 8, 1, 0); //задаем параметры порта, 9600 - скорость, такую же нужно задать для Serial.begin в Arduino
					//остальные параметры стандартные для работы с портом
					serialPort.addEventListener(event -> {  //слушатель порта для приема сообщений от ардуино
						if (event.isRXCHAR()) {// если есть данные для приема
							try {  //тут секция с try...catch для работы с портом
								String str = serialPort.readString(); //считываем данные из порта в строку
								str = str.trim(); //убираем лишние символы (типа пробелов, которые могут быть в принятой строке) 
								System.out.println(str); //выводим принятую строку
								if (str.contains("button=1")) { 
									leftPlayer.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 5)); //желтая рамка
									if (myThread.isAlive()) myThread.suspendThread();  //ставим поток на паузу
								}
								if (str.contains("button=2")) {
									rightPlayer.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 5));
									if (myThread.isAlive()) myThread.suspendThread();
								}
							} catch (SerialPortException ex) { //для обработки возможных ошибок
								System.out.println(ex);
							}
						}
					});
					
				} catch (SerialPortException e) {//для обработки возможных ошибок
					e.printStackTrace();
				}
			} else
				System.out.println("Same port!!"); //это если выбрали в списке тот же порт, что и до этого
		});
		
//		file = new File("а4.txt");
		System.out.println("file = " + file.getAbsolutePath());
		myReader = new FileReader(file);
		bufReader = new BufferedReader(myReader);
		JButton nextWord = new JButton("Next");
		JButton startBtn = new JButton("Старт!");
		JPanel centerPanel = new JPanel();  //центральная панель с буквами
		centerPanel.addMouseListener(new MouseAdapter() { //для обработки нажатия на панель
			public void mouseClicked(MouseEvent e) {
				if (e.getButton()==MouseEvent.BUTTON1) { //если ЛКМ 
					if (myThread.isAlive()) myThread.resumeThread(); //то возбновляем поток 
				} else {
					if (myThread.isAlive()) myThread.suspendThread();  //иначе (ПКМ) ставим поток на паузу
				}
			}
		});
		startBtn.addActionListener(e->{  //нажатие на кнопку Старт
			try {
				str = bufReader.readLine();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			leftPlayer.setBorder(BorderFactory.createLineBorder(myColor, 5)); //возвращаем нормальный цвет рамок счета игроков
			rightPlayer.setBorder(BorderFactory.createLineBorder(myColor, 5));
//			System.out.println("string = "+str);
			myThread = new ThreadForWord(centerPanel, str, answer, nextWord); //новый поток с новым словом
			myThread.start(); //стартуем поток
			startBtn.setEnabled(false); //делаем кнопку неактивной
		});
		
		JButton loadBtn = new JButton("Load file");
		loadBtn.addActionListener(action -> {  //нажатие на кнопку загрузки файла со словами
			leftPlayer.setBorder(BorderFactory.createLineBorder(myColor, 5)); //возвращаем нормальный цвет рамок счета игроков
			rightPlayer.setBorder(BorderFactory.createLineBorder(myColor, 5));

			FileDialog fdlg = new FileDialog(mainFrame, "LoadFile");  //диалог открытия файла
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

		nextWord.addActionListener(action -> {  //нажатие на кнопку следующего слова
			try {
				String oldStr = str;
				str = bufReader.readLine();
				leftPlayer.setBorder(BorderFactory.createLineBorder(myColor, 5));
				rightPlayer.setBorder(BorderFactory.createLineBorder(myColor, 5));

				if (str != null) { //если слово есть
					System.out.println("Panel comps ="+centerPanel.getComponents().length);
					myThread = new ThreadForWord(centerPanel, str, answer, nextWord);  //новый поток с новым словом
					myThread.start();
					if (oldStr!=null) {
						System.out.println("oldStr.length = " + oldStr.length());
//						System.out.println("doneRemoving = "+doneRemoving);
					}
//					mainFrame.repaint();
				} else
					nextWord.setEnabled(false);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		Box northBox = new Box(BoxLayout.X_AXIS);  //верхняя панель с кнопками загрузки файла, выбора порта, след. слово 

		ImageIcon nvsuLogo = new ImageIcon("nvsu_logo_small.png"); //лого универа
		Image image = nvsuLogo.getImage(); // объект для преобразования
		Image newimg = image.getScaledInstance(100, 100,  java.awt.Image.SCALE_SMOOTH); // задаем размер
		nvsuLogo = new ImageIcon(newimg); //применяем новые параметры
	
		JLabel nvsuLabel = new JLabel(nvsuLogo); //лейбл с лого универа
//		nvsuLabel.setBorder(BorderFactory.createLineBorder(myColor, 5));

		ImageIcon fitimLogo= new ImageIcon("fitim_logo_small.png"); //лого факультета
		image = fitimLogo.getImage(); //объект для преобразования
		newimg = image.getScaledInstance(100, 100,  java.awt.Image.SCALE_SMOOTH); // задаем размер  
		fitimLogo = new ImageIcon(newimg);  //применяем новые параметры
		JLabel fitimLabel = new JLabel(fitimLogo);   //лейбл с лого факультета
		
		Box countPanel = new Box(BoxLayout.X_AXIS);  //панель со счетом
		countPanel.addMouseListener(new MouseAdapter() {  //слушатель нажатия на панель для сброса цвета рамок
			public void mouseClicked(MouseEvent e) {
				if (e.getButton()==MouseEvent.BUTTON1) {
					leftPlayer.setBorder(BorderFactory.createLineBorder(myColor, 5));
					rightPlayer.setBorder(BorderFactory.createLineBorder(myColor, 5));
				}
			}
		});
		Box leftBox = new Box(BoxLayout.Y_AXIS);  //левая панель со счетом
//		leftBox.setAlignmentX(Box.CENTER_ALIGNMENT);
		leftBox.add(leftPlayer);
//		leftBox.add(nvsuLabel);
		
		Box rightBox = new Box(BoxLayout.Y_AXIS); //правая панель со счетом
		rightBox.add(rightPlayer);
//		rightBox.add(fitimLabel);
		
		countPanel.add(leftBox); //добавляем левый, правый счет и кнопку старта
		countPanel.add(Box.createHorizontalGlue());
		countPanel.add(startBtn);
		countPanel.add(Box.createHorizontalGlue());
		countPanel.add(rightBox);
		
//		Box centerBox = new Box(BoxLayout.Y_AXIS);
		JPanel centerBox = new JPanel(); //основная панель окна 
//		centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
		centerBox.setLayout(new GridLayout(2, 1));  //делаем на ней грид из 2-х строк и 1 столбца
		
//		centerBox.setBorder(BorderFactory.createLineBorder(myColor, 7));
		JPanel labelPanel = new JPanel(); //панель с лого
		labelPanel.add(nvsuLabel); //добавляем в нее лого универа
		labelPanel.add(fitimLabel); //и лого факультета
//		centerBox.add(labelPanel);
		JPanel upperPanel = new JPanel(new BorderLayout()); //панель с BorderLayout 
		upperPanel.add(labelPanel, BorderLayout.NORTH); //на север вставляем панель с лого
		upperPanel.add(centerPanel, BorderLayout.CENTER); //в центр - панель с буквами
		centerPanel.setBorder(BorderFactory.createLineBorder(myColor, 5));
		centerBox.add(upperPanel); //в основную панель вставляем все остальные 
		centerBox.add(countPanel);
		
		northBox.add(loadBtn); //в самую верхнюю панель вставляем кнопки и выбор порта
		northBox.add(Box.createHorizontalGlue());
		northBox.add(comPorts);
		northBox.add(Box.createHorizontalGlue());
		northBox.add(nextWord);
		
		//вставляем все в окно
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
	private static JPanel createSideBox(String string) { //функция для создания боковых панелей со счетом 
//		Font font = new Font("Serif", Font.BOLD, 12);
		Font font = null;
		try { //загружаем универский шрифт
			font = Font.createFont(Font.TRUETYPE_FONT, new File(System.getProperty("user.dir") + "/Fonts/RobotoMedium/Roboto-Medium.ttf"));
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment(); // объект для регистрации шрифта
		genv.registerFont(font); // регистрируем шрифт
		font = font.deriveFont(41f); // задаем ему размер

//		Box sideBox = new Box(BoxLayout.Y_AXIS);
		JPanel sideBox = new JPanel(new GridLayout(2,1)); //панель с гридом 2 строки и 1 столбец
//		sideBox.setLayout(new BoxLayout(sideBox, BoxLayout.Y_AXIS));
		JLabel player = new JLabel(string);  //лейбл с названием игрока
		player.setHorizontalAlignment(JLabel.CENTER); //выравнивание по центру
		player.setFont(font);  //задаем шрифт
		sideBox.add(player);  //лейбл на панель 
		JLabel count = new JLabel("0");  //лейб со счетом
		Font countFont = player.getFont();  //новый шрифт, берем из предыдущего лейбла
		countFont = countFont.deriveFont(100f); //увеличиваем шрифт
		count.setFont(countFont); //устанавливаем шрифт для счета
		count.addMouseListener(new MouseAdapter() { //лиснер для обработки нажатия на счет
			public void mouseClicked(MouseEvent e) {
				if (e.getButton()==MouseEvent.BUTTON1) countUp((JLabel)e.getSource()); //если нажали ЛКМ, то вызываем метод для увеличения счета
				else countDown((JLabel)e.getSource());  //иначе вызываем метод для уменьшения счета
			}
		});
		
		count.setHorizontalAlignment(JLabel.CENTER); //выравнивание по центру
		sideBox.add(count);
//		Color myColor = new Color(32,77,128);
		sideBox.setBorder(BorderFactory.createLineBorder(myColor, 5));  //граница
		return sideBox;
	}
	
	protected static void countDown(JLabel source) { //метод для уменьшения счета
		System.out.println("count down");
		int count = Integer.parseInt(source.getText());  //берем счет с лейбла
		count--; //уменьшаем его 
		source.setText(""+count); //устанавливаем новый счет
	}
	protected static void countUp(JLabel source) { //метод для увеличения счета
		System.out.println("count up");
		int count = Integer.parseInt(source.getText());  //берем счет с лейбла
		count++;  //увеличиваем его 
		source.setText(""+count);  //устанавливаем новый счет
	}
}
