package uk.ac.ceh.components.userstore.springsecurity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;

/**
 * The following KerberosTicketValidatorSelector will allow Validators to be 
 * selected based upon the hostname which that validator is set up to use.
 * 
 * This implementation will locate KerberosTicketValidator's irrespective of case.
 * @author cjohn
 */
public class HostnameKerberosTicketValidatorSelector implements KerberosTicketValidatorSelector {
    private final Map<String, KerberosTicketValidator> validators;
    
    public HostnameKerberosTicketValidatorSelector(KerberosTicketValidator... validators) {
        this(Arrays.asList(validators));
    }
    
    /**
     * Constructs a list HostnameKerberosTicketValidatorSelector filled with the
     * validators which are supported.
     * @param validators to add to this selector
     */
    public HostnameKerberosTicketValidatorSelector(List<KerberosTicketValidator> validators) {
        this.validators = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for(KerberosTicketValidator validator: validators) {
            this.validators.put(validator.getServicePrincipalHostname(), validator);
        }
    }
    
    @Override
    public KerberosTicketValidator selectValidator(HttpServletRequest request) {
        return validators.get(request.getServerName());
    }
}
