package uk.ac.ceh.components.datastore.git;

import java.io.IOException;
import java.io.InputStream;
import lombok.Data;
import org.eclipse.jgit.lib.ObjectLoader;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataRepositoryException;

/**
 *
 * @author cjohn
 */
@Data
public class GitDataDocument implements DataDocument {
    private final String filename, revision;
    private final ObjectLoader loader;
    
    @Override
    public InputStream getInputStream() throws IOException, DataRepositoryException {
        return loader.openStream();
    }

    @Override
    public long length() {
        return loader.getSize();
    }
}
