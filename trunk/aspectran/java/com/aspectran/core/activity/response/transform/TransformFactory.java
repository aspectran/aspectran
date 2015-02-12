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
package com.aspectran.core.activity.response.transform;

import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.type.ContentType;
import com.aspectran.core.context.rule.type.TransformType;

/**
 * <p>Created: 2008. 03. 22 오후 5:51:58</p>
 */
public class TransformFactory {

	/**
	 * Creates the transformer.
	 * 
	 * @param tr the tr
	 * 
	 * @return the transform response
	 */
	public static Responsible createTransform(TransformRule tr) {
		TransformType tranformType = tr.getTransformType();
		
		Responsible transform = null;
		
		if(tranformType == TransformType.XSL_TRANSFORM) {
			transform = new XslTransform(tr);
		} else if(tranformType == TransformType.XML_TRANSFORM) {
			if(tr.getContentType() == null)
				tr.setContentType(ContentType.TEXT_XML.toString());

			transform = new XmlTransform(tr);
		} else if(tranformType == TransformType.TEXT_TRANSFORM) {
			transform = new TextTransform(tr);
		} else if(tranformType == TransformType.JSON_TRANSFORM) {
			if(tr.getContentType() == null)
				tr.setContentType(ContentType.TEXT_PLAIN.toString());
			
			transform = new JsonTransform(tr);
		} else if(tranformType == TransformType.CUSTOM_TRANSFORM) {
			transform = new CustomTransform(tr);
		} else {
			throw new IllegalArgumentException("Cannot create a transformer. Cause: Unkown transform-type '" + tr.toString() + "'.");
		}
		
		return transform;
	}
	
}
