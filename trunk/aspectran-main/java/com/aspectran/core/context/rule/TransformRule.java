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
package com.aspectran.core.context.rule;

import java.io.File;
import java.util.List;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.context.rule.ability.ActionPossessable;
import com.aspectran.core.context.type.ContentType;
import com.aspectran.core.context.type.ResponseType;
import com.aspectran.core.context.type.TransformType;
import com.aspectran.core.expr.token.Token;
import com.aspectran.core.expr.token.Tokenizer;

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
	
	private File templateFile;
	
	private String templateUrl;

	private String templateEncoding;

	private String templateContent;
	
	private Token[] contentTokens;
	
	private Boolean templateNoCache;
	
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
	
	/**
	 * Gets the template file.
	 * 
	 * @return the templateFile
	 */
	public File getTemplateFile() {
		return templateFile;
	}

	/**
	 * Sets the template file.
	 * 
	 * @param templateFile the templateFile to set
	 */
	public void setTemplateFile(File templateFile) {
		this.templateFile = templateFile;
		
		if(templateUrl != null)
			templateUrl = null;
	}

	/**
	 * Gets the template url.
	 * 
	 * @return the templateUrl
	 */
	public String getTemplateUrl() {
		return templateUrl;
	}

	/**
	 * Sets the template url.
	 * 
	 * @param templateUrl the templateUrl to set
	 */
	public void setTemplateUrl(String templateUrl) {
		this.templateUrl = templateUrl;

		if(templateFile != null)
			templateFile = null;
	}

	/**
	 * Gets the template encoding.
	 * 
	 * @return the templateEncoding
	 */
	public String getTemplateEncoding() {
		return templateEncoding;
	}

	/**
	 * Sets the template encoding.
	 * 
	 * @param templateEncoding the templateEncoding to set
	 */
	public void setTemplateEncoding(String templateEncoding) {
		this.templateEncoding = templateEncoding;
	}

	/**
	 * Gets the template content.
	 * 
	 * @return the templateContent
	 */
	public String getTemplateContent() {
		return templateContent;
	}

	/**
	 * Sets the template content.
	 * 
	 * @param templateContent the templateContent to set
	 */
	public void setTemplateContent(String templateContent) {
		this.templateContent = templateContent;
		
		if(templateContent == null || templateContent.length() == 0) {
			contentTokens = null;
		} else {
			List<Token> tokenList = Tokenizer.tokenize(templateContent, false);
	
			if(tokenList.size() > 0) {
				contentTokens = tokenList.toArray(new Token[tokenList.size()]);
				contentTokens = Tokenizer.optimizeTokens(contentTokens);
			} else {
				contentTokens = null;
			}
		}
	}

	public Token[] getContentTokens() {
		return contentTokens;
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
	
	
	/**
	 * Gets the template no cache.
	 * 
	 * @return the template no cache
	 */
	public Boolean getTemplateNoCache() {
		return templateNoCache;
	}

	/**
	 * Sets the template no cache.
	 * 
	 * @param templateNoCache the new template no cache
	 */
	public void setTemplateNoCache(Boolean templateNoCache) {
		this.templateNoCache = templateNoCache;
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

		if(actionList != null) {
			sb.append(", actionList=");
			sb.append('[');

			for(int i = 0; i < actionList.size(); i++) {
				Executable action = actionList.get(i);

				if(i > 0)
					sb.append(", ");

				sb.append(action.getActionId());
			}

			sb.append(']');
		}

		if(templateFile != null)
			sb.append(", templateFile=").append(templateFile.getAbsolutePath());
		
		if(templateUrl != null)
			sb.append(", templateUrl=").append(templateUrl);
		
		if(templateFile != null || templateUrl != null) {
			sb.append(", templateEncoding=").append(templateEncoding);
			sb.append(", templateNoCache=").append(templateNoCache);
		}

		sb.append("}");
		
		return sb.toString();
	}
}
