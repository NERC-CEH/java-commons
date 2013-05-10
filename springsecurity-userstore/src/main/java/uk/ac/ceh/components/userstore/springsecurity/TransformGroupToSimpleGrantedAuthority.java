package uk.ac.ceh.components.userstore.springsecurity;

import com.google.common.base.Function;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import uk.ac.ceh.components.userstore.Group;

/**
 * The following class represents a function which is used for converting from 
 * the userstores representation of a {@link Group} to Spring Securities 
 * SimpleGrantedAuthority representation of a group.
 * 
 * The function interface is defined in the guava suite of utilities.
 * 
 * @author cjohn
 */
class TransformGroupToSimpleGrantedAuthority implements Function<Group, SimpleGrantedAuthority> {
    /**
     * Transforms a representation of a Group to a SimpleGrantedAuthority
     * @param group to transform
     * @return The SimpleGrantedAuthority equivalent.
     */
    @Override 
    public SimpleGrantedAuthority apply(Group group) {
        return new SimpleGrantedAuthority(group.getName());
    }
}
