package com.aspectran.undertow.server.encoding;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.encoding.ContentEncodingProvider;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.DeflateEncodingProvider;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Created: 2019-08-18</p>
 */
public class EncodingHandlerFactory {

    private static final String GZIP = "gzip";

    private static final String DEFLATE = "deflate";

    private HttpHandler handler;

    private ContentEncodingRepository contentEncodingRepository;

    private int priority;

    public EncodingHandlerFactory() {
        contentEncodingRepository = new ContentEncodingRepository();
    }

    public void setHandler(HttpHandler handler) {
        this.handler = handler;
    }

    public void setContentEncodingProviders(String... contentEncodingProviderNames) {
        if (contentEncodingProviderNames != null && contentEncodingProviderNames.length != 0) {
            Set<String> names = new HashSet<>(Arrays.asList(contentEncodingProviderNames));
            for (String name : names) {
                if (GZIP.equalsIgnoreCase(name)) {
                    addGzipEncodingProvider(new GzipEncodingProvider());
                } else if (DEFLATE.equalsIgnoreCase(name)) {
                    addDeflateEncodingProvider(new DeflateEncodingProvider());
                } else {
                    throw new IllegalArgumentException("Unknown content encoding provider '" + name + "'");
                }
            }
        }
    }

    public void addGzipEncodingProvider(ContentEncodingProvider contentEncodingProvider) {
        contentEncodingRepository.addEncodingHandler(GZIP, contentEncodingProvider, priority++);
    }

    public void addDeflateEncodingProvider(ContentEncodingProvider contentEncodingProvider) {
        contentEncodingRepository.addEncodingHandler(DEFLATE, contentEncodingProvider, priority++);
    }

    public void clearEncodingProvider() {
        contentEncodingRepository.removeEncodingHandler(GZIP);
        contentEncodingRepository.removeEncodingHandler(DEFLATE);
    }

    public EncodingHandler createEncodingHandler() {
        if (handler == null) {
            throw new IllegalStateException("The next handler is not specified");
        }
        return new EncodingHandler(handler, contentEncodingRepository);
    }

}
