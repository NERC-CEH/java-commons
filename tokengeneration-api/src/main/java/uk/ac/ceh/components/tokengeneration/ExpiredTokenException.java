package uk.ac.ceh.components.tokengeneration;

/**
 * The following exception will be thrown if an expired token is attempted to be
 * used to obtain a user
 * @author Christopher Johnson
 */
public class ExpiredTokenException extends Exception {
    public ExpiredTokenException() {
        super();
    }
    
    public ExpiredTokenException(String mess) {
        super(mess);
    }
    
    public ExpiredTokenException(Throwable cause) {
        super(cause);
    }
    
    public ExpiredTokenException(String mess, Throwable cause) {
        super(mess, cause);
    }
}
