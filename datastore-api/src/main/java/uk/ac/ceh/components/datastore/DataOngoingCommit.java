package uk.ac.ceh.components.datastore;

/**
 *
 * @author cjohn
 */
public interface DataOngoingCommit<A extends DataAuthor> {
    DataOngoingCommit<A> submitData(String filename, DataWriter toWrite);
    DataOngoingCommit<A> deleteData(String toDelete);
    DataRevision<A> commit(A author, String message) throws DataRepositoryException;
}
