package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import lexicalAnalyzer.Keyword;
import tokens.Token;

public class PrintStatementNode extends ParseNode {
	
	public PrintStatementNode(Token token) {
		super(token);
		assert(token.isLextant(Keyword.PRINT));
	}

	public PrintStatementNode(ParseNode node) {
		super(node);
	}

	
	////////////////////////////////////////////////////////////
	// attributes
	
	
	///////////////////////////////////////////////////////////
	// boilerplate for visitors
		
	public void accept(ParseNodeVisitor visitor) {
		visitor.visitEnter(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}

}
