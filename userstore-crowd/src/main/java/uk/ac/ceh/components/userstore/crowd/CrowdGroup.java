package uk.ac.ceh.components.userstore.crowd;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import uk.ac.ceh.components.userstore.Group;

/**
 *
 * @author Christopher Johnson
 */
@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CrowdGroup implements Group {
    private String name, description;
}
