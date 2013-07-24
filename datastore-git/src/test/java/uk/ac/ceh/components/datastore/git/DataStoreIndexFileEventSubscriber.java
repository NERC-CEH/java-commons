package uk.ac.ceh.components.datastore.git;

import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.List;
import uk.ac.ceh.components.datastore.DataSubmittedEvent;

/**
 *
 * @author cjohn
 */
public class DataStoreIndexFileEventSubscriber {
    final List<DataSubmittedEvent> events = new ArrayList<>();
    
    @Subscribe
    public void addIndexEvent(DataSubmittedEvent event) {
        events.add(event);
    }    
}
