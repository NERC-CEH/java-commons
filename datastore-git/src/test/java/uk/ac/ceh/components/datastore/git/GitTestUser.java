
package uk.ac.ceh.components.datastore.git;

import lombok.Data;
import uk.ac.ceh.components.datastore.DataAuthor;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserAttribute;

/**
 *
 * @author cjohn
 */
@Data
public class GitTestUser implements User, DataAuthor{
    private @UserAttribute(UserAttribute.USERNAME) String username;
    private @UserAttribute(UserAttribute.EMAIL) String email;
    
    public static class Builder {
        private GitTestUser instance;
        
        public Builder(String username) {
            this.instance = new GitTestUser();
            instance.username = username;
        }
        
        public Builder setEmail(String email) {
            instance.email = email;
            return this;
        }

        public GitTestUser build() {
            return instance;
        }
    }
}
