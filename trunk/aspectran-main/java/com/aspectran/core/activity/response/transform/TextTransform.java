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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.core.activity.AspectranActivity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.rule.TransformRule;
import com.aspectran.core.token.Token;
import com.aspectran.core.token.Tokenizer;
import com.aspectran.core.token.expression.TokenExpression;
import com.aspectran.core.token.expression.TokenExpressor;
import com.aspectran.core.type.TokenType;

/**
 * <p>Created: 2008. 03. 22 오후 5:51:58</p>
 */
public class TextTransform extends AbstractTransform implements Responsible {

	private final Log log = LogFactory.getLog(TextTransform.class);

	private boolean debugEnabled = log.isDebugEnabled();

	private Token[] contentTokens;

	private long templateLastModifiedTime;
	
	private File templateFile;
	
	private String templateUrl;

	private String templateEncoding;

	private String templateContent;
	
	private boolean templateLoaded;

	private boolean templateNoCache;
	
	/**
	 * Instantiates a new text transformer.
	 * 
	 * @param transformRule the transform rule
	 */
	public TextTransform(TransformRule transformRule) {
		super(transformRule);
		this.templateFile = transformRule.getTemplateFile();
		this.templateUrl = transformRule.getTemplateUrl();
		this.templateContent = transformRule.getTemplateContent();
		this.templateNoCache = (transformRule.getTemplateNoCache() == Boolean.TRUE);
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#response(org.jhlabs.translets.action.Translet)
	 */
	public void response(AspectranActivity activity) throws TransformResponseException {
		try {
			ResponseAdapter responseAdapter = activity.getResponseAdapter();
			
			String contentType = transformRule.getContentType();
			String outputEncoding = transformRule.getCharacterEncoding();

			if(contentType != null)
				responseAdapter.setContentType(contentType);

			if(outputEncoding != null)
				responseAdapter.setCharacterEncoding(outputEncoding);
			
			TokenExpressor expressor = new TokenExpression(activity);
			String content = expressor.express(getContentTokens());

			if(content != null) {
				Writer output = responseAdapter.getWriter();
				output.write(content);
				output.flush();
				output.close();
			}

			if(debugEnabled) {
				log.debug("response " + transformRule);
			}
		} catch(Exception e) {
			throw new TransformResponseException("Text Tranformation error: " + transformRule, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#getActionList()
	 */
	public ActionList getActionList() {
		return transformRule.getActionList();
	}

	/**
	 * Gets the content tokens.
	 * 
	 * @return the content tokens
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Token[] getContentTokens() throws IOException {
		if(templateFile != null) {
			if(templateNoCache) {
				Reader reader = getTemplateAsReader(templateFile, templateEncoding);
				setContent(reader);
			} else {
				long lastModifiedTime = templateFile.lastModified();
				
				if(lastModifiedTime > templateLastModifiedTime) {
					synchronized(this) {
						lastModifiedTime = templateFile.lastModified();
	
						if(lastModifiedTime > templateLastModifiedTime) {
							Reader reader = getTemplateAsReader(templateFile, templateEncoding);
							setContent(reader);
							templateLastModifiedTime = lastModifiedTime;
						}
					}
				}
			}
		} else if(templateUrl != null) {
			if(templateNoCache) {
				Reader reader = getTemplateAsReader(new URL(templateUrl), templateEncoding);
				setContent(reader);
			} else {
				if(!templateLoaded) {
					synchronized(this) {
						if(!templateLoaded) {
							Reader reader = getTemplateAsReader(new URL(templateUrl), templateEncoding);
							setContent(reader);
							templateLoaded = true;
						}
					}
				}
			}
		} else if(templateContent != null) {
			if(templateNoCache) {
				setContent(templateContent);
			} else {
				if(!templateLoaded) {
					synchronized(this) {
						if(!templateLoaded) {
							setContent(templateContent);
							templateLoaded = true;
						}
					}
				}
			}
		}
		
		return contentTokens;
	}
	
	/**
	 * Gets the content.
	 * 
	 * @return the content
	 */
	public String getContent() {
		if(contentTokens == null || contentTokens.length == 0)
			return null;
		
		StringBuilder sb = new StringBuilder();
		
		for(Token t : contentTokens) {
			sb.append(t.toString());
		}
		
		return sb.toString();
	}

	/**
	 * Sets the content.
	 * 
	 * @param reader the new content
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void setContent(Reader reader) throws IOException {
		final char[] buffer = new char[1024];

		StringBuilder sb = new StringBuilder();
		int len;

		while((len = reader.read(buffer)) != -1) {
			sb.append(buffer, 0, len);
		}

		setContent(sb.toString());
	}

	/**
	 * Sets the content.
	 * 
	 * @param content the new content
	 */
	private void setContent(String content) {
		if(content == null || content.length() == 0) {
			contentTokens = null;
			return;
		}

		List<Token> tokenList = Tokenizer.tokenize(content, false);

		if(tokenList.size() > 0) {
			contentTokens = tokenList.toArray(new Token[tokenList.size()]);
			contentTokens = Tokenizer.optimizeTokens(contentTokens);
		} else
			contentTokens = null;
		
		if(debugEnabled) {
			if(contentTokens != null) {
				StringBuilder sb = new StringBuilder();

				for(Token t : contentTokens) {
					if(t.getType() != TokenType.TEXT) {
						if(sb.length() > 0)
							sb.append(", ");
						sb.append(t.toString());
					}
				}
				
				log.debug("text-transform template tokens: " + sb.toString());
			}
		}
//		
//		if(traceEnabled) {
//			log.trace("Sets the content of the text-transform..." + AspectranContextConstant.LINE_SEPARATOR + getContent());
//		}
	}
}
