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
import com.aspectran.core.context.rule.ExceptionHandlingRule;
import com.aspectran.core.context.rule.type.JoinpointScopeType;
import com.aspectran.core.context.rule.type.RequestMethodType;
import com.aspectran.core.context.template.TemplateProcessor;

/**
 * The Interface Activity.
 * 
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public interface Activity {

	public Class<? extends Translet> getTransletInterfaceClass();
	
	public Class<? extends CoreTranslet> getTransletImplementClass();

	public void ready(String transletName);
	
	public void ready(String transletName, String restVerb);

	public void ready(String transletName, RequestMethodType requestMethod);
	
	public void perform();
	
	public void performWithoutResponse();

	public void finish();
	
	public void execute(List<AspectAdviceRule> aspectAdviceRuleList);
	
	public void forceExecute(List<AspectAdviceRule> aspectAdviceRuleList);
	
	public ProcessResult getProcessResult();
	
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

	public RequestMethodType getRestVerb();
	
	public ApplicationAdapter getApplicationAdapter();

	public SessionAdapter getSessionAdapter();
	
	public RequestAdapter getRequestAdapter();
	
	public ResponseAdapter getResponseAdapter();

	public BeanRegistry getBeanRegistry();
	
	public TemplateProcessor getTemplateProcessor();

	public <T> T getBean(String id);
	
	public <T> T getBean(Class<T> classType);

	public <T> T getBean(String id, Class<T> classType);

	public <T> T getTransletSetting(String settingName);
	
	public <T> T getRequestSetting(String settingName);
	
	public <T> T getResponseSetting(String settingName);
	
	public void registerAspectRule(AspectRule aspectRule);
	
	public <T> T  getAspectAdviceBean(String aspectId);
	
	public Scope getRequestScope();

	public void setRequestScope(Scope requestScope);

	public JoinpointScopeType getCurrentJoinpointScope();

}
