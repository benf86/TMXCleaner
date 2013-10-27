package si.ferreira.tmxcleaner;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

@SuppressWarnings("serial")
public class TmxCleaner extends JPanel implements ActionListener {
	static String sysLog = "";
	static String[] tmFile = null;
	static private final String newline = "\n";
	static private final String welcomeMessage = "This application should work with TMX files exported by MemoQ, SDL Studio, SDL TRADOS TagEditor, Across, Transit NXT, and with any other standard TMX file.\n\nIf you run across a TMX that the app can't process, please send its header and a few sample translation units to benjamin@ferreira.si, and I'll look into it.\n\nI don't take any responsibility for what you do with this app or the consequences of its use.\n\n";
	JButton openButton, saveButton;
	static JTextArea log;
	JFileChooser fc;
	
	public TmxCleaner() {
		super(new BorderLayout());
		
		log = new JTextArea(5,20);
		log.setWrapStyleWord(true);
		log.setLineWrap(true);
		log.setMargin(new Insets(5,5,5,5));
		log.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(log, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				
		log.append(welcomeMessage);
		fc = new JFileChooser();
		
		openButton = new JButton("Open the source TM XML");
		openButton.addActionListener(this);
		
		saveButton = new JButton("Save source as TXT");
		saveButton.addActionListener(this);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(openButton);
		buttonPanel.add(saveButton);
		
		add(buttonPanel, BorderLayout.PAGE_START);
		add(logScrollPane, BorderLayout.CENTER);
	}
	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
	
	private static void createAndShowGUI() {
		JFrame frame = new JFrame("TMX Cleaner by Rolando Benjamin Vaz Ferreira");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Dimension d = new Dimension(800,600);
		frame.setPreferredSize(d);
		
		
		
		frame.add(new TmxCleaner());
		frame.pack();
		frame.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		String sourcePath;
		
		//Handle open button action.
        if (e.getSource() == openButton) {
        	log.append("____________________________________________________" + newline);
            log.append("Opening a large file can take a while. Please be patient..." + newline + newline);
            int returnVal = fc.showOpenDialog(TmxCleaner.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                sourcePath = file.getPath();
                System.out.println("Invoking cleanTMX()");
                tmFile = cleanTMX(sourcePath);
                log.append("TM XML file opened:\t" + file.getPath() + newline);
				log.append("Detected file encoding:\t" + tmFile[3] + newline);
				log.append("Detected creation CAT:\t" + tmFile[4] + newline);
				log.append("Detected source language:\t" + tmFile[5] + newline);
				log.append("Recognized segments:\t" + tmFile[2] + newline);
            } else {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
 
        //Handle save button action.
        } else if (e.getSource() == saveButton) {
        	String outputPath;
            int returnVal = fc.showSaveDialog(TmxCleaner.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                outputPath = file.getPath() + ".txt";
                saveFile(outputPath, tmFile[0]);
                log.append("\n\nSource segments exported to:\t" + file.getPath() + ".txt\n");
                try {
                	File openFile = new File(file.getPath() + ".txt");
					Desktop.getDesktop().open((openFile));
				} catch (IOException e1) {
					sysLog = e1.getMessage();
				}
            } else {
                log.append("Save command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
        }
    }
	
	private static String[] getFile(String sourcePath) {
		String feRe, ctRe, slRe = "";
		String[] fileInfo = new String[4];
			//0 = file contents
			//1 = encoding
			//2 = creation tool
			//3 = source language
		
		Pattern fileInfoPattern;
		Matcher fileInfoMatcher;
		
		//Regex filters for file info		
		feRe = "[Ee]ncoding=\"(.*?)\"";
		ctRe = "[Cc]reation[Tt]ool=\"(.*?)\"";
		slRe = "[Ss][ou]*rc[e]*[Ll]ang[uage]*=\"(.*?)\"";
		
		

		//Get file contents
		System.out.println("Invoking openFile()");
		fileInfo[0] = openFile(sourcePath);

		//Get file encoding
		fileInfoPattern = Pattern.compile(feRe, Pattern.CASE_INSENSITIVE);
		fileInfoMatcher = fileInfoPattern.matcher(fileInfo[0]);
		while (fileInfoMatcher.find())
				fileInfo[1] = fileInfoMatcher.group(1).toUpperCase();

		//Get file creation tool
		fileInfoPattern = Pattern.compile(ctRe);
		fileInfoMatcher = fileInfoPattern.matcher(fileInfo[0]);
		while (fileInfoMatcher.find())
				fileInfo[2] = fileInfoMatcher.group(1);
		
		//Get TU source language
		fileInfoPattern = Pattern.compile(slRe);
		fileInfoMatcher = fileInfoPattern.matcher(fileInfo[0]);
		while (fileInfoMatcher.find())
				fileInfo[3] = fileInfoMatcher.group(1);
		
		System.out.println("Returning from getFile()");
		return fileInfo;
	}
	
	private static String littleFixes(String preOutputText) {
		preOutputText = preOutputText.replaceAll("<prop.*?</prop>", ""); //Across Standard-Adherence Fail Fix
		preOutputText = preOutputText.replaceAll("<[/]?seg>", "");
		
		//Fix HTML entities
		preOutputText = preOutputText.replace("&lt;", "<");
		preOutputText = preOutputText.replace("&gt;", ">");
		preOutputText = preOutputText.replace("&amp;", "&");
		preOutputText = preOutputText.replace("&cent;", "¢");
		preOutputText = preOutputText.replace("&pound;", "£");
		preOutputText = preOutputText.replace("&yen;", "¥");
		preOutputText = preOutputText.replace("&euro;", "€");
		preOutputText = preOutputText.replace("&sect;", "§");
		preOutputText = preOutputText.replace("&copy;", "©");
		preOutputText = preOutputText.replace("&reg;", "®");
		preOutputText = preOutputText.replace("&trade;", "™");

		return preOutputText;
	}

	private static void saveFile(String outputPath, String outputText) {
		try {
			BufferedWriter out;
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath),"UTF-16"));
			out.write(outputText);
			out.close();
		} catch (UnsupportedEncodingException e) {
			sysLog = e.getMessage();
		} catch (IOException e) {
			sysLog = e.getMessage();
		}
	}

	private static String openFile(String sourcePath) {
		String contentText = "", lineData = "", fileEncoding = "";
		String findFileEncodingFilter = "encoding=\"(.*?)\"";

		//Determine the encoding to open the file with
		RandomAccessFile inFile;
		try {
			inFile = new RandomAccessFile(sourcePath,"r");
			 lineData = inFile.readLine();
		     inFile.close();
		} catch (IOException e1) {
			sysLog = e1.getMessage();
		}
        
		Pattern findFileEncodingPattern = Pattern.compile(findFileEncodingFilter, Pattern.CASE_INSENSITIVE);
		Matcher findFileEncodingMatcher = findFileEncodingPattern.matcher(lineData);
		while (findFileEncodingMatcher.find()) {
			fileEncoding = findFileEncodingMatcher.group(1).toUpperCase();
		}
		if (fileEncoding == "")
			fileEncoding = "UTF-16";
		
		System.out.println("Preliminary open for charset complete. Opening for real...");
		
		//Open the file
		try {
			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sourcePath), fileEncoding));
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				sb.append(newline);
				line = br.readLine();
			}
			br.close();
			contentText = sb.toString();
			sb = null;
			sb = new StringBuilder();
			System.out.println("File completely read");
		} catch (UnsupportedEncodingException e) {
			sysLog = e.getMessage();
		} catch (FileNotFoundException e) {
			sysLog = e.getMessage();
		} catch (IOException e) {
			sysLog = e.getMessage();
		}
		
		System.out.println("Returning from openFile()");
		return contentText;
	}

	private static String[] cleanTMX(String sourcePath) {
		System.out.println("Invoking getFile()");
		String[] fileInfo = getFile(sourcePath);
		System.out.println("Language: " + fileInfo[3]);
		String[] outputData = new String[6];
			//0 cleaned text
			//1 original text
			//2 number of segments found
			//3 encoding
			//4 creation tool
			//5 source language
		
		String outputText = "";
		String tuFilter = "<[Tt]uv (xml:)?[Ll]ang=\"" + fileInfo[3] + "\".*?>(<seg>)?(.*?)(</seg>)?</[Tt]uv>";
		Integer segmentCounter = 0;
		System.out.println("Starting cleaning");
		
		Pattern tuPattern = Pattern.compile(tuFilter, Pattern.DOTALL);
		Matcher tuMatcher = tuPattern.matcher(fileInfo[0]);
		while (tuMatcher.find()) {
			outputText += tuMatcher.group(3) + newline;
			segmentCounter++;
		}
		
		outputData[0] = littleFixes(outputText);
		outputData[1] = fileInfo[0];
		outputData[2] = segmentCounter.toString();
		outputData[3] = fileInfo[1];
		outputData[4] = fileInfo[2];
		outputData[5] = fileInfo[3];
		return outputData;
	}
}
