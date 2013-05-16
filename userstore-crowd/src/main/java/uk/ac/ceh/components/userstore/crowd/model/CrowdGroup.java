package uk.ac.ceh.components.userstore.crowd.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import uk.ac.ceh.components.userstore.Group;

/**
 *
 * @author Christopher Johnson
 */
@Data
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class CrowdGroup implements Group {
    @NonNull private String name;
    private boolean active = true;
    private String description, type = "GROUP";
    
    public CrowdGroup(String name, String description) {
        this(name);
        this.description = description;
    }
}
