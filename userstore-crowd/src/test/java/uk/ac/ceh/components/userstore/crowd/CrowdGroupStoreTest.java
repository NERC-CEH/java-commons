package uk.ac.ceh.components.userstore.crowd;

import java.util.List;
import org.junit.Test;
import uk.ac.ceh.components.userstore.Group;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Rule;
import uk.ac.ceh.components.userstore.InvalidCredentialsException;
import uk.ac.ceh.components.userstore.UsernameAlreadyTakenException;

/**
 *
 * @author Christopher Johnson
 */

public class CrowdGroupStoreTest {
    private CrowdApplicationCredentials crowdCred = new CrowdApplicationCredentials(
                System.getProperty("crowd-url"),
                System.getProperty("crowd-application"),
                System.getProperty("crowd-password"));
    
    @Rule
    public TestCrowdGroupStoreResource groupStoreResource = new TestCrowdGroupStoreResource(crowdCred);   
    
    @Rule
    public TestCrowdUserStoreResource userStoreResource = new TestCrowdUserStoreResource(crowdCred);
    
    @Test
    public void createGroup() {
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        String groupname = "newGroup";
        String description = "My New Group";
        
        //When
        Group createdGroup = groupstore.createGroup(groupname, description);
        
        //Then
        Group obtainedGroup = groupstore.getGroup(groupname);
        assertEquals("Expected the created group to be equal to the one obtained", createdGroup, obtainedGroup);
        assertEquals("Expected to find a group with the same name as the one created", groupname, obtainedGroup.getName());
        assertEquals("Expected to find a group with the same description as the one created", description, obtainedGroup.getDescription());
    }
    
    @Test
    public void createSecondGroups() {
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        groupstore.createGroup("testGroup", "group description");
        
        //When
        groupstore.createGroup("secondGroup", "new description");
        
        //Then
        List<Group> allGroups = groupstore.getAllGroups();
        assertEquals("Expected to find two groups", 2, allGroups.size());
    }
    
    @Test
    public void checkAllGroupsAreFullyPopulated() {
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        
        //When
        Group populatedGroup = groupstore.createGroup("testgroup", "description which should be returned");
        List<Group> allGroups = groupstore.getAllGroups();
        
        //Then
        assertTrue("Expected fully populated group to be in allGroups", allGroups.contains(populatedGroup));
    }
    
    @Test
    public void checkUsersGroupsAreFullyPopulated() throws UsernameAlreadyTakenException, InvalidCredentialsException {
        //Given
        CrowdUserStore<TestUser> userstore = userStoreResource.userstore();
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        
        TestUser registeredUser = new TestUser();
        registeredUser.setUsername("testuser");
        registeredUser.setFirstname("firstname");
        registeredUser.setEmail("test@user.com");
        userstore.addUser(registeredUser, "testpassword");
        
        Group populatedGroup = groupstore.createGroup("testgroup", "description which should be returned");
        groupstore.grantGroupToUser(registeredUser, "testgroup");
        
        //When
        List<Group> usersGroups = groupstore.getGroups(registeredUser);
        
        //Then
        assertTrue("Expected fully populated group to be in usersGroups", usersGroups.contains(populatedGroup));
    }
    
    @Test
    public void checkCreatedGroupIsPopulated() {
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        String groupname = "testgroup";
        String description = "description which should be returned";
        
        //When
        Group populatedGroup = groupstore.createGroup(groupname, description);
        
        //Then
        assertEquals("Expected groupname to be set", groupname, populatedGroup.getName());
        assertEquals("Expected description to be set", description, populatedGroup.getDescription());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void createExistingGroup() {
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        groupstore.createGroup("testGroup", "group description");
        
        //When
        groupstore.createGroup("testGroup", "new description");
        
        //Then
        fail("Didn't expect to be able to create the group with the same name");
    }
    
    @Test
    public void assignGroupToUser() throws UsernameAlreadyTakenException, InvalidCredentialsException {
        //Given
        CrowdUserStore<TestUser> userstore = userStoreResource.userstore();
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        
        TestUser registeredUser = new TestUser();
        registeredUser.setUsername("testuser");
        registeredUser.setFirstname("firstname");
        registeredUser.setEmail("test@user.com");
        userstore.addUser(registeredUser, "testpassword");
        
        groupstore.createGroup("testgroup", "Test group for testing that user can be added to group");
        
        //When
        groupstore.grantGroupToUser(registeredUser, "testgroup");
        List<Group> usersGroups = groupstore.getGroups(registeredUser);
                             
        //Then
        assertEquals("Expected one group", 1, usersGroups.size());
        assertEquals("Expected group to be called testgroup", "testgroup", usersGroups.get(0).getName());
    }
    
    @Test
    public void removeGroupFromUser() throws UsernameAlreadyTakenException, InvalidCredentialsException {
        //Given
        CrowdUserStore<TestUser> userstore = userStoreResource.userstore();
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        
        TestUser registeredUser = new TestUser();
        registeredUser.setUsername("testuser");
        registeredUser.setFirstname("firstname");
        registeredUser.setEmail("test@user.com");
        userstore.addUser(registeredUser, "testpassword");
        
        groupstore.createGroup("testgroup", "Test group for testing that user can be added to group");
        groupstore.grantGroupToUser(registeredUser, "testgroup");
        
        //When
        groupstore.revokeGroupFromUser(registeredUser, "testgroup");
                             
        //Then
        assertEquals("Expected no groups", 0, groupstore.getGroups(registeredUser).size());
    }
    
    @Test
    public void assignManyGroupsToUser() throws UsernameAlreadyTakenException, InvalidCredentialsException {
        //Given
        CrowdUserStore<TestUser> userstore = userStoreResource.userstore();
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        
        TestUser registeredUser = new TestUser();
        registeredUser.setUsername("testuser");
        registeredUser.setFirstname("firstname");
        registeredUser.setEmail("test@user.com");
        userstore.addUser(registeredUser, "testpassword");
        
        groupstore.createGroup("testgroup", "Test group for testing that user can be added to group");
        groupstore.createGroup("testgroup2", "Test group for testing that user can be added to group");
        
        //When
        groupstore.grantGroupToUser(registeredUser, "testgroup");
        groupstore.grantGroupToUser(registeredUser, "testgroup2");
                             
        //Then
        assertEquals("Expected two groups", 2, groupstore.getGroups(registeredUser).size());
    }
    
    @Test
    public void checkGroupWhichDoesNotExistsDoesNotExist() {
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        String groupWhichDoesNotExist = "groupWhichDoesNotExist";
        
        //When
        boolean groupInExistance = groupstore.isGroupInExistance(groupWhichDoesNotExist);
        
        //Then
        assertFalse("Didn't expect group to exist", groupInExistance);
    }
    
    @Test
    public void checkGroupWhichDoesExistExists() {
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        groupstore.createGroup("testgroup", "No description");
        
        //When
        boolean groupInExistance = groupstore.isGroupInExistance("testgroup");
        
        //Then
        assertTrue("Expected that group exists", groupInExistance);
    }
    
    @Test
    public void isGroupDeleteable() {
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        groupstore.createGroup("testgroup", "No description");
        
        //When
        boolean deleteable = groupstore.isGroupDeletable("testgroup");
        
        //Then
        assertTrue("Expected that group should be deletable", deleteable);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void isGroupWhichDoesNotExistDeletable() {
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        String groupnameWhichDoesNotExist = "testgroup";
        
        //When
        boolean deleteable = groupstore.isGroupDeletable(groupnameWhichDoesNotExist);
        
        //Then
        fail("Expected to fail with an exception");
    }
    
    @Test
    public void updateGroupWhichExists() {
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
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
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        String groupWhichDoesNotExist = "falseGroup";
        
        //When
        groupstore.updateGroup(groupWhichDoesNotExist, "New group description");
        
        //Then
        fail("Expected to fail, no group should exist to update");
    }
    
    @Test
    public void deleteGroupWhichExists() {
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        groupstore.createGroup("testgroup", "group description");
        
        //When
        boolean deleteGroup = groupstore.deleteGroup("testgroup");
        
        //Then
        assertTrue("Expected that group would be deleted", deleteGroup);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void deleteGroupWhichDoesNotExist() {
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        String groupWhichDoesNotExist = "testgroup";
        
        //When
        boolean deleteGroup = groupstore.deleteGroup(groupWhichDoesNotExist);
        
        //Then
        fail("Expected not to be able to delete group");
    }
    
    @Test
    public void deleteGroupWhichIsAlreadyInUse() throws UsernameAlreadyTakenException, InvalidCredentialsException {
        //Given
        CrowdUserStore<TestUser> userstore = userStoreResource.userstore();
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        
        TestUser registeredUser = new TestUser();
        registeredUser.setUsername("testuser");
        registeredUser.setFirstname("firstname");
        registeredUser.setEmail("test@user.com");
        userstore.addUser(registeredUser, "testpassword");
        
        groupstore.createGroup("testgroup", "Test group for testing that user can be added to group");
        groupstore.grantGroupToUser(registeredUser, "testgroup");
        
        //When
        groupstore.deleteGroup("testgroup");
        
        //Then
        assertEquals("Expected no groups", 0, groupstore.getGroups(registeredUser).size());
    }
    
    @Test
    public void grantGroupToGroup(){
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        groupstore.createGroup("parentGroup", "Test Parent Group");
        groupstore.createGroup("childgroup", "Test Child Group");
        
        //When
        groupstore.grantGroupToGroup("childgroup", "parentGroup");
        List<Group> childGroupsForGroup = groupstore.getChildGroupsForGroup("parentGroup");
        List<Group> parentGroupsForGroup = groupstore.getParentGroupsForGroup("childgroup");
        
        //Then
        assertEquals("Expected parentGroup to have child", "childgroup", childGroupsForGroup.get(0).getName());
        assertEquals("Expected childgroup to have parent", "parentGroup", parentGroupsForGroup.get(0).getName());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void grantGroupToGroupWhichDoesntExist() {
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        groupstore.createGroup("parentGroup", "Test Parent Group");

        //When
        groupstore.grantGroupToGroup("fakegroup", "parentGroup");
        
        //Then
        fail("Expected to fail granting group");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void grantGroupWhichDoesntExistToGroup() {
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        groupstore.createGroup("childGroup", "Test Child Group");

        //When
        groupstore.grantGroupToGroup("childGroup", "fakegroup");
        
        //Then
        fail("Expected to fail granting group");
    }
    
    @Test
    public void grantUserToAGroupOfAGroup() throws UsernameAlreadyTakenException, InvalidCredentialsException {
        //Given
        CrowdUserStore<TestUser> userstore = userStoreResource.userstore();       
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        
        groupstore.createGroup("parentGroup", "Test Parent Group");
        groupstore.createGroup("childGroup", "Test Child Group");
        groupstore.grantGroupToGroup("childgroup", "parentGroup");
        
        TestUser registeredUser = new TestUser();
        registeredUser.setUsername("testuser");
        registeredUser.setFirstname("firstname");
        registeredUser.setEmail("test@user.com");
        userstore.addUser(registeredUser, "testpassword");
        
        //When
        groupstore.grantGroupToUser(registeredUser, "childgroup");
        
        List<Group> groups = groupstore.getGroups(registeredUser);
        List<Group> directGroups = groupstore.getDirectGroups(registeredUser);
        
        //Then
        assertEquals("Expected to be a member of two groups", 2, groups.size());
        assertEquals("Expected to be a direct member of one group", 1, directGroups.size());
    }
    
    @Test
    public void revokeGroupFromGroup() {
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        
        groupstore.createGroup("parentGroup", "Test Parent Group");
        groupstore.createGroup("childGroup", "Test Child Group");
        groupstore.grantGroupToGroup("childGroup", "parentGroup");
        
        //When
        groupstore.revokeGroupFromGroup("childGroup", "parentGroup");
        List<Group> childGroupsForGroup = groupstore.getChildGroupsForGroup("parentGroup");
        List<Group> parentGroupsForGroup = groupstore.getParentGroupsForGroup("childGroup");
        
        //Then
        assertEquals("Expected no groups to be part of the child", 0, childGroupsForGroup.size());
        assertEquals("Expected no groups to be part of the parent", 0, parentGroupsForGroup.size());
    }
    
    @Test
    public void checkDirectParentGroupsOfGroup() {
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        
        groupstore.createGroup("parentGroup", "Test Parent Group");
        groupstore.createGroup("childGroup", "Test Child Group");
        
        //When
        groupstore.grantGroupToGroup("childGroup", "parentGroup");
        List<Group> directParentGroupsForGroup = groupstore.getDirectParentGroupsForGroup("childGroup");
        
        //Then
        assertEquals("Expected childgroup to have parentgroup", "parentGroup", directParentGroupsForGroup.get(0).getName());
    }
    
    @Test
    public void checkDirectChildGroupsOfGroup() {
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        
        groupstore.createGroup("parentGroup", "Test Parent Group");
        groupstore.createGroup("childGroup", "Test Child Group");
        
        //When
        groupstore.grantGroupToGroup("childGroup", "parentGroup");
        List<Group> directChildGroupsForGroup = groupstore.getDirectChildGroupsForGroup("parentGroup");
        
        //Then
        assertEquals("Expected parentgroup to have childgroup", "childGroup", directChildGroupsForGroup.get(0).getName());
    }
    
    @Test(expected=IllegalArgumentException.class)
    @Ignore("There is an issue with the crowd rest api, it should return a 404 "+
            "when requesting a group list for a group which does not exist. EOFMF-58")
    public void getDirectParentGroupsOfGroupThatDoesntExist() {
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        
        //When
        groupstore.getDirectParentGroupsForGroup("fakeGroup");
        
        //Then
        fail("Expected to fail in getting parent groups");
    }
    
    @Test(expected=IllegalArgumentException.class)
    @Ignore("There is an issue with the crowd rest api, it should return a 404 "+
            "when requesting a group list for a group which does not exist. EOFMF-58")
    public void getDirectChildGroupsOfGroupThatDoesntExist() {
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        
        //When
        groupstore.getDirectChildGroupsForGroup("fakeGroup");
        
        //Then
        fail("Expected to fail in getting parent groups");
    }
    
    @Test(expected=IllegalArgumentException.class)
    @Ignore("There is an issue with the crowd rest api, it should return a 404 "+
            "when requesting a group list for a group which does not exist. EOFMF-58")
    public void getChildGroupsOfGroupThatDoesntExist() {
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        
        //When
        groupstore.getChildGroupsForGroup("fakeGroup");
        
        //Then
        fail("Expected to fail in getting parent groups");
    }
    
    @Test(expected=IllegalArgumentException.class)
    @Ignore("There is an issue with the crowd rest api, it should return a 404 "+
            "when requesting a group list for a group which does not exist. EOFMF-58")
    public void getParentGroupsOfGroupThatDoesntExist() {
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        
        //When
        groupstore.getParentGroupsForGroup("fakeGroup");
        
        //Then
        fail("Expected to fail in getting parent groups");
    }
    
    @Test(expected=IllegalArgumentException.class)
    @Ignore("There is an issue with the crowd rest api, it should return a 404 "+
            "when requesting a group list for a user which does not exist. EOFMF-58")
    public void getGroupsForUserWhoDoesNotExist() {
        //Given
        CrowdGroupStore<TestUser> groupstore = groupStoreResource.groupstore();
        TestUser userWhoIsNotRegistered = new TestUser();
        userWhoIsNotRegistered.setUsername("testuser");
        userWhoIsNotRegistered.setFirstname("firstname");
        userWhoIsNotRegistered.setEmail("test@user.com");
        
        //When
        groupstore.getGroups(userWhoIsNotRegistered);
        
        //Then
        fail("Expeceted to fail when getting groups");
    }
}
