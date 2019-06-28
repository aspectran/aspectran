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
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.io.File;
import java.io.IOException;
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
     * Executes the translet.
     *
     * @param name the translet name
     * @return the {@code Translet} object
     */
    Translet translate(String name);

    Translet translate(String name, String body);

    /**
     * Executes the translet with the given parameters.
     *
     * @param name the translet name
     * @param parameterMap the parameter map
     * @return the {@code Translet} object
     */
    Translet translate(String name, ParameterMap parameterMap);

    /**
     * Executes the translet with the given parameters and attributes.
     *
     * @param name the translet name
     * @param parameterMap the parameter map
     * @param attributeMap the attribute map
     * @return the {@code Translet} object
     */
    Translet translate(String name, ParameterMap parameterMap, Map<String, Object> attributeMap);

    /**
     * Executes the translet with the given parameters.
     *
     * @param name the translet name
     * @param attributeMap the attribute map
     * @return the {@code Translet} object
     */
    Translet translate(String name, Map<String, Object> attributeMap);

    /**
     * Executes the translet without the supplied variables.
     *
     * @param name the translet name
     * @param method the request method
     * @return the {@code Translet} object
     */
    Translet translate(String name, MethodType method);

    /**
     * Executes the translet with the given parameters.
     *
     * @param name the translet name
     * @param method the request method
     * @param parameterMap the parameter map
     * @return the {@code Translet} object
     */
    Translet translate(String name, MethodType method, ParameterMap parameterMap);

    /**
     * Executes the translet with the given attributes.
     *
     * @param name the translet name
     * @param method the request method
     * @param attributeMap the attribute map
     * @return the {@code Translet} object
     */
    Translet translate(String name, MethodType method, Map<String, Object> attributeMap);

    /**
     * Executes the translet with the given parameters and attributes.
     *
     * @param name the translet name
     * @param method the request method
     * @param parameterMap the parameter map
     * @param attributeMap the attribute map
     * @param body the request body
     * @return the {@code Translet} object
     */
    Translet translate(String name, MethodType method, ParameterMap parameterMap, Map<String, Object> attributeMap, String body);

    /**
     * Renders the template without the supplied variables.
     *
     * @param templateId the template id
     * @return the output string of the template
     */
    String render(String templateId);

    /**
     * Renders the template with the given parameters.
     *
     * @param templateId the template id
     * @param parameterMap the parameter map
     * @return the output string of the template
     */
    String render(String templateId, ParameterMap parameterMap);

    /**
     * Renders the template with the given attributes.
     *
     * @param templateId the template id
     * @param attributeMap the attribute map
     * @return the output string of the template
     */
    String render(String templateId, Map<String, Object> attributeMap);

    /**
     * Renders the template with the given parameters and attributes.
     *
     * @param templateId the template id
     * @param parameterMap the parameter map
     * @param attributeMap the attribute map
     * @return the output string of the template
     */
    String render(String templateId, ParameterMap parameterMap, Map<String, Object> attributeMap);

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
        if (aspectranConfigFile == null) {
            throw new IllegalArgumentException("aspectranConfigFile must not be null");
        }
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
        if (aspectranConfigFile == null) {
            throw new IllegalArgumentException("aspectranConfigFile must not be null");
        }
        AspectranConfig aspectranConfig;
        try {
            aspectranConfig = new AspectranConfig(aspectranConfigFile);
        } catch (IOException e) {
            throw new AspectranServiceException("Error parsing aspectran configuration file: " + aspectranConfigFile, e);
        }
        return run(aspectranConfig);
    }

    /**
     * Creates and starts a new {@code DefaultEmbeddedAspectran}.
     *
     * @param configFileReader the aspectran configuration file reader
     * @return the instance of {@code EmbeddedAspectran}
     */
    static EmbeddedAspectran run(Reader configFileReader) {
        if (configFileReader == null) {
            throw new IllegalArgumentException("aspectranConfigFile must not be null");
        }
        AspectranConfig aspectranConfig;
        try {
            aspectranConfig = new AspectranConfig(configFileReader);
        } catch (IOException e) {
            throw new AspectranServiceException("Error parsing aspectran configuration", e);
        }
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
            String message = "EmbeddedAspectran run failed with parameters:" + System.lineSeparator() +
                    aspectranConfig.toString();
            Log log = LogFactory.getLog(EmbeddedAspectran.class);
            log.error(message);
            throw new AspectranServiceException("EmbeddedAspectran run failed", e);
        }
    }

}
