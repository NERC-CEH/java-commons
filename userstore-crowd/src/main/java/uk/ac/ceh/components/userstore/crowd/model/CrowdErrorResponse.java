package uk.ac.ceh.components.userstore.crowd.model;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 *
 * @author Christopher Johnson
 */
@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class CrowdErrorResponse {
    public String reason, message;
}
