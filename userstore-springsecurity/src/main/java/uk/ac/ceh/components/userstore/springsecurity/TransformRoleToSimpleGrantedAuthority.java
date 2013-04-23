package uk.ac.ceh.components.userstore.springsecurity;

import com.google.common.base.Function;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * The following class represents a function which is used for converting from 
 * the {@link Roled} representation of a Role to Spring Securities SimpleGrantedAuthority
 * representation of a role.
 * 
 * The function interface is defined in the guava suite of utilities.
 * 
 * @author cjohn
 */
class TransformRoleToSimpleGrantedAuthority implements Function<String, SimpleGrantedAuthority> {
    /**
     * Transforms a String representation of a role to a SimpleGrantedAuthority
     * @param role to transform
     * @return The SimpleGrantedAuthority equivalent.
     */
    @Override 
    public SimpleGrantedAuthority apply(String role) {
        return new SimpleGrantedAuthority(role);
    }
}
