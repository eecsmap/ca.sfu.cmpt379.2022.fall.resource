package applications.tests;

import static applications.tests.FixtureDefinitions.TOKEN_PRINTER_EXPECTED_FILENAME;
import static applications.tests.FixtureDefinitions.TOKEN_PRINTER_INPUT_FILENAME;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import tests.FileFixturesTestCase;
import tokens.Tokens;
import applications.BilbyTokenPrinter;

public class TestTokenPrinter extends FileFixturesTestCase {

	
	public void testTokenPrinter() throws Exception {
		String actualOutput =	tokenPrinterOutput(TOKEN_PRINTER_INPUT_FILENAME);
		String expectedOutput = getContents(TOKEN_PRINTER_EXPECTED_FILENAME);
		assertEquals(expectedOutput, actualOutput);
	}

	private String tokenPrinterOutput(String filename) throws Exception {
		return outputFor(new TokenPrinterCommand(filename));
	}
	
	public class TokenPrinterCommand implements Command {
		String filename;
		public TokenPrinterCommand(String filename) {
			this.filename = filename;
		}

		public void run(PrintStream out) throws FileNotFoundException {
			Tokens.setPrintLevel(Tokens.Level.TYPE_AND_VALUE);
			BilbyTokenPrinter.scanFile(filename, out);
		}
	}
}
