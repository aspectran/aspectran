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
package com.aspectran.thymeleaf.template;

import com.aspectran.utils.Assert;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.cache.AlwaysValidCacheEntryValidity;
import org.thymeleaf.cache.ICacheEntryValidity;
import org.thymeleaf.cache.NonCacheableCacheEntryValidity;
import org.thymeleaf.cache.TTLCacheEntryValidity;
import org.thymeleaf.exceptions.ConfigurationException;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.AbstractTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;

import java.util.Map;

/**
 * A Thymeleaf {@link ITemplateResolver} that resolves templates directly from a
 * {@link String}.
 *
 * <p>This resolver treats the template name passed to the engine as the template
 * content itself. No external file or resource is accessed. By default, templates
 * are considered non-cacheable.</p>
 *
 * <p>Created: 2016. 1. 27.</p>
 */
public class StringTemplateResolver extends AbstractTemplateResolver {

    /**
     * Default template mode: {@link TemplateMode#HTML}
     */
    public static final TemplateMode DEFAULT_TEMPLATE_MODE = TemplateMode.HTML;

    /**
     * Default value for the <i>cacheable</i> flag: {@value}
     */
    public static final boolean DEFAULT_CACHEABLE = false;

    /**
     * Default value for the cache TTL: null. This means the parsed template will live in
     * cache until removed by LRU (because of being the oldest entry).
     */
    public static final Long DEFAULT_CACHE_TTL_MS = null;

    private TemplateMode templateMode = DEFAULT_TEMPLATE_MODE;

    private boolean cacheable = DEFAULT_CACHEABLE;

    private Long cacheTTLMs = DEFAULT_CACHE_TTL_MS;

    /**
     * Creates a new instance of this template resolver.
     */
    public StringTemplateResolver() {
        super();
    }

    /**
     * Returns the template mode to be applied to templates resolved by
     * this template resolver.
     * @return the template mode to be used.
     */
    public TemplateMode getTemplateMode() {
        return this.templateMode;
    }

    /**
     * Sets the template mode to be applied to templates resolved by this resolver.
     * @param templateMode the template mode.
     */
    public void setTemplateMode(TemplateMode templateMode) {
        Assert.notNull(templateMode, "Cannot set a null template mode value");
        this.templateMode = templateMode;
    }

    /**
     * Sets the template mode to be applied to templates resolved by this resolver.
     * Allowed templates modes are defined by the {@link TemplateMode} class.
     * @param templateMode the template mode.
     */
    public void setTemplateMode(String templateMode) {
        // Setter overload actually goes against the JavaBeans spec, but having this one is good for legacy
        // compatibility reasons. Besides, given the getter returns TemplateMode, intelligent frameworks like
        // Spring will recognized the property as TemplateMode-typed and simply ignore this setter.
        Assert.notNull(templateMode, "Cannot set a null template mode value");
        this.templateMode = TemplateMode.parse(templateMode);
    }

    /**
     * Returns whether templates resolved by this resolver have to be considered
     * cacheable or not.
     * @return whether templates resolved are cacheable or not.
     */
    public boolean isCacheable() {
        return this.cacheable;
    }

    /**
     * Sets a new value for the <i>cacheable</i> flag.
     * @param cacheable whether resolved patterns should be considered cacheable or not.
     */
    public void setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
    }

    /**
     * Returns the TTL (Time To Live) in cache of templates resolved by this
     * resolver.
     * <p>
     * If a template is resolved as <i>cacheable</i> but cache TTL is null,
     * this means the template will live in cache until evicted by LRU
     * (Least Recently Used) algorithm for being the oldest entry in cache.
     * @return the cache TTL for resolved templates.
     */
    public Long getCacheTTLMs() {
        return this.cacheTTLMs;
    }

    /**
     * Sets a new value for the cache TTL for resolved templates.
     * <p>
     * If a template is resolved as <i>cacheable</i> but cache TTL is null,
     * this means the template will live in cache until evicted by LRU
     * (Least Recently Used) algorithm for being the oldest entry in cache.
     * @param cacheTTLMs the new cache TTL, or null for using natural LRU eviction.
     */
    public void setCacheTTLMs(Long cacheTTLMs) {
        this.cacheTTLMs = cacheTTLMs;
    }

    @Override
    public void setUseDecoupledLogic(boolean useDecoupledLogic) {
        if (useDecoupledLogic) {
            throw new ConfigurationException("The 'useDecoupledLogic' flag is not allowed for String template resolution");
        }
        super.setUseDecoupledLogic(useDecoupledLogic);
    }

    @Override
    protected ITemplateResource computeTemplateResource(
            IEngineConfiguration configuration,
            String ownerTemplate,
            String template,
            Map<String, Object> templateResolutionAttributes) {
        return new StringTemplateResource(template);
    }

    @Override
    protected TemplateMode computeTemplateMode(
            IEngineConfiguration configuration,
            String ownerTemplate,
            String template,
            Map<String, Object> templateResolutionAttributes) {
        return templateMode;
    }

    @Override
    protected ICacheEntryValidity computeValidity(
            IEngineConfiguration configuration,
            String ownerTemplate,
            String template,
            Map<String, Object> templateResolutionAttributes) {
        if (isCacheable()) {
            if (cacheTTLMs != null) {
                return new TTLCacheEntryValidity(cacheTTLMs);
            } else {
                return AlwaysValidCacheEntryValidity.INSTANCE;
            }
        }
        return NonCacheableCacheEntryValidity.INSTANCE;
    }

}
