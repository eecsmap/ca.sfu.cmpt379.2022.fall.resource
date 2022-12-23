package applications.tests;

import static applications.tests.FixtureDefinitions.SEMANTIC_INPUT_FILENAME;
import static applications.tests.FixtureDefinitions.SEMANTIC_EXPECTED_FILENAME;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import parseTree.ParseTreePrinter;
import parseTree.ParseTreePrinter.Level;

import tests.FileFixturesTestCase;
import tokens.Tokens;
import applications.BilbySemanticChecker;

public class TestSemanticChecker extends FileFixturesTestCase {
	public void testSemanticChecker() throws Exception {
		semanticCheckerSingleTest(Level.FULL, Tokens.Level.FULL, SEMANTIC_EXPECTED_FILENAME);
	}
	
	public void semanticCheckerSingleTest(Level level, Tokens.Level tokenLevel, String string) throws Exception {
		ParseTreePrinter.setPrintLevel(level);
		Tokens.setPrintLevel(tokenLevel);
		String actualOutput =	tokenPrinterOutput(SEMANTIC_INPUT_FILENAME);
		String expectedOutput = getContents(string);
		assertEquals(expectedOutput, actualOutput);
	}
	private String tokenPrinterOutput(String filename) throws Exception {
		return outputFor(new ASTCommand(filename));
	}
	
	public class ASTCommand implements Command {
		String filename;
		public ASTCommand(String filename) {
			this.filename = filename;
		}

		public void run(PrintStream out) throws FileNotFoundException {
			BilbySemanticChecker.checkFileSemantics(filename, out);
		}
	}
}
