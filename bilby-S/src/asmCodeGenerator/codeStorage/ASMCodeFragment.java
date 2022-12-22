package asmCodeGenerator.codeStorage;

import inputHandler.InputHandler;

import java.util.ArrayList;
import java.util.List;

import logging.BilbyLogger;

public class ASMCodeFragment {
	private List<ASMCodeChunk> chunks;
	
	// these names refer to what the code in the fragment
	// leaves on top of the accumulator: either nothing, a value
	// (like 5 or 17.4) or an address where the "result" can
	// be found.
	public enum CodeType {
		GENERATES_VOID,
		GENERATES_VALUE,
		GENERATES_ADDRESS;
	}
	CodeType codeType;
	
	public ASMCodeFragment(CodeType codeType) {
		chunks = new ArrayList<ASMCodeChunk>();
		this.codeType = codeType;
	}
	public ASMCodeFragment(ASMCodeFragment source) {
		chunks = new ArrayList<ASMCodeChunk>();
		for(ASMCodeChunk chunk: source.chunks) {
			ASMCodeChunk chunkCopy = new ASMCodeChunk(chunk);
			chunks.add(chunkCopy);
		}
		codeType = source.codeType;
	}
	
	public void markAsVoid() {
		codeType = CodeType.GENERATES_VOID;
	}
	public void markAsValue() {
		codeType = CodeType.GENERATES_VALUE;
	}
	public void markAsAddress() {
		codeType = CodeType.GENERATES_ADDRESS;
	}
	public boolean isAddress() {
		return codeType == CodeType.GENERATES_ADDRESS;
	}
	public boolean isValue() {
		return codeType == CodeType.GENERATES_VALUE;
	}
	public boolean isVoid() {
		return codeType == CodeType.GENERATES_VOID;
	}
	
	/** Append all instructions in the argument to this code fragment.
	 *  This does not change the type of this code fragment; you must
	 *  call markAsXXX afterwards if you need that to happen.
	 * @param fragment
	 */
	public void append(ASMCodeFragment fragment) {
		chunks.addAll(fragment.chunks);
	}
	
	public void add(ASMOpcode opcode, int operand, String comment) {
		lastChunk().add(opcode, operand, comment);
	}
	public void add(ASMOpcode opcode, int operand) {
		lastChunk().add(opcode, operand);
	}
	public void add(ASMOpcode opcode, double operand, String comment) {
		lastChunk().add(opcode, operand, comment);
	}
	public void add(ASMOpcode opcode, double operand) {
		lastChunk().add(opcode, operand);
	}
	public void add(ASMOpcode opcode, String operand, String comment) {
		lastChunk().add(opcode, operand, comment);
	}
	public void add(ASMOpcode opcode, String operand) {
		lastChunk().add(opcode, operand);
	}
	public void add(ASMOpcode opcode) {
		lastChunk().add(opcode);
	}

	private ASMCodeChunk lastChunk() {
		if(chunks.size() == 0) {
			newChunk();
		}
		return chunks.get(chunks.size() - 1);
	}

	private void newChunk() {
		ASMCodeChunk chunk = new ASMCodeChunk();
		chunks.add(chunk);
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for(ASMCodeChunk chunk: chunks) {
			buffer.append(chunk.toString());
		}
		return buffer.toString();
	}

	public static ASMCodeFragment readFrom(String filename) {
		ASMCodeFragment result = new ASMCodeFragment(CodeType.GENERATES_VOID);
		try {
			read(filename, result);
		}
		catch (IllegalArgumentException | InstructionReadingException e) {
			error("problem reading " + filename);
		}
		
		return result;
	}

	private static void read(String filename, ASMCodeFragment result) {
		InputHandler handler = InputHandler.fromFilename(filename);
		for(String line: handler) {
			readInstruction(line, result);
		}
	}

	private static void readInstruction(String line, ASMCodeFragment result) {
		if(line.charAt(0) == '#') {			// will lose all full-line comments
			return;
		}
		
		String[] words = line.trim().split("\\s+");
		ASMOpcode opcode = readOpcode(words[0]);
		
		if(opcode.takesFloat()) {
			double d = Double.parseDouble(words[1]);
			String comment = commentStartingAtIndex(2, words);
			result.add(opcode, d, comment);
		}
		else if (opcode.takesInteger()) {
			int i = Integer.parseInt(words[1]);
			String comment = commentStartingAtIndex(2, words);
			result.add(opcode, i, comment);
		}
		else if (opcode.takesString()) {
			String s = words[1];
			String comment = commentStartingAtIndex(2, words);
			result.add(opcode, s, comment);
		}
		else {
			String comment = commentStartingAtIndex(1, words);
			result.add(opcode, "", comment);
		}
	}

	private static String commentStartingAtIndex(int i, String[] words) {
		if(words.length <= i) {
			return "";
		}
		
		String comment = words[i];
		
		for(int index=i+1; index < words.length; index++) {
			comment = comment + " " + words[index];
		}
		return comment;
	}

	private static ASMOpcode readOpcode(String word) {
		try {
			return ASMOpcode.valueOf(word);
		}
		catch (IllegalArgumentException | NullPointerException e) {
			throw new InstructionReadingException("opcode " + word, e);
		}
	}
	
	private static class InstructionReadingException extends RuntimeException {
		private static final long serialVersionUID = -6900237919002204679L;

		public InstructionReadingException(String s, RuntimeException e) {
			super(s, e);
		}
	}
	private static void error(String message) {
		BilbyLogger log = BilbyLogger.getLogger("compiler.Optimizer");
		log.severe("read error: " + message);
	}	
}
