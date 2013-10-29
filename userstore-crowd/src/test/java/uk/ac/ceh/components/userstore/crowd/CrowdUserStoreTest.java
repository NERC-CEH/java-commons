package uk.ac.ceh.components.userstore.crowd;

import org.junit.*;

import uk.ac.ceh.components.userstore.InvalidCredentialsException;
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.components.userstore.UsernameAlreadyTakenException;
import static org.junit.Assert.*;

/**
 *
 * @author Christopher Johnson
 */

public class CrowdUserStoreTest {    
    @Rule
    public TestCrowdUserStoreResource testCrowd = new TestCrowdUserStoreResource(
            new CrowdApplicationCredentials(
                System.getProperty("crowd-url"),
                System.getProperty("crowd-application"),
                System.getProperty("crowd-password")));

    @Test
    public void registerNewUser() throws UsernameAlreadyTakenException, InvalidCredentialsException, UnknownUserException {
        //Given
        CrowdUserStore<TestUser> userstore = testCrowd.userstore();
        TestUser toRegister = new TestUser();
        toRegister.setUsername("test-user");
        toRegister.setEmail("test@user.com");
        toRegister.setFirstname("Testington");
        String password = "testpassword";
        
        //When
        userstore.addUser(toRegister, password);
        
        //Then
        TestUser crowdUser = userstore.getUser("test-user");
        assertEquals("Expected to get a registered user", toRegister ,crowdUser);
    }
    
    @Test
    public void authenticateUser() throws UsernameAlreadyTakenException, InvalidCredentialsException {
        //Given
        CrowdUserStore<TestUser> userstore = testCrowd.userstore();
        TestUser toRegister = new TestUser();
        toRegister.setUsername("test-user");
        toRegister.setEmail("test@user.com");
        toRegister.setFirstname("Testington");
        userstore.addUser(toRegister, "testpassword");
        
        //When
        TestUser authenticated = userstore.authenticate("test-user", "testpassword");
        
        //Then
        assertEquals("Assumed that the registed user would be the same as the one obtained", toRegister, authenticated);
    }
    
    @Test(expected=InvalidCredentialsException.class)
    public void attemptToAuthenticateUserInvalidly() throws UsernameAlreadyTakenException, InvalidCredentialsException {
        //Given
        CrowdUserStore<TestUser> userstore = testCrowd.userstore();
        TestUser toRegister = new TestUser();
        toRegister.setUsername("test-user");
        toRegister.setEmail("test@user.com");
        toRegister.setFirstname("Testington");
        userstore.addUser(toRegister, "testpassword");
        
        //When
        TestUser authenticated = userstore.authenticate("test-user", "wrongPassword");
        
        //Then
        fail("Expected to fail authentication");
    }
    
    @Test
    public void deleteUser() throws UsernameAlreadyTakenException, InvalidCredentialsException, UnknownUserException {
        //Given
        CrowdUserStore<TestUser> userstore = testCrowd.userstore();
        TestUser toRegister = new TestUser();
        toRegister.setUsername("test-user");
        toRegister.setEmail("test@user.com");
        toRegister.setFirstname("Testington");
        userstore.addUser(toRegister, "testpassword");
        
        //When
        userstore.deleteUser("test-user");
        
        //Then
        assertFalse("Expected user to not exist", userstore.userExists("test-user"));
    }
    
    @Test(expected=UnknownUserException.class)
    public void deleteUserWhoDoesNotExist() throws UnknownUserException {
        //Given
        CrowdUserStore<TestUser> userstore = testCrowd.userstore();
        String usernameWhoDoesNotExist = "someRandomUser";
        
        //When
        userstore.deleteUser(usernameWhoDoesNotExist);
        
        //Then
        fail("Didn't expect to be able to delete a user who does not exist");
    }
    
    @Test
    public void updateUsersPassword() throws UsernameAlreadyTakenException, InvalidCredentialsException, UnknownUserException {
        //Given 
        CrowdUserStore<TestUser> userstore = testCrowd.userstore();
        TestUser toRegister = new TestUser();
        toRegister.setUsername("test-user");
        toRegister.setEmail("test@user.com");
        toRegister.setFirstname("Testington");
        userstore.addUser(toRegister, "testpassword");
        
        //When
        userstore.setUserPassword("test-user", "newpassword");
        
        //Then
        TestUser authenticated = userstore.authenticate("test-user", "newpassword");
        assertEquals("Expected the authenticated user to be the same as the test user", toRegister, authenticated);
    }
    
    @Test
    public void updateUserDetails() throws UsernameAlreadyTakenException, InvalidCredentialsException, UnknownUserException {
        //Given
        CrowdUserStore<TestUser> userstore = testCrowd.userstore();
        TestUser registeredUser = new TestUser();
        registeredUser.setUsername("test-user");
        registeredUser.setEmail("Old@email.com");
        registeredUser.setFirstname("Testington");
        userstore.addUser(registeredUser, "anyoldpassword");
        
        //When
        registeredUser.setEmail("new@email.com");
        userstore.updateUser(registeredUser);
        
        //Then
        TestUser user = userstore.getUser("test-user");
        assertEquals("Expected user to have the new email", "new@email.com", user.getEmail());
    }
    
    @Test
    public void checkUserExists() throws UsernameAlreadyTakenException, InvalidCredentialsException {
        //Given
        CrowdUserStore<TestUser> userstore = testCrowd.userstore();
        TestUser toRegister = new TestUser();
        toRegister.setUsername("test-user");
        toRegister.setEmail("test@user.com");
        toRegister.setFirstname("Testington");
        userstore.addUser(toRegister, "testpassword");
        
        //When
        boolean userExists = userstore.userExists("test-user");
        
        //Then
        assertTrue("Expected test-user to exist", userExists);
    }
    
    @Test
    public void checkUserDoesNotExist() {
        //Given
        CrowdUserStore<TestUser> userstore = testCrowd.userstore();
        String usernameOfUserWhoDoesNotExist = "mrSneaky";
        
        //When
        boolean userExists = userstore.userExists(usernameOfUserWhoDoesNotExist);
        
        //Then
        assertFalse("Didn't expect user to exist", userExists);
    }
    
}
