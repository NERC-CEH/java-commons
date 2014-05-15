package uk.ac.ceh.components.datastore;

/**
 *
 * @author cjohn
 */
public interface OngoingDataCommit<A extends DataAuthor> {
    OngoingDataCommit<A> submitData(String filename, DataWriter toWrite);
    OngoingDataCommit<A> deleteData(String toDelete);
    DataRevision<A> commit(A author, String message) throws DataRepositoryException;
}
