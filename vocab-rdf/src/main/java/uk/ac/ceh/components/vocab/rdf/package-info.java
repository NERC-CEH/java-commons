@XmlSchema(
    elementFormDefault = XmlNsForm.QUALIFIED,
    attributeFormDefault = XmlNsForm.QUALIFIED,
    namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
    xmlns = {
        @XmlNs(prefix = "skos", namespaceURI = "http://www.w3.org/2004/02/skos/core#"),
        @XmlNs(prefix = "rdf", namespaceURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    }
)
@XmlAccessorType(XmlAccessType.FIELD)
package uk.ac.ceh.components.vocab.rdf;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
