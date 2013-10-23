import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TmxCleaner {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.out.println("\n#################################################\nRolando Benjamin Vaz Ferreira's TMXCleaner\n\nTo make the magic happen, type: java -jar TmxCleaner [path to tm file]" + "\n" + "e.g.: java -jar TmxCleaner.jar c:\\random_file.tmx");
			System.exit(0);
		}
		String sourcePath = args[0].toString();
		String outputPath = sourcePath + ".txt";
		String inputFileText = "";
		String finalTextOut = "";
		StringBuilder sb = new StringBuilder();
		String filterPattern = "";
		Integer lineCounter = 0;
		String fileEncoding = getFileEncoding(sourcePath);
		
		
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
		
		
		
		System.out.println("\n#################################################\nRolando Benjamin Vaz Ferreira's TMXCleaner v0.9b\n\nThe magic begins here:\n");
		System.out.println("This ToMe was encoded with: " + fileEncoding);
		filterPattern = setRegexFilter(getCreationTool(inputFileText), getLanguageCode(inputFileText));
		
		Pattern regexPattern = Pattern.compile(filterPattern, Pattern.CASE_INSENSITIVE);
		Matcher regexMatcher = regexPattern.matcher(inputFileText);
		
		while (regexMatcher.find()) {
			sb.append(regexMatcher.group(1));
			sb.append('\n');
			lineCounter++;
		}
		finalTextOut = sb.toString();
		
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath),"UTF-16"));
		
		out.write(finalTextOut);
		out.close();
		
		System.out.println(lineCounter + " lines of magic have been reconstructed.\n\nYour new TeXTome is hiding here: " + outputPath);
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
		System.out.println("This ToMe was created with: " + creationTool);
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
			languageCode = "TMXCleaner cannot determine the source language of these incantations!";
		
		System.out.println("This ToMe was written in: " + languageCode);
		return languageCode;
	}
	
	private static String setRegexFilter(String creationTool, String languageCode) {
		String regexFilter = "";
		
		if (creationTool.equalsIgnoreCase("memoq") || creationTool.equalsIgnoreCase("sdl language platform"))
			regexFilter = "<tuv xml:lang=\"" + languageCode + "\">\n.*<seg>(.*)</seg>"; //MemoQ TMX; SDL Language Platform
		else if (creationTool.equalsIgnoreCase("sdl trados tageditor"))
			regexFilter = "<Tuv Lang=\"" + languageCode + "\">(.*?)</Tuv>"; //SDL TRADOS TagEditor TTX
		else regexFilter = "TMXCleaner doesn't know what to doooo with " + creationTool + " " + languageCode;
		
		System.out.println("This ToMe is decoded with the incantation: " + regexFilter.replace("\n", "\\n") + "\n\n");
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
}
