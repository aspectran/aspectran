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
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.AbstractTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;

import java.util.Map;

/**
 * A Thymeleaf {@link ITemplateResolver} that acts as a default, always returning
 * a predefined template content.
 *
 * <p>This resolver is useful as a fallback or for testing purposes. It always
 * considers its resolved templates as cacheable and uses {@link TemplateMode#HTML}
 * by default.</p>
 *
 * <p>Created: 2016. 1. 27.</p>
 */
public class DefaultTemplateResolver extends AbstractTemplateResolver {

    /**
     * Default template mode: {@link TemplateMode#HTML}
     */
    public static final TemplateMode DEFAULT_TEMPLATE_MODE = TemplateMode.HTML;

    private TemplateMode templateMode = DEFAULT_TEMPLATE_MODE;

    private String template = "";

    /**
     * Creates a new instance of this template resolver.
     */
    public DefaultTemplateResolver() {
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
     * Returns the text that will always be returned by this template resolver as the resolved template.
     * @return the text to be returned as template.
     */
    public String getTemplate() {
        return this.template;
    }

    /**
     * Set the text that will be returned as the resolved template.
     * @param template the text to be returned as template.
     */
    public void setTemplate(String template) {
        this.template = template;
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
        return AlwaysValidCacheEntryValidity.INSTANCE;
    }

}