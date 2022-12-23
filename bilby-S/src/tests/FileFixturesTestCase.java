package tests;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;


import junit.framework.TestCase;


public abstract class FileFixturesTestCase extends TestCase {
	public interface Command {
		void run(PrintStream out) throws Exception;
	}

////////////////////////////////////////////////////////////////////////////////////
//string i/o	

	public String outputFor(Command command) throws Exception {
		System.setProperty("line.separator", "\r\n");
		OutputStream byteArrayOS = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(byteArrayOS);
		command.run(out);
		return byteArrayOS.toString();
	}

////////////////////////////////////////////////////////////////////////////////////
//  file i/o
	
	public InputStreamReader readerForFilename(String filename) throws FileNotFoundException {
		FileInputStream sourceStream = new FileInputStream(filename);
		return new InputStreamReader(sourceStream);
	}
	
	public String getContents(String filename)
	throws IOException {
		InputStreamReader reader = readerForFilename(filename);
		return contentsAsString(reader);
	}
	private String contentsAsString(InputStreamReader reader) 
	throws IOException {
		StringBuffer result = new StringBuffer();
		char[] buffer = new char[1024];
		int amount;
		while ((amount = reader.read(buffer)) != -1) {
			result.append(buffer, 0, amount);
		}
		reader.close();
		
		return result.toString();
	}
}
