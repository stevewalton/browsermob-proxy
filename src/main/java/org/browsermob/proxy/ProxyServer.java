package org.browsermob.proxy;

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.browsermob.core.har.*;
import org.browsermob.core.util.ThreadUtils;
import org.browsermob.proxy.http.BrowserMobHttpClient;
import org.browsermob.proxy.jetty.http.HttpContext;
import org.browsermob.proxy.jetty.http.HttpListener;
import org.browsermob.proxy.jetty.http.SocketListener;
import org.browsermob.proxy.jetty.jetty.Server;
import org.browsermob.proxy.jetty.util.InetAddrPort;
import org.openqa.selenium.Proxy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class ProxyServer {
    private static final HarNameVersion CREATOR = new HarNameVersion("BrowserMob Proxy", "2.0");

    private Server server;
    private int port = -1;
    private BrowserMobHttpClient client;
    private HarPage currentPage;
    private BrowserMobProxyHandler handler;
    private int pageCount = 1;

    public ProxyServer() {
    }

    public ProxyServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        if (port == -1) {
            throw new IllegalStateException("Must set port before starting");
        }

        server = new Server();
        HttpListener listener = new SocketListener(new InetAddrPort(getPort()));
        server.addListener(listener);
        HttpContext context = new HttpContext();
        context.setContextPath("/");
        server.addContext(context);

        handler = new BrowserMobProxyHandler();
        handler.setJettyServer(server);
        handler.setShutdownLock(new Object());
        client = new BrowserMobHttpClient();
        client.prepareForBrowser();
        handler.setHttpClient(client);
        client.setDownstreamKbps(500 * 1024 * 8);

        context.addHandler(handler);

        server.start();

        setPort(listener.getPort());
    }

    public org.openqa.selenium.Proxy seleniumProxy() throws UnknownHostException {
        Proxy proxy = new Proxy();
        proxy.setProxyType(Proxy.ProxyType.MANUAL);
        String proxyStr = String.format("%s:%d", InetAddress.getLocalHost().getCanonicalHostName(),  getPort());
        proxy.setHttpProxy(proxyStr);
        proxy.setSslProxy(proxyStr);

        return proxy;
    }

    public void cleanup() {
        handler.cleanup();
    }

    public void stop() throws Exception {
        cleanup();
        client.shutdown();
        server.stop();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Har getHar() {
        return client.getHar();
    }

    public Har newHar(String initialPageRef) {
        pageCount = 1;

        Har oldHar = getHar();

        Har har = new Har(new HarLog(CREATOR));
        client.setHar(har);
        newPage(initialPageRef);

        return oldHar;
    }

    public void newPage(String pageRef) {
        if (pageRef == null) {
            pageRef = "Page " + pageCount;
        }

        client.setHarPageRef(pageRef);
        currentPage = new HarPage(pageRef);
        client.getHar().getLog().addPage(currentPage);

        pageCount++;
    }

    public void endPage() {
        if (currentPage == null) {
            return;
        }

        currentPage.getPageTimings().setOnLoad(new Date().getTime() - currentPage.getStartedDateTime().getTime());
        client.setHarPageRef(null);
        currentPage = null;
    }

    public void setRetryCount(int count) {
        client.setRetryCount(count);
    }

    public void remapHost(String source, String target) {
        client.remapHost(source, target);
    }

    public void addRequestInterceptor(HttpRequestInterceptor i) {
        client.addRequestInterceptor(i);
    }

    public void addResponseInterceptor(HttpResponseInterceptor i) {
        client.addResponseInterceptor(i);
    }

    public void setDownstreamKbps(long downstreamKbps) {
        client.setDownstreamKbps(downstreamKbps);
    }

    public void setUpstreamKbps(long upstreamKbps) {
        client.setUpstreamKbps(upstreamKbps);
    }

    public void setLatency(long latency) {
        client.setLatency(latency);
    }

    public void setRequestTimeout(int requestTimeout) {
        client.setRequestTimeout(requestTimeout);
    }

    public void setSocketOperationTimeout(int readTimeout) {
        client.setSocketOperationTimeout(readTimeout);
    }

    public void setConnectionTimeout(int connectionTimeout) {
        client.setConnectionTimeout(connectionTimeout);
    }

    public void autoBasicAuthorization(String domain, String username, String password) {
        client.autoBasicAuthorization(domain, username, password);
    }

    public void rewriteUrl(String match, String replace) {
        client.rewriteUrl(match, replace);
    }

    public void blacklistRequests(String pattern, int responseCode) {
        client.blacklistRequests(pattern, responseCode);
    }

    public void whitelistRequests(String[] patterns, int responseCode) {
        client.whitelistRequests(patterns, responseCode);
    }

    public void addHeader(String name, String value) {
        client.addHeader(name, value);
    }

    public void removeHeader(String name) {
        client.removeHeader(name);
    }
    
    public void removeAllHeaders() {
        client.removeAllHeaders();
    }

    public String getHeader(String name) {
        return client.getHeader(name);
    }

    public void setCaptureHeaders(boolean captureHeaders) {
        client.setCaptureHeaders(captureHeaders);
    }

    public void setCaptureContent(boolean captureContent) {
        client.setCaptureContent(captureContent);
    }

    public void clearDNSCache() {
        client.clearDNSCache();
    }

    public void setDNSCacheTimeout(int timeout) {
        client.setDNSCacheTimeout(timeout);
    }

    public void waitForNetworkTrafficToStop(final long quietPeriodInMs, long timeoutInMs) {
        long start = System.currentTimeMillis();
        boolean result = ThreadUtils.waitFor(new ThreadUtils.WaitCondition() {
            @Override
            public boolean checkCondition(long elapsedTimeInMs) {
                Date lastCompleted = null;
                Har har = client.getHar();
                if (har == null || har.getLog() == null) {
                    return true;
                }

                for (HarEntry entry : har.getLog().getEntries()) {
                    // if there is an active request, just stop looking
                    if (entry.getResponse().getStatus() < 0) {
                        return false;
                    }

                    Date end = new Date(entry.getStartedDateTime().getTime() + entry.getTime());
                    if (lastCompleted == null) {
                        lastCompleted = end;
                    } else if (end.after(lastCompleted)) {
                        lastCompleted = end;
                    }
                }
                
                return lastCompleted != null && System.currentTimeMillis() - lastCompleted.getTime() >= quietPeriodInMs;
            }
        }, TimeUnit.MILLISECONDS, timeoutInMs);
        long end = System.currentTimeMillis();
        long time = (end - start);

        if (!result) {
            throw new RuntimeException("Timed out after " + timeoutInMs + " ms while waiting for network traffic to stop");
        }
    }

    public void setOptions(Map<String, String> options) {
        if (options.containsKey("httpProxy")) {
            client.setHttpProxy(options.get("httpProxy"));
        }
    }
}
