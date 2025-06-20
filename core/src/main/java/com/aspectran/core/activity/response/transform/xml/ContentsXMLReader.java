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
package com.aspectran.core.activity.response.transform.xml;

import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.utils.BeanUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.StringifyContext;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.apon.Parameter;
import com.aspectran.utils.apon.Parameters;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
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

    private static final Attributes NULL_ATTRS = new AttributesImpl();

    private StringifyContext stringifyContext;

    private ContentHandler handler;

    /**
     * Instantiates a new ContentsXMLReader.
     */
    public ContentsXMLReader() {
    }

    public void setStringifyContext(StringifyContext stringifyContext) {
        this.stringifyContext = stringifyContext;
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

    @Override
    public void parse(InputSource is) throws SAXException {
        if (handler == null) {
            throw new SAXException("No XML ContentHandler");
        }
        ContentsInputSource cis = (ContentsInputSource)is;
        Object data = cis.getData();
        handler.startDocument();
        if (data != null) {
            if (data instanceof ProcessResult processResult) {
                if (!processResult.isEmpty()) {
                    parseProcessResult(processResult);
                }
            } else {
                parseObject(data);
            }
        }
        handler.endDocument();
    }

    private void parseProcessResult(@NonNull ProcessResult processResult) throws SAXException {
        String contentsName = processResult.getName();
        if (processResult.isExplicit()) {
            if (contentsName != null) {
                handler.startElement(StringUtils.EMPTY, contentsName, contentsName, NULL_ATTRS);
            } else {
                handler.startElement(StringUtils.EMPTY, CONTENTS_TAG, CONTENTS_TAG, NULL_ATTRS);
            }
        }
        for (ContentResult contentResult : processResult) {
            String contentName = contentResult.getName();
            if (contentResult.isExplicit()) {
                if (contentName != null) {
                    handler.startElement(StringUtils.EMPTY, contentName, contentName, NULL_ATTRS);
                } else {
                    handler.startElement(StringUtils.EMPTY, CONTENT_TAG, CONTENT_TAG, NULL_ATTRS);
                }
            }
            for (ActionResult actionResult : contentResult) {
                String actionId = actionResult.getActionId();
                Object resultValue = actionResult.getResultValue();
                if (actionId != null) {
                    handler.startElement(StringUtils.EMPTY, actionId, actionId, NULL_ATTRS);
                }
                parseObject(resultValue);
                if (actionId != null) {
                    handler.endElement(StringUtils.EMPTY, actionId, actionId);
                }
            }
            if (contentResult.isExplicit()) {
                if (contentResult.getName() != null) {
                    handler.endElement(StringUtils.EMPTY, contentName, contentName);
                } else {
                    handler.endElement(StringUtils.EMPTY, CONTENT_TAG, CONTENT_TAG);
                }
            }
        }
        if (processResult.isExplicit()) {
            if (contentsName != null) {
                handler.endElement(StringUtils.EMPTY, contentsName, contentsName);
            } else {
                handler.endElement(StringUtils.EMPTY, CONTENTS_TAG, CONTENTS_TAG);
            }
        }
    }

    private void parseObject(Object object) throws SAXException {
        if (object == null) {
            return;
        }
        if (object instanceof ProcessResult processResult) {
            parseProcessResult(processResult);
        } else if (object instanceof String ||
                object instanceof Number ||
                object instanceof Boolean) {
            parseString(object.toString());
        } else if (object instanceof Parameters parameters) {
            for (Parameter p: parameters.getParameterValues()) {
                String name = p.getName();
                Object value = p.getValue();
                checkCircularReference(object, value);
                handler.startElement(StringUtils.EMPTY, name, name, NULL_ATTRS);
                parseObject(value);
                handler.endElement(StringUtils.EMPTY, name, name);
            }
        } else if (object instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String name = entry.getKey().toString();
                Object value = entry.getValue();
                checkCircularReference(object, value);
                handler.startElement(StringUtils.EMPTY, name, name, NULL_ATTRS);
                parseObject(value);
                handler.endElement(StringUtils.EMPTY, name, name);
            }
        } else if (object instanceof Collection<?> collection) {
            handler.startElement(StringUtils.EMPTY, ROWS_TAG, ROWS_TAG, NULL_ATTRS);
            for (Object value : collection) {
                checkCircularReference(object, value);
                handler.startElement(StringUtils.EMPTY, ROW_TAG, ROW_TAG, NULL_ATTRS);
                parseObject(value);
                handler.endElement(StringUtils.EMPTY, ROW_TAG, ROW_TAG);
            }
            handler.endElement(StringUtils.EMPTY, ROWS_TAG, ROWS_TAG);
        } else if (object instanceof Iterator<?> iterator) {
            handler.startElement(StringUtils.EMPTY, ROWS_TAG, ROWS_TAG, NULL_ATTRS);
            while (iterator.hasNext()) {
                Object value = iterator.next();
                checkCircularReference(object, value);
                handler.startElement(StringUtils.EMPTY, ROW_TAG, ROW_TAG, NULL_ATTRS);
                parseObject(value);
                handler.endElement(StringUtils.EMPTY, ROW_TAG, ROW_TAG);
            }
            handler.endElement(StringUtils.EMPTY, ROWS_TAG, ROWS_TAG);
        } else if (object instanceof Enumeration<?> enumeration) {
            handler.startElement(StringUtils.EMPTY, ROWS_TAG, ROWS_TAG, NULL_ATTRS);
            while (enumeration.hasMoreElements()) {
                Object value = enumeration.nextElement();
                checkCircularReference(object, value);
                handler.startElement(StringUtils.EMPTY, ROW_TAG, ROW_TAG, NULL_ATTRS);
                parseObject(value);
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
                parseObject(value);
                handler.endElement(StringUtils.EMPTY, ROW_TAG, ROW_TAG);
            }
            handler.endElement(StringUtils.EMPTY, ROWS_TAG, ROWS_TAG);
        } else if (object instanceof LocalDateTime localDateTime) {
            if (stringifyContext != null) {
                parseString(stringifyContext.toString(localDateTime));
            } else {
                parseString(localDateTime.toString());
            }
        } else if (object instanceof LocalDate localDate) {
            if (stringifyContext != null) {
                parseString(stringifyContext.toString(localDate));
            } else {
                parseString(localDate.toString());
            }
        } else if (object instanceof LocalTime localTime) {
            if (stringifyContext != null) {
                parseString(stringifyContext.toString(localTime));
            } else {
                parseString(localTime.toString());
            }
        } else if (object instanceof Date date) {
            if (stringifyContext != null) {
                parseString(stringifyContext.toString(date));
            } else {
                parseString(date.toString());
            }
        } else {
            String[] readablePropertyNames = BeanUtils.getReadablePropertyNamesWithoutNonSerializable(object);
            if (readablePropertyNames != null) {
                for (String name : readablePropertyNames) {
                    Object value;
                    try {
                        value = BeanUtils.getProperty(object, name);
                    } catch (InvocationTargetException e) {
                        throw new SAXException(e);
                    }
                    checkCircularReference(object, value);
                    handler.startElement(StringUtils.EMPTY, name, name, NULL_ATTRS);
                    parseObject(value);
                    handler.endElement(StringUtils.EMPTY, name, name);
                }
            }
        }
    }

    private void parseString(@NonNull String s) throws SAXException {
        handler.characters(s.toCharArray(), 0, s.length());
    }

    private void checkCircularReference(@NonNull Object wrapper, Object member) throws SAXException {
        if (wrapper.equals(member)) {
            throw new SAXException("XML Serialization Failure: Circular reference was detected" +
                    " while converting member object " + member + " in " + wrapper);
        }
    }

}
