package uk.ac.ceh.components.datastore;

import java.util.Collection;

/**
 *
 * @author cjohn
 */
public class DataSubmittedEvent {
    private final Collection<String> dataAdded;

    public DataSubmittedEvent(Collection<String> dataAdded) {
        this.dataAdded = dataAdded;
    }
    
    public Collection<String> getFilenames() {
        return dataAdded;
    }
}
