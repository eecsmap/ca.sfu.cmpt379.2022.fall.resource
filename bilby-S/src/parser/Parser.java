package parser;

import java.util.Arrays;

import logging.BilbyLogger;
import parseTree.*;
import parseTree.nodeTypes.AssignmentStatementNode;
import parseTree.nodeTypes.BlockStatementNode;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.CallStatementNode;
import parseTree.nodeTypes.CastNode;
import parseTree.nodeTypes.CharConstantNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ErrorNode;
import parseTree.nodeTypes.ExpressionListNode;
import parseTree.nodeTypes.FloatConstantNode;
import parseTree.nodeTypes.FunctionDefinitionNode;
import parseTree.nodeTypes.FunctionInvocationNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IfStatementNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.OperatorNode;
import parseTree.nodeTypes.ParameterListNode;
import parseTree.nodeTypes.ParameterNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.ReturnStatementNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.TabNode;
import parseTree.nodeTypes.TypeNode;
import tokens.*;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;
import lexicalAnalyzer.Scanner;

public class Parser {
    private Scanner scanner;
    private Token nowReading;
    private Token previouslyRead;

    public static ParseNode parse(Scanner scanner) {
        Parser parser = new Parser(scanner);
        return parser.parse();
    }

    public Parser(Scanner scanner) {
        super();
        this.scanner = scanner;
    }

    public ParseNode parse() {
        readToken();
        return parseProgram();
    }

    ////////////////////////////////////////////////////////////
    // "program" is the start symbol S
    // S -> functionDefinition* MAIN blockStatement

    private ParseNode parseProgram() {
        if (!startsProgram(nowReading)) {
            return syntaxErrorNode("program");
        }
        ParseNode program = new ProgramNode(nowReading);

        while (startsFunctionDefinition(nowReading)) {
            ParseNode globalDefinition = parseFunctionDefinition();
            program.appendChild(globalDefinition);
        }

        expect(Keyword.MAIN);
        ParseNode block = parseBlockStatement();
        program.appendChild(block);

        if (!(nowReading instanceof NullToken)) {
            return syntaxErrorNode("end of program");
        }

        return program;
    }

    // functionDefinition -> FUNC type identifier ( parameterList ) blockStatement
    private ParseNode parseFunctionDefinition() {
        assert startsFunctionDefinition(nowReading);
        Token token = nowReading;
        expect(Keyword.FUNC);
        ParseNode type = parseType();
        ParseNode identifier = parseIdentifier();
        expect(Punctuator.OPEN_PAREN);
        ParseNode parameterList = parseParameterList();
        expect(Punctuator.CLOSE_PAREN);
        ParseNode blockStatement = parseBlockStatement();
        return FunctionDefinitionNode.withChildren(token, type, identifier, parameterList, blockStatement);
    }

    private boolean startsFunctionDefinition(Token token) {
        return token.isLextant(Keyword.FUNC);
    }

    private ParseNode parseType() {
        if (!startsType(nowReading)) {
            return syntaxErrorNode("type");
        }
        ParseNode type = new TypeNode(nowReading);
        readToken();
        return type;
    }

    private boolean startsType(Token token) {
        return startsNonVoidType(token) || token.isLextant(Keyword.VOID);
    }

    // parameterList -> e | parameterSpecification ( , parameterSpecification )*
    private ParseNode parseParameterList() {
        ParseNode parameterList = new ParameterListNode(nowReading);
        while (startsParameter(nowReading)) {
            ParseNode parameterSpecification = parseParameter();
            parameterList.appendChild(parameterSpecification);
            if (nowReading.isLextant(Punctuator.COMMA)) {
                readToken();
            }
        }
        return parameterList;
    }

    // parameterSpecification -> type identifier
    private ParseNode parseParameter() {
        assert startsParameter(nowReading);
        Token token = nowReading;
        ParseNode type = parseType();
        ParseNode identifier = parseIdentifier();
        return ParameterNode.withChildren(token, type, identifier);
    }

    private boolean startsParameter(Token token) {
        return startsNonVoidType(token);
    }

    private boolean startsNonVoidType(Token token) {
        return token.isLextant(Keyword.INT, Keyword.BOOL, Keyword.CHAR, Keyword.FLOAT, Keyword.STRING);
    }

    // blockStatement -> { statement* }
    private ParseNode parseBlockStatement() {
        if (!startsBlockStatement(nowReading)) {
            return syntaxErrorNode("block statement");
        }
        ParseNode blockStatement = new BlockStatementNode(nowReading);
        expect(Punctuator.OPEN_BRACE);
        while (startsStatement(nowReading)) {
            ParseNode statement = parseStatement();
            blockStatement.appendChild(statement);
        }
        expect(Punctuator.CLOSE_BRACE);
        return blockStatement;
    }

    private boolean startsBlockStatement(Token token) {
        return token.isLextant(Punctuator.OPEN_BRACE);
    }

    private boolean startsProgram(Token token) {
        return startsFunctionDefinition(token) || token.isLextant(Keyword.MAIN);
    }

    ///////////////////////////////////////////////////////////
    // statements

    // statement-> declaration | printStmt | callStatement | ifStmt |
    // returnStatement | assignmentStatement | blockStatement
    private ParseNode parseStatement() {
        if (!startsStatement(nowReading)) {
            return syntaxErrorNode("statement");
        }
        if (startsDeclaration(nowReading)) {
            return parseDeclaration();
        }
        if (startsPrintStatement(nowReading)) {
            return parsePrintStatement();
        }
        if (startsCallStatement(nowReading)) {
            return parseCallStatement();
        }
        if (startsIfStatement(nowReading)) {
            return parseIfStatement();
        }
        if (startsReturnStatement(nowReading)) {
            return parseReturnStatement();
        }
        if (startsAssignmentStatement(nowReading)) {
            return parseAssignmentStatement();
        }
        if (startsBlockStatement(nowReading)) {
            return parseBlockStatement();
        }
        return syntaxErrorNode("statement");
    }

    // assignmentStatement -> identifier := expression ;
    private ParseNode parseAssignmentStatement() {
        assert startsAssignmentStatement(nowReading);
        Token token = nowReading;
        ParseNode identifier = parseIdentifier();
        expect(Punctuator.ASSIGN);
        ParseNode expression = parseExpression();
        expect(Punctuator.TERMINATOR);
        return AssignmentStatementNode.withChildren(token, identifier, expression);
    }

    private boolean startsAssignmentStatement(Token token) {
        return startsIdentifier(token);
    }

    // returnStatement -> RETURN expression? ;
    private ParseNode parseReturnStatement() {
        assert startsReturnStatement(nowReading);
        Token token = nowReading;
        expect(Keyword.RETURN);
        if (startsExpression(nowReading)) {
            ParseNode expression = parseExpression();
            expect(Punctuator.TERMINATOR);
            return ReturnStatementNode.withChildren(token, expression);
        }
        expect(Punctuator.TERMINATOR);
        return new ReturnStatementNode(token);
    }

    private boolean startsReturnStatement(Token token) {
        return token.isLextant(Keyword.RETURN);
    }

    // ifStmt -> IF ( expression ) blockStatement ( ELSE blockStatement )?
    private ParseNode parseIfStatement() {
        assert startsIfStatement(nowReading);
        Token token = nowReading;
        expect(Keyword.IF);
        expect(Punctuator.OPEN_PAREN);
        ParseNode expression = parseExpression();
        expect(Punctuator.CLOSE_PAREN);
        ParseNode blockStatement = parseBlockStatement();
        if (nowReading.isLextant(Keyword.ELSE)) {
            readToken();
            ParseNode elseBlockStatement = parseBlockStatement();
            return IfStatementNode.make(token, expression, blockStatement, elseBlockStatement);
        }
        return IfStatementNode.make(token, expression, blockStatement);
    }

    // callStatement -> CALL functionInvocation ;
    // functionInvocation -> identifier ( expressionList )
    private ParseNode parseCallStatement() {
        assert startsCallStatement(nowReading);
        Token token = nowReading;
        expect(Keyword.CALL);
        ParseNode identifier = parseIdentifier();
        expect(Punctuator.OPEN_PAREN);
        ParseNode expressionList = parseExpressionList();
        expect(Punctuator.CLOSE_PAREN);
        ParseNode functionInvocation = FunctionInvocationNode.withChildren(identifier.getToken(), identifier,
                expressionList);
        expect(Punctuator.TERMINATOR);
        return CallStatementNode.withChildren(token, functionInvocation);
    }

    // expressionList -> e | expression ( , expression )*
    private ParseNode parseExpressionList() {
        ParseNode expressionList = new ExpressionListNode(nowReading);
        while (startsExpression(nowReading)) {
            ParseNode expression = parseExpression();
            expressionList.appendChild(expression);
            if (nowReading.isLextant(Punctuator.COMMA)) {
                readToken();
            }
        }
        return expressionList;
    }

    private boolean startsStatement(Token token) {
        return startsPrintStatement(token) || startsDeclaration(token) || startsCallStatement(token)
                || startsIfStatement(token) || startsReturnStatement(token) || startsAssignmentStatement(token)
                || startsBlockStatement(token);
    }

    private boolean startsIfStatement(Token token) {
        return token.isLextant(Keyword.IF);
    }

    private boolean startsCallStatement(Token token) {
        return token.isLextant(Keyword.CALL);
    }

    // printStmt -> PRINT printExpressionList TERMINATOR
    private ParseNode parsePrintStatement() {
        if (!startsPrintStatement(nowReading)) {
            return syntaxErrorNode("print statement");
        }
        ParseNode result = new PrintStatementNode(nowReading);

        readToken();
        result = parsePrintExpressionList(result);

        expect(Punctuator.TERMINATOR);
        return result;
    }

    private boolean startsPrintStatement(Token token) {
        return token.isLextant(Keyword.PRINT);
    }

    // This adds the printExpressions it parses to the children of the given parent
    // printExpressionList -> printSeparator* (expression printSeparator+)*
    // expression? (note that this is nullable)

    private ParseNode parsePrintExpressionList(ParseNode parent) {
        if (!startsPrintExpressionList(nowReading)) {
            return syntaxErrorNode("printExpressionList");
        }

        while (startsPrintSeparator(nowReading)) {
            parsePrintSeparator(parent);
        }
        while (startsExpression(nowReading)) {
            parent.appendChild(parseExpression());
            if (nowReading.isLextant(Punctuator.TERMINATOR)) {
                return parent;
            }
            do {
                parsePrintSeparator(parent);
            } while (startsPrintSeparator(nowReading));
        }
        return parent;
    }

    private boolean startsPrintExpressionList(Token token) {
        return startsExpression(token) || startsPrintSeparator(token);
    }

    // This adds the printSeparator it parses to the children of the given parent
    // printSeparator -> PRINT_SEPARATOR | PRINT_SPACE | PRINT_NEWLINE | PRINT_TAB

    private void parsePrintSeparator(ParseNode parent) {
        if (!startsPrintSeparator(nowReading)) {
            ParseNode child = syntaxErrorNode("print separator");
            parent.appendChild(child);
            return;
        }

        if (nowReading.isLextant(Punctuator.PRINT_NEWLINE)) {
            readToken();
            ParseNode child = new NewlineNode(previouslyRead);
            parent.appendChild(child);
        } else if (nowReading.isLextant(Punctuator.PRINT_SPACE)) {
            readToken();
            ParseNode child = new SpaceNode(previouslyRead);
            parent.appendChild(child);
        } else if (nowReading.isLextant(Punctuator.PRINT_TAB)) {
            readToken();
            ParseNode child = new TabNode(previouslyRead);
            parent.appendChild(child);
        } else if (nowReading.isLextant(Punctuator.PRINT_SEPARATOR)) {
            readToken();
        }
    }

    private boolean startsPrintSeparator(Token token) {
        return token.isLextant(Punctuator.PRINT_SEPARATOR, Punctuator.PRINT_SPACE, Punctuator.PRINT_NEWLINE,
                Punctuator.PRINT_TAB);
    }

    // declaration -> IMM identifier := expression TERMINATOR
    private ParseNode parseDeclaration() {
        if (!startsDeclaration(nowReading)) {
            return syntaxErrorNode("declaration");
        }
        Token declarationToken = nowReading;
        readToken();

        ParseNode identifier = parseIdentifier();
        expect(Punctuator.ASSIGN);
        ParseNode initializer = parseExpression();
        expect(Punctuator.TERMINATOR);

        return DeclarationNode.withChildren(declarationToken, identifier, initializer);
    }

    private boolean startsDeclaration(Token token) {
        return token.isLextant(Keyword.IMM, Keyword.MUT);
    }

    ///////////////////////////////////////////////////////////
    // expressions
    // expr -> comparisonExpression
    // comparisonExpression -> additiveExpression [> additiveExpression]?
    // additiveExpression -> multiplicativeExpression [+ multiplicativeExpression]*
    /////////////////////////////////////////////////////////// (left-assoc)
    // multiplicativeExpression -> atomicExpression [MULT atomicExpression]*
    /////////////////////////////////////////////////////////// (left-assoc)
    // atomicExpression -> unaryExpression | literal | functionInvocation |
    /////////////////////////////////////////////////////////// parenthesizedExpression
    /////////////////////////////////////////////////////////// | castExpression
    // unaryExpression -> UNARYOP atomicExpression
    // functionInvocation -> identifier ( expressionList )
    // literal -> intNumber | identifier | booleanConstant | charConstant |
    /////////////////////////////////////////////////////////// floatNumber

    // expr -> comparisonExpression
    private ParseNode parseExpression() {
        if (!startsExpression(nowReading)) {
            return syntaxErrorNode("expression");
        }
        return parseComparisonExpression();
    }

    private boolean startsExpression(Token token) {
        return startsComparisonExpression(token);
    }

    // comparisonExpression -> additiveExpression [(>|==|!=) additiveExpression]*
    private ParseNode parseComparisonExpression() {
        if (!startsComparisonExpression(nowReading)) {
            return syntaxErrorNode("comparison expression");
        }

        ParseNode left = parseAdditiveExpression();
        while (nowReading.isLextant(Punctuator.GREATER, Punctuator.LESS, Punctuator.GREATEREQUAL, Punctuator.LESSEQUAL,
                Punctuator.EQUAL, Punctuator.NOTEQUAL)) {
            Token compareToken = nowReading;
            readToken();
            ParseNode right = parseAdditiveExpression();

            left = OperatorNode.withChildren(compareToken, left, right);
        }
        return left;
    }

    private boolean startsComparisonExpression(Token token) {
        return startsAdditiveExpression(token);
    }

    // additiveExpression -> multiplicativeExpression [(+|-)
    // multiplicativeExpression]* (left-assoc)
    private ParseNode parseAdditiveExpression() {
        if (!startsAdditiveExpression(nowReading)) {
            return syntaxErrorNode("additiveExpression");
        }

        ParseNode left = parseMultiplicativeExpression();
        while (nowReading.isLextant(Punctuator.ADD, Punctuator.SUBTRACT)) {
            Token additiveToken = nowReading;
            readToken();
            ParseNode right = parseMultiplicativeExpression();

            left = OperatorNode.withChildren(additiveToken, left, right);
        }
        return left;
    }

    private boolean startsAdditiveExpression(Token token) {
        return startsMultiplicativeExpression(token);
    }

    // multiplicativeExpression -> atomicExpression [MULT atomicExpression]*
    // (left-assoc)
    private ParseNode parseMultiplicativeExpression() {
        if (!startsMultiplicativeExpression(nowReading)) {
            return syntaxErrorNode("multiplicativeExpression");
        }

        ParseNode left = parseAtomicExpression();
        while (nowReading.isLextant(Punctuator.MULTIPLY, Punctuator.DIVIDE)) {
            Token multiplicativeToken = nowReading;
            readToken();
            ParseNode right = parseAtomicExpression();

            left = OperatorNode.withChildren(multiplicativeToken, left, right);
        }
        return left;
    }

    private boolean startsMultiplicativeExpression(Token token) {
        return startsAtomicExpression(token);
    }

    // atomicExpression -> unaryExpression | literal | functionInvocation
    // functionInvocation -> identifier ( expressionList )
    private ParseNode parseAtomicExpression() {
        if (!startsAtomicExpression(nowReading)) {
            return syntaxErrorNode("atomic expression");
        }
        if (startsParenthesizedExpression(nowReading)) {
            return parseParenthesizedExpression();
        }
        if (startsCastExpression(nowReading)) {
            return parseCastExpression();
        }
        if (startsUnaryExpression(nowReading)) {
            return parseUnaryExpression();
        }

        ParseNode literal = parseLiteral();
        if (literal instanceof IdentifierNode && nowReading.isLextant(Punctuator.OPEN_PAREN)) {
            expect(Punctuator.OPEN_PAREN);
            ParseNode expressionList = parseExpressionList();
            expect(Punctuator.CLOSE_PAREN);
            return FunctionInvocationNode.withChildren(literal.getToken(), literal, expressionList);
        }
        return literal;
    }

    // castExpression -> [(expression) as type]
    private ParseNode parseCastExpression() {
        assert startsCastExpression(nowReading);
        Token castToken = nowReading;
        expect(Punctuator.OPEN_BRACKET);
        ParseNode expression = parseExpression();
        expect(Keyword.AS);
        ParseNode type = parseType();
        expect(Punctuator.CLOSE_BRACKET);
        return CastNode.withChildren(castToken, expression, type);
    }

    private boolean startsCastExpression(Token token) {
        return token.isLextant(Punctuator.OPEN_BRACKET);
    }

    private ParseNode parseParenthesizedExpression() {
        expect(Punctuator.OPEN_PAREN);
        ParseNode expression = parseExpression();
        expect(Punctuator.CLOSE_PAREN);
        return expression;
    }

    private boolean startsAtomicExpression(Token token) {
        return startsLiteral(token) || startsUnaryExpression(token) || startsParenthesizedExpression(token)
                || startsCastExpression(token);
    }

    private boolean startsParenthesizedExpression(Token token) {
        return token.isLextant(null, Punctuator.OPEN_PAREN);
    }

    // unaryExpression -> UNARYOP atomicExpression
    private ParseNode parseUnaryExpression() {
        if (!startsUnaryExpression(nowReading)) {
            return syntaxErrorNode("unary expression");
        }
        Token operatorToken = nowReading;
        readToken();
        ParseNode child = parseAtomicExpression();
        return OperatorNode.withChildren(operatorToken, child);
    }

    private boolean startsUnaryExpression(Token token) {
        return token.isLextant(Punctuator.SUBTRACT, Punctuator.ADD);
    }

    // literal -> number | identifier | booleanConstant | charConstant | floatNumber
    // | stringConstant
    private ParseNode parseLiteral() {
        if (!startsLiteral(nowReading)) {
            return syntaxErrorNode("literal");
        }

        if (startsIntLiteral(nowReading)) {
            return parseIntLiteral();
        }
        if (startsIdentifier(nowReading)) {
            return parseIdentifier();
        }
        if (startsBooleanLiteral(nowReading)) {
            return parseBooleanLiteral();
        }
        if (startsCharLiteral(nowReading)) {
            return parseCharLiteral();
        }
        if (startsFloatLiteral(nowReading)) {
            return parseFloatLiteral();
        }
        if (startsStringLiteral(nowReading)) {
            return parseStringLiteral();
        }

        return syntaxErrorNode("literal");
    }

    private ParseNode parseStringLiteral() {
        assert startsStringLiteral(nowReading);
        readToken();
        return new StringConstantNode(previouslyRead);
    }

    private boolean startsStringLiteral(Token token) {
        return token instanceof StringToken;
    }

    private ParseNode parseFloatLiteral() {
        assert startsFloatLiteral(nowReading);
        readToken();
        return new FloatConstantNode(previouslyRead);
    }

    private ParseNode parseCharLiteral() {
        assert startsCharLiteral(nowReading);
        readToken();
        return new CharConstantNode(previouslyRead);
    }

    private boolean startsLiteral(Token token) {
        return startsIntLiteral(token) || startsIdentifier(token) || startsBooleanLiteral(token)
                || startsCharLiteral(token) || startsFloatLiteral(token) || startsStringLiteral(token);
    }

    private boolean startsFloatLiteral(Token token) {
        return token instanceof FloatToken;
    }

    private boolean startsCharLiteral(Token token) {
        return token instanceof CharacterToken;
    }

    // number (literal)
    private ParseNode parseIntLiteral() {
        if (!startsIntLiteral(nowReading)) {
            return syntaxErrorNode("integer constant");
        }
        readToken();
        return new IntegerConstantNode(previouslyRead);
    }

    private boolean startsIntLiteral(Token token) {
        return token instanceof NumberToken;
    }

    // identifier (terminal)
    private ParseNode parseIdentifier() {
        if (!startsIdentifier(nowReading)) {
            return syntaxErrorNode("identifier");
        }
        readToken();
        return new IdentifierNode(previouslyRead);
    }

    private boolean startsIdentifier(Token token) {
        return token instanceof IdentifierToken;
    }

    // boolean literal
    private ParseNode parseBooleanLiteral() {
        if (!startsBooleanLiteral(nowReading)) {
            return syntaxErrorNode("boolean constant");
        }
        readToken();
        return new BooleanConstantNode(previouslyRead);
    }

    private boolean startsBooleanLiteral(Token token) {
        return token.isLextant(Keyword.TRUE, Keyword.FALSE);
    }

    private void readToken() {
        previouslyRead = nowReading;
        nowReading = scanner.next();
    }

    // if the current token is one of the given lextants, read the next token.
    // otherwise, give a syntax error and read next token (to avoid endless
    // looping).
    private void expect(Lextant... lextants) {
        if (!nowReading.isLextant(lextants)) {
            syntaxError(nowReading, "expecting " + Arrays.toString(lextants));
        }
        readToken();
    }

    private ErrorNode syntaxErrorNode(String expectedSymbol) {
        syntaxError(nowReading, "expecting " + expectedSymbol);
        ErrorNode errorNode = new ErrorNode(nowReading);
        readToken();
        return errorNode;
    }

    private void syntaxError(Token token, String errorDescription) {
        String message = "" + token.getLocation() + " " + errorDescription;
        error(message);
    }

    private void error(String message) {
        BilbyLogger log = BilbyLogger.getLogger("compiler.Parser");
        log.severe("syntax error: " + message);
    }
}
