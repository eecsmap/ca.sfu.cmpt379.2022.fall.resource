package inputHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;


/** Simple line-based file reader.
 *  The lines returned DO NOT include the line terminator.
 * @author shermer
 *
 */
public class LineBasedReader implements Iterator<String>  {
	private BufferedReader reader;
	private String nextLine = null;

	public LineBasedReader(String filename) {
		try {
			this.reader = openFile(filename);
		}
		catch (FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
		preloadNextLine();
	}
	
//////////////////////////////////////////////////////////////////////////////
// interface: just an iterator

	@Override
	public boolean hasNext() {
		return nextLine != null;
	}

	@Override
	public String next() {
		String result = nextLine;
		preloadNextLine();
		return result;
	}

	/**
	 * remove is an unsupported operation.  It throws an UnsupportedOperationException.
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	
//////////////////////////////////////////////////////////////////////////////
// private parts

	private BufferedReader openFile(String filename)
			throws FileNotFoundException {
		File file = new File(filename);
		FileInputStream fstream = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fstream);
		return new BufferedReader(isr);
	}
	
	private void preloadNextLine() {
		nextLine  = readOneLine();
	}
	private String readOneLine() {
		try {
			return reader.readLine();
		} catch (IOException e) {
			System.err.println("Input file read error.");
			return null;
		}
	}
}
