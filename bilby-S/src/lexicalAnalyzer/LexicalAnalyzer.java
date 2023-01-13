package lexicalAnalyzer;

import logging.BilbyLogger;

import inputHandler.InputHandler;
import inputHandler.LocatedChar;
import inputHandler.LocatedCharStream;
import inputHandler.PushbackCharStream;
import tokens.CharacterToken;
import tokens.FloatToken;
import tokens.IdentifierToken;
import tokens.LextantToken;
import tokens.NullToken;
import tokens.NumberToken;
import tokens.StringToken;
import tokens.Token;

import static lexicalAnalyzer.PunctuatorScanningAids.*;

public class LexicalAnalyzer extends ScannerImp {
    public static LexicalAnalyzer make(String filename) {
        InputHandler handler = InputHandler.fromFilename(filename);
        PushbackCharStream charStream = PushbackCharStream.make(handler);
        return new LexicalAnalyzer(charStream);
    }

    public LexicalAnalyzer(PushbackCharStream input) {
        super(input);
    }

    //////////////////////////////////////////////////////////////////////////////
    // Token-finding main dispatch

    @Override
    protected Token findNextToken() {
        LocatedChar ch = nextNonWhitespaceChar();
        if (ch.isDigit()) {
            try {
                return scanNumber(ch);
            } catch (NumberFormatException e) {
                lexicalError(ch);
                return findNextToken();
            }
        } else if (ch.isIdentifierLeadingChar()) {
            return scanIdentifier(ch);
        } else if (isCharacterStart(ch)) {
            try {
                return scanCharacter(ch);
            } catch (Exception e) {
                lexicalError(ch);
                return findNextToken();
            }
        } else if (isPunctuatorStart(ch)) {
            return PunctuatorScanner.scan(ch, input);
        } else if (ch.isChar('%')) {
            skipComment(ch);
            return findNextToken();
        } else if (ch.isChar('"')) {
            try {
                return scanString(ch);
            } catch (Exception e) {
                lexicalError(ch);
                return findNextToken();
            }
        } else if (isEndOfInput(ch)) {
            return NullToken.make(ch);
        } else {
            lexicalError(ch);
            return findNextToken();
        }
    }

    private Token scanString(LocatedChar ch) {
        assert ch.isChar('"');
        StringBuffer buffer = new StringBuffer();
        buffer.append(ch.getCharacter());
        LocatedChar c = input.next();
        while (!c.isChar('"') && !c.isChar('\n') && !isEndOfInput(c)) {
            buffer.append(c.getCharacter());
            c = input.next();
        }
        if (c.isChar('"')) {
            buffer.append(c.getCharacter());
            return StringToken.make(ch, buffer.toString());
        }
        lexicalError(c);
        return findNextToken();
    }

    private void skipComment(LocatedChar ch) {
        LocatedChar c = input.next();
        while (!c.isChar('%') && !c.isChar('\n') && !isEndOfInput(c)) {
            c = input.next();
        }
    }

    private Token scanCharacter(LocatedChar ch) {
        assert isCharacterStart(ch);
        StringBuffer buffer = new StringBuffer();
        buffer.append(ch.getCharacter());
        LocatedChar c = input.next();
        if (c.isWhitespace()) {
            buffer.append(c.getCharacter());
            return CharacterToken.make(ch, buffer.toString());
        }
        buffer.append(c.getCharacter());
        assert !isEndOfInput(c);
        c = input.next();
        // TODO: REFACTOR THIS
        while (!c.isWhitespace() && !isEndOfInput(c) && !c.isChar(';')) {
            buffer.append(c.getCharacter());
            c = input.next();
        }
        input.pushback(c);
        return CharacterToken.make(ch, buffer.toString());
    }

    private boolean isCharacterStart(LocatedChar ch) {
        return ch.getCharacter() == '#';
    }

    private LocatedChar nextNonWhitespaceChar() {
        LocatedChar ch = input.next();
        while (ch.isWhitespace()) {
            ch = input.next();
        }
        return ch;
    }

    //////////////////////////////////////////////////////////////////////////////
    // Integer/Floating lexical analysis

    private Token scanNumber(LocatedChar firstChar) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(firstChar.getCharacter());
        appendSubsequentDigits(buffer);
        LocatedChar c = input.next();
        if (!c.isChar('.')) {
            input.pushback(c);
            return NumberToken.make(firstChar, buffer.toString());
        }
        buffer.append(c.getCharacter()); // .
        c = input.next();
        if (!c.isDigit()) {
            lexicalError(c);
            return findNextToken();
        }
        buffer.append(c.getCharacter());
        appendSubsequentDigits(buffer);
        c = input.next();
        if (!c.isChar('E')) {
            input.pushback(c);
            return FloatToken.make(firstChar, buffer.toString());
        }
        buffer.append(c.getCharacter()); // E
        c = input.next();
        if (c.isChar('+') || c.isChar('-')) {
            buffer.append(c.getCharacter());
            c = input.next();
        }
        if (!c.isDigit()) {
            lexicalError(c);
            return findNextToken();
        }
        buffer.append(c.getCharacter());
        appendSubsequentDigits(buffer);
        return FloatToken.make(firstChar, buffer.toString());
    }

    private void appendSubsequentDigits(StringBuffer buffer) {
        LocatedChar c = input.next();
        while (c.isDigit()) {
            buffer.append(c.getCharacter());
            c = input.next();
        }
        input.pushback(c);
    }

    //////////////////////////////////////////////////////////////////////////////
    // Identifier and keyword lexical analysis

    private Token scanIdentifier(LocatedChar firstChar) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(firstChar.getCharacter());
        appendSubsequentIdentifierChars(buffer);

        String lexeme = buffer.toString();
        if (Keyword.isAKeyword(lexeme)) {
            return LextantToken.make(firstChar, lexeme, Keyword.forLexeme(lexeme));
        } else {
            return IdentifierToken.make(firstChar, lexeme);
        }
    }

    private void appendSubsequentIdentifierChars(StringBuffer buffer) {
        LocatedChar c = input.next();
        while (c.isIdentifierLeadingChar() || c.isDigit()) {
            buffer.append(c.getCharacter());
            c = input.next();
        }
        input.pushback(c);
    }

    //////////////////////////////////////////////////////////////////////////////
    // Punctuator lexical analysis
    // old method left in to show a simple scanning method.
    // current method is the algorithm object PunctuatorScanner.java

    @SuppressWarnings("unused")
    private Token oldScanPunctuator(LocatedChar ch) {

        switch (ch.getCharacter()) {
        case '*':
            return LextantToken.make(ch, "*", Punctuator.MULTIPLY);
        case '+':
            return LextantToken.make(ch, "+", Punctuator.ADD);
        case '>':
            return LextantToken.make(ch, ">", Punctuator.GREATER);
        case ':':
            if (ch.getCharacter() == '=') {
                return LextantToken.make(ch, ":=", Punctuator.ASSIGN);
            } else {
                lexicalError(ch);
                return (NullToken.make(ch));
            }
        case ',':
            return LextantToken.make(ch, ",", Punctuator.PRINT_SEPARATOR);
        case ';':
            return LextantToken.make(ch, ";", Punctuator.TERMINATOR);
        default:
            lexicalError(ch);
            return (NullToken.make(ch));
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    // Character-classification routines specific to bilby scanning.

    private boolean isPunctuatorStart(LocatedChar lc) {
        char c = lc.getCharacter();
        return isPunctuatorStartingCharacter(c);
    }

    private boolean isEndOfInput(LocatedChar lc) {
        return lc == LocatedCharStream.FLAG_END_OF_INPUT;
    }

    //////////////////////////////////////////////////////////////////////////////
    // Error-reporting

    private void lexicalError(LocatedChar ch) {
        BilbyLogger log = BilbyLogger.getLogger("compiler.lexicalAnalyzer");
        log.severe("Lexical error: invalid character " + ch);
    }

}
