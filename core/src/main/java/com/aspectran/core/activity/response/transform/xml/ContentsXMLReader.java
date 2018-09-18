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
package com.aspectran.core.activity.response.transform.xml;

import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.util.BeanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.Parameter;
import com.aspectran.core.util.apon.ParameterValue;
import com.aspectran.core.util.apon.Parameters;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Converts a ProcessResult object to a XML string.
 * 
 * <p>Created: 2008. 05. 26 PM 2:03:15</p>
 */
public class ContentsXMLReader implements XMLReader {

    private static final String CONTENTS_TAG = "contents";

    private static final String CONTENT_TAG = "content";

    private static final String ROWS_TAG = "rows";

    private static final String ROW_TAG = "row";

    private static final String EMPTY_TAG = "empty";

    private static final Attributes NULL_ATTRS = new AttributesImpl();

    protected ContentHandler handler;

    /**
     * Instantiates a new ContentsXMLReader.
     */
    public ContentsXMLReader() {
    }

    @Override
    public void setContentHandler(ContentHandler handler) {
        this.handler = handler;
    }

    @Override
    public ContentHandler getContentHandler() {
        return handler;
    }

    @Override
    public void setErrorHandler(ErrorHandler errorhandler) {
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return null;
    }

    @Override
    public void parse(String s) throws IOException, SAXException {
    }

    @Override
    public DTDHandler getDTDHandler() {
        return null;
    }

    @Override
    public EntityResolver getEntityResolver() {
        return null;
    }

    @Override
    public void setEntityResolver(EntityResolver entityresolver) {
    }

    @Override
    public void setDTDHandler(DTDHandler dtdhandler) {
    }

    @Override
    public Object getProperty(String name) {
        return null;
    }

    @Override
    public void setProperty(String s, Object obj) {
    }

    @Override
    public void setFeature(String s, boolean flag) {
    }

    @Override
    public boolean getFeature(String name) {
        return false;
    }

    /**
     * Outputs a string.
     *
     * @param s the input string
     * @throws SAXException the SAX exception
     */
    protected void outputString(String s) throws SAXException {
        handler.characters(s.toCharArray(), 0, s.length());
    }

    /**
     * Output a ignorable whitespace string.
     *
     * @param s the whitespace string
     * @throws SAXException the SAX exception
     */
    protected void outputIgnorableWhitespace(String s) throws SAXException {
        handler.characters(s.toCharArray(), 0, s.length());
    }

    @Override
    public void parse(InputSource is) throws IOException, SAXException {
        if (handler == null) {
            throw new SAXException("No XML ContentHandler");
        }
        try {
            ContentsInputSource cis = (ContentsInputSource)is;
            ProcessResult processResult = cis.getProcessResult();
            handler.startDocument();
            if (processResult != null && !processResult.isEmpty()) {
                String contentsName = processResult.getName();
                if (!processResult.isOmittable()) {
                    if (contentsName != null) {
                        handler.startElement(StringUtils.EMPTY, contentsName, contentsName, NULL_ATTRS);
                    } else {
                        handler.startElement(StringUtils.EMPTY, CONTENTS_TAG, CONTENTS_TAG, NULL_ATTRS);
                    }
                }
                parse(processResult);
                if (!processResult.isOmittable()) {
                    if (contentsName != null) {
                        handler.endElement(StringUtils.EMPTY, contentsName, contentsName);
                    } else {
                        handler.endElement(StringUtils.EMPTY, CONTENTS_TAG, CONTENTS_TAG);
                    }
                }
            } else {
                handler.startElement(StringUtils.EMPTY, EMPTY_TAG, EMPTY_TAG, NULL_ATTRS);
                handler.endElement(StringUtils.EMPTY, EMPTY_TAG, EMPTY_TAG);
            }
            handler.endDocument();
        } catch (InvocationTargetException e) {
            throw new SAXException("Cannot parse process-result. Cause: " + e.toString());
        } catch (IOException | SAXException e) {
            throw e;
        }
    }

    /**
     * Parses a {@code ProcessResult} object.
     *
     * @param processResult a {@code ProcessResult} object
     * @throws IOException if an I/O error has occurred
     * @throws SAXException the SAX exception
     * @throws InvocationTargetException the invocation target exception
     */
    private void parse(ProcessResult processResult) throws IOException, SAXException, InvocationTargetException {
        for (ContentResult contentResult : processResult) {
            String contentName = contentResult.getName();
            if (!contentResult.isOmittable()) {
                if (contentName != null) {
                    handler.startElement(StringUtils.EMPTY, contentName, contentName, NULL_ATTRS);
                } else {
                    handler.startElement(StringUtils.EMPTY, CONTENT_TAG, CONTENT_TAG, NULL_ATTRS);
                }
            }
            for (ActionResult actionResult : contentResult) {
                String actionId = actionResult.getActionId();
                Object resultValue = actionResult.getResultValue();
                if (resultValue instanceof ProcessResult) {
                    parse((ProcessResult)resultValue);
                } else {
                    if (actionId != null) {
                        handler.startElement(StringUtils.EMPTY, actionId, actionId, NULL_ATTRS);
                    }
                    parse(resultValue);
                    if (actionId != null) {
                        handler.endElement(StringUtils.EMPTY, actionId, actionId);
                    }
                }
            }
            if (!contentResult.isOmittable()) {
                if (contentResult.getName() != null) {
                    handler.endElement(StringUtils.EMPTY, contentName, contentName);
                } else {
                    handler.endElement(StringUtils.EMPTY, CONTENT_TAG, CONTENT_TAG);
                }
            }
        }
    }

    /**
     * Parses an object.
     *
     * @param object the object
     * @throws IOException if an I/O error has occurred
     * @throws SAXException the SAX exception
     * @throws InvocationTargetException the invocation target exception
     */
    @SuppressWarnings("unchecked")
    private void parse(Object object) throws IOException, SAXException, InvocationTargetException {
        if (object == null) {
            return;
        }
        if (object instanceof ProcessResult) {
            parse((ProcessResult)object);
        } else if (object instanceof String
                || object instanceof Number
                || object instanceof Boolean
                || object instanceof Date) {
            outputString(object.toString());
        } else if (object instanceof Parameters) {
            Map<String, ParameterValue> params = ((Parameters)object).getParameterValueMap();
            for (Parameter p: params.values()) {
                String name = p.getName();
                Object value = p.getValue();
                checkCircularReference(object, value);
                handler.startElement(StringUtils.EMPTY, name, name, NULL_ATTRS);
                parse(value);
                handler.endElement(StringUtils.EMPTY, name, name);
            }
        } else if (object instanceof Map<?, ?>) {
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>)object).entrySet()) {
                String name = entry.getKey().toString();
                Object value = entry.getValue();
                checkCircularReference(object, value);
                handler.startElement(StringUtils.EMPTY, name, name, NULL_ATTRS);
                parse(value);
                handler.endElement(StringUtils.EMPTY, name, name);
            }
        } else if (object instanceof Collection<?>) {
            handler.startElement(StringUtils.EMPTY, ROWS_TAG, ROWS_TAG, NULL_ATTRS);
            for (Object value : ((Collection<Object>) object)) {
                checkCircularReference(object, value);
                handler.startElement(StringUtils.EMPTY, ROW_TAG, ROW_TAG, NULL_ATTRS);
                parse(value);
                handler.endElement(StringUtils.EMPTY, ROW_TAG, ROW_TAG);
            }
            handler.endElement(StringUtils.EMPTY, ROWS_TAG, ROWS_TAG);
        } else if (object.getClass().isArray()) {
            handler.startElement(StringUtils.EMPTY, ROWS_TAG, ROWS_TAG, NULL_ATTRS);
            int len = Array.getLength(object);
            for (int i = 0; i < len; i++) {
                Object value = Array.get(object, i);
                checkCircularReference(object, value);
                handler.startElement(StringUtils.EMPTY, ROW_TAG, ROW_TAG, NULL_ATTRS);
                parse(value);
                handler.endElement(StringUtils.EMPTY, ROW_TAG, ROW_TAG);
            }
            handler.endElement(StringUtils.EMPTY, ROWS_TAG, ROWS_TAG);
        } else {
            String[] readablePropertyNames = BeanUtils.getReadablePropertyNamesWithoutNonSerializable(object);
            if (readablePropertyNames != null && readablePropertyNames.length > 0) {
                for (String name : readablePropertyNames) {
                    Object value = BeanUtils.getProperty(object, name);
                    checkCircularReference(object, value);
                    handler.startElement(StringUtils.EMPTY, name, name, NULL_ATTRS);
                    parse(value);
                    handler.endElement(StringUtils.EMPTY, name, name);
                }
            }
        }
    }

    private void checkCircularReference(Object wrapper, Object member) {
        if (wrapper.equals(member)) {
            throw new IllegalArgumentException("XML Serialization Failure: A circular reference was detected" +
                    " while converting a member object [" + member + "] in [" + wrapper + "]");
        }
    }

}
