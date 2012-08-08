package org.browsermob.proxy.bricks;

import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;

@At("/config")
@Service
public class ProxyConfig extends BaseBrick {

    @Get
    public Reply<?> getConfig() {
        LOG.info("GET /config");

        try {
            return(this.wrapSuccess(restConfig));
        }
        catch (Exception e) {
            return this.wrapError(e);
        }
    }

    @Post
    @At("/enhancedReplies")
    public Reply<?> setEnhancedReplies(Request request) {
        LOG.info("POST /config/enhancedreplies");

        String rawParam = request.param("enhancedReplies");
        if (rawParam == null)
        {
            return this.wrapError("Missing param enhancedReplies");
        }

        Boolean enhancedReplies = Boolean.parseBoolean(rawParam);

        this.logParam("rawParam", rawParam);
        this.logParam("enhancedReplies", enhancedReplies);

        restConfig.setEnhancedReplies(enhancedReplies);

        return this.wrapEmptySuccess();
    }

    @Post
    @At("/paramLogs")
    public Reply<?> setParamLogs(Request request) {
        LOG.info("POST /config/paramLogs");

        String rawParam = request.param("paramLogs");
        if (rawParam == null)
        {
            return this.wrapError("Missing param paramLogs");
        }

        Boolean paramLogs = Boolean.parseBoolean(rawParam);

        this.logParam("rawParam", rawParam);
        this.logParam("paramLogs", paramLogs);

        restConfig.setParamLogs(paramLogs);

        return this.wrapEmptySuccess();
    }
}
