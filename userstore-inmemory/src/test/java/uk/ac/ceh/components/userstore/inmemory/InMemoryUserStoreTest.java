
package uk.ac.ceh.components.userstore.inmemory;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ceh.components.userstore.UsernameAlreadyTakenException;
import static org.junit.Assert.*;
import uk.ac.ceh.components.userstore.InvalidCredentialsException;
import uk.ac.ceh.components.userstore.UnknownUserException;

/**
 *
 * @author cjohn
 */
public class InMemoryUserStoreTest {
    private InMemoryUserStore<InMemoryTestUser> userStore;
    
    @Before
    public void createEmptyInMemoryUserStore() {
        userStore = new InMemoryUserStore<>();
    }
    
    @Test
    public void createNewUser() throws UsernameAlreadyTakenException, UnknownUserException {
        //Given
        InMemoryTestUser newUser = new InMemoryTestUser("testuser");
        String usersPassword = "password";
        
        //When
        userStore.addUser(newUser, usersPassword);
        
        //Then
        assertNotNull("Expected to get a user", userStore.getUser("testuser"));
    }
    
    @Test
    public void authenticateUser() throws UsernameAlreadyTakenException, UnknownUserException, InvalidCredentialsException {
        //Given
        InMemoryTestUser newUser = new InMemoryTestUser("testuser");
        String usersPassword = "password";
        
        //When
        userStore.addUser(newUser, usersPassword);
        //Then
        InMemoryTestUser authenticate = userStore.authenticate("testuser", "password");
        assertEquals("Expected to authenticate a user", "testuser", authenticate.getUsername());
    }
    
    @Test(expected=InvalidCredentialsException.class)
    public void invalidAuthenticationOfUser() throws UsernameAlreadyTakenException, UnknownUserException, InvalidCredentialsException {
        //Given
        InMemoryTestUser newUser = new InMemoryTestUser("testuser");
        String usersPassword = "password";
        
        //When
        userStore.addUser(newUser, usersPassword);
        
        //Then
        InMemoryTestUser authenticate = userStore.authenticate("testuser", "wrongPassword");
        fail("Should not have been able to authenticate a user");
    }
    
    
    @Test(expected=UnknownUserException.class)
    public void getUserWhoDoesNotExists() throws UnknownUserException {
        //Given
        String username = "SomeoneWhoDoesnotExist";
       
        //When
        userStore.getUser(username);
        
        //Then
        fail("Should not have gotten a user");
    }
    
    @Test(expected=UnknownUserException.class)
    public void getDeletedUser() throws UsernameAlreadyTakenException, UnknownUserException {
        //Given
        InMemoryTestUser newUser = new InMemoryTestUser("testuser");
        String usersPassword = "password";
        userStore.addUser(newUser, usersPassword);
       
        //When
        userStore.deleteUser("testuser");
        
        //Then
        InMemoryTestUser user = userStore.getUser("testuser");
        fail("Should not have gotten a user");
    }
    
    @Test(expected=InvalidCredentialsException.class)
    public void authenticateDeletedUser() throws UsernameAlreadyTakenException, UnknownUserException, InvalidCredentialsException {
        //Given
        InMemoryTestUser newUser = new InMemoryTestUser("testuser");
        String usersPassword = "password";
        userStore.addUser(newUser, usersPassword);
       
        //When
        userStore.deleteUser("testuser");
        
        //Then
        InMemoryTestUser authenticate = userStore.authenticate("testuser", "anything");
        fail("Should not have been able to authenticate a user");
    }
    
    
    @Test(expected=UsernameAlreadyTakenException.class)
    public void registerTheSameUsername() throws UsernameAlreadyTakenException, UnknownUserException, InvalidCredentialsException {
        //Given
        InMemoryTestUser newUser = new InMemoryTestUser("takenusername");
        String usersPassword = "password";
        userStore.addUser(newUser, usersPassword);
       
        //When
        userStore.addUser(new InMemoryTestUser("takenusername"), "somethingelse");
        
        //Then
        fail("Should not have been able to add the user");
    }
    
    @Test
    public void changeUserPassword() throws UsernameAlreadyTakenException, UnknownUserException, InvalidCredentialsException {
        //Given
        InMemoryTestUser newUser = new InMemoryTestUser("testuser");
        userStore.addUser(newUser, "originalPassword");
        
        //When
        userStore.setUserPassword("testuser", "password");

        //Then
        InMemoryTestUser authenticate = userStore.authenticate("testuser", "password");
        assertEquals("Expected to authenticate a user", "testuser", authenticate.getUsername());
    }
    
    @Test(expected=InvalidCredentialsException.class)
    public void loginWithOldPassword() throws UsernameAlreadyTakenException, UnknownUserException, InvalidCredentialsException {
        //Given
        InMemoryTestUser newUser = new InMemoryTestUser("testuser");
        userStore.addUser(newUser, "originalPassword");
        
        //When
        userStore.setUserPassword("testuser", "password");

        //Then
        InMemoryTestUser authenticate = userStore.authenticate("testuser", "originalPassword");
        fail("should not have been able to authenticate");
    }
    
    @Test
    public void checkUserExists() throws UsernameAlreadyTakenException, UnknownUserException, InvalidCredentialsException {
        //Given
        InMemoryTestUser newUser = new InMemoryTestUser("testuser");
        
        //When
        userStore.addUser(newUser, "Password");

        //Then
        assertTrue("User should exist", userStore.userExists("testuser"));
    }
       
    @Test
    public void checkUserDoesntExists() throws UsernameAlreadyTakenException, UnknownUserException, InvalidCredentialsException {
        //Given
        InMemoryTestUser newUser = new InMemoryTestUser("testuser");
        
        //When
        userStore.addUser(newUser, "Password");

        //Then
        assertFalse("User shouldn't exist", userStore.userExists("testuser2"));
    }
}
