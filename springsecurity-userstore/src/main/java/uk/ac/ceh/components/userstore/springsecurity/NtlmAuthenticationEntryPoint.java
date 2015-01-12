package uk.ac.ceh.components.userstore.springsecurity;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;

/**
 * An authentication entry point which is used to initiate an authentication 
 * using the negotiate or the ntlm mechanism
 * @see KerberosAuthenticationFilter
 * @see NtlmAuthenticationFilter
 * @author cjohn
 */
public class NtlmAuthenticationEntryPoint extends NegotiateAuthenticationEntryPoint {

    /**
     * Returns a 401 Unauthorized status to the given response along with the two
     * WWW-Authenticate headers. One for NTLM and one for Negotiate
     *
     * @param request The current request we are dealing with
     * @param response To send the 401 response to
     * @param authException which triggered this entry point
     * @throws IOException If an issue occurs whilst writing to the http response
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.addHeader("WWW-Authenticate", "NTLM");
        super.commence(request, response, authException);
    }
}
