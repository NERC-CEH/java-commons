package uk.ac.ceh.components.datastore;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 *
 * @author cjohn
 */
public interface DataRepository<A extends DataAuthor> {
    InputStream getData(String filename) throws DataRepositoryException;
    InputStream getData(String version, String filename) throws DataRepositoryException;
    DataRevision<A> deleteData(A author, String message, List<String> toDelete) throws DataRepositoryException;
    DataRevision<A> submitData(A author, String message, Map<String, InputStream> data) throws DataRepositoryException;
    List<DataRevision<A>> getRevisions(String filename) throws DataRepositoryException;
    
    void addDataSubmissionListener(DataSubmissionListener<A> listener);
    boolean removeDataSubmissionListener(DataSubmissionListener<A> listener);
    void triggerReindex();
}
