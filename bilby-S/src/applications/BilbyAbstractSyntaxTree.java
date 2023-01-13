package applications;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import parseTree.ParseNode;
import parseTree.ParseTreePrinter;
import parser.Parser;

import lexicalAnalyzer.LexicalAnalyzer;
import lexicalAnalyzer.Scanner;
import tokens.Tokens;

public class BilbyAbstractSyntaxTree extends BilbyApplication {
    /**
     * Prints abstract syntax tree of a bilby file. Prints errors if syntax
     * incorrect.
     * 
     * @param args
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        checkArguments(args, className());

        ParseTreePrinter.setPrintLevel(ParseTreePrinter.Level.FULL);
        Tokens.setPrintLevel(Tokens.Level.TYPE_VALUE_SEQ);
        parseFileToAST(args[0], System.out);
    }

    /**
     * analyzes a file specified by filename.
     * 
     * @param filename the name of the file to be analyzed.
     * @throws FileNotFoundException
     */
    public static void parseFileToAST(String filename, PrintStream out) throws FileNotFoundException {
        Scanner scanner = LexicalAnalyzer.make(filename);
        ParseNode syntaxTree = Parser.parse(scanner);

        out.print(syntaxTree);
    }
}
