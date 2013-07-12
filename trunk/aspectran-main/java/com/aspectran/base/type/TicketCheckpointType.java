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
package com.aspectran.base.type;

import java.util.HashMap;
import java.util.Map;


/**
 * Ticket Checkpoint.
 * 
 * <p>Created: 2010. 01. 30 오전 2:40:00</p>
 */
public final class TicketCheckpointType extends Type {
	
	/** The "request" ticket checkpoint type. */
	public static final TicketCheckpointType REQUEST;
	
	/** The "response" ticket checkpoint type. */
	public static final TicketCheckpointType RESPONSE;
	
	private static final Map<String, TicketCheckpointType> types;
	
	static {
		REQUEST = new TicketCheckpointType("request");
		RESPONSE = new TicketCheckpointType("response");

		types = new HashMap<String, TicketCheckpointType>();
		types.put(REQUEST.toString(), REQUEST);
		types.put(RESPONSE.toString(), RESPONSE);
	}

	/**
	 * Instantiates a new ticket checkpoint type.
	 * 
	 * @param type the type
	 */
	private TicketCheckpointType(String type) {
		super(type);
	}

	/**
	 * Returns a <code>TicketCheckpointType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the scope type
	 */
	public static TicketCheckpointType valueOf(String type) {
		return types.get(type);
	}
}
