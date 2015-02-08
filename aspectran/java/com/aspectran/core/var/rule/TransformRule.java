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
package com.aspectran.core.var.rule;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.var.rule.ability.ActionPossessable;
import com.aspectran.core.var.type.ContentType;
import com.aspectran.core.var.type.ResponseType;
import com.aspectran.core.var.type.TransformType;

/**
 * <p>
 * Created: 2008. 03. 22 오후 5:51:58
 * </p>
 */
public class TransformRule extends ActionPossessSupport implements ActionPossessable {
	
	public static final ResponseType RESPONSE_TYPE = ResponseType.TRANSFORM;

	protected TransformType transformType;

	protected String contentType;
	
	protected String characterEncoding;
	
	private TemplateRule templateRule;
	
	private ActionList actionList;
	
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
	
//	private void setContent(String content) {
//		if(content == null || content.length() == 0) {
//			contentTokens = null;
//			return;
//		}
//
//		List<Token> tokenList = Tokenizer.tokenize(content, false);
//
//		if(tokenList.size() > 0) {
//			contentTokens = tokenList.toArray(new Token[tokenList.size()]);
//			contentTokens = Tokenizer.optimizeTokens(contentTokens);
//		} else
//			contentTokens = null;
//		
//		if(debugEnabled) {
//			if(contentTokens != null) {
//				StringBuilder sb = new StringBuilder();
//
//				for(Token t : contentTokens) {
//					if(t.getType() != TokenType.TEXT) {
//						if(sb.length() > 0)
//							sb.append(", ");
//						sb.append(t.toString());
//					}
//				}
//				
//				log.debug("text-transform template tokens [" + sb.toString() + "]");
//			}
//		}
//		
//		if(traceEnabled) {
//			log.trace("Sets the content of the text-transform..." + AspectranContextConstant.LINE_SEPARATOR + getContent());
//		}
//	}

	public TemplateRule getTemplateRule() {
		return templateRule;
	}

	public void setTemplateRule(TemplateRule templateRule) {
		this.templateRule = templateRule;

		if(templateRule.getEncoding() != null && characterEncoding == null)
			characterEncoding = templateRule.getEncoding();
	}

	/**
	 * Gets the action list.
	 *
	 * @return the action list
	 */
	public ActionList getActionList() {
		return actionList;
	}

	/**
	 * Sets the action list.
	 *
	 * @param actionList the new action list
	 */
	public void setActionList(ActionList actionList) {
		this.actionList = actionList;
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
		sb.append("}");
		
		return sb.toString();
	}
	
	public static TransformRule newInstance(String type, String contentType, String characterEncoding) {
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
		
		return tr;
	}
	
}
