/*
 *  Copyright (c) 2010 Jeong Ju Ho, All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aspectran.core.activity;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.process.ProcessException;
import com.aspectran.core.activity.process.action.ActionExecutionException;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.request.RequestException;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.ForwardingFailedException;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.context.bean.BeanRegistry;
import com.aspectran.core.context.bean.scope.RequestScope;
import com.aspectran.core.context.translet.TransletInstantiationException;
import com.aspectran.core.context.translet.TransletRuleRegistry;
import com.aspectran.core.rule.RequestRule;
import com.aspectran.core.rule.ResponseByContentTypeRule;
import com.aspectran.core.rule.ResponseRule;
import com.aspectran.core.rule.TicketCheckRule;
import com.aspectran.core.rule.TransletRule;
import com.aspectran.core.ticket.TicketCheckActionList;
import com.aspectran.core.ticket.TicketCheckException;
import com.aspectran.core.ticket.TicketCheckRejectedException;
import com.aspectran.core.ticket.action.TicketCheckAction;
import com.aspectran.core.type.ResponseType;
import com.aspectran.core.type.TicketCheckpointType;

/**
 * Action Translator.
 * processes the active request and response.
 * 
 * <p>Created: 2008. 03. 22 오후 5:48:09</p>
 */
public abstract class AbstractAspectranActivity implements AspectranActivity {

	/** The log. */
	private final Log log = LogFactory.getLog(AbstractAspectranActivity.class);
	
	/** The debug enabled. */
	private final boolean debugEnabled = log.isDebugEnabled();

	/** The context. */
	protected final AspectranContext context;
	
	/** The request adapter. */
	private RequestAdapter requestAdapter;

	/** The response adapter. */
	private ResponseAdapter responseAdapter;

	/** The session adapter. */
	private SessionAdapter sessionAdapter;

	/** The translet interface class. */
	private Class<? extends SuperTranslet> transletInterfaceClass;
	
	/** The translet instance class. */
	private Class<? extends AbstractSuperTranslet> transletInstanceClass;

	/** The translet rule. */
	private TransletRule transletRule;
	
	/** The request rule. */
	private RequestRule requestRule;
	
	/** The response rule. */
	private ResponseRule responseRule;
	
	/** The translet. */
	private SuperTranslet translet;
	
	/** The request scope. */
	private RequestScope requestScope;
	
	/** The is response end. */
	private boolean isResponseEnd;
	
	/** The forward translet name. */
	private String forwardTransletName;
	
	/** The enforceable response id. */
	private String multipleTransletResponseId;
	
	private Exception raisedException;

	/** The translet name. */
	private String transletName;
	
	/**
	 * Instantiates a new action translator.
	 *
	 * @param context the translets context
	 */
	public AbstractAspectranActivity(AspectranContext context) {
		this.context = context;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AspectranActivity#getRequestAdapter()
	 */
	public RequestAdapter getRequestAdapter() {
		return requestAdapter;
	}
	
	/**
	 * Sets the request adapter.
	 *
	 * @param requestAdapter the new request adapter
	 */
	protected void setRequestAdapter(RequestAdapter requestAdapter) {
		this.requestAdapter = requestAdapter;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AspectranActivity#getResponseAdapter()
	 */
	public ResponseAdapter getResponseAdapter() {
		return responseAdapter;
	}

	/**
	 * Sets the response adapter.
	 *
	 * @param responseAdapter the new response adapter
	 */
	protected void setResponseAdapter(ResponseAdapter responseAdapter) {
		this.responseAdapter = responseAdapter;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AspectranActivity#getSessionAdapter()
	 */
	public SessionAdapter getSessionAdapter() {
		return sessionAdapter;
	}
	
	/**
	 * Sets the session adapter.
	 *
	 * @param sessionAdapter the new session adapter
	 */
	protected void setSessionAdapter(SessionAdapter sessionAdapter) {
		this.sessionAdapter = sessionAdapter;
	}
	
	/**
	 * Gets the translet interface class.
	 *
	 * @return the translet interface class
	 */
	public Class<? extends SuperTranslet> getTransletInterfaceClass() {
		if(transletRule != null && transletRule.getTransletInterfaceClass() != null)
			return transletRule.getTransletInterfaceClass();
		
		return transletInterfaceClass;
	}

	/**
	 * Sets the translet interface class.
	 *
	 * @param transletInterfaceClass the new translet interface class
	 */
	public void setTransletInterfaceClass(Class<? extends SuperTranslet> transletInterfaceClass) {
		this.transletInterfaceClass = transletInterfaceClass;
	}

	/**
	 * Gets the translet instance class.
	 *
	 * @return the translet instance class
	 */
	public Class<? extends AbstractSuperTranslet> getTransletInstanceClass() {
		if(transletRule != null && transletRule.getTransletInstanceClass() != null)
			return transletRule.getTransletInstanceClass();

		return transletInstanceClass;
	}

	/**
	 * Sets the translet instance class.
	 *
	 * @param transletInstanceClass the new translet instance class
	 */
	public void setTransletInstanceClass(Class<? extends AbstractSuperTranslet> transletInstanceClass) {
		this.transletInstanceClass = transletInstanceClass;
	}

	public SuperTranslet getSuperTranslet() {
		return translet;
	}
	
	public void run(String transletName) throws RequestException, ProcessException, ResponseException {
		init(transletName);
		request();
		process();
		response();
	}

	public void init(String transletName) {
		TransletRule transletRule = context.getTransletRuleRegistry().getTransletRule(transletName);

		if(transletRule.getMultipleTransletResponseId() != null) {
			multipleTransletResponseId = transletRule.getMultipleTransletResponseId();
		}
		
		Class<? extends SuperTranslet> transletInterfaceClass = getTransletInterfaceClass();
		Class<? extends AbstractSuperTranslet> transletInstanceClass = getTransletInstanceClass();

		//create translet instance
		try {
			Constructor<?> transletInstanceConstructor = transletInstanceClass.getConstructor(AspectranActivity.class);
			Object[] args = new Object[] { this };
			translet = (SuperTranslet)transletInstanceConstructor.newInstance(args);
		} catch(Exception e) {
			throw new TransletInstantiationException(transletInterfaceClass, transletInstanceClass, e);
		}
		
		this.transletName = transletName;
		this.transletRule = transletRule;
		this.requestRule = transletRule.getRequestRule();
		this.responseRule = transletRule.getResponseRule();
	}
	
	abstract public void request() throws RequestException;
	
	public ProcessResult process() throws ProcessException {
		if(debugEnabled) {
			log.debug(">> Processing for path '" + transletName + "'");
		}

		try {
//			TicketCheckActionList ticketCheckActionList = transletRule.getTicketCheckActionList();
//			int requestCheckpointCount = 0;
//			int responseCheckpointCount = 0;
//			
//			if(ticketCheckActionList != null) {
//				requestCheckpointCount = ticketCheckActionList.getRequestCheckpointCount();
//				responseCheckpointCount = ticketCheckActionList.size() - requestCheckpointCount;
//			}
//			
//			// ticket check: request-checkpoint
//			if(!ignoreTicket && requestCheckpointCount > 0) {
//				checkTicket(ticketCheckActionList, TicketCheckpointType.REQUEST);
//			}
			
//			if(!isResponseEnd) {
				// execute action on contents area
				ContentList contentList = transletRule.getContentList();
				
				if(contentList != null) {
					for(ActionList actionList : contentList) {
						execute(actionList);
						
						if(isResponseEnd)
							break;
					}
				}
//			}
			
			if(!isResponseEnd) {
				// execute action on response area
				Responsible response = getResponse();
				
				if(response != null) {
					ActionList actionList = response.getActionList();
					
					if(actionList != null)
						execute(actionList);
				}
			}

//			// ticket check: response-checkpoint
//			if(!ignoreTicket && responseCheckpointCount > 0) {
//				checkTicket(ticketCheckActionList, TicketCheckpointType.RESPONSE);
//			}
		} catch(Exception e) {
			if(debugEnabled) {
				log.error("An error occurred while executing actions. Cause: " + e, e);
			}

			setRaisedException(e);
			
			if(transletRule.getExceptionHandleRule() != null) {
				responseByContentType(transletRule.getExceptionHandleRule());

				// execute action on response area
				Responsible response = getResponse();

				if(response != null) {
					ActionList actionList = response.getActionList();
					
					if(actionList != null)
						execute(actionList);
				}
				
				return translet.getProcessResult();
			} else {
				throw new ProcessException("An error occurred while processing response by content-type. Cause: " + e, e);
			}
		} finally {
			if(requestScope != null) {
				//TODO
				requestScope.destroy();
			}
		}
		
		return translet.getProcessResult();
	}
	
	public ProcessResult getProcessResult() {
		if(translet == null)
			return null;
		
		return translet.getProcessResult();
	}
	
	protected void response() throws ResponseException {
		if(debugEnabled) {
			log.debug(">> Responsing for path '" + transletName + "'");
		}

		Responsible res = getResponse();
		
		if(res != null)
			response(res);
		
		if(forwardTransletName != null)
			forward();
	}
	
	/**
	 * Execute.
	 *
	 * @param actionList the action list
	 * @throws ActionExecutionException the action execution exception
	 */
	private void execute(ActionList actionList) throws ActionExecutionException {
		if(debugEnabled) {
			log.debug("Content " + actionList.toString());
		}
//
//		if(isResponseEnd) {
//			if(debugEnabled) {
//				log.debug("Response has already ended.");
//			}
//
//			return;
//		}
		
		ContentResult contentResult;

		if(!actionList.isHidden()) {
			contentResult = new ContentResult();
			contentResult.setContentId(actionList.getContentId());
		} else {
			contentResult = null;
		}
		
		for(Executable action : actionList) {
			if(debugEnabled) {
				log.debug("Execute " + action.toString());
			}
			
			Object resultValue = action.execute(this);

			if(debugEnabled) {
				log.debug("  Result " + resultValue);
			}

			if(contentResult != null && !action.isHidden() && resultValue != ActionResult.NO_RESULT) {
				ActionResult actionResult = new ActionResult();
				actionResult.setActionId(action.getId());
				actionResult.setResultValue(resultValue);
				
				contentResult.addActionResult(actionResult);
			}
			
			if(isResponseEnd)
				break;
		}

		if(contentResult != null)
			translet.addContentResult(contentResult);
	}
	
	/**
	 * Check ticket.
	 *
	 * @param ticketCheckActionList the ticket bean action list
	 * @param checkpoint the check point
	 * @throws TicketCheckException the ticket check exception
	 */
	private void checkTicket(TicketCheckActionList ticketCheckActionList, TicketCheckpointType checkpoint) throws TicketCheckException {
		try {
			for(TicketCheckAction ticketCheckAction : ticketCheckActionList) {
				TicketCheckRule ticketCheckRule = ticketCheckAction.getTicketCheckRule();
				
				if(ticketCheckRule.getTicketCheckpoint() == checkpoint) {
					if(debugEnabled) {
						log.debug("Check ticket " + ticketCheckAction.toString());
					}
					
					Object result = ticketCheckAction.execute(this);
					
					if(result == Boolean.FALSE) {
						if(ticketCheckRule.getRejectInvalidTicket() == Boolean.TRUE) {
							if(debugEnabled) {
								log.debug("Rejected by ticket: " + ticketCheckRule);
							}
							
							ResponseByContentTypeRule responseByContentTypeRule = ticketCheckRule.getTicketCheckcaseRule().getResponseByContentTypeRule();;
							
							if(responseByContentTypeRule != null) {
								responseByContentType(responseByContentTypeRule);
								return;
							}
							
							throw new TicketCheckRejectedException(ticketCheckRule);
						}
					}
					
					if(isResponseEnd)
						break;
				}
			}
		} catch(ActionExecutionException e) {
			throw new TicketCheckException(e);
		}
	}
	
	/**
	 * Response.
	 * 
	 * @param res the responsible
	 * 
	 * @throws ResponseException the response exception
	 */
	public void response(Responsible res) throws ResponseException {
//		if(responsible == null)
//			throw new IllegalArgumentException("responsible is null.");

		res.response(this);
		
		if(res.getResponseType() == ResponseType.FORWARD) {
			ForwardResponse forwardResponse = (ForwardResponse)res;
			String forwardTransletName = forwardResponse.getForwardResponseRule().getTransletName();
			setForwardTransletName(forwardTransletName);
		}
		
		responseEnd();
	}
	
	/**
	 * Forwarding.
	 *
	 * @throws ResponseException the active response exception
	 */
	private void forward() throws ResponseException {
		if(debugEnabled) {
			log.debug("Forwarding for translet '" + forwardTransletName + "'");
		}
		
		try {
			ProcessResult processResult = translet.getProcessResult();
			init(forwardTransletName);
			translet.setProcessResult(processResult);
			request();
			process();
			response();
		} catch(Exception e) {
			throw new ForwardingFailedException("Forwarding failed for path '" + forwardTransletName + "'", e);
		}
	}
	
	/**
	 * Response by content type.
	 *
	 * @param responseByContentTypeRule the response by content type rule
	 */
	private void responseByContentType(ResponseByContentTypeRule responseByContentTypeRule) {
		Responsible response = getResponse();
		
		if(response != null && response.getContentType() != null) {
			ResponseRule newResponseRule = responseRule.newResponseRule(responseByContentTypeRule.getResponseMap());
			newResponseRule.setDefaultResponseId(response.getContentType().toString());
			responseRule = newResponseRule;
		}
		
		if(debugEnabled) {
			log.debug("Response by content type: " + responseRule);
		}

		multipleTransletResponseId = null;
		translet.setProcessResult(null);
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.activity.Activity#getForwardTransletName()
	 */
	public String getForwardTransletName() {
		return forwardTransletName;
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.activity.Activity#setForwardTransletName(java.lang.String)
	 */
	public void setForwardTransletName(String forwardTransletName) {
		this.forwardTransletName = forwardTransletName;
	}
	
//	/**
//	 * 응답 시작(계속).
//	 */
//	public void responseStart() {
//		isResponseEnd = false;
//		forwardingPath = null;
//	}
	
	/**
	 * 응답 강제 종료.
	 */
	public void responseEnd() {
		if(debugEnabled) {
			log.debug("Response terminated");
		}
		
		isResponseEnd = true;
	}
	
	/**
	 * 응답 종료 여부 반환.
	 * 
	 * @return true, if checks if is response end
	 */
	public boolean isResponseEnd() {
		return isResponseEnd;
	}

	/**
	 * Checks if is exception raised.
	 *
	 * @return true, if is exception raised
	 */
	public boolean isExceptionRaised() {
		return (raisedException == null);
	}
	
	public Exception getRaisedException() {
		return raisedException;
	}

	public void setRaisedException(Exception raisedException) {
		this.raisedException = raisedException;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AspectranActivity#getContext()
	 */
	public AspectranContext getContext() {
		return context;
	}
	
	/**
	 * The <code>response</code> will return to find.
	 *
	 * @param responseId the response id
	 * @return the response
	 */
	public Responsible getResponse(String responseId) {
		if(responseRule == null)
			return null;
		
		return responseRule.getResponseMap().get(responseId);
	}
	
	/**
	 * The <code>response</code> will return to find.
	 *
	 * @return the response
	 */
	public Responsible getResponse() {
		if(responseRule == null)
			return null;

		String responseId = null;
		
		if(multipleTransletResponseId != null && multipleTransletResponseId.length() > 0) {
			if(responseRule.getResponseMap().containsKey(multipleTransletResponseId))
				responseId = multipleTransletResponseId;
		} else {
			responseId = responseRule.getDefaultResponseId();
		}

		if(responseId == null || responseId.length() == 0) {
			if(responseRule.getResponseMap().size() == 1)
				return responseRule.getResponseMap().get(0);
		}
		
		if(responseId == null || responseId.length() == 0) {
			responseId = ResponseRule.DEFAULT_ID;
		}
		
		return responseRule.getResponseMap().get(responseId);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AspectranActivity#getBean(java.lang.String)
	 */
	public Object getBean(String id) {
		return context.getBeanRegistry().getBean(id, this);
	}
	
	public abstract AspectranActivity newAspectranActivity();
	
	/**
	 * Gets the application adapter.
	 *
	 * @return the application adapter
	 */
	public ApplicationAdapter getApplicationAdapter() {
		return context.getApplicationAdapter();
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AspectranActivity#getTransletName()
	 */
	public String getTransletName() {
		return transletName;
	}
	
	/**
	 * Sets the translet name.
	 *
	 * @param transletName the new translet name
	 */
	protected void setTransletName(String transletName) {
		this.transletName = transletName;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AspectranActivity#getRequestScope()
	 */
	public RequestScope getRequestScope() {
		return requestScope;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AspectranActivity#setRequestScope(com.aspectran.core.context.bean.scope.RequestScope)
	 */
	public void setRequestScope(RequestScope requestScope) {
		this.requestScope = requestScope;
	}

	/**
	 * Gets the translet rule.
	 *
	 * @return the translet rule
	 */
	public TransletRule getTransletRule() {
		return transletRule;
	}

	/**
	 * Sets the translet rule.
	 *
	 * @param transletRule the new translet rule
	 */
	public void setTransletRule(TransletRule transletRule) {
		this.transletRule = transletRule;
	}

	/**
	 * Gets the request rule.
	 *
	 * @return the request rule
	 */
	public RequestRule getRequestRule() {
		return requestRule;
	}

	/**
	 * Sets the request rule.
	 *
	 * @param requestRule the new request rule
	 */
	public void setRequestRule(RequestRule requestRule) {
		this.requestRule = requestRule;
	}

	/**
	 * Gets the response rule.
	 *
	 * @return the response rule
	 */
	public ResponseRule getResponseRule() {
		return responseRule;
	}

	/**
	 * Sets the response rule.
	 *
	 * @param responseRule the new response rule
	 */
	public void setResponseRule(ResponseRule responseRule) {
		this.responseRule = responseRule;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AspectranActivity#getBeanRegistry()
	 */
	public BeanRegistry getBeanRegistry() {
		return context.getBeanRegistry();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AspectranActivity#getTransletRegistry()
	 */
	public TransletRuleRegistry getTransletRegistry() {
		return context.getTransletRuleRegistry();
	}
	
}
