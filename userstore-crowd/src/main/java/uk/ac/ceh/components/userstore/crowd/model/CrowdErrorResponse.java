package uk.ac.ceh.components.userstore.crowd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 *
 * @author Christopher Johnson
 */
@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class CrowdErrorResponse {
    public String reason, message;
}
