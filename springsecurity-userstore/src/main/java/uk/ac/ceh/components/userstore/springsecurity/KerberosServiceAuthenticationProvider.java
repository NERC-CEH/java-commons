package uk.ac.ceh.components.userstore.springsecurity;

import com.google.common.collect.Collections2;
import lombok.Data;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserStore;

/**
 * An authentication provider which validates KerberosService tokens against some
 * KerberosTicketValidator.
 * 
 * The principal represented by the Kerberos Ticket will be looked up from a 
 * given userStore. The associated authorities will be retrieved from the 
 * supplied groupStore.
 * @author cjohn
 */
@Data
public class KerberosServiceAuthenticationProvider<U extends User> implements AuthenticationProvider {
    private final KerberosTicketValidator ticketValidator;
    private final UserStore<U> userStore;
    private final GroupStore<U> groupStore;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        KerberosServiceRequestToken auth = (KerberosServiceRequestToken)authentication;
        byte[] token = auth.getToken(); //Grab the token
        String username = ticketValidator.validateTicket(token);
        try {
            U user = userStore.getUser(username);
            return new KerberosServiceRequestToken(user, null, 
                        Collections2.transform(groupStore.getGroups(user), new TransformGroupToSimpleGrantedAuthority()));
        }catch (UnknownUserException ex) {
            throw new UsernameNotFoundException("The supplied username is not present in the user store", ex);
        }
    }

    /**
     * Checks if the given authentication class is supported by this provider
     * @param authentication to check
     * @return if this provider supports instances of the given class
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(KerberosServiceRequestToken.class);
    }
}
