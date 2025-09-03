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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.asel.token.Tokenizer;
import com.aspectran.core.context.rule.ability.BeanReferenceable;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.type.BeanRefererType;
import com.aspectran.core.context.rule.type.TextStyleType;
import com.aspectran.core.context.rule.util.TextStyler;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.ResourceUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * Defines a template for generating a response, typically for views.
 * This rule specifies the template engine to use, the location of the template file
 * (or its content directly), and other rendering options.
 *
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class TemplateRule implements Replicable<TemplateRule>, BeanReferenceable {

    private static final String INTERNAL_TEMPLATE_ENGINE_NAME = "token";

    private static final String NONE_TEMPLATE_ENGINE_NAME = "none";

    private static final BeanRefererType BEAN_REFERER_TYPE = BeanRefererType.TEMPLATE_RULE;

    private String id;

    private String engine;

    private String name;

    private String file;

    private String resource;

    private String url;

    private TextStyleType textStyle;

    private String content;

    private String contentType;

    private String encoding;

    private Boolean noCache;

    private String engineBeanId;

    private Class<?> engineBeanClass;

    private String templateSource;

    private Token[] templateTokens;

    private boolean tokenize;

    private volatile long lastModifiedTime;

    private volatile boolean loaded;

    private boolean builtin;

    /**
     * Instantiates a new TemplateRule.
     */
    public TemplateRule() {
    }

    /**
     * Gets the template ID.
     * @return the template ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the template ID.
     * @param id the template ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the name of the template engine.
     * @return the template engine name
     */
    public String getEngine() {
        return engine;
    }

    /**
     * Gets the template name.
     * @return the template name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the template name.
     * @param name the template name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the path to the template file.
     * @return the file path
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the path to the template file.
     * @param file the file path
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * Gets the classpath resource path for the template.
     * @return the resource path
     */
    public String getResource() {
        return resource;
    }

    /**
     * Sets the classpath resource path for the template.
     * @param resource the resource path
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * Gets the URL of the template.
     * @return the URL string
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL of the template.
     * @param url the URL string
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets the text style for the template content.
     * @return the text style type
     */
    public TextStyleType getTextStyle() {
        return textStyle;
    }

    /**
     * Sets the text style for the template content.
     * @param textStyle the text style type
     */
    protected void setTextStyle(TextStyleType textStyle) {
        this.textStyle = textStyle;
    }

    /**
     * Gets the raw content of the template.
     * @return the template content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the raw content of the template.
     * @param content the template content
     */
    protected void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the content type of the response generated by this template.
     * @return the content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type.
     * @param contentType the content type
     */
    protected void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Gets the character encoding for the template.
     * @return the character encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the character encoding for the template.
     * @param encoding the character encoding
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Gets whether to disable caching for this template.
     * @return true to disable cache, false otherwise
     */
    public Boolean getNoCache() {
        return noCache;
    }

    /**
     * Returns whether to disable caching for this template.
     * @return true to disable cache, false otherwise
     */
    public boolean isNoCache() {
        return BooleanUtils.toBoolean(noCache);
    }

    /**
     * Sets whether to disable caching for this template.
     * @param noCache true to disable cache
     */
    public void setNoCache(Boolean noCache) {
        this.noCache = noCache;
    }

    /**
     * Returns whether this is a built-in template.
     * @return true if it is a built-in template, false otherwise
     */
    public boolean isBuiltin() {
        return builtin;
    }

    /**
     * Sets whether this is a built-in template.
     * @param builtin true if it is a built-in template
     */
    public void setBuiltin(boolean builtin) {
        this.builtin = builtin;
    }

    /**
     * Returns whether the template is outsourced to a template engine.
     * @return true if outsourced, false otherwise
     */
    public boolean isOutsourcing() {
        return (name != null && file == null && resource == null && url == null);
    }

    /**
     * Gets the ID of the template engine bean.
     * @return the template engine bean ID
     */
    public String getEngineBeanId() {
        return engineBeanId;
    }

    /**
     * Sets the ID of the template engine bean.
     * @param engineBeanId the template engine bean ID
     */
    public void setEngineBeanId(String engineBeanId) {
        this.engine = engineBeanId;
        if (engineBeanId != null) {
            switch (engineBeanId) {
                case INTERNAL_TEMPLATE_ENGINE_NAME:
                    this.engineBeanId = null;
                    this.tokenize = true;
                    break;
                case NONE_TEMPLATE_ENGINE_NAME:
                    this.engineBeanId = null;
                    this.tokenize = false;
                    break;
                default:
                    this.engineBeanId = engineBeanId;
                    this.tokenize = false;
                    break;
            }
        } else {
            this.engineBeanId = null;
            this.tokenize = true;
        }
    }

    /**
     * Gets the class of the template engine bean.
     * @return the template engine bean class
     */
    public Class<?> getEngineBeanClass() {
        return engineBeanClass;
    }

    /**
     * Sets the class of the template engine bean.
     * @param engineBeanClass the template engine bean class
     */
    public void setEngineBeanClass(Class<?> engineBeanClass) {
        this.engineBeanClass = engineBeanClass;
    }

    /**
     * Returns whether an external template engine is used.
     * @return true if an external engine is used, false otherwise
     */
    public boolean isExternalEngine() {
        return (this.engineBeanId != null);
    }

    /**
     * Returns whether the template content should be tokenized.
     * @return true if tokenization is enabled, false otherwise
     */
    public boolean isTokenize() {
        return this.tokenize;
    }

    /**
     * Gets the loaded template source content.
     * @return the template source
     */
    public String getTemplateSource() {
        return templateSource;
    }

    /**
     * Gets the template source, loading it from its source if necessary.
     * @param context the activity context
     * @return the template source content
     * @throws IOException if an I/O error occurs
     */
    public String getTemplateSource(ActivityContext context) throws IOException {
        if (this.file != null || this.resource != null || this.url != null) {
            if (isNoCache()) {
                return loadTemplateSource(context);
            } else {
                loadCachedTemplateSource(context);
                return this.templateSource;
            }
        } else {
            return this.templateSource;
        }
    }

    /**
     * Sets the template source content and parses it into tokens if tokenization is enabled.
     * @param templateSource the template source content
     */
    public void setTemplateSource(String templateSource) {
        this.templateSource = templateSource;
        if (isTokenize()) {
            this.templateTokens = parseContentTokens(templateSource);
        }
    }

    protected void setTemplateSource(String templateSource, Token[] templateTokens) {
        this.templateSource = templateSource;
        this.templateTokens = templateTokens;
    }

    /**
     * Gets the parsed tokens from the template source.
     * @return an array of tokens
     */
    public Token[] getTemplateTokens() {
        return this.templateTokens;
    }

    /**
     * Gets the parsed tokens, loading the template source if necessary.
     * @param context the activity context
     * @return an array of tokens
     * @throws IOException if an I/O error occurs
     */
    public Token[] getTemplateTokens(ActivityContext context) throws IOException {
        if (isExternalEngine()) {
            throw new UnsupportedOperationException();
        }
        if (this.file != null || this.resource != null || this.url != null) {
            if (isNoCache()) {
                String source = loadTemplateSource(context);
                return parseContentTokens(source);
            } else {
                loadCachedTemplateSource(context);
                return this.templateTokens;
            }
        } else {
            return this.templateTokens;
        }
    }

    private Token[] parseContentTokens(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        List<Token> tokenList = Tokenizer.tokenize(content, false);
        if (!tokenList.isEmpty()) {
            return tokenList.toArray(new Token[0]);
        } else {
            return new Token[0];
        }
    }

    private String loadTemplateSource(ActivityContext context) throws IOException {
        String templateSource = null;
        if (this.file != null) {
            File file = context.getApplicationAdapter().getRealPath(this.file).toFile();
            templateSource = ResourceUtils.read(file, this.encoding);
        } else if (this.resource != null) {
            ClassLoader classLoader = context.getAvailableActivity().getClassLoader();
            URL url = classLoader.getResource(this.resource);
            templateSource = ResourceUtils.read(url, this.encoding);
        } else if (this.url != null) {
            URL url = URI.create(this.url).toURL();
            templateSource = ResourceUtils.read(url, this.encoding);
        }
        return templateSource;
    }

    private void loadCachedTemplateSource(ActivityContext context) throws IOException {
        if (this.file != null) {
            File file = context.getApplicationAdapter().getRealPath(this.file).toFile();
            long time1 = this.lastModifiedTime;
            long time2 = file.lastModified();
            if (time2 > time1) {
                synchronized (this) {
                    time1 = this.lastModifiedTime;
                    time2 = file.lastModified();
                    if (time2 > time1) {
                        String template = ResourceUtils.read(file, this.encoding);
                        setTemplateSource(template);
                        this.lastModifiedTime = time2;
                    }
                }
            }
        } else if (this.resource != null) {
            boolean loaded = this.loaded;
            if (!loaded) {
                synchronized (this) {
                    loaded = this.loaded;
                    if (!loaded) {
                        URL url = context.getAvailableActivity().getClassLoader().getResource(this.resource);
                        String template = ResourceUtils.read(url, this.encoding);
                        setTemplateSource(template);
                        this.loaded = true;
                    }
                }
            }
        } else if (this.url != null) {
            boolean loaded = this.loaded;
            if (!loaded) {
                synchronized (this) {
                    loaded = this.loaded;
                    if (!loaded) {
                        URL url = URI.create(this.url).toURL();
                        String template = ResourceUtils.read(url, this.encoding);
                        setTemplateSource(template);
                        this.loaded = true;
                    }
                }
            }
        }
    }

    @Override
    public TemplateRule replicate() {
        return replicate(this);
    }

    @Override
    public BeanRefererType getBeanRefererType() {
        return BEAN_REFERER_TYPE;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        if (!builtin) {
            tsb.append("id", id);
        }
        tsb.append("engine", engine);
        if (file != null) {
            tsb.append("file", file);
        } else if (resource != null) {
            tsb.append("resource", resource);
        } else if (url != null) {
            tsb.append("url", url);
        } else if (name != null) {
            tsb.append("name", name);
        } else {
            tsb.appendSize("contentLength", content);
        }
        tsb.append("style", textStyle);
        tsb.append("contentType", contentType);
        tsb.append("encoding", encoding);
        tsb.append("noCache", noCache);
        return tsb.toString();
    }

    /**
     * Creates a new instance of TemplateRule.
     * @param id the template ID
     * @param engine the template engine name
     * @param name the template name
     * @param file the template file path
     * @param resource the template classpath resource
     * @param url the template URL
     * @param style the text style
     * @param content the template content
     * @param contentType the response content type
     * @param encoding the character encoding
     * @param noCache whether to disable caching
     * @return a new TemplateRule instance
     * @throws IllegalRuleException if the configuration is invalid
     */
    @NonNull
    public static TemplateRule newInstance(String id, String engine, String name,
                                           String file, String resource, String url,
                                           String style, String content, String contentType,
                                           String encoding, Boolean noCache)
            throws IllegalRuleException {

        if (id == null) {
            throw new IllegalRuleException("The 'template' element requires an 'id' attribute");
        }

        TextStyleType textStyleType = TextStyleType.resolve(style);
        if (style != null && textStyleType == null) {
            throw new IllegalRuleException("No text style type for '" + style + "'");
        }

        TemplateRule tr = new TemplateRule();
        tr.setId(id);
        tr.setEngineBeanId(engine);
        tr.setName(name);
        tr.setFile(file);
        tr.setResource(resource);
        tr.setUrl(url);
        tr.setTextStyle(textStyleType);
        tr.setContent(content);
        tr.setContentType(contentType);
        tr.setEncoding(encoding);
        tr.setNoCache(noCache);

        updateTemplateSource(tr);
        return tr;
    }

    /**
     * Creates a new instance of a built-in TemplateRule.
     * @return a new TemplateRule instance
     * @throws IllegalRuleException if the configuration is invalid
     */
    @NonNull
    public static TemplateRule newInstanceForBuiltin(String id, String engine, String name,
                                                     String file, String resource, String url,
                                                     String style, String content, String contentType,
                                                     String encoding, Boolean noCache)
            throws IllegalRuleException {

        TextStyleType textStyleType = TextStyleType.resolve(style);
        if (style != null && textStyleType == null) {
            throw new IllegalRuleException("No text style type for '" + style + "'");
        }

        TemplateRule tr = new TemplateRule();
        tr.setId(id);
        tr.setEngineBeanId(engine);
        tr.setName(name);
        tr.setFile(file);
        tr.setResource(resource);
        tr.setUrl(url);
        tr.setTextStyle(textStyleType);
        tr.setContent(content);
        tr.setContentType(contentType);
        tr.setEncoding(encoding);
        tr.setNoCache(noCache);
        tr.setBuiltin(true);

        updateTemplateSource(tr);
        return tr;
    }

    /**
     * Creates a replica of the given TemplateRule.
     * @param templateRule the template rule to replicate
     * @return a new, replicated instance of TemplateRule
     */
    @NonNull
    public static TemplateRule replicate(@NonNull TemplateRule templateRule) {
        TemplateRule tr = new TemplateRule();
        tr.setId(templateRule.getId());
        tr.setEngineBeanId(templateRule.getEngine());
        tr.setEngineBeanClass(templateRule.getEngineBeanClass());
        tr.setName(templateRule.getName());
        tr.setFile(templateRule.getFile());
        tr.setResource(templateRule.getResource());
        tr.setUrl(templateRule.getUrl());
        tr.setTextStyle(templateRule.getTextStyle());
        tr.setContent(templateRule.getContent());
        tr.setContentType(templateRule.getContentType());
        tr.setTemplateSource(templateRule.getTemplateSource(), templateRule.getTemplateTokens());
        tr.setEncoding(templateRule.getEncoding());
        tr.setNoCache(templateRule.getNoCache());
        tr.setBuiltin(templateRule.isBuiltin());
        return tr;
    }

    /**
     * Updates the template source content and styles it.
     * @param templateRule the template rule to update
     * @param content the new content
     */
    public static void updateTemplateSource(@NonNull TemplateRule templateRule, String content) {
        templateRule.setContent(content);
        updateTemplateSource(templateRule);
    }

    private static void updateTemplateSource(@NonNull TemplateRule templateRule) {
        String content = templateRule.getContent();
        if (content != null) {
            content = TextStyler.styling(content, templateRule.getTextStyle());
            templateRule.setTemplateSource(content);
        }
    }

}
