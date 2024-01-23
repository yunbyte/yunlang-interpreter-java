/**
 * Token: lexer's output and paser's input, have two properties: type and text
 */
public interface Token {

    public TokenType getType();

    public String getText();
}
