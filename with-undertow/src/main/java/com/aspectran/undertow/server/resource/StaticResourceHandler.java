package com.aspectran.undertow.server.resource;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.wildcard.WildcardPattern;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.handlers.resource.ResourceSupplier;

import java.util.ArrayList;
import java.util.List;

public class StaticResourceHandler extends ResourceHandler {

    private WildcardPattern[] staticResourcePathPatterns;

    public StaticResourceHandler(ResourceManager resourceManager) {
        super(resourceManager);
    }

    public StaticResourceHandler(ResourceManager resourceManager, HttpHandler next) {
        super(resourceManager, next);
    }

    public StaticResourceHandler(ResourceSupplier resourceSupplier) {
        super(resourceSupplier);
    }

    public StaticResourceHandler(ResourceSupplier resourceSupplier, HttpHandler next) {
        super(resourceSupplier, next);
    }

    public void setStaticResourcePaths(String[] staticResourcePaths) {
        List<WildcardPattern> patterns = new ArrayList<>();
        if (staticResourcePaths != null) {
            for (String path : staticResourcePaths) {
                if (StringUtils.hasText(path)) {
                    patterns.add(WildcardPattern.compile(path.trim(), ActivityContext.NAME_SEPARATOR_CHAR));
                }
            }
        }
        if (!patterns.isEmpty()) {
            this.staticResourcePathPatterns = patterns.toArray(new WildcardPattern[0]);
        } else {
            this.staticResourcePathPatterns = null;
        }
    }

    public boolean hasPatterns() {
        return (staticResourcePathPatterns != null);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String requestUri = exchange.getRequestURI();
        if (staticResourcePathPatterns != null) {
            for (WildcardPattern pattern : staticResourcePathPatterns) {
                if (pattern.matches(requestUri)) {
                    super.handleRequest(exchange);
                    break;
                }
            }
        }
    }

}
