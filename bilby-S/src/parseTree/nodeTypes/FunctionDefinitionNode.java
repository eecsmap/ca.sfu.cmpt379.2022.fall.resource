package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.Token;

public class FunctionDefinitionNode extends ParseNode {

    private String name;

    public FunctionDefinitionNode(Token token) {
        super(token);
    }

    ///////////////////////////////////////////////////////////
    // boilerplate for visitors
    public void accept(ParseNodeVisitor visitor) {
        visitor.visitEnter(this);
        visitChildren(visitor);
        visitor.visitLeave(this);
    }

    public static ParseNode withChildren(Token token, ParseNode type, ParseNode identifier, ParseNode parameterList,
            ParseNode blockStatement) {
        FunctionDefinitionNode node = new FunctionDefinitionNode(token);
        node.appendChild(type);
        node.appendChild(identifier);
        node.appendChild(parameterList);
        node.appendChild(blockStatement);
        node.name = ((IdentifierNode) identifier).getToken().getLexeme();
        return node;
    }

    public String getName() {
        return name;
    }

    public String getEpilogueLabel() {
        return "function-" + name + "-epilogue";
    }
}
