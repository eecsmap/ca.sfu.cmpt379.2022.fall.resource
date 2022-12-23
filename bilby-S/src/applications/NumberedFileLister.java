package applications;

import inputHandler.InputHandler;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class NumberedFileLister extends BilbyApplication {
	private static InputHandler handler;

	/** Lists a file with line-number prefixes.
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		checkArguments(args, "NumberedFileLister");
		listFile(args[0], System.out);
	}

	
	/** prints a file specified by filename to the given PrintStream.
	 *  Each line of the file is preceded by its one-based line number.
	 * @param filename the name of the file to be listed.
	 * @param out the PrintStream to list to.
	 * @throws FileNotFoundException 
	 */
	public static void listFile(String filename, PrintStream out) throws FileNotFoundException {
		handler = InputHandler.fromFilename(filename, "");

		for(String line: handler) {
			int number = handler.lineNumber();
			out.format("%3d: %s", number, line);
			out.println();				// to get the proper line terminator.
		}
	}
}
