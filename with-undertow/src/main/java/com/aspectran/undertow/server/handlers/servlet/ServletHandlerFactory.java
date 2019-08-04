package com.aspectran.undertow.server.handlers.servlet;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentManager;

import java.util.Collection;

/**
 * <p>Created: 2019-08-04</p>
 */
public class ServletHandlerFactory {

    private TowServletContainer servletContainer;

    public TowServletContainer getServletContainer() {
        return servletContainer;
    }

    public void setServletContainer(TowServletContainer servletContainer) {
        this.servletContainer = servletContainer;
    }

    public HttpHandler createServletHandler() throws Exception {
        if (servletContainer != null) {
            PathHandler pathHandler = new PathHandler();
            Collection<String> deploymentNames = servletContainer.listDeployments();
            for (String deploymentName : deploymentNames) {
                DeploymentManager manager = servletContainer.getDeployment(deploymentName);
                manager.deploy();
                HttpHandler handler = manager.start();
                pathHandler.addPrefixPath(manager.getDeployment().getDeploymentInfo().getContextPath(), handler);
            }
            return pathHandler;
        } else {
            return null;
        }
    }

}
