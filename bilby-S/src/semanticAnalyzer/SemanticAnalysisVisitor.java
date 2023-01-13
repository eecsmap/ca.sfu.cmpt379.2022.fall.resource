package semanticAnalyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import logging.BilbyLogger;
import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import parseTree.nodeTypes.AssignmentStatementNode;
import parseTree.nodeTypes.BlockStatementNode;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.CastNode;
import parseTree.nodeTypes.CharConstantNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ErrorNode;
import parseTree.nodeTypes.FloatConstantNode;
import parseTree.nodeTypes.FunctionDefinitionNode;
import parseTree.nodeTypes.FunctionInvocationNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.OperatorNode;
import parseTree.nodeTypes.ParameterNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.ReturnStatementNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.TypeNode;
import semanticAnalyzer.signatures.FunctionSignature;
import semanticAnalyzer.signatures.FunctionSignatures;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import symbolTable.Binding;
import symbolTable.Scope;
import tokens.LextantToken;
import tokens.Token;

class SemanticAnalysisVisitor extends ParseNodeVisitor.Default {
    @Override
    public void visitLeave(ParseNode node) {
        throw new RuntimeException("Node class unimplemented in SemanticAnalysisVisitor: " + node.getClass());
    }

    ///////////////////////////////////////////////////////////////////////////
    // constructs larger than statements
    public void visitLeave(ProgramNode node) {
        leaveScope(node);
    }

    public void visitEnter(BlockStatementNode node) {
        if (node.getParent() instanceof FunctionDefinitionNode) {
            Scope parameterScope = node.getLocalScope();
            node.setScope(parameterScope.createProcedureScope());
        } else {
            enterSubscope(node);
        }
    }

    public void visitLeave(BlockStatementNode node) {
        leaveScope(node);
    }

    ///////////////////////////////////////////////////////////////////////////
    // helper methods for scoping.
    private void enterSubscope(ParseNode node) {
        Scope baseScope = node.getLocalScope();
        Scope scope = baseScope.createSubscope();
        node.setScope(scope);
    }

    private void leaveScope(ParseNode node) {
        node.getScope().leave();
    }

    ///////////////////////////////////////////////////////////////////////////
    // statements and declarations
    @Override
    public void visitLeave(PrintStatementNode node) {
    }

    @Override
    public void visitLeave(DeclarationNode node) {
        IdentifierNode identifier = (IdentifierNode) node.child(0);
        ParseNode initializer = node.child(1);

        Type declarationType = initializer.getType();
        node.setType(declarationType);
        if (declarationType == PrimitiveType.ERROR) {
            logError("Cannot declare variable of type ERROR");
            return;
        }
        identifier.setType(declarationType);
        identifier.setMutable(node.getToken().isLextant(Keyword.MUT));
        SemanticAnalyzer.addBinding(identifier, declarationType);
    }

    @Override
    public void visitEnter(FunctionDefinitionNode node) {
        Scope scope = Scope.createParameterScope();
        node.setScope(scope);
    }

    @Override
    public void visitLeave(FunctionDefinitionNode node) {
        leaveScope(node);
    }

    @Override
    public void visitLeave(ParameterNode node) {
        TypeNode type = (TypeNode) node.child(0);
        IdentifierNode identifier = (IdentifierNode) node.child(1);
        identifier.setType(type.getType());
        // identifier.setMutable(true);
        SemanticAnalyzer.addBinding(identifier, type.getType());
    }

    @Override
    public void visitLeave(AssignmentStatementNode node) {
        IdentifierNode identifier = (IdentifierNode) node.child(0);
        if (!identifier.isMutable()) {
            BilbyLogger logger = BilbyLogger.getLogger("compiler.semanticAnalyzer");
            logger.severe("Cannot assign to immutable variable " + identifier.getToken().getLexeme());
            return;
        }
        ParseNode expression = node.child(1);
        Type identifierType = identifier.getType();
        Type expressionType = expression.getType();
        if (!identifierType.equals(expressionType)) {
            List<Type> types = new ArrayList<Type>();
            types.add(identifierType);
            types.add(expressionType);
            typeCheckError(node, types);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // expressions
    @Override
    public void visitLeave(OperatorNode node) {
        List<Type> childTypes;
        if (node.nChildren() == 1) {
            ParseNode child = node.child(0);
            childTypes = Arrays.asList(child.getType());
        } else {
            assert node.nChildren() == 2;
            ParseNode left = node.child(0);
            ParseNode right = node.child(1);

            childTypes = Arrays.asList(left.getType(), right.getType());
        }

        Lextant operator = operatorFor(node);
        FunctionSignature signature = FunctionSignatures.signature(operator, childTypes);

        if (signature.accepts(childTypes)) {
            node.setSignature(signature);
            node.setType(signature.resultType());
        } else {
            typeCheckError(node, childTypes);
            node.setType(PrimitiveType.ERROR);
        }
    }

    private Lextant operatorFor(OperatorNode node) {
        LextantToken token = (LextantToken) node.getToken();
        return token.getLextant();
    }

    @Override
    public void visitLeave(FunctionInvocationNode node) {
        IdentifierNode identifier = (IdentifierNode) node.child(0);
        Binding binding = identifier.getBinding();
        Type type = binding.getType();
        if (type == PrimitiveType.ERROR) {
            node.setType(PrimitiveType.ERROR);
            return;
        }
        assert type instanceof FunctionSignature;
        FunctionSignature signature = (FunctionSignature) type;
        node.setType(signature.resultType());
        // List<Type> childTypes = signature.parameterTypes();
        // if(signature.accepts(childTypes)) {
        // node.setType(signature.resultType());
        // } else {
        // typeCheckError(node, childTypes);
        // node.setType(PrimitiveType.ERROR);
        // }
    }

    public void visitLeave(ReturnStatementNode node) {
        FunctionDefinitionNode function = node.getFunctionDefinitionNode();
        if (function == null) {
            logError("return statement outside of function");
            return;
        }
        if (node.nChildren() == 1) {
            ParseNode expression = node.child(0);
            if (expression.getType() != ((FunctionSignature) function.getType()).resultType()) {
                logError("return type does not match function type");
            }
        } else {
            assert node.nChildren() == 0;
            if (((FunctionSignature) function.getType()).resultType() != PrimitiveType.VOID) {
                logError("return type does not match function type");
            }
        }
    }

    @Override
    public void visitLeave(CastNode node) {
        ParseNode expression = node.child(0);
        TypeNode typeNode = (TypeNode) node.child(1);
        Type targetType = typeNode.getType();
        Type expressionType = expression.getType();
        if (targetType == PrimitiveType.ERROR || expressionType == PrimitiveType.ERROR) {
            node.setType(PrimitiveType.ERROR);
            return;
        }
        if (expressionType == targetType) {
            node.setType(targetType);
            return;
        }
        if (expressionType == PrimitiveType.BOOLEAN && targetType != PrimitiveType.BOOLEAN) {
            logError("cannot cast boolean to non-boolean type");
            node.setType(PrimitiveType.ERROR);
            return;
        }
        if ((expressionType == PrimitiveType.CHAR && targetType == PrimitiveType.INTEGER)
                || (expressionType == PrimitiveType.INTEGER && targetType == PrimitiveType.CHAR)
                || (expressionType == PrimitiveType.INTEGER && targetType == PrimitiveType.FLOAT)
                || (expressionType == PrimitiveType.FLOAT && targetType == PrimitiveType.INTEGER)
                || (expressionType == PrimitiveType.INTEGER && targetType == PrimitiveType.BOOLEAN)
                || (expressionType == PrimitiveType.CHAR && targetType == PrimitiveType.BOOLEAN)) {
            node.setType(targetType);
            return;
        }
        logError("cannot cast " + expressionType + " to " + targetType);
        node.setType(PrimitiveType.ERROR);
    }

    ///////////////////////////////////////////////////////////////////////////
    // simple leaf nodes
    @Override
    public void visit(BooleanConstantNode node) {
        node.setType(PrimitiveType.BOOLEAN);
    }

    @Override
    public void visit(ErrorNode node) {
        node.setType(PrimitiveType.ERROR);
    }

    @Override
    public void visit(IntegerConstantNode node) {
        node.setType(PrimitiveType.INTEGER);
    }

    @Override
    public void visit(CharConstantNode node) {
        node.setType(PrimitiveType.CHAR);
    }

    @Override
    public void visit(FloatConstantNode node) {
        node.setType(PrimitiveType.FLOAT);
    }

    @Override
    public void visit(StringConstantNode node) {
        node.setType(PrimitiveType.STRING);
    }

    @Override
    public void visit(NewlineNode node) {
    }

    @Override
    public void visit(SpaceNode node) {
    }

    @Override
    public void visit(TypeNode node) {
    }

    ///////////////////////////////////////////////////////////////////////////
    // IdentifierNodes, with helper methods
    @Override
    public void visit(IdentifierNode node) {
        if (!isBeingDeclared(node)) {
            Binding binding = node.findVariableBinding();

            node.setType(binding.getType());
            node.setBinding(binding);
            node.setMutable(binding.isMutable());
        }
        // else parent DeclarationNode does the processing.
    }

    private boolean isBeingDeclared(IdentifierNode node) {
        ParseNode parent = node.getParent();
        return (parent instanceof DeclarationNode) && (node == parent.child(0))
                || (parent instanceof FunctionDefinitionNode) && (node == parent.child(1))
                || (parent instanceof ParameterNode) && (node == parent.child(1));
    }

    ///////////////////////////////////////////////////////////////////////////
    // error logging/printing

    private void typeCheckError(ParseNode node, List<Type> operandTypes) {
        Token token = node.getToken();

        logError("operator " + token.getLexeme() + " not defined for types " + operandTypes + " at "
                + token.getLocation());
    }

    private void logError(String message) {
        BilbyLogger log = BilbyLogger.getLogger("compiler.semanticAnalyzer");
        log.severe(message);
    }
}