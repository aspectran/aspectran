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
package com.aspectran.core.activity.response.transform;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.transform.xml.ContentsInputSource;
import com.aspectran.core.activity.response.transform.xml.ContentsXMLReader;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.utils.StringifyContext;
import com.aspectran.utils.annotation.jsr305.NonNull;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.Writer;

/**
 * XML Transform Response converts the response data to XML and outputs it.
 *
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class XmlTransformResponse extends TransformResponse {

    private static final String OUTPUT_METHOD_XML = "xml";

    private static final String INDENT_NUMBER_KEY = "indent-number";

    private static final Integer DEFAULT_INDENT_SIZE = 2;

    private static final String YES = "yes";

    private final String contentType;

    private final String encoding;

    private final Boolean pretty;

    /**
     * Instantiates a new XmlTransformResponse.
     * @param transformRule the transform rule
     */
    public XmlTransformResponse(TransformRule transformRule) {
        super(transformRule);

        this.contentType = transformRule.getContentType();
        this.encoding = transformRule.getEncoding();
        this.pretty = transformRule.getPretty();
    }

    @Override
    protected void transform(@NonNull Activity activity) throws Exception {
        ResponseAdapter responseAdapter = activity.getResponseAdapter();

        String encoding;
        if (this.encoding != null) {
            encoding = this.encoding;
        } else {
            encoding = responseAdapter.getEncoding();
            if (encoding == null) {
                encoding = activity.getTranslet().getDefinitiveResponseEncoding();
            }
        }
        if (encoding != null) {
            responseAdapter.setEncoding(encoding);
        }

        if (contentType != null) {
            responseAdapter.setContentType(contentType);
        }

        ProcessResult processResult = activity.getProcessResult();
        Writer writer = responseAdapter.getWriter();

        StringifyContext stringifyContext = activity.getStringifyContext();
        if (pretty != null && stringifyContext.isPrettyPrint() != pretty) {
            stringifyContext = stringifyContext.clone();
            stringifyContext.setPrettyPrint(pretty);
        }

        transform(processResult, writer, encoding, stringifyContext, pretty);
    }

    @Override
    public Response replicate() {
        return new XmlTransformResponse(getTransformRule().replicate());
    }

    public static void transform(
            Object object, Writer writer, String encoding,
            StringifyContext stringifyContext) throws TransformerException {
        transform(object, writer, encoding, stringifyContext, null);
    }

    private static void transform(
            Object object, Writer writer, String encoding,
            StringifyContext stringifyContext, Boolean prettyForce) throws TransformerException {
        boolean pretty = (prettyForce != null ? prettyForce : (stringifyContext == null || stringifyContext.isPrettyPrint()));

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        if (pretty) {
            if (stringifyContext != null && stringifyContext.hasIndentSize()) {
                transformerFactory.setAttribute(INDENT_NUMBER_KEY, stringifyContext.getIndentSize());
            } else {
                transformerFactory.setAttribute(INDENT_NUMBER_KEY, DEFAULT_INDENT_SIZE);
            }
        }

        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, OUTPUT_METHOD_XML);
        if (encoding != null) {
            transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
        }
        if (pretty) {
            transformer.setOutputProperty(OutputKeys.INDENT, YES);
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, YES);
        }

        ContentsXMLReader xmlReader = new ContentsXMLReader();
        xmlReader.setStringifyContext(stringifyContext);

        ContentsInputSource inputSource = new ContentsInputSource(object);
        Source source = new SAXSource(xmlReader, inputSource);
        transformer.transform(source, new StreamResult(writer));
    }

}
