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
 * Translet provides the parsed request data to the user and
 * processes the user's response command.
 *
 * <p>Created: 2008. 7. 5. AM 12:35:44</p>
 */
public interface Translet {

    /**
     * Returns the current activity mode.
     */
    Activity.Mode getMode();

    /**
     * Returns the context path. This is similar to the
     * servlet context path.
     * @return the context path of the activity, or ""
     *      for the root context
     */
    String getContextPath();

    /**
     * Returns the request name for this {@code Translet}.
     * @return the request name
     */
    String getRequestName();

    /**
     * Returns the actual request name. It may or may not
     * have a context path.
     * @return the actual request name
     */
    String getActualRequestName();

    /**
     * Returns the request method. This is similar to the HTTP
     * request method.
     * @return the request method
     */
    MethodType getRequestMethod();

    /**
     * Returns the translet name.
     * @return the translet name
     */
    String getTransletName();

    /**
     * Returns a description of this {@code Translet}.
     * @return a description of this {@code Translet}
     */
    String getDescription();

    /**
     * Returns the environment of the current activity context.
     * @return the environment
     */
    Environment getEnvironment();

    /**
     * Gets the application adapter.
     * @return the application adapter
     */
    ApplicationAdapter getApplicationAdapter();

    boolean hasSessionAdapter();

    /**
     * Gets the session adapter.
     * @return the session adapter
     */
    SessionAdapter getSessionAdapter();

    /**
     * Gets the request adapter.
     * @return the request adapter
     */
    RequestAdapter getRequestAdapter();

    /**
     * Gets the response adapter.
     * @return the response adapter
     */
    ResponseAdapter getResponseAdapter();

    /**
     * Returns the adaptee object to provide session information.
     * @param <V> the type of the session adaptee
     * @return the session adaptee object
     */
    <V> V getSessionAdaptee();

    /**
     * Returns the adaptee object to provide request information.
     * @param <V> the type of the request adaptee
     * @return the request adaptee object
     */
    <V> V getRequestAdaptee();

    /**
     * Returns the adaptee object to provide response information.
     * @param <V> the type of the response adaptee
     * @return the response adaptee object
     */
    <V> V getResponseAdaptee();

    /**
     * Returns the definitive request encoding.
     * @return the definitive request encoding
     */
    String getDefinitiveRequestEncoding();

    /**
     * Returns the definitive response encoding.
     * @return the definitive response encoding
     */
    String getDefinitiveResponseEncoding();

    /**
     * Returns the process result.
     * @return the process result
     */
    ProcessResult getProcessResult();

    /**
     * Returns an action result for the specified action id from the process result,
     * or {@code null} if the action does not exist.
     * @param actionId the specified action id
     * @return the action result
     */
    Object getProcessResult(String actionId);

    /**
     * Sets the process result.
     * @param processResult the new process result
     */
    void setProcessResult(ProcessResult processResult);

    /**
     * Returns an Activity Data containing the activity result data.
     * @return the activity data
     */
    ActivityData getActivityData();

    /**
     * Gets the setting value in the translet scope.
     * @param <V> the type of the value
     * @param settingName the setting name
     * @return the setting value
     */
    <V> V getSetting(String settingName);

    /**
     * Returns the value of the property on environment.
     * @param <V> the type of the value
     * @param name the given property name
     * @return the value of the property on environment
     */
    <V> V getProperty(String name);

    /**
     * Returns the value of an activity's request parameter as a {@code String},
     * or {@code null} if the parameter does not exist.
     * @param name a {@code String} specifying the name of the parameter
     * @return a {@code String} representing the
     *            single value of the parameter
     * @see #getParameterValues
     */
    String getParameter(String name);

    /**
     * Returns an array of {@code String} objects containing all
     * the values the given activity's request parameter has,
     * or {@code null} if the parameter does not exist.
     * @param name a {@code String} specifying the name of the parameter
     * @return an array of {@code String} objects
     *            containing the parameter's values
     * @see #getParameter
     */
    String[] getParameterValues(String name);

    /**
     * Returns a {@code Collection} of {@code String} objects containing
     * the names of the parameters contained in this request.
     * If the request has no parameters, the method returns an empty {@code Collection}.
     * @return an {@code Collection} of {@code String} objects, each {@code String}
     *             containing the name of a request parameter;
     *             or an empty {@code Collection} if the request has no parameters
     */
    Collection<String> getParameterNames();

    /**
     * Sets the value to the parameter with the given name.
     * @param name a {@code String} specifying the name of the parameter
     * @param value a {@code String} representing the
     *            single value of the parameter
     * @see #setParameter(String, String[])
     */
    void setParameter(String name, String value);

    /**
     * Sets the value to the parameter with the given name.
     * @param name a {@code String} specifying the name of the parameter
     * @param values an array of {@code String} objects
     *            containing the parameter's values
     * @see #setParameter
     */
    void setParameter(String name, String[] values);

    /**
     * Return a mutable Map of the request parameters,
     * with parameter names as map keys and parameter values as map values.
     * If the parameter value type is the {@code String} then map value will be of type {@code String}.
     * If the parameter value type is the {@code String} array then map value will be of type {@code String} array.
     * @return the mutable parameter map
     * @since 1.4.0
     */
    Map<String, Object> getAllParameters();

    /**
     * Extracts all the parameters and fills in the specified map.
     * @param targetParameters the target parameter map to be filled
     * @since 2.0.0
     */
    void extractParameters(Map<String, Object> targetParameters);

    /**
     * Returns a {@code FileParameter} object as a given activity's request parameter name,
     * or {@code null} if the parameter does not exist.
     * @param name a {@code String} specifying the name of the file parameter
     * @return a {@code FileParameter} representing the
     *            single value of the parameter
     * @see #getFileParameterValues
     */
    FileParameter getFileParameter(String name);

    /**
     * Returns an array of {@code FileParameter} objects containing all
     * the values the given activity's request file parameter has,
     * or {@code null} if the parameter does not exist.
     * @param name a {@code String} specifying the name of the file parameter
     * @return an array of {@code FileParameter} objects
     *            containing the parameter's values
     * @see #getFileParameter
     */
    FileParameter[] getFileParameterValues(String name);

    /**
     * Returns a {@code Collection} of {@code String} objects containing
     * the names of the file parameters contained in this request.
     * If the request has no parameters, the method returns an empty {@code Collection}.
     * @return an {@code Collection} of {@code String} objects, each {@code String}
     *             containing the name of a file parameter;
     *             or an empty {@code Collection} if the request has no file parameters
     */
    Collection<String> getFileParameterNames();

    /**
     * Sets the {@code FileParameter} object to the file parameter with the given name.
     * @param name a {@code String} specifying the name of the file parameter
     * @param fileParameter a {@code FileParameter} representing the
     *            single value of the parameter
     * @see #setFileParameter(String, FileParameter[])
     */
    void setFileParameter(String name, FileParameter fileParameter);

    /**
     * Sets the value to the file parameter with the given name.
     * @param name a {@code String} specifying the name of the file parameter
     * @param fileParameters an array of {@code FileParameter} objects
     *            containing the file parameter's values
     * @see #setFileParameter
     */
    void setFileParameter(String name, FileParameter[] fileParameters);

    /**
     * Removes the file parameter with the specified name.
     * @param name a {@code String} specifying the name of the file parameter
     */
    void removeFileParameter(String name);

    /**
     * Returns the value of the named attribute as a given type,
     * or {@code null} if no attribute of the given name exists.
     * @param <V> the type of attribute retrieved
     * @param name a {@code String} specifying the name of the attribute
     * @return an {@code Object} containing the value of the attribute,
     *             or {@code null} if the attribute does not exist
     */
    <V> V getAttribute(String name);

    /**
     * Stores an attribute in this request.
     * @param name specifying the name of the attribute
     * @param value the {@code Object} to be stored
     */
    void setAttribute(String name, Object value);

    /**
     * Returns a {@code Collection} containing the
     * names of the attributes available to this request.
     * This method returns an empty {@code Collection}
     * if the request has no attributes available to it.
     * @return the attribute names
     */
    Collection<String> getAttributeNames();

    /**
     * Removes an attribute from this request.
     * @param name a {@code String} specifying the name of the attribute to remove
     */
    void removeAttribute(String name);

    boolean hasInputFlashMap();

    Map<String, ?> getInputFlashMap();

    boolean hasOutputFlashMap();

    FlashMap getOutputFlashMap();

    /**
     * Transformation according to a given rule, and transmits this response.
     * @param transformRule the transformation rule
     */
    void transform(TransformRule transformRule);

    void transform(CustomTransformer transformer);

    /**
     * Dispatch to other resources as the given name.
     * @param name the dispatch name
     */
    void dispatch(String name);

    /**
     * Dispatch to other resources as the given name.
     * @param name the dispatch name
     * @param dispatcherName the id or class name of the view dispatcher bean
     */
    void dispatch(String name, String dispatcherName);

    /**
     * Dispatch to other resources as the given rule.
     * @param dispatchRule the dispatch rule
     */
    void dispatch(DispatchRule dispatchRule);

    /**
     * Forward to the specified translet immediately.
     * @param transletName the translet name of the target to be forwarded
     */
    void forward(String transletName);

    /**
     * Forward according to a given rule.
     * @param forwardRule the forward rule
     */
    void forward(ForwardRule forwardRule);

    /**
     * Redirect a client to a new target resource.
     * If an intended redirect rule exists, that may be used.
     * @param path the redirect path
     */
    void redirect(String path);

    /**
     * Redirect to the other target resource.
     * @param path the redirect path
     * @param parameters the parameters
     */
    void redirect(String path, Map<String, String> parameters);

    /**
     * Redirect a client according to the given rule.
     * @param redirectRule the redirect rule
     */
    void redirect(RedirectRule redirectRule);

    /**
     * Respond immediately, and the remaining jobs will be canceled.
     * @param response the response
     */
    void response(Response response);

    /**
     * Respond immediately, and the remaining jobs will be canceled.
     */
    void response();

    /**
     * Returns the originally declared response.
     * @return the declared response
     * @since 5.2.0
     */
    Response getDeclaredResponse();

    /**
     * Returns whether the response is reserved.
     * @return true, if the response is reserved
     */
    boolean isResponseReserved();

    /**
     * Returns whether the exception was thrown.
     * @return true, if is exception raised
     */
    boolean isExceptionRaised();

    /**
     * Returns the raised exception instance.
     * @return the raised exception instance
     */
    Throwable getRaisedException();

    /**
     * Returns the innermost one of the chained (wrapped) exceptions.
     * @return the innermost one of the chained (wrapped) exceptions
     */
    Throwable getRootCauseOfRaisedException();

    /**
     * Remove the raised exception.
     */
    void removeRaisedException();

    /**
     * Gets the advice bean.
     * @param <V> the result type of the advice
     * @param aspectId the aspect id
     * @return the advice bean
     */
    <V> V getAdviceBean(String aspectId);

    /**
     * Gets the before advice result.
     * @param <V> the result type of the before advice
     * @param aspectId the aspect id
     * @return the before advice result
     */
    <V> V getBeforeAdviceResult(String aspectId);

    /**
     * Gets the after advice result.
     * @param <V> the result type of the after advice
     * @param aspectId the aspect id
     * @return the after advice result
     */
    <V> V getAfterAdviceResult(String aspectId);

    /**
     * Gets the around advice result.
     * @param <V> the result type of the around advice
     * @param aspectId the aspect id
     * @return the around advice result
     */
    <V> V getAroundAdviceResult(String aspectId);

    /**
     * Gets the final advice result.
     * @param <V> the result type of the final advice
     * @param aspectId the aspect id
     * @return the result of final advice
     */
    <V> V getFinallyAdviceResult(String aspectId);

    /**
     * Returns whether the translet name has tokens for extracting parameters or attributes.
     * @return true if the translet name has tokens for extracting parameters or attributes
     */
    boolean hasPathVariables();

    String getWrittenResponse();

    /**
     * Evaluates a token expression.
     * @param <V> the type of evaluation result value
     * @param expression the token expression to evaluate
     * @return if there are multiple tokens, the result of evaluating them is returned
     *      as a string. If there is only one token, it is returned as is.
     * @since 7.4.3
     */
    <V> V evaluate(String expression);

    /**
     * Evaluates a token expression.
     * @param <V> the type of evaluation result value
     * @param tokens the tokens to evaluate
     * @return if there are multiple tokens, the result of evaluating them is returned
     *      as a string. If there is only one token, it is returned as is.
     * @since 7.4.3
     */
    <V> V evaluate(Token[] tokens);

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
     * @param type the type that the bean must match; can be an interface or
     *      superclass, and {@code null} is also allowed.
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

    /**
     * Try to resolve the message. Treat as an error if the message can't be found.
     * @param code the code to lookup up, such as 'calculator.noRateSet'
     * @return the resolved message
     * @throws NoSuchMessageException if the message wasn't found
     * @see java.text.MessageFormat
     */
    String getMessage(String code) throws NoSuchMessageException;

    /**
     * Try to resolve the message. Treat as an error if the message can't be found.
     * @param code the code to lookup up, such as 'calculator.noRateSet'
     * @param args Array of arguments that will be filled in for params within
     *         the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
     *         or {@code null} if none.
     * @return the resolved message
     * @throws NoSuchMessageException if the message wasn't found
     * @see java.text.MessageFormat
     */
    String getMessage(String code, Object[] args) throws NoSuchMessageException;

    /**
     * Try to resolve the message. Return a default message if no message was found.
     * @param code the code to lookup up, such as 'calculator.noRateSet'. Users of
     *         this class are encouraged to base message names on the relevant fully
     *         qualified class name, thus avoiding conflict and ensuring maximum clarity.
     * @param defaultMessage String to return if the lookup fails
     * @return the resolved message if the lookup was successful;
     *         otherwise the default message passed as a parameter
     * @see java.text.MessageFormat
     */
    String getMessage(String code, String defaultMessage);

    /**
     * Try to resolve the message. Return default message if no message was found.
     * @param code the code to lookup up, such as 'calculator.noRateSet'. Users of
     *         this class are encouraged to base message names on the relevant fully
     *         qualified class name, thus avoiding conflict and ensuring maximum clarity.
     * @param args array of arguments that will be filled in for params within
     *         the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
     *         or {@code null} if none.
     * @param defaultMessage String to return if the lookup fails
     * @return the resolved message if the lookup was successful;
     *         otherwise the default message passed as a parameter
     * @see java.text.MessageFormat
     */
    String getMessage(String code, Object[] args, String defaultMessage);

    /**
     * Try to resolve the message. Treat as an error if the message can't be found.
     * @param code the code to lookup up, such as 'calculator.noRateSet'
     * @param locale the Locale in which to do the lookup
     * @return the resolved message
     * @throws NoSuchMessageException if the message wasn't found
     * @see java.text.MessageFormat
     */
    String getMessage(String code, Locale locale) throws NoSuchMessageException;

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
     * Try to resolve the message. Return a default message if no message was found.
     * @param code the code to lookup up, such as 'calculator.noRateSet'. Users of
     *         this class are encouraged to base message names on the relevant fully
     *         qualified class name, thus avoiding conflict and ensuring maximum clarity.
     * @param defaultMessage String to return if the lookup fails
     * @param locale the Locale in which to do the lookup
     * @return the resolved message if the lookup was successful;
     *         otherwise the default message passed as a parameter
     * @see java.text.MessageFormat
     */
    String getMessage(String code, String defaultMessage, Locale locale);

    /**
     * Try to resolve the message. Return a default message if no message was found.
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

}
