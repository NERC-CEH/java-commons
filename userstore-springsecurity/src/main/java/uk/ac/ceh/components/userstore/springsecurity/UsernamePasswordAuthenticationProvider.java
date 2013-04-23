package uk.ac.ceh.components.userstore.springsecurity;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import java.util.Arrays;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import uk.ac.ceh.components.userstore.InvalidCredentialsException;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserStore;

/**
 *
 * @author Rod Scott
 */
public class UsernamePasswordAuthenticationProvider<U extends User & Roled> implements AuthenticationProvider {
    private final UserStore<U> userStore;

    @Autowired 
    public UsernamePasswordAuthenticationProvider(UserStore<U> userStore) {
        this.userStore = Objects.requireNonNull(userStore);
    }

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
                        Collections2.transform(user.getRoles(), new TransformRoleToSimpleGrantedAuthority()));            
            } catch (InvalidCredentialsException ex) {
                throw new BadCredentialsException("Authentication failed", ex);
            }
        }
        else {
            throw new BadCredentialsException("no credentials provided");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}