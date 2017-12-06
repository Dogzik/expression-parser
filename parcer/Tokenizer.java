package parcer;

import exceptions.*;
import operations.Operation;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Лев on 09.04.2017.
 */
public class Tokenizer<T> {
    private String expression;
    private int ind = 0;

    final private Operation<T> op;

    private Token curToken;
    private static HashSet<Token> OPERATIONS = new HashSet<>();
    private static HashSet<Token> BINARY_OPERATIONS = new HashSet<>();
    private static HashMap<String, Token> IDENTIFIERS = new HashMap<>();

    static {
        IDENTIFIERS.put("square", Token.SQR);
        IDENTIFIERS.put("mod", Token.MOD);
        IDENTIFIERS.put("abs", Token.ABS);
        IDENTIFIERS.put("x", Token.VARIABLE);
        IDENTIFIERS.put("y", Token.VARIABLE);
        IDENTIFIERS.put("z", Token.VARIABLE);

        OPERATIONS.add(Token.SQR);
        OPERATIONS.add(Token.NOT);
        OPERATIONS.add(Token.ABS);
        OPERATIONS.add(Token.ADD);
        OPERATIONS.add(Token.SUB);
        OPERATIONS.add(Token.MUL);
        OPERATIONS.add(Token.DIV);
        OPERATIONS.add(Token.MOD);

        BINARY_OPERATIONS.add(Token.ADD);
        BINARY_OPERATIONS.add(Token.SUB);
        BINARY_OPERATIONS.add(Token.MUL);
        BINARY_OPERATIONS.add(Token.DIV);
        BINARY_OPERATIONS.add(Token.MOD);
    }

    private T value;
    private char name;
    private int balance;

    public Tokenizer(final String s, final Operation<T> operation) {
        expression = s;
        ind = 0;
        curToken = Token.BEGIN;
        balance = 0;
        op = operation;
    }

    public Token getNextToken() throws ParsingException {
        nextToken();
        return curToken;
    }

    public Token getCurrentToken() {
        return curToken;
    }

    public T getValue() {
        return value;
    }

    public char getName() {
        return name;
    }

    public int getInd() {
        return ind;
    }

    public String getExpression() {
        return expression;
    }

    private void skipWhiteSpace() {
        while (ind < expression.length() && Character.isWhitespace(expression.charAt(ind))) {
            ind++;
        }
    }

    private void checkForOperand(final int pos) throws MissingOperandException {
        if (OPERATIONS.contains(curToken) || curToken == Token.OPEN_BRACE || curToken == Token.BEGIN) {
            throw new MissingOperandException(expression, pos);
        }
    }

    private void checkForOperation(final int pos) throws MissingOperationException {
        if (curToken == Token.CLOSE_BRACE || curToken == Token.VARIABLE || curToken == Token.NUMBER) {
            throw new MissingOperationException(expression, pos);
        }
    }

    private boolean isPartOfNumber(final char x) {
        return Character.isDigit(x) || x == '.' || x == 'e';
    }

    private String getNumber() {
        int l = ind;
        while (ind < expression.length() && isPartOfNumber(expression.charAt(ind))) {
            ind++;
        }
        int r = ind;
        ind--;
        return expression.substring(l, r);
    }

    private boolean isPartOfIdentifier(final char x) {
        return Character.isLetterOrDigit(x) || x == '_';
    }

    private String getIdentifier() throws UnknownSymbolException {
        if (!Character.isLetter(expression.charAt(ind))) {
            throw new UnknownSymbolException(expression, ind);
        }
        int l = ind;
        while (ind < expression.length() && isPartOfIdentifier(expression.charAt(ind))) {
            ind++;
        }
        int r = ind;
        ind--;
        return expression.substring(l, r);
    }

    private void nextToken() throws ParsingException {
        skipWhiteSpace();
        if (ind >= expression.length()) {
            checkForOperand(ind);
            curToken = Token.END;
            return;
        }
        char ch = expression.charAt(ind);
        switch (ch) {
            case '-':
                if (curToken == Token.NUMBER || curToken == Token.VARIABLE || curToken == Token.CLOSE_BRACE) {
                    curToken = Token.SUB;
                } else {
                    if (ind + 1 >= expression.length()) {
                        throw new MissingOperandException(expression, ind + 1);
                    }
                    if (Character.isDigit(expression.charAt(ind + 1))) {
                        ind++;
                        String temp = getNumber();
                        try {
                            value = op.parseNumber("-" + temp);
                        } catch (IncorrectConstException e) {
                            throw new IllegalConstantException("-" + temp, expression, ind - temp.length());
                        }
                        curToken = Token.NUMBER;
                    } else {
                        curToken = Token.NOT;
                    }
                }
                break;
            case '+':
                checkForOperand(ind);
                curToken = Token.ADD;
                break;
            case '*':
                checkForOperand(ind);
                curToken = Token.MUL;
                break;
            case '/':
                checkForOperand(ind);
                curToken = Token.DIV;
                break;
            case '(':
                if (curToken == Token.CLOSE_BRACE || curToken == Token.NUMBER || curToken == Token.VARIABLE) {
                    throw new OddOpeningParenthesisException(expression, ind);
                }
                balance++;
                curToken = Token.OPEN_BRACE;
                break;
            case ')':
                if (OPERATIONS.contains(curToken) || curToken == Token.OPEN_BRACE) {
                    throw new MissingOperandException(expression, ind);
                }
                balance--;
                if (balance < 0) {
                    throw new OddClosingParenthesisException(expression, ind);
                }
                curToken = Token.CLOSE_BRACE;
                break;
            default:
                if (Character.isDigit(ch)) {
                    checkForOperation(ind);
                    String temp = getNumber();
                    try {
                        value = op.parseNumber(temp);
                    } catch (IncorrectConstException e) {
                        throw new IllegalConstantException(temp, expression, ind - temp.length() + 1);
                    }
                    curToken = Token.NUMBER;
                } else {
                    String curInd = getIdentifier();
                    if (!IDENTIFIERS.containsKey(curInd)) {
                        throw new UnknownIdentifierException(curInd, expression, ind - curInd.length() + 1);
                    }
                    if (BINARY_OPERATIONS.contains(IDENTIFIERS.get(curInd))) {
                        checkForOperand(ind - curInd.length() + 1);
                    } else {
                        checkForOperation(ind - curInd.length() + 1);
                    }
                    curToken = IDENTIFIERS.get(curInd);
                    if (curToken == Token.VARIABLE) {
                        name = curInd.charAt(0);
                    }
                }

        }
        ind++;
    }
}
