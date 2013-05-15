package uk.ac.ceh.components.userstore.crowd;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.components.userstore.User;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Christopher Johnson
 */
@Ignore
public class CrowdGroupStoreTest {
    private boolean performCleanUp;
    private CrowdGroupStore<User> groupstore;
    
    @Before
    public void createCrowdUserStore() throws URISyntaxException {
        this.groupstore = new CrowdGroupStore<>(
                new URI("http://localhost:8095/crowd/rest/usermanagement/latest"), 
                "testapp", 
                "password");
        
        assertTrue("The crowd groupstore should not have any groups in to start with", performCleanUp = groupstore.getAllGroups().isEmpty());
    }
    
    @After
    public void removeSideEffects() throws UnknownUserException {
        if(performCleanUp) {
            for(Group currGroup: groupstore.getAllGroups()) {
                groupstore.deleteGroup(currGroup.getName());
            }
        }
    }
    
    @Test
    public void createGroup() {
        //Given
        String groupname = "newGroup";
        String description = "My New Group";
        
        //When
        Group createdGroup = groupstore.createGroup(groupname, description);
        
        //Then
        Group obtainedGroup = groupstore.getGroup(groupname);
        assertEquals("Expected the created group to be equal to the one obtained", createdGroup, obtainedGroup);
        assertEquals("Expected to find a group with the same name as the one created", groupname, obtainedGroup.getName());
    }
    
    @Test
    public void createManyGroups() {
        //Given
        //created
        //When
        
        //Then
    }
    
    @Test
    public void createExistingGroup() {
        
    }
    
    @Test
    public void assignGroupToUser() {
        
    }
    
    @Test
    public void removeGroupFromUser() {
        
    }
    
    @Test
    public void assignManyGroupsToUser() {
        
    }
    
    @Test
    public void checkGroupWhichDoesNotExistsDoesNotExist() {
        
    }
    
    @Test
    public void checkGroupWhichDoesExistExists() {
        
    }
    
    @Test
    public void isGroupDeleteable() {
        
    }
    
    @Test
    public void isGroupWhichDoesNotExistDeletable() {
        
    }
    
    @Test
    public void updateGroupWhichExists() {
        
    }
    
    @Test
    public void updateGroupWhichDoesNotExist() {
        
    }
    
    @Test
    public void deleteGroupWhichExists() {
        
    }
    
    @Test
    public void deleteGroupWhichDoesNotExist() {
        
    }
    
    @Test
    public void deleteGroupWhichIsAlreadyInUse() {
        
    }
    
    
}
