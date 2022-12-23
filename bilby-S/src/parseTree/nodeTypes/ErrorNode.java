package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.Token;

public class ErrorNode extends ParseNode {

	public ErrorNode(ParseNode node) {
		super(node);
	}
	public ErrorNode(Token token) {
		super(token);
	}
	
	
	///////////////////////////////////////////////////////////
	// boilerplate for visitors
			
	public void accept(ParseNodeVisitor visitor) {
		visitor.visit(this);
	}
}
