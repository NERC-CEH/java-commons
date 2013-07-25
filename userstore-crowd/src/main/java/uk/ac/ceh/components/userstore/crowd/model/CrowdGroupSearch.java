package uk.ac.ceh.components.userstore.crowd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

/**
 *
 * @author Christopher Johnson
 */
@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class CrowdGroupSearch {
    private List<CrowdGroup> groups;
}
