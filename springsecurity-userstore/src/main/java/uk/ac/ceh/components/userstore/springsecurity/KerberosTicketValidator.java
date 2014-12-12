package uk.ac.ceh.components.userstore.springsecurity;

import org.springframework.security.authentication.BadCredentialsException;

/**
 * Provides a mechanism to validate kerberos tickets.
 * @author cjohn
 */
public interface KerberosTicketValidator {
    String validateTicket(byte[] token) throws BadCredentialsException;
}
