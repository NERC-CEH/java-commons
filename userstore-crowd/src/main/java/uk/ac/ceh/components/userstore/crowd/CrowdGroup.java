package uk.ac.ceh.components.userstore.crowd;

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
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class CrowdGroup implements Group {
    @NonNull private String name, description;
    private boolean active = true;
    private String type = "GROUP";
}
