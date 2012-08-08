package org.browsermob.proxy.bricks;

import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Delete;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;

@At("/features")
@Service
public class ProxyFeatures extends BaseBrick {

    @Get
    public Reply<?> getFeatures() {
        LOG.info("GET /features");

        try {
            return(this.wrapSuccess(featureFlags));
        }
        catch (Exception e) {
            return this.wrapError(e);
        }
    }

    @Post
    @At("/enhancedReplies")
    public Reply<?> setEnhancedReplies(Request request) {
        LOG.info("POST /features/enhancedreplies");

        String rawParam = request.param("enhancedReplies");
        if (rawParam == null)
        {
            return this.wrapError("Missing param enhancedReplies");
        }

        Boolean enhancedReplies = Boolean.parseBoolean(rawParam);

        this.logParam("rawParam", rawParam);
        this.logParam("enhancedReplies", enhancedReplies);

        featureFlags.setEnhancedReplies(enhancedReplies);

        return this.wrapEmptySuccess();
    }

    @Delete
    @At("/enhancedReplies")
    public Reply<?> deleteEnhancedReplies() {
        LOG.info("DELETE /features/enhancedReplies");
        
        featureFlags.setEnhancedReplies(false);
        
        return this.wrapEmptySuccess();
    }

    @Post
    @At("/paramLogs")
    public Reply<?> setParamLogs(Request request) {
        LOG.info("POST /features/paramLogs");

        String rawParam = request.param("paramLogs");
        if (rawParam == null)
        {
            return this.wrapError("Missing param paramLogs");
        }

        Boolean paramLogs = Boolean.parseBoolean(rawParam);

        this.logParam("rawParam", rawParam);
        this.logParam("paramLogs", paramLogs);

        featureFlags.setParamLogs(paramLogs);

        return this.wrapEmptySuccess();
    }

    @Delete
    @At("/paramLogs")
    public Reply<?> deleteParamLogs() {
        LOG.info("DELETE /features/paramLogs");

        featureFlags.setParamLogs(false);

        return this.wrapEmptySuccess();
    }
}
