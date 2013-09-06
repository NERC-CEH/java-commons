package uk.ac.ceh.components.vocab.sparql;

import lombok.Data;
import lombok.experimental.Accessors;
import uk.ac.ceh.components.vocab.Concept;

/**
 *
 * @author Christopher Johnson
 */
@Data
@Accessors(chain=true)
public class SparqlConcept implements Concept {
    private String term, uri;
}
