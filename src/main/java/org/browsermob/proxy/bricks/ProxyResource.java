package org.browsermob.proxy.bricks;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.sitebricks.At;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Delete;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.http.Put;
import java.util.Hashtable;
import java.util.Map;
import org.browsermob.core.har.Har;
import org.browsermob.proxy.ProxyManager;
import org.browsermob.proxy.ProxyServer;

@At("/proxy")
@Service
public class ProxyResource extends BaseBrick {
    private ProxyManager proxyManager;
    
    @Inject
    public ProxyResource(ProxyManager proxyManager) {
        this.proxyManager = proxyManager;
    }

    @Post
    public Reply<?> newProxy(Request request) throws Exception {
        LOG.info("POST /proxy");

        try {
            String httpProxy = request.param("httpProxy");
            this.logParam("httpProxy", httpProxy);

            Hashtable<String, String> options = new Hashtable<String, String>();
            if (httpProxy != null) {
                options.put("httpProxy", httpProxy);
            }

            String paramPort = request.param("port");
            this.logParam("port", paramPort);

            int port = 0;
            if (paramPort != null) {
                port = Integer.parseInt(paramPort);
                ProxyServer proxy = proxyManager.create(options, port);
            } else {
                ProxyServer proxy = proxyManager.create(options);
                port = proxy.getPort();
            }

            return this.wrapSuccess(new ProxyDescriptor(port));
        }
        catch (Exception e)
        {
            return this.wrapError(e.toString());
        }
    }

    @Get
    @At("/:port/har")
    public Reply<?> getHar(@Named("port") int port) {
        LOG.info("GET /proxy/:port/har");

        try
        {
            ProxyServer proxy = proxyManager.get(port);
            Har har = proxy.getHar();

            return this.wrapSuccess(har);
        }
        catch (Exception e)
        {
            return this.wrapError(e.toString());
        }
    }

    @Put
    @At("/:port/har")
    public Reply<?> newHar(@Named("port") int port, Request request) {
        LOG.info("PUT /proxy/:port/har");

        try
        {
            String initialPageRef = request.param("initialPageRef");
            this.logParam("initialPageRef", initialPageRef);
            ProxyServer proxy = proxyManager.get(port);
            Har oldHar = proxy.newHar(initialPageRef);

            String captureHeaders = request.param("captureHeaders");
            this.logParam("captureHeaders", captureHeaders);
            proxy.setCaptureHeaders(Boolean.parseBoolean(captureHeaders));

            String captureContent = request.param("captureContent");
            this.logParam("captureContent", captureContent);
            proxy.setCaptureContent(Boolean.parseBoolean(captureContent));

            if (oldHar != null) {
                return this.wrapSuccess(oldHar);
            } else {
                return this.wrapEmptySuccess();
            }
        }
        catch (Exception e)
        {
            return this.wrapError(e.toString());
        }
    }

    @Put
    @At("/:port/har/pageRef")
    public Reply<?> setPage(@Named("port") int port, Request request) {
        LOG.info("PUT /proxy/:port/har/pageRef");

        try
        {
            String pageRef = request.param("pageRef");
            this.logParam("pageRef", pageRef);

            ProxyServer proxy = proxyManager.get(port);
            proxy.newPage(pageRef);

            return this.wrapEmptySuccess();
        }
        catch (Exception e)
        {
            return this.wrapError(e.toString());
        }
    }

    @Put
    @At("/:port/blacklist")
    public Reply<?> blacklist(@Named("port") int port, Request request) {
        LOG.info("PUT /proxy/:port/blacklist");

        try
        {
            String regex = request.param("regex");
            this.logParam("regex", regex);

            this.logParam("status", request.param("status"));
            int responseCode = parseResponseCode(request.param("status"));
            this.logParam("responseCode", responseCode);

            ProxyServer proxy = proxyManager.get(port);
            proxy.blacklistRequests(regex, responseCode);

            return this.wrapEmptySuccess();
        }
        catch (Exception e)
        {
            return this.wrapError(e.toString());
        }
    }

    @Put
    @At("/:port/whitelist")
    public Reply<?> whitelist(@Named("port") int port, Request request) {
        LOG.info("PUT /proxy/:port/whitelist");

        try
        {
            String regex = request.param("regex");
            this.logParam("regex", regex);

            this.logParam("status", request.param("status"));
            int responseCode = parseResponseCode(request.param("status"));
            this.logParam("responseCode", responseCode);

            ProxyServer proxy = proxyManager.get(port);
            proxy.whitelistRequests(regex.split(","), responseCode);

            return this.wrapEmptySuccess();
        }
        catch (Exception e)
        {
            return this.wrapError(e.toString());
        }
    }

    @Post
    @At("/:port/headers")
    public Reply<?> updateHeaders(@Named("port") int port, Request request) {
        LOG.info("POST /proxy/:port/headers");

        try
        {
            ProxyServer proxy = proxyManager.get(port);
            Map<String, String> headers = request.read(Map.class).as(Json.class);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                this.logParam("HEADER NAME ", key);
                this.logParam("HEADER VALUE", value);
                proxy.addHeader(key, value);
            }

            return this.wrapEmptySuccess();
        }
        catch (Exception e)
        {
            return this.wrapError(e.toString());
        }
    }

    @Delete
    @At("/:port/headers")
    public Reply<?> removeAllHeaders(@Named("port") int port, Request request) {
        LOG.info("DELETE /proxy/%s/headers", port);

        try
        {
            ProxyServer proxy = proxyManager.get(port);
            proxy.removeAllHeaders();

            return this.wrapEmptySuccess();
        }
        catch (Exception e)
        {
            return this.wrapError(e.toString());
        }
    }

    @Get
    @At("/:port/header/:name")
    public Reply<?> getHeader(@Named("port") int port, @Named("name") String name, Request request) {
        LOG.info("GET /proxy/%s/header/%s", port, name);

        try
        {
            ProxyServer proxy = proxyManager.get(port);
            return this.wrapSuccess(proxy.getHeader(name));
        }
        catch (Exception e)
        {
            return this.wrapError(e.toString());
        }
    }

    @Delete
    @At("/:port/header/:name")
    public Reply<?> removeHeader(@Named("port") int port, @Named("name") String name, Request request) {
        LOG.info("DELETE /proxy/%s/header/%s", port, name);

        try
        {
            ProxyServer proxy = proxyManager.get(port);
            proxy.removeHeader(name);

            return this.wrapEmptySuccess();
        }
        catch (Exception e)
        {
            return this.wrapError(e.toString());
        }
    }

    @Put
    @At("/:port/limit")
    public Reply<?> limit(@Named("port") int port, Request request) {
        LOG.info("PUT /proxy/:port/limit");

        try
        {
            ProxyServer proxy = proxyManager.get(port);
            String upstreamKbps = request.param("upstreamKbps");
            this.logParam("upstreamKbps", upstreamKbps);
            if (upstreamKbps != null) {
                try {
                    proxy.setUpstreamKbps(Integer.parseInt(upstreamKbps));
                } catch (NumberFormatException e) { }
            }
            String downstreamKbps = request.param("downstreamKbps");
            this.logParam("downstreamKbps", downstreamKbps);
            if (downstreamKbps != null) {
                try {
                    proxy.setDownstreamKbps(Integer.parseInt(downstreamKbps));
                } catch (NumberFormatException e) { }
            }
            String latency = request.param("latency");
            this.logParam("latency", latency);
            if (latency != null) {
                try {
                    proxy.setLatency(Integer.parseInt(latency));
                } catch (NumberFormatException e) { }
            }

            return this.wrapEmptySuccess();
        }
        catch (Exception e)
        {
            return this.wrapError(e.toString());
        }
    }

    @Delete
    @At("/:port")
    public Reply<?> delete(@Named("port") int port) throws Exception {
        LOG.info("DELETE /proxy/:port");

        try
        {
            proxyManager.delete(port);

            return this.wrapEmptySuccess();
        }
        catch (Exception e)
        {
            return this.wrapError(e.toString());
        }
    }

    @Post
    @At("/:port/hosts")
    public Reply<?> remapHosts(@Named("port") int port, Request request) {
        LOG.info("POST /proxy/:port/hosts");

        try
        {
            ProxyServer proxy = proxyManager.get(port);
            @SuppressWarnings("unchecked") Map<String, String> headers = request.read(Map.class).as(Json.class);

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                this.logParam("HOST NAME ", key);
                this.logParam("HOST VALUE", value);
                proxy.remapHost(key, value);
                proxy.setDNSCacheTimeout(0);
                proxy.clearDNSCache();
            }

            return this.wrapEmptySuccess();
        }
        catch (Exception e)
        {
            return this.wrapError(e.toString());
        }
    }

    @Put
    @At("/:port/basicAuth/:domain")
    public Reply<?> putHttpBasicAuth(@Named("port") int port, @Named("domain") String domain, Request request)
    {
        LOG.info("PUT /proxy/%s/basicAuth/%s", port, domain);

        try
        {
            ProxyServer proxy = proxyManager.get(port);

            String username = request.param("username");
            this.logParam("username", username);
            if (username == null)
            {
                return this.wrapError("Missing param 'username'");
            }

            String password = request.param("password");
            this.logParam("password", password);
            if (password == null)
            {
                return this.wrapError("Missing param 'password'");
            }

            proxy.autoBasicAuthorization(domain, username, password);

            return this.wrapEmptySuccess();
        }
        catch (Exception e)
        {
            return this.wrapError(e.toString());
        }
    }

    private int parseResponseCode(String response)
    {
        int responseCode = 200;
        if (response != null) {
            try {
                responseCode = Integer.parseInt(response);
            } catch (NumberFormatException e) { }
        }
        return responseCode;
    }

    public static class ProxyDescriptor {
        private int port;

        public ProxyDescriptor() {
        }

        public ProxyDescriptor(int port) {
            this.port = port;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}
