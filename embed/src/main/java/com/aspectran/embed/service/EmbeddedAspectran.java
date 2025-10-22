/*
 * Copyright (c) 2008-present The Aspectran Project
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
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.CoreServiceException;
import com.aspectran.core.support.i18n.message.NoSuchMessageException;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.apon.AponParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Reader;
import java.util.Locale;
import java.util.Map;

/**
 * Defines the primary interface for embedding the Aspectran framework within a Java application.
 * <p>This interface provides a simplified, high-level API for interacting with an
 * embedded Aspectran instance. It allows for programmatic execution of translets,
 * template rendering, and access to Aspectran's managed beans and internationalized
 * messages. It serves as a facade over the more complex
 * {@link com.aspectran.core.service.CoreService} layer, making it easier to integrate
 * Aspectran's features into any Java environment.
 *
 * <p>To get started, use the static {@link #run(String)} methods to create and
 * initialize an {@code EmbeddedAspectran} instance from a configuration file.
 * When the application is shutting down, call the {@link #destroy()} method to
 * release all resources gracefully.
 *
 * @since 3.0.0
 */
public interface EmbeddedAspectran {

    /**
     * Executes an instant action within the Aspectran context.
     * <p>This method allows for executing a piece of code that can access Aspectran's
     * managed components (beans, environment, etc.) without needing to define a translet.
     * @param instantAction the instant action to execute
     * @param <V> the return type of the instant action
     * @return the result of performing the instant action
     * @since 6.5.1
     */
    <V> V execute(InstantAction<V> instantAction);

    /**
     * Executes a translet with the specified name.
     * <p>The result of the execution, including the response content, can be
     * retrieved from the returned {@link Translet} object.
     * @param name the name of the translet to execute
     * @return a {@link Translet} object containing the result of the execution
     */
    Translet translate(String name);

    /**
     * Executes a translet with the specified name and request body.
     * @param name the name of the translet to execute
     * @param body the request body content
     * @return a {@link Translet} object containing the result of the execution
     */
    Translet translate(String name, String body);

    /**
     * Executes a translet with the specified name and attributes.
     * @param name the name of the translet to execute
     * @param attributeMap a map of attributes to be passed to the activity
     * @return a {@link Translet} object containing the result of the execution
     */
    Translet translate(String name, Map<String, Object> attributeMap);

    /**
     * Executes a translet with the specified name and parameters.
     * @param name the name of the translet to execute
     * @param parameterMap a map of parameters to be passed to the activity
     * @return a {@link Translet} object containing the result of the execution
     */
    Translet translate(String name, ParameterMap parameterMap);

    /**
     * Executes a translet with the specified name, attributes, and parameters.
     * <p>Attributes are server-side objects that can be passed between actions,
     * while parameters are request-style parameters, typically from a client.
     * The result of the execution can be retrieved from the returned {@link Translet} object.
     * @param name the name of the translet to execute
     * @param attributeMap a map of attributes to be passed to the activity
     * @param parameterMap a map of parameters to be passed to the activity
     * @return a {@link Translet} object containing the result of the execution
     */
    Translet translate(String name, Map<String, Object> attributeMap, ParameterMap parameterMap);

    /**
     * Executes a translet with the specified name and request method.
     * @param name the name of the translet to execute
     * @param method the request method (e.g., GET, POST)
     * @return a {@link Translet} object containing the result of the execution
     */
    Translet translate(String name, MethodType method);

    /**
     * Executes a translet with the specified name, request method, and attributes.
     * @param name the name of the translet to execute
     * @param method the request method (e.g., GET, POST)
     * @param attributeMap a map of attributes to be passed to the activity
     * @return a {@link Translet} object containing the result of the execution
     */
    Translet translate(String name, MethodType method, Map<String, Object> attributeMap);

    /**
     * Executes a translet with the specified name, request method, and parameters.
     * @param name the name of the translet to execute
     * @param method the request method (e.g., GET, POST)
     * @param parameterMap a map of parameters to be passed to the activity
     * @return a {@link Translet} object containing the result of the execution
     */
    Translet translate(String name, MethodType method, ParameterMap parameterMap);

    /**
     * Executes a translet with the specified name, request method, attributes, and parameters.
     * @param name the name of the translet to execute
     * @param method the request method (e.g., GET, POST)
     * @param attributeMap a map of attributes to be passed to the activity
     * @param parameterMap a map of parameters to be passed to the activity
     * @return a {@link Translet} object containing the result of the execution
     */
    Translet translate(String name, MethodType method, Map<String, Object> attributeMap, ParameterMap parameterMap);

    /**
     * Executes a translet with the specified name, request method, attributes, parameters, and request body.
     * @param name the name of the translet to execute
     * @param method the request method (e.g., GET, POST)
     * @param attributeMap a map of attributes to be passed to the activity
     * @param parameterMap a map of parameters to be passed to the activity
     * @param body the request body content
     * @return a {@link Translet} object containing the result of the execution
     */
    Translet translate(String name, MethodType method, Map<String, Object> attributeMap, ParameterMap parameterMap, String body);

    /**
     * Renders a template with the given ID.
     * @param templateId the ID of the template to render
     * @return the rendered content as a string
     */
    String render(String templateId);

    /**
     * Renders a template with the given ID and attributes.
     * @param templateId the ID of the template to render
     * @param attributeMap a map of attributes to be passed to the template renderer
     * @return the rendered content as a string
     */
    String render(String templateId, Map<String, Object> attributeMap);

    /**
     * Renders a template with the given ID and parameters.
     * @param templateId the ID of the template to render
     * @param parameterMap a map of parameters to be passed to the template renderer
     * @return the rendered content as a string
     */
    String render(String templateId, ParameterMap parameterMap);

    /**
     * Renders a template with the given ID, attributes, and parameters.
     * @param templateId the ID of the template to render
     * @param attributeMap a map of attributes to be passed to the template renderer
     * @param parameterMap a map of parameters to be passed to the template renderer
     * @return the rendered content as a string
     */
    String render(String templateId, Map<String, Object> attributeMap, ParameterMap parameterMap);

    /**
     * Retrieves the current environment for this application.
     * @return the environment instance
     */
    Environment getEnvironment();

    /**
     * Returns the application adapter.
     * @return the application adapter
     */
    ApplicationAdapter getApplicationAdapter();

    /**
     * Returns the bean instance that uniquely matches the given ID.
     * @param <V> the type of the bean
     * @param id the ID of the bean to retrieve
     * @return an instance of the bean
     */
    <V> V getBean(String id);

    /**
     * Returns the bean instance that uniquely matches the given object type.
     * @param <V> the type of the bean
     * @param type the type the bean must match; can be an interface or superclass
     * @return an instance of the bean
     * @since 1.3.1
     */
    <V> V getBean(Class<V> type);

    /**
     * Returns the bean instance that uniquely matches the given object type and ID.
     * @param <V> the type of the bean
     * @param type the type the bean must match; can be an interface or superclass
     * @param id the ID of the bean to retrieve
     * @return an instance of the bean
     * @since 2.0.0
     */
    <V> V getBean(Class<V> type, String id);

    /**
     * Returns whether a bean with the specified ID is present.
     * @param id the ID of the bean to query
     * @return {@code true} if a bean with the specified ID is present, {@code false} otherwise
     */
    boolean containsBean(String id);

    /**
     * Returns whether a bean with the specified object type is present.
     * @param type the object type of the bean to query
     * @return {@code true} if a bean with the specified type is present, {@code false} otherwise
     */
    boolean containsBean(Class<?> type);

    /**
     * Returns whether a bean with the specified object type and ID is present.
     * @param type the object type of the bean to query
     * @param id the ID of the bean to query
     * @return {@code true} if a bean with the specified type and ID is present, {@code false} otherwise
     */
    boolean containsBean(Class<?> type, String id);

    /**
     * Tries to resolve the message for the given code and locale.
     * @param code the code to lookup, such as 'calculator.noRateSet'
     * @param args arguments that will be filled in for parameters within the message (e.g., "{0}", "{1,date}")
     * @param locale the {@link Locale} in which to do the lookup
     * @return the resolved message
     * @throws NoSuchMessageException if the message is not found
     * @see java.text.MessageFormat
     */
    String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException;

    /**
     * Tries to resolve the message for the given code and locale. Returns a default message if not found.
     * @param code the code to lookup, such as 'calculator.noRateSet'
     * @param args arguments that will be filled in for parameters within the message
     * @param defaultMessage the default message to return if the lookup fails
     * @param locale the {@link Locale} in which to do the lookup
     * @return the resolved message if successful, otherwise the default message
     * @see java.text.MessageFormat
     */
    String getMessage(String code, Object[] args, String defaultMessage, Locale locale);

    /**
     * Creates and returns a new session adapter from the embedded Aspectran instance.
     * @return the session adapter
     */
    SessionAdapter newSessionAdapter();

    /**
     * Stops the service and releases all associated resources.
     * This method is the final step in the lifecycle of an embedded Aspectran instance
     * and should be called to ensure a graceful shutdown.
     */
    void destroy();

    /**
     * Creates and starts a new {@code DefaultEmbeddedAspectran} instance from a configuration file path.
     * @param aspectranConfigFile the path to the Aspectran configuration file
     * @return the instance of {@code EmbeddedAspectran}
     */
    @NonNull
    static EmbeddedAspectran run(String aspectranConfigFile) {
        if (aspectranConfigFile == null) {
            throw new IllegalArgumentException("aspectranConfigFile must not be null");
        }
        File configFile = new File(aspectranConfigFile);
        return run(configFile);
    }

    /**
     * Creates and starts a new {@code DefaultEmbeddedAspectran} instance from a configuration {@link File}.
     * @param aspectranConfigFile the Aspectran configuration file
     * @return the instance of {@code EmbeddedAspectran}
     */
    @NonNull
    static EmbeddedAspectran run(File aspectranConfigFile) {
        if (aspectranConfigFile == null) {
            throw new IllegalArgumentException("aspectranConfigFile must not be null");
        }
        AspectranConfig aspectranConfig;
        try {
            aspectranConfig = new AspectranConfig(aspectranConfigFile);
        } catch (AponParseException e) {
            throw new CoreServiceException("Error parsing aspectran configuration file: " +
                    aspectranConfigFile, e);
        }
        return run(aspectranConfig);
    }

    /**
     * Creates and starts a new {@code DefaultEmbeddedAspectran} instance from a configuration {@link Reader}.
     * @param configFileReader the Aspectran configuration file reader
     * @return the instance of {@code EmbeddedAspectran}
     */
    @NonNull
    static EmbeddedAspectran run(Reader configFileReader) {
        if (configFileReader == null) {
            throw new IllegalArgumentException("configFileReader must not be null");
        }
        AspectranConfig aspectranConfig;
        try {
            aspectranConfig = new AspectranConfig(configFileReader);
        } catch (AponParseException e) {
            throw new CoreServiceException("Error parsing aspectran configuration", e);
        }
        return run(aspectranConfig);
    }

    /**
     * Creates and starts a new {@code DefaultEmbeddedAspectran} instance from an {@link AspectranConfig} object.
     * @param aspectranConfig the Aspectran configuration
     * @return the instance of {@code EmbeddedAspectran}
     */
    @NonNull
    static EmbeddedAspectran run(AspectranConfig aspectranConfig) {
        Assert.notNull(aspectranConfig, "aspectranConfig must not be null");
        try {
            DefaultEmbeddedAspectran aspectran = DefaultEmbeddedAspectran.create(aspectranConfig);
            aspectran.start();
            return aspectran;
        } catch (CoreServiceException e) {
            throw e;
        } catch (Exception e) {
            String message = "EmbeddedAspectran run failed with parameters:" + System.lineSeparator() + aspectranConfig;
            Logger logger = LoggerFactory.getLogger(EmbeddedAspectran.class);
            logger.error(message);
            throw new CoreServiceException("EmbeddedAspectran run failed", e);
        }
    }

}
