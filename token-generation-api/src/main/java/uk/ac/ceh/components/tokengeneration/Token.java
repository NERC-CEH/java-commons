package uk.ac.ceh.components.tokengeneration;

/**
 * The following is a simple wrapper for a token which is constructed of a byte
 * array.
 * @author Christopher Johnson
 */
public class Token {
    private final byte[] bytes;
    
    protected Token() {
        this(new byte[0]);
    }
    
    public Token(byte[] bytes) {
        this.bytes = bytes;
    }
    
    /**
     * @return The underlying byte array of this token
     */
    public byte[] getBytes() {
        return bytes;
    }
}
