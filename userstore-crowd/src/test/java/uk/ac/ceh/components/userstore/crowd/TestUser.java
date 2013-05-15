package uk.ac.ceh.components.userstore.crowd;

import lombok.Data;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserAttribute;

/**
 *
 * @author Christopher Johnson
 */
@Data
public class TestUser implements User{
    private @UserAttribute(UserAttribute.USERNAME) String username;
    private @UserAttribute(UserAttribute.EMAIL) String email;
    
}
