package inputHandler.tests;

import inputHandler.InputHandler;

import java.io.FileNotFoundException;
import java.util.Iterator;

import junit.framework.TestCase;

import static inputHandler.tests.FixtureDefinitions.*;


public class TestInputHandler extends TestCase {
	protected InputHandler factory(String filename, String terminator) {
		return InputHandler.fromFilename(filename, terminator);
	}


	public void testHappyPath() throws FileNotFoundException {
		happyPath("\n");
	}
	public void testHappyPathOtherTerminator() throws FileNotFoundException {
		happyPath("aa");
	}
	public void testHappyPathEmptyTerminator() throws FileNotFoundException {
		happyPath("");
	}
	
	
	private void happyPath(String terminator) throws FileNotFoundException {
		String filename = SIMPLE_FIXTURE_FILENAME;
		InputHandler handler = factory(filename, terminator);
		
		happyPathTest(handler, filename, terminator);
	}
	
	private void happyPathTest(InputHandler inputHandler, String filename, String terminator)
			throws FileNotFoundException {
		assertEquals(0, inputHandler.lineNumber());
		assertEquals(filename, inputHandler.fileName());

		Iterator<String> iterator = inputHandler.iterator();
		int lineNumber = 1;
		for(String lineExpected : simpleFixtureStrings) {
			assertTrue(iterator.hasNext());
			String lineRead = iterator.next();
			assertEquals(lineExpected + terminator, lineRead);
			assertEquals(lineNumber++, inputHandler.lineNumber());
		}

		assertEquals(filename, inputHandler.fileName());
		assertFalse(iterator.hasNext());
	}

	public void testFileNotFound() {
		try {
			InputHandler handler = factory(NONEXISTENT_FILENAME, "b");
			handler.iterator();
			fail();
		}
		catch(IllegalArgumentException e) {}
	}
}
