package uk.ac.ceh.components.userstore.crowd;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import static com.sun.jersey.api.client.ClientResponse.Status.NOT_FOUND;
import static com.sun.jersey.api.client.ClientResponse.Status.OK;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.WritableGroupStore;

/**
 *
 * @author Christopher Johnson
 */
public class CrowdGroupStore<U extends User> implements WritableGroupStore<U> {
    private WebResource crowd;
    
    public CrowdGroupStore(URI crowd, String app, String password) {
        ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        Client client = Client.create(config);
        client.addFilter(new HTTPBasicAuthFilter(app, password));
        
        this.crowd = client.resource(crowd);
    }
    
    @Override
    public List<Group> getGroups(U user) {
        GenericType<List<Group>> genericType = new GenericType<List<Group>>() {};
        
        ClientResponse crowdResponse = crowd.path("user/group/direct")
                                            .queryParam("username", user.getUsername())
                                            .accept(MediaType.APPLICATION_JSON)
                                            .get(ClientResponse.class);
        
        switch(crowdResponse.getClientResponseStatus()) {
            case OK: return crowdResponse.getEntity(genericType);
            case NOT_FOUND: throw new IllegalArgumentException("The specified user is not known to crowd");
            default: throw new CrowdRestException("Unexpected status code: " + crowdResponse.getStatus());
        }
    }

    @Override
    public Group getGroup(String groupname) throws IllegalArgumentException {
        ClientResponse crowdResponse = getCrowdGroupClientResponse(groupname);
        
        switch(crowdResponse.getClientResponseStatus()) {
            case OK : return crowdResponse.getEntity(CrowdGroup.class);
            case NOT_FOUND : throw new IllegalArgumentException("The specified group does not exist");
            default: throw new CrowdRestException("Unexpected status code: " + crowdResponse.getStatus());
        }
    }
    
    @Override
    public List<Group> getAllGroups() {
        ClientResponse crowdResponse = crowd.path("search")
                                            .queryParam("entity-type", "group")
                                            //.queryParam("expand", "group")
                                            .accept(MediaType.APPLICATION_JSON)
                                            .get(ClientResponse.class);
        
        switch(crowdResponse.getClientResponseStatus()) {
            case OK : return new ArrayList<Group>(crowdResponse.getEntity(CrowdGroupSearch.class)
                                                                .getGroups());
            default: throw new CrowdRestException("Unexpected status code: " + crowdResponse.getStatus());
        }
    }

    @Override
    public boolean isGroupInExistance(String groupname) {
        ClientResponse crowdResponse = getCrowdGroupClientResponse(groupname);
        switch(crowdResponse.getClientResponseStatus()) {
            case OK : return true;
            case NOT_FOUND : return false;
            default: throw new CrowdRestException("Unexpected status code: " + crowdResponse.getStatus());
        }
        
    }

    @Override
    public boolean isGroupDeletable(String groupname) throws IllegalArgumentException {
        getGroup(groupname); //attempt to get the group with the given name
        return true;    //all groups in crowd are deletable
    }

    @Override
    public Group createGroup(String groupname, String description) throws IllegalArgumentException {
        CrowdGroup newGroup = new CrowdGroup(groupname, description);
        
        ClientResponse crowdResponse = crowd.path("group")
                                            .type(MediaType.APPLICATION_JSON)
                                            .post(ClientResponse.class, newGroup);
        switch(crowdResponse.getClientResponseStatus()) {
            case CREATED : return newGroup;
            case BAD_REQUEST : throw new IllegalArgumentException("The specified group already exists");
            case FORBIDDEN:
            default: throw new CrowdRestException("Unexpected status code: " + crowdResponse.getStatus());
        }
    }

    @Override
    public Group updateGroup(String groupname, String description) throws IllegalArgumentException {
        CrowdGroup updatedGroup = new CrowdGroup(groupname, description);
        
        ClientResponse crowdResponse = crowd.path("group")
                                            .queryParam("groupname", groupname)
                                            .type(MediaType.APPLICATION_JSON)
                                            .put(ClientResponse.class, updatedGroup);
        switch(crowdResponse.getClientResponseStatus()) {
            case OK : return updatedGroup;
            case FORBIDDEN: //throw some exception
            case NOT_FOUND: throw new IllegalArgumentException("The given group does not exist");
            default: throw new CrowdRestException("Unexpected status code: " + crowdResponse.getStatus());
        }
    }

    @Override
    public boolean deleteGroup(String groupname) throws IllegalArgumentException {
        ClientResponse crowdResponse = crowd.path("group")
                                            .queryParam("groupname", groupname)
                                            .delete(ClientResponse.class);
        switch(crowdResponse.getClientResponseStatus()) {
            case NO_CONTENT : return true; //successfully deleted
            case NOT_FOUND : throw new IllegalArgumentException("The given group does not exist");
            default: throw new CrowdRestException("Unexpected status code: " + crowdResponse.getStatus());
        }
    }

    @Override
    public boolean grantGroupToUser(U user, String groupname) throws IllegalArgumentException {
        ClientResponse crowdResponse = crowd.path("group/user/direct")
                                            .queryParam("groupname", groupname)
                                            .type(MediaType.APPLICATION_JSON)
                                            .post(ClientResponse.class, user);
        switch(crowdResponse.getClientResponseStatus()) {
            case CREATED : return true;
            case BAD_REQUEST: throw new IllegalArgumentException("The specified user is not known");
            case NOT_FOUND:throw new IllegalArgumentException("The given group does not exist");
            default: throw new CrowdRestException("Unexpected status code: " + crowdResponse.getStatus());
        }
    }

    @Override
    public boolean revokeGroupFromUser(U user, String groupname) throws IllegalArgumentException {
        ClientResponse crowdResponse = crowd.path("group/user/direct")
                                            .queryParam("groupname", groupname)
                                            .queryParam("username", user.getUsername())
                                            .delete(ClientResponse.class);
        switch(crowdResponse.getClientResponseStatus()) {
            case NO_CONTENT : return true;
            case NOT_FOUND : throw new IllegalArgumentException("Either the group or username don't exist");
            default: throw new CrowdRestException("Unexpected status code: " + crowdResponse.getStatus());
        }
    }
    
    private ClientResponse getCrowdGroupClientResponse(String groupname) {
        return crowd.path("group")
                    .queryParam("groupname", groupname)
                    .accept(MediaType.APPLICATION_JSON)
                    .get(ClientResponse.class);
    }
}