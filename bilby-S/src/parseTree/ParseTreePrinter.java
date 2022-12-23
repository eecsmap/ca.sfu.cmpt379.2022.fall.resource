package parseTree;

/**
 * Through the static method <code>print</code>, this class will create
 * a string that corresponds to a pretty-printing of the AST subtree rooted
 * at a given node.
 */
public class ParseTreePrinter {
	private static final int INDENT_INCREMENT = 4;
	// making terminator static causes OS-dependent failures in some FileFixturesTestCase subclasses.
	private String terminator = System.getProperty("line.separator");

	// a Level specifies which things to print.	
	public enum Level {
		FULL,
		NODE_CLASS_AND_TOKEN,
		NODE_CLASS_ONLY,
		TOKEN_ONLY;
		public boolean printToken() {
			return this != NODE_CLASS_ONLY;
		}
		public boolean printNodeClass() {
			return this != TOKEN_ONLY;
		}
		public boolean printDecorations() {
			return this == FULL;
		}
	}
	static Level printLevel = Level.FULL;
	
	/** Sets the print level for abstract syntax trees
	 * @param level	a <code>ParseTreePrinter.Level</code> specifying what information to print.
	 */
	static public void setPrintLevel(Level level) {
		ParseTreePrinter.printLevel = level;
	}
	/** Returns the current print level for abstract syntax trees.
	 * @return a specifier of what information the printer currently prints.
	 */
	static public Level getPrintLevel() {
		return ParseTreePrinter.printLevel;
	}
	/** Returns a string representation of the AST subtree rooted at the given node.
	 * 
	 * @param node	root of the subtree to create a string for
	 * @return string representation of AST
	 */
	static public String print(ParseNode node) {
		ParseTreePrinter printer = new ParseTreePrinter(0);
		return printer.makeString(node);
	}


	// per-instance code
	private final int baseIndentation;
	private StringBuffer result;
	
	// Constructor and main interface (makeString).
	private ParseTreePrinter(int indentAmount) { 
		super();
		this.baseIndentation = indentAmount;
	}	
	private String makeString(ParseNode node) {
		result = new StringBuffer();
		appendIndentedSubtree(node, baseIndentation);
		return result.toString();
	}

	// main logic for printing indented trees.
	private void appendIndentedSubtree(ParseNode node, int indentAmount) {
		appendIndentedLineFor(node, indentAmount);
		
		for(ParseNode child : node.children) {
			appendIndentedSubtree(child, indentAmount+INDENT_INCREMENT);
		}
	}
	
	// appends the information for the node, indented by the indentAmount.
	private void appendIndentedLineFor(ParseNode node, int indentAmount) {
		indent(indentAmount);
		appendLineFor(node);
	}
	private void indent(int indent) {
		for(int i=0; i<indent; i++) {
			result.append(' ');
		}
	}
	private void appendLineFor(ParseNode node) {
		appendNodeClass(node);
		appendToken(node);
		appendType(node);
		appendAllocatedSize(node);
		
		result.append(terminator);
	}
	
	// methods for the four pieces of information that we print;
	// each one appends only if the current printLevel allows it to.
	private void appendNodeClass(ParseNode node) {
		if(printLevel.printNodeClass()) {
			result.append(node.getClass().getSimpleName() + " ");
		}
	}
	private void appendToken(ParseNode node) {
		if(printLevel.printToken()) {
			result.append(node.getToken().toString() + " ");
		}
	}
	private void appendType(ParseNode node) {
		if(printLevel.printDecorations()) {
			result.append(node.getType().infoString() + " ");
		}
	}
	private void appendAllocatedSize(ParseNode node) {
		if(printLevel.printDecorations() && node.hasScope()) {
			result.append("[scope:" + node.getScope().getAllocatedSize() + " bytes] ");
		}
	}
}


