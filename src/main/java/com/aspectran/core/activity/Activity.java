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
	Class<? extends GenericTranslet> getTransletImplementationClass();

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
	 * Perform activity.
	 */
	void perform();
	
	/**
	 * Perform activity without reponse.
	 */
	void performWithoutResponse();

	/**
	 * Finish the activity.
	 * It must be called before exiting activities.
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
	 * Forced to Execute the aspect advices.
	 *
	 * @param aspectAdviceRuleList the aspect advice rule list
	 */
	void forceExecute(List<AspectAdviceRule> aspectAdviceRuleList);
	
	/**
	 * Returns the process result.
	 *
	 * @return the process result
	 */
	ProcessResult getProcessResult();
	
	/**
	 * Returns a action result  from the process result.
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
	
	void response(Response response);
	
	void responseByContentType(List<ExceptionRule> exceptionRuleList);

	Response getResponse();

	boolean isExceptionRaised();

	Throwable getRaisedException();

	Throwable getOriginRaisedException();

	void setRaisedException(Throwable raisedException);

	ActivityContext getActivityContext();

	<T extends Activity> T newActivity();

	Translet getTranslet();
	
	String getTransletName();

	MethodType getRequestMethod();
	
	ApplicationAdapter getApplicationAdapter();

	SessionAdapter getSessionAdapter();
	
	RequestAdapter getRequestAdapter();
	
	ResponseAdapter getResponseAdapter();

	BeanRegistry getBeanRegistry();
	
	TemplateProcessor getTemplateProcessor();

	<T> T getTransletSetting(String settingName);
	
	<T> T getRequestSetting(String settingName);
	
	<T> T getResponseSetting(String settingName);
	
	void registerAspectRule(AspectRule aspectRule);
	
	<T> T getAspectAdviceBean(String aspectId);
	
	Scope getRequestScope();

	void setRequestScope(Scope requestScope);

}
