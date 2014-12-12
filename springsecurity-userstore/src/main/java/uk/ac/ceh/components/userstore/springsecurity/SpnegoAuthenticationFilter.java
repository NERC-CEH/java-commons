package uk.ac.ceh.components.userstore.springsecurity;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 *
 * @author cjohn
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class SpnegoAuthenticationFilter extends OncePerRequestFilter {
    private final AuthenticationManager authenticationManager;
    private RememberMeServices rememberMeServices = new NullRememberMeServices();
    private boolean skipIfAlreadyAuthenticated = true;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(isSpnegoRequest(request)) {
            String header = request.getHeader("Authorization");
            KerberosServiceRequestToken serviceRequest = createToken(header.substring(10));
            
            try {
                authenticationManager.authenticate(serviceRequest);
                rememberMeServices.loginSuccess(request, response, serviceRequest);
            }
            catch(AuthenticationException ae) {
                //Authentication failed. Let the remember me services know and carry on
                rememberMeServices.loginFail(request, response);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return skipIfAlreadyAuthenticated && isAuthenticated();
    }
    
    /**
     * Given a Token represented as Base64, decode it and create a Kerberos
     * Service Request Token
     * @param token
     * @return A KerberosServiceRequestToken
     * @throws java.io.IOException if UTF-8 is not supported (This shouldn't happen
     *  as UTF-8 is a JVM requirement
     */
    protected KerberosServiceRequestToken createToken(String token) throws IOException {
        try {
            byte[] ticket = Base64.decode(token.getBytes("UTF-8"));
            return new KerberosServiceRequestToken(ticket);
        }
        catch(IllegalArgumentException iae) {
            throw new BadCredentialsException("Failed to decode kerberos authentication ticket");
        }
    }
    
    /*
     * Simple method to check if the given request represents a Spenego request 
     */
    public static boolean isSpnegoRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return header != null && header.startsWith("Negotiate ");
    }
    
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated();
    }
}
