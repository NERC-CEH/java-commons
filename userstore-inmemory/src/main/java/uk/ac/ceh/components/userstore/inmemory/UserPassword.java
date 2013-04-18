package uk.ac.ceh.components.userstore.inmemory;

import uk.ac.ceh.components.userstore.User;

/**
 * The following class is a container for a user and password combination. 
 * It does ensure any security on users passwords other than not exposing an
 * accessor
 * @author cjohn
 */
class UserPassword<U extends User> {
    private U user;
    private String password;
    
    UserPassword(U user, String password) {
        this.user = user;
        this.password = password;
    }
    
    U getUser() {
        return user;
    }
    
    boolean isGivenPasswordAMatch(String password) {
        return this.password.equals(password);
    }
    
    void setUser(U user) {
        this.user = user;
    }
    
    void setPassword(String password) {
        this.password = password;
    }
}
