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
package com.aspectran.core.activity;

import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.request.parameter.FileParameter;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.context.rule.DispatchResponseRule;
import com.aspectran.core.context.rule.ForwardResponseRule;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.support.i18n.message.MessageSource;
import com.aspectran.core.support.i18n.message.NoSuchMessageException;

import java.util.Collection;
import java.util.Map;

/**
 * The Interface Translet.
 *
 * <p>Created: 2008. 7. 5. AM 12:35:44</p>
 */
public interface Translet extends BeanRegistry, MessageSource {

    /**
     * Returns the name of this {@code Translet}.
     *
     * @return the translet name
     */
    String getName();

    /**
     * Returns a description of this {@code Translet}.
     *
     * @return a description of this {@code Translet}
     */
    String getDescription();

    /**
     * Gets the request http method.
     *
     * @return the request method
     */
    MethodType getRequestMethod();

    /**
     * Returns the environment of the current activity context.
     *
     * @return the environment
     */
    Environment getEnvironment();

    /**
     * Gets the application adapter.
     *
     * @return the application adapter
     */
    ApplicationAdapter getApplicationAdapter();

    /**
     * Gets the session adapter.
     *
     * @return the session adapter
     */
    SessionAdapter getSessionAdapter();

    /**
     * Gets the request adapter.
     *
     * @return the request adapter
     */
    RequestAdapter getRequestAdapter();

    /**
     * Gets the response adapter.
     *
     * @return the response adapter
     */
    ResponseAdapter getResponseAdapter();

    /**
     * Returns the adaptee object to provide application information.
     *
     * @param <T> the type of the application adaptee
     * @return the application adaptee object
     */
    <T> T getApplicationAdaptee();

    /**
     * Returns the adaptee object to provide session information.
     *
     * @param <T> the type of the session adaptee
     * @return the session adaptee object
     */
    <T> T getSessionAdaptee();

    /**
     * Returns the adaptee object to provide request information.
     *
     * @param <T> the type of the request adaptee
     * @return the request adaptee object
     */
    <T> T getRequestAdaptee();

    /**
     * Returns the adaptee object to provide response information.
     *
     * @param <T> the type of the response adaptee
     * @return the response adaptee object
     */
    <T> T getResponseAdaptee();

    /**
     * Returns the request encoding.
     *
     * @return the request encoding
     */
    String getRequestEncoding();

    /**
     * Returns the response encoding.
     *
     * @return the response encoding
     */
    String getResponseEncoding();

    /**
     * Returns the process result.
     *
     * @return the process result
     */
    ProcessResult getProcessResult();

    /**
     * Returns a action result for the specified action id from the process result,
     * or {@code null} if the action does not exist.
     *
     * @param actionId the specified action id
     * @return the action result
     */
    Object getProcessResult(String actionId);

    /**
     * Sets the process result.
     *
     * @param processResult the new process result
     */
    void setProcessResult(ProcessResult processResult);

    /**
     * Returns the ProcessResult.
     * If not yet instantiated then create a new one.
     *
     * @return the process result
     */
    ProcessResult touchProcessResult();

    /**
     * Returns the process result with the contents name.
     * If not yet instantiated then create a new one.
     *
     * @param contentsName the contents name
     * @return the process result
     */
    ProcessResult touchProcessResult(String contentsName);

    /**
     * Returns the process result with the contents name.
     * If not yet instantiated then create a new one.
     *
     * @param contentsName the contents name
     * @param initialCapacity the initial capacity of the process result
     * @return the process result
     */
    ProcessResult touchProcessResult(String contentsName, int initialCapacity);

    /**
     * Returns an Activity Data Map containing the activity result data.
     *
     * @return the activity data map
     */
    ActivityDataMap getActivityDataMap();

    /**
     * Returns an Activity Data Map containing the activity result data.
     *
     * @param prefill whether data pre-fill.
     * @return the activity data map
     */
    ActivityDataMap getActivityDataMap(boolean prefill);

    /**
     * Gets the setting value in the translet scope.
     *
     * @param <T> the type of the value
     * @param settingName the setting name
     * @return the setting value
     */
    <T> T getSetting(String settingName);

    /**
     * Returns the value of an activity's request parameter as a {@code String},
     * or {@code null} if the parameter does not exist.
     *
     * @param name a {@code String} specifying the name of the parameter
     * @return a {@code String} representing the
     *            single value of the parameter
     * @see #getParameterValues
     */
    String getParameter(String name);

    /**
     * Returns an array of {@code String} objects containing all
     * of the values the given activity's request parameter has,
     * or {@code null} if the parameter does not exist.
     *
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
     *
     * @return an {@code Collection} of {@code String} objects, each {@code String}
     *             containing the name of a request parameter;
     *             or an empty {@code Collection} if the request has no parameters
     */
    Collection<String> getParameterNames();

    /**
     * Sets the value to the parameter with the given name.
     *
     * @param name a {@code String} specifying the name of the parameter
     * @param value a {@code String} representing the
     *            single value of the parameter
     * @see #setParameter(String, String[])
     */
    void setParameter(String name, String value);

    /**
     * Sets the value to the parameter with the given name.
     *
     * @param name a {@code String} specifying the name of the parameter
     * @param values an array of {@code String} objects
     *            containing the parameter's values
     * @see #setParameter
     */
    void setParameter(String name, String[] values);

    /**
     * Return an mutable Map of the request parameters,
     * with parameter names as map keys and parameter values as map values.
     * If the parameter value type is the {@code String} then map value will be of type {@code String}.
     * If the parameter value type is the {@code String} array then map value will be of type {@code String} array.
     *
     * @return the mutable parameter map
     * @since 1.4.0
     */
    Map<String, Object> getAllParameters();

    /**
     * Extracts all the parameters and fills in the specified map.
     *
     * @param targetParameters the target parameter map to be filled
     * @since 2.0.0
     */
    void extractParameters(Map<String, Object> targetParameters);

    /**
     * Returns a {@code FileParameter} object as a given activity's request parameter name,
     * or {@code null} if the parameter does not exist.
     *
     * @param name a {@code String} specifying the name of the file parameter
     * @return a {@code FileParameter} representing the
     *            single value of the parameter
     * @see #getFileParameterValues
     */
    FileParameter getFileParameter(String name);

    /**
     * Returns an array of {@code FileParameter} objects containing all
     * of the values the given activity's request parameter has,
     * or {@code null} if the parameter does not exist.
     *
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
     *
     * @return an {@code Collection} of {@code String} objects, each {@code String}
     *             containing the name of a file parameter;
     *             or an empty {@code Collection} if the request has no file parameters
     */
    Collection<String> getFileParameterNames();

    /**
     * Sets the {@code FileParameter} object to the file parameter with the given name.
     *
     * @param name a {@code String} specifying the name of the file parameter
     * @param fileParameter a {@code FileParameter} representing the
     *            single value of the parameter
     * @see #setFileParameter(String, FileParameter[])
     */
    void setFileParameter(String name, FileParameter fileParameter);

    /**
     * Sets the value to the file parameter with the given name.
     *
     * @param name a {@code String} specifying the name of the file parameter
     * @param fileParameters an array of {@code FileParameter} objects
     *            containing the file parameter's values
     * @see #setFileParameter
     */
    void setFileParameter(String name, FileParameter[] fileParameters);

    /**
     * Removes the file parameter with the specified name.
     *
     * @param name a {@code String} specifying the name of the file parameter
     */
    void removeFileParameter(String name);

    /**
     * Returns the value of the named attribute as a given type,
     * or {@code null} if no attribute of the given name exists.
     *
     * @param <T> the generic type
     * @param name a {@code String} specifying the name of the attribute
     * @return an {@code Object} containing the value of the attribute,
     *             or {@code null} if the attribute does not exist
     */
    <T> T getAttribute(String name);

    /**
     * Stores an attribute in this request.
     *
     * @param name specifying the name of the attribute
     * @param value the {@code Object} to be stored
     */
    void setAttribute(String name, Object value);

    /**
     * Returns a {@code Collection} containing the
     * names of the attributes available to this request.
     * This method returns an empty {@code Collection}
     * if the request has no attributes available to it.
     *
     * @return the attribute names
     */
    Collection<String> getAttributeNames();

    /**
     * Removes an attribute from this request.
     *
     * @param name a {@code String} specifying the name of the attribute to remove
     */
    void removeAttribute(String name);

    /**
     * Return a mutable {@code Map} of the request attributes,
     * with attribute names as map keys and attribute value as map value.
     *
     * @return the attribute map
     * @since 2.0.0
     */
    Map<String, Object> getAllAttributes();

    /**
     * Extracts all the attributes and fills in the specified map.
     *
     * @param attributes the attribute map
     * @since 2.0.0
     */
    void extractAttributes(Map<String, Object> attributes);


    /**
     * Returns the originally declared response.
     *
     * @return the declared response
     * @since 5.2.0
     */
    Response getDeclaredResponse();

    /**
     * Respond immediately, and the remaining jobs will be canceled.
     */
    void response();

    /**
     * Respond immediately, and the remaining jobs will be canceled.
     *
     * @param response the response
     */
    void response(Response response);

    /**
     * Transformation according to a given rule, and transmits this response.
     *
     * @param transformRule the transformation rule
     */
    void transform(TransformRule transformRule);

    /**
     * Dispatch to other resources as the given rule.
     *
     * @param dispatchResponseRule the dispatch response rule
     */
    void dispatch(DispatchResponseRule dispatchResponseRule);

    /**
     * Dispatch to other resources as the given name.
     *
     * @param name the dispatch name
     */
    void dispatch(String name);

    /**
     * Dispatch to other resources as the given name.
     *
     * @param name the dispatch name
     * @param immediately whether to override the intended dispatch response rule
     */
    void dispatch(String name, boolean immediately);

    /**
     * Redirect a client according to the given rule.
     *
     * @param redirectResponseRule the redirect response rule
     */
    void redirect(RedirectResponseRule redirectResponseRule);

    /**
     * Redirect a client to a new target resource.
     * If an intended redirect response rule exists, that may be used.
     *
     * @param path the redirect path
     */
    void redirect(String path);

    /**
     * Redirect a client to a new target resource.
     * If {@code immediately} is true, create a new redirect response rule
     * and override the intended redirect response rule.
     *
     * @param path the redirect path
     * @param immediately whether to override the intended redirect response rule
     */
    void redirect(String path, boolean immediately);

    /**
     * Redirect to the other target resource.
     *
     * @param path the redirect path
     * @param parameters the parameters
     */
    void redirect(String path, Map<String, String> parameters);

    /**
     * Forward according to a given rule.
     *
     * @param forwardResponseRule the forward response rule
     */
    void forward(ForwardResponseRule forwardResponseRule);

    /**
     * Forward to the specified translet immediately.
     *
     * @param transletName the translet name of the target to be forwarded
     */
    void forward(String transletName);

    /**
     * Forward to the specified translet.
     *
     * @param transletName the translet name
     * @param immediately whether forwarding immediately
     */
    void forward(String transletName, boolean immediately);

    /**
     * Returns whether the exception was thrown.
     *
     * @return true, if is exception raised
     */
    boolean isExceptionRaised();

    /**
     * Returns the raised exception instance.
     *
     * @return the raised exception instance
     */
    Throwable getRaisedException();

    /**
     * Returns the innermost one of the chained (wrapped) exceptions.
     *
     * @return the innermost one of the chained (wrapped) exceptions
     */
    Throwable getRootCauseOfRaisedException();

    /**
     * Returns an interface class for the {@code Translet}.
     *
     * @return the interface class for the translet
     */
    Class<? extends Translet> getTransletInterfaceClass();

    /**
     * Returns an implementation class for the {@code Translet}.
     *
     * @return the implementation class for the translet
     */
    Class<? extends CoreTranslet> getTransletImplementationClass();

    /**
     * Return whether the given profile is active.
     * If active profiles are empty whether the profile should be active by default.
     *
     * @param profiles the profiles
     * @return {@code true} if profile is active, otherwise {@code false}
     */
    boolean acceptsProfiles(String... profiles);

    /**
     * Gets the aspect advice bean.
     *
     * @param <T> the generic type
     * @param aspectId the aspect id
     * @return the aspect advice bean
     */
    <T> T getAspectAdviceBean(String aspectId);

    /**
     * Gets the before advice result.
     *
     * @param <T> the generic type
     * @param aspectId the aspect id
     * @return the before advice result
     */
    <T> T getBeforeAdviceResult(String aspectId);

    /**
     * Gets the after advice result.
     *
     * @param <T> the generic type
     * @param aspectId the aspect id
     * @return the after advice result
     */
    <T> T getAfterAdviceResult(String aspectId);

    /**
     * Gets the around advice result.
     *
     * @param <T> the generic type
     * @param aspectId the aspect id
     * @return the around advice result
     */
    <T> T getAroundAdviceResult(String aspectId);

    /**
     * Gets the finally advice result.
     *
     * @param <T> the generic type
     * @param aspectId the aspect id
     * @return the finally advice result
     */
    <T> T getFinallyAdviceResult(String aspectId);

    boolean hasPathVariable();

    /**
     * Try to resolve the message. Return default message if no message was found.
     *
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
     *
     * @param code the code to lookup up, such as 'calculator.noRateSet'
     * @param args Array of arguments that will be filled in for params within
     *         the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
     *         or {@code null} if none.
     * @return the resolved message
     * @throws NoSuchMessageException if the message wasn't found
     * @see java.text.MessageFormat
     */
    String getMessage(String code, Object[] args) throws NoSuchMessageException;

}
