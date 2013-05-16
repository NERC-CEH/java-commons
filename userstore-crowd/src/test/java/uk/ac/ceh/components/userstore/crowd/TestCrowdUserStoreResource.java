package uk.ac.ceh.components.userstore.crowd;

import org.junit.rules.ExternalResource;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import uk.ac.ceh.components.userstore.UnknownUserException;

/**
 * Defines the test external resource for interacting with an instance of the 
 * CrowdUser store. This resource will ensure that no users are present before 
 * interacting with Crowd.
 * 
 * After a test has been performed, this resource will remove any created users
 * @author Christopher Johnson
 */
public class TestCrowdUserStoreResource extends ExternalResource {
    private final CrowdUserStore<TestUser> userstore;
    
    public TestCrowdUserStoreResource(CrowdApplicationCredentials crowdCred) {
        AnnotatedUserHelper annotatedUserHelper = new AnnotatedUserHelper(TestUser.class);
        this.userstore = new CrowdUserStore<>(crowdCred, annotatedUserHelper, annotatedUserHelper); 
    }
    
    /**
     * Checks to see that no users are present before running tests
     * @throws AssertionError if users are present in the crowdstore
     */
    @Override
    public void before() throws AssertionError {
        if(!userstore.getAllUsers().isEmpty())
            throw new AssertionError("The crowd userstore should not have any users in to start with");
    }
    
    /**
     * @return the userstore for testing
     */
    public CrowdUserStore<TestUser> userstore() {
        return userstore;
    }
    
    /**
     * Deletes any created users after a test has been performed
     */
    @Override
    public void after() {
        for(TestUser currUser: userstore.getAllUsers()) {
            try {
                userstore.deleteUser(currUser.getUsername());
            } catch (UnknownUserException ex) {} //user not known for deleting. Just ignore
        }
    }
}
