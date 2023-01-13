package asmCodeGenerator;

import java.util.HashMap;
import java.util.Map;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMOpcode;
import asmCodeGenerator.runtime.RunTime;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;
import parseTree.*;
import parseTree.nodeTypes.AssignmentStatementNode;
import parseTree.nodeTypes.BlockStatementNode;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.CallStatementNode;
import parseTree.nodeTypes.CastNode;
import parseTree.nodeTypes.CharConstantNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ExpressionListNode;
import parseTree.nodeTypes.FloatConstantNode;
import parseTree.nodeTypes.FunctionDefinitionNode;
import parseTree.nodeTypes.FunctionInvocationNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IfStatementNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.OperatorNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.ReturnStatementNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.TabNode;
import semanticAnalyzer.signatures.FunctionSignature;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import symbolTable.Binding;
import symbolTable.Scope;
import static asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType.*;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;

// do not call the code generator if any errors have occurred during analysis.
public class ASMCodeGenerator {
    ParseNode root;

    public static ASMCodeFragment generate(ParseNode syntaxTree) {
        ASMCodeGenerator codeGenerator = new ASMCodeGenerator(syntaxTree);
        return codeGenerator.makeASM();
    }

    public ASMCodeGenerator(ParseNode root) {
        super();
        this.root = root;
    }

    public ASMCodeFragment makeASM() {
        ASMCodeFragment code = new ASMCodeFragment(GENERATES_VOID);

        code.append(RunTime.getEnvironment());
        code.append(globalVariableBlockASM());
        code.append(programASM());
        // code.append( MemoryManager.codeForAfterApplication() );

        return code;
    }

    private ASMCodeFragment globalVariableBlockASM() {
        assert root.hasScope();
        Scope scope = root.getScope();
        int globalBlockSize = scope.getAllocatedSize();

        ASMCodeFragment code = new ASMCodeFragment(GENERATES_VOID);
        code.add(DLabel, RunTime.GLOBAL_MEMORY_BLOCK);
        code.add(DataZ, globalBlockSize);
        return code;
    }

    private ASMCodeFragment programASM() {
        ASMCodeFragment code = new ASMCodeFragment(GENERATES_VOID);

        code.add(Label, RunTime.MAIN_PROGRAM_LABEL);
        code.append(programCode());

        return code;
    }

    private ASMCodeFragment programCode() {
        ASMCodeFragment code = new ASMCodeFragment(GENERATES_VOID);
        CodeVisitor visitor = new CodeVisitor();
        root.accept(visitor);
        code.append(visitor.removeRootCode(root));
        code.add(Halt);
        code.append(visitor.functions);
        return code;
    }

    protected class CodeVisitor extends ParseNodeVisitor.Default {
        private Map<ParseNode, ASMCodeFragment> codeMap;
        ASMCodeFragment code;
        ASMCodeFragment functions;

        public CodeVisitor() {
            codeMap = new HashMap<ParseNode, ASMCodeFragment>();
            functions = new ASMCodeFragment(GENERATES_VOID);
        }

        ////////////////////////////////////////////////////////////////////
        // Make the field "code" refer to a new fragment of different sorts.
        private void newAddressCode(ParseNode node) {
            code = new ASMCodeFragment(GENERATES_ADDRESS);
            codeMap.put(node, code);
        }

        private void newValueCode(ParseNode node) {
            code = new ASMCodeFragment(GENERATES_VALUE);
            codeMap.put(node, code);
        }

        private void newVoidCode(ParseNode node) {
            code = new ASMCodeFragment(GENERATES_VOID);
            codeMap.put(node, code);
        }

        ////////////////////////////////////////////////////////////////////
        // Get code from the map.
        private ASMCodeFragment getAndRemoveCode(ParseNode node) {
            ASMCodeFragment result = codeMap.get(node);
            codeMap.remove(node);
            return result;
        }

        public ASMCodeFragment removeRootCode(ParseNode tree) {
            return getAndRemoveCode(tree);
        }

        ASMCodeFragment removeValueCode(ParseNode node) {
            ASMCodeFragment frag = getAndRemoveCode(node);
            makeFragmentValueCode(frag, node);
            return frag;
        }

        private ASMCodeFragment removeAddressCode(ParseNode node) {
            ASMCodeFragment frag = getAndRemoveCode(node);
            assert frag.isAddress();
            return frag;
        }

        ASMCodeFragment removeVoidCode(ParseNode node) {
            ASMCodeFragment frag = getAndRemoveCode(node);
            assert frag.isVoid();
            return frag;
        }

        ////////////////////////////////////////////////////////////////////
        // convert code to value-generating code.
        private void makeFragmentValueCode(ASMCodeFragment code, ParseNode node) {
            assert !code.isVoid();

            if (code.isAddress()) {
                turnAddressIntoValue(code, node);
            }
        }

        private void turnAddressIntoValue(ASMCodeFragment code, ParseNode node) {
            if (node.getType() == PrimitiveType.INTEGER) {
                code.add(LoadI);
            } else if (node.getType() == PrimitiveType.BOOLEAN) {
                code.add(LoadC);
            } else if (node.getType() == PrimitiveType.FLOAT) {
                code.add(LoadF);
            } else if (node.getType() == PrimitiveType.CHAR) {
                code.add(LoadC);
            } else if (node.getType() == PrimitiveType.STRING) {
                code.add(LoadI);
            } else {
                assert false : "node " + node;
            }
            code.markAsValue();
        }

        ////////////////////////////////////////////////////////////////////
        // ensures all types of ParseNode in given AST have at least a visitLeave
        public void visitLeave(ParseNode node) {
            assert false : "node " + node + " not handled in ASMCodeGenerator";
        }

        ///////////////////////////////////////////////////////////////////////////
        // constructs larger than statements
        public void visitLeave(ProgramNode node) {
            newVoidCode(node);
            for (ParseNode child : node.getChildren()) {
                ASMCodeFragment childCode = removeVoidCode(child);
                code.append(childCode);
            }
        }

        public void visitLeave(FunctionDefinitionNode node) {
            // part 1: generate the function code
            ASMCodeFragment functionCode = new ASMCodeFragment(GENERATES_VOID);
            functionCode.add(Label, node.getName());
            // [... ra]
            // prologue

            // dynamic link: mem[sp - 4] <= fp
            Macros.loadIFrom(functionCode, RunTime.STACK_POINTER);
            functionCode.add(PushI, 4);
            functionCode.add(Subtract);
            Macros.loadIFrom(functionCode, RunTime.FRAME_POINTER);
            functionCode.add(StoreI);

            // mem[sp - 8] <= return address
            Macros.loadIFrom(functionCode, RunTime.STACK_POINTER);
            functionCode.add(PushI, 8);
            functionCode.add(Subtract);
            // [... ra sp-8]
            functionCode.add(Exchange);
            functionCode.add(StoreI);

            // fp <= sp
            Macros.loadIFrom(functionCode, RunTime.STACK_POINTER);
            Macros.storeITo(functionCode, RunTime.FRAME_POINTER);

            // reserve space for dynamic link and return address, sp <= sp - 8
            Macros.loadIFrom(functionCode, RunTime.STACK_POINTER);
            functionCode.add(PushI, 8);
            functionCode.add(Subtract);
            // reserve space for local variables, sp <= sp - scopeSize
            BlockStatementNode block = (BlockStatementNode) node.child(3);
            functionCode.add(PushI, block.getScope().getAllocatedSize());
            functionCode.add(Subtract);
            Macros.storeITo(functionCode, RunTime.STACK_POINTER);

            functionCode.append(removeVoidCode(node.child(3))); // [... rv?]

            // epilogue
            functionCode.add(Label, node.getEpilogueLabel());

            // restore ra, push to asm stack the value mem[fp - 8]
            Macros.loadIFrom(functionCode, RunTime.FRAME_POINTER);
            functionCode.add(PushI, 8);
            functionCode.add(Subtract);
            functionCode.add(LoadI); // [... rv? ra]

            // restore sp <- fp
            Macros.loadIFrom(functionCode, RunTime.FRAME_POINTER);
            Macros.storeITo(functionCode, RunTime.STACK_POINTER);

            // restore fp
            Macros.loadIFrom(functionCode, RunTime.FRAME_POINTER);
            functionCode.add(PushI, 4);
            functionCode.add(Subtract);
            functionCode.add(LoadI); // [... ra fp']
            Macros.storeITo(functionCode, RunTime.FRAME_POINTER); // [... rv? ra]

            FunctionSignature signature = (FunctionSignature) node.getType();
            if (signature.resultType() != PrimitiveType.VOID) {
                functionCode.add(Exchange); // [... ra rv]
                // put the value on the call stack
                // sp <= sp - 4
                Macros.loadIFrom(functionCode, RunTime.STACK_POINTER);
                functionCode.add(PushI, 4);
                functionCode.add(Subtract);
                Macros.storeITo(functionCode, RunTime.STACK_POINTER);
                // mem[sp] <= rv
                Macros.loadIFrom(functionCode, RunTime.STACK_POINTER);
                functionCode.add(Exchange);
                functionCode.add(StoreI);
            }
            functionCode.add(Return);
            functions.append(functionCode);
            // part 2: set the function pointer
            newVoidCode(node);
            code.append(removeAddressCode(node.child(1)));
            code.add(PushD, node.getName());
            code.add(StoreI);
        }

        public void visitLeave(BlockStatementNode node) {
            newVoidCode(node);
            for (ParseNode child : node.getChildren()) {
                ASMCodeFragment childCode = removeVoidCode(child);
                code.append(childCode);
            }
        }

        ///////////////////////////////////////////////////////////////////////////
        // statements and declarations

        public void visitLeave(PrintStatementNode node) {
            newVoidCode(node);
            new PrintStatementGenerator(code, this).generate(node);
        }

        public void visit(NewlineNode node) {
            newVoidCode(node);
            code.add(PushD, RunTime.NEWLINE_PRINT_FORMAT);
            code.add(Printf);
        }

        public void visit(SpaceNode node) {
            newVoidCode(node);
            code.add(PushD, RunTime.SPACE_PRINT_FORMAT);
            code.add(Printf);
        }

        public void visit(TabNode node) {
            newVoidCode(node);
            code.add(PushD, RunTime.TAB_PRINT_FORMAT);
            code.add(Printf);
        }

        public void visitLeave(CallStatementNode node) {
            newVoidCode(node);
            FunctionInvocationNode functionInvocation = (FunctionInvocationNode) node.child(0);
            code.append(removeValueCode(functionInvocation));
            code.add(Pop);
        }

        public void visitLeave(IfStatementNode node) {
            newVoidCode(node);

            Labeller labeller = new Labeller("if");
            String elseLabel = labeller.newLabel("else");
            String endLabel = labeller.newLabel("end");
            boolean hasElseBlock = node.nChildren() == 3;

            code.append(removeValueCode(node.child(0)));
            code.add(JumpFalse, hasElseBlock ? elseLabel : endLabel);
            code.append(removeVoidCode(node.child(1)));
            code.add(Jump, endLabel);
            if (hasElseBlock) {
                code.add(Label, elseLabel);
                code.append(removeVoidCode(node.child(2)));
            }
            code.add(Label, endLabel);
        }

        public void visitLeave(ReturnStatementNode node) {
            newVoidCode(node);

            if (node.nChildren() == 1) {
                code.append(removeValueCode(node.child(0)));
            }

            FunctionDefinitionNode function = node.getFunctionDefinitionNode();
            assert function != null;
            code.add(Jump, function.getEpilogueLabel());
        }

        public void visitLeave(DeclarationNode node) {
            newVoidCode(node);
            ASMCodeFragment lvalue = removeAddressCode(node.child(0));
            ASMCodeFragment rvalue = removeValueCode(node.child(1));

            code.append(lvalue);
            code.append(rvalue);

            Type type = node.getType();
            code.add(opcodeForStore(type));
        }

        private ASMOpcode opcodeForStore(Type type) {
            if (type == PrimitiveType.INTEGER) {
                return StoreI;
            }
            if (type == PrimitiveType.BOOLEAN) {
                return StoreC;
            }
            if (type == PrimitiveType.FLOAT) {
                return StoreF;
            }
            if (type == PrimitiveType.CHAR) {
                return StoreC;
            }
            if (type == PrimitiveType.STRING) {
                return StoreI;
            }

            assert false : "Type " + type + " unimplemented in opcodeForStore()";
            return null;
        }

        @Override
        public void visitLeave(AssignmentStatementNode node) {
            newVoidCode(node);
            ASMCodeFragment lvalue = removeAddressCode(node.child(0));
            ASMCodeFragment rvalue = removeValueCode(node.child(1));
            code.append(lvalue);
            code.append(rvalue);
            code.add(opcodeForStore(node.child(0).getType()));
        }

        ///////////////////////////////////////////////////////////////////////////
        // expressions
        public void visitLeave(OperatorNode node) {
            Lextant operator = node.getOperator();

            if ((operator == Punctuator.ADD || operator == Punctuator.SUBTRACT) && node.nChildren() == 1) {
                visitUnaryOperatorNode(node);
            } else {
                visitNormalBinaryOperatorNode(node);
            }
        }

        private void visitUnaryOperatorNode(OperatorNode node) {
            newValueCode(node);
            ASMCodeFragment arg1 = removeValueCode(node.child(0));

            code.append(arg1);

            ASMOpcode opcode = opcodeForOperator(node);
            code.add(opcode);
        }

        private void visitNormalBinaryOperatorNode(OperatorNode node) {
            newValueCode(node);
            ASMCodeFragment arg1 = removeValueCode(node.child(0));
            ASMCodeFragment arg2 = removeValueCode(node.child(1));

            code.append(arg1);
            code.append(arg2);
            // handle division by zero
            if (node.getOperator() == Punctuator.DIVIDE) {
                Type divisorType = node.child(1).getType();
                assert divisorType == PrimitiveType.INTEGER || divisorType == PrimitiveType.FLOAT;
                Labeller labeller = new Labeller("divide");
                String startLabel = labeller.newLabel("start");
                String zeroLabel = labeller.newLabel("zero");
                String joinLabel = labeller.newLabel("join");
                code.add(Label, startLabel);
                code.add(Duplicate);
                code.add(divisorType == PrimitiveType.FLOAT ? JumpFZero : JumpFalse, zeroLabel);
                code.add(Jump, joinLabel);
                code.add(Label, zeroLabel);
                code.add(Jump, divisorType == PrimitiveType.FLOAT ? RunTime.FLOAT_DIVIDE_BY_ZERO_RUNTIME_ERROR
                        : RunTime.INTEGER_DIVIDE_BY_ZERO_RUNTIME_ERROR);
                code.add(Label, joinLabel);
            }

            ASMOpcode opcode = opcodeForOperator(node);
            code.add(opcode);

            if (node.getOperator() == Punctuator.EQUAL) {
                Type type = node.child(1).getType();
                Labeller labeller = new Labeller("equal");
                String trueLabel = labeller.newLabel("true");
                String falseLabel = labeller.newLabel("false");
                String joinLabel = labeller.newLabel("join");
                code.add(type == PrimitiveType.FLOAT ? JumpFZero : JumpFalse, trueLabel);
                code.add(Label, falseLabel);
                code.add(PushI, 0);
                code.add(Jump, joinLabel);
                code.add(Label, trueLabel);
                code.add(PushI, 1);
                code.add(Label, joinLabel);
            }
            if (node.getOperator() == Punctuator.NOTEQUAL) {
                Type type = node.child(1).getType();
                Labeller labeller = new Labeller("notequal");
                String trueLabel = labeller.newLabel("true");
                String falseLabel = labeller.newLabel("false");
                String joinLabel = labeller.newLabel("join");
                code.add(type == PrimitiveType.FLOAT ? JumpFZero : JumpFalse, falseLabel);
                code.add(Label, trueLabel);
                code.add(PushI, 1);
                code.add(Jump, joinLabel);
                code.add(Label, falseLabel);
                code.add(PushI, 0);
                code.add(Label, joinLabel);
            }
            if (node.getOperator() == Punctuator.LESS) {
                Type type = node.child(1).getType();
                Labeller labeller = new Labeller("less");
                String trueLabel = labeller.newLabel("true");
                String falseLabel = labeller.newLabel("false");
                String joinLabel = labeller.newLabel("join");
                code.add(type == PrimitiveType.FLOAT ? JumpFNeg : JumpNeg, trueLabel);
                code.add(Label, falseLabel);
                code.add(PushI, 0);
                code.add(Jump, joinLabel);
                code.add(Label, trueLabel);
                code.add(PushI, 1);
                code.add(Label, joinLabel);
            }
            if (node.getOperator() == Punctuator.GREATER) {
                Type type = node.child(1).getType();
                Labeller labeller = new Labeller("greater");
                String trueLabel = labeller.newLabel("true");
                String falseLabel = labeller.newLabel("false");
                String joinLabel = labeller.newLabel("join");
                code.add(type == PrimitiveType.FLOAT ? JumpFPos : JumpPos, trueLabel);
                code.add(Label, falseLabel);
                code.add(PushI, 0);
                code.add(Jump, joinLabel);
                code.add(Label, trueLabel);
                code.add(PushI, 1);
                code.add(Label, joinLabel);
            }
            if (node.getOperator() == Punctuator.LESSEQUAL) {
                Type type = node.child(1).getType();
                Labeller labeller = new Labeller("lessequal");
                String trueLabel = labeller.newLabel("true");
                String falseLabel = labeller.newLabel("false");
                String joinLabel = labeller.newLabel("join");
                code.add(type == PrimitiveType.FLOAT ? JumpFPos : JumpPos, falseLabel);
                code.add(Label, trueLabel);
                code.add(PushI, 1);
                code.add(Jump, joinLabel);
                code.add(Label, falseLabel);
                code.add(PushI, 0);
                code.add(Label, joinLabel);
            }
            if (node.getOperator() == Punctuator.GREATEREQUAL) {
                Type type = node.child(1).getType();
                Labeller labeller = new Labeller("greaterequal");
                String trueLabel = labeller.newLabel("true");
                String falseLabel = labeller.newLabel("false");
                String joinLabel = labeller.newLabel("join");
                code.add(type == PrimitiveType.FLOAT ? JumpFNeg : JumpNeg, falseLabel);
                code.add(Label, trueLabel);
                code.add(PushI, 1);
                code.add(Jump, joinLabel);
                code.add(Label, falseLabel);
                code.add(PushI, 0);
                code.add(Label, joinLabel);
            }
        }

        private ASMOpcode opcodeForOperator(OperatorNode node) {
            FunctionSignature signature = node.getSignature();
            if (signature != null) {
                return opcodeForOperator(signature);
            }
            assert false : "unimplemented operator in opcodeForOperator";
            return null;
        }

        private ASMOpcode opcodeForOperator(FunctionSignature signature) {
            Object variant = signature.getVariant();
            if (variant instanceof ASMOpcode) {
                return (ASMOpcode) variant;
            } else {
                assert false : "unimplemented operator in opcodeForOperator";
                return null;
            }
        }

        // identifier ( expression-list )
        @Override
        public void visitLeave(FunctionInvocationNode node) {

            // no matter what the return type is,
            // function invocation always returns a value
            newValueCode(node);

            // push arguments to call stack
            ExpressionListNode arguments = (ExpressionListNode) node.child(1);
            int sizeOfArguments = 0;
            // push arguments from right to left
            // ----------
            // | argN-1 |
            // | argN-2 |
            // | ... |
            // | arg1 |
            // | arg0 | <- sp
            // ----------
            for (int i = arguments.nChildren() - 1; i >= 0; i--) {
                Macros.loadIFrom(code, RunTime.STACK_POINTER); // [... sp]
                Type argumentType = arguments.child(i).getType();
                int argumentSize = argumentType.getSize();
                code.add(PushI, argumentSize); // [... sp size]
                code.add(Subtract); // [... sp - size]
                Macros.storeITo(code, RunTime.STACK_POINTER); // sp <= sp - size, [...]
                Macros.loadIFrom(code, RunTime.STACK_POINTER); // [... sp]
                code.append(removeValueCode(arguments.child(i))); // [... sp value]
                code.add(opcodeForStore(argumentType)); // [...], mem[sp] <= value
                sizeOfArguments += argumentSize;
            }

            // call function
            IdentifierNode function = (IdentifierNode) node.child(0);
            code.append(removeAddressCode(function));
            code.add(LoadI);
            code.add(CallV);

            // get return value from mem[sp] and put it on accumulator stack
            FunctionSignature signature = (FunctionSignature) function.getType();
            Type returnType = signature.resultType();
            if (returnType != PrimitiveType.VOID) {
                // [... rv] <- mem[sp]
                Macros.loadIFrom(code, RunTime.STACK_POINTER); // [... sp]
                code.add(opcodeForload(returnType)); // [... rv]
            } else {
                code.add(PushI, 0); // [... 0]
            }

            // clean up the stack, pull back the space for arguments and return value
            Macros.loadIFrom(code, RunTime.STACK_POINTER); // [... rv sp]
            code.add(PushI, returnType.getSize() + sizeOfArguments); // [... rv sp size]
            code.add(Add); // [... rv sp + size]
            Macros.storeITo(code, RunTime.STACK_POINTER); // sp <= sp + size, [... rv]
        }

        public ASMOpcode opcodeForload(Type returnType) {
            if (returnType == PrimitiveType.BOOLEAN) {
                return LoadC;
            }
            if (returnType == PrimitiveType.INTEGER) {
                return LoadI;
            }
            if (returnType == PrimitiveType.FLOAT) {
                return LoadF;
            }
            if (returnType == PrimitiveType.CHAR) {
                return LoadC;
            }
            if (returnType == PrimitiveType.STRING) {
                return LoadI;
            }

            assert false : "unimplemented type in loadForType";
            return null;
        }

        @Override
        public void visitLeave(CastNode node) {
            newValueCode(node);
            Type fromType = node.child(0).getType();
            Type toType = node.getType();
            code.append(removeValueCode(node.child(0)));
            if (fromType == toType) {
                return;
            }
            if ((fromType == PrimitiveType.INTEGER || fromType == PrimitiveType.CHAR)
                    && toType == PrimitiveType.FLOAT) {
                code.add(ConvertF);
            }
            if (fromType == PrimitiveType.FLOAT && (toType == PrimitiveType.INTEGER || toType == PrimitiveType.CHAR)) {
                code.add(ConvertI);
            }
            if (toType == PrimitiveType.BOOLEAN) {
                Labeller labeller = new Labeller("cast");
                String falseLabel = labeller.newLabel("false");
                String joinLabel = labeller.newLabel("join");
                code.add(fromType == PrimitiveType.FLOAT ? JumpFZero : JumpFalse, falseLabel);
                code.add(PushI, 1);
                code.add(Jump, joinLabel);
                code.add(Label, falseLabel);
                code.add(PushI, 0);
                code.add(Label, joinLabel);
            }
            if (toType == PrimitiveType.CHAR) {
                code.add(PushI, 0x7f);
                code.add(BTAnd);
            }
        }

        ///////////////////////////////////////////////////////////////////////////
        // leaf nodes (ErrorNode not necessary)
        public void visit(BooleanConstantNode node) {
            newValueCode(node);
            code.add(PushI, node.getValue() ? 1 : 0);
        }

        public void visit(IdentifierNode node) {
            newAddressCode(node);
            Binding binding = node.getBinding();

            binding.generateAddress(code);
        }

        public void visit(IntegerConstantNode node) {
            newValueCode(node);

            code.add(PushI, node.getValue());
        }

        public void visit(CharConstantNode node) {
            newValueCode(node);
            code.add(PushI, node.getValue());
        }

        public void visit(FloatConstantNode node) {
            newValueCode(node);
            code.add(PushF, node.getValue());
        }

        public void visit(StringConstantNode node) {
            newValueCode(node);
            String value = node.getValue();
            Labeller labeller = new Labeller("string-constant");
            String label = labeller.newLabel(value);
            code.add(DLabel, label);
            code.add(DataI, 3); // record type
            code.add(DataI, 9); // record status
            code.add(DataI, value.length()); // record length
            code.add(DataS, node.getValue());
            code.add(PushD, label);
        }
    }

}
