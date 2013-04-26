
package uk.ac.ceh.components.datastore.git;

import uk.ac.ceh.components.datastore.DataAuthor;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserBuilder;

/**
 *
 * @author cjohn
 */
public class GitTestUser implements User, DataAuthor{
    private final String username, email;
    
    private GitTestUser(String username, String email) {
        this.username = username;
        this.email = email;
    }
    
    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public static class Builder implements GitAuthorBuilder<GitTestUser> {
        private String username, email;
        
        public Builder(String username) {
            this.username = username;
        }
        
        @Override
        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        @Override
        public GitTestUser build() {
            return new GitTestUser(username, email);
        }
    }
}
