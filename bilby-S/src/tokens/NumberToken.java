package tokens;

import inputHandler.Locator;

public class NumberToken extends TokenImp {
	protected int value;
	
	protected NumberToken(Locator locator, String lexeme) {
		super(locator, lexeme);
	}
	protected void setValue(int value) {
		this.value = value;
	}
	public int getValue() {
		return value;
	}
	
	public static NumberToken make(Locator locator, String lexeme) {
		NumberToken result = new NumberToken(locator, lexeme);
		result.setValue(Integer.parseInt(lexeme));
		return result;
	}
	
	@Override
	protected String rawString() {
		return "number, " + value;
	}
}
