/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.activity.response.transform.apon;

import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.util.BeanUtils;
import com.aspectran.core.util.apon.GenericParameters;
import com.aspectran.core.util.apon.Parameters;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * Converts a ProcessResult object to a APON object.
 * 
 * <p>Created: 2015. 03. 16 오후 11:14:29</p>
 */
public class ContentsAponAssembler {
	
	public static Parameters assemble(ProcessResult processResult) throws InvocationTargetException {
		if(processResult == null || processResult.isEmpty()) {
			return null;
		}

		if(processResult.size() == 1) {
			ContentResult contentResult = processResult.get(0);
			
			if(contentResult.getName() == null && contentResult.size() == 1) {
				ActionResult actionResult = contentResult.get(0);
				Object resultValue = actionResult.getResultValue();
				
				if(actionResult.getActionId() == null) {
					if(resultValue instanceof Parameters) {
						return (Parameters)resultValue;
					} else {
						return null;
					}
				} else {
					Parameters container = new GenericParameters();
					putValue(container, actionResult.getActionId(), resultValue);
					return container;
				}
			}
		}
		
		Parameters container = new GenericParameters();
		Iterator<ContentResult> iter = processResult.iterator();

		while(iter.hasNext()) {
			ContentResult contentResult = iter.next();
			assemble(contentResult, container);
		}
	
		return container;
	}

	private static void assemble(ContentResult contentResult, Parameters container) throws InvocationTargetException {
		if(contentResult.isEmpty()) {
			return;
		}
		
		if(contentResult.getName() != null) {
			Parameters p = new GenericParameters();
			container.putValue(contentResult.getName(), p);
			container = p;
		}

		Iterator<ActionResult> iter = contentResult.iterator();

		while(iter.hasNext()) {
			ActionResult actionResult = iter.next();
			
			if(actionResult.getActionId() != null) {
				Object resultValue = actionResult.getResultValue();
				putValue(container, actionResult.getActionId(), resultValue);
			}
		}
	}
	
	private static void putValue(Parameters container, String name, Object value) throws InvocationTargetException {
		if(value == null)
			return;
		
		if(value instanceof Collection<?>) {
			@SuppressWarnings("unchecked")
			Iterator<Object> iter = ((Collection<Object>)value).iterator();
		
			while(iter.hasNext()) {
				container.putValue(name, assemble(iter.next()));
			}
		} else if(value.getClass().isArray()) {
			int len = Array.getLength(value);

			for(int i = 0; i < len; i++) {
				container.putValue(name, assemble(Array.get(value, i)));
			}
		} else {
			container.putValue(name, assemble(value));
		}
	}
	
	private static Object assemble(Object object) throws InvocationTargetException {
		if(object instanceof Parameters ||
				object instanceof String ||
				object instanceof Number ||
				object instanceof Boolean ||
				object instanceof Date) {
			return object;
		} else if(object instanceof Map<?, ?>) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>)object;
			Parameters p = new GenericParameters();
			
			for(Map.Entry<String, Object> entry : map.entrySet()) {
				String name = entry.getKey();
				Object value = assemble(entry.getValue());
				
				p.putValue(name, value);
			}
			
			return p;
		} else if(object instanceof Collection<?>) {
			return object.toString();
		} else if(object.getClass().isArray()) {
			return object.toString();
		} else {
			String[] readablePropertyNames = BeanUtils.getReadablePropertyNames(object);
	
			if(readablePropertyNames != null && readablePropertyNames.length > 0) {
				Parameters p = new GenericParameters();
				
				for(String name : readablePropertyNames) {
					Object value = BeanUtils.getObject(object, name);
					
	
					if(object == value || object.equals(value))
						continue;
					
					p.putValue(name, value);
				}
				
				return p;
			} else {
				return object.toString();
			}
		}
	}
	
}
