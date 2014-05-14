package uk.ac.ceh.components.datastore;

import java.util.List;
import java.util.Map;

/**
 *
 * @author cjohn
 */
public interface DataRepository<T, A extends DataAuthor> {
    T getData(String filename) throws DataRepositoryException;
    T getData(String version, String filename) throws DataRepositoryException;
    DataRevision<A> deleteData(A author, String message, List<String> toDelete) throws DataRepositoryException;
    DataRevision<A> submitData(A author, String message, Map<String, T> data) throws DataRepositoryException;
    List<String> getFiles() throws DataRepositoryException;
    List<String> getFiles(String revision) throws DataRepositoryException;
    List<DataRevision<A>> getRevisions(String filename) throws DataRepositoryException;
}
