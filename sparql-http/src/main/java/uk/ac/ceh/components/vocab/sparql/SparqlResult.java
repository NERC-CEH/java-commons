package uk.ac.ceh.components.vocab.sparql;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import lombok.Data;

/**
 *
 * @author Christopher Johnson
 */
@Data
public class SparqlResult {
    @XmlElement(name="binding")
    private List<Binding> bindings;

    @Data
    public static class Binding {
        @XmlElement
        private String uri;

        @XmlElement(name="literal")
        private String term;
    }
}