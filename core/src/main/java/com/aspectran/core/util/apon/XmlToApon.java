package com.aspectran.core.util.apon;

import com.aspectran.core.util.ClassUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
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
        if (in == null) {
            throw new IllegalArgumentException("in must not be null");
        }
        if (container == null) {
            throw new IllegalArgumentException("container must not be null");
        }

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(new InputSource(in), new ParameterValueHandler(container));
        } catch (Exception e) {
            throw new IOException(e);
        }

        return container;
    }

    private static class ParameterValueHandler extends DefaultHandler {

        private final Parameters container;

        public ParameterValueHandler(Parameters container) {
            this.container = container;
        }

        @Override
        public void startDocument() throws SAXException {
            // TODO
        }

        @Override
        public void endDocument() throws SAXException {
            // TODO
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            // TODO
        }

        @Override
        public void endElement (String uri, String localName, String qName) throws SAXException {
            // TODO
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            // TODO
        }

    }

}
