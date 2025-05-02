package edu.rice.cs.drjava.model.compiler.fjpreprocessor;

import java.util.ArrayList;

import org.antlr.v4.runtime.*;

import edu.rice.cs.drjava.model.compiler.fjpreprocessor.grammar.JavaLexer;

public class BufferedLexer {

    JavaLexer lexer;
    ArrayList<Token> buffer;

    public BufferedLexer(CharStream input) {
        this.lexer = new JavaLexer(input);
        this.buffer = new ArrayList<>();
    }

    public Token nextTokenUnbuffered() {
        Token token = lexer.nextToken();
        return token;
    }

    public Token nextTokenUnbufferedSkipWS() {
        Token token = lexer.nextToken();
        while (token.getType() == JavaLexer.WS) {
            token = lexer.nextToken();
        }
        return token;
    }

    public Token nextToken() {

        if (buffer.isEmpty()) {
            Token token = lexer.nextToken();
            return token;
        } else {
            Token token = buffer.remove(0);

            return token;
        }
    }

    public Token nextTokenSkipWS() {
        Token token = nextToken();
        while (token.getType() == JavaLexer.WS) {
            token = nextToken();
        }
        return token;
    }

    public Token[] peekN(int n, boolean skipWS) {
        while (buffer.size() < n) {
            Token token = skipWS ? nextTokenUnbufferedSkipWS()
                    : nextTokenUnbuffered();
            if (token.getType() == Token.EOF) {
                break;
            }
            buffer.add(token);
        }
        int newn = Math.min(n, buffer.size());
        Token[] tokens = new Token[newn];
        for (int i = 0; i < newn; i++) {
            tokens[i] = buffer.get(i);
        }
        return tokens;
    }

}
