package uk.ac.ceh.components.userstore.springsecurity;

import java.io.File;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSManager;
import org.springframework.security.authentication.BadCredentialsException;

/**
 * A Kerberos ticket validator which uses the SUN JAAS login module, which is 
 * included in the SUN JRE, it will not work with an IBM JRE. The whole 
 * configuration is done in this class, no additional JAAS configuration
 * is needed.
 * 
 * The implementation provided here is based upon the work provided in
 * https://github.com/spring-projects/spring-security-kerberos.git
 * @author cjohn
 * @author Mike Wiesner
 */
public class GSSKerberosTicketValidator implements KerberosTicketValidator {
    private final Subject serviceSubject;
    
    public GSSKerberosTicketValidator(File keytab, String servicePrincipal) throws LoginException {
        LoginConfig loginConfig = new LoginConfig(keytab, servicePrincipal);
        Set<Principal> princ = new HashSet<>(1);
        princ.add(new KerberosPrincipal(servicePrincipal));
        Subject sub = new Subject(false, princ, new HashSet<>(), new HashSet<>());
        LoginContext lc = new LoginContext("", sub, null, loginConfig);
        lc.login();
        this.serviceSubject = lc.getSubject();
    }
    
    @Override
    public String validateTicket(byte[] token) throws BadCredentialsException {
        try {
            return Subject.doAs(this.serviceSubject, new KerberosValidateAction(token));
        } catch (PrivilegedActionException e) {
            throw new BadCredentialsException("Kerberos validation not successful", e);
        }
    }
    
    /**
     * This class is needed, because the validation must run with previously generated JAAS subject
     * which belongs to the service principal and was loaded out of the keytab during startup.
     *
     * @author Mike Wiesner
     * @since 1.0
     */
    private static class KerberosValidateAction implements PrivilegedExceptionAction<String> {
        byte[] kerberosTicket;

        public KerberosValidateAction(byte[] kerberosTicket) {
            this.kerberosTicket = kerberosTicket;
        }

        @Override
        public String run() throws Exception {
            GSSContext context = GSSManager.getInstance().createContext((GSSCredential) null);
            context.acceptSecContext(kerberosTicket, 0, kerberosTicket.length);
            String user = context.getSrcName().toString();
            context.dispose();
            return user;
        }

    }

    /**
     * Normally you need a JAAS config file in order to use the JAAS Kerberos Login Module,
     * with this class it is not needed and you can have different configurations in one JVM.
     *
     * @author Mike Wiesner
     * @since 1.0
     */
    @Data
    @EqualsAndHashCode(callSuper=false)
    private static class LoginConfig extends Configuration {
        private final File keyTabLocation;
        private final String servicePrincipalName;

        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
            HashMap<String, String> options = new HashMap<>();
            options.put("useKeyTab", "true");
            options.put("keyTab", keyTabLocation.getAbsolutePath());
            options.put("principal", servicePrincipalName);
            options.put("storeKey", "true");
            options.put("doNotPrompt", "true");
            options.put("isInitiator", "false");

            return new AppConfigurationEntry[] { new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule",
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options), };
        }

    }
}
