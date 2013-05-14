package uk.ac.ceh.components.userstore.crowd;

import java.util.List;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 *
 * @author Christopher Johnson
 */
@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class CrowdUserSearch {
    private List<CrowdUser> users;
}
