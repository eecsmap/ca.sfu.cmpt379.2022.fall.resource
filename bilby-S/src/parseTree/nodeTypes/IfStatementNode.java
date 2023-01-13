package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.Token;

public class IfStatementNode extends ParseNode {

    public IfStatementNode(Token token) {
        super(token);
    }

    public static ParseNode make(Token token, ParseNode expression, ParseNode blockStatement,
            ParseNode elseBlockStatement) {
        IfStatementNode node = new IfStatementNode(token);
        node.appendChild(expression);
        node.appendChild(blockStatement);
        node.appendChild(elseBlockStatement);
        return node;
    }

    public static ParseNode make(Token token, ParseNode expression, ParseNode blockStatement) {
        IfStatementNode node = new IfStatementNode(token);
        node.appendChild(expression);
        node.appendChild(blockStatement);
        return node;
    }

    public void accept(ParseNodeVisitor visitor) {
        visitor.visitEnter(this);
        visitChildren(visitor);
        visitor.visitLeave(this);
    }
}
