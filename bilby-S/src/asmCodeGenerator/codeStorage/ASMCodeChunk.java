package asmCodeGenerator.codeStorage;

import java.util.LinkedList;
import java.util.List;

// a glorified list of instructions.
public class ASMCodeChunk {
    List<ASMInstruction> instructions;

    public ASMCodeChunk() {
        instructions = new LinkedList<ASMInstruction>();
    }

    public ASMCodeChunk(ASMCodeChunk source) {
        instructions = new LinkedList<ASMInstruction>();
        for (ASMInstruction instruction : source.instructions) {
            ASMInstruction instructionCopy = new ASMInstruction(instruction);
            instructions.add(instructionCopy);
        }
    }

    public void add(ASMOpcode opcode, int operand, String comment) {
        ASMInstruction instruction = new ASMInstruction(opcode, operand, comment);
        instructions.add(instruction);
    }

    public void add(ASMOpcode opcode, int operand) {
        ASMInstruction instruction = new ASMInstruction(opcode, operand);
        instructions.add(instruction);
    }

    public void add(ASMOpcode opcode, double operand, String comment) {
        ASMInstruction instruction = new ASMInstruction(opcode, operand, comment);
        instructions.add(instruction);
    }

    public void add(ASMOpcode opcode, double operand) {
        ASMInstruction instruction = new ASMInstruction(opcode, operand);
        instructions.add(instruction);
    }

    public void add(ASMOpcode opcode, String operand, String comment) {
        ASMInstruction instruction = new ASMInstruction(opcode, operand, comment);
        instructions.add(instruction);
    }

    public void add(ASMOpcode opcode, String operand) {
        ASMInstruction instruction = new ASMInstruction(opcode, operand);
        instructions.add(instruction);
    }

    public void add(ASMOpcode opcode) {
        ASMInstruction instruction = new ASMInstruction(opcode);
        instructions.add(instruction);
    }

    static private String terminator = System.getProperty("line.separator");

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (ASMInstruction instruction : instructions) {
            buffer.append(instruction.toString());
            buffer.append(terminator);
        }
        return buffer.toString();
    }
}
