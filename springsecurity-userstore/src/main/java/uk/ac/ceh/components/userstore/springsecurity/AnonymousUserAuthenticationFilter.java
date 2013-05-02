package uk.ac.ceh.components.userstore.springsecurity;

import com.google.common.collect.Collections2;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

/**
 * The following AnonymousAuthenticationFilter will return an AnonymousAuthenticationToken
 * for a set principle.
 * 
 * The set principle must be an implementation of {@link Roled}
 * @author Christopher Johnson
 */
public class AnonymousUserAuthenticationFilter extends AnonymousAuthenticationFilter {
    private final String key;
    
    public AnonymousUserAuthenticationFilter(String key, Roled principle) {
        super(key, principle, new ArrayList(
                Collections2.transform( principle.getRoles(), 
                                        new TransformRoleToSimpleGrantedAuthority())));
        this.key = key;
    }
    
    @Override
    protected Authentication createAuthentication(HttpServletRequest request) {
       return new AnonymousAuthenticationToken(key, getPrincipal(), getAuthorities());
    }
}
