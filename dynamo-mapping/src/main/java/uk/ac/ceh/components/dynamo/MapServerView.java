package uk.ac.ceh.components.dynamo;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.web.servlet.View;

/**
 *
 * @author Christopher Johnson
 */
public class MapServerView implements View {    
    private static final String URL_PARAMETER_ENCODING = "UTF-8";
    private final URL mapServerURL;
    private final Template mapFileTemplate;
    private final File templateDirectory;

    /**
     * Creates a MapServerView for the given mapFileTemplate to be called against
     * a given mapServer
     * @param mapServerURL The url for the mapserver which this view should be rendered against
     * @param mapFileTemplate The map template to process to create a map file to pass to mapserver
     * @param templateDirectory The folder which the template was loaded from 
     *  and to use for creating the temporary map file to pass to mapserver
     */
    public MapServerView(URL mapServerURL, Template mapFileTemplate, File templateDirectory) {
        this.mapServerURL = mapServerURL;
        this.mapFileTemplate = mapFileTemplate;
        this.templateDirectory = templateDirectory;
    }
    
    /**
     * Let MapServer report it's content type.
     * @return always null
     */
    @Override public String getContentType() {
        return null;
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse servletResponse) throws Exception {
        Response response = queryMapServer(model, request);
        servletResponse.setContentType(response.getContentType());
        copyAndClose(response.getInputStream(), servletResponse);
    }
    
    private Response queryMapServer(Map<String, ?> model, HttpServletRequest request) throws IOException, TemplateException {
        File mapFile = getMapFile(model);
        try {
            HttpURLConnection conn = (HttpURLConnection)mapServerURL.openConnection();
            conn.setDoOutput(true);
            String requestToPost = getQueryFromMap(getMapServerRequest(mapFile, request));
            try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream())) {
                wr.write(requestToPost);
                wr.flush();
            }
            return new Response(conn.getContentType(),conn.getInputStream());
        }
        finally {
            mapFile.delete();
        }
    }
    
    private File getMapFile(Map<String, ?> model) throws IOException, TemplateException {
        // File output
        File file = File.createTempFile("generated", ".map", templateDirectory);
        try (Writer out = new FileWriter (file)) {
            mapFileTemplate.process(model, out);
            out.flush();
        }
        return file;
    }
    
    private static Map<String, String[]> getMapServerRequest(File mapFile, HttpServletRequest request) {
        Map<String, String[]> modifiedQuery = new HashMap<>(request.getParameterMap());
        modifiedQuery.put("map", new String[] {mapFile.getAbsolutePath()});
        return modifiedQuery;
    }
    
    private static String getQueryFromMap(Map<String, String[]> query) throws UnsupportedEncodingException {
        StringBuilder toReturn = new StringBuilder();
        
        for(Map.Entry<String, String[]> entry : query.entrySet()) {
            for(String currValue : entry.getValue()) {
                toReturn.append(URLEncoder.encode(entry.getKey(), URL_PARAMETER_ENCODING))
                        .append("=")
                        .append(URLEncoder.encode(currValue, URL_PARAMETER_ENCODING))
                        .append("&");
            }
        }
        return toReturn.substring(0, toReturn.length()-1);
    }
    
    private static void copyAndClose(InputStream in, HttpServletResponse response) throws IOException {
        try (ServletOutputStream out = response.getOutputStream()) {
            try {
                IOUtils.copy(in, out);
            }
            finally {
                in.close();
            }
        }
    }
}
