package com.aspectran.velocity;

import com.aspectran.core.component.template.engine.TemplateEngine;
import com.aspectran.core.component.template.engine.TemplateEngineProcessException;

import java.io.Writer;
import java.util.Locale;
import java.util.Map;

/**
 * @since 6.3.0
 */
public class VelocityTemplateEngine implements TemplateEngine {

    @Override
    public void process(String templateName, Map<String, Object> model, String templateSource, Writer writer) throws TemplateEngineProcessException {

    }

    @Override
    public void process(String templateName, Map<String, Object> model, Writer writer) throws TemplateEngineProcessException {

    }

    @Override
    public void process(String templateName, Map<String, Object> model, Writer writer, Locale locale) throws TemplateEngineProcessException {

    }

}
