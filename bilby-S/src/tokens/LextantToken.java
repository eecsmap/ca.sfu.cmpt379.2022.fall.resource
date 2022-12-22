package tokens;

import lexicalAnalyzer.Lextant;
import inputHandler.Locator;

public final class LextantToken extends TokenImp {

	private Lextant lextant;
	
	private LextantToken(Locator locator, String lexeme, Lextant lextant) {
		super(locator, lexeme);
		this.lextant = lextant;
	}
	
	public Lextant getLextant() {
		return lextant;
	}
	public boolean isLextant(Lextant ...lextants) {
		for(Lextant lextant: lextants) {
			if(this.lextant == lextant)
				return true;
		}
		return false;
	}
	protected String rawString() {
		return lextant.toString();
	}
	
	
	public static LextantToken make(Locator locator, String lexeme, Lextant lextant) {
		return new LextantToken(locator, lexeme, lextant);
	}
}
