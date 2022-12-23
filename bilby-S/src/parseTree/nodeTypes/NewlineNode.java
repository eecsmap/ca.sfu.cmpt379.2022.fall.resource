package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.Token;

public class NewlineNode extends ParseNode {

	public NewlineNode(ParseNode node) {
		super(node);
	}
	public NewlineNode(Token token) {
		super(token);
	}
	
	
	///////////////////////////////////////////////////////////
	// boilerplate for visitors
			
	public void accept(ParseNodeVisitor visitor) {
		visitor.visit(this);
	}
}
