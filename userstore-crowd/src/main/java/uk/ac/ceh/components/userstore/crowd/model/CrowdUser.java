package uk.ac.ceh.components.userstore.crowd.model;

import javax.xml.bind.annotation.XmlElement;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 *
 * @author Christopher Johnson
 */
@Data
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class CrowdUser {
    public String name, email;
    public @XmlElement(name="first-name") String firstname;
    public @XmlElement(name="last-name") String lastname;
    public @XmlElement(name="display-name") String displayname;
    public boolean active = true;
    
    public CrowdAttributes attributes = new CrowdAttributes();
    public CrowdUserPassword password;
}
