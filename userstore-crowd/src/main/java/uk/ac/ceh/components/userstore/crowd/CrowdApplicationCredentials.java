package uk.ac.ceh.components.userstore.crowd;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author Christopher Johnson
 */
@Data
@AllArgsConstructor
public class CrowdApplicationCredentials {
    private final String location, applicationName, password;
    
    WebResource getCrowdJerseryResource() {
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        client.addFilter(new HTTPBasicAuthFilter(applicationName, password));
        
        return client.resource(location);
    }
}
