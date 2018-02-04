/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.context.rule.parser.xml;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Offline entity resolver for the Aspectran DTDs.
 * 
 * <p>Created: 2008. 06. 14 AM 4:48:34</p>
 */
public class AspectranDtdResolver implements EntityResolver {

    private static final String ASPECTRAN_DTD = "/com/aspectran/core/context/rule/parser/xml/dtd/aspectran-5.dtd";

    private static final Map<String, String> doctypeMap = new HashMap<>();

    private final boolean validating;

    static {
        doctypeMap.put("-//ASPECTRAN//DTD Aspectran Configuration 5.0//EN".toUpperCase(), ASPECTRAN_DTD);
        doctypeMap.put("aspectran-5.dtd".toUpperCase(), ASPECTRAN_DTD);
    }

    public AspectranDtdResolver() {
        this(true);
    }

    public AspectranDtdResolver(boolean validating) {
        this.validating = validating;
    }

    /**
     * Converts a public DTD into a local one.
     *
     * @param publicId unused but required by EntityResolver interface
     * @param systemId the DTD that is being requested
     * @return the InputSource for the DTD
     * @throws SAXException if anything goes wrong
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
        if (validating) {
            try {
                InputSource source = null;

                if (publicId != null) {
                    String path = doctypeMap.get(publicId.toUpperCase());
                    source = getInputSource(path);
                }

                if (source == null && systemId != null) {
                    String path = doctypeMap.get(systemId.toUpperCase());
                    source = getInputSource(path);
                }

                return source;
            } catch (Exception e) {
                throw new SAXException(e.toString());
            }
        } else {
            return new InputSource(new StringReader(""));
        }
    }

    /**
     * Gets the input source.
     *
     * @param path the path
     * @return the input source
     * @throws IOException if an I/O error has occurred
     */
    private InputSource getInputSource(String path) throws IOException {
        InputSource source = null;

        if (path != null) {
            InputStream in = getClass().getResourceAsStream(path);
            source = new InputSource(in);
        }

        return source;
    }

}
