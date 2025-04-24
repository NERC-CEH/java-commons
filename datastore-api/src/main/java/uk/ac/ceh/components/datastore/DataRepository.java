package uk.ac.ceh.components.datastore;

import java.util.List;

/**
 *
 * @author cjohn
 */
public interface DataRepository<A extends DataAuthor> {
    DataRevision<A> getLatestRevision() throws DataRepositoryException;
    
    DataDocument getData(String filename) throws DataRepositoryException;
    DataDocument getData(String version, String filename) throws DataRepositoryException;
    List<String> getFiles() throws DataRepositoryException;
    List<String> getFiles(String revision) throws DataRepositoryException;
    List<DataRevision<A>> getRevisions(String filename) throws DataRepositoryException;
    List<DataRevision<A>> getRevisions(long timeLimitInSecond, String commitMsg) throws DataRepositoryException;

    DataOngoingCommit<A> submitData(String filename, DataWriter writer);
    DataOngoingCommit<A> deleteData(String toDelete);
}
