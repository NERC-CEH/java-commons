package uk.ac.ceh.components.datastore;

import java.io.InputStream;
import java.util.List;

/**
 *
 * @author cjohn
 */
public interface DataRepository<A extends DataAuthor> {
    InputStream getData(String filename) throws DataRepositoryException;
    InputStream getData(String version, String filename) throws DataRepositoryException;
    List<String> getFiles() throws DataRepositoryException;
    List<String> getFiles(String revision) throws DataRepositoryException;
    List<DataRevision<A>> getRevisions(String filename) throws DataRepositoryException;
    
    OngoingDataCommit<A> submitData(String filename, DataWriter writer);
    OngoingDataCommit<A> deleteData(String toDelete);
}
