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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.base.adapter.ApplicationAdapter;
import com.aspectran.base.adapter.RequestAdapter;
import com.aspectran.base.adapter.ResponseAdapter;
import com.aspectran.base.adapter.SessionAdapter;
import com.aspectran.base.context.AspectranContext;
import com.aspectran.base.rule.MultiActivityTransletRule;
import com.aspectran.base.rule.RequestRule;
import com.aspectran.base.rule.ResponseByContentTypeRule;
import com.aspectran.base.rule.ResponseRule;
import com.aspectran.base.rule.TicketCheckRule;
import com.aspectran.base.rule.TransletRule;
import com.aspectran.base.type.ResponseType;
import com.aspectran.base.type.TicketCheckpointType;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.process.ProcessException;
import com.aspectran.core.activity.process.action.ActionExecutionException;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.request.RequestException;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.ForwardingFailedException;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.activity.ticket.TicketCheckActionList;
import com.aspectran.core.activity.ticket.TicketCheckException;
import com.aspectran.core.activity.ticket.TicketCheckRejectedException;
import com.aspectran.core.activity.ticket.action.TicketCheckAction;
import com.aspectran.core.bean.registry.BeanRegistry;
import com.aspectran.core.bean.scope.RequestScope;
import com.aspectran.core.translet.AbstractSuperTranslet;
import com.aspectran.core.translet.SuperTranslet;
import com.aspectran.core.translet.registry.TransletNotFoundException;
import com.aspectran.web.activity.AspectranWebTranslet;

/**
 * Action Translator.
 * processes the active request and response.
 * 
 * <p>Created: 2008. 03. 22 오후 5:48:09</p>
 */
public abstract class AbstractAspectranActivity implements AspectranActivity {

	private final Log log = LogFactory.getLog(AbstractAspectranActivity.class);
	
	private final boolean debugEnabled = log.isDebugEnabled();

	protected final AspectranContext context;
	
	protected Class<? extends SuperTranslet> transletInterface;
	
	protected Object transletInstance;
	
	private RequestAdapter requestAdapter;

	private ResponseAdapter responseAdapter;

	private SessionAdapter sessionAdapter;
	
	private TransletRule transletRule;
	
	private RequestRule requestRule;
	
	private ResponseRule responseRule;
	
	private AspectranWebTranslet translet;
	
	private RequestScope requestScope;
	
	private boolean isResponseEnd;
	
	private String forwardTransletName;
	
	private String enforceableResponseId;
	
	private boolean exceptionRaised;
	
	private String transletName;

	/**
	 * Instantiates a new action translator.
	 * 
	 * @param context the translets context
	 * @param output the output
	 * @param ignoreTicket the ignore ticket
	 */
	public AbstractAspectranActivity(AspectranContext context) {
		this.context = context;
	}

	public RequestAdapter getRequestAdapter() {
		return requestAdapter;
	}
	
	protected void setRequestAdapter(RequestAdapter requestAdapter) {
		this.requestAdapter = requestAdapter;
	}
	
	public ResponseAdapter getResponseAdapter() {
		return responseAdapter;
	}

	protected void setResponseAdapter(ResponseAdapter responseAdapter) {
		this.responseAdapter = responseAdapter;
	}

	public SessionAdapter getSessionAdapter() {
		return sessionAdapter;
	}
	
	protected void setSessionAdapter(SessionAdapter sessionAdapter) {
		this.sessionAdapter = sessionAdapter;
	}
	
	public Class<? extends SuperTranslet> getTransletInterface() {
		return transletInterface;
	}

	protected void setTransletInterface(Class<? extends SuperTranslet> transletInterface) {
		this.transletInterface = transletInterface;
	}
	
	public Object getTransletInstance() {
		return transletInstance;
	}

	protected void setTransletInstance(Object transletInstance) {
		this.transletInstance = transletInstance;
	}
	
	public void request(String transletName) throws RequestException {
		if(debugEnabled) {
			log.debug(">> Requesting for translet name '" + transletName + "'");
		}
		
		TransletRule transletRule = getTransletRule(transletName);
		
		if(transletRule == null)
			throw new TransletNotFoundException();

		this.transletName = transletName;
		
		this.transletRule = transletRule;
		this.requestRule = transletRule.getRequestRule();
		this.responseRule = transletRule.getResponseRule();
		
		try {
			if(requestAdapter != null && requestRule.getCharacterEncoding() != null)
				requestAdapter.setCharacterEncoding(requestRule.getCharacterEncoding());
			
			if(responseAdapter != null && responseRule.getCharacterEncoding() != null)
				responseAdapter.setCharacterEncoding(responseRule.getCharacterEncoding());
		} catch(UnsupportedEncodingException e) {
			throw new RequestException(e);
		}
	}
	
	public void process() throws ProcessException {
		process(false);
	}
	
	public void process(boolean ignoreTicket) throws ProcessException {
		if(debugEnabled) {
			log.debug(">> Processing for path '" + transletName + "'");
		}

		try {
			TicketCheckActionList ticketCheckActionList = transletRule.getTicketCheckActionList();
			int requestCheckpointCount = 0;
			int responseCheckpointCount = 0;
			
			if(ticketCheckActionList != null) {
				requestCheckpointCount = ticketCheckActionList.getRequestCheckpointCount();
				responseCheckpointCount = ticketCheckActionList.size() - requestCheckpointCount;
			}
			
			// ticket check: request-checkpoint
			if(!ignoreTicket && requestCheckpointCount > 0) {
				checkTicket(ticketCheckActionList, TicketCheckpointType.REQUEST);
			}
			
			if(!isResponseEnd) {
				// execute action on contents area
				ContentList contentList = transletRule.getContentList();
				
				if(contentList != null) {
					for(ActionList actionList : contentList) {
						execute(actionList);
						
						if(isResponseEnd)
							break;
					}
				}
			}
			
			if(!isResponseEnd) {
				// execute action on response area
				Responsible response = getResponse();
				
				if(response != null) {
					ActionList actionList = response.getActionList();
					
					if(actionList != null)
						execute(actionList);
				}
			}

			// ticket check: response-checkpoint
			if(!ignoreTicket && responseCheckpointCount > 0) {
				checkTicket(ticketCheckActionList, TicketCheckpointType.RESPONSE);
			}
		} catch(Exception e) {
			if(debugEnabled) {
				log.error("An error occurred while executing actions. Cause: " + e, e);
			}

			setExceptionRaised(true);
			
			if(transletRule.getExceptionHandleRule() != null) {
				responseByContentType(transletRule.getExceptionHandleRule());

				// execute action on response area
				Responsible response = getResponse();

				if(response != null) {
					ActionList actionList = response.getActionList();
					
					if(actionList != null)
						execute(actionList);
				}
				
				return;
			} else {
				throw new ProcessException("An error occurred while processing response by content-type. Cause: " + e, e);
			}
		} finally {
			if(requestScope != null) {
				//TODO
				requestScope.destroy();
			}
		}
	}
	
	public void response() throws ResponseException {
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
	 * @param translet the translet
	 * @param actionList the action list
	 * @throws Exception the exception
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
	 * @param translet the translet
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
	 * @param path the path
	 * @param translet the translet
	 * 
	 * @throws ProcessException the process exception
	 * @throws ResponseException the active response exception
	 */
	private void forward() throws ResponseException {
		if(debugEnabled) {
			log.debug("Forwarding for path '" + forwardTransletName + "'");
		}
		
		try {
			request(forwardTransletName);
			process();
			response();
		} catch(Exception e) {
			throw new ForwardingFailedException("Forwarding failed for path '" + forwardTransletName + "'", e);
		}
	}
	
	protected TransletRule getTransletRule(String transletName) {
		TransletRule transletRule = context.getTransletRule(transletName);

		// TODO check multiActivityEnable
		if(transletRule == null && context.isMultiActivityEnable()) {
			MultiActivityTransletRule matr = context.getMultiActivityTransletRule(transletName);
			
			if(matr != null) {
				transletRule = matr.getTransletRule();
				enforceableResponseId = matr.getResponseId();
			}
		}
		
		return transletRule;
	}
	
	/**
	 * Response by content type.
	 *
	 * @param responseByContentTypeRule the response by content type rule
	 */
	private void responseByContentType(ResponseByContentTypeRule responseByContentTypeRule) {
		Responsible response = getResponse();
		
		if(response != null && response.getContentType() != null) {
			responseRule.setResponseMap(responseByContentTypeRule.getResponseMap());
			responseRule.setDefaultResponseId(response.getContentType().toString());
		}
		
		if(debugEnabled) {
			log.debug("Response by content type: " + responseRule);
		}

		enforceableResponseId = null;
		translet.setProcessResult(null);
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.activity.Activity#getEnforceableResponseId()
	 */
	public String getEnforceableResponseId() {
		return enforceableResponseId;
	}

	/**
	 * Sets the enforceable response id.
	 *
	 * @param enforceableResponseId the new enforceable response id
	 */
	public void setEnforceableResponseId(String enforceableResponseId) {
		this.enforceableResponseId = enforceableResponseId;
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

	public boolean isExceptionRaised() {
		return exceptionRaised;
	}

	public void setExceptionRaised(boolean exceptionRaised) {
		this.exceptionRaised = exceptionRaised;
	}
	
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
		
		if(enforceableResponseId != null && enforceableResponseId.length() > 0) {
			if(responseRule.getResponseMap().containsKey(enforceableResponseId))
				responseId = enforceableResponseId;
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
	
	public String getTransletName() {
		return transletName;
	}
	
	protected void setTransletName(String transletName) {
		this.transletName = transletName;
	}

	public RequestScope getRequestScope() {
		return requestScope;
	}

	public void setRequestScope(RequestScope requestScope) {
		this.requestScope = requestScope;
	}

	public TransletRule getTransletRule() {
		return transletRule;
	}

	public void setTransletRule(TransletRule transletRule) {
		this.transletRule = transletRule;
	}

	public RequestRule getRequestRule() {
		return requestRule;
	}

	public void setRequestRule(RequestRule requestRule) {
		this.requestRule = requestRule;
	}

	public ResponseRule getResponseRule() {
		return responseRule;
	}

	public void setResponseRule(ResponseRule responseRule) {
		this.responseRule = responseRule;
	}
	
	/**
	 * Gets the bean registry.
	 *
	 * @return the bean registry
	 */
	public BeanRegistry getBeanRegistry() {
		return context.getBeanRegistry();
	}
}
