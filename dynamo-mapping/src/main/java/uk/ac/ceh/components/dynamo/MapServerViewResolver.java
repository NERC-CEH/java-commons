package uk.ac.ceh.components.dynamo;

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

/**
 *
 * @author Christopher Johnson
 */
public class MapServerViewResolver implements ViewResolver {
    private final URL mapServerURL;
    private final Configuration config;
    private final File templateDirectory;
    
    public MapServerViewResolver(File templateDirectory, URL mapServerURL) throws IOException {
        this.config = new Configuration();
        this.mapServerURL = mapServerURL;
        this.templateDirectory = templateDirectory;
        config.setDirectoryForTemplateLoading(templateDirectory);
    }
    
    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        try {
            Template mapFileTemplate = config.getTemplate(viewName);
            File templatesParent = new File(templateDirectory, viewName).getParentFile();
            return new MapServerView(mapServerURL, mapFileTemplate, templatesParent);
        }
        catch(FileNotFoundException fnfe) {
            return null;
        }
    }
}
