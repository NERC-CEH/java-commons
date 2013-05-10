package uk.ac.ceh.components.userstore;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author CJOHN
 */
public class AnnotatedUserHelperTest {
    private AnnotatedUserHelper<TestAnnotatedUser> userHelper;
    
    @Before
    public void createUserHelper() {
        this.userHelper = new AnnotatedUserHelper<>(TestAnnotatedUser.class);
    }
    
    @Test
    public void createNewUserAndPopulateProperties() {
        //given
        UserBuilder<TestAnnotatedUser> newUserBuilder = userHelper.newUserBuilder("testuser");
        
        //when
        TestAnnotatedUser newUser = newUserBuilder
                                  .set(UserAttribute.EMAIL, "test@user.com")
                                  .build();
                
        //then
        assertEquals("Expected the same username", "testuser", newUser.getUsername());
        assertEquals("Expected the same email", "test@user.com", newUser.getEmail());
    }
    
    @Test
    public void createNewUserAndPopulateReadProperties() {
        //given
        UserBuilder<TestAnnotatedUser> newUserBuilder = userHelper.newUserBuilder("testuser");
        
        //when
        TestAnnotatedUser newUser = newUserBuilder
                                  .set(UserAttribute.EMAIL, "test@user.com")
                                  .build();
        String username = userHelper.get(newUser, UserAttribute.USERNAME, String.class);
        String email = userHelper.get(newUser, UserAttribute.EMAIL, String.class);
        
        //then
        assertEquals("Expected the same username", "testuser", username);
        assertEquals("Expected the same email", "test@user.com", email);
    }
    
    @Test
    public void setMorePropertiesThanObjectHas() {
        //given
        UserBuilder<TestAnnotatedUser> newUserBuilder = userHelper.newUserBuilder("testuser");
        
        //when
        TestAnnotatedUser newUser = newUserBuilder
                                  .set("SomeWeridAndUnknownProperty", "Value")
                                  .build();
        String propValue = userHelper.get(newUser, "SomeWeridAndUnknownProperty", String.class);
        
        //then
        assertNull("Didn't expect a value for SomeWeridAndUnknownProperty", propValue);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void setValueAsDifferentTypeToWhatIsExpected() {
        //given
        UserBuilder<TestAnnotatedUser> newUserBuilder = userHelper.newUserBuilder("testuser");
        
        //when
        TestAnnotatedUser newUser = newUserBuilder
                                  .set("age", "Value")
                                  .build();
        //then
        fail("Expceted to fail with IllegalArgumentException");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void readValueAsDifferentTypeToWhatIsSet() {
        //given
        TestAnnotatedUser newUser = userHelper
                                            .newUserBuilder("testuser")
                                            .set("age", 20)
                                            .build();
        //when
        String ageAsString = userHelper.get(newUser, "age", String.class);
        
        //then
        fail("Expceted to fail with IllegalArgumentException");
    }
}
