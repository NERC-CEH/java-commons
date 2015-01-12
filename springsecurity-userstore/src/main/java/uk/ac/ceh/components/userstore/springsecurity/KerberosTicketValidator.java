package uk.ac.ceh.components.userstore.springsecurity;

import org.springframework.security.authentication.BadCredentialsException;

/**
 * Provides a mechanism to validate kerberos tickets.
 * @author cjohn
 */
public interface KerberosTicketValidator {
    /**
     * Obtains the username of the principal represented in the kerberos ticket
     * @param token byte array of kerberos ticket
     * @return the username of a principal
     * @throws BadCredentialsException if the token cannot be read
     */
    String validateTicket(byte[] token) throws BadCredentialsException;
    
    /**
     * Obtain the service principal domain name which this ticket validator can
     * work with
     * @return the domain name this validator works on
     */
    String getServicePrincipalDomain();
}
