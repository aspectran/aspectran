/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.undertow.server.handler.encoding;

import io.undertow.predicate.Predicate;
import io.undertow.predicate.Predicates;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.DeflateEncodingProvider;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link HandlerWrapper} that creates and configures an Undertow {@link EncodingHandler}
 * for HTTP content compression.
 * <p>This wrapper allows for easy, bean-style configuration of compression providers
 * (e.g., gzip, deflate) and predicates that control when compression should be applied.</p>
 *
 * <p>Created: 2019-08-18</p>
 */
public class EncodingHandlerWrapper implements HandlerWrapper {

    private static final String GZIP = "gzip";

    private static final String DEFLATE = "deflate";

    private String[] encodingProviderNames;

    private ContentEncodingPredicates[] encodingPredicates;

    /**
     * Sets the names of the content encoding providers to enable (e.g., "gzip", "deflate").
     * The order in this array determines the priority.
     * @param encodingProviderNames an array of provider names
     */
    public void setEncodingProviders(String... encodingProviderNames) {
        this.encodingProviderNames = encodingProviderNames;
    }

    /**
     * Sets the predicates that determine whether a response should be compressed.
     * If multiple predicates are provided, they are combined with a logical OR.
     * @param encodingPredicates an array of predicate configurations
     */
    public void setEncodingPredicates(ContentEncodingPredicates... encodingPredicates) {
        this.encodingPredicates = encodingPredicates;
    }

    /**
     * Wraps the given handler with a new {@link EncodingHandler}.
     * <p>This method configures the encoding repository based on the provided providers
     * and predicates, and then constructs the handler.</p>
     * @param handler the next handler in the chain
     * @return the new {@code EncodingHandler}
     */
    @Override
    public HttpHandler wrap(HttpHandler handler) {
        ContentEncodingRepository contentEncodingRepository = new ContentEncodingRepository();
        if (encodingProviderNames != null && encodingProviderNames.length != 0) {
            Predicate predicate = createPredicate();
            int priority = encodingProviderNames.length;
            for (String providerName : encodingProviderNames) {
                if (GZIP.equalsIgnoreCase(providerName)) {
                    contentEncodingRepository.addEncodingHandler(GZIP,
                            new GzipEncodingProvider(), priority, predicate);
                } else if (DEFLATE.equalsIgnoreCase(providerName)) {
                    contentEncodingRepository.addEncodingHandler(DEFLATE,
                            new DeflateEncodingProvider(), priority, predicate);
                } else {
                    throw new IllegalArgumentException("Unknown content encoding provider '" + providerName + "'");
                }
                priority--;
            }
        }
        return new EncodingHandler(handler, contentEncodingRepository);
    }

    /**
     * Private helper method to combine multiple {@link ContentEncodingPredicates} into a
     * single Undertow {@link Predicate} using a logical OR.
     * @return the combined predicate, or {@code null} if none are configured
     */
    @Nullable
    private Predicate createPredicate() {
        Predicate predicate = null;
        if (encodingPredicates != null && encodingPredicates.length > 0) {
            if (encodingPredicates.length == 1) {
                predicate = encodingPredicates[0].createPredicate();
            } else {
                List<Predicate> predicates = new ArrayList<>(encodingPredicates.length);
                for (ContentEncodingPredicates compressionPredicate : encodingPredicates) {
                    predicates.add(compressionPredicate.createPredicate());
                }
                predicate = Predicates.or(predicates.toArray(new Predicate[0]));
            }
        }
        return predicate;
    }

}
