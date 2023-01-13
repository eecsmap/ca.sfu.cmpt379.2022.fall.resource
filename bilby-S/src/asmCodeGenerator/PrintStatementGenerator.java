package asmCodeGenerator;

import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import parseTree.ParseNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.TabNode;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import asmCodeGenerator.ASMCodeGenerator.CodeVisitor;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.runtime.RunTime;

public class PrintStatementGenerator {
    ASMCodeFragment code;
    ASMCodeGenerator.CodeVisitor visitor;

    public PrintStatementGenerator(ASMCodeFragment code, CodeVisitor visitor) {
        super();
        this.code = code;
        this.visitor = visitor;
    }

    public void generate(PrintStatementNode node) {
        for (ParseNode child : node.getChildren()) {
            if (child instanceof NewlineNode || child instanceof SpaceNode || child instanceof TabNode) {
                ASMCodeFragment childCode = visitor.removeVoidCode(child);
                code.append(childCode);
            } else {
                appendPrintCode(child);
            }
        }
    }

    private void appendPrintCode(ParseNode node) {
        String format = printFormat(node.getType());

        code.append(visitor.removeValueCode(node));
        convertToStringIfBoolean(node);
        getValueIfString(node);
        code.add(PushD, format);
        code.add(Printf);
    }

    private void getValueIfString(ParseNode node) {
        if (node.getType() != PrimitiveType.STRING) {
            return;
        }
        code.add(PushI, 12); // value offset in string record
        code.add(Add);
    }

    private void convertToStringIfBoolean(ParseNode node) {
        if (node.getType() != PrimitiveType.BOOLEAN) {
            return;
        }

        Labeller labeller = new Labeller("print-boolean");
        String trueLabel = labeller.newLabel("true");
        String endLabel = labeller.newLabel("join");

        code.add(JumpTrue, trueLabel);
        code.add(PushD, RunTime.BOOLEAN_FALSE_STRING);
        code.add(Jump, endLabel);
        code.add(Label, trueLabel);
        code.add(PushD, RunTime.BOOLEAN_TRUE_STRING);
        code.add(Label, endLabel);
    }

    private static String printFormat(Type type) {
        assert type instanceof PrimitiveType;

        switch ((PrimitiveType) type) {
        case INTEGER:
            return RunTime.INTEGER_PRINT_FORMAT;
        case BOOLEAN:
            return RunTime.BOOLEAN_PRINT_FORMAT;
        case CHAR:
            return RunTime.CHAR_PRINT_FORMAT;
        case FLOAT:
            return RunTime.FLOAT_PRINT_FORMAT;
        case STRING:
            return RunTime.STRING_PRINT_FORMAT;
        default:
            assert false : "Type " + type + " unimplemented in PrintStatementGenerator.printFormat()";
            return "";
        }
    }
}
