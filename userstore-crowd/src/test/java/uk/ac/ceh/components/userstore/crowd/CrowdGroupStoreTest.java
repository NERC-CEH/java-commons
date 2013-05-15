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
    public void createSecondGroups() {
        //Given
        groupstore.createGroup("testGroup", "group description");
        
        //When
        groupstore.createGroup("secondGroup", "new description");
        
        //Then
        assertEquals("Expected to find two groups", 2, groupstore.getAllGroups().size());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void createExistingGroup() {
        //Given
        groupstore.createGroup("testGroup", "group description");
        
        //When
        groupstore.createGroup("testGroup", "new description");
        
        //Then
        fail("Didn't expect to be able to create the group with the same name");
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
        //Given
        String groupWhichDoesNotExist = "groupWhichDoesNotExist";
        
        //When
        boolean groupInExistance = groupstore.isGroupInExistance(groupWhichDoesNotExist);
        
        //Then
        assertFalse("Didn't expect group to exist", groupInExistance);
    }
    
    @Test
    public void checkGroupWhichDoesExistExists() {
        //Given
        groupstore.createGroup("testgroup", "No description");
        
        //When
        boolean groupInExistance = groupstore.isGroupInExistance("testgroup");
        
        //Then
        assertTrue("Expected that group exists", groupInExistance);
    }
    
    @Test
    public void isGroupDeleteable() {
        //Given
        groupstore.createGroup("testgroup", "No description");
        
        //When
        boolean deleteable = groupstore.isGroupDeletable("testgroup");
        
        //Then
        assertTrue("Expected that group should be deletable", deleteable);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void isGroupWhichDoesNotExistDeletable() {
        //Given
        String groupnameWhichDoesNotExist = "testgroup";
        
        //When
        boolean deleteable = groupstore.isGroupDeletable(groupnameWhichDoesNotExist);
        
        //Then
        fail("Expected to fail with an exception");
    }
    
    @Test
    public void updateGroupWhichExists() {
        //Given
        groupstore.createGroup("testgroup", "No description");
        
        //When
        groupstore.updateGroup("testgroup", "New Description");
        Group updatedGroup = groupstore.getGroup("testgroup");
        
        //Then
        assertEquals("Expected to find the updated description", "New Description", updatedGroup.getDescription());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void updateGroupWhichDoesNotExist() {
        //Given
        String groupWhichDoesNotExist = "falseGroup";
        
        //When
        groupstore.updateGroup(groupWhichDoesNotExist, "New group description");
        
        //Then
        fail("Expected to fail, no group should exist to update");
    }
    
    @Test
    public void deleteGroupWhichExists() {
        //Given
        groupstore.createGroup("testgroup", "group description");
        
        //When
        boolean deleteGroup = groupstore.deleteGroup("testgroup");
        
        //Then
        assertTrue("Expected that group would be deleted", deleteGroup);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void deleteGroupWhichDoesNotExist() {
        //Given
        String groupWhichDoesNotExist = "testgroup";
        
        //When
        boolean deleteGroup = groupstore.deleteGroup(groupWhichDoesNotExist);
        
        //Then
        fail("Expected not to be able to delete group");
    }
    
    @Test
    public void deleteGroupWhichIsAlreadyInUse() {
        
    }
    
    
}
