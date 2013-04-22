package uk.ac.ceh.components.userstore.inmemory;

import java.util.HashMap;
import java.util.Map;
import uk.ac.ceh.components.userstore.*;

/**
 * The following class defines a writable userstore which is based upon a hashmap
 * 
 * This implementation is thread safe
 * @author cjohn
 */
public class InMemoryUserStore<U extends User> implements WritableUserStore<U> {
    private final Map<String, UserPassword<U>> userStore;
    
    public InMemoryUserStore() {
        this.userStore = new HashMap<>();
    }
    
    @Override
    public synchronized void addUser(U user, String password) throws UsernameAlreadyTakenException {
        if(!userStore.containsKey(user.getUsername())) {
            userStore.put(user.getUsername(), new UserPassword<>(user, password));
        }
        else
            throw new UsernameAlreadyTakenException("A user already exists with the username " + user.getUsername());
    }

    @Override
    public synchronized void updateUser(U user) throws UnknownUserException {
        UserPassword<U> userWrapper = userStore.get(user.getUsername());
        if(userWrapper != null) {
            userWrapper.setUser(user);
        }
        else
            throw new UnknownUserException("No user exists with the username " + user.getUsername());
    }

    @Override
    public synchronized void deleteUser(String username) throws UnknownUserException {
        if(userStore.remove(username) == null) {
            throw new UnknownUserException("No user was in this userstore with that username");
        }
    }

    @Override
    public U getUser(String username) throws UnknownUserException {
        UserPassword<U> userWrapper = userStore.get(username);
        if(userWrapper != null) {
            return userWrapper.getUser();
        }
        throw new UnknownUserException("No user was in this userstore with that username");
    }

    @Override
    public boolean userExists(String username) {
        return userStore.containsKey(username);
    }

    @Override
    public U authenticate(String username, String password) throws InvalidCredentialsException {
        UserPassword<U> userWrapper = userStore.get(username);
        if(userWrapper != null && userWrapper.isGivenPasswordAMatch(password)) {
            return userWrapper.getUser();
        }
        throw new InvalidCredentialsException("The username and password combination was incorrect");
    }

    @Override
    public synchronized void setUserPassword(String username, String newPassword) throws UnknownUserException {
        UserPassword<U> userWrapper = userStore.get(username);
        if(userWrapper != null) {
            userWrapper.setPassword(newPassword);
        }
        throw new UnknownUserException("The given user is not known");
    }
}
