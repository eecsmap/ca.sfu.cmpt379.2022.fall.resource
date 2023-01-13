package applications;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import lexicalAnalyzer.LexicalAnalyzer;
import lexicalAnalyzer.Scanner;
import parseTree.ParseNode;
import parseTree.ParseTreePrinter;
import parser.Parser;
import semanticAnalyzer.SemanticAnalyzer;
import tokens.Tokens;

public class BilbySemanticChecker extends BilbyApplication {
    /**
     * Checks semantics of a bilby file. Prints filename and "done" if syntax is
     * correct; prints errors if not.
     * 
     * @param args
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        checkArguments(args, className());

        ParseTreePrinter.setPrintLevel(ParseTreePrinter.Level.FULL);
        Tokens.setPrintLevel(Tokens.Level.FULL);
        checkFileSemantics(args[0], System.out);
    }

    /**
     * analyzes a file specified by filename and prints a decorated syntax tree for
     * the file.
     * 
     * @param filename the name of the file to be analyzed.
     * @param out      the PrintStream to print the decorated tree to.
     * @throws FileNotFoundException
     */
    public static void checkFileSemantics(String filename, PrintStream out) throws FileNotFoundException {
        Scanner scanner = LexicalAnalyzer.make(filename);
        ParseNode syntaxTree = Parser.parse(scanner);
        ParseNode decoratedTree = SemanticAnalyzer.analyze(syntaxTree);

        out.print(decoratedTree);
    }
}
