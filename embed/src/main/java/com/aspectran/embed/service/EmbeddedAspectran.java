/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.core.activity.InstantAction;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.support.i18n.message.NoSuchMessageException;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Locale;
import java.util.Map;

/**
 * The Interface EmbeddedAspectran.
 *
 * <p>Created: 2017. 10. 28.</p>
 */
public interface EmbeddedAspectran {

    /**
     * Returns whether the translet can be exposed to the daemon service.
     * @param transletName the name of the translet to check
     * @return true if the translet can be exposed; false otherwise
     */
    boolean isExposable(String transletName);

    /**
     * Create and return a new session adapter from the embedded aspectran.
     * @return the session adapter
     */
    SessionAdapter newSessionAdapter();

    /**
     * Executes an instant activity.
     * @param instantAction the instant action
     * @return An object that is the result of performing an instant activity
     * @since 6.5.1
     */
    <V> V execute(InstantAction<V> instantAction);

    /**
     * Executes the translet.
     * @param name the translet name
     * @return the {@code Translet} object
     */
    Translet translate(String name);

    /**
     * Executes the translet.
     * @param name the translet name
     * @param body the request body
     * @return the {@code Translet} object
     */
    Translet translate(String name, String body);

    /**
     * Executes the translet with the given parameters.
     * @param name the translet name
     * @param attributeMap the attribute map
     * @return the {@code Translet} object
     */
    Translet translate(String name, Map<String, Object> attributeMap);

    /**
     * Executes the translet with the given parameters.
     * @param name the translet name
     * @param parameterMap the parameter map
     * @return the {@code Translet} object
     */
    Translet translate(String name, ParameterMap parameterMap);

    /**
     * Executes the translet with the given attributes and parameters.
     * @param name the translet name
     * @param attributeMap the attribute map
     * @param parameterMap the parameter map
     * @return the {@code Translet} object
     */
    Translet translate(String name, Map<String, Object> attributeMap, ParameterMap parameterMap);

    /**
     * Executes the translet without the supplied variables.
     * @param name the translet name
     * @param method the request method
     * @return the {@code Translet} object
     */
    Translet translate(String name, MethodType method);

    /**
     * Executes the translet with the given attributes.
     * @param name the translet name
     * @param method the request method
     * @param attributeMap the attribute map
     * @return the {@code Translet} object
     */
    Translet translate(String name, MethodType method, Map<String, Object> attributeMap);

    /**
     * Executes the translet with the given parameters.
     * @param name the translet name
     * @param method the request method
     * @param parameterMap the parameter map
     * @return the {@code Translet} object
     */
    Translet translate(String name, MethodType method, ParameterMap parameterMap);

    /**
     * Executes the translet with the given attributes and parameters.
     * @param name the translet name
     * @param method the request method
     * @param attributeMap the attribute map
     * @param parameterMap the parameter map
     * @return the {@code Translet} object
     */
    Translet translate(String name, MethodType method, Map<String, Object> attributeMap, ParameterMap parameterMap);

    /**
     * Executes the translet with the given attributes and parameters.
     * @param name the translet name
     * @param method the request method
     * @param attributeMap the attribute map
     * @param parameterMap the parameter map
     * @param body the request body
     * @return the {@code Translet} object
     */
    Translet translate(String name, MethodType method, Map<String, Object> attributeMap, ParameterMap parameterMap, String body);

    /**
     * Renders the template without the supplied variables.
     * @param templateId the template id
     * @return the output string of the template
     */
    String render(String templateId);

    /**
     * Renders the template with the given attributes.
     * @param templateId the template id
     * @param attributeMap the attribute map
     * @return the output string of the template
     */
    String render(String templateId, Map<String, Object> attributeMap);

    /**
     * Renders the template with the given parameters.
     * @param templateId the template id
     * @param parameterMap the parameter map
     * @return the output string of the template
     */
    String render(String templateId, ParameterMap parameterMap);

    /**
     * Renders the template with the given attributes and parameters.
     * @param templateId the template id
     * @param attributeMap the attribute map
     * @param parameterMap the parameter map
     * @return the output string of the template
     */
    String render(String templateId, Map<String, Object> attributeMap, ParameterMap parameterMap);

    /**
     * Gets the environment.
     * @return the environment
     */
    Environment getEnvironment();

    /**
     * Return an instance of the bean that matches the given id.
     * @param <V> the type of bean object retrieved
     * @param id the id of the bean to retrieve
     * @return an instance of the bean
     */
    <V> V getBean(String id);

    /**
     * Return an instance of the bean that matches the given object type.
     * @param <V> the type of bean object retrieved
     * @param type the type the bean must match; can be an interface or superclass.
     *      {@code null} is disallowed.
     * @return an instance of the bean
     * @since 1.3.1
     */
    <V> V getBean(Class<V> type);

    /**
     * Return an instance of the bean that matches the given object type.
     * @param <V> the type of bean object retrieved
     * @param type type the bean must match; can be an interface or superclass.
     *      {@code null} is allowed.
     * @param id the id of the bean to retrieve
     * @return an instance of the bean
     * @since 2.0.0
     */
    <V> V getBean(Class<V> type, String id);

    /**
     * Return whether a bean with the specified id is present.
     * @param id the id of the bean to query
     * @return whether a bean with the specified id is present
     */
    boolean containsBean(String id);

    /**
     * Return whether a bean with the specified object type is present.
     * @param type the object type of the bean to query
     * @return whether a bean with the specified type is present
     */
    boolean containsBean(Class<?> type);

    /**
     * Returns whether the bean corresponding to the specified object type and ID exists.
     * @param type the object type of the bean to query
     * @param id the id of the bean to query
     * @return whether a bean with the specified type is present
     */
    boolean containsBean(Class<?> type, String id);

    //---------------------------------------------------------------------
    // Implementation of MessageSource interface
    //---------------------------------------------------------------------

    /**
     * Try to resolve the message. Treat as an error if the message can't be found.
     * @param code the code to lookup up, such as 'calculator.noRateSet'
     * @param args Array of arguments that will be filled in for params within
     *         the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
     *         or {@code null} if none.
     * @param locale the Locale in which to do the lookup
     * @return the resolved message
     * @throws NoSuchMessageException if the message wasn't found
     * @see java.text.MessageFormat
     */
    String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException;

    /**
     * Try to resolve the message. Return default message if no message was found.
     * @param code the code to lookup up, such as 'calculator.noRateSet'. Users of
     *         this class are encouraged to base message names on the relevant fully
     *         qualified class name, thus avoiding conflict and ensuring maximum clarity.
     * @param args array of arguments that will be filled in for params within
     *         the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
     *         or {@code null} if none.
     * @param defaultMessage String to return if the lookup fails
     * @param locale the Locale in which to do the lookup
     * @return the resolved message if the lookup was successful;
     *         otherwise the default message passed as a parameter
     * @see java.text.MessageFormat
     */
    String getMessage(String code, Object[] args, String defaultMessage, Locale locale);

    /**
     * Stop the service and release all allocated resources.
     */
    void release();

    /**
     * Creates and starts a new {@code DefaultEmbeddedAspectran}.
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
            throw new AspectranServiceException("Error parsing aspectran configuration file: " +
                    aspectranConfigFile, e);
        }
        return run(aspectranConfig);
    }

    /**
     * Creates and starts a new {@code DefaultEmbeddedAspectran}.
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
            String message = "EmbeddedAspectran run failed with parameters:" + System.lineSeparator() + aspectranConfig;
            Logger logger = LoggerFactory.getLogger(EmbeddedAspectran.class);
            logger.error(message);
            throw new AspectranServiceException("EmbeddedAspectran run failed", e);
        }
    }

}
