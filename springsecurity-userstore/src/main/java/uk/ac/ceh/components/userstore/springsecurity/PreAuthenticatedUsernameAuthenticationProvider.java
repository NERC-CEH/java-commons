package uk.ac.ceh.components.userstore.springsecurity;

import com.google.common.collect.Collections2;
import java.util.Objects;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserStore;

/**
 * An authentication provider which handles PreAuthenticatiedAuthenticationTokens
 * and attempts to locate these from with in the given userStore and groupStore.
 * 
 * This authentication provider will not attempt to authenticate the users 
 * credentials, it will merely obtain them from the user store and return if 
 * present.
 * 
 * @see org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter
 * @author cjohn
 */
public class PreAuthenticatedUsernameAuthenticationProvider<U extends User> implements AuthenticationProvider {
    private final UserStore<U> userStore;
    private final GroupStore<U> groupStore;
    
    public PreAuthenticatedUsernameAuthenticationProvider(UserStore<U> userStore, GroupStore<U> groupStore) {
        this.userStore = Objects.requireNonNull(userStore);
        this.groupStore = Objects.requireNonNull(groupStore);
    }

    /**
     * Obtain the PreAuthenticatedAuthenticationToken object and lookup the name
     * of the principal from the userstore. Once obtained, return a new 
     * PreAuthenticatedAuthenticationToken with the full principal populated (of
     * type U) along with the GrantedAuthorities associated with that User
     * @param authentication The unauthenticated PreAuthenticatedAuthenticationToken
     *  which wraps up the given users username
     * @return An authenticated PreAuthenticatedAuthenticationToken with groups and
     * user fully populated
     * @throws AuthenticationException if the no user can be found with the 
     *  supplied username
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        
        if(username == null) {
            throw new UsernameNotFoundException("no username provided");
        }
        
        try {
            U user = userStore.getUser(username);
            return new PreAuthenticatedAuthenticationToken(user, null, 
                    Collections2.transform(groupStore.getGroups(user), new TransformGroupToSimpleGrantedAuthority()));            
        } catch (UnknownUserException ex) {
            throw new UsernameNotFoundException("The supplied username is not present in the user store", ex);
        }
    }

    
    /**
     * Checks if this provider can support the given authentication object
     * @param authentication
     * @return true if authentication is PreAuthenticatedAuthenticationToken
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }
}
