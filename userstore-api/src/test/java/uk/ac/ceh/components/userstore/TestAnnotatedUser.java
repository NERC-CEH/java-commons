package uk.ac.ceh.components.userstore;

/**
 *
 * @author CJOHN
 */
public class TestAnnotatedUser implements User {
    private @UserAttribute(UserAttribute.USERNAME) String username;
    private @UserAttribute(UserAttribute.EMAIL) String email;
    private @UserAttribute("age") Integer age;
    
    @Override
    public String getUsername() {
        return username;
    }
    
    public String getEmail() {
        return email;
    }
}
