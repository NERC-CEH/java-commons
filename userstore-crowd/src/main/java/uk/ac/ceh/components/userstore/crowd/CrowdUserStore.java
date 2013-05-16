package uk.ac.ceh.components.userstore.crowd;

import com.google.common.collect.Collections2;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import static com.sun.jersey.api.client.ClientResponse.Status.FORBIDDEN;
import static com.sun.jersey.api.client.ClientResponse.Status.NOT_FOUND;
import static com.sun.jersey.api.client.ClientResponse.Status.NO_CONTENT;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import java.net.URI;
import java.util.Collection;
import javax.ws.rs.core.MediaType;
import uk.ac.ceh.components.userstore.InvalidCredentialsException;
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserAttribute;
import uk.ac.ceh.components.userstore.UserAttributeReader;
import uk.ac.ceh.components.userstore.UserBuilderFactory;
import uk.ac.ceh.components.userstore.UsernameAlreadyTakenException;
import uk.ac.ceh.components.userstore.WritableUserStore;

/**
 *
 * @author Christopher Johnson
 */
public class CrowdUserStore<U extends User> implements WritableUserStore<U> {
    private WebResource crowd;
    
    private final TransformCrowdUserToDomainUser<U> userTransformer;
    private final UserAttributeReader<U> reader;
    
    public CrowdUserStore(URI crowd, String app, String password, 
                                                UserBuilderFactory<U> userFactory, 
                                                UserAttributeReader<U> reader) {
        ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        Client client = Client.create(config);
        client.addFilter(new HTTPBasicAuthFilter(app, password));
        
        this.crowd = client.resource(crowd);
        this.reader = reader;
        this.userTransformer = new TransformCrowdUserToDomainUser<>(userFactory);
    }
    
    public Collection<U> getAllUsers() {
        ClientResponse crowdResponse = crowd.path("search")
                                            .queryParam("entity-type", "user")
                                            .accept(MediaType.APPLICATION_JSON)
                                            .get(ClientResponse.class);
        
        switch(crowdResponse.getClientResponseStatus()) {
            case OK : return Collections2.transform(crowdResponse
                    .getEntity(CrowdUserSearch.class).getUsers(), userTransformer);
            default : throw new CrowdRestException("Unexpected status code from crowd");     
        }
    }
    
    @Override
    public void addUser(U user, String password) throws UsernameAlreadyTakenException, InvalidCredentialsException {
        //Build a crowd user from the given user
        CrowdUser newUser = new CrowdUser();
        newUser.setName(user.getUsername());
        newUser.setEmail(reader.get(user, UserAttribute.EMAIL, String.class));
        newUser.setDisplayname(reader.get(user, UserAttribute.DISPLAY_NAME, String.class));
        newUser.setFirstname(reader.get(user, UserAttribute.FIRSTNAME, String.class));
        newUser.setLastname(reader.get(user, UserAttribute.LASTNAME, String.class));
        newUser.setPassword(new CrowdUserPassword(password));
        
        ClientResponse crowdResponse = crowd.path("user")
                                            .type(MediaType.APPLICATION_JSON)
                                            .post(ClientResponse.class, newUser);
        
        switch(crowdResponse.getClientResponseStatus()) {
            case CREATED : return;
            case BAD_REQUEST : throw new UsernameAlreadyTakenException("The given username is already taken");
            default : throw new CrowdRestException("Unexpected status code from crowd" + crowdResponse.getEntity(String.class)); 
        }
    }

    @Override
    public void updateUser(U user) throws UnknownUserException {
        //Build a crowd user from the given user
        CrowdUser newUser = new CrowdUser();
        newUser.setName(user.getUsername());
        newUser.setEmail(reader.get(user, UserAttribute.EMAIL, String.class));
        newUser.setDisplayname(reader.get(user, UserAttribute.DISPLAY_NAME, String.class));
        newUser.setFirstname(reader.get(user, UserAttribute.FIRSTNAME, String.class));
        newUser.setLastname(reader.get(user, UserAttribute.LASTNAME, String.class));
        
        ClientResponse crowdResponse = crowd.path("user")
                                            .queryParam("username", user.getUsername())
                                            .type(MediaType.APPLICATION_JSON)
                                            .put(ClientResponse.class, newUser);

        switch(crowdResponse.getClientResponseStatus()) {
            case NO_CONTENT : return;
            case NOT_FOUND : throw new UnknownUserException("Can not update the user as the username is not known");
            default : throw new CrowdRestException("Unexpected status code from crowd");
        }
    }

    @Override
    public void deleteUser(String username) throws UnknownUserException {
        ClientResponse crowdResponse = crowd.path("user")
                                            .queryParam("username", username)
                                            .type(MediaType.APPLICATION_JSON)
                                            .delete(ClientResponse.class);
        switch(crowdResponse.getClientResponseStatus()) {
            case NO_CONTENT : return; //success
            case NOT_FOUND : throw new UnknownUserException("The specified user does not exist");
            case FORBIDDEN : throw new CrowdRestException("The crowd application is not allowed to delete users");
            default : throw new CrowdRestException("Unexpected status code from crowd");
        }
    }

    @Override
    public void setUserPassword(String username, String newPassword) throws UnknownUserException {
        CrowdUserPassword request = new CrowdUserPassword(newPassword);
        
        ClientResponse crowdResponse = crowd.path("user/password")
                                            .queryParam("username", username)
                                            .type(MediaType.APPLICATION_JSON)
                                            .put(ClientResponse.class, request);
        
        switch(crowdResponse.getClientResponseStatus()) {
            case NO_CONTENT : return; //success
            case NOT_FOUND : throw new UnknownUserException("The specified user does not exist");
            case FORBIDDEN : throw new CrowdRestException("The crowd application is not allowed to update users passwords");
            default : throw new CrowdRestException("Unexpected status code from crowd");
        }
    }

    @Override
    public U getUser(String username) throws UnknownUserException {
        ClientResponse crowdResponse = crowd.path("user")
                                            .queryParam("username", username)
                                            .accept(MediaType.APPLICATION_JSON)
                                            .get(ClientResponse.class);
        
        switch(crowdResponse.getClientResponseStatus()) {
            case OK : return userTransformer.apply(crowdResponse.getEntity(CrowdUser.class));
            case NOT_FOUND: throw new UnknownUserException("The given username is not known");
            default: throw new CrowdRestException("Unexpected status code: " + crowdResponse.getStatus());
        }
    }

    @Override
    public boolean userExists(String username) {
        ClientResponse crowdResponse = crowd.path("user")
                                            .queryParam("username", username)
                                            .get(ClientResponse.class);
        
        switch(crowdResponse.getClientResponseStatus()) {
            case OK : return true;
            case NOT_FOUND : return false;
            default: throw new CrowdRestException("Unexpected status code: " + crowdResponse.getStatus());
        }
    }

    @Override
    public U authenticate(String username, String password) throws InvalidCredentialsException {
        CrowdUserPassword request = new CrowdUserPassword(password);
        
        ClientResponse crowdResponse = crowd.path("authentication")
                                            .queryParam("username", username)
                                            .type(MediaType.APPLICATION_JSON)
                                            .accept(MediaType.APPLICATION_JSON)
                                            .post(ClientResponse.class, request);
        
        switch(crowdResponse.getClientResponseStatus()) {
            case OK: return userTransformer.apply(crowdResponse.getEntity(CrowdUser.class));
            case BAD_REQUEST: throw new InvalidCredentialsException(
                                            crowdResponse.getEntity(CrowdErrorResponse.class)
                                                         .getMessage());
            default: throw new CrowdRestException("Unexpected status code: " + crowdResponse.getStatus());
        }
    }
}
