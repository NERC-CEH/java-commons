package uk.ac.ceh.components.userstore.crowd.model;

import java.util.List;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 *
 * @author Christopher Johnson
 */
@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class CrowdGroupSearch {
    private List<CrowdGroup> users;
}
