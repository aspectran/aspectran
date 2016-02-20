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
import com.aspectran.core.context.bean.BeanRegistrySupport;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.ExceptionHandlingRule;
import com.aspectran.core.context.rule.type.JoinpointScopeType;
import com.aspectran.core.context.rule.type.RequestMethodType;
import com.aspectran.core.context.template.TemplateProcessor;

/**
 * The Interface Activity.
 * 
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public interface Activity extends BeanRegistrySupport {

	/**
	 * Return the interface class for {@code Translet}.
	 *
	 * @return the translet interface class
	 */
	public Class<? extends Translet> getTransletInterfaceClass();
	
	/**
	 * Return the implementation class for {@code Translet}.
	 *
	 * @return the translet implementation class
	 */
	public Class<? extends CoreTranslet> getTransletImplementationClass();

	/**
	 * Preparation for the activity.
	 *
	 * @param transletName the translet name
	 */
	public void ready(String transletName);
	
	/**
	 * Preparation for the activity.
	 *
	 * @param transletName the translet name
	 * @param requestMethod the request method
	 */
	public void ready(String transletName, String requestMethod);

	/**
	 * Preparation for the activity.
	 *
	 * @param transletName the translet name
	 * @param requestMethod the request method
	 */
	public void ready(String transletName, RequestMethodType requestMethod);
	
	/**
	 * Perform activity.
	 */
	public void perform();
	
	/**
	 * Perform activity without reponse.
	 */
	public void performWithoutResponse();

	/**
	 * Finish the activity.
	 * It must be called before exiting activities.
	 */
	public void finish();

	/**
	 * Determine request character encoding.
	 *
	 * @return the request character encoding
	 */
	public String determineRequestCharacterEncoding();

	/**
	 * Determine response character encoding.
	 *
	 * @return the response character encoding
	 */
	public String determineResponseCharacterEncoding();
	
	/**
	 * Execute the aspect advices.
	 *
	 * @param aspectAdviceRuleList the aspect advice rule list
	 */
	public void execute(List<AspectAdviceRule> aspectAdviceRuleList);
	
	/**
	 * Forced to Execute the aspect advices.
	 *
	 * @param aspectAdviceRuleList the aspect advice rule list
	 */
	public void forceExecute(List<AspectAdviceRule> aspectAdviceRuleList);
	
	/**
	 * Returns the process result.
	 *
	 * @return the process result
	 */
	public ProcessResult getProcessResult();
	
	/**
	 * Returns a action result  from the process result.
	 *
	 * @param actionId the specified action id
	 * @return the action result
	 */
	public Object getProcessResult(String actionId);
	
	/**
	 * Returns the forwarding destination translet name.
	 *
	 * @return the forwarding destination translet name
	 */
	public String getForwardTransletName();
	
	/**
	 * Returns whether the current activity is completed or terminated.
	 * 
	 * @return true, if the current activity is completed or terminated
	 */
	public boolean isActivityEnded();

	/**
	 * Stop the activity and responds immediately.
	 */
	public void activityEnd();
	
	public void response(Response response);
	
	public void responseByContentType(List<ExceptionHandlingRule> exceptionHandlingRuleList);

	public Response getResponse();

	public boolean isExceptionRaised();

	public Exception getRaisedException();

	public void setRaisedException(Exception raisedException);

	public ActivityContext getActivityContext();

	public <T extends Activity> T newActivity();

	public Translet getTranslet();
	
	public String getTransletName();

	public RequestMethodType getRequestMethod();
	
	public ApplicationAdapter getApplicationAdapter();

	public SessionAdapter getSessionAdapter();
	
	public RequestAdapter getRequestAdapter();
	
	public ResponseAdapter getResponseAdapter();

	public BeanRegistry getBeanRegistry();
	
	public TemplateProcessor getTemplateProcessor();

	public <T> T getTransletSetting(String settingName);
	
	public <T> T getRequestSetting(String settingName);
	
	public <T> T getResponseSetting(String settingName);
	
	public void registerAspectRule(AspectRule aspectRule);
	
	public <T> T  getAspectAdviceBean(String aspectId);
	
	public Scope getRequestScope();

	public void setRequestScope(Scope requestScope);

	public JoinpointScopeType getCurrentJoinpointScope();

}
