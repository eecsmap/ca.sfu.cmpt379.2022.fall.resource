package applications.tests;

public class FixtureDefinitions {
	public static final String FIXTURE_DIRECTORY = "src/applications/tests/fixtures/";
	
	public static final String NUMBERED_FILE_INPUT_FILENAME     = FIXTURE_DIRECTORY + "sampleFile.txt";
	public static final String NUMBERED_FILE_EXPECTED_FILENAME  = FIXTURE_DIRECTORY + "sampleFileListing.txt";
	
	public static final String TOKEN_PRINTER_INPUT_FILENAME     = FIXTURE_DIRECTORY + "coinTest.bilby";
	public static final String TOKEN_PRINTER_EXPECTED_FILENAME  = FIXTURE_DIRECTORY + "coinTestTokens.txt";
	
	public static final String AST_INPUT_FILENAME               = FIXTURE_DIRECTORY + "coinTest.bilby";
	public static final String AST_EXPECTED_FULL_FILENAME       = FIXTURE_DIRECTORY + "coinTestAST.txt";
	public static final String AST_EXPECTED_TOKEN_ONLY_FILENAME = FIXTURE_DIRECTORY + "coinTestTokenOnlyAST.txt";

	public static final String SEMANTIC_INPUT_FILENAME          = FIXTURE_DIRECTORY + "coinTest.bilby";
	public static final String SEMANTIC_EXPECTED_FILENAME       = FIXTURE_DIRECTORY + "coinTestSemantics.txt";
}
