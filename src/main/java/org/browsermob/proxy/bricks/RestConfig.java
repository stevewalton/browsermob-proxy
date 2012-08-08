package org.browsermob.proxy.bricks;

/**
 *
 * @author stuart
 */
public class RestConfig {
    private boolean enhancedReplies = false;

    /**
     * private constructor
     */
    private RestConfig() {

    }

    public boolean getEnhancedReplies() {
        return this.enhancedReplies;
    }

    public void setEnhancedReplies(boolean enhancedReplies) {
        this.enhancedReplies = enhancedReplies;
    }

    public static RestConfig getInstance() {
        RestConfig instance = null;
        
        if (instance == null) {
            instance = new RestConfig();
        }

        return instance;
    }
}
