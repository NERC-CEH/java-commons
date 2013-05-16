package uk.ac.ceh.components.userstore.crowd;

import uk.ac.ceh.components.userstore.crowd.model.CrowdUser;
import com.google.common.base.Function;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserAttribute;
import uk.ac.ceh.components.userstore.UserAttributeReader;

/**
 *
 * @author Christopher Johnson
 */
class TransformDomainUserToCrowdUser<U extends User> implements Function<U, CrowdUser> {
    private final UserAttributeReader<U> reader;

    public TransformDomainUserToCrowdUser(UserAttributeReader<U> reader) {
        this.reader = reader;
    }
    
    @Override
    public CrowdUser apply(U user) {
        CrowdUser newUser = new CrowdUser();
        newUser.setName(user.getUsername());
        newUser.setEmail(reader.get(user, UserAttribute.EMAIL, String.class));
        newUser.setDisplayname(reader.get(user, UserAttribute.DISPLAY_NAME, String.class));
        newUser.setFirstname(reader.get(user, UserAttribute.FIRSTNAME, String.class));
        newUser.setLastname(reader.get(user, UserAttribute.LASTNAME, String.class));
        return newUser;
    }
}
