package uk.ac.ceh.components.userstore.springsecurity;

import com.google.common.collect.Collections2;
import java.util.Objects;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.InvalidCredentialsException;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserStore;

/**
 * The following AuthenticationProvider will validate username and password 
 * authentications against the given userstore.
 * 
 * @author Rod Scott, Christopher Johnson
 */
public class UsernamePasswordAuthenticationProvider<U extends User> implements AuthenticationProvider {
    private final UserStore<U> userStore;
    private final GroupStore<U> groupStore;
    
    public UsernamePasswordAuthenticationProvider(UserStore<U> userStore, GroupStore<U> groupStore) {
        this.userStore = Objects.requireNonNull(userStore);
        this.groupStore = Objects.requireNonNull(groupStore);
    }

    /**
     * Performs an authentication process on the given 
     * {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken}
     * 
     * If the authentication was successful, this method will create a new 
     * UsernamePasswordAuthenticationToken object with an implementation of U 
     * set as the principle.
     * @param authentication to authenticate containing username and password
     * @return A fully populated UsernamePasswordAuthenticationToken
     * @throws AuthenticationException If authentication was not possible
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        Object credentials = authentication.getCredentials();
        
        if(username == null) {
            throw new UsernameNotFoundException("no username provided");
        }
        
        if(credentials instanceof String) {
            try {
                U user = userStore.authenticate(username, (String)credentials);
                return new UsernamePasswordAuthenticationToken(user, null, 
                        Collections2.transform(groupStore.getGroups(user), new TransformGroupToSimpleGrantedAuthority()));            
            } catch (InvalidCredentialsException ex) {
                throw new BadCredentialsException("Authentication failed", ex);
            }
            catch(RuntimeException re) {
                throw new AuthenticationServiceException("Failed to communicate with user/group store", re);
            }
        }
        else {
            throw new BadCredentialsException("no credentials provided");
        }
    }

    /**
     * Checks if this provider can support the given authentication object
     * @param authentication
     * @return true if authentication is UsernamePasswordAuthenticationToken
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}