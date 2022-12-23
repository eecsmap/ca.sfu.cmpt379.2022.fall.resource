package inputHandler.tests;

import inputHandler.LineBasedReader;
import junit.framework.TestCase;

import static inputHandler.tests.FixtureDefinitions.*;


public class TestLineBasedIteratorImp extends TestCase {

	protected LineBasedReader factory(String filename) {
		return new LineBasedReader(filename);
	}
	
	
	public void testHappyPath() {
		LineBasedReader reader = factory(SIMPLE_FIXTURE_FILENAME);
		
		for(String lineExpected : simpleFixtureStrings) {
			assertTrue(reader.hasNext());
			String lineRead = reader.next();
			assertEquals(lineExpected, lineRead);
		}
		assertFalse(reader.hasNext());
	}
	
	public void testFileNotFound() {
		try {
			factory(NONEXISTENT_FILENAME);
			fail();
		}
		catch(IllegalArgumentException e) {}
	}
}
