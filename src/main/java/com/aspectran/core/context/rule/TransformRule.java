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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.rule.ability.ActionPossessable;
import com.aspectran.core.context.rule.type.ContentType;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.context.rule.type.TransformType;
import com.aspectran.core.util.BooleanUtils;

/**
 * The Class TransformRule.
 * 
 * <p>Created: 2008. 03. 22 오후 5:51:58</p>
 */
public class TransformRule extends ActionPossessSupport implements ActionPossessable {
	
	public static final ResponseType RESPONSE_TYPE = ResponseType.TRANSFORM;

	protected TransformType transformType;

	protected String contentType;
	
	protected String characterEncoding;
	
	private TemplateRule templateRule;
	
	private Boolean defaultResponse;
	
	private Boolean pretty;
	
	public TransformRule() {
	}
	
	/**
	 * Gets the transform type.
	 * 
	 * @return the transform type
	 */
	public TransformType getTransformType() {
		return transformType;
	}
	
	/**
	 * Sets the transform type.
	 * 
	 * @param transformType the transformType to set
	 */
	public void setTransformType(TransformType transformType) {
		this.transformType = transformType;
		
		if(contentType == null) {
			if(transformType == TransformType.TEXT_TRANSFORM)
				contentType = ContentType.TEXT_PLAIN.toString();
			else if(transformType == TransformType.JSON_TRANSFORM)
				contentType = ContentType.TEXT_JSON.toString();
			else if(transformType == TransformType.XML_TRANSFORM)
				contentType = ContentType.TEXT_XML.toString();
		}
	}

	/**
	 * Gets the content type.
	 * 
	 * @return the content type
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Sets the content type.
	 * 
	 * @param contentType the new content type
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	/**
	 * Gets the character encoding.
	 * 
	 * @return the characterEncoding
	 */
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	/**
	 * Sets the character encoding.
	 * 
	 * @param characterEncoding the characterEncoding to set
	 */
	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	public TemplateRule getTemplateRule() {
		return templateRule;
	}

	public void setTemplateRule(TemplateRule templateRule) {
		this.templateRule = templateRule;

		if(templateRule.getEncoding() != null && characterEncoding == null)
			characterEncoding = templateRule.getEncoding();
	}

	public Boolean getDefaultResponse() {
		return defaultResponse;
	}

	public boolean isDefaultResponse() {
		return BooleanUtils.toBoolean(defaultResponse);
	}

	public void setDefaultResponse(Boolean defaultResponse) {
		this.defaultResponse = defaultResponse;
	}

	public Boolean getPretty() {
		return pretty;
	}

	public boolean isPretty() {
		return BooleanUtils.toBoolean(pretty);
	}

	public void setPretty(Boolean pretty) {
		this.pretty = pretty;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{transformType=").append(transformType);
		sb.append(", contentType=").append(contentType);
		sb.append(", characterEncoding=").append(characterEncoding);
		sb.append(", templateRule=").append(templateRule);
		if(defaultResponse != null)
			sb.append(", defaultResponse=").append(defaultResponse);
		sb.append("}");
		
		return sb.toString();
	}
	
	public static TransformRule newInstance(String type, String contentType, String characterEncoding, Boolean defaultResponse, Boolean pretty) {
		TransformType transformType = TransformType.valueOf(type);

		if(transformType == null && contentType != null) {
			transformType = TransformType.valueOf(ContentType.valueOf(contentType));
		}
		
		if(transformType == null)
			throw new IllegalArgumentException("Unknown transform-type '" + type + "'.");

		TransformRule tr = new TransformRule();
		tr.setContentType(contentType);
		tr.setTransformType(transformType);
		tr.setCharacterEncoding(characterEncoding);
		tr.setDefaultResponse(defaultResponse);
		tr.setPretty(pretty);
		
		return tr;
	}
	
	public static TransformRule newDerivedTransformRule(TransformRule transformRule) {
		TransformRule newTransformRule = new TransformRule();
		newTransformRule.setContentType(transformRule.getContentType());
		newTransformRule.setTransformType(transformRule.getTransformType());
		newTransformRule.setCharacterEncoding(transformRule.getCharacterEncoding());
		newTransformRule.setDefaultResponse(transformRule.getDefaultResponse());
		newTransformRule.setPretty(transformRule.getPretty());
		newTransformRule.setActionList(transformRule.getActionList());
		
		TemplateRule templateRule = transformRule.getTemplateRule();
		if(templateRule != null) {
			templateRule = TemplateRule.newDerivedBuiltinTemplateRule(templateRule);
			newTransformRule.setTemplateRule(templateRule);
		}
		
		return newTransformRule;
	}
	
}
