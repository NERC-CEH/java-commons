package uk.ac.ceh.components.userstore.springsecurity;

import javax.servlet.http.HttpServletRequest;

/**
 * This interface allows a particular KerberosTicketValidator to be best 
 * selected for a given HttpServletRequest. In general you will want to use a
 * HostnameKerberosTicketValidatorSelector which will select a ticket validator
 * based upon the supported Kerberos Hostname.
 * 
 * There are some situations where the default HostnameKerberosTicketValidatorSelector 
 * will fail (see http://support.microsoft.com/kb/911149 for a DNS related 
 * Internet Explorer bug)
 * 
 * @see HostnameKerberosTicketValidatorSelector
 * @author cjohn
 */
public interface KerberosTicketValidatorSelector {
    /**
     * Returns a KerberosTicketValidator which is best suited to handling the 
     * given HttpServletRequest
     * @param request to handle with this validator
     * @return a KerberosTicketValidator or null if none are supported
     */
    KerberosTicketValidator selectValidator(HttpServletRequest request);
}
