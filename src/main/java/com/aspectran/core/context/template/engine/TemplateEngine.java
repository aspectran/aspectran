package com.aspectran.core.context.template.engine;

import com.aspectran.core.context.rule.TemplateRule;

import java.io.Reader;
import java.io.Writer;
import java.util.Map;

/**
 * Created by gulendol on 2016. 1. 9..
 */
public interface TemplateEngine {

    public String process(TemplateRule templateRule, Reader reader, Map<String, String> dataModel, Writer writer);

}
