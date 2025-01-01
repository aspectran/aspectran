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
package com.aspectran.utils.apon;

import com.aspectran.utils.Assert;
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
public class XmlToParameters {

    private final Class<? extends Parameters> requiredType;

    private final EntityResolver entityResolver;

    public XmlToParameters() {
        this.requiredType = null;
        this.entityResolver = null;
    }

    public XmlToParameters(Class<? extends Parameters> requiredType) {
        Assert.notNull(requiredType, "requiredType must not be null");
        this.requiredType = requiredType;
        this.entityResolver = null;
    }

    public XmlToParameters(Class<? extends Parameters> requiredType, EntityResolver entityResolver) {
        Assert.notNull(requiredType, "requiredType must not be null");
        Assert.notNull(entityResolver, "entityResolver must not be null");
        this.requiredType = requiredType;
        this.entityResolver = entityResolver;
    }

    @NonNull
    public <T extends Parameters> T read(String xml) throws IOException {
        T container = createContainer();
        return read(xml, container);
    }

    @NonNull
    public <T extends Parameters> T read(String xml, T container) throws IOException {
        Assert.notNull(xml, "xml must not be null");
        read(new StringReader(xml), container);
        return container;
    }

    @NonNull
    public <T extends Parameters> T read(Reader reader) throws IOException {
        T container = createContainer();
        return read(reader, container);
    }

    @NonNull
    public <T extends Parameters> T read(Reader reader, T container) throws IOException {
        Assert.notNull(reader, "reader must not be null");
        return read(new InputSource(reader), container);
    }

    @NonNull
    public <T extends Parameters> T read(InputStream inputStream) throws IOException {
        T container = createContainer();
        return read(inputStream, container);
    }

    @NonNull
    public <T extends Parameters> T read(InputStream inputStream, T container) throws IOException {
        Assert.notNull(inputStream, "inputStream must not be null");
        return read(new InputSource(inputStream), container);
    }

    @NonNull
    public <T extends Parameters> T read(File file) throws IOException {
        T container = createContainer();
        return read(file, container);
    }

    @NonNull
    public <T extends Parameters> T read(File file, T container) throws IOException {
        Assert.notNull(file, "file must not be null");
        InputSource inputSource = new InputSource(file.toURI().toASCIIString());
        return read(inputSource, container);
    }

    @NonNull
    public <T extends Parameters> T read(InputSource inputSource) throws IOException {
        T container = createContainer();
        return read(inputSource, container);
    }

    @NonNull
    public <T extends Parameters> T read(InputSource inputSource, T container) throws IOException {
        Assert.notNull(inputSource, "inputSource must not be null");
        Assert.notNull(container, "container must not be null");
        ParameterValueHandler valueHandler = null;
        try {
            valueHandler = new ParameterValueHandler(container);
            if (entityResolver != null) {
                valueHandler.setEntityResolver(entityResolver);
            }
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(inputSource, valueHandler);
        } catch (Exception e) {
            String location = getLocation(valueHandler);
            throw new IOException("Failed to convert XML to APON " + location + "; " + e.getMessage(), e);
        }
        return container;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private <T extends Parameters> T createContainer() {
        Parameters container;
        if (requiredType != null) {
            container = ClassUtils.createInstance(requiredType);
        } else {
            container = new VariableParameters();
        }
        return (T)container;
    }

    private String getLocation(ParameterValueHandler valueHandler) {
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

    @NonNull
    public static Parameters from(String xml) throws IOException {
        return new XmlToParameters().read(xml);
    }

    @NonNull
    public static <T extends Parameters> T from(String xml, Class<? extends Parameters> requiredType) throws IOException {
        return new XmlToParameters(requiredType).read(xml);
    }

    @NonNull
    public static <T extends Parameters> T from(String xml, Class<? extends Parameters> requiredType, EntityResolver entityResolver) throws IOException {
        return new XmlToParameters(requiredType, entityResolver).read(xml);
    }

    @NonNull
    public static Parameters from(Reader reader) throws IOException {
        return new XmlToParameters().read(reader);
    }

    @NonNull
    public static <T extends Parameters> T from(Reader reader, Class<? extends Parameters> requiredType) throws IOException {
        return new XmlToParameters(requiredType).read(reader);
    }

    @NonNull
    public static <T extends Parameters> T from(Reader reader, Class<? extends Parameters> requiredType, EntityResolver entityResolver) throws IOException {
        return new XmlToParameters(requiredType, entityResolver).read(reader);
    }

    @NonNull
    public static Parameters from(InputStream inputStream) throws IOException {
        return new XmlToParameters().read(inputStream);
    }

    @NonNull
    public static <T extends Parameters> T from(InputStream inputStream, Class<? extends Parameters> requiredType) throws IOException {
        return new XmlToParameters(requiredType).read(inputStream);
    }

    @NonNull
    public static <T extends Parameters> T from(InputStream inputStream, Class<? extends Parameters> requiredType, EntityResolver entityResolver) throws IOException {
        return new XmlToParameters(requiredType, entityResolver).read(inputStream);
    }

    @NonNull
    public static Parameters from(File file) throws IOException {
        return new XmlToParameters().read(file);
    }

    @NonNull
    public static <T extends Parameters> T from(File file, Class<? extends Parameters> requiredType) throws IOException {
        return new XmlToParameters(requiredType).read(file);
    }

    @NonNull
    public static <T extends Parameters> T from(File file, Class<? extends Parameters> requiredType, EntityResolver entityResolver) throws IOException {
        return new XmlToParameters(requiredType, entityResolver).read(file);
    }

    @NonNull
    public static Parameters from(InputSource inputSource) throws IOException {
        return new XmlToParameters().read(inputSource);
    }

    @NonNull
    public static <T extends Parameters> T from(InputSource inputSource, Class<? extends Parameters> requiredType) throws IOException {
        return new XmlToParameters(requiredType).read(inputSource);
    }

    @NonNull
    public static <T extends Parameters> T from(InputSource inputSource, Class<? extends Parameters> requiredType, EntityResolver entityResolver) throws IOException {
        return new XmlToParameters(requiredType, entityResolver).read(inputSource);
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
