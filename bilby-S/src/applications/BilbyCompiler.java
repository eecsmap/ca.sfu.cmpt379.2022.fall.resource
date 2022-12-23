package applications;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import asmCodeGenerator.ASMCodeGenerator;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import lexicalAnalyzer.LexicalAnalyzer;
import lexicalAnalyzer.Scanner;
import parseTree.ParseNode;
import parser.Parser;
import semanticAnalyzer.SemanticAnalyzer;
import tokens.Tokens;

public class BilbyCompiler extends BilbyApplication {
	/** Compiles a bilby file.
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		checkArguments(args, className());
		
		Tokens.setPrintLevel(Tokens.Level.FULL);
		compile(args[0]);
		System.exit(0);
	}
	
	/** analyzes a file specified by filename.
	 * @param filename the name of the file to be analyzed.
	 * @throws FileNotFoundException 
	 */
	public static void compile(String filename) throws FileNotFoundException {
		Scanner scanner         = LexicalAnalyzer.make(filename);
		ParseNode syntaxTree    = Parser.parse(scanner);
		ParseNode decoratedTree = SemanticAnalyzer.analyze(syntaxTree);

		generateCodeIfNoErrors(filename, decoratedTree);
	}

	private static void generateCodeIfNoErrors(String filename, ParseNode decoratedTree)
			throws FileNotFoundException {
		String outfile = outputFilename(filename);
		
		if(thereAreErrors()) {
			stopProcessing(outfile);
			System.exit(1);
		} 
		else {
			generateAndPrintCode(outfile, decoratedTree);
		}
	}

	// stopProcessing -- inform user and clean up.
	private static void stopProcessing(String outfile) {
		informUserNoCodeGenerated();
		removeOldASMFile(outfile);
	}
	private static void informUserNoCodeGenerated() {
		System.err.println("program has errors.  no executable created.");
	}
	private static void removeOldASMFile(String filename) {
		File file = new File(filename);
		if(file.exists()) {
			file.delete();
		}
	}
	
	// normal code generation and optimization.
	private static void generateAndPrintCode(String outfile, ParseNode decoratedTree) 
			throws FileNotFoundException {
		ASMCodeFragment code = ASMCodeGenerator.generate(decoratedTree);
		printCodeToFile(outfile, code);
	}
	private static void printCodeToFile(String filename, ASMCodeFragment code)
			throws FileNotFoundException {
		File file = new File(filename);
		PrintStream out = new PrintStream(file);
		out.print(code);
		out.close();
	}

	private static boolean thereAreErrors() {
		return logging.BilbyLogger.hasErrors();
	}
}
