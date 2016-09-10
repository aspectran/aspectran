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
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.BeanRegistry;
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
	 * Throws an Activity Terminated Exception in order to end the current activity.
	 * @throws ActivityTerminatedException if an activity is terminated during processing
	 */
	void terminate();

	/**
	 * Gets the request http method.
	 *
	 * @return the request method
	 */
	MethodType getRequestMethod();

	/**
	 * Gets the name of the current translet.
	 *
	 * @return the translet name
	 */
	String getTransletName();

	/**
	 * Returns an instance of the current translet.
	 *
	 * @return an instance of the current translet
	 */
	Translet getTranslet();

	/**
	 * Returns the process result.
	 *
	 * @return the process result
	 */
	ProcessResult getProcessResult();

	/**
	 * Returns an action result for the specified action id from the process result,
	 * or {@code null} if the action does not exist.
	 *
	 * @param actionId the specified action id
	 * @return an action result
	 */
	Object getProcessResult(String actionId);

	/**
	 * Determine the request character encoding.
	 *
	 * @return the request character encoding
	 */
	String resolveRequestCharacterEncoding();

	/**
	 * Determine the response character encoding.
	 *
	 * @return the response character encoding
	 */
	String resolveResponseCharacterEncoding();
	
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
	 * Execute the aspect advice.
	 *
	 * @param aspectAdviceRule the aspect advice rule
	 */
	void execute(AspectAdviceRule aspectAdviceRule);
	
	/**
	 * Execute the aspect advice without throw exceptions.
	 *
	 * @param aspectAdviceRule the aspect advice rule
	 */
	void executeWithoutThrow(AspectAdviceRule aspectAdviceRule);
	
	/**
	 * Returns whether the response is reserved.
	 * 
	 * @return true, if the response is reserved
	 */
	boolean isResponseReserved();

	/**
	 * Exception handling.
	 *
	 * @param exceptionRuleList the exception rule list
	 */
	void exceptionHandling(List<ExceptionRule> exceptionRuleList);

	/**
	 * Returns whether the exception was thrown.
	 *
	 * @return true, if is exception raised
	 */
	boolean isExceptionRaised();

	/**
	 * Returns an instance of the currently raised exception.
	 *
	 * @return an instance of the currently raised exception
	 */
	Throwable getRaisedException();

	/**
	 * Returns an instance of the originally raised exception.
	 *
	 * @return an instance of the originally raised exception
	 */
	Throwable getOriginRaisedException();

	/**
	 * Sets an instance of the currently raised exception.
	 *
	 * @param raisedException an instance of the currently raised exception
	 */
	void setRaisedException(Throwable raisedException);

	/**
	 * Gets the activity context.
	 *
	 * @return the activity context
	 */
	ActivityContext getActivityContext();

	/**
	 * Returns the class loader.
	 *
	 * @return the class loader
	 */
	ClassLoader getClassLoader();

	/**
	 * Create a new inner activity.
	 *
	 * @param <T> the type of the activity
	 * @return the activity object
	 */
	<T extends Activity> T newActivity();

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
	<T> T getSetting(String settingName);

	/**
	 * Register an aspect rule dynamically.
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
	
}
