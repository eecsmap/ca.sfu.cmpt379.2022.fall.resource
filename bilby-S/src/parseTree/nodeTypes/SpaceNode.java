package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.Token;

public class SpaceNode extends ParseNode {

	public SpaceNode(ParseNode node) {
		super(node);
	}
	public SpaceNode(Token token) {
		super(token);
	}
	
	
	///////////////////////////////////////////////////////////
	// boilerplate for visitors
			
	public void accept(ParseNodeVisitor visitor) {
		visitor.visit(this);
	}
}
