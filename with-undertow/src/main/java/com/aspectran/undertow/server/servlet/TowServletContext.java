package com.aspectran.undertow.server.servlet;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import io.undertow.server.session.SessionManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;

import javax.servlet.ServletContainerInitializer;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * <p>Created: 2019-08-05</p>
 */
public class TowServletContext extends DeploymentInfo implements ApplicationAdapterAware {

    public static final String INHERIT_ROOT_WEB_SERVICE_ATTRIBUTE = TowServletContext.class.getName() + ".INHERIT_ROOT_WEB_SERVICE";

    private static final Set<Class<?>> NO_CLASSES = Collections.emptySet();

    private ApplicationAdapter applicationAdapter;

    private SessionManager sessionManager;

    @Override
    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
        setClassLoader(applicationAdapter.getClassLoader());
    }

    public void setScratchDir(String scratchDir) throws IOException {
        File dir = applicationAdapter.toRealPathAsFile(scratchDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        setTempDir(applicationAdapter.toRealPathAsFile(scratchDir));
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        setSessionManagerFactory(deployment -> sessionManager);
    }

    public void setServlets(TowServlet[] towServlets) {
        if (towServlets != null) {
            for (TowServlet towServlet : towServlets) {
                addServlet(towServlet);
            }
        }
    }

    public void setFilters(TowFilter[] towFilters) {
        if (towFilters != null) {
            for (TowFilter towFilter : towFilters) {
                addFilter(towFilter);
            }
        }
    }

    public void setFilterUrlMappings(TowFilterUrlMapping[] towFilterUrlMappings) {
        if (towFilterUrlMappings != null) {
            for (TowFilterUrlMapping filterUrlMapping : towFilterUrlMappings) {
                addFilterUrlMapping(filterUrlMapping.getFilterName(), filterUrlMapping.getMapping(),
                        filterUrlMapping.getDispatcher());
            }
        }
    }

    public void setFilterServletMappings(TowFilterServletMapping[] towFilterServletMappings) {
        if (towFilterServletMappings != null) {
            for (TowFilterServletMapping filterServletMapping : towFilterServletMappings) {
                addFilterServletNameMapping(filterServletMapping.getFilterName(), filterServletMapping.getMapping(),
                        filterServletMapping.getDispatcher());
            }
        }
    }

    public void setInheritRootWebService(boolean inheritRootWebService) {
        if (inheritRootWebService) {
            getServletContextAttributes().put(INHERIT_ROOT_WEB_SERVICE_ATTRIBUTE, "true");
        } else {
            getServletContextAttributes().remove(INHERIT_ROOT_WEB_SERVICE_ATTRIBUTE);
        }
    }

    public void setServletContainerInitializers(ServletContainerInitializer[] servletContainerInitializers) {
        for (ServletContainerInitializer initializer : servletContainerInitializers) {
            Class<? extends ServletContainerInitializer> servletContainerInitializerClass = initializer.getClass();
            InstanceFactory<? extends ServletContainerInitializer> instanceFactory = new ImmediateInstanceFactory<>(initializer);
            ServletContainerInitializerInfo sciInfo = new ServletContainerInitializerInfo(servletContainerInitializerClass, instanceFactory, NO_CLASSES);
            addServletContainerInitializer(sciInfo);
        }
    }

}
