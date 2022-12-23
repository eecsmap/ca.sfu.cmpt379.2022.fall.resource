package applications.tests;

import static applications.tests.FixtureDefinitions.NUMBERED_FILE_EXPECTED_FILENAME;
import static applications.tests.FixtureDefinitions.NUMBERED_FILE_INPUT_FILENAME;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import tests.FileFixturesTestCase;
import applications.NumberedFileLister;

public class TestNumberedFileLister extends FileFixturesTestCase {

	
	public void testListFile() throws Exception {
		String actualOutput =	listFileOutput(NUMBERED_FILE_INPUT_FILENAME);
		String expectedOutput = getContents(NUMBERED_FILE_EXPECTED_FILENAME);
		assertEquals(expectedOutput, actualOutput);
	}

	private String listFileOutput(String filename) throws Exception {
		return outputFor(new ListFileCommand(filename));
	}
	
	public class ListFileCommand implements Command {
		String filename;
		public ListFileCommand(String filename) {
			this.filename = filename;
		}

		public void run(PrintStream out) throws FileNotFoundException {
			NumberedFileLister.listFile(filename, out);
		}
	}


}
