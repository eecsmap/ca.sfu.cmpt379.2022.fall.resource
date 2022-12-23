package inputHandler.tests;

import static inputHandler.tests.FixtureDefinitions.*;

import inputHandler.InputHandler;
import inputHandler.LocatedChar;
import inputHandler.LocatedCharStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import tests.FileFixturesTestCase;

public class TestLocatedCharStream extends FileFixturesTestCase {

	public void testLocCharStream() throws Exception {
		String actualOutput =	locCharStreamOutput(SIMPLE_FIXTURE_FILENAME);
		String expectedOutput = getContents(SIMPLE_LCHAR_FILENAME);
		assertEquals(expectedOutput, actualOutput);
	}
	
	public String locCharStreamOutput(String filename) throws Exception {
		InputHandler input = InputHandler.fromFilename(filename, "\n");
		LocatedCharStream stream = new LocatedCharStream(input);
		Command printCommand = new printLCStreamCommand(stream);
		return outputFor(printCommand);
	}
	
	private void printLocatedCharStream(LocatedCharStream stream, PrintStream out) {
		while(stream.hasNext()) {
			LocatedChar c = stream.next();
			out.println(c);
		}
	}

	public class printLCStreamCommand implements Command {
		LocatedCharStream stream;
		public printLCStreamCommand(LocatedCharStream stream) {
			this.stream = stream;
		}
		
		public void run(PrintStream out) throws FileNotFoundException {
			printLocatedCharStream(stream, out);
		}
	}

//////////////////////////////////////////////////////////////////////////////
// write the fixture for this regression test.  Change writeFixture to true then run.
// don't forget to change it back to false after.
	
	
	public void testWriteRegressionFixture() throws Exception {
		boolean writeFixture = false;
		if(writeFixture) {	
			String actualOutput =	locCharStreamOutput(SIMPLE_FIXTURE_FILENAME);		
			PrintStream out = forFile(SIMPLE_LCHAR_FILENAME);
			out.print(actualOutput);
			System.out.println("rewrote regression fixture " + SIMPLE_LCHAR_FILENAME);
			fail();
		}
	}
	
	private PrintStream forFile(String filename) throws FileNotFoundException {
		File file = new File(filename);
		FileOutputStream fos = new FileOutputStream(file);
		return new PrintStream(fos);
	}

}
