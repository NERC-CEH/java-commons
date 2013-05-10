package uk.ac.ceh.components.userstore;

/**
 * The following exception will be thrown if it is not possible to read a 
 * property off of a User object
 * @author Christopher Johnson
 */
public class UserAttributeReaderException extends IllegalArgumentException {
    public UserAttributeReaderException() {
        super();
    }
    
    public UserAttributeReaderException(String mess) {
        super(mess);
    }
    
    public UserAttributeReaderException(Throwable cause) {
        super(cause);
    }
    
    public UserAttributeReaderException(String mess, Throwable cause) {
        super(mess, cause);
    }
}