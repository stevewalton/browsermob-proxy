package org.browsermob.proxy;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.sitebricks.SitebricksModule;
import java.io.InputStream;
import java.util.Properties;
import javax.servlet.ServletContextEvent;
import org.browsermob.proxy.bricks.ProxyFeatures;
import org.browsermob.proxy.bricks.ProxyResource;
import org.browsermob.proxy.guice.ConfigModule;
import org.browsermob.proxy.guice.JettyModule;
import org.browsermob.proxy.util.Log;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class Main {
    private static final Log LOG = new Log();

    public static void main(String[] args) throws Exception {
        String version = "UNKNOWN/DEVELOPMENT";
        InputStream is = Main.class.getResourceAsStream("/META-INF/maven/biz.neustar/browsermob-proxy/pom.properties");
        if (is != null) {
            Properties props = new Properties();
            props.load(is);
            version = props.getProperty("version");
        }
        LOG.info("Starting BrowserMob Proxy version %s", version);


        final Injector injector = Guice.createInjector(new ConfigModule(args), new JettyModule(), new SitebricksModule() {
            @Override
            protected void configureSitebricks() {
                scan(ProxyResource.class.getPackage());
                scan(ProxyFeatures.class.getPackage());
            }
        });

        Server server = injector.getInstance(Server.class);
        GuiceServletContextListener gscl = new GuiceServletContextListener() {
            @Override
            protected Injector getInjector() {
                return injector;
            }
        };
        server.start();

        ServletContextHandler context = (ServletContextHandler) server.getHandler();
        gscl.contextInitialized(new ServletContextEvent(context.getServletContext()));

        server.join();
    }
}
