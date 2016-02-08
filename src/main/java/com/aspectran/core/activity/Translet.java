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

import java.util.Map;

import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.ForwardResponseRule;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.type.RequestMethodType;

/**
 * The Interface Translet.
 *
 * <p>Created: 2008. 7. 5. AM 12:35:44</p>
 */
public interface Translet {

	/**
	 * Returns the path of translet.
	 *
	 * @return the translet path
	 */
	public String getTransletName();

	/**
	 * Gets the REST verb.
	 *
	 * @return the REST verb
	 */
	public RequestMethodType getRestVerb();

	/**
	 * Gets the declared attribute map.
	 *
	 * @return the declared attribute map
	 */
	public Map<String, Object> getDeclaredAttributeMap();

	/**
	 * Sets the declared attribute map.
	 *
	 * @param declaredAttributeMap the declared attribute map
	 */
	public void setDeclaredAttributeMap(Map<String, Object> declaredAttributeMap);

	/**
	 * Get a named attribute and convert it into the given type.
	 *
	 * @param <T> the generic type
	 * @param name the name of the attribute
	 * @return return named value converted to type T or null if non existing.
	 */
	public <T> T getAttribute(String name);

	/**
	 * Get a named attribute and convert it into the given type.
	 *
	 * @param <T> the generic type
	 * @param name the name of the attribute
	 * @param defaultValue the default value to use if the named attribute does not exist. the default value is also used to define the type to convert the value to.
	 * @return return named value converted to type T or the default value if non existing.
	 */
	public <T> T getAttribute(String name, T defaultValue);

	/**
	 * Sets the attribute.
	 *
	 * @param name the attribute name
	 * @param value the attribute value
	 */
	public void setAttribute(String name, Object value);

	/**
	 * Gets the process result.
	 *
	 * @return the process result
	 */
	public ProcessResult getProcessResult();

	/**
	 * Gets the action result value by specified action id.
	 *
	 * @param actionId the specified action id
	 * @return the action result vlaue
	 */
	public Object getProcessResult(String actionId);

	/**
	 * Sets the process result.
	 *
	 * @param processResult the new process result
	 */
	public void setProcessResult(ProcessResult processResult);

	/**
	 * Returns the ProcessResult. If not yet instantiated then create a new one.
	 *
	 * @return the process result
	 */
	public ProcessResult touchProcessResult();

	/**
	 * Returns the ProcessResult.
	 * If not yet instantiated then create a new one.
	 *
	 * @param contentName the content name
	 * @return the process result
	 */
	public ProcessResult touchProcessResult(String contentName);

	/**
	 * Gets activity data map.
	 *
	 * @return the activity data map
	 */
	public ActivityDataMap getActivityDataMap();

	/**
	 * Gets activity data map.
	 *
	 * @param prefill whether data pre-fill.
	 * @return the activity data map
	 */
	public ActivityDataMap getActivityDataMap(boolean prefill);

	/**
	 * Respond immediately, and the remaining jobs will be canceled.
	 */
	public void response();

	/**
	 * Respond immediately, and the remaining jobs will be canceled.
	 *
	 * @param response the response
	 */
	public void response(Response response);

	/**
	 * Transformation according to a given rule, and transmits this response.
	 *
	 * @param transformRule the transformation rule
	 */
	public void transform(TransformRule transformRule);

	/**
	 * Redirect according to a given rule.
	 *
	 * @param redirectResponseRule the redirect response rule
	 */
	public void redirect(RedirectResponseRule redirectResponseRule);

	/**
	 * Redirect to other resource.
	 *
	 * @param target the target resource
	 */
	public void redirect(String target);

	/**
	 * Redirect.
	 *
	 * @param target the target
	 * @param immediately the immediately
	 */
	public void redirect(String target, boolean immediately);

	/**
	 * Redirect.
	 *
	 * @param target the target
	 * @param parameters the parameters
	 */
	public void redirect(String target, Map<String, String> parameters);

	/**
	 * Forward according to a given rule.
	 *
	 * @param forwardResponseRule the forward response rule
	 */
	public void forward(ForwardResponseRule forwardResponseRule);

	/**
	 * Immediately forwarding.
	 *
	 * @param transletName the translet name of the target to be forwarded
	 */
	public void forward(String transletName);

	/**
	 * Forward.
	 *
	 * @param transletName the translet name
	 * @param immediately the immediately
	 */
	public void forward(String transletName, boolean immediately);

	/**
	 * Returns whether the exception was thrown.
	 *
	 * @return true, if is exception raised
	 */
	public boolean isExceptionRaised();

	/**
	 * Returns the raised exception.
	 *
	 * @return the raised exception
	 */
	public Exception getRaisedException();

	/**
	 * Gets the application adapter.
	 *
	 * @return the application adapter
	 */
	public ApplicationAdapter getApplicationAdapter();

	/**
	 * Gets the session adapter.
	 *
	 * @return the session adapter
	 */
	public SessionAdapter getSessionAdapter();

	/**
	 * Gets the request adapter.
	 *
	 * @return the request adapter
	 */
	public RequestAdapter getRequestAdapter();

	/**
	 * Gets the response adapter.
	 *
	 * @return the response adapter
	 */
	public ResponseAdapter getResponseAdapter();

	/**
	 * Gets the request adaptee.
	 *
	 * @param <T> the generic type
	 * @return the request adaptee
	 */
	public <T> T getRequestAdaptee();

	/**
	 * Gets the response adaptee.
	 *
	 * @param <T> the generic type
	 * @return the response adaptee
	 */
	public <T> T getResponseAdaptee();

	/**
	 * Gets the session adaptee.
	 *
	 * @param <T> the generic type
	 * @return the session adaptee
	 */
	public <T> T getSessionAdaptee();

	/**
	 * Gets the application adaptee.
	 *
	 * @param <T> the generic type
	 * @return the application adaptee
	 */
	public <T> T getApplicationAdaptee();

	/**
	 * Return an instance.
	 *
	 * @param <T> the generic type
	 * @param id the id of the bean to retrieve
	 * @return an instance of the bean
	 */
	public <T> T getBean(String id);

	/**
	 * Return the bean instance that matches the given object type.
	 * The class name and bean id must be the same.
	 *
	 * @param <T> the generic type
	 * @param classType type the bean must match; can be an interface or superclass. null is disallowed.
	 * @return an instance of the bean
	 * @since 1.3.1
	 */
	public <T> T getBean(Class<T> classType);

	/**
	 * Return an instance.
	 * If the bean is not of the required type then throw a BeanNotOfRequiredTypeException.
	 *
	 * @param <T> the generic type
	 * @param id the id of the bean to retrieve
	 * @param requiredType type the bean must match; can be an interface or superclass. null is disallowed.
	 * @return an instance of the bean
	 * @since 1.3.1
	 */
	public <T> T getBean(String id, Class<T> requiredType);

	/**
	 * Gets the aspect advice bean.
	 *
	 * @param <T> the generic type
	 * @param aspectId the aspect id
	 * @return the aspect advice bean
	 */
	public <T> T getAspectAdviceBean(String aspectId);

	/**
	 * Put aspect advice bean.
	 *
	 * @param aspectId the aspect id
	 * @param adviceBean the advice bean
	 */
	public void putAspectAdviceBean(String aspectId, Object adviceBean);

	/**
	 * Gets the before advice result.
	 *
	 * @param <T> the generic type
	 * @param aspectId the aspect id
	 * @return the before advice result
	 */
	public <T> T getBeforeAdviceResult(String aspectId);

	/**
	 * Gets the after advice result.
	 *
	 * @param <T> the generic type
	 * @param aspectId the aspect id
	 * @return the after advice result
	 */
	public <T> T getAfterAdviceResult(String aspectId);

	/**
	 * Gets the finally advice result.
	 *
	 * @param <T> the generic type
	 * @param aspectId the aspect id
	 * @return the finally advice result
	 */
	public <T> T getFinallyAdviceResult(String aspectId);

	/**
	 * Put advice result.
	 *
	 * @param aspectAdviceRule the aspect advice rule
	 * @param adviceActionResult the advice action result
	 */
	public void putAdviceResult(AspectAdviceRule aspectAdviceRule, Object adviceActionResult);

	/**
	 * Gets the translet interface class.
	 *
	 * @return the translet interface class
	 */
	public Class<? extends Translet> getTransletInterfaceClass();

	/**
	 * Gets the translet implement class.
	 *
	 * @return the translet implement class
	 */
	public Class<? extends CoreTranslet> getTransletImplementClass();

}
