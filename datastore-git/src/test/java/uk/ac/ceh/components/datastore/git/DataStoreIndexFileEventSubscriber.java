package uk.ac.ceh.components.datastore.git;

import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.List;
import uk.ac.ceh.components.datastore.IndexFileEvent;

/**
 *
 * @author cjohn
 */
public class DataStoreIndexFileEventSubscriber {
    final List<IndexFileEvent> events = new ArrayList<>();
    
    @Subscribe
    public void addIndexEvent(IndexFileEvent event) {
        events.add(event);
    }    
}
