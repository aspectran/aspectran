/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
 * The Class TemplateRule.
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

    public TemplateRule() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEngine() {
        return engine;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public TextStyleType getTextStyle() {
        return textStyle;
    }

    protected void setTextStyle(TextStyleType textStyle) {
        this.textStyle = textStyle;
    }

    public String getContent() {
        return content;
    }

    protected void setContent(String content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    protected void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public Boolean getNoCache() {
        return noCache;
    }

    public boolean isNoCache() {
        return BooleanUtils.toBoolean(noCache);
    }

    public void setNoCache(Boolean noCache) {
        this.noCache = noCache;
    }

    public boolean isBuiltin() {
        return builtin;
    }

    public void setBuiltin(boolean builtin) {
        this.builtin = builtin;
    }

    public boolean isOutsourcing() {
        return (name != null && file == null && resource == null && url == null);
    }

    public String getEngineBeanId() {
        return engineBeanId;
    }

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

    public Class<?> getEngineBeanClass() {
        return engineBeanClass;
    }

    public void setEngineBeanClass(Class<?> engineBeanClass) {
        this.engineBeanClass = engineBeanClass;
    }

    public boolean isExternalEngine() {
        return (this.engineBeanId != null);
    }

    public boolean isTokenize() {
        return this.tokenize;
    }

    public String getTemplateSource() {
        return templateSource;
    }

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

    public Token[] getTemplateTokens() {
        return this.templateTokens;
    }

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
            File file = context.getApplicationAdapter().toRealPathAsFile(this.file);
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
            File file = context.getApplicationAdapter().toRealPathAsFile(this.file);
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
