package si.ferreira.tmxcleaner;

import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

@SuppressWarnings("serial")
public class TmxCleaner extends JPanel implements ActionListener, ItemListener {
	//Prepare global strings
	static String sysLog = "";
	static String[] tmFile = null;
	static private final String newline = "\n";
	static private final String welcomeMessage = "This application should work with TMX files exported by MemoQ, SDL Studio, SDL TRADOS TagEditor, Across, Transit NXT, and with any other standard TMX file.\n\nIf you run across a TMX that the app can't process, please send its header and a few sample translation units to benjamin@ferreira.si, and I'll look into it.\n\nI don't take any responsibility for what you do with this app or the consequences of its use.";
	
	//Prepare UI elements
	static JCheckBox openAfterExportCB;
	static JButton openButton;
	static JButton saveButton;
	static JButton originalTextButton;
	static JButton cleanedTextButton;
	static boolean openAfterExport = true;
	static JTextArea log;
	JFileChooser fc;
	
	static JTextArea tmRecognizedSegmentsContent = new JTextArea();
	static JTextArea tmCreationToolContent = new JTextArea();
	static JTextArea tmFilePathContent = new JTextArea();
	static JTextArea tmFileEncodingContent = new JTextArea();
	static JTextArea tmSourceLanguageContent = new JTextArea();

	
	
	public TmxCleaner() {
		//Prepare the layout
		openButton.addActionListener(this);
		saveButton.addActionListener(this);
		openAfterExportCB.addItemListener(this);
		originalTextButton.addActionListener(this);
		cleanedTextButton.addActionListener(this);
		
		//Create FileChooser and set default directory
		fc = new JFileChooser("E:\\Java\\TmxCleaner\\files\\tms");
	}
	
	public static void addComponentsToPane(Container pane) {
		pane.setLayout(new GridBagLayout());
		
		pane.setBackground(Color.white);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(10,10,10,10);
		
		//Create UI elements
		openButton = new JButton("Open the source TM XML");
		openButton.setPreferredSize(new Dimension(200,40));
		c.insets = new Insets(10,0,0,0);
		c.gridx = 0;
		c.gridy = 0;
		pane.add(openButton, c);
		
		saveButton = new JButton("Save source as TXT");
		saveButton.setPreferredSize(new Dimension(200,40));
		c.gridx = 0;
		c.gridy = 2;
		pane.add(saveButton, c);
				
		openAfterExportCB = new JCheckBox("Open exported file when done");
		openAfterExportCB.setSelected(true);
		openAfterExportCB.setBackground(Color.white);
		c.gridx = 0;
		c.gridy = 3;
		pane.add(openAfterExportCB, c);
		
		
		JLabel tmFilePathLabel = new JLabel();
		tmFilePathLabel.setText("File name:");
		c.insets = new Insets(10,10,10,10);
		c.gridy = 6;
		c.fill = GridBagConstraints.HORIZONTAL;
		pane.add(tmFilePathLabel, c);
		
		c.insets = new Insets(-10,10,10,10);
		c.gridy = 7;
		c.fill = GridBagConstraints.HORIZONTAL;
		tmFilePathContent.setWrapStyleWord(true);
		tmFilePathContent.setLineWrap(true);
		pane.add(tmFilePathContent, c);
		
		
		JLabel tmFileEncodingLabel = new JLabel();
		tmFileEncodingLabel.setText("Encoded in:");
		c.insets = new Insets(10,10,10,10);
		c.gridy = 8;
		c.fill = GridBagConstraints.HORIZONTAL;
		pane.add(tmFileEncodingLabel, c);
				
		c.insets = new Insets(-10,10,10,10);
		c.gridy = 9;
		c.fill = GridBagConstraints.HORIZONTAL;
		tmFileEncodingContent.setWrapStyleWord(true);
		tmFileEncodingContent.setLineWrap(true);
		pane.add(tmFileEncodingContent, c);
		
		
		JLabel tmCreationToolLabel = new JLabel();
		tmCreationToolLabel.setText("Created with:");
		c.insets = new Insets(10,10,10,10);
		c.gridy = 10;
		c.fill = GridBagConstraints.HORIZONTAL;
		pane.add(tmCreationToolLabel, c);
				
		c.insets = new Insets(-10,10,10,10);
		c.gridy = 11;
		c.fill = GridBagConstraints.HORIZONTAL;
		tmCreationToolContent.setWrapStyleWord(true);
		tmCreationToolContent.setLineWrap(true);
		pane.add(tmCreationToolContent, c);
		
		
		JLabel tmSourceLanguageLabel = new JLabel();
		tmSourceLanguageLabel.setText("Source language:");
		c.insets = new Insets(10,10,10,10);
		c.gridy = 12;
		c.fill = GridBagConstraints.HORIZONTAL;
		pane.add(tmSourceLanguageLabel, c);
				
		c.insets = new Insets(-10,10,10,10);
		c.gridy = 13;
		c.fill = GridBagConstraints.HORIZONTAL;
		tmSourceLanguageContent.setWrapStyleWord(true);
		tmSourceLanguageContent.setLineWrap(true);
		pane.add(tmSourceLanguageContent, c);
		
		
		JLabel tmRecognizedSegmentsLabel = new JLabel();
		tmRecognizedSegmentsLabel.setText("Segments found:");
		c.insets = new Insets(10,10,10,10);
		c.gridy = 14;
		c.fill = GridBagConstraints.HORIZONTAL;
		pane.add(tmRecognizedSegmentsLabel, c);
				
		c.insets = new Insets(-10,10,10,10);
		c.gridy = 15;
		c.fill = GridBagConstraints.HORIZONTAL;
		tmRecognizedSegmentsContent.setWrapStyleWord(true);
		tmRecognizedSegmentsContent.setLineWrap(true);
		pane.add(tmRecognizedSegmentsContent, c);
		
		
		originalTextButton = new JButton("Display original TMX");
		originalTextButton.setPreferredSize(new Dimension(200,40));
		c.insets = new Insets(10,10,10,10);
		c.gridx = 0;
		c.gridy = 16;
		pane.add(originalTextButton, c);
		
		cleanedTextButton = new JButton("Display cleaned TXT");
		cleanedTextButton.setPreferredSize(new Dimension(200,40));
		c.insets = new Insets(10,10,10,10);
		c.gridx = 0;
		c.gridy = 17;
		pane.add(cleanedTextButton, c);
		
		
		JTextArea message = new JTextArea();
		c.insets = new Insets(-10,10,10,10);
		message.setText(welcomeMessage);
		message.setWrapStyleWord(true);
		message.setLineWrap(true);
		message.setMargin(new Insets(10,10,10,10));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridheight = 5;
		c.gridx = 1;
		c.gridy = 0;
		pane.add(message, c);
		
		//Prepare the text output area
		log = new JTextArea(5,20);
		log.setWrapStyleWord(true);
		log.setLineWrap(true);
		log.setMargin(new Insets(5,5,5,5));
		log.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(log, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 1;
		c.gridy = 6;
		c.gridheight = 15;
		c.weighty = 1;
		c.weightx = 1;
		pane.add(logScrollPane, c);
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
		
		frame.setPreferredSize(new Dimension(800,600));
		
		addComponentsToPane(frame.getContentPane());
		
		frame.add(new TmxCleaner());
		frame.pack();
		frame.setVisible(true);
	}
	
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		
		if (source == openAfterExportCB) {
			openAfterExport = !openAfterExport;
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		String sourcePath;
		
		//Handle open button action.
        if (e.getSource() == openButton) {
            log.setText("");
            log.setText("Opening a large file can take a while. Please be patient..." + newline + newline);
            int returnVal = fc.showOpenDialog(TmxCleaner.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                sourcePath = file.getPath();
                //System.out.println("Invoking cleanTMX()");
                tmFile = cleanTMX(sourcePath);
                if (Integer.valueOf(tmFile[2]) > 0)
                	log.setText("Processing successful!");
                else
                	log.setText("Processing seems to have failed...");
                tmFilePathContent.setText(file.getName());
				tmFileEncodingContent.setText(tmFile[3]);
				tmCreationToolContent.setText(tmFile[4]);
				tmSourceLanguageContent.setText(tmFile[5]);
				tmRecognizedSegmentsContent.setText(tmFile[2]);
            } else {
                log.setText("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
 
        //Handle save button action.
        } else if (e.getSource() == saveButton) {
        	if (tmFile != null) {
	        	String outputPath;
	            int returnVal = fc.showSaveDialog(TmxCleaner.this);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                File file = fc.getSelectedFile();
	                outputPath = file.getPath() + ".txt";
	                saveFile(outputPath, tmFile[0]);
	                log.setText("Source segments exported to:\t" + file.getPath() + ".txt\n\nYou can now open another file or exit the program.");
	                try {
	                	File openFile = new File(file.getPath() + ".txt");
						if (openAfterExport == true) 
							Desktop.getDesktop().open((openFile));
					} catch (IOException e1) {
						sysLog = e1.getMessage();
					}
	            } else {
	                log.setText("Save command cancelled by user." + newline);
	            }
	            log.setCaretPosition(log.getDocument().getLength());
        	} else {
        		log.setText("Please open a file first\n");
        	}
        } else if (e.getSource() == originalTextButton) {
        	if (tmFile != null) {
        		log.setText(tmFile[1]);
        		log.setCaretPosition(0);
        	}
        	else
        		log.setText("Please open a file first\n");
        } else if (e.getSource() == cleanedTextButton) {
        	if (tmFile != null) {
        		log.setText(tmFile[0]);
        		log.setCaretPosition(0);
        	}
        	else
        		log.setText("Please open a file first\n");
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
		//System.out.println("Invoking openFile()");
		fileInfo[0] = openFile(sourcePath);

		//Get file encoding
		fileInfoPattern = Pattern.compile(feRe);
		fileInfoMatcher = fileInfoPattern.matcher(fileInfo[0]);
		fileInfoMatcher.find();
		fileInfo[1] = fileInfoMatcher.group(1).toUpperCase();
		//System.out.println("Encoding " + fileInfo[1]);
		
		//Get file creation tool
		fileInfoPattern = Pattern.compile(ctRe);
		fileInfoMatcher = fileInfoPattern.matcher(fileInfo[0]);
		fileInfoMatcher.find();
		fileInfo[2] = fileInfoMatcher.group(1);
		
		//Get TU source language
		fileInfoPattern = Pattern.compile(slRe);
		fileInfoMatcher = fileInfoPattern.matcher(fileInfo[0]);
		fileInfoMatcher.find();
		fileInfo[3] = fileInfoMatcher.group(1);
		
		//System.out.println("Returning from getFile()");
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
		
		//System.out.println("Preliminary open for charset complete. Opening for real...");
		
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
			//System.out.println("File completely read");
		} catch (UnsupportedEncodingException e) {
			sysLog = e.getMessage();
		} catch (FileNotFoundException e) {
			sysLog = e.getMessage();
		} catch (IOException e) {
			sysLog = e.getMessage();
		}
		
		//System.out.println("Returning from openFile()");
		return contentText;
	}

	private static String[] cleanTMX(String sourcePath) {
		//System.out.println("Invoking getFile()");
		String[] fileInfo = getFile(sourcePath);
		//System.out.println("Language: " + fileInfo[3]);
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
		//System.out.println("Starting cleaning");
		
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
