/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.utils.apon;

import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

/**
 * Converts XML to APON.
 *
 * @since 6.2.0
 */
public class XmlToApon {

    @NonNull
    public static Parameters from(String xml) throws IOException {
        return from(xml, new VariableParameters());
    }

    @NonNull
    public static <T extends Parameters> T from(String xml, Class<T> requiredType) throws IOException {
        T container = ClassUtils.createInstance(requiredType);
        from(xml, container);
        return container;
    }

    @NonNull
    public static <T extends Parameters> T from(String xml, T container) throws IOException {
        if (xml == null) {
            throw new IllegalArgumentException("xml must not be null");
        }
        return from(new StringReader(xml), container);
    }

    @NonNull
    public static Parameters from(Reader in) throws IOException {
        return from(in, new VariableParameters());
    }

    @NonNull
    public static <T extends Parameters> T from(Reader in, Class<T> requiredType) throws IOException {
        return from(in, requiredType, null);
    }

    @NonNull
    public static <T extends Parameters> T from(Reader in, Class<T> requiredType, EntityResolver entityResolver)
            throws IOException {
        T container = ClassUtils.createInstance(requiredType);
        from(in, container, entityResolver);
        return container;
    }

    @NonNull
    public static <T extends Parameters> T from(Reader in, T container) throws IOException {
        return from(in, container, null);
    }

    @NonNull
    public static <T extends Parameters> T from(Reader in, T container, EntityResolver entityResolver)
            throws IOException {
        return from(new InputSource(in), container, entityResolver);
    }

    @NonNull
    public static Parameters from(InputStream in) throws IOException {
        return from(in, new VariableParameters());
    }

    @NonNull
    public static <T extends Parameters> T from(InputStream in, Class<T> requiredType) throws IOException {
        return from(in, requiredType, null);
    }

    @NonNull
    public static <T extends Parameters> T from(InputStream in, Class<T> requiredType, EntityResolver entityResolver)
            throws IOException {
        T container = ClassUtils.createInstance(requiredType);
        from(in, container, entityResolver);
        return container;
    }

    @NonNull
    public static <T extends Parameters> T from(InputStream in, T container) throws IOException {
        return from(in, container, null);
    }

    @NonNull
    public static <T extends Parameters> T from(InputStream in, T container, EntityResolver entityResolver)
            throws IOException {
        return from(new InputSource(in), container, entityResolver);
    }

    @NonNull
    public static Parameters from(File file) throws IOException {
        return from(file, new VariableParameters());
    }

    @NonNull
    public static <T extends Parameters> T from(File file, Class<T> requiredType) throws IOException {
        return from(file, requiredType, null);
    }

    @NonNull
    public static <T extends Parameters> T from(File file, Class<T> requiredType, EntityResolver entityResolver)
            throws IOException {
        T container = ClassUtils.createInstance(requiredType);
        from(file, container, entityResolver);
        return container;
    }

    @NonNull
    public static <T extends Parameters> T from(File file, T container) throws IOException {
        return from(file, container, null);
    }

    @NonNull
    public static <T extends Parameters> T from(@NonNull File file, T container, EntityResolver entityResolver)
            throws IOException {
        return from(new InputSource(file.toURI().toASCIIString()), container, entityResolver);
    }

    @NonNull
    public static <T extends Parameters> T from(InputSource is, T container) throws IOException {
        return from(is, container, null);
    }

    @NonNull
    public static <T extends Parameters> T from(InputSource is, T container, EntityResolver entityResolver)
            throws IOException {
        if (is == null) {
            throw new IllegalArgumentException("InputSource must not be null");
        }
        if (container == null) {
            throw new IllegalArgumentException("container must not be null");
        }

        ParameterValueHandler valueHandler = null;
        try {
            valueHandler = new ParameterValueHandler(container);
            if (entityResolver != null) {
                valueHandler.setEntityResolver(entityResolver);
            }
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(is, valueHandler);
        } catch (Exception e) {
            String location = getLocation(valueHandler);
            throw new IOException("Failed to convert XML to APON" + location + "; " + e.getMessage(), e);
        }

        return container;
    }

    private static String getLocation(ParameterValueHandler valueHandler) {
        String location;
        if (valueHandler != null && valueHandler.getLocator() != null) {
            Locator locator = valueHandler.getLocator();
            location = "Line Number " + locator.getLineNumber() +
                ", Column " + locator.getColumnNumber();
            if (locator.getSystemId() != null) {
                location = "; " + locator.getSystemId() + " " + location;
            } else {
                location = "; " + location;
            }
        } else {
            location = StringUtils.EMPTY;
        }
        return location;
    }

    private static class ParameterValueHandler extends DefaultHandler {

        private final StringBuilder buffer = new StringBuilder();

        private Parameters parameters;

        private EntityResolver entityResolver;

        private String name;

        private boolean open;

        private boolean leaf;

        private Locator locator;

        public ParameterValueHandler(Parameters container) {
            this.parameters = container;
        }

        public void setEntityResolver(EntityResolver entityResolver) {
            this.entityResolver = entityResolver;
        }

        @Override
        @Nullable
        public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
            if (entityResolver != null) {
                return entityResolver.resolveEntity(publicId, systemId);
            } else {
                return null;
            }
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            // Save the locator, so that it can be used later for line tracking when traversing nodes.
            this.locator = locator;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            if (name != null) {
                parameters = parameters.newParameters(name);
                leaf = false;
            }
            Parameter p = parameters.getParameter(qName);
            if (attributes != null && attributes.getLength() > 0 ||
                    p != null && p.getValueType() == ValueType.PARAMETERS) {
                parameters = parameters.newParameters(qName);
                if (attributes != null) {
                    for (int i = 0; i < attributes.getLength(); i++) {
                        parameters.putValue(attributes.getQName(i), attributes.getValue(i));
                    }
                }
                name = null;
                leaf = false;
            } else {
                name = qName;
                leaf = true;
            }
            open = true;
            if (!buffer.isEmpty()) {
                buffer.delete(0, buffer.length());
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (open) {
                String text = null;
                if (!buffer.isEmpty()) {
                    text = buffer.toString();
                    buffer.delete(0, buffer.length());
                }
                if (text != null) {
                    parameters.putValue(qName, text);
                }
                name = null;
                open = false;
            }
            if (!leaf) {
                parameters = parameters.getProprietor().getContainer();
            } else {
                leaf = false;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            buffer.append(ch, start, length);
        }

        public Locator getLocator() {
            return locator;
        }

    }

}
