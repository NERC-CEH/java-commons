package uk.ac.ceh.components.userstore.inmemory;

/**
 * Simple exception to be thrown when a user builder can not be created
 * @author cjohn
 */
public class UnableToConstructUserBuilderException extends RuntimeException {

    public UnableToConstructUserBuilderException() {
        super();
    }
    
    public UnableToConstructUserBuilderException(String mess) {
        super(mess);
    }
    
    public UnableToConstructUserBuilderException(Throwable cause) {
        super(cause);
    }
    
    public UnableToConstructUserBuilderException(String mess, Throwable cause) {
        super(mess, cause);
    }
}
