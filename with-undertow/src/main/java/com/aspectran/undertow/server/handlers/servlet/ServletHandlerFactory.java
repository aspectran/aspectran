package com.aspectran.undertow.server.handlers.servlet;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentManager;

import java.util.Collection;

/**
 * <p>Created: 2019-08-04</p>
 */
public class ServletHandlerFactory {

    private TowServletContainer towServletContainer;

    public TowServletContainer getTowServletContainer() {
        return towServletContainer;
    }

    public void setTowServletContainer(TowServletContainer towServletContainer) {
        this.towServletContainer = towServletContainer;
    }

    public HttpHandler createServletHandler() throws Exception {
        if (towServletContainer != null) {
            //PathHandler pathHandler = new PathHandler(Handlers.redirect("/"));
            PathHandler pathHandler = new PathHandler();
            Collection<String> deploymentNames = towServletContainer.listDeployments();
            for (String deploymentName : deploymentNames) {
                DeploymentManager manager = towServletContainer.getDeployment(deploymentName);
                manager.deploy();
                HttpHandler handler = manager.start();
                String contextPath = manager.getDeployment().getDeploymentInfo().getContextPath();
                pathHandler.addPrefixPath(contextPath, handler);
            }
            return pathHandler;
        } else {
            return null;
        }
    }

}
