package uk.ac.ceh.components.userstore.crowd;

import lombok.Data;

/**
 *
 * @author Christopher Johnson
 */
@Data
public class CrowdErrorResponse {
    public String reason, message;
}
