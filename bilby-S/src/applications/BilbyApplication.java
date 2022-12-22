package applications;

import java.io.File;

public class BilbyApplication {
	private static final int EXIT_CODE_FOR_ERROR = 1;
	private static String outputDirectory = "output/";

	public BilbyApplication() {
		super();
	}

	protected static void checkArguments(String[] args, String applicationName) {
		if(!correctNumArguments(args)) {
			printUsageMessage(applicationName, "");
		}
	
		ensureSourceFileExists(args, applicationName); 			// first arg
		ensureTargetDirectoryExists(args, applicationName);		// second (optional) arg
	}

	
	protected static boolean correctNumArguments(String[] args) {
		return  1 <= args.length && args.length <= 2;
	}
	protected static void ensureSourceFileExists(String[] args, String applicationName) {
		if(!fileExists(args[0])) {
			printUsageMessage(applicationName, "Source file does not exist.");
		}
	}	
	protected static void ensureTargetDirectoryExists(String[] args, String applicationName) {
		if(args.length > 1) {
			outputDirectory  = args[1];
		}
		
		if(!makeDirectoryIfNecessary(outputDirectory)) {
			printUsageMessage(applicationName, "Target directory cannot be created.");
		}
		outputDirectory = ensureEndsWithSeparator(outputDirectory);
	}


	
	protected static String ensureEndsWithSeparator(String string) {
		return string + 
			   (endsWithSeparator(string) ? File.separator : "");
	}
	protected static boolean endsWithSeparator(String filePath) {
		return (!filePath.endsWith(File.separator));
	}
	protected static String outputFilename(String filename) {
		return outputDirectory + basename(filename) + ".asm";
	}
	// removes preceding directory names and the file extension
	// e.g. /usr/root/tricks/bigBag.cpp  ->  bigBag
	protected static String basename(String filename) {
		int lastSlash = filename.lastIndexOf('/');
		int lastBackslash = filename.lastIndexOf('\\');
		int start = Math.max(lastSlash, lastBackslash) + 1;
		
		int end = filename.indexOf('.', start);
		if(end == -1) {
			return filename.substring(start);
		}
		return filename.substring(start, end);
	}
	
	protected static String className() {
		return (new Throwable()).getStackTrace()[0].getClassName();
	}


	protected static boolean makeDirectoryIfNecessary(String directoryName) {
		return directoryExists(directoryName) || createDirectory(directoryName);
	}
	protected static boolean fileExists(String filePath) {
		return (new File(filePath)).exists();
	}
	protected static boolean directoryExists(String filePath) {
		return fileExists(filePath) && (new File(filePath)).isDirectory();
	}
	protected static boolean createDirectory(String filePath) {
		return (new File(filePath)).mkdirs();
	}


	protected static void printUsageMessage(String applicationName, String errorMessage) {
		System.err.println("usage: " + applicationName + " filename" + " [target output directory]");
		System.err.println(errorMessage);
		System.exit(EXIT_CODE_FOR_ERROR);
	}
}