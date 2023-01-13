package parseTree;

import parseTree.nodeTypes.AssignmentStatementNode;
import parseTree.nodeTypes.BlockStatementNode;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.CallStatementNode;
import parseTree.nodeTypes.CastNode;
import parseTree.nodeTypes.CharConstantNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ErrorNode;
import parseTree.nodeTypes.ExpressionListNode;
import parseTree.nodeTypes.FloatConstantNode;
import parseTree.nodeTypes.FunctionDefinitionNode;
import parseTree.nodeTypes.FunctionInvocationNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IfStatementNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.OperatorNode;
import parseTree.nodeTypes.ParameterListNode;
import parseTree.nodeTypes.ParameterNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.ReturnStatementNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.TabNode;
import parseTree.nodeTypes.TypeNode;

// Visitor pattern with pre- and post-order visits
public interface ParseNodeVisitor {

    // non-leaf nodes: visitEnter and visitLeave
    void visitEnter(OperatorNode node);

    void visitLeave(OperatorNode node);

    void visitEnter(DeclarationNode node);

    void visitLeave(DeclarationNode node);

    void visitEnter(ParseNode node);

    void visitLeave(ParseNode node);

    void visitEnter(PrintStatementNode node);

    void visitLeave(PrintStatementNode node);

    void visitEnter(ProgramNode node);

    void visitLeave(ProgramNode node);

    void visitEnter(ParameterListNode node);

    void visitLeave(ParameterListNode node);

    void visitEnter(ParameterNode node);

    void visitLeave(ParameterNode node);

    void visitEnter(BlockStatementNode node);

    void visitLeave(BlockStatementNode node);

    void visitEnter(FunctionDefinitionNode node);

    void visitLeave(FunctionDefinitionNode node);

    void visitEnter(ExpressionListNode node);

    void visitLeave(ExpressionListNode node);

    void visitEnter(CallStatementNode node);

    void visitLeave(CallStatementNode node);

    void visitEnter(ReturnStatementNode node);

    void visitLeave(ReturnStatementNode node);

    void visitEnter(IfStatementNode node);

    void visitLeave(IfStatementNode node);

    void visitEnter(FunctionInvocationNode node);

    void visitLeave(FunctionInvocationNode node);

    void visitEnter(AssignmentStatementNode node);

    void visitLeave(AssignmentStatementNode node);

    void visitEnter(CastNode node);

    void visitLeave(CastNode node);

    // leaf nodes: visitLeaf only
    void visit(BooleanConstantNode node);

    void visit(ErrorNode node);

    void visit(IdentifierNode node);

    void visit(IntegerConstantNode node);

    void visit(NewlineNode node);

    void visit(SpaceNode node);

    void visit(TabNode node);

    void visit(TypeNode node);

    void visit(CharConstantNode node);

    void visit(FloatConstantNode node);

    void visit(StringConstantNode node);

    public static class Default implements ParseNodeVisitor {
        public void defaultVisit(ParseNode node) {
        }

        public void defaultVisitEnter(ParseNode node) {
            defaultVisit(node);
        }

        public void defaultVisitLeave(ParseNode node) {
            defaultVisit(node);
        }

        public void defaultVisitForLeaf(ParseNode node) {
            defaultVisit(node);
        }

        public void visitEnter(OperatorNode node) {
            defaultVisitEnter(node);
        }

        public void visitLeave(OperatorNode node) {
            defaultVisitLeave(node);
        }

        public void visitEnter(DeclarationNode node) {
            defaultVisitEnter(node);
        }

        public void visitLeave(DeclarationNode node) {
            defaultVisitLeave(node);
        }

        public void visitEnter(ParseNode node) {
            defaultVisitEnter(node);
        }

        public void visitLeave(ParseNode node) {
            defaultVisitLeave(node);
        }

        public void visitEnter(PrintStatementNode node) {
            defaultVisitEnter(node);
        }

        public void visitLeave(PrintStatementNode node) {
            defaultVisitLeave(node);
        }

        public void visitEnter(ProgramNode node) {
            defaultVisitEnter(node);
        }

        public void visitLeave(ProgramNode node) {
            defaultVisitLeave(node);
        }

        @Override
        public void visitEnter(ParameterListNode node) {
            defaultVisitEnter(node);
        }

        @Override
        public void visitLeave(ParameterListNode node) {
            defaultVisitLeave(node);
        }

        @Override
        public void visitEnter(ParameterNode node) {
            defaultVisitEnter(node);
        }

        @Override
        public void visitLeave(ParameterNode node) {
            defaultVisitLeave(node);
        }

        @Override
        public void visitEnter(BlockStatementNode node) {
            defaultVisitEnter(node);
        }

        @Override
        public void visitLeave(BlockStatementNode node) {
            defaultVisitLeave(node);
        }

        @Override
        public void visitEnter(FunctionDefinitionNode node) {
            defaultVisitEnter(node);
        }

        @Override
        public void visitLeave(FunctionDefinitionNode node) {
            defaultVisitLeave(node);
        }

        @Override
        public void visitEnter(ExpressionListNode node) {
            defaultVisitEnter(node);
        }

        @Override
        public void visitLeave(ExpressionListNode node) {
            defaultVisitLeave(node);
        }

        @Override
        public void visitEnter(CallStatementNode node) {
            defaultVisitEnter(node);
        }

        @Override
        public void visitLeave(CallStatementNode node) {
            defaultVisitLeave(node);
        }

        @Override
        public void visitEnter(ReturnStatementNode node) {
            defaultVisitEnter(node);
        }

        @Override
        public void visitLeave(ReturnStatementNode node) {
            defaultVisitLeave(node);
        }

        @Override
        public void visitEnter(IfStatementNode node) {
            defaultVisitEnter(node);
        }

        @Override
        public void visitLeave(IfStatementNode node) {
            defaultVisitLeave(node);
        }

        @Override
        public void visitEnter(FunctionInvocationNode node) {
            defaultVisitEnter(node);
        }

        @Override
        public void visitLeave(FunctionInvocationNode node) {
            defaultVisitLeave(node);
        }

        @Override
        public void visitEnter(AssignmentStatementNode node) {
            defaultVisitEnter(node);
        }

        @Override
        public void visitLeave(AssignmentStatementNode node) {
            defaultVisitLeave(node);
        }

        @Override
        public void visitEnter(CastNode node) {
            defaultVisitEnter(node);
        }

        @Override
        public void visitLeave(CastNode node) {
            defaultVisitLeave(node);
        }

        public void visit(BooleanConstantNode node) {
            defaultVisitForLeaf(node);
        }

        public void visit(ErrorNode node) {
            defaultVisitForLeaf(node);
        }

        public void visit(IdentifierNode node) {
            defaultVisitForLeaf(node);
        }

        public void visit(IntegerConstantNode node) {
            defaultVisitForLeaf(node);
        }

        public void visit(NewlineNode node) {
            defaultVisitForLeaf(node);
        }

        public void visit(SpaceNode node) {
            defaultVisitForLeaf(node);
        }

        @Override
        public void visit(TabNode node) {
            defaultVisitForLeaf(node);
        }

        @Override
        public void visit(TypeNode node) {
            defaultVisitForLeaf(node);
        }

        @Override
        public void visit(CharConstantNode node) {
            defaultVisitForLeaf(node);
        }

        @Override
        public void visit(FloatConstantNode node) {
            defaultVisitForLeaf(node);
        }

        @Override
        public void visit(StringConstantNode node) {
            defaultVisitForLeaf(node);
        }
    }
}
