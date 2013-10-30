package uk.ac.ceh.components.dynamo;

import java.io.InputStream;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author Christopher Johnson
 */
@Data
@AllArgsConstructor
class Response {
    private final String contentType;
    private InputStream inputStream;
}
