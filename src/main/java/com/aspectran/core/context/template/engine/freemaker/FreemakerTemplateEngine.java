package com.aspectran.core.context.template.engine.freemaker;

import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.template.engine.TemplateEngine;
import freemarker.template.Configuration;

import java.io.Reader;
import java.io.Writer;
import java.util.Map;

/**
 * Created by gulendol on 2016. 1. 9..
 */
public class FreemakerTemplateEngine implements TemplateEngine {

    private Configuration configuration;

    public FreemakerTemplateEngine(Configuration configuration) {
        this.configuration = configuration;
    }

    public void process(TemplateRule templateRule, Reader reader, Map<String, String> dataModel, Writer writer) {

    }

}
