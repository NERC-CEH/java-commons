
package uk.ac.ceh.components.datastore.git;

import uk.ac.ceh.components.userstore.UserBuilderFactory;

/**
 *
 * @author cjohn
 */
public class GitTestUserBuilderFactory implements UserBuilderFactory<GitTestUser.Builder> {

    @Override
    public GitTestUser.Builder newUserBuilder(String username) {
        return new GitTestUser.Builder(username);
    }
}
