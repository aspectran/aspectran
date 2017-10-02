package com.aspectran.with.jetty;

import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.service.AspectranService;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.web.service.WebAspectranService;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created: 2017. 1. 27.</p>
 */
public class JettyWebAppContext extends WebAppContext implements ActivityContextAware, InitializableBean {

    private static final Log log = LogFactory.getLog(JettyWebAppContext.class);

    private ActivityContext context;

    private boolean standalone;

    @Override
    public void setActivityContext(ActivityContext context) {
        this.context = context;
    }

    public void setTempDirectory(String tempDirectory) {
        File tempDir = null;

        try {
            tempDir = new File(tempDirectory);
            if (!tempDir.exists()) {
                if (!tempDir.mkdirs()) {
                    throw new IOException("Unable to create scratch directory: " + tempDir);
                }
            }
            super.setTempDirectory(tempDir);
        } catch (Exception e) {
            log.error("Failed to establish Scratch directory: " + tempDir, e);
        }
    }

    public void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }

    @Override
    public void initialize() throws Exception {
        if (context == null) {
            throw new IllegalStateException();
        }

        /*
         * Configure the application to support the compilation of JSP files.
         * We need a new class loader and some stuff so that Jetty can call the
         * onStartup() methods as required.
         */
        setAttribute("org.eclipse.jetty.containerInitializers", jspInitializers());
        setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
        addBean(new ServletContainerInitializersStarter(this), true);
        //setClassLoader(new URLClassLoader(new URL[0], context.getClassLoader()));

        if (!standalone) {
            AspectranService rootAspectranService = context.getRootAspectranService();
            WebAspectranService webAspectranService = WebAspectranService.create(getServletContext(), rootAspectranService);
            webAspectranService.start();

            setAttribute(WebAspectranService.ROOT_WEB_ASPECTRAN_SERVICE_ATTRIBUTE, webAspectranService);
        }
    }

    private static List<ContainerInitializer> jspInitializers() {
        JettyJasperInitializer sci = new JettyJasperInitializer();
        ContainerInitializer initializer = new ContainerInitializer(sci, null);
        List<ContainerInitializer> initializers = new ArrayList<>();
        initializers.add(initializer);
        return initializers;
    }

}
