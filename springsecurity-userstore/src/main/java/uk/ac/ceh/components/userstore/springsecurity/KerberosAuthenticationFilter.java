package uk.ac.ceh.components.userstore.springsecurity;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * 
 * The following request will handle Kerberos authentication, by either starting
 * the authentication process or validating a kerberos Authorization header.
 * 
 * This filter takes a request matcher, this allows Kerberos automated sign on
 * to only be applied to requests which match the request matcher. For example
 * this could be an ipaddress range (or whatever request matcher is applicable)
 * 
 * @author cjohn
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class KerberosAuthenticationFilter extends AbstractSpnegoAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final KerberosTicketValidatorSelector validatorSelector;
    private RememberMeServices rememberMeServices = new NullRememberMeServices();
    private boolean stripRealm = true;
        
    @Override
    protected boolean isAuthenticatable(Authorization authorization) {
        return authorization.getMechanism().equals("Negotiate");
    }

    @Override
    protected void doAuthentication(Authorization authorization, HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        KerberosTicketValidator validator = validatorSelector.selectValidator(request);
        if(validator != null) {
            try {
                PreAuthenticatedAuthenticationToken token = createToken(validator, authorization.getToken());
                Authentication auth = authenticationManager.authenticate(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
                rememberMeServices.loginSuccess(request, response, auth);
            }
            catch(AuthenticationException ae) {
                //Authentication failed. Let the remember me services know and carry on
                SecurityContextHolder.getContext().setAuthentication(null);
                rememberMeServices.loginFail(request, response);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Validate the given a Token and return a PreAuthenticatedAuthenticationToken
     * representation
     * @param validator the validator to use to create a token from
     * @param token the token from the Authorization header
     * @return A PreAuthenticatedAuthenticationToken
     */
    private PreAuthenticatedAuthenticationToken createToken(KerberosTicketValidator validator, byte[] token) {
        String username = stripRealm(validator.validateTicket(token));
        return new PreAuthenticatedAuthenticationToken(username, token);
    }
    
    /*
     * Simple method to remove any realm information from a given username if
     * stripRealm is set to true
     */
    private String stripRealm(String username) {
        int indexOfAt = username.lastIndexOf('@');
        if(stripRealm && indexOfAt != -1) {
            return username.substring(0, indexOfAt);
        }
        else {
            return username;
        }
    }
}
