package uk.ac.ceh.components.userstore.crowd.jaas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import static lombok.AccessLevel.PROTECTED;
import lombok.Setter;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.InvalidCredentialsException;
import uk.ac.ceh.components.userstore.UserStore;

/**
 *
 * @author mw
 */
public abstract class UserStoreLoginModule implements LoginModule {
    //Shared state objects
    private @Setter(PROTECTED) UserStore<UserPrincipal> userStore;
    private @Setter(PROTECTED) GroupStore<UserPrincipal> groupStore;
    
    //Login specific state
    private Subject subject;
    private CallbackHandler callbackHandler;
    private UserPrincipal user;
    private List<GroupPrincipal> groups;
    
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        createUserStoreAndGroupStore(options);
    }
    
    @Override
    public boolean login() throws LoginException {
        NameCallback name = new NameCallback("login");
        PasswordCallback password = new PasswordCallback("password", false);
        Callback[] callbacks = new Callback[]{name, password};
        
        try {
            //Get the username and password for the subject who is to be logged in
            callbackHandler.handle(callbacks);
            
            user = userStore.authenticate(name.getName(), String.valueOf(password.getPassword()));

            return true;
        } catch (IOException | UnsupportedCallbackException ex) {
            throw new LoginException(ex.getMessage());
        } catch (InvalidCredentialsException ex) {
            return false;
        }    
    }
    
    protected abstract void createUserStoreAndGroupStore(Map<String, ?> options);

    @Override
    public boolean commit() throws LoginException {
        try {
            groups = new ArrayList<>();
            
            for(Group group: groupStore.getGroups(user)) {
                groups.add(new GroupPrincipal(group));
            }
            
            subject.getPrincipals().add(user);
            subject.getPrincipals().addAll(groups);
            
            return true;
        }
        catch(Exception ex) {
            throw new LoginException(ex.getMessage());
        }
    }

    @Override
    public boolean abort() throws LoginException {
        return false;
    }

    @Override
    public boolean logout() throws LoginException {
        subject.getPrincipals().remove(user);
        subject.getPrincipals().removeAll(groups);
        return true;
    }
    
}
