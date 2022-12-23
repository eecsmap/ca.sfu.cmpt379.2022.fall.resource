package applications;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import tokens.Token;
import tokens.Tokens;

import lexicalAnalyzer.LexicalAnalyzer;
import lexicalAnalyzer.Scanner;

public class BilbyTokenPrinter extends BilbyApplication {
	/** Prints tokens from a bilby file.
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		checkArguments(args, className());
		
		Tokens.setPrintLevel(Tokens.Level.TYPE_AND_VALUE);
		scanFile(args[0], System.out);
	}
	
	/** prints the bilby tokens in the file specified by filename
	 * to the given PrintStream.
	 * @param filename the name of the file to be listed.
	 * @param out the PrintStream to list to.
	 * @throws FileNotFoundException 
	 */
	public static void scanFile(String filename, PrintStream out) throws FileNotFoundException {
		Scanner scanner     = LexicalAnalyzer.make(filename);
		
		while(scanner.hasNext()) {
			printNextToken(out, scanner);
		}
		printNextToken(out, scanner);		// prints NullToken
	}

	private static void printNextToken(PrintStream out, Scanner scanner) {
		Token token = scanner.next();
		out.println(token.toString());
	}
}
