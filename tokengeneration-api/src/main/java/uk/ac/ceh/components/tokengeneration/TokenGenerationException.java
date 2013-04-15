package uk.ac.ceh.components.tokengeneration;

/**
 * The following exception will be thrown if it was not possible to generate a 
 * token
 * @author Christopher Johnson
 */
public class TokenGenerationException extends Exception {
    public TokenGenerationException() {
        super();
    }
    
    public TokenGenerationException(String mess) {
        super(mess);
    }
    
    public TokenGenerationException(Throwable cause) {
        super(cause);
    }
    
    public TokenGenerationException(String mess, Throwable cause) {
        super(mess, cause);
    }
}
