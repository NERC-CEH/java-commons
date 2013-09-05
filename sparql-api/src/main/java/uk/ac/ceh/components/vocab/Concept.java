package uk.ac.ceh.components.vocab;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 *
 * @author Christopher Johnson
 */
@Data
@Accessors(chain=true)
public class Concept {
    private String term, uri;
}