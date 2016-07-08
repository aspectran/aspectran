/**
 * Copyright 2008-2016 Juho Jeong
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

import java.util.Enumeration;
import java.util.Map;

import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.request.parameter.FileParameter;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.bean.BeanRegistry;
import com.aspectran.core.context.message.MessageSource;
import com.aspectran.core.context.message.NoSuchMessageException;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.ForwardResponseRule;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.type.MethodType;

/**
 * The Interface Translet.
 *
 * <p>Created: 2008. 7. 5. AM 12:35:44</p>
 */
public interface Translet extends BeanRegistry, MessageSource {

	/**
	 * Returns the name of the translet.
	 *
	 * @return the translet name
	 */
	String getTransletName();

	/**
	 * Gets the request http method.
	 *
	 * @return the request method
	 */
	MethodType getRequestMethod();

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
	 * Gets the request adaptee.
	 *
	 * @param <T> the generic type
	 * @return the request adaptee
	 */
	<T> T getRequestAdaptee();

	/**
	 * Gets the response adaptee.
	 *
	 * @param <T> the generic type
	 * @return the response adaptee
	 */
	<T> T getResponseAdaptee();

	/**
	 * Gets the session adaptee.
	 *
	 * @param <T> the generic type
	 * @return the session adaptee
	 */
	<T> T getSessionAdaptee();

	/**
	 * Gets the application adaptee.
	 *
	 * @param <T> the generic type
	 * @return the application adaptee
	 */
	<T> T getApplicationAdaptee();

	/**
	 * Gets the process result.
	 *
	 * @return the process result
	 */
	ProcessResult getProcessResult();

	/**
	 * Gets the action result value by specified action id.
	 *
	 * @param actionId the specified action id
	 * @return the action result vlaue
	 */
	Object getProcessResult(String actionId);

	/**
	 * Sets the process result.
	 *
	 * @param processResult the new process result
	 */
	void setProcessResult(ProcessResult processResult);

	/**
	 * Returns the ProcessResult. If not yet instantiated then create a new one.
	 *
	 * @return the process result
	 */
	ProcessResult touchProcessResult();

	/**
	 * Returns the process result.
	 * If not yet instantiated then create a new one.
	 *
	 * @param contentsName the content name
	 * @return the process result
	 */
	ProcessResult touchProcessResult(String contentsName);

	/**
	 * Returns the process result.
	 * If not yet instantiated then create a new one.
	 *
	 * @param contentsName the content name
	 * @param initialCapacity the initial capacity of the process result
	 * @return the process result
	 */
	ProcessResult touchProcessResult(String contentsName, int initialCapacity);

	/**
	 * Gets activity data map.
	 *
	 * @return the activity data map
	 */
	ActivityDataMap getActivityDataMap();

	/**
	 * Gets activity data map.
	 *
	 * @param prefill whether data pre-fill.
	 * @return the activity data map
	 */
	ActivityDataMap getActivityDataMap(boolean prefill);

	/**
	 * Gets the parameter.
	 *
	 * @param name the parameter name
	 * @return the parameter value
	 */
	String getParameter(String name);

	/**
	 * Gets the parameter values.
	 *
	 * @param name the name
	 * @return an array of <code>String</code> objects
	 *			containing the parameter's values
	 */
	String[] getParameterValues(String name);

	/**
	 * Gets the parameter names.
	 *
	 * @return the parameter names
	 */
	Enumeration<String> getParameterNames();

	/**
	 * Sets the parameter.
	 *
	 * @param name the parameter name
	 * @param value a <code>String</code> representing the
	 *			single value of the parameter
	 */
	void setParameter(String name, String value);

	/**
	 * Sets the parameter.
	 *
	 * @param name the parameter name
	 * @param values a <code>String</code> representing the
	 *			single value of the parameter
	 */
	void setParameter(String name, String[] values);

	/**
	 * Gets the file parameter.
	 *
	 * @param name the parameter name
	 * @return the file parameter
	 */
	FileParameter getFileParameter(String name);

	/**
	 * Gets the file parameter values.
	 *
	 * @param name the parameter name
	 * @return the file parameter values
	 */
	FileParameter[] getFileParameterValues(String name);

	/**
	 * Gets the file parameter names.
	 *
	 * @return the parameter names
	 */
	Enumeration<String> getFileParameterNames();

	/**
	 * Sets the file parameter.
	 *
	 * @param name the parameter name
	 * @param fileParameter the file parameter
	 */
	void setFileParameter(String name, FileParameter fileParameter);

	/**
	 * Sets the file parameter.
	 *
	 * @param name the parameter name
	 * @param fileParameters the file parameters
	 */
	void setFileParameter(String name, FileParameter[] fileParameters);

	/**
	 * Removes the file parameter.
	 *
	 * @param name the file parameter name
	 * @return the file parameter[]
	 */
	FileParameter[] removeFileParameter(String name);

	/**
	 * Returns the value of the named attribute as a given type, or <code>null</code> if no attribute of the given name exists.
	 *
	 * @param <T> the generic type
	 * @param name a String specifying the name of the attribute
	 * @return an Object containing the value of the attribute, or null if the attribute does not exist
	 */
	<T> T getAttribute(String name);

	/**
	 * Stores an attribute in this request.
	 *
	 * @param name specifying the name of the attribute
	 * @param value the Object to be stored
	 */
	void setAttribute(String name, Object value);

	/**
	 * Returns an <code>Enumeration</code> containing the
	 * names of the attributes available to this request.
	 * This method returns an empty <code>Enumeration</code>
	 * if the request has no attributes available to it.
	 *
	 * @return the attribute names
	 */
	Enumeration<String> getAttributeNames();

	/**
	 * Removes an attribute from this request.
	 *
	 * @param name a String specifying the name of the attribute to remove
	 */
	void removeAttribute(String name);

	/**
	 * Return a mutable Map of the request parameters,
	 * with parameter names as map keys and parameter values as map values.
	 * If the parameter value type is the String then map value will be of type String.
	 * If the parameter value type is the String array then map value will be of type String array.
	 *
	 * @return the parameter map
	 * @since 1.4.0
	 */
	Map<String, Object> getParameterMap();

	/**
	 * Fills all parameters to the specified map.
	 *
	 * @param parameterMap the parameter map
	 * @since 2.0.0
	 */
	void fillPrameterMap(Map<String, Object> parameterMap);

	/**
	 * Return a mutable Map of the request attributes,
	 * with attribute names as map keys and attribute value as map value.
	 *
	 * @return the attribute map
	 * @since 2.0.0
	 */
	Map<String, Object> getAttributeMap();

	/**
	 * Fills all attributes to the specified map.
	 *
	 * @param attributeMap the attribute map
	 * @since 2.0.0
	 */
	void fillAttributeMap(Map<String, Object> attributeMap);

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
	 * Redirect according to a given rule.
	 *
	 * @param redirectResponseRule the redirect response rule
	 */
	void redirect(RedirectResponseRule redirectResponseRule);

	/**
	 * Redirect to other resource.
	 *
	 * @param target the target resource
	 */
	void redirect(String target);

	/**
	 * Redirect.
	 *
	 * @param target the target
	 * @param immediately the immediately
	 */
	void redirect(String target, boolean immediately);

	/**
	 * Redirect to the other target resouce.
	 *
	 * @param target the redirect target
	 * @param parameters the parameters
	 */
	void redirect(String target, Map<String, String> parameters);

	/**
	 * Forward according to a given rule.
	 *
	 * @param forwardResponseRule the forward response rule
	 */
	void forward(ForwardResponseRule forwardResponseRule);

	/**
	 * Forward to specified translet immediately.
	 *
	 * @param transletName the translet name of the target to be forwarded
	 */
	void forward(String transletName);

	/**
	 * Forward to specified translet.
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
	Exception getRaisedException();

	/**
	 * Gets the aspect advice bean.
	 *
	 * @param <T> the generic type
	 * @param aspectId the aspect id
	 * @return the aspect advice bean
	 */
	<T> T getAspectAdviceBean(String aspectId);

	/**
	 * Put aspect advice bean.
	 *
	 * @param aspectId the aspect id
	 * @param adviceBean the advice bean
	 */
	void putAspectAdviceBean(String aspectId, Object adviceBean);

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
	 * Gets the finally advice result.
	 *
	 * @param <T> the generic type
	 * @param aspectId the aspect id
	 * @return the finally advice result
	 */
	<T> T getFinallyAdviceResult(String aspectId);

	/**
	 * Put advice result.
	 *
	 * @param aspectAdviceRule the aspect advice rule
	 * @param adviceActionResult the advice action result
	 */
	void putAdviceResult(AspectAdviceRule aspectAdviceRule, Object adviceActionResult);

	/**
	 * Return the interface class for {@code Translet}.
	 *
	 * @return the translet interface class
	 */
	Class<? extends Translet> getTransletInterfaceClass();

	/**
	 * Return the implementation class for {@code Translet}.
	 *
	 * @return the translet implementation class
	 */
	Class<? extends GenericTranslet> getTransletImplementationClass();

	/**
	 * Return whether the given profile is active.
	 * If active profiles are empty whether the profile should be active by default.
	 *
	 * @param profiles the profiles
	 * @return {@code true} if profile is active, otherwise {@code false}
	 */
	boolean acceptsProfiles(String... profiles);

	/**
	 * Try to resolve the message. Return default message if no message was found.
	 *
	 * @param code the code to lookup up, such as 'calculator.noRateSet'. Users of
	 * this class are encouraged to base message names on the relevant fully
	 * qualified class name, thus avoiding conflict and ensuring maximum clarity.
	 * @param args array of arguments that will be filled in for params within
	 * the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
	 * or {@code null} if none.
	 * @param defaultMessage String to return if the lookup fails
	 * @return the resolved message if the lookup was successful;
	 * otherwise the default message passed as a parameter
	 * @see java.text.MessageFormat
	 */
	String getMessage(String code, Object[] args, String defaultMessage);

	/**
	 * Try to resolve the message. Treat as an error if the message can't be found.
	 *
	 * @param code the code to lookup up, such as 'calculator.noRateSet'
	 * @param args Array of arguments that will be filled in for params within
	 * the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
	 * or {@code null} if none.
	 * @return the resolved message
	 * @throws NoSuchMessageException if the message wasn't found
	 * @see java.text.MessageFormat
	 */
	String getMessage(String code, Object[] args) throws NoSuchMessageException;

}
