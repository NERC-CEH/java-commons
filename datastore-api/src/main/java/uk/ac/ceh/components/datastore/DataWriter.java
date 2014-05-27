package uk.ac.ceh.components.datastore;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author cjohn
 */
public interface DataWriter {
    void write(OutputStream out) throws IOException, DataRepositoryException;
}
