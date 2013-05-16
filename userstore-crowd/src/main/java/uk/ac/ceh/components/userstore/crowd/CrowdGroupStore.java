package uk.ac.ceh.components.userstore.crowd;

import uk.ac.ceh.components.userstore.crowd.model.CrowdGroup;
import com.sun.jersey.api.client.ClientResponse;
import static com.sun.jersey.api.client.ClientResponse.Status.NOT_FOUND;
import static com.sun.jersey.api.client.ClientResponse.Status.OK;
import com.sun.jersey.api.client.WebResource;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.WritableGroupStore;
import uk.ac.ceh.components.userstore.crowd.model.CrowdErrorResponse;
import uk.ac.ceh.components.userstore.crowd.model.CrowdGroupSearch;

/**
 * The following class defines a WritableGroupStore implementation which is 
 * powered by Atlassian's Crowd Rest API
 * 
 * @author Christopher Johnson
 */
public class CrowdGroupStore<U extends User> implements WritableGroupStore<U> {
    private WebResource crowd;
    
    public CrowdGroupStore(CrowdApplicationCredentials credentials) {
        this.crowd = credentials.getCrowdJerseryResource();
    }
    
    @Override
    public List<Group> getGroups(U user) {        
        ClientResponse crowdResponse = crowd.path("user/group/direct")
                                            .queryParam("username", user.getUsername())
                                            .queryParam("expand", "group")
                                            .accept(MediaType.APPLICATION_JSON)
                                            .get(ClientResponse.class);
        
        switch(crowdResponse.getClientResponseStatus()) {
            case OK: return new ArrayList<Group>(crowdResponse.getEntity(CrowdGroupSearch.class).getGroups());
            case NOT_FOUND: throw new IllegalArgumentException("The specified user is not known to crowd");
            default: throw new CrowdRestException(crowdResponse.getEntity(CrowdErrorResponse.class));
        }
    }

    @Override
    public Group getGroup(String groupname) throws IllegalArgumentException {
        ClientResponse crowdResponse = getCrowdGroupClientResponse(groupname);
        
        switch(crowdResponse.getClientResponseStatus()) {
            case OK : return crowdResponse.getEntity(CrowdGroup.class);
            case NOT_FOUND : throw new IllegalArgumentException("The specified group does not exist");
            default: throw new CrowdRestException(crowdResponse.getEntity(CrowdErrorResponse.class));
        }
    }
    
    @Override
    public List<Group> getAllGroups() {
        ClientResponse crowdResponse = crowd.path("search")
                                            .queryParam("entity-type", "group")
                                            .queryParam("expand", "group")
                                            .accept(MediaType.APPLICATION_JSON)
                                            .get(ClientResponse.class);
        
        switch(crowdResponse.getClientResponseStatus()) {
            case OK : return new ArrayList<Group>(crowdResponse.getEntity(CrowdGroupSearch.class)
                                                                .getGroups());
            default: throw new CrowdRestException(crowdResponse.getEntity(CrowdErrorResponse.class));
        }
    }

    @Override
    public boolean isGroupInExistance(String groupname) {
        ClientResponse crowdResponse = getCrowdGroupClientResponse(groupname);
        switch(crowdResponse.getClientResponseStatus()) {
            case OK : return true;
            case NOT_FOUND : return false;
            default: throw new CrowdRestException(crowdResponse.getEntity(CrowdErrorResponse.class));
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
            default: throw new CrowdRestException(crowdResponse.getEntity(CrowdErrorResponse.class));
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
            default: throw new CrowdRestException(crowdResponse.getEntity(CrowdErrorResponse.class));
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
            default: throw new CrowdRestException(crowdResponse.getEntity(CrowdErrorResponse.class));
        }
    }

    @Override
    public boolean grantGroupToUser(U user, String groupname) throws IllegalArgumentException {
        ClientResponse crowdResponse = crowd.path("user/group/direct")
                                            .queryParam("username", user.getUsername())
                                            .type(MediaType.APPLICATION_JSON)
                                            .post(ClientResponse.class, new CrowdGroup(groupname));
        switch(crowdResponse.getClientResponseStatus()) {
            case CREATED : return true;
            case BAD_REQUEST: throw new IllegalArgumentException("The specified user is not known");
            case NOT_FOUND:throw new IllegalArgumentException("The given group does not exist");
            default: throw new CrowdRestException(crowdResponse.getEntity(CrowdErrorResponse.class));
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
            default: throw new CrowdRestException(crowdResponse.getEntity(CrowdErrorResponse.class));
        }
    }
    
    private ClientResponse getCrowdGroupClientResponse(String groupname) {
        return crowd.path("group")
                    .queryParam("groupname", groupname)
                    .accept(MediaType.APPLICATION_JSON)
                    .get(ClientResponse.class);
    }
}
