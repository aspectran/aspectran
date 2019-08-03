package com.aspectran.undertow.service;

import com.aspectran.core.service.CoreService;
import com.aspectran.undertow.server.session.TowSessionManager;
import io.undertow.server.HttpServerExchange;

import java.io.IOException;

/**
 * <p>Created: 2019-07-27</p>
 */
public interface TowService extends CoreService {

    TowSessionManager getTowSessionManager();

    /**
     * Executes web activity.
     *
     * @param exchange the HTTP request/response exchange
     * @return true if the activity was handled; false otherwise
     * @throws IOException If an error occurs during Activity execution
     */
    boolean execute(HttpServerExchange exchange) throws IOException;

}
