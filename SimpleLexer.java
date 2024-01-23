import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimpleLexer {
    public static void main(String[] args) {
        SimpleLexer lexer = new SimpleLexer();
        // testging: int age = 45;
        String script = "int age = 45;";
        System.out.println("parse code: " + script);
        SimpleTokenReader tokenReader = lexer.tokenize(script);
        dump(tokenReader);

        script = "inta age = 45;";
        System.out.println("------------------------");
        System.out.println("parse code: " + script);
        tokenReader = lexer.tokenize(script);
        dump(tokenReader);

        // testing: in
        script = "in age = 45;";
        System.out.println("------------------------");
        System.out.println("parse code: " + script);
        tokenReader = lexer.tokenize(script);
        dump(tokenReader);

        // testing: age >= 45;
        script = "age >= 45;";
        System.out.println("------------------------");
        System.out.println("parse code: " + script);
        tokenReader = lexer.tokenize(script);
        dump(tokenReader);

        // testing: age > 45;
        script = "age > 45;";
        System.out.println("------------------------");
        System.out.println("parse code: " + script);
        tokenReader = lexer.tokenize(script);
        dump(tokenReader);

        // testing: 1+ 2*3;
        script = "1+2*3;";
        System.out.println("------------------------");
        System.out.println("parse code: " + script);
        tokenReader = lexer.tokenize(script);
        dump(tokenReader);

        // testing: 1 + 2 * 3;
        script = "1 + 2 * 3;";
        System.out.println("------------------------");
        System.out.println("parse code: " + script);
        tokenReader = lexer.tokenize(script);
        dump(tokenReader);
    }

    /**
     * Various stats of finite state machine.
     */
    private enum DfaState {
        Initial,

        If, Id_if1, Id_if2, Else, Id_else1, Id_else2, Id_else3, Id_else4, Int, Id_int1, Id_int2, Id_int3, Id, GT, GE,

        Assignment,

        Plus, Minus, Star, Slash,

        SemiColon,
        LeftParen,
        RightParen,

        IntLiteral
    }

    private StringBuffer tokenText = null;
    private List<Token> tokens = null;
    private SimpleToken token = null;

    /**
     * Parse string to generate Token.
     * This is a finite state machine.
     * 
     * @param code
     * @return
     */
    public SimpleTokenReader tokenize(String code) {
        CharArrayReader reader = new CharArrayReader(code.toCharArray());
        tokens = new ArrayList<Token>();
        tokenText = new StringBuffer();
        token = new SimpleToken();
        int ich = 0;
        char ch = 0;
        DfaState state = DfaState.Initial;

        try {
            while ((ich = reader.read()) != -1) {
                ch = (char) ich;
                switch (state) {
                    case Initial:
                        state = initToken(ch); // Reconfirm subsequent status
                        break;
                    case Id:
                        if (isAlpha(ch) || isDigit(ch)) {
                            tokenText.append(ch); // keep Identifire state
                        } else {
                            state = initToken(ch); // exit Identifier state
                        }
                        break;
                    case GT:
                        if (ch == '=') {
                            token.type = TokenType.GE; // change state to GE
                            state = DfaState.GE;
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch); // exit identifier state
                        }
                        break;
                    case GE:
                    case Assignment:
                    case Plus:
                    case Minus:
                    case Star:
                    case Slash:
                    case SemiColon:
                    case LeftParen:
                    case RightParen:
                        state = initToken(ch); // exit current state and save Token
                        break;
                    case IntLiteral:
                        if (isDigit(ch)) {
                            tokenText.append(ch); // keep IntLiteral state
                        } else {
                            state = initToken(ch); // exit current state and save Token
                        }
                        break;
                    case Id_int1:
                        if (ch == 'n') {
                            state = DfaState.Id_int2;
                            tokenText.append(ch);
                        } else if (isDigit(ch) || isAlpha(ch)) {
                            state = DfaState.Id;
                            tokenText.append(ch);

                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case Id_int2:
                        if (ch == 't') {
                            state = DfaState.Id_int3;
                            tokenText.append(ch);
                        } else if (isDigit(ch) || isAlpha(ch)) {
                            state = DfaState.Id;
                            tokenText.append(ch);

                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case Id_int3:
                        if (isBlank(ch)) {
                            token.type = TokenType.Int;
                            state = initToken(ch);
                        } else {
                            state = DfaState.Id;
                            tokenText.append(ch);
                        }
                        break;
                    default:
                        break;
                }
            }
            // append last Token to Token list
            if (tokenText.length() > 0) {
                initToken(ch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new SimpleTokenReader(tokens);
    }

    /**
     * Finite state machine enters initial state.
     * 
     * @param ch
     * @return
     */
    private DfaState initToken(char ch) {
        if (tokenText.length() > 0) {
            token.text = tokenText.toString();
            tokens.add(token);

            tokenText = new StringBuffer();
            token = new SimpleToken();
        }

        DfaState newState = DfaState.Initial;
        if (isAlpha(ch)) { // the first chan is alpha
            if (ch == 'i') {
                newState = DfaState.Id_int1;
            } else {
                newState = DfaState.Id; // entry Id state
            }
            token.type = TokenType.Identifier;
            tokenText.append(ch);
        } else if (isDigit(ch)) { // the first char is digit
            newState = DfaState.IntLiteral;
            token.type = TokenType.IntLiteral;
            tokenText.append(ch);
        } else if (ch == '>') { // the first char is GT
            newState = DfaState.GT;
            token.type = TokenType.GT;
            tokenText.append(ch);
        } else if (ch == '+') {
            newState = DfaState.Plus;
            token.type = TokenType.Plus;
            tokenText.append(ch);
        } else if (ch == '-') {
            newState = DfaState.Minus;
            token.type = TokenType.Plus;
            tokenText.append(ch);
        } else if (ch == '*') {
            newState = DfaState.Star;
            token.type = TokenType.Star;
            tokenText.append(ch);
        } else if (ch == '/') {
            newState = DfaState.Slash;
            token.type = TokenType.Slash;
            tokenText.append(ch);
        } else if (ch == ';') {
            newState = DfaState.SemiColon;
            token.type = TokenType.SemiColon;
            tokenText.append(ch);
        } else if (ch == '(') {
            newState = DfaState.LeftParen;
            token.type = TokenType.LeftParen;
            tokenText.append(ch);
        } else if (ch == ')') {
            newState = DfaState.RightParen;
            token.type = TokenType.RightParen;
            tokenText.append(ch);
        } else if (ch == '=') {
            newState = DfaState.Assignment;
            token.type = TokenType.Assignment;
            tokenText.append(ch);
        } else {
            newState = DfaState.Initial; // skip all unknown patterns
        }

        return newState;
    }

    /**
     * Determine if the input character is a alpha.
     * 
     * @param ch
     * @return
     */
    private boolean isAlpha(int ch) {
        return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z';
    }

    /**
     * Determine if the input character is a digit.
     * 
     * @param ch
     * @return
     */
    private boolean isDigit(int ch) {
        return ch >= '0' && ch <= '9';
    }

    /**
     * Determine if the input character is a digit.
     * 
     * @param ch
     * @return
     */
    private boolean isBlank(int ch) {
        return ch == ' ' || ch == '\t' || ch == '\n';
    }

    /**
     * A simple impementation of Token
     */
    private final class SimpleToken implements Token {

        private TokenType type = null;
        private String text = null;

        @Override
        public TokenType getType() {
            return type;
        }

        @Override
        public String getText() {
            return text;
        }
    }

    /**
     * Print all tokens
     * 
     * @param tokenReader
     */
    public static void dump(SimpleTokenReader tokenReader) {
        System.out.println("text\ttype");
        Token token = null;
        while ((token = tokenReader.read()) != null) {
            System.out.println(token.getText() + "\t\t" + token.getType());
        }
    }

    /**
     * Token stream, encapsulates a token list.
     */
    private class SimpleTokenReader implements TokenReader {
        List<Token> tokens = null;
        int pos = 0;

        public SimpleTokenReader(List<Token> tokens) {
            this.tokens = tokens;
        }

        @Override
        public Token read() {
            if (pos < tokens.size()) {
                return tokens.get(pos++);
            }
            return null;
        }

        @Override
        public Token peek() {
            if (pos < tokens.size()) {
                return tokens.get(pos);
            }
            return null;
        }

        @Override
        public void unread() {
            if (pos > 0) {
                pos--;
            }
        }

        @Override
        public int getPosition() {
            return pos;
        }

        @Override
        public void setPosition(int position) {
            if (position >= 0 && position < tokens.size()) {
                pos = position;
            }
        }
    }
}
