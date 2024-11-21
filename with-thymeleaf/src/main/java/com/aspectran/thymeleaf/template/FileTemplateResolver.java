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
            IEngineConfiguration configuration,
            String ownerTemplate,
            String template,
            String resourceName,
            String characterEncoding,
            Map<String, Object> templateResolutionAttributes) {
        return new FileTemplateResource(resourceName, characterEncoding);
    }

}
