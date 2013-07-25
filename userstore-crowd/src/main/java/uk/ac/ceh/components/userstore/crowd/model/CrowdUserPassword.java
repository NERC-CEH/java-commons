package uk.ac.ceh.components.userstore.crowd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
