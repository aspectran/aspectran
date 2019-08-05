package com.aspectran.undertow.server.handlers.servlet;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import com.aspectran.core.util.Assert;
import com.aspectran.core.util.ResourceUtils;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.servlet.api.DeploymentInfo;

import java.io.File;
import java.io.IOException;

/**
 * <p>Created: 2019-08-05</p>
 */
public class TowServletContext extends DeploymentInfo implements ApplicationAdapterAware {

    public static final String INHERIT_ROOT_WEB_SERVICE_ATTRIBUTE = TowServletContext.class.getName() + ".INHERIT_ROOT_WEB_SERVICE";

    private ApplicationAdapter applicationAdapter;

    @Override
    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
        setClassLoader(applicationAdapter.getClassLoader());
    }

    public void setResourceBase(String resourceBase) throws IOException {
        Assert.notNull(applicationAdapter, "applicationAdapter must not be null");
        ResourceManager resourceManager;
        if (resourceBase.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            String basePackage = resourceBase.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
            resourceManager = new ClassPathResourceManager(applicationAdapter.getClassLoader(), basePackage);
        } else {
            File basePath = applicationAdapter.toRealPathAsFile(resourceBase);
            resourceManager = new FileResourceManager(basePath);
        }
        setResourceManager(resourceManager);
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
                addFilterUrlMapping(filterUrlMapping.getFilterName(), filterUrlMapping.getMapping(), filterUrlMapping.getDispatcher());
            }
        }
    }

    public void setFilterServletMappings(TowFilterServletMapping[] towFilterServletMappings) {
        if (towFilterServletMappings != null) {
            for (TowFilterServletMapping filterServletMapping : towFilterServletMappings) {
                addFilterServletNameMapping(filterServletMapping.getFilterName(), filterServletMapping.getMapping(), filterServletMapping.getDispatcher());
            }
        }
    }

    public void setEnableRootWebService(boolean enableRootWebService) {
        if (enableRootWebService) {
            getServletContextAttributes().put(INHERIT_ROOT_WEB_SERVICE_ATTRIBUTE, "enabled");
        } else {
            getServletContextAttributes().remove(INHERIT_ROOT_WEB_SERVICE_ATTRIBUTE);
        }
    }

}
