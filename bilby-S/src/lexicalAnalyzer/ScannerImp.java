package lexicalAnalyzer;

import inputHandler.PushbackCharStream;
import tokens.NullToken;
import tokens.Token;

public abstract class ScannerImp implements Scanner {
	private Token nextToken;
	protected final PushbackCharStream input;
	
	protected abstract Token findNextToken();

	public ScannerImp(PushbackCharStream input) {
		super();
		this.input = input;
		nextToken = findNextToken();
	}

	// Iterator<Token> implementation
	@Override
	public boolean hasNext() {
		return !(nextToken instanceof NullToken);
	}

	@Override
	public Token next() {
		Token result = nextToken;
		nextToken = findNextToken();
		return result;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}