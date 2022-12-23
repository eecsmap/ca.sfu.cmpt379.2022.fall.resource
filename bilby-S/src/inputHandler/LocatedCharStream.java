package inputHandler;


import java.util.Iterator;



public class LocatedCharStream implements Iterator<LocatedChar> {
	public static final char NULL_CHAR = '\0';
	public static final LocatedChar FLAG_END_OF_INPUT = new LocatedChar(NULL_CHAR, new TextLocation("null", -1, -1));

	
	private Iterator<String> inputIterator;
	private String line;
	private int index;

	private LocatedChar next;
	private InputHandler input;
	
	
	public LocatedCharStream(InputHandler input) {
		super();
		this.input = input;
		this.inputIterator = input.iterator();
		this.index = 0;
		this.line = "";
		preloadChar();
	}
	
	private void preloadChar() {
		ensureLineHasACharacter();
		next = nextCharInLine();
	}	
	private LocatedChar nextCharInLine() {
		if(endOfInput()) {
			return FLAG_END_OF_INPUT;
		}
		
		TextLocation location = new TextLocation(input.fileName(), input.lineNumber(), index);
		char character = line.charAt(index++);
		return new LocatedChar(character, location);
	}
	private void ensureLineHasACharacter() {
		while(!moreCharsInLine() && inputIterator.hasNext()) {
			readNextLine();
		}
	}
	private boolean endOfInput() {
		return !moreCharsInLine() && !inputIterator.hasNext();
	}
	private boolean moreCharsInLine() {
		return index < line.length();
	}
	private void readNextLine() {
		assert(inputIterator.hasNext());
		line = inputIterator.next();
		index = 0;
	}
	
	
//////////////////////////////////////////////////////////////////////////////
// Iterator<LocatedChar> overrides
// next() extra-politely returns a fully-formed LocatedChar (FLAG_END_OF_INPUT)
//         if hasNext() is false.  FLAG_END_OF_INPUT is a lightweight Null Object.

	@Override
	public boolean hasNext() {
		return next != FLAG_END_OF_INPUT;
	}
	@Override
	public LocatedChar next() {
		LocatedChar result = next;
		preloadChar();
		return result;
	}

	/**
	 * remove is an unsupported operation.  It throws an UnsupportedOperationException.
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
