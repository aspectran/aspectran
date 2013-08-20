/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.web.servlet;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.core.activity.AspectranActivity;
import com.aspectran.core.context.translet.TransletNotFoundException;
import com.aspectran.web.activity.WebAspectranActivity;

/**
 * Servlet implementation class for Servlet: Translets.
 */
public class IPAddressBlockableActivityServlet extends WebAspectranActivityServlet implements Servlet {

	/** @serial */
	static final long serialVersionUID = -2369788867122156319L;

	private static final Log log = LogFactory.getLog(IPAddressBlockableActivityServlet.class);
	
	private static boolean debugEnabled = log.isDebugEnabled();
	
	private static final String DELIMITERS = " ,;\t\n\r\f";
	
	private Set<String> allowedAddresses;
	
	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	/**
	 * Instantiates a new action servlet.
	 */
	public IPAddressBlockableActivityServlet() {
		super();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() throws ServletException {
		String addresses = getServletConfig().getInitParameter("aspectran:allowedAddresses");

		if(addresses != null) {
			allowedAddresses = new HashSet<String>();

			StringTokenizer st = new StringTokenizer(addresses, DELIMITERS);
			
			while(st.hasMoreTokens()) {
				String token = st.nextToken();
				allowedAddresses.add(token);			
			}
		}
		
		super.init();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try {
			String remoteAddr = req.getRemoteAddr();
		
			if(!isValidAdress(remoteAddr)) {
				if(debugEnabled) {
					log.debug("Access denied '" + remoteAddr + "'.");
				}
					
				res.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			
			String requestUri = req.getRequestURI();

			AspectranActivity activity = new WebAspectranActivity(aspectranContext, req, res);
			activity.run(requestUri);
			
		} catch(TransletNotFoundException e) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			log.error(e.getMessage(), e);
			//e.printStackTrace();
		} catch(Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			log.error(e.getMessage(), e);
			//e.printStackTrace();
		}
	}
	
	/**
	 * Checks if is valid access.
	 * 
	 * @param ipAddress the ip address
	 * 
	 * @return true, if is valid access
	 */
	public boolean isValidAdress(String ipAddress) {
		if(allowedAddresses == null)
			return false;
		
		// IPv4
		int offset = ipAddress.lastIndexOf('.');
		
		if(offset == -1) {
			// IPv6
			offset = ipAddress.lastIndexOf(':');
			
			if(offset == -1)
				return false;
		}
		
		String ipAddressClass = ipAddress.substring(0, offset + 1) + '*';
		
		if(allowedAddresses.contains(ipAddressClass) || allowedAddresses.contains(ipAddress))
			return true;
		
		return false;
	}
	
}