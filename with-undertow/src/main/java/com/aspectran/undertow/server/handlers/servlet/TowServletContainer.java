package com.aspectran.undertow.server.handlers.servlet;

import io.undertow.servlet.core.ServletContainerImpl;

/**
 * <p>Created: 2019-08-05</p>
 */
public class TowServletContainer extends ServletContainerImpl {

    public void setTowServletContexts(TowServletContext... towServletContexts) {
        if (towServletContexts != null) {
            for (TowServletContext towServletContext : towServletContexts) {
                addDeployment(towServletContext);
            }
        }
    }

}
