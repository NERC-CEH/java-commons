package uk.ac.ceh.components.userstore.crowd.jaas;

import java.security.Principal;
import lombok.Data;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserAttribute;
import static uk.ac.ceh.components.userstore.UserAttribute.USERNAME;

/**
 *
 * @author mw
 */
@Data
public class UserPrincipal implements User, Principal {
    private @UserAttribute(USERNAME) String username;

    @Override
    public String getName() {
        return username;
    }
}
