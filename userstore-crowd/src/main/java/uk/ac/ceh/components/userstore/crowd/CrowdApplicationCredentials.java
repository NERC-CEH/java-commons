package uk.ac.ceh.components.userstore.crowd;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import java.net.URI;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Christopher Johnson
 */
@Data
@RequiredArgsConstructor
public class CrowdApplicationCredentials {
    private final String location, applicationName, password;
    
    WebResource getCrowdJerseryResource() {
        ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        Client client = Client.create(config);
        client.addFilter(new HTTPBasicAuthFilter(applicationName, password));
        
        return client.resource(location);
    }
}
