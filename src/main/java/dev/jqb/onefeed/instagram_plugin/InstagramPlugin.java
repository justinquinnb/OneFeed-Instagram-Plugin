package dev.jqb.onefeed.instagram_plugin;

import java.util.ArrayList;
import java.util.List;
import org.pf4j.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstagramPlugin extends Plugin {
    private static final Logger logger = LoggerFactory.getLogger(InstagramPlugin.class);
    private static final RequestHandler requestHandler = new RequestHandler();

    @Override
    public void start() {
        logger.info("Instagram plugin started!");
    }

    private List<String> getApiKeys(String envLocation) {

        return new ArrayList<>();
    }

    private String getPluginEnvPrefix() {
        // Probably a better way to do this...
        // plugin.properties would be great if it wasn't for the fact that multiple plugins
        // would be trying to read from the same one in a plugins folder, requiring UIDs which
        // this is supposed to be
        return "INSTAGRAM_";
    }
}
