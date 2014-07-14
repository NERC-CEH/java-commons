package uk.ac.ceh.components.datastore.git;

import uk.ac.ceh.components.datastore.DataRepositoryException;

/**
 *
 * @author cjohn
 */
public class GitRevisionNotFoundException extends DataRepositoryException {

    public GitRevisionNotFoundException(String mess) {
        super(mess);
    }
    
    public GitRevisionNotFoundException(String mess, Throwable ex) {
        super(mess, ex);
    }

    public GitRevisionNotFoundException(Throwable ex) {
        super(ex);
    }
}
