package applications.tests;

import static applications.tests.FixtureDefinitions.AST_INPUT_FILENAME;
import static applications.tests.FixtureDefinitions.AST_EXPECTED_FULL_FILENAME;
import static applications.tests.FixtureDefinitions.AST_EXPECTED_TOKEN_ONLY_FILENAME;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import parseTree.ParseTreePrinter;
import parseTree.ParseTreePrinter.Level;

import tests.FileFixturesTestCase;
import applications.BilbyAbstractSyntaxTree;

public class TestAbstractSyntaxTree extends FileFixturesTestCase {

	
	public void testAbstractSyntaxTreeFull() throws Exception {
		tokenPrinterSingleTest(Level.FULL, AST_EXPECTED_FULL_FILENAME);
	}
	public void testAbstractSyntaxTreeTokenOnly() throws Exception {
		tokenPrinterSingleTest(Level.TOKEN_ONLY, AST_EXPECTED_TOKEN_ONLY_FILENAME);
	}
	
	public void tokenPrinterSingleTest(Level level, String string) throws Exception {
		ParseTreePrinter.setPrintLevel(level);
		String actualOutput =	tokenPrinterOutput(AST_INPUT_FILENAME);
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
			BilbyAbstractSyntaxTree.parseFileToAST(filename, out);
		}
	}
}
