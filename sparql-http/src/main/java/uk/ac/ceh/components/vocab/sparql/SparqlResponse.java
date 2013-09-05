package uk.ac.ceh.components.vocab.sparql;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

/**
 *
 * @author Christopher Johnson
 */
@Data
@XmlRootElement(name="sparql")
public class SparqlResponse {
    
    @XmlElementWrapper(name="results")
    @XmlElement(name="result")
    private List<SparqlResult> results;
}
