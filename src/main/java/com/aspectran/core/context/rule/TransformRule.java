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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.rule.ability.ActionPossessable;
import com.aspectran.core.context.rule.ability.BeanReferenceInspectable;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.type.BeanReferrerType;
import com.aspectran.core.context.rule.type.ContentType;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.context.rule.type.TransformType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class TransformRule.
 * 
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class TransformRule extends ActionPossessSupport implements ActionPossessable, Replicable<TransformRule>, BeanReferenceInspectable {
	
	public static final ResponseType RESPONSE_TYPE = ResponseType.TRANSFORM;

	private static final BeanReferrerType BEAN_REFERABLE_RULE_TYPE = BeanReferrerType.TRANSFORM_RULE;

	private TransformType transformType;

	private String contentType;
	
	private String templateId;

	private TemplateRule templateRule;

	private String characterEncoding;

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

		if(templateRule != null) {
			if(templateRule.getEncoding() != null && this.characterEncoding == null)
				this.characterEncoding = templateRule.getEncoding();
		}
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
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

	@Override
	public TransformRule replicate() {
		return replicate(this);
	}

	@Override
	public BeanReferrerType getBeanReferrerType() {
		return BEAN_REFERABLE_RULE_TYPE;
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.appendForce("responseType", RESPONSE_TYPE);
		tsb.append("transformType", transformType);
		tsb.append("contentType", contentType);
		tsb.append("templateId", templateId);
		tsb.append("templateRule", templateRule);
		tsb.append("characterEncoding", characterEncoding);
		tsb.append("defaultResponse", defaultResponse);
		return tsb.toString();
	}
	
	public static TransformRule newInstance(String type, String contentType, String templateId, String characterEncoding, Boolean defaultResponse, Boolean pretty) {
		TransformType transformType = TransformType.lookup(type);

		if(transformType == null && contentType != null) {
			transformType = TransformType.lookup(ContentType.lookup(contentType));
		}
		
		if(transformType == null)
			throw new IllegalArgumentException("No transform type registered for '" + type + "'.");

		TransformRule tr = new TransformRule();
		tr.setTransformType(transformType);
		if(contentType != null) {
			tr.setContentType(contentType);
		}
		tr.setTemplateId(templateId);
		tr.setCharacterEncoding(characterEncoding);
		tr.setDefaultResponse(defaultResponse);
		tr.setPretty(pretty);
		
		return tr;
	}

	public static TransformRule replicate(TransformRule transformRule) {
		TransformRule newTransformRule = new TransformRule();
		newTransformRule.setTransformType(transformRule.getTransformType());
		newTransformRule.setContentType(transformRule.getContentType());
		newTransformRule.setTemplateId(transformRule.getTemplateId());
		newTransformRule.setCharacterEncoding(transformRule.getCharacterEncoding());
		newTransformRule.setDefaultResponse(transformRule.getDefaultResponse());
		newTransformRule.setPretty(transformRule.getPretty());
		newTransformRule.setActionList(transformRule.getActionList());
		
		TemplateRule templateRule = transformRule.getTemplateRule();
		if(templateRule != null) {
			newTransformRule.setTemplateRule(templateRule.replicate());
		}
		
		return newTransformRule;
	}
	
}
