package uk.ac.ceh.components.userstore.crowd;

import uk.ac.ceh.components.userstore.crowd.model.CrowdUserSearch;
import uk.ac.ceh.components.userstore.crowd.model.CrowdUserPassword;
import uk.ac.ceh.components.userstore.crowd.model.CrowdErrorResponse;
import uk.ac.ceh.components.userstore.crowd.model.CrowdUser;
import com.google.common.collect.Collections2;
import com.sun.jersey.api.client.ClientResponse;
import static com.sun.jersey.api.client.ClientResponse.Status.FORBIDDEN;
import static com.sun.jersey.api.client.ClientResponse.Status.NOT_FOUND;
import static com.sun.jersey.api.client.ClientResponse.Status.NO_CONTENT;
import com.sun.jersey.api.client.WebResource;
import java.util.Collection;
import javax.ws.rs.core.MediaType;
import uk.ac.ceh.components.userstore.InvalidCredentialsException;
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.components.userstore.User;
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
    private final TransformDomainUserToCrowdUser<U> reader;
    
    public CrowdUserStore(CrowdApplicationCredentials credentials, 
                                                UserBuilderFactory<U> userFactory, 
                                                UserAttributeReader<U> reader) {
        this.crowd = credentials.getCrowdJerseryResource();
        this.reader = new TransformDomainUserToCrowdUser<>(reader);
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
            default: throw new CrowdRestException(crowdResponse.getEntity(CrowdErrorResponse.class));  
        }
    }
    
    @Override
    public void addUser(U user, String password) throws UsernameAlreadyTakenException, InvalidCredentialsException {
        //Build a crowd user from the given user
        CrowdUser newUser = reader.apply(user);
        newUser.setPassword(new CrowdUserPassword(password));
        
        ClientResponse crowdResponse = crowd.path("user")
                                            .type(MediaType.APPLICATION_JSON)
                                            .post(ClientResponse.class, newUser);
        
        switch(crowdResponse.getClientResponseStatus()) {
            case CREATED : return;
            case BAD_REQUEST : throw new UsernameAlreadyTakenException("The given username is already taken");
            default: throw new CrowdRestException(crowdResponse.getEntity(CrowdErrorResponse.class));
        }
    }

    @Override
    public void updateUser(U user) throws UnknownUserException {
        //Build a crowd user from the given user
        CrowdUser newUser = reader.apply(user);
        
        ClientResponse crowdResponse = crowd.path("user")
                                            .queryParam("username", user.getUsername())
                                            .type(MediaType.APPLICATION_JSON)
                                            .put(ClientResponse.class, newUser);

        switch(crowdResponse.getClientResponseStatus()) {
            case NO_CONTENT : return;
            case NOT_FOUND : throw new UnknownUserException("Can not update the user as the username is not known");
            default: throw new CrowdRestException(crowdResponse.getEntity(CrowdErrorResponse.class));
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
            default: throw new CrowdRestException(crowdResponse.getEntity(CrowdErrorResponse.class));
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
            default: throw new CrowdRestException(crowdResponse.getEntity(CrowdErrorResponse.class));
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
            default: throw new CrowdRestException(crowdResponse.getEntity(CrowdErrorResponse.class));
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
            default: throw new CrowdRestException(crowdResponse.getEntity(CrowdErrorResponse.class));
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
            default: throw new CrowdRestException(crowdResponse.getEntity(CrowdErrorResponse.class));
        }
    }
}
