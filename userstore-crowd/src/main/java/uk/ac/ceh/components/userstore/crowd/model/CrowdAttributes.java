package uk.ac.ceh.components.userstore.crowd.model;

import java.util.List;
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
public class CrowdAttributes {
    private List<CrowdAttribute> attributes;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown=true)
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public static class CrowdAttribute {
        private String name;
        private List<String> values;
    }
}
