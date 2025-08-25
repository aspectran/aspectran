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
package com.aspectran.core.activity;

import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.request.FileParameter;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.transform.CustomTransformer;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.context.rule.DispatchRule;
import com.aspectran.core.context.rule.ForwardRule;
import com.aspectran.core.context.rule.RedirectRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.support.i18n.message.NoSuchMessageException;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/**
 * Represents the central context for a single request-response cycle (a transaction).
 * The Translet serves as the primary API for actions and other components to interact
 * with the current request's state and to control the execution flow.
 *
 * <p>This interface is a stateful, request-scoped container that aggregates all
 * data related to a request, such as parameters, attributes, and action results.
 * It also provides methods to command the framework to perform flow-control actions
 * like redirect, forward, or dispatch.</p>
 *
 * @since 2008. 07. 05.
 */
public interface Translet {

    /**
     * Returns the mode of the current activity (e.g., "web", "shell").
     * @return the current activity mode
     */
    Activity.Mode getMode();

    /**
     * Returns the context path of the application.
     * @return the context path, or an empty string for the root context
     */
    String getContextPath();

    /**
     * Returns the name of the request, relative to the application's context path.
     * @return the request name (e.g., "/users/123")
     */
    String getRequestName();

    /**
     * Returns the full, actual request name as perceived by the client,
     * including the context path if applicable.
     * @return the actual request name (e.g., "/myapp/users/123")
     */
    String getActualRequestName();

    /**
     * Returns the request method for the current request.
     * This is an abstract representation that maps to HTTP methods in a web environment.
     * @return the request method
     */
    MethodType getRequestMethod();

    /**
     * Returns the name of the translet rule that is currently being executed.
     * @return the name of the current translet rule
     */
    String getTransletName();

    /**
     * Returns a description of this translet, if provided in the rule definition.
     * @return a description, or {@code null} if not specified
     */
    String getDescription();

    /**
     * Returns the environment for the current activity context.
     * @return the current environment
     */
    Environment getEnvironment();

    /**
     * Returns the application adapter, which provides access to application-level resources.
     * @return the application adapter
     */
    ApplicationAdapter getApplicationAdapter();

    /**
     * Checks if a session adapter is available for this translet.
     * @return {@code true} if a session is available, {@code false} otherwise
     */
    boolean hasSessionAdapter();

    /**
     * Returns the session adapter for interacting with the user session.
     * @return the session adapter
     */
    SessionAdapter getSessionAdapter();

    /**
     * Returns the request adapter for accessing request-specific data.
     * @return the request adapter
     */
    RequestAdapter getRequestAdapter();

    /**
     * Returns the response adapter for manipulating the response.
     * @return the response adapter
     */
    ResponseAdapter getResponseAdapter();

    /**
     * Returns the underlying, native session object from the environment (e.g., HttpSession).
     * @param <V> the type of the session adaptee
     * @return the native session object
     */
    <V> V getSessionAdaptee();

    /**
     * Returns the underlying, native request object from the environment (e.g., HttpServletRequest).
     * @param <V> the type of the request adaptee
     * @return the native request object
     */
    <V> V getRequestAdaptee();

    /**
     * Returns the underlying, native response object from the environment (e.g., HttpServletResponse).
     * @param <V> the type of the response adaptee
     * @return the native response object
     */
    <V> V getResponseAdaptee();

    /**
     * Returns the character encoding used for the request.
     * @return the request character encoding
     */
    String getDefinitiveRequestEncoding();

    /**
     * Returns the character encoding used for the response.
     * @return the response character encoding
     */
    String getDefinitiveResponseEncoding();

    /**
     * Returns the container for all action results produced during processing.
     * @return the process result container
     */
    ProcessResult getProcessResult();

    /**
     * Returns the result of a specific action by its ID.
     * @param actionId the ID of the action
     * @return the result of the specified action, or {@code null} if not found
     */
    Object getProcessResult(String actionId);

    /**
     * Sets the process result container, replacing any existing results.
     * @param processResult the new process result container
     */
    void setProcessResult(ProcessResult processResult);

    /**
     * Returns the container for all data related to the current activity.
     * @return the activity data container
     */
    ActivityData getActivityData();

    /**
     * Gets a configuration setting value defined within the translet's scope.
     * @param <V> the type of the setting value
     * @param settingName the name of the setting
     * @return the setting value, or {@code null} if not found
     */
    <V> V getSetting(String settingName);

    /**
     * Returns the value of a property from the application's environment.
     * @param <V> the type of the property value
     * @param name the name of the property
     * @return the property value, or {@code null} if not found
     */
    <V> V getProperty(String name);

    /**
     * Returns the value of a request parameter as a {@code String}.
     * @param name the name of the parameter
     * @return the parameter value, or {@code null} if not found
     */
    String getParameter(String name);

    /**
     * Returns an array of {@code String} values for a request parameter.
     * @param name the name of the parameter
     * @return an array of values, or {@code null} if the parameter does not exist
     */
    String[] getParameterValues(String name);

    /**
     * Returns a collection of all parameter names for the current request.
     * @return a collection of parameter names
     */
    Collection<String> getParameterNames();

    /**
     * Sets a request parameter value, replacing any existing value.
     * @param name the name of the parameter
     * @param value the new value for the parameter
     */
    void setParameter(String name, String value);

    /**
     * Sets a request parameter with multiple values.
     * @param name the name of the parameter
     * @param values the array of values for the parameter
     */
    void setParameter(String name, String[] values);

    /**
     * Returns a mutable map of all request parameters.
     * @return a map of all parameters
     */
    Map<String, Object> getAllParameters();

    /**
     * Extracts all request parameters into a provided target map.
     * @param targetParameters the map to populate with parameters
     */
    void extractParameters(Map<String, Object> targetParameters);

    /**
     * Returns a {@link FileParameter} for a given file upload parameter name.
     * @param name the name of the file parameter
     * @return the {@code FileParameter} object, or {@code null} if not found
     */
    FileParameter getFileParameter(String name);

    /**
     * Returns an array of {@link FileParameter} objects for a file upload parameter.
     * @param name the name of the file parameter
     * @return an array of {@code FileParameter} objects, or {@code null} if not found
     */
    FileParameter[] getFileParameterValues(String name);

    /**
     * Returns a collection of all file parameter names for the current request.
     * @return a collection of file parameter names
     */
    Collection<String> getFileParameterNames();

    /**
     * Sets a file parameter value.
     * @param name the name of the file parameter
     * @param fileParameter the {@code FileParameter} object
     */
    void setFileParameter(String name, FileParameter fileParameter);

    /**
     * Sets a file parameter with multiple values.
     * @param name the name of the file parameter
     * @param fileParameters an array of {@code FileParameter} objects
     */
    void setFileParameter(String name, FileParameter[] fileParameters);

    /**
     * Removes a file parameter by its name.
     * @param name the name of the file parameter to remove
     */
    void removeFileParameter(String name);

    /**
     * Returns the value of a request-scoped attribute.
     * @param <V> the type of the attribute value
     * @param name the name of the attribute
     * @return the attribute value, or {@code null} if not found
     */
    <V> V getAttribute(String name);

    /**
     * Stores a request-scoped attribute.
     * @param name the name of the attribute
     * @param value the value to store
     */
    void setAttribute(String name, Object value);

    /**
     * Returns a collection of all attribute names in the current request scope.
     * @return a collection of attribute names
     */
    Collection<String> getAttributeNames();

    /**
     * Removes a request-scoped attribute.
     * @param name the name of the attribute to remove
     */
    void removeAttribute(String name);

    /**
     * Checks if there is a flash map from a previous request (e.g., after a redirect).
     * @return {@code true} if an input flash map exists, {@code false} otherwise
     */
    boolean hasInputFlashMap();

    /**
     * Returns the input flash map from a previous request.
     * @return the input flash map, or {@code null} if none exists
     */
    Map<String, ?> getInputFlashMap();

    /**
     * Checks if an output flash map is available to store attributes for a subsequent request.
     * @return {@code true} if an output flash map is available, {@code false} otherwise
     */
    boolean hasOutputFlashMap();

    /**
     * Returns the output flash map for storing attributes for a subsequent request.
     * @return the output flash map
     */
    FlashMap getOutputFlashMap();

    /**
     * Immediately performs a transformation and ends the request processing.
     * @param transformRule the rule defining the transformation
     */
    void transform(TransformRule transformRule);

    /**
     * Immediately performs a transformation using a custom transformer and ends the request processing.
     * @param transformer the custom transformer instance
     */
    void transform(CustomTransformer transformer);

    /**
     * Dispatches the request to a different view resource (e.g., a JSP or template file).
     * @param name the name of the view resource to dispatch to
     */
    void dispatch(String name);

    /**
     * Dispatches the request to a different view resource using a specific dispatcher bean.
     * @param name the name of the view resource to dispatch to
     * @param dispatcherName the name of the dispatcher bean
     */
    void dispatch(String name, String dispatcherName);

    /**
     * Dispatches the request according to the provided dispatch rule.
     * @param dispatchRule the rule defining the dispatch
     */
    void dispatch(DispatchRule dispatchRule);

    /**
     * Forwards the request internally to another translet.
     * @param transletName the name of the target translet to forward to
     */
    void forward(String transletName);

    /**
     * Forwards the request internally to another translet as defined by the rule.
     * @param forwardRule the rule defining the forward
     */
    void forward(ForwardRule forwardRule);

    /**
     * Initiates a client-side redirect to a new URL.
     * @param path the target URL for the redirect
     */
    void redirect(String path);

    /**
     * Initiates a client-side redirect to a new URL with additional parameters.
     * @param path the target URL for the redirect
     * @param parameters a map of parameters to append to the URL
     */
    void redirect(String path, Map<String, String> parameters);

    /**
     * Initiates a client-side redirect as defined by the rule.
     * @param redirectRule the rule defining the redirect
     */
    void redirect(RedirectRule redirectRule);

    /**
     * Immediately sends the specified response and halts further processing.
     * @param response the response to send
     */
    void response(Response response);

    /**
     * Immediately sends the current response and halts further processing.
     */
    void response();

    /**
     * Returns the response that was originally declared in the translet rule.
     * @return the declared response rule
     */
    Response getDeclaredResponse();

    /**
     * Checks if a response has been reserved (i.e., if a flow control method like
     * redirect, forward, or transform has been called).
     * @return true if a response is already reserved, false otherwise
     */
    boolean isResponseReserved();

    /**
     * Checks if an exception has been raised during the current activity.
     * @return true if an exception was raised, false otherwise
     */
    boolean isExceptionRaised();

    /**
     * Returns the exception that was raised during the activity.
     * @return the raised {@code Throwable}, or {@code null} if no exception was raised
     */
    Throwable getRaisedException();

    /**
     * Returns the root cause of the exception that was raised.
     * @return the root cause exception, or {@code null}
     */
    Throwable getRootCauseOfRaisedException();

    /**
     * Clears the currently stored raised exception.
     */
    void removeRaisedException();

    /**
     * Retrieves an advice bean instance by its aspect ID.
     * @param <V> the type of the advice bean
     * @param aspectId the ID of the aspect containing the advice
     * @return the advice bean instance
     */
    <V> V getAdviceBean(String aspectId);

    /**
     * Retrieves the result of a 'before' advice execution for a given aspect.
     * @param <V> the type of the result
     * @param aspectId the ID of the aspect
     * @return the result of the before advice, or {@code null}
     */
    <V> V getBeforeAdviceResult(String aspectId);

    /**
     * Retrieves the result of an 'after' advice execution for a given aspect.
     * @param <V> the type of the result
     * @param aspectId the ID of the aspect
     * @return the result of the after advice, or {@code null}
     */
    <V> V getAfterAdviceResult(String aspectId);

    /**
     * Retrieves the result of an 'around' advice execution for a given aspect.
     * @param <V> the type of the result
     * @param aspectId the ID of the aspect
     * @return the result of the around advice, or {@code null}
     */
    <V> V getAroundAdviceResult(String aspectId);

    /**
     * Retrieves the result of a 'finally' advice execution for a given aspect.
     * @param <V> the type of the result
     * @param aspectId the ID of the aspect
     * @return the result of the finally advice, or {@code null}
     */
    <V> V getFinallyAdviceResult(String aspectId);

    /**
     * Checks if the current translet name contains path variables to be extracted.
     * @return true if path variables are present, false otherwise
     */
    boolean hasPathVariables();

    /**
     * Returns the response content that has been written so far, if supported by the adapter.
     * @return the written response content, or {@code null} if not available
     */
    String getWrittenResponse();

    /**
     * Evaluates an expression containing tokens (e.g., "Hello, ${userName}!").
     * @param <V> the type of the evaluation result
     * @param expression the expression string to evaluate
     * @return the evaluated result
     */
    <V> V evaluate(String expression);

    /**
     * Evaluates an array of pre-parsed tokens.
     * @param <V> the type of the evaluation result
     * @param tokens the tokens to evaluate
     * @return the evaluated result
     */
    <V> V evaluate(Token[] tokens);

    /**
     * Retrieves a bean instance by its ID from the bean registry.
     * @param <V> the type of the bean
     * @param id the ID of the bean
     * @return the bean instance
     */
    <V> V getBean(String id);

    /**
     * Retrieves a bean instance by its type from the bean registry.
     * @param <V> the type of the bean
     * @param type the class or interface type of the bean
     * @return the bean instance
     */
    <V> V getBean(Class<V> type);

    /**
     * Retrieves a bean instance by its type and ID from the bean registry.
     * @param <V> the type of the bean
     * @param type the class or interface type of the bean
     * @param id the ID of the bean
     * @return the bean instance
     */
    <V> V getBean(Class<V> type, String id);

    /**
     * Checks if a bean with the specified ID exists in the bean registry.
     * @param id the ID of the bean
     * @return true if the bean exists, false otherwise
     */
    boolean containsBean(String id);

    /**
     * Checks if a bean of the specified type exists in the bean registry.
     * @param type the class or interface type of the bean
     * @return true if a bean of the given type exists, false otherwise
     */
    boolean containsBean(Class<?> type);

    /**
     * Checks if a bean with the specified type and ID exists in the bean registry.
     * @param type the class or interface type of the bean
     * @param id the ID of the bean
     * @return true if the bean exists, false otherwise
     */
    boolean containsBean(Class<?> type, String id);

    /**
     * Resolves a message from the configured message source.
     * @param code the message code to look up
     * @return the resolved message
     * @throws NoSuchMessageException if the message is not found
     */
    String getMessage(String code) throws NoSuchMessageException;

    /**
     * Resolves a message with arguments from the configured message source.
     * @param code the message code to look up
     * @param args the arguments to format the message with
     * @return the resolved message
     * @throws NoSuchMessageException if the message is not found
     */
    String getMessage(String code, Object[] args) throws NoSuchMessageException;

    /**
     * Resolves a message, returning a default message if not found.
     * @param code the message code to look up
     * @param defaultMessage the message to return if the lookup fails
     * @return the resolved or default message
     */
    String getMessage(String code, String defaultMessage);

    /**
     * Resolves a message with arguments, returning a default message if not found.
     * @param code the message code to look up
     * @param args the arguments to format the message with
     * @param defaultMessage the message to return if the lookup fails
     * @return the resolved or default message
     */
    String getMessage(String code, Object[] args, String defaultMessage);

    /**
     * Resolves a message for a specific locale.
     * @param code the message code to look up
     * @param locale the locale to use for the lookup
     * @return the resolved message
     * @throws NoSuchMessageException if the message is not found
     */
    String getMessage(String code, Locale locale) throws NoSuchMessageException;

    /**
     * Resolves a message with arguments for a specific locale.
     * @param code the message code to look up
     * @param args the arguments to format the message with
     * @param locale the locale to use for the lookup
     * @return the resolved message
     * @throws NoSuchMessageException if the message is not found
     */
    String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException;

    /**
     * Resolves a message for a specific locale, returning a default message if not found.
     * @param code the message code to look up
     * @param defaultMessage the message to return if the lookup fails
     * @param locale the locale to use for the lookup
     * @return the resolved or default message
     */
    String getMessage(String code, String defaultMessage, Locale locale);

    /**
     * Resolves a message with arguments for a specific locale, returning a default message if not found.
     * @param code the message code to look up
     * @param args the arguments to format the message with
     * @param defaultMessage the message to return if the lookup fails
     * @param locale the locale to use for the lookup
     * @return the resolved or default message
     */
    String getMessage(String code, Object[] args, String defaultMessage, Locale locale);

}
