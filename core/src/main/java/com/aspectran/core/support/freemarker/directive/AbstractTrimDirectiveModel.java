/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.support.freemarker.directive;

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
 * The Class AbstractTrimDirectiveModel.
 *
 * <p>Created: 2016. 1. 29.</p>
 */
public abstract class AbstractTrimDirectiveModel implements TemplateDirectiveModel {

    @SuppressWarnings("rawtypes")
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
     * Gets trimmer.
     *
     * @param params the params
     * @return the trimmer
     * @throws TemplateModelException the template model exception
     */
    @SuppressWarnings("rawtypes")
    abstract protected Trimmer getTrimmer(Map params) throws TemplateModelException;

    /**
     * Parse string parameter.
     *
     * @param params the params
     * @param paramName the param name
     * @return the string
     */
    @SuppressWarnings("rawtypes")
    protected String parseStringParameter(Map params, String paramName) {
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
     * Parse sequence parameter.
     *
     * @param params the params
     * @param paramName the param name
     * @return the string [ ]
     * @throws TemplateModelException the template model exception
     */
    @SuppressWarnings("rawtypes")
    protected String[] parseSequenceParameter(Map params, String paramName) throws TemplateModelException {
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
     * Transform simple sequence as string list.
     *
     * @param sequence the sequence
     * @param paramName the param name
     * @return the list
     * @throws TemplateModelException the template model exception
     */
    private List<String> transformSimpleSequenceAsStringList(SimpleSequence sequence, String paramName)
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
