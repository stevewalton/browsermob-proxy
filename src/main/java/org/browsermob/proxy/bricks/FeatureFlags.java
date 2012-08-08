package org.browsermob.proxy.bricks;

public class FeatureFlags {
    private boolean enhancedReplies = false;
    private boolean paramLogs = false;
    private boolean headerGetDelete = true;

    /**
     * private constructor
     */
    private FeatureFlags() {

    }

    public boolean getEnhancedReplies() {
        return this.enhancedReplies;
    }

    public void setEnhancedReplies(boolean enhancedReplies) {
        this.enhancedReplies = enhancedReplies;
    }

    public boolean getHeaderGetDelete() {
        return this.headerGetDelete;
    }

    public boolean getParamLogs() {
        return this.paramLogs;
    }

    public void setParamLogs(boolean paramLogs) {
        this.paramLogs = paramLogs;
    }

    public static FeatureFlags getInstance() {
        FeatureFlags instance = null;
        
        if (instance == null) {
            instance = new FeatureFlags();
        }

        return instance;
    }
}
