package uk.ac.ceh.components.userstore.crowd;

import org.junit.rules.ExternalResource;
import uk.ac.ceh.components.userstore.Group;

/**
 * Defines the test external resource for interacting with an instance of the 
 * CrowdGroup store. This resource will ensure that no groups are present before 
 * interacting with Crowd.
 * 
 * After a test has been performed, this resource will remove any created groups
 * @author Christopher Johnson
 */
public class TestCrowdGroupStoreResource extends ExternalResource {
    private final CrowdGroupStore<TestUser> groupstore;
    
    public TestCrowdGroupStoreResource(CrowdApplicationCredentials crowdCred) {
        this.groupstore = new CrowdGroupStore<>(crowdCred); 
    }
    
    /**
     * Checks to see if no groups are present before allowing tests to be performed
     * @throws AssertionError if groups are present
     */
    @Override
    public void before() throws AssertionError {
        if(!groupstore.getAllGroups().isEmpty())
            throw new AssertionError("The crowd userstore should not have any users in to start with");
    }
    
    /**
     * @return the groupstore to use for testing
     */
    public CrowdGroupStore<TestUser> groupstore() {
        return groupstore;
    }
    
    /**
     * Removes any created groups after tests have been performed
     */
    @Override
    public void after() {
        for(Group currGroup: groupstore.getAllGroups()) {
            groupstore.deleteGroup(currGroup.getName());
        }
    }
}
