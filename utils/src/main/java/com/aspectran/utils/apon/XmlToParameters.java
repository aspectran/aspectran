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
 * Utility that converts XML into {@link Parameters} using a SAX parser.
 * <p>
 * Accepts input from {@link String}, {@link Reader}, {@link InputStream},
 * {@link File}, or {@link org.xml.sax.InputSource}. Elements become parameters,
 * attributes are captured as nested parameters, and element nesting produces
 * hierarchical parameter groups. An optional {@link org.xml.sax.EntityResolver}
 * may be supplied.
 * </p>
 *
 * @since 6.2.0
 */
public class XmlToParameters {

    private final Class<? extends Parameters> requiredType;

    private final EntityResolver entityResolver;

    /**
     * Create a converter that produces a default {@link VariableParameters} container.
     */
    public XmlToParameters() {
        this.requiredType = null;
        this.entityResolver = null;
    }

    /**
     * Create a converter that will instantiate the given {@code requiredType}
     * for the target {@link Parameters} container.
     * @param requiredType the concrete Parameters implementation to instantiate (not null)
     * @throws IllegalArgumentException if {@code requiredType} is null
     */
    public XmlToParameters(Class<? extends Parameters> requiredType) {
        Assert.notNull(requiredType, "requiredType must not be null");
        this.requiredType = requiredType;
        this.entityResolver = null;
    }

    /**
     * Create a converter that will instantiate the given {@code requiredType}
     * and use the provided {@link EntityResolver} during SAX parsing.
     * @param requiredType the container type to create for results (not null)
     * @param entityResolver SAX entity resolver to apply (not null)
     * @throws IllegalArgumentException if any argument is null
     */
    public XmlToParameters(Class<? extends Parameters> requiredType, EntityResolver entityResolver) {
        Assert.notNull(requiredType, "requiredType must not be null");
        Assert.notNull(entityResolver, "entityResolver must not be null");
        this.requiredType = requiredType;
        this.entityResolver = entityResolver;
    }

    /**
     * Parse XML text into a newly created {@link Parameters} container.
     * @param <T> the container type
     * @param xml the XML text (not null)
     * @return the populated container
     * @throws IOException if reading or conversion fails
     */
    @NonNull
    public <T extends Parameters> T read(String xml) throws IOException {
        T container = createContainer();
        return read(xml, container);
    }

    /**
     * Parse XML text into the supplied container.
     * @param <T> the container type
     * @param xml the XML text (not null)
     * @param container the target container to populate (not null)
     * @return the same {@code container} instance
     * @throws IOException if reading or conversion fails
     */
    @NonNull
    public <T extends Parameters> T read(String xml, T container) throws IOException {
        Assert.notNull(xml, "xml must not be null");
        read(new StringReader(xml), container);
        return container;
    }

    /**
     * Parse XML from a {@link Reader} into a newly created container.
     * @param <T> the container type
     * @param reader character stream with XML content (not null)
     * @return the populated container
     * @throws IOException if reading or conversion fails
     */
    @NonNull
    public <T extends Parameters> T read(Reader reader) throws IOException {
        T container = createContainer();
        return read(reader, container);
    }

    /**
     * Parse XML from a {@link Reader} into the supplied container.
     * @param <T> the container type
     * @param reader the XML reader (not null)
     * @param container the target container (not null)
     * @return the same {@code container}
     * @throws IOException if reading or conversion fails
     */
    @NonNull
    public <T extends Parameters> T read(Reader reader, T container) throws IOException {
        Assert.notNull(reader, "reader must not be null");
        return read(new InputSource(reader), container);
    }

    /**
     * Parse XML from an {@link InputStream} into a newly created container.
     * @param <T> the container type
     * @param inputStream the XML input stream (not null)
     * @return the populated container
     * @throws IOException if reading or conversion fails
     */
    @NonNull
    public <T extends Parameters> T read(InputStream inputStream) throws IOException {
        T container = createContainer();
        return read(inputStream, container);
    }

    /**
     * Parse XML from an {@link InputStream} into the supplied container.
     * @param <T> the container type
     * @param inputStream the XML input stream (not null)
     * @param container the target container (not null)
     * @return the same {@code container}
     * @throws IOException if reading or conversion fails
     */
    @NonNull
    public <T extends Parameters> T read(InputStream inputStream, T container) throws IOException {
        Assert.notNull(inputStream, "inputStream must not be null");
        return read(new InputSource(inputStream), container);
    }

    /**
     * Parse XML from a {@link File} into a newly created container.
     * @param <T> the container type
     * @param file the XML file (not null)
     * @return the populated container
     * @throws IOException if reading or conversion fails
     */
    @NonNull
    public <T extends Parameters> T read(File file) throws IOException {
        T container = createContainer();
        return read(file, container);
    }

    /**
     * Parse XML from a {@link File} into the supplied container.
     * @param <T> the container type
     * @param file the XML file (not null)
     * @param container the target container (not null)
     * @return the same {@code container}
     * @throws IOException if reading or conversion fails
     */
    @NonNull
    public <T extends Parameters> T read(File file, T container) throws IOException {
        Assert.notNull(file, "file must not be null");
        InputSource inputSource = new InputSource(file.toURI().toASCIIString());
        return read(inputSource, container);
    }

    /**
     * Parse XML from an {@link InputSource} into a newly created container.
     * @param <T> the container type
     * @param inputSource the XML input source (not null)
     * @return the populated container
     * @throws IOException if reading or conversion fails
     */
    @NonNull
    public <T extends Parameters> T read(InputSource inputSource) throws IOException {
        T container = createContainer();
        return read(inputSource, container);
    }

    /**
     * Parse XML from an {@link InputSource} into the supplied container.
     * @param <T> the container type
     * @param inputSource the XML input source (not null)
     * @param container the target container (not null)
     * @return the same {@code container}
     * @throws IOException if reading or conversion fails
     */
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

    /**
     * SAX {@link DefaultHandler} that walks the XML and populates a {@link Parameters}
     * container by treating elements as parameter names, attributes as nested values,
     * and element text as scalar contents. Maintains a simple stack via owner/proprietor
     * pointers to build a hierarchical parameter structure.
     */
    private static class ParameterValueHandler extends DefaultHandler {

        private final StringBuilder buffer = new StringBuilder();

        private Parameters parameters;

        private EntityResolver entityResolver;

        private String name;

        private boolean open;

        private boolean leaf;

        private Locator locator;

        /**
         * Create a new handler bound to the given result container.
         * @param container the target {@link Parameters} to populate
         */
        public ParameterValueHandler(Parameters container) {
            this.parameters = container;
        }

        /**
         * Optionally set an {@link EntityResolver} to delegate external entity resolution.
         */
        public void setEntityResolver(EntityResolver entityResolver) {
            this.entityResolver = entityResolver;
        }

        /**
         * Resolve external entities during parsing using the configured resolver if present.
         */
        @Override
        @Nullable
        public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
            if (entityResolver != null) {
                return entityResolver.resolveEntity(publicId, systemId);
            } else {
                return null;
            }
        }

        /**
         * Capture the current {@link Locator} so we can include line/column info in errors.
         */
        @Override
        public void setDocumentLocator(Locator locator) {
            // Save the locator, so that it can be used later for line tracking when traversing nodes.
            this.locator = locator;
        }

        /**
         * Start a new element; if the previous element name was pending, create a
         * nested parameters block. Attributes are copied as nested name/value pairs.
         */
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

        /**
         * Finish the current element; if character content was collected, store it under
         * the element name. When closing a non-leaf node, move back up to the parent container.
         */
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

        /**
         * Accumulate character data for the current element.
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            buffer.append(ch, start, length);
        }

        /**
         * Return the last seen {@link Locator} snapshot, if any.
         */
        public Locator getLocator() {
            return locator;
        }

    }

}
