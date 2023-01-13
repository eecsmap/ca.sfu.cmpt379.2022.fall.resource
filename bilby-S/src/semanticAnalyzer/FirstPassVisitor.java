package semanticAnalyzer;

import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import parseTree.nodeTypes.FunctionDefinitionNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.ParameterListNode;
import parseTree.nodeTypes.ParameterNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.TypeNode;
import semanticAnalyzer.signatures.FunctionSignature;
import semanticAnalyzer.types.PrimitiveType;
import symbolTable.Scope;
import tokens.LextantToken;
import tokens.Token;

public class FirstPassVisitor extends ParseNodeVisitor.Default {

    @Override
    public void visitEnter(ProgramNode node) {
        enterProgramScope(node);
    }

    private void enterProgramScope(ParseNode node) {
        Scope scope = Scope.createProgramScope();
        node.setScope(scope);
    }

    @Override
    public void visitLeave(FunctionDefinitionNode node) {
        TypeNode type = (TypeNode) node.child(0);
        IdentifierNode identifier = (IdentifierNode) node.child(1);
        ParameterListNode parameterList = (ParameterListNode) node.child(2);
        FunctionSignature signature = FunctionSignature.make(type.getType(), parameterList.getTypes());
        node.setType(signature);
        identifier.setType(signature);
        SemanticAnalyzer.addBinding(identifier, signature);
    }

    @Override
    public void visitLeave(ParameterNode node) {
        TypeNode type = (TypeNode) node.child(0);
        node.setType(type.getType());
    }

    @Override
    public void visit(TypeNode node) {
        Token token = node.getToken();
        assert token instanceof LextantToken;
        LextantToken lextantToken = (LextantToken) node.getToken();
        Lextant lextant = lextantToken.getLextant();
        assert lextant instanceof Keyword;
        Keyword keyword = (Keyword) lextant;

        switch (keyword) {
        case BOOL:
            node.setType(PrimitiveType.BOOLEAN);
            break;
        case INT:
            node.setType(PrimitiveType.INTEGER);
            break;
        case FLOAT:
            node.setType(PrimitiveType.FLOAT);
            break;
        case CHAR:
            node.setType(PrimitiveType.CHAR);
            break;
        case STRING:
            node.setType(PrimitiveType.STRING);
            break;
        case VOID:
            node.setType(PrimitiveType.VOID);
            break;
        default:
            node.setType(PrimitiveType.ERROR);
        }
    }

}
