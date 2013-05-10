package uk.ac.ceh.components.userstore.springsecurity;

import com.google.common.base.Function;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import uk.ac.ceh.components.userstore.Group;

/**
 * The following class represents a function which is used for converting from 
 * the userstores representation of a {@link Group}s groupname to Spring Securities 
 * SimpleGrantedAuthority representation of a group.
 * 
 * The function interface is defined in the guava suite of utilities.
 * 
 * @author cjohn
 */
class TransformGroupnameToSimpleGrantedAuthority implements Function<String, SimpleGrantedAuthority> {
    /**
     * Transforms a String representation of a group to a SimpleGrantedAuthority
     * @param group name to transform
     * @return The SimpleGrantedAuthority equivalent.
     */
    @Override 
    public SimpleGrantedAuthority apply(String group) {
        return new SimpleGrantedAuthority(group);
    }
}
