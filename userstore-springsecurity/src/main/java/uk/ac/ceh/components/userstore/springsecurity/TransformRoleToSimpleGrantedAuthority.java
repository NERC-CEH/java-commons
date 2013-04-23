
package uk.ac.ceh.components.userstore.springsecurity;

import com.google.common.base.Function;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 *
 * @author cjohn
 */
public class TransformRoleToSimpleGrantedAuthority implements Function<String, SimpleGrantedAuthority> {
    @Override 
    public SimpleGrantedAuthority apply(String role) {
        return new SimpleGrantedAuthority(role);
    }
}
