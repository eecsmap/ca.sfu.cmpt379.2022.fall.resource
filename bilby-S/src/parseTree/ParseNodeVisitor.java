package parseTree;

import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.MainBlockNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ErrorNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.OperatorNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.SpaceNode;

// Visitor pattern with pre- and post-order visits
public interface ParseNodeVisitor {

    // non-leaf nodes: visitEnter and visitLeave
    void visitEnter(OperatorNode node);

    void visitLeave(OperatorNode node);

    void visitEnter(MainBlockNode node);

    void visitLeave(MainBlockNode node);

    void visitEnter(DeclarationNode node);

    void visitLeave(DeclarationNode node);

    void visitEnter(ParseNode node);

    void visitLeave(ParseNode node);

    void visitEnter(PrintStatementNode node);

    void visitLeave(PrintStatementNode node);

    void visitEnter(ProgramNode node);

    void visitLeave(ProgramNode node);

    // leaf nodes: visitLeaf only
    void visit(BooleanConstantNode node);

    void visit(ErrorNode node);

    void visit(IdentifierNode node);

    void visit(IntegerConstantNode node);

    void visit(NewlineNode node);

    void visit(SpaceNode node);

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

        public void visitEnter(MainBlockNode node) {
            defaultVisitEnter(node);
        }

        public void visitLeave(MainBlockNode node) {
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
    }
}
