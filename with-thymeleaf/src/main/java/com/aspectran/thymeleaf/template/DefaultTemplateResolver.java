/*
 * =============================================================================
 *
 *   Copyright (c) 2011-2018, The THYMELEAF team (http://www.thymeleaf.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * =============================================================================
 */
package com.aspectran.thymeleaf.template;

import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.cache.AlwaysValidCacheEntryValidity;
import org.thymeleaf.cache.ICacheEntryValidity;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.AbstractTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;
import org.thymeleaf.util.Validate;

import java.util.Map;

/**
 * Implementation of {@link ITemplateResolver} that extends {@link AbstractTemplateResolver}
 * and acts as a default template resolver, always returning the same specified text in the form of
 * a {@link StringTemplateResource} instance.
 * This template resolver will consider its resolved templates always <strong>cacheable</strong>.
 * Also, the {@link TemplateMode#HTML} template mode will be used by default.
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
    public final TemplateMode getTemplateMode() {
        return this.templateMode;
    }

    /**
     * Sets the template mode to be applied to templates resolved by this resolver.
     * @param templateMode the template mode.
     */
    public final void setTemplateMode(final TemplateMode templateMode) {
        Validate.notNull(templateMode, "Cannot set a null template mode value");
        this.templateMode = templateMode;
    }

    /**
     * Sets the template mode to be applied to templates resolved by this resolver.
     * Allowed templates modes are defined by the {@link TemplateMode} class.
     * @param templateMode the template mode.
     */
    public final void setTemplateMode(final String templateMode) {
        // Setter overload actually goes against the JavaBeans spec, but having this one is good for legacy
        // compatibility reasons. Besides, given the getter returns TemplateMode, intelligent frameworks like
        // Spring will recognized the property as TemplateMode-typed and simply ignore this setter.
        Validate.notNull(templateMode, "Cannot set a null template mode value");
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
    public void setTemplate(final String template) {
        this.template = template;
    }

    @Override
    protected ITemplateResource computeTemplateResource(final IEngineConfiguration configuration,
                                                        final String ownerTemplate, final String template,
                                                        final Map<String, Object> templateResolutionAttributes) {
        return new StringTemplateResource(this.template);
    }

    @Override
    protected TemplateMode computeTemplateMode(final IEngineConfiguration configuration, final String ownerTemplate,
                                               final String template,
                                               final Map<String, Object> templateResolutionAttributes) {
        return this.templateMode;
    }

    @Override
    protected ICacheEntryValidity computeValidity(final IEngineConfiguration configuration, final String ownerTemplate,
                                                  final String template,
                                                  final Map<String, Object> templateResolutionAttributes) {
        return AlwaysValidCacheEntryValidity.INSTANCE;
    }

}
