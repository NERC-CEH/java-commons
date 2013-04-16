
package uk.ac.ceh.components.datastore;

import java.io.File;

/**
 *
 * @author cjohn
 */
public class IndexFileEvent {
    private final File toIndex;

    public IndexFileEvent(File toIndex) {
        this.toIndex = toIndex;
    }
    
    public File getFileToIndex() {
        return toIndex;
    }
}
