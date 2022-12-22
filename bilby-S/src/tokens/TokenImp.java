package tokens;

import lexicalAnalyzer.Lextant;
import inputHandler.Locator;
import inputHandler.TextLocation;

public abstract class TokenImp implements Token {
	private TextLocation location;
	private String lexeme;

	protected TokenImp(Locator locator, String lexeme) {
		super();
		this.location =locator.getLocation();
		this.lexeme = lexeme;
	}

	@Override
	public String getLexeme() {
		return lexeme;
	}
	@Override
	public TextLocation getLocation() {
		return location;
	}
	
	/** A string (not surrounded by parentheses) representing the subclass information.
	 * @return subclass information string
	 */
	abstract protected String rawString();
	
	public String toString() {
		return "(" + rawString() + ")";
	}
	
	/** convert to a string containing all information about the token.
	 * @return string with all token info.
	 */
	public String fullString() {
		String locationString = location == null ? "(no text location)" : location.toString();
		return "(" + rawString() + 
			   ", " + locationString +
			   ", " + lexeme +
			   ")";
	}
	
	@Override
	public boolean isLextant(Lextant ...lextants) {
		return false;
	}
	
}
