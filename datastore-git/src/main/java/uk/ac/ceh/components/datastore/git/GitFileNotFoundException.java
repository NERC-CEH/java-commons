package uk.ac.ceh.components.datastore.git;

import uk.ac.ceh.components.datastore.DataRepositoryException;

/**
 *
 * @author cjohn
 */
public class GitFileNotFoundException extends DataRepositoryException {

    public GitFileNotFoundException(String mess) {
        super(mess);
    }

    public GitFileNotFoundException(Throwable ex) {
        super(ex);
    }
}
