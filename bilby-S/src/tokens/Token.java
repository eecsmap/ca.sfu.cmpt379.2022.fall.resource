package tokens;

import lexicalAnalyzer.Lextant;
import inputHandler.Locator;
import inputHandler.TextLocation;

public interface Token extends Locator {
	public String getLexeme();
	public TextLocation getLocation();
	public String fullString();
	public boolean isLextant(Lextant ...lextants);
}

