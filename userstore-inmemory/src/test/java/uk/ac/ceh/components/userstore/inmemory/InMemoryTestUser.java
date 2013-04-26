
package uk.ac.ceh.components.userstore.inmemory;

import uk.ac.ceh.components.userstore.User;

/**
 *
 * @author cjohn
 */
public class InMemoryTestUser implements User {
    private final String username;

    public InMemoryTestUser(String username) {
        this.username = username;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
}
