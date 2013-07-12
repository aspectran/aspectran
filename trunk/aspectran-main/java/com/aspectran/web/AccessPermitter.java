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
package com.aspectran.web;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * 접근허용 제어기
 * 
 * <p>Created: 2008. 03. 24 오후 11:40:58</p>
 */
public class AccessPermitter {
	
	private static final String DELIMITERS = " ,;\t\n\r\f";
	
	private Set<String> allowedAddresses;
	
	private Set<String> deniedAddresses;
	
	/**
	 * Instantiates a new access permitter.
	 * 
	 * @param accessPermitRule the access permit rule
	 */
	public AccessPermitter() {
	}
	
	/**
	 * Adds the allowed address.
	 * 
	 * @param ipAddress the ip address
	 */
	public void addAllowedAddress(String ipAddress) {
		if(deniedAddresses != null) {
			deniedAddresses.clear();
			deniedAddresses = null;
		}
		
		if(allowedAddresses == null)
			allowedAddresses = new HashSet<String>();
		
		allowedAddresses.add(ipAddress);
	}

	/**
	 * Adds the denied address.
	 * 
	 * @param ipAddress the ip address
	 */
	public void addDeniedAddress(String ipAddress) {
		if(allowedAddresses != null) {
			allowedAddresses.clear();
			allowedAddresses = null;
		}
		
		if(deniedAddresses == null)
			deniedAddresses = new HashSet<String>();
		
		deniedAddresses.add(ipAddress);
	}
	
	/**
	 * Sets the allowed addresses.
	 * 
	 * @param ipAddresses the new allowed addresses
	 */
	public void setAllowedAddresses(String ipAddresses) {
		StringTokenizer st = new StringTokenizer(ipAddresses, DELIMITERS);
		
		String token;
		
		while(st.hasMoreTokens()) {
			token = st.nextToken();
			
			addAllowedAddress(token);
		}
	}
	
	/**
	 * Sets the denied addresses.
	 * 
	 * @param ipAddresses the new denied addresses
	 */
	public void setDeniedAddresses(String ipAddresses) {
		StringTokenizer st = new StringTokenizer(ipAddresses, DELIMITERS);
		
		String token;
		
		while(st.hasMoreTokens()) {
			token = st.nextToken();
			
			addDeniedAddress(token);
		}
	}
	
	/**
	 * Checks if is valid access.
	 * 
	 * @param ipAddress the ip address
	 * 
	 * @return true, if is valid access
	 */
	public boolean isValidAccess(String ipAddress) {
		// IPv4
		int offset = ipAddress.lastIndexOf('.');
		
		if(offset == -1) {
			// IPv6
			offset = ipAddress.lastIndexOf(':');
			
			if(offset == -1)
				return false;
		}
		
		String ipAddressClass = ipAddress.substring(0, offset + 1) + '*';
		
		if(deniedAddresses != null) {
			if(deniedAddresses.contains(ipAddress) || deniedAddresses.contains(ipAddressClass))
				return false;
			else
				return true;
		}
		
		if(allowedAddresses != null) {
			if(allowedAddresses.contains(ipAddressClass) || allowedAddresses.contains(ipAddress))
				return true;
			else
				return false;
		}
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{allowedAddresses=").append(allowedAddresses);
		sb.append(", deniedAddresses=").append(deniedAddresses);
		sb.append("}");
		
		return sb.toString();
	}
}
