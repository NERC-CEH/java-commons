package uk.ac.ceh.components.userstore.crowd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Christopher Johnson
 */
@Data
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrowdAttributes {
    private List<CrowdAttribute> attributes = new ArrayList<>();
    
    public void add(CrowdAttribute toAdd) {
        attributes.add(toAdd);
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown=true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CrowdAttribute {
        private String name;
        private List<String> values;
    }
}
