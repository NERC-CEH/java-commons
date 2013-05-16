package uk.ac.ceh.components.userstore.crowd;

import org.junit.rules.ExternalResource;
import uk.ac.ceh.components.userstore.Group;

/**
 *
 * @author Christopher Johnson
 */
public class TestCrowdGroupStoreResource extends ExternalResource {
    private final CrowdGroupStore<TestUser> groupstore;
    
    public TestCrowdGroupStoreResource(CrowdApplicationCredentials crowdCred) {
        this.groupstore = new CrowdGroupStore<>(crowdCred); 
    }
    
    @Override
    public void before() {
        if(!groupstore.getAllGroups().isEmpty())
            throw new AssertionError("The crowd userstore should not have any users in to start with");
    }
    
    public CrowdGroupStore<TestUser> groupstore() {
        return groupstore;
    }
    
    @Override
    public void after() {
        for(Group currGroup: groupstore.getAllGroups()) {
            groupstore.deleteGroup(currGroup.getName());
        }
    }
}
