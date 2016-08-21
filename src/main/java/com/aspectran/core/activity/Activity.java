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

import java.util.List;

import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.BeanRegistry;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.context.template.TemplateProcessor;

/**
 * The Interface Activity.
 * 
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public interface Activity extends BeanRegistry {

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
	Class<? extends CoreTranslet> getTransletImplementationClass();

	/**
	 * Preparation for the activity.
	 *
	 * @param transletName the translet name
	 */
	void prepare(String transletName);
	
	/**
	 * Preparation for the activity.
	 *
	 * @param transletName the translet name
	 * @param requestMethod the request method
	 */
	void prepare(String transletName, String requestMethod);

	/**
	 * Preparation for the activity.
	 *
	 * @param transletName the translet name
	 * @param requestMethod the request method
	 */
	void prepare(String transletName, MethodType requestMethod);
	
	/**
	 * Perform a prepared activity.
	 */
	void perform();
	
	/**
	 * Perform a prepared activity but does not respond to the client.
	 */
	void performWithoutResponse();

	/**
	 * Finish the activity.
	 * It must be called before exiting activity.
	 */
	void finish();

	/**
	 * Determine the request character encoding.
	 *
	 * @return the request character encoding
	 */
	String determineRequestCharacterEncoding();

	/**
	 * Determine the response character encoding.
	 *
	 * @return the response character encoding
	 */
	String determineResponseCharacterEncoding();
	
	/**
	 * Execute the aspect advices.
	 *
	 * @param aspectAdviceRuleList the aspect advice rule list
	 */
	void execute(List<AspectAdviceRule> aspectAdviceRuleList);
	
	/**
	 * Execute the aspect advices without throw exceptions.
	 *
	 * @param aspectAdviceRuleList the aspect advice rule list
	 */
	void executeWithoutThrow(List<AspectAdviceRule> aspectAdviceRuleList);
	
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
	 * Returns the translet name will be forwarded.
	 *
	 * @return the forwarding destination translet name
	 */
	String getForwardTransletName();
	
	/**
	 * Returns whether the current activity is completed or terminated.
	 * 
	 * @return true, if the current activity is completed or terminated
	 */
	boolean isActivityEnded();

	/**
	 * Stop the activity and responds immediately.
	 */
	void activityEnd();
	
	/**
	 * Respond immediately, and the remaining jobs will be canceled.
	 *
	 * @param response the response
	 */
	void response(Response response);
	
	/**
	 * Respond depending on the content type.
	 *
	 * @param exceptionRuleList the exception rule list
	 */
	void responseByContentType(List<ExceptionRule> exceptionRuleList);

	/**
	 * Gets the response.
	 *
	 * @return the response
	 */
	Response getResponse();

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
	 * Returns the origin raised exception instance.
	 *
	 * @return the origin raised exception instance
	 */
	Throwable getOriginRaisedException();

	/**
	 * Sets the raised exception.
	 *
	 * @param raisedException the new raised exception
	 */
	void setRaisedException(Throwable raisedException);

	/**
	 * Gets the activity context.
	 *
	 * @return the activity context
	 */
	ActivityContext getActivityContext();

	/**
	 * Create a new activity.
	 *
	 * @param <T> the type of the activity
	 * @return the activity object
	 */
	<T extends Activity> T newActivity();

	/**
	 * Gets the translet.
	 *
	 * @return the translet
	 */
	Translet getTranslet();
	
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
	 * Gets the bean registry.
	 *
	 * @return the bean registry
	 */
	BeanRegistry getBeanRegistry();
	
	/**
	 * Gets the template processor.
	 *
	 * @return the template processor
	 */
	TemplateProcessor getTemplateProcessor();

	/**
	 * Gets the setting value in the translet scope.
	 *
	 * @param <T> the type of the value
	 * @param settingName the setting name
	 * @return the setting value
	 */
	<T> T getTransletSetting(String settingName);
	
	/**
	 * Gets the setting value in the request scope.
	 *
	 * @param <T> the type of the value
	 * @param settingName the setting name
	 * @return the setting value
	 */
	<T> T getRequestSetting(String settingName);
	
	/**
	 * Gets the setting value in the response scope.
	 *
	 * @param <T> the type of the value
	 * @param settingName the setting name
	 * @return the setting value
	 */
	<T> T getResponseSetting(String settingName);
	
	/**
	 * Register the aspect rule.
	 *
	 * @param aspectRule the aspect rule
	 */
	void registerAspectRule(AspectRule aspectRule);
	
	/**
	 * Gets the aspect advice bean.
	 *
	 * @param <T> the type of the bean
	 * @param aspectId the aspect id
	 * @return the aspect advice bean object
	 */
	<T> T getAspectAdviceBean(String aspectId);
	
	/**
	 * Gets the request scope.
	 *
	 * @return the request scope
	 */
	Scope getRequestScope();

	/**
	 * Sets the request scope.
	 *
	 * @param requestScope the new request scope
	 */
	void setRequestScope(Scope requestScope);

}
