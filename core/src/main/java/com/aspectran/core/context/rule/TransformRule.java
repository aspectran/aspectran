/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.type.ContentType;
import com.aspectran.core.context.rule.type.FormatType;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.ToStringBuilder;

/**
 * The Class TransformRule.
 *
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class TransformRule implements Replicable<TransformRule> {

    public static final ResponseType RESPONSE_TYPE = ResponseType.TRANSFORM;

    private FormatType formatType;

    private String contentType;

    private String encoding;

    private Boolean pretty;

    private String templateId;

    private TemplateRule templateRule;

    private Boolean defaultResponse;

    /**
     * Instantiates a new TransformRule.
     */
    public TransformRule() {
    }

    /**
     * Gets the format type.
     * @return the format type
     */
    public FormatType getFormatType() {
        return formatType;
    }

    /**
     * Sets the format type.
     * @param formatType the format type to set
     */
    public void setFormatType(FormatType formatType) {
        if (formatType == FormatType.CUSTOM) {
            throw new IllegalArgumentException("CustomTransform is only allowed to be defined via an annotated method");
        }
        this.formatType = formatType;
        if (this.contentType == null && formatType != null) {
            if (formatType == FormatType.TEXT) {
                this.contentType = ContentType.TEXT_PLAIN.toString();
            } else if (formatType == FormatType.JSON) {
                this.contentType = ContentType.APPLICATION_JSON.toString();
            } else if (formatType == FormatType.XML) {
                this.contentType = ContentType.APPLICATION_XML.toString();
            }
        }
    }

    /**
     * Gets the content type.
     * @return the content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type.
     * @param contentType the new content type
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Gets the character encoding.
     * @return the character encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the character encoding.
     * @param encoding the character encoding to set
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Returns whether to format the content of the response
     * to make it easier to read by adding spaces or tabs.
     * @return true, if the content of the response should be
     *      formatted to make it easier to read
     */
    public Boolean getPretty() {
        return pretty;
    }

    /**
     * Returns whether to format the content of the response
     * to make it easier to read by adding spaces or tabs.
     * @return true, if the content of the response should be
     *      formatted to make it easier to read
     */
    public boolean isPretty() {
        return BooleanUtils.toBoolean(pretty);
    }

    /**
     * Set whether to format the content of the response
     * to make it easier to read by adding spaces or tabs.
     * @param pretty if true, format the content of the response
     *      by adding spaces or tabs
     */
    public void setPretty(Boolean pretty) {
        this.pretty = pretty;
    }

    /**
     * Gets the template id.
     * @return the template id
     */
    public String getTemplateId() {
        return templateId;
    }

    /**
     * Sets the template id.
     * @param templateId the template id
     */
    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    /**
     * Gets the template rule.
     * @return the template rule
     */
    public TemplateRule getTemplateRule() {
        return templateRule;
    }

    /**
     * Sets the template rule.
     * @param templateRule the template rule
     */
    public void setTemplateRule(TemplateRule templateRule) {
        this.templateRule = templateRule;
        if (templateRule != null) {
            this.templateId = templateRule.getId();
            if (this.formatType == null) {
                setFormatType(FormatType.TEXT);
            }
            if (templateRule.getEncoding() != null && this.encoding == null) {
                this.encoding = templateRule.getEncoding();
            }
        }
    }

    /**
     * Returns whether the default response.
     * @return whether the default response
     */
    public Boolean getDefaultResponse() {
        return defaultResponse;
    }

    /**
     * Returns whether the default response.
     * @return true, if is default response
     */
    public boolean isDefaultResponse() {
        return BooleanUtils.toBoolean(defaultResponse);
    }

    /**
     * Sets whether the default response.
     * @param defaultResponse whether the default response
     */
    public void setDefaultResponse(Boolean defaultResponse) {
        this.defaultResponse = defaultResponse;
    }

    @Override
    public TransformRule replicate() {
        return replicate(this);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.appendForce("type", RESPONSE_TYPE);
        tsb.appendForce("format", formatType);
        tsb.append("contentType", contentType);
        tsb.append("encoding", encoding);
        tsb.append("default", getDefaultResponse());
        tsb.append("pretty", pretty);
        tsb.append("template", templateId);
        tsb.append("template", templateRule);
        return tsb.toString();
    }

    public static TransformRule newInstance(String format, String contentType,
            String encoding, Boolean defaultResponse, Boolean pretty) {
        FormatType formatType = FormatType.resolve(format);
        return newInstance(formatType, contentType, encoding, defaultResponse, pretty);
    }

    public static TransformRule newInstance(FormatType formatType, String contentType,
                                            String encoding, Boolean pretty) {
        return newInstance(formatType, contentType, encoding, null, pretty);
    }

    public static TransformRule newInstance(FormatType formatType, String contentType,
                                            String encoding, Boolean defaultResponse, Boolean pretty) {
        if (formatType == null && contentType != null) {
            formatType = FormatType.resolve(ContentType.resolve(contentType));
        }
        TransformRule tr = new TransformRule();
        tr.setFormatType(formatType);
        if (contentType != null) {
            tr.setContentType(contentType);
        }
        tr.setEncoding(encoding);
        tr.setDefaultResponse(defaultResponse);
        tr.setPretty(pretty);
        return tr;
    }

    public static TransformRule replicate(TransformRule transformRule) {
        TransformRule tr = new TransformRule();
        tr.setFormatType(transformRule.getFormatType());
        tr.setContentType(transformRule.getContentType());
        tr.setEncoding(transformRule.getEncoding());
        tr.setDefaultResponse(transformRule.getDefaultResponse());
        tr.setPretty(transformRule.getPretty());
        TemplateRule templateRule = transformRule.getTemplateRule();
        if (templateRule != null) {
            tr.setTemplateRule(templateRule.replicate());
        }
        return tr;
    }

}
