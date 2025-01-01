/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import com.aspectran.utils.annotation.jsr305.Nullable;
import io.undertow.predicate.Predicate;
import io.undertow.predicate.Predicates;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.DeflateEncodingProvider;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Configure HTTP compression in Undertow {@link HttpHandler} and
 * create an {@link EncodingHandler}.
 *
 * <p>Created: 2019-08-18</p>
 */
public class EncodingHandlerWrapper implements HandlerWrapper {

    private static final String GZIP = "gzip";

    private static final String DEFLATE = "deflate";

    private String[] encodingProviderNames;

    private ContentEncodingPredicates[] encodingPredicates;

    public void setEncodingProviders(String... encodingProviderNames) {
        this.encodingProviderNames = encodingProviderNames;
    }

    public void setEncodingPredicates(ContentEncodingPredicates... encodingPredicates) {
        this.encodingPredicates = encodingPredicates;
    }

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
