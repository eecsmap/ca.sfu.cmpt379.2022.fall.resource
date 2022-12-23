package inputHandler;

import java.util.Iterator;

/** A line-based file reader that knows the filename and line number.
 *  Use one of the factories <code>fromFilename(...)</code> to construct.
 *  <p>
 *  The lines returned by its iterator have their original line terminator
 *  replaced by the terminator given to the factory (or the default of "\n"
 *  if the one-argument factory is used).
 *  <p>
 *  Although InputHandler is an Iterable on String, it permits only
 *  one Iterator.  The current line number of the InputHandler is the
 *  line number of the corresponding Iterator, if any. 
 *
 */
public class InputHandler implements Iterable<String> {
	private static final String DEFAULT_TERMINATOR = "\n";
	
	protected int lineNumber = 0;
	private boolean iteratorIssued = false;
	protected String filename;
	protected String terminator;
	
	
	private InputHandler(String filename, String terminator) {
		this.filename = filename;
		this.terminator = terminator;
	}
	private InputHandler(String filename) {
		this(filename, DEFAULT_TERMINATOR);
	}

	
	/** Get the file name.
	 * @return the filename that was passed to the constructor.
	 * This may be a relative or absolute file name.
	 */
	public String fileName() {
		return filename;
	}
	/** Get the current line number.
	 * @return the one-based line number of the line that the
	 * <code>next()</code> last returned, or
	 * zero if <code>next</code> has not been called.
	 */
	public int lineNumber() {
		return lineNumber;
	}


	@Override
	public Iterator<String> iterator() {
		ensureSingleIterator();
		
		return new HandlerIterator(filename);
	}
	private void ensureSingleIterator() {
		if(iteratorIssued) {
			throw new IllegalStateException("Cannot get two iterators on same InputHandler.");
		}
		iteratorIssued = true;
	}
	

	protected class HandlerIterator extends LineBasedReader {
		public HandlerIterator(String filename) {
			super(filename);
		}

		@Override
		public String next() {
			lineNumber++;
			return super.next() + terminator;
		}
	}


//////////////////////////////////////////////////////////////////////////////
// factories
	
	public static InputHandler fromFilename(String filename, String terminator) {
		return new InputHandler(filename, terminator);
	}
	public static InputHandler fromFilename(String filename) {
		return fromFilename(filename, DEFAULT_TERMINATOR);
	}
}
