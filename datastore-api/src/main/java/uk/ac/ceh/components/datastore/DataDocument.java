package uk.ac.ceh.components.datastore;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author cjohn
 */
public interface DataDocument {
    InputStream getInputStream() throws IOException, DataRepositoryException;
    
    /**
     * The length of the input stream or -1 if the length can not be determined
     * @return 
     */
    long length();
    
    String getRevision();
    String getFilename();
}
