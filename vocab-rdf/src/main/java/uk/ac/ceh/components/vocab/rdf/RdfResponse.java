package uk.ac.ceh.components.vocab.rdf;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

/**
 *
 * @author Christopher Johnson
 */
@Data
@XmlRootElement(name="RDF")
public class RdfResponse {
    @XmlElement(name="Description")
    private List<RdfDescription> descriptions;
    
    @XmlElement(name="Concept", namespace = "http://www.w3.org/2004/02/skos/core#")
    private List<RdfDescription> concepts;
    
    public List<RdfDescription> getDescriptions() {
        if (descriptions != null) {
            return descriptions;
        } else if(concepts != null) {
            return concepts;
        } else {
            return Collections.emptyList();
        }
    }
    
    @XmlAttribute(name="base", namespace="http://www.w3.org/XML/1998/namespace")
    private URI base;
}
