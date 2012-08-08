package org.browsermob.proxy.bricks;

/**
 *
 * @author stuart
 */
public class RestConfig {
    private boolean enhancedReplies = false;
    private boolean paramLogs = false;

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

    public boolean getParamLogs() {
        return this.paramLogs;
    }

    public void setParamLogs(boolean paramLogs) {
        this.paramLogs = paramLogs;
    }

    public static RestConfig getInstance() {
        RestConfig instance = null;
        
        if (instance == null) {
            instance = new RestConfig();
        }

        return instance;
    }
}
