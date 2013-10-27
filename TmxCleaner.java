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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


@SuppressWarnings("serial")
public class TmxCleaner extends JPanel implements ActionListener {

	/**
	 * @param args
	 * @throws IOException 
	 */
	
	static String sourcePath, outputPath, tmxCleanerMessages, outputText = "";
	static private final String currentlySupportedCATSoftware = "Currently supported CAT Software: MemoQ, SDL Language Platform, SDL TRADOS TagEditor, Across\n\nIf the TM export in XML format (usually TMX or TTX) doesn't work for one of these, let me know!\n____________________________________________________\n\n";
	
	static private final String newline = "\n";
	JButton openButton, saveButton;
	JTextArea log;
	JFileChooser fc;
	
	public TmxCleaner() {
		super(new BorderLayout());
		
		log = new JTextArea(5,20);
		log.setMargin(new Insets(5,5,5,5));
		log.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(log);
		log.append(currentlySupportedCATSoftware);
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
	
	public void actionPerformed(ActionEvent e) {
		//Handle open button action.
        if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(TmxCleaner.this);
 
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                sourcePath = file.getPath();
                try {
					tmxCleanerMessages = cleanTMX(sourcePath);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
                log.append("Opening source file:\t" + file.getPath() + "." + newline);
                log.append(tmxCleanerMessages);
            } else {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
 
        //Handle save button action.
        } else if (e.getSource() == saveButton) {
            int returnVal = fc.showSaveDialog(TmxCleaner.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                outputPath = file.getPath() + ".txt";
                try {
					saveFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
                log.append("\n\nSource segments exported to:\t" + file.getPath() + ".txt\n____________________________________________________\n\n");
                try {
                	File openFile = new File(file.getPath() + ".txt");
					Desktop.getDesktop().open((openFile));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            } else {
                log.append("Save command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
        }
    }
	
	
	
	public static void main(String[] args) throws IOException {
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
		
		
		JTextArea textArea = new JTextArea(tmxCleanerMessages);
		frame.getContentPane().add(textArea);
		
		frame.add(new TmxCleaner());
		frame.pack();
		frame.setVisible(true);
	}
	
	
	
	private static String cleanTMX(String sourcePath) throws IOException {
		String finalTextOut = "";
		String messagesOut = "";
		String filterPattern = "";
		Integer lineCounter = 0;
		String fileEncoding = getFileEncoding(sourcePath);
		String inputFileText = "";
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sourcePath), fileEncoding));
		try {
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				sb.append('\n');
				line = br.readLine();
			}
			inputFileText = sb.toString();
		} finally {
			sb = null;
			sb = new StringBuilder();
			br.close();
		}
		

		String creationTool = getCreationTool(inputFileText);
		String languageCode = getLanguageCode(inputFileText);
		
		filterPattern = setRegexFilter(creationTool, languageCode);
		
		
		
		Pattern regexPattern = Pattern.compile(filterPattern, Pattern.CASE_INSENSITIVE);
		Matcher regexMatcher = regexPattern.matcher(inputFileText);
		
		while (regexMatcher.find()) {
			sb.append(regexMatcher.group(1));
			sb.append('\n');
			lineCounter++;
		}
		
		finalTextOut = sb.toString();
		outputText = littleFixes(finalTextOut);
		
		
		messagesOut += "File was created with:\t" + creationTool + "\nFile was encoded with:\t" + fileEncoding + "\nSource text language is:\t" + languageCode;
		messagesOut += "\nSource segments found:\t" + lineCounter;
		return messagesOut;
	}
	
	private static void saveFile() throws IOException {
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath),"UTF-16"));
		out.write(outputText);
		out.close();
	}
	
	private static String getCreationTool(String inputFileText) {
		String findCreationToolFilter = "creationtool=\"(.*?)\"";
		Pattern findCreationToolPattern = Pattern.compile(findCreationToolFilter, Pattern.CASE_INSENSITIVE);
		Matcher findCreationToolMatcher = findCreationToolPattern.matcher(inputFileText);
		
		String creationTool = "";
				
		while (findCreationToolMatcher.find()) {
			if (findCreationToolMatcher.group(1) != null)
				creationTool = findCreationToolMatcher.group(1);
			else break;
		}
		return creationTool;
	}
	
	private static String getLanguageCode(String inputFileText) {
		String languageCode = "";
		String findLanguageCodeFilter = "";
		
		if (inputFileText.contains("srclang") == true)
			findLanguageCodeFilter = "srclang=\"(.*?)\""; //MemoQ TMX; SDL Language Platform
		if (inputFileText.contains("SourceLanguage") == true)
			findLanguageCodeFilter = "SourceLanguage=\"(.*?)\""; //SDL TRADOS TagEditor TTX
		
		Pattern findLanguageCodePattern = Pattern.compile(findLanguageCodeFilter, Pattern.CASE_INSENSITIVE);
		Matcher findLanguageCodeMatcher = findLanguageCodePattern.matcher(inputFileText);
		
		while (findLanguageCodeMatcher.find()) {
				languageCode = findLanguageCodeMatcher.group(1);
		}
		if (languageCode == "")
			languageCode = "TMXCleaner cannot determine the segment source language!";
		
		return languageCode;
	}
	
	private static String setRegexFilter(String creationTool, String languageCode) {
		String regexFilter = "";
		
		if (creationTool.equalsIgnoreCase("memoq") || creationTool.equalsIgnoreCase("sdl language platform"))
			regexFilter = "<tuv xml:lang=\"" + languageCode + "\">\n.*<seg>(.*)</seg>"; //MemoQ TMX; SDL Language Platform
		else if (creationTool.equalsIgnoreCase("sdl trados tageditor"))
			regexFilter = "<Tuv Lang=\"" + languageCode + "\">(.*?)</Tuv>"; //SDL TRADOS TagEditor TTX
		else if (creationTool.equalsIgnoreCase("across"))
			regexFilter = "<tuv xml:lang=\"" + languageCode + "\">.*<seg>(.*)</seg>"; //Across
		else regexFilter = "TMXCleaner doesn't know what to do with " + creationTool + " " + languageCode;
		
		return regexFilter;
	}
	
	private static String getFileEncoding(String inputFile) throws IOException {
		RandomAccessFile inFile = new RandomAccessFile(inputFile,"r");
        String lineData = inFile.readLine();
        String fileEncoding = "";
        inFile.close();
        String findFileEncodingFilter = "encoding=\"(.*?)\"";
		Pattern findFileEncodingPattern = Pattern.compile(findFileEncodingFilter, Pattern.CASE_INSENSITIVE);
		Matcher findFileEncodingMatcher = findFileEncodingPattern.matcher(lineData);
		while (findFileEncodingMatcher.find()) {
			fileEncoding = findFileEncodingMatcher.group(1).toUpperCase();
		}
		if (fileEncoding == "")
			fileEncoding = "UTF-16";
		return fileEncoding;
	}
	
	private static String littleFixes(String inputText) {
		inputText = inputText.replaceAll("<prop.*?</prop>", ""); //Across Standard-Adherence Fail Fix
		
		inputText = inputText.replace("&lt;", "<");	//Fix HTML entities
		inputText = inputText.replace("&gt;", ">");
		inputText = inputText.replace("&amp;", "&");
		inputText = inputText.replace("&cent;", "¢");
		inputText = inputText.replace("&pound;", "£");
		inputText = inputText.replace("&yen;", "¥");
		inputText = inputText.replace("&euro;", "€");
		inputText = inputText.replace("&sect;", "§");
		inputText = inputText.replace("&copy;", "©");
		inputText = inputText.replace("&reg;", "®");
		inputText = inputText.replace("&trade;", "™");
		
		return inputText;
	}
}
