/**
 * TokenReader: Token stream
 * Generated by lexer, paser can read token from this stream.
 */
public interface TokenReader {
    /**
     * Return Token in stream and remove it from stream, return null if the stream
     * is empty.
     * 
     * @return Token
     */
    public Token read();

    /**
     * Return Token in stream but does not remove it from stream, return null if the
     * stream is empty.
     * 
     * @return Token
     */
    public Token peek();

    /**
     * Token stream goes back one position and restores the original token.
     */
    public void unread();

    /**
     * Get current reading position of Token stream.
     * 
     * @return position
     */
    public int getPosition();

    /**
     * Set current reading position of Token stream.
     * 
     * @param position
     */
    public void setPosition(int position);
}
