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
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.type.ContentType;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

/**
 * The Class XslTransformResponse.
 * 
 * Created: 2008. 03. 22 PM 5:51:58
 */
public class XslTransformResponse extends TransformResponse {

    private static final String OUTPUT_METHOD_XML = "xml";

    private static final String OUTPUT_METHOD_HTML = "html";

    private static final String OUTPUT_METHOD_TEXT = "text";

    private static final Log log = LogFactory.getLog(XslTransformResponse.class);

    private final TemplateRule templateRule;

    private Templates templates;

    private String contentType;

    private String outputEncoding;

    private volatile long templateLastModifiedTime;

    private volatile boolean templateLoaded;

    /**
     * Instantiates a new XslTransform.
     *
     * @param transformRule the transform rule
     */
    public XslTransformResponse(TransformRule transformRule) {
        super(transformRule);
        this.templateRule = transformRule.getTemplateRule();
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
            loadTemplate(activity.getActivityContext().getEnvironment());

            if (outputEncoding != null) {
                responseAdapter.setEncoding(outputEncoding);
            }
            if (contentType != null) {
                responseAdapter.setContentType(contentType);
            }

            Writer writer = responseAdapter.getWriter();
            ProcessResult processResult = activity.getProcessResult();

            ContentsXMLReader xreader = new ContentsXMLReader();
            ContentsInputSource isource = new ContentsInputSource(processResult);

            Transformer transformer = templates.newTransformer();
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
        return new XslTransformResponse(transformRule);
    }

    private void loadTemplate(Environment environment) throws TransformerConfigurationException, IOException {
        String templateFile = templateRule.getFile();
        String templateResource = templateRule.getResource();
        String templateUrl = templateRule.getUrl();
        boolean noCache = templateRule.isNoCache();

        if (templateFile != null) {
            if (noCache) {
                File file = environment.toRealPathAsFile(templateFile);
                this.templates = createTemplates(file);
                determineOutputStyle();
            } else {
                File file = environment.toRealPathAsFile(templateFile);
                long lastModifiedTime = file.lastModified();
                if (lastModifiedTime > this.templateLastModifiedTime) {
                    synchronized (this) {
                        lastModifiedTime = file.lastModified();
                        if (lastModifiedTime > this.templateLastModifiedTime) {
                            this.templates = createTemplates(file);
                            determineOutputStyle();
                            this.templateLastModifiedTime = lastModifiedTime;
                        }
                    }
                }
            }
        } else if (templateResource != null) {
            if (noCache) {
                ClassLoader classLoader = environment.getClassLoader();
                this.templates = createTemplates(classLoader.getResource(templateResource));
                determineOutputStyle();
            } else {
                if (!this.templateLoaded) {
                    synchronized (this) {
                        if (!this.templateLoaded) {
                            ClassLoader classLoader = environment.getClassLoader();
                            this.templates = createTemplates(classLoader.getResource(templateResource));
                            determineOutputStyle();
                            this.templateLoaded = true;
                        }
                    }
                }
            }
        } else if (templateUrl != null) {
            if (noCache) {
                this.templates = createTemplates(new URL(templateUrl));
                determineOutputStyle();
            } else {
                if (!this.templateLoaded) {
                    synchronized (this) {
                        if (!this.templateLoaded) {
                            this.templates = createTemplates(new URL(templateUrl));
                            determineOutputStyle();
                            this.templateLoaded = true;
                        }
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("No specified template " + templateRule);
        }
    }

    private void determineOutputStyle() {
        contentType = transformRule.getContentType();
        if (contentType == null) {
            contentType = getContentType(templates);
        }

        outputEncoding = getOutputEncoding(templates);
        if (outputEncoding == null) {
            outputEncoding = transformRule.getEncoding();
        }
    }

    private Templates createTemplates(File templateFile) throws TransformerConfigurationException {
        Source source = new StreamSource(templateFile);
        return createTemplates(source);
    }
    
    private Templates createTemplates(URL url) throws TransformerConfigurationException, IOException {
        URLConnection conn = url.openConnection();
        Source source = new StreamSource(conn.getInputStream());
        return createTemplates(source);
    }

    private Templates createTemplates(Source source) throws TransformerConfigurationException {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        transFactory.setAttribute("generate-translet", Boolean.TRUE);
        return transFactory.newTemplates(source);
    }

    private String getContentType(Templates templates) {
        Properties outputProperties = templates.getOutputProperties();
        String outputMethod = outputProperties.getProperty(OutputKeys.METHOD);
        String contentType = null;
        if (outputMethod != null) {
            if (outputMethod.equalsIgnoreCase(XslTransformResponse.OUTPUT_METHOD_XML)) {
                contentType = ContentType.TEXT_XML.toString();
            } else if (outputMethod.equalsIgnoreCase(XslTransformResponse.OUTPUT_METHOD_HTML)) {
                contentType = ContentType.TEXT_HTML.toString();
            } else if (outputMethod.equalsIgnoreCase(XslTransformResponse.OUTPUT_METHOD_TEXT)) {
                contentType = ContentType.TEXT_PLAIN.toString();
            }
        }
        return contentType;
    }
    
    private String getOutputEncoding(Templates templates) {
        Properties outputProperties = templates.getOutputProperties();
        return outputProperties.getProperty(OutputKeys.ENCODING);
    }

}
