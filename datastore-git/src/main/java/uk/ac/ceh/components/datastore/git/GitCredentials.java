package uk.ac.ceh.components.datastore.git;

import lombok.Data;
import lombok.experimental.Accessors;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 *
 * @author Christopher Johnson
 */
@Data
@Accessors(chain=true)
public class GitCredentials {
    private String username, password;
    
    UsernamePasswordCredentialsProvider getUsernamePasswordCredentialsProvider() {
        return new UsernamePasswordCredentialsProvider(username, password);
    }
}
