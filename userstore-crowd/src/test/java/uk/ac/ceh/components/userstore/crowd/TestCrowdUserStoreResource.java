package uk.ac.ceh.components.userstore.crowd;

import org.junit.rules.ExternalResource;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import uk.ac.ceh.components.userstore.UnknownUserException;

/**
 *
 * @author Christopher Johnson
 */
public class TestCrowdUserStoreResource extends ExternalResource {
    private final CrowdUserStore<TestUser> userstore;
    
    public TestCrowdUserStoreResource(CrowdApplicationCredentials crowdCred) {
        AnnotatedUserHelper annotatedUserHelper = new AnnotatedUserHelper(TestUser.class);
        this.userstore = new CrowdUserStore<>(crowdCred, annotatedUserHelper, annotatedUserHelper); 
    }
    
    @Override
    public void before() {
        if(!userstore.getAllUsers().isEmpty())
            throw new AssertionError("The crowd userstore should not have any users in to start with");
    }
    
    public CrowdUserStore<TestUser> userstore() {
        return userstore;
    }
    
    @Override
    public void after() {
        for(TestUser currUser: userstore.getAllUsers()) {
            try {
                userstore.deleteUser(currUser.getUsername());
            } catch (UnknownUserException ex) {} //user not known for deleting. Just ignore
        }
    }
}
