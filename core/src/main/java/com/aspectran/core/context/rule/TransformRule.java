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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.rule.ability.ActionPossessSupport;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.type.ContentType;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.context.rule.type.TransformType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class TransformRule.
 * 
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class TransformRule extends ActionPossessSupport implements Replicable<TransformRule> {

    public static final ResponseType RESPONSE_TYPE = ResponseType.TRANSFORM;

    private TransformType transformType;

    private String contentType;

    private String encoding;

    private Boolean defaultResponse;

    private Boolean pretty;

    private String templateId;

    private TemplateRule templateRule;

    /**
     * Instantiates a new TransformRule.
     */
    public TransformRule() {
    }

    /**
     * Gets the transform type.
     *
     * @return the transform type
     */
    public TransformType getTransformType() {
        return transformType;
    }

    /**
     * Sets the transform type.
     *
     * @param transformType the transformType to set
     */
    public void setTransformType(TransformType transformType) {
        this.transformType = transformType;

        if (contentType == null) {
            if (transformType == TransformType.TEXT) {
                contentType = ContentType.TEXT_PLAIN.toString();
            } else if (transformType == TransformType.JSON) {
                contentType = ContentType.TEXT_JSON.toString();
            } else if (transformType == TransformType.XML) {
                contentType = ContentType.TEXT_XML.toString();
            }
        }
    }

    /**
     * Gets the content type.
     *
     * @return the content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type.
     *
     * @param contentType the new content type
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Gets the character encoding.
     *
     * @return the character encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the character encoding.
     *
     * @param encoding the character encoding to set
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Returns whether this is the default response.
     *
     * @return whether this is the default response
     */
    public Boolean getDefaultResponse() {
        return defaultResponse;
    }

    /**
     * Returns whether this is the default response.
     *
     * @return whether this is the default response
     */
    public boolean isDefaultResponse() {
        return BooleanUtils.toBoolean(defaultResponse);
    }

    /**
     * Sets whether this is the default response.
     *
     * @param defaultResponse whether this is the default response
     */
    public void setDefaultResponse(Boolean defaultResponse) {
        this.defaultResponse = defaultResponse;
    }

    /**
     * Returns whether to format the content of the response
     * to make it easier to read by adding spaces or tabs.
     *
     * @return true, if the content of the response should be
     *      formatted to make it easier to read
     */
    public Boolean getPretty() {
        return pretty;
    }

    /**
     * Returns whether to format the content of the response
     * to make it easier to read by adding spaces or tabs.
     *
     * @return true, if the content of the response should be
     *      formatted to make it easier to read
     */
    public boolean isPretty() {
        return BooleanUtils.toBoolean(pretty);
    }

    /**
     * Set whether to format the content of the response
     * to make it easier to read by adding spaces or tabs.
     *
     * @param pretty if true, format the content of the response
     *      by adding spaces or tabs
     */
    public void setPretty(Boolean pretty) {
        this.pretty = pretty;
    }

    /**
     * Gets the template id.
     *
     * @return the template id
     */
    public String getTemplateId() {
        return templateId;
    }

    /**
     * Sets the template id.
     *
     * @param templateId the template id
     */
    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    /**
     * Gets the template rule.
     *
     * @return the template rule
     */
    public TemplateRule getTemplateRule() {
        return templateRule;
    }

    /**
     * Sets the template rule.
     *
     * @param templateRule the template rule
     */
    public void setTemplateRule(TemplateRule templateRule) {
        this.templateRule = templateRule;
        if (templateRule != null) {
            if (templateRule.getEncoding() != null && this.encoding == null) {
                this.encoding = templateRule.getEncoding();
            }
        }
    }

    @Override
    public TransformRule replicate() {
        return replicate(this);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.appendForce("responseType", RESPONSE_TYPE);
        tsb.append("transformType", transformType);
        tsb.append("contentType", contentType);
        tsb.append("encoding", encoding);
        tsb.append("defaultResponse", defaultResponse);
        tsb.append("pretty", pretty);
        tsb.append("template", templateId);
        tsb.append("template", templateRule);
        return tsb.toString();
    }

    public static TransformRule newInstance(String type, String contentType,
            String encoding, Boolean defaultResponse, Boolean pretty) {
        TransformType transformType = TransformType.resolve(type);
        if (transformType == null && contentType != null) {
            transformType = TransformType.resolve(ContentType.resolve(contentType));
        }
        TransformRule tr = new TransformRule();
        tr.setTransformType(transformType);
        if (contentType != null) {
            tr.setContentType(contentType);
        }
        tr.setEncoding(encoding);
        tr.setDefaultResponse(defaultResponse);
        tr.setPretty(pretty);
        return tr;
    }

    public static TransformRule newInstance(TransformType transformType, String contentType,
            String encoding, Boolean defaultResponse, Boolean pretty) {
        if (transformType == null && contentType != null) {
            transformType = TransformType.resolve(ContentType.resolve(contentType));
        }
        TransformRule tr = new TransformRule();
        tr.setTransformType(transformType);
        if (contentType != null) {
            tr.setContentType(contentType);
        }
        tr.setEncoding(encoding);
        tr.setDefaultResponse(defaultResponse);
        tr.setPretty(pretty);
        return tr;
    }

    public static void updateTemplateId(TransformRule transformRule, String templateId) throws IllegalRuleException {
        if (templateId == null) {
            throw new IllegalRuleException("The 'call' element of 'transform' must have a 'template' attribute");
        }
        transformRule.setTemplateId(templateId);
    }

    public static TransformRule replicate(TransformRule transformRule) {
        TransformRule tr = new TransformRule();
        tr.setTransformType(transformRule.getTransformType());
        tr.setContentType(transformRule.getContentType());
        tr.setEncoding(transformRule.getEncoding());
        tr.setDefaultResponse(transformRule.getDefaultResponse());
        tr.setPretty(transformRule.getPretty());
        tr.setActionList(transformRule.getActionList());
        TemplateRule templateRule = transformRule.getTemplateRule();
        if (templateRule != null) {
            tr.setTemplateRule(templateRule.replicate());
        }
        return tr;
    }

}
