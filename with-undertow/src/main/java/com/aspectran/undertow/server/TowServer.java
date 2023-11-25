package com.aspectran.undertow.server;

import com.aspectran.core.component.session.SessionHandler;
import com.aspectran.core.util.lifecycle.LifeCycle;
import io.undertow.servlet.api.DeploymentManager;

/**
 * <p>Created: 11/25/23</p>
 */
public interface TowServer extends LifeCycle {

    boolean isAutoStart();

    DeploymentManager getDeploymentManager(String deploymentName);

    DeploymentManager getDeploymentManagerByPath(String path);

    SessionHandler getSessionHandler(String deploymentName);

    SessionHandler getSessionHandlerByPath(String path);

}
