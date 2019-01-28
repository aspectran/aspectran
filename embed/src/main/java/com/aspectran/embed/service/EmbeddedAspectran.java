/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.embed.service;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.CoreService;

import java.io.File;
import java.io.Reader;
import java.util.Map;

/**
 * The Interface EmbeddedAspectran.
 *
 * <p>Created: 2017. 10. 28.</p>
 */
public interface EmbeddedAspectran extends CoreService {

    /**
     * Create and return a new session adapter from the embedded aspectran.
     *
     * @return the session adapter
     */
    SessionAdapter newSessionAdapter();

    /**
     * Execute the translet.
     *
     * @param name the translet name
     * @return the {@code Translet} object
     */
    Translet translate(String name);

    /**
     * Execute the translet.
     *
     * @param name the translet name
     * @param parameterMap the parameter map
     * @return the {@code Translet} object
     */
    Translet translate(String name, ParameterMap parameterMap);

    /**
     * Execute the translet.
     *
     * @param name the translet name
     * @param parameterMap the parameter map
     * @param attributeMap the attribute map
     * @return the {@code Translet} object
     */
    Translet translate(String name, ParameterMap parameterMap, Map<String, Object> attributeMap);

    /**
     * Execute the translet.
     *
     * @param name the translet name
     * @param attributeMap the attribute map
     * @return the {@code Translet} object
     */
    Translet translate(String name, Map<String, Object> attributeMap);

    /**
     * Execute the translet.
     *
     * @param name the translet name
     * @param method the request method
     * @return the {@code Translet} object
     */
    Translet translate(String name, MethodType method);

    /**
     * Execute the translet.
     *
     * @param name the translet name
     * @param method the request method
     * @param parameterMap the parameter map
     * @return the {@code Translet} object
     */
    Translet translate(String name, MethodType method, ParameterMap parameterMap);

    /**
     * Execute the translet.
     *
     * @param name the translet name
     * @param method the request method
     * @param attributeMap the attribute map
     * @return the {@code Translet} object
     */
    Translet translate(String name, MethodType method, Map<String, Object> attributeMap);

    /**
     * Execute the translet.
     *
     * @param name the translet name
     * @param method the request method
     * @param parameterMap the parameter map
     * @param attributeMap the attribute map
     * @return the {@code Translet} object
     */
    Translet translate(String name, MethodType method, ParameterMap parameterMap, Map<String, Object> attributeMap);

    /**
     * Evaluate the template without any provided variables.
     *
     * @param templateId the template id
     * @return the output string of the template
     */
    String template(String templateId);

    /**
     * Evaluate the template with a set of parameters.
     *
     * @param templateId the template id
     * @param parameterMap the parameter map
     * @return the output string of the template
     */
    String template(String templateId, ParameterMap parameterMap);

    /**
     * Evaluate the template with a set of parameters.
     *
     * @param templateId the template id
     * @param attributeMap the attribute map
     * @return the output string of the template
     */
    String template(String templateId, Map<String, Object> attributeMap);

    /**
     * Evaluate the template with a set of parameters and a set of attributes.
     *
     * @param templateId the template id
     * @param parameterMap the parameter map
     * @param attributeMap the attribute map
     * @return the output string of the template
     */
    String template(String templateId, ParameterMap parameterMap, Map<String, Object> attributeMap);

    /**
     * Stop the service and release all allocated resources.
     */
    void release();

    /**
     * Creates and starts a new {@code DefaultEmbeddedAspectran}.
     *
     * @param aspectranConfigFile the aspectran configuration file
     * @return the instance of {@code EmbeddedAspectran}
     */
    static EmbeddedAspectran run(String aspectranConfigFile) {
        File configFile = new File(aspectranConfigFile);
        return run(configFile);
    }

    /**
     * Creates and starts a new {@code DefaultEmbeddedAspectran}.
     *
     * @param aspectranConfigFile the aspectran configuration file
     * @return the instance of {@code EmbeddedAspectran}
     */
    static EmbeddedAspectran run(File aspectranConfigFile) {
        AspectranConfig aspectranConfig = new AspectranConfig(aspectranConfigFile);
        return run(aspectranConfig);
    }

    /**
     * Creates and starts a new {@code DefaultEmbeddedAspectran}.
     *
     * @param configFileReader the aspectran configuration file reader
     * @return the instance of {@code EmbeddedAspectran}
     */
    static EmbeddedAspectran run(Reader configFileReader) {
        AspectranConfig aspectranConfig = new AspectranConfig(configFileReader);
        return run(aspectranConfig);
    }

    /**
     * Creates and starts a new {@code DefaultEmbeddedAspectran}.
     *
     * @param aspectranConfig the parameters for aspectran configuration
     * @return the instance of {@code EmbeddedAspectran}
     */
    static EmbeddedAspectran run(AspectranConfig aspectranConfig) {
        if (aspectranConfig == null) {
            throw new IllegalArgumentException("aspectranConfig must not be null");
        }
        try {
            DefaultEmbeddedAspectran aspectran = DefaultEmbeddedAspectran.create(aspectranConfig);
            aspectran.start();
            return aspectran;
        } catch (AspectranServiceException e) {
            throw e;
        } catch (Exception e) {
            String appConfigRootFile = aspectranConfig.getAppConfigRootFile();
            throw new AspectranServiceException("EmbeddedAspectran run failed with " +
                    (appConfigRootFile != null ? appConfigRootFile : aspectranConfig), e);
        }
    }

}
