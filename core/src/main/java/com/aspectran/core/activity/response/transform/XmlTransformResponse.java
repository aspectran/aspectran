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
package com.aspectran.core.activity.response.transform;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.transform.xml.ContentsInputSource;
import com.aspectran.core.activity.response.transform.xml.ContentsXMLReader;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.Writer;

/**
 * The Class XmlTransformResponse.
 * 
 * Created: 2008. 03. 22 PM 5:51:58
 */
public class XmlTransformResponse extends TransformResponse {

    private static final String OUTPUT_METHOD_XML = "xml";

    private static final String OUTPUT_INDENT_YES = "yes";

    private static final String INDENT_NUMBER_KEY = "indent-number";

    private static final Integer INDENT_NUMBER_VAL = 1;

    private static final Log log = LogFactory.getLog(XmlTransformResponse.class);

    private final String encoding;

    private final String contentType;

    private final boolean pretty;

    /**
     * Instantiates a new XmlTransform.
     *
     * @param transformRule the transform rule
     */
    public XmlTransformResponse(TransformRule transformRule) {
        super(transformRule);

        this.encoding = transformRule.getEncoding();
        this.contentType = transformRule.getContentType();
        this.pretty = transformRule.isPretty();
    }

    @Override
    public void commit(Activity activity) throws TransformResponseException {
        ResponseAdapter responseAdapter = activity.getResponseAdapter();
        if (responseAdapter == null) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("response " + transformRule);
        }

        try {
            String encoding;
            if (this.encoding != null) {
                encoding = this.encoding;
            } else {
                encoding = activity.getTranslet().getResponseEncoding();
            }
            if (encoding != null) {
                responseAdapter.setEncoding(encoding);
            }
            if (contentType != null) {
                responseAdapter.setContentType(contentType);
            }

            Writer writer = responseAdapter.getWriter();
            ProcessResult processResult = activity.getProcessResult();

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            if (pretty) {
                transformerFactory.setAttribute(INDENT_NUMBER_KEY, INDENT_NUMBER_VAL);
            }

            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, OUTPUT_METHOD_XML);
            if (encoding != null) {
                transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
            }
            if (pretty) {
                transformer.setOutputProperty(OutputKeys.INDENT, OUTPUT_INDENT_YES);
            }

            ContentsXMLReader xreader = new ContentsXMLReader();
            ContentsInputSource isource = new ContentsInputSource(processResult);
            transformer.transform(new SAXSource(xreader, isource), new StreamResult(writer));
        } catch (Exception e) {
            throw new TransformResponseException(transformRule, e);
        }
    }

    @Override
    public ActionList getActionList() {
        return transformRule.getActionList();
    }

    @Override
    public Response replicate() {
        TransformRule transformRule = getTransformRule().replicate();
        return new XmlTransformResponse(transformRule);
    }

}
