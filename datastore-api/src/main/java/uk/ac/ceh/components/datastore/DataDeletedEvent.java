package uk.ac.ceh.components.datastore;

import java.util.Collection;

/**
 *
 * @author cjohn
 */
public class DataDeletedEvent {
    private final Collection<String> dataAdded;

    public DataDeletedEvent(Collection<String> dataAdded) {
        this.dataAdded = dataAdded;
    }
    
    public Collection<String> getFilenames() {
        return dataAdded;
    }
}
