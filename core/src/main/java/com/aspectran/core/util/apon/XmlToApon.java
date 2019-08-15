/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.core.util.apon;

import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.StringUtils;
import org.xml.sax.Attributes;
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

    public static Parameters from(String xml) throws IOException {
        return from(xml, new VariableParameters());
    }

    public static <T extends Parameters> T from(String xml, Class<T> requiredType) throws IOException {
        T container = ClassUtils.createInstance(requiredType);
        from(xml, container);
        return container;
    }

    public static <T extends Parameters> T from(String xml, T container) throws IOException {
        if (xml == null) {
            throw new IllegalArgumentException("xml must not be null");
        }
        return from(new StringReader(xml), container);
    }

    public static Parameters from(Reader in) throws IOException {
        return from(in, new VariableParameters());
    }

    public static <T extends Parameters> T from(Reader in, Class<T> requiredType) throws IOException {
        T container = ClassUtils.createInstance(requiredType);
        from(in, container);
        return container;
    }

    public static <T extends Parameters> T from(Reader in, T container) throws IOException {
        return from(new InputSource(in), container);
    }

    public static Parameters from(InputStream in) throws IOException {
        return from(in, new VariableParameters());
    }

    public static <T extends Parameters> T from(InputStream in, Class<T> requiredType) throws IOException {
        T container = ClassUtils.createInstance(requiredType);
        from(in, container);
        return container;
    }

    public static <T extends Parameters> T from(InputStream in, T container) throws IOException {
        return from(new InputSource(in), container);
    }

    public static Parameters from(File file) throws IOException {
        return from(new InputSource(file.toURI().toASCIIString()), new VariableParameters());
    }

    public static <T extends Parameters> T from(File file, Class<T> requiredType) throws IOException {
        T container = ClassUtils.createInstance(requiredType);
        from(file, container);
        return container;
    }

    public static <T extends Parameters> T from(File file, T container) throws IOException {
        return from(new InputSource(file.toURI().toASCIIString()), container);
    }

    public static <T extends Parameters> T from(InputSource is, T container) throws IOException {
        if (is == null) {
            throw new IllegalArgumentException("InputSource must not be null");
        }
        if (container == null) {
            throw new IllegalArgumentException("container must not be null");
        }

        ParameterValueHandler valueHandler = null;
        try {
            valueHandler = new ParameterValueHandler(container);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(is, valueHandler);
        } catch (Exception e) {
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
            throw new IOException("Failed to convert XML to APON" + location + "; " + e.getMessage(), e);
        }

        return container;
    }

    private static class ParameterValueHandler extends DefaultHandler {

        private final StringBuilder buffer = new StringBuilder();

        private Parameters parameters;

        private String name;

        private boolean open;

        private boolean leaf;

        private Locator locator;

        public ParameterValueHandler(Parameters container) {
            this.parameters = container;
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            // Save the locator, so that it can be used later for line tracking when traversing nodes.
            this.locator = locator;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
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
            if (buffer.length() > 0) {
                buffer.delete(0, buffer.length());
            }
        }

        @Override
        public void endElement (String uri, String localName, String qName) throws SAXException {
            if (open) {
                String text = null;
                if (buffer.length() > 0) {
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
