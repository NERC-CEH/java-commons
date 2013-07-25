package uk.ac.ceh.components.userstore.crowd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 *
 * @author Christopher Johnson
 */
@Data
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(Include.NON_NULL)
public class CrowdUser {
    public String name, email;
    public @JsonProperty("first-name") String firstname;
    public @JsonProperty("last-name") String lastname;
    public @JsonProperty("display-name") String displayname;
    public boolean active = true;
    
    public CrowdAttributes attributes = new CrowdAttributes();
    public CrowdUserPassword password;
}
