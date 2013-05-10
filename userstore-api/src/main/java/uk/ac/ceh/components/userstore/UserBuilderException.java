package uk.ac.ceh.components.userstore;

/**
 * The following exception will be thrown if it is not possible to create or set
 * a method on a user from some UserBuilder
 * @author Christopher Johnson
 */
public class UserBuilderException extends RuntimeException {
    public UserBuilderException() {
        super();
    }
    
    public UserBuilderException(String mess) {
        super(mess);
    }
    
    public UserBuilderException(Throwable cause) {
        super(cause);
    }
    
    public UserBuilderException(String mess, Throwable cause) {
        super(mess, cause);
    }
}