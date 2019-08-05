package com.aspectran.undertow.server.handlers.servlet;

import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.service.CoreService;
import com.aspectran.web.service.AspectranWebService;
import com.aspectran.web.service.WebService;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentManager;

import javax.servlet.ServletContext;
import java.util.Collection;

/**
 * <p>Created: 2019-08-04</p>
 */
public class ServletHandlerFactory implements ActivityContextAware {

    private ActivityContext context;

    private TowServletContainer towServletContainer;

    @Override
    public void setActivityContext(ActivityContext context) {
        this.context = context;
    }

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

                ServletContext servletContext = manager.getDeployment().getServletContext();
                Object attr = servletContext.getAttribute(WebService.ROOT_WEB_SERVICE_ATTRIBUTE);
                if ("enabled".equals(attr)) {
                    CoreService rootService = context.getRootService();
                    WebService webService = AspectranWebService.create(servletContext, rootService);
                    servletContext.setAttribute(WebService.ROOT_WEB_SERVICE_ATTRIBUTE, webService);
                }
            }
            return pathHandler;
        } else {
            return null;
        }
    }

}
