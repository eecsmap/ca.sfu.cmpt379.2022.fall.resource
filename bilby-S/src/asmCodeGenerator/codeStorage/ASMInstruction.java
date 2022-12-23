package asmCodeGenerator.codeStorage;


public class ASMInstruction {
	private ASMOpcode opcode;
	private Object argument;
	private String comment;
	
	
/////////////////////////////////////////////////////////////////////
// constructors
	
	public ASMInstruction(ASMInstruction source) {
		this.opcode   = source.opcode;
		this.argument = source.argument;
		this.comment  = source.comment;
	}
	public ASMInstruction(ASMOpcode opcode, int argument) {
		this(opcode, argument, "");
	}
	public ASMInstruction(ASMOpcode opcode, int argument, String comment) {
		assert opcode.takesInteger() : opcode.toString();
		this.opcode = opcode;
		this.argument = argument;
		this.comment = comment;
	}
	public ASMInstruction(ASMOpcode opcode, double argument) {
		this(opcode, argument, "");
	}
	public ASMInstruction(ASMOpcode opcode, double argument, String comment) {
		assert opcode.takesFloat() : opcode.toString();
		this.opcode = opcode;
		this.argument = argument;
		this.comment = comment;
	}
	public ASMInstruction(ASMOpcode opcode, String argument) {
		this(opcode, argument, "");
	}
	public ASMInstruction(ASMOpcode opcode, String argument, String comment) {
		assert (nullOrEmpty(argument) || opcode.takesString()) : opcode.toString();
		this.opcode = opcode;
		this.argument = argument;
		this.comment = comment;
	}
	private boolean nullOrEmpty(String argument) {
		return argument == null || argument.length()==0;
	}
	// no commented version...use new ASMInstruction(opcode, "", comment) instead.
	public ASMInstruction(ASMOpcode opcode) {
		this.opcode = opcode;
		this.argument = null;
		this.comment = "";
	}

	
/////////////////////////////////////////////////////////////////////////
// toString ... particular attention paid to DataS and Comment instructions,
//              which the emulator doesn't handle.
	
	static private String indentation = "        ";
	public String toString() {
		if(opcode == ASMOpcode.DataS) {
			return DataStoString();
		}
		if(opcode == ASMOpcode.Comment) {
			return CommentToString();
		}
		String result = indentation;	// indentation(8);
		result += opcodeString();
		result += argumentString();
		if(comment != null)
			result += " " + comment;
		return result;
	}
	
	private String CommentToString() {
		String result = "#";
		if(argument instanceof String) {
			result += (String)argument;
		}
		if(comment != null) {
			result += (String)comment;
		}
		return result;
	}


	static String terminator = System.getProperty("line.separator");
	private String DataStoString() {
		String string = (String)this.argument;
		
		if(string.length() == 0) {
			return NullDataStoString();
		}
		
		if(comment == null || comment == "") {
			comment = DataSComment();
		}
		String result = dataCString((int)string.charAt(0), comment) + terminator;
		
		for(int i=1; i<string.length(); i++) {
			result += dataCString((int)string.charAt(i), "");
			result += terminator;
		}
		
		result += dataCString(0, "");	// no terminator
		return result;
	}
	private String DataSComment() {
		return "%% \"" + printable((String)argument) + "\"";
	}
	private String printable(String string) {
		StringBuilder sb = new StringBuilder();
		
		for(int i=0 ; i<string.length(); i++) {
			char c = string.charAt(i);
			sb.append(printable(c));
		}
		
		return sb.toString();
	}
	private String printable(char c) {
		switch(c) {
		case '\n': return "\\n";
		case '\r': return "\\r";
		case '\t': return "\\t";
		case '\b': return "\\b";
		case '\f': return "\\f";
		default:   return String.valueOf(c);
		}
	}
	private String NullDataStoString() {
		return dataCString(0, this.comment);
	}
	private String dataCString(int charAt, String comment) {
		ASMInstruction instruction = new ASMInstruction(ASMOpcode.DataC, charAt, comment);
		return instruction.toString();
	}
	
	private String opcodeString() {
		return String.format("%-12s ", opcode.toString());
	}
	
	private String argumentString() {
		if(opcode.takesFloat()) {
			return String.format("%-25f", (Double)argument);
		}
		
		if(opcode.takesInteger()) {
			return String.format("%-25d", (Integer)argument);
		}
		
		if(opcode.takesString()) {
			return String.format("%-25s", (String)argument);
		}
		return String.format("%-25s", "");		
	}
}
