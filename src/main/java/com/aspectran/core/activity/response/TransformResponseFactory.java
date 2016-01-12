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
package com.aspectran.core.activity.response;

import com.aspectran.core.activity.response.transform.JsonTransform;
import com.aspectran.core.activity.response.transform.TextTransform;
import com.aspectran.core.activity.response.transform.XmlTransform;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.type.TransformType;

/**
 * The Class TransformResponseFactory.
 * 
 * @author Juho Jeong
 * @since 2011. 3. 12.
 */
public class TransformResponseFactory {

	public static Response getResponse(TransformRule transformRule) {
		TransformType type = transformRule.getTransformType();
		Response res = null;
		
		if(type == TransformType.XML_TRANSFORM) {
			res = new XmlTransform(transformRule);
		} else if(type == TransformType.XSL_TRANSFORM) {
			res = new XmlTransform(transformRule);
		} else if(type == TransformType.JSON_TRANSFORM) {
			res = new JsonTransform(transformRule);
		} else if(type == TransformType.TEXT_TRANSFORM) {
			res = new TextTransform(transformRule);
		} else {
			throw new ResponseNotFoundException("transform response is not found. transformRule " + transformRule);
		}
		
		return res;
	}
	
}
