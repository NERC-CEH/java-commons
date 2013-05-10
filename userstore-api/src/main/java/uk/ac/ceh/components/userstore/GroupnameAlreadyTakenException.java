package uk.ac.ceh.components.userstore;

/**
 * The following exception will be thrown if a group is attempted to be added to
 * a group store but that groups groupname is already taken
 * @author Christopher Johnson
 */
public class GroupnameAlreadyTakenException extends Exception {
    public GroupnameAlreadyTakenException() {
        super();
    }
    
    public GroupnameAlreadyTakenException(String mess) {
        super(mess);
    }
    
    public GroupnameAlreadyTakenException(Throwable cause) {
        super(cause);
    }
    
    public GroupnameAlreadyTakenException(String mess, Throwable cause) {
        super(mess, cause);
    }
}