package uk.ac.ceh.components.userstore.crowd;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 *
 * @author Christopher Johnson
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class CrowdUserPassword {
    private String value;
}
