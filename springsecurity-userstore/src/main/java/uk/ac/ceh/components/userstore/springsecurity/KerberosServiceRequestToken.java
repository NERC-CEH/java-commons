package uk.ac.ceh.components.userstore.springsecurity;

import java.util.Collection;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * A simple class which stores KerberosServiceRequestToken and principals 
 * authenticated using the KerberosServiceAuthenticationProvider
 * @author cjohn
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class KerberosServiceRequestToken extends AbstractAuthenticationToken {
    private final byte[] token;
    private final Object principal;
    
    public KerberosServiceRequestToken(Object principal, byte[] token, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.token = token;
        this.principal = principal;
        setAuthenticated(true);
    }
    
    public KerberosServiceRequestToken(byte[] token) {
        this(null, token, null);
    }
    
    @Override
    public Object getCredentials() {
        return token;
    }
}
