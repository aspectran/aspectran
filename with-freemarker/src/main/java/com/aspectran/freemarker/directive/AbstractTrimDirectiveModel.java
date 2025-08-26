/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may not use this file except in compliance with the License.
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
package com.aspectran.freemarker.directive;

import com.aspectran.utils.annotation.jsr305.NonNull;
import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for custom trim directives, implementing the FreeMarker {@link TemplateDirectiveModel}.
 * <p>This class provides the core execution logic for a trim directive. It renders the directive's
 * body into a string, passes it to a {@link Trimmer} instance provided by the subclass, and then
 * writes the processed result to the output. It also includes helper methods for parsing
 * parameters from the template.</p>
 *
 * @since 2016. 1. 29.
 */
public abstract class AbstractTrimDirectiveModel implements TemplateDirectiveModel {

    /**
     * Executes the trim directive.
     * This method renders the directive body, trims the result using a {@link Trimmer},
     * and writes the output to the environment's writer.
     * @param env the FreeMarker environment
     * @param params the parameters passed to the directive
     * @param loopVars the loop variables (not supported by this directive)
     * @param body the body of the directive
     * @throws TemplateException if an error occurs during template processing
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
            throws TemplateException, IOException {
        if (body == null) {
            return;
        }
        if (loopVars.length != 0) {
            throw new TemplateModelException("Trim directive doesn't allow loop variables");
        }

        StringWriter bodyWriter = new StringWriter();
        body.render(bodyWriter);
        String trimmed = getTrimmer(params).trim(bodyWriter.toString());

        Writer out = env.getOut();
        out.write(trimmed);
    }

    /**
     * Abstract method that subclasses must implement to provide a {@link Trimmer} instance.
     * The trimmer can be pre-configured or created dynamically based on the directive parameters.
     * @param params the parameters passed to the directive from the template
     * @return a configured {@link Trimmer} instance
     * @throws TemplateModelException if the parameters are invalid
     */
    @SuppressWarnings("rawtypes")
    protected abstract Trimmer getTrimmer(Map params) throws TemplateModelException;

    /**
     * Helper method to parse a string parameter from the directive's parameter map.
     * @param params the map of parameters from the template
     * @param paramName the name of the parameter to parse
     * @return the string value of the parameter, or {@code null} if not found
     */
    @SuppressWarnings("rawtypes")
    protected String parseStringParameter(@NonNull Map params, String paramName) {
        Object paramModel = params.get(paramName);
        if (paramModel == null) {
            return null;
        }
        if (!(paramModel instanceof SimpleScalar)) {
            throw new IllegalArgumentException(paramName + " must be string");
        }
        return ((SimpleScalar)paramModel).getAsString();
    }

    /**
     * Helper method to parse a sequence (array) of strings from the directive's parameter map.
     * @param params the map of parameters from the template
     * @param paramName the name of the parameter to parse
     * @return an array of strings, or {@code null} if the parameter is not found
     * @throws TemplateModelException if a parameter is invalid
     */
    @SuppressWarnings("rawtypes")
    protected String[] parseSequenceParameter(@NonNull Map params, String paramName) throws TemplateModelException {
        Object paramModel = params.get(paramName);
        if (paramModel == null) {
            return null;
        }
        if (!(paramModel instanceof SimpleSequence)) {
            throw new IllegalArgumentException(paramName + " must be sequence");
        }
        List<String> list = transformSimpleSequenceAsStringList((SimpleSequence)paramModel, paramName);
        return list.toArray(new String[0]);
    }

    /**
     * Transforms a FreeMarker {@link SimpleSequence} into a Java {@link List} of strings.
     * @param sequence the sequence to transform
     * @param paramName the name of the parameter, for use in error messages
     * @return a list of strings
     * @throws TemplateModelException if an item in the sequence is not a string
     */
    @NonNull
    private List<String> transformSimpleSequenceAsStringList(@NonNull SimpleSequence sequence, String paramName)
            throws TemplateModelException {
        List<String> list = new ArrayList<>();
        int size = sequence.size();
        for (int i = 0; i < size; i++) {
            TemplateModel model = sequence.get(i);
            if (!(model instanceof SimpleScalar)) {
                throw new IllegalArgumentException(paramName + "'s item must be string");
            }
            list.add(((SimpleScalar)model).getAsString());
        }
        return list;
    }

}
