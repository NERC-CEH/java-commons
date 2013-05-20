package uk.ac.ceh.components.userstore.crowd.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private List<CrowdAttribute> attributes = new ArrayList<>();
    
    public void add(CrowdAttribute toAdd) {
        attributes.add(toAdd);
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown=true)
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CrowdAttribute {
        private String name;
        private List<String> values;
    }
}
