package uk.ac.ceh.components.userstore.springsecurity;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import jcifs.util.Base64;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.ntlmv2.liferay.NtlmLogonException;
import org.ntlmv2.liferay.NtlmManager;
import org.ntlmv2.liferay.NtlmUserAccount;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * 
 * The following request will handle NTLM authentication, by either starting
 * the authentication process or validating a ntlm Authorization headers.
 * 
 * This filter takes a request matcher, this allows NTLM automated sign on
 * to only be applied to requests which match the request matcher. For example
 * this could be an ipaddress range (or whatever request matcher is applicable)
 * 
 * In order to be able to user this Ntlm Authentication filter you will need to 
 * set up a computer account in you active directory and set a password on it.
 * 
 * The README file of this project explains how this can be done with a supplied
 * .vb script
 * 
 * @author cjohn
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class NtlmAuthenticationFilter extends AbstractSpnegoAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final NtlmManager ntlmManager;
    private RememberMeServices rememberMeServices = new NullRememberMeServices();
    private SecureRandom secureRandom = new SecureRandom();
    private String challengeAttribute = "NTLM_SERVER_CHALLENGE";

    @Override
    protected boolean isAuthenticatable(Authorization authorization) {
        String mechanism = authorization.getMechanism();
        return mechanism.equals("NTLM") || mechanism.equals("Negotiate");
    }

    @Override
    protected void doAuthentication(Authorization authorization, HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(authorization.isNtlmSSP()) {
            byte[] ntlmToken = authorization.getToken();
            if(ntlmToken[8] == 1) {
                //An initial ntlmssp token has been supplied. Return an ntlm 
                //server challenge and store this in the session.
                byte[] serverChallenge = new byte[8];
                secureRandom.nextBytes(serverChallenge);
                request.getSession().setAttribute(challengeAttribute, serverChallenge);

                //Create the challenge message and set this as the WWW-Authenticate header.
                String challengeMessage = Base64.encode(ntlmManager.negotiate(ntlmToken, serverChallenge));
                response.addHeader("WWW-Authenticate", authorization.getMechanism() + " " + challengeMessage);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentLength(0);
                response.flushBuffer();
            }
            else {
                //Browser responded to server challenge (with a Type 3 message),
                //attempt to perform Login
                HttpSession session = request.getSession(false);
                if(session != null) {
                    try {
                        PreAuthenticatedAuthenticationToken token = createToken(authorization.getToken(), session);
                        Authentication auth = authenticationManager.authenticate(token);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        rememberMeServices.loginSuccess(request, response, auth);
                        filterChain.doFilter(request, response);
                    }
                    catch(AuthenticationException ae) {
                        //Authentication failed. Restart the process                        
                        getEntryPoint().commence(request, response, ae);
                    }
                }
                else {
                    //A Type 3 message was recieved but no HttpSession is present.
                    //This may occur if the application is using cookies to track
                    //sessions and the client has disabled cookies. In which case
                    //this filter will be unable to authenticate this client, skip
                    //over this filter.
                    filterChain.doFilter(request, response);
                }
            }
        }
        else {
            // The Negotiate Header was provided but not an NTLMSSP token.
            // This indicates that the supplied token is for Kerberos 
            // authentication. If you register a KerberosAuthenticationFilter
            // after this filter, it will be able to authenticate the given
            // authorisation.
            filterChain.doFilter(request, response);
        }
    }
    
    private PreAuthenticatedAuthenticationToken createToken(byte[] ntlmToken, HttpSession session) throws IOException, ServletException {
        Object serverChallenge = session.getAttribute(challengeAttribute);
        if(serverChallenge != null) {
            //Need to check if the server challenge was presented session is not new and challenge is present
            try {
                byte[] challenge = (byte[])session.getAttribute(challengeAttribute);

                NtlmUserAccount ntlmUserAccount = ntlmManager.authenticate(ntlmToken, challenge);
                return new PreAuthenticatedAuthenticationToken(ntlmUserAccount.getUserName(), ntlmToken);
            }
            catch(NoSuchAlgorithmException nsae) {
                throw new ServletException("The auth service is not set up correctly", nsae);
            }
            catch(NtlmLogonException nle) {
                throw new BadCredentialsException("The provided credentials are incorrect", nle);
            }
            finally {
                session.removeAttribute(challengeAttribute); //Remove the challenge
            }
        }
        else {
            throw new BadCredentialsException("Recieved a Type 3 message before a Type 1");
        }
    }
}
