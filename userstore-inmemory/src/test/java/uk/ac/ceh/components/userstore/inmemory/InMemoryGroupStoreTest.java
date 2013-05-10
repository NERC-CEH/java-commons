package uk.ac.ceh.components.userstore.inmemory;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.ac.ceh.components.userstore.User;

/**
 *
 * @author CJOHN
 */
public class InMemoryGroupStoreTest {
    private InMemoryGroupStore<InMemoryTestUser> groupStore;
    
    @Before
    public void createEmptyInMemoryUserStore() {
        groupStore = new InMemoryGroupStore<>();
    }
    
    @Test
    public void createNewGroup() {
        //Given
        String groupname = "testgroup";
        String description = "testgroup-description";
        
        //When
        groupStore.createGroup(groupname, description);
        
        //Then
        InMemoryGroup group = groupStore.getGroup("testgroup");
        assertEquals("Expected a group with correctname", "testgroup", group.getName());
        assertEquals("Expected a group with correct description", "testgroup-description", 
                group.getDescription());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void getGroupThatDoesNotExist() {
        //Given
        String nonExistantGroupname = "newGroup";
        
        //When
        InMemoryGroup group = groupStore.getGroup(nonExistantGroupname);
        
        //Then
        fail("Didn't expect to be able to get a group");
    }
    
    @Test
    public void checkToSeeIfGroupExists() {
        //Given
        String groupname = "testgroup";
        String description = "testgroup description";
        
        //When
        groupStore.createGroup(groupname, description);
        
        //Then
        boolean groupInExistance = groupStore.isGroupInExistance("testgroup");
        assertTrue("Expected to find the group i just created", groupInExistance);
    }
    
    
    @Test
    public void checkToSeeIfGroupDoesntExists() {
        //Given
        String groupname = "testgroup";
        
        //When
        //Nothing happens
        
        //Then
        boolean groupInExistance = groupStore.isGroupInExistance("testgroup");
        assertFalse("Expected not to find group", groupInExistance);
    }
    
    @Test
    public void updateAGroupsDescription() {
        //Given
        groupStore.createGroup("groupname", "original description");
        
        //When
        groupStore.updateGroup("groupname", "new description");
        
        //Then
        InMemoryGroup group = groupStore.getGroup("groupname");
        assertEquals("Expected the new description", "new description", group.getDescription());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void updateAGroupsWhichDoesntExists() {
        //Given
        String groupnameToUpdate = "groupname";
        
        //When
        groupStore.updateGroup(groupnameToUpdate, "new description");
        
        //Then
        fail("Didn't expect to be able to update a group which doesn't exists");
    }
    
    @Test
    public void deleteAUnusedGroup() {
        //Given
        groupStore.createGroup("todelete", "nothing of importance");
        
        //When
        groupStore.deleteGroup("todelete");
        
        //Then
        assertFalse("Group should now be deleted", groupStore.isGroupInExistance("todelete"));
    }
    
    @Test
    public void checkAllGroupsAfterAddingNewGroups() {
        //Given
        String groupOneName = "groupname";
        String groupOneDescription = "groupname";
        String groupTwoName = "groupname2";
        String groupTwoDescription = "groupname2";
        
        //When
        groupStore.createGroup(groupOneName, groupOneDescription);
        groupStore.createGroup(groupTwoName, groupTwoDescription);
        
        //Then
        assertEquals("Expected to find two groups", 2, groupStore.getAllGroups().size());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void attemptToAddAGroupWhichAlreadyExists() {
        //Given
        groupStore.createGroup("groupname", "description");
        
        //When
        groupStore.createGroup("groupname", "I should not work");
        
        //Then
        fail("Should not have been able to create a group which already exists");
    }
    
    @Test
    public void deleteASoleUsedGroup() {
        //Given
        InMemoryTestUser user = new InMemoryTestUser("testUser");
        groupStore.createGroup("groupname", "description");
        groupStore.grantGroupToUser(user, "groupname");
        
        //When
        boolean successOnDeletingGroup = groupStore.deleteGroup("groupname");
        
        //Then
        assertTrue("expected to be able to delete the group", successOnDeletingGroup);
        assertEquals("expected no groups on user", 0, groupStore.getGroups(user).size());
        assertEquals("expected no groups in store", 0, groupStore.getAllGroups().size());
    }
    
    @Test
    public void deleteAUsedGroup() {
        //Given
        InMemoryTestUser user = new InMemoryTestUser("testUser");
        groupStore.createGroup("groupname", "description");
        groupStore.createGroup("groupname2", "description2");
        groupStore.grantGroupToUser(user, "groupname");
        groupStore.grantGroupToUser(user, "groupname2");
        
        //When
        boolean successOnDeletingGroup = groupStore.deleteGroup("groupname");
        
        //Then
        assertTrue("expected to be able to delete the group", successOnDeletingGroup);
        assertEquals("expected one group on user", 1, groupStore.getGroups(user).size());
        assertEquals("expected one group in store", 1, groupStore.getAllGroups().size());
    }
    
    @Test
    public void grantUserGroupHeAlreadyHas() {
        //Given
        InMemoryTestUser user = new InMemoryTestUser("testUser");
        groupStore.createGroup("groupname", "description");
        groupStore.grantGroupToUser(user, "groupname");
        
        //When
        boolean grantGroupToUser = groupStore.grantGroupToUser(user, "groupname");
        
        //Then
        assertFalse("expected to not have to add the group", grantGroupToUser);
        assertEquals("expected no groups on user", 1, groupStore.getGroups(user).size());
    }
    
    @Test
    public void revokeAUsersGroup() {
        //Given
        InMemoryTestUser user = new InMemoryTestUser("testUser");
        groupStore.createGroup("groupname", "description");
        groupStore.createGroup("groupname2", "description2");
        groupStore.grantGroupToUser(user, "groupname");
        groupStore.grantGroupToUser(user, "groupname2");
        
        //When
        boolean successOnRemovingGroup = groupStore.revokeGroupFromUser(user, "groupname");
        
        //Then
        assertTrue("expected to be able to delete the group", successOnRemovingGroup);
        assertEquals("expected no groups on user", 1, groupStore.getGroups(user).size());
    }
}
