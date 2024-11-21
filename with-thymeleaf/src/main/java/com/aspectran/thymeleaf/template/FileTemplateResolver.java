package com.aspectran.thymeleaf.template;

import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresource.FileTemplateResource;
import org.thymeleaf.templateresource.ITemplateResource;

import java.util.Map;

/**
 * Implementation of {@link ITemplateResolver} that extends {@link AbstractConfigurableTemplateResolver}
 * and creates {@link FileTemplateResource} instances for template resources.
 */
public class FileTemplateResolver extends AbstractConfigurableTemplateResolver {

    public FileTemplateResolver() {
        super();
    }

    @Override
    protected ITemplateResource computeTemplateResource(
        final IEngineConfiguration configuration, final String ownerTemplate, final String template,
        final String resourceName, final String characterEncoding,
        final Map<String, Object> templateResolutionAttributes) {
        return new FileTemplateResource(resourceName, characterEncoding);
    }

}
