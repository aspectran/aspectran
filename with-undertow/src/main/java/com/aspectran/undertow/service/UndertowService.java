package com.aspectran.undertow.service;

import com.aspectran.core.service.CoreService;
import io.undertow.server.HttpServerExchange;

import java.io.IOException;

/**
 * <p>Created: 2019-07-27</p>
 */
public interface UndertowService extends CoreService {

    /**
     * Executes web activity.
     *
     * @param exchange the HTTP request/response exchange
     * @throws IOException If an error occurs during Activity execution
     */
    void execute(HttpServerExchange exchange) throws IOException;

}
