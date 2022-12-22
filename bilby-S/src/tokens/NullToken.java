package tokens;

import inputHandler.Locator;

public class NullToken extends TokenImp {

	protected NullToken(Locator locator, String lexeme) {
		super(locator, lexeme);
	}

	@Override
	protected String rawString() {
		return "END OF INPUT";
	}
	
	public static NullToken make(Locator locator) {
		NullToken result = new NullToken(locator, "");
		return result;
	}
}
