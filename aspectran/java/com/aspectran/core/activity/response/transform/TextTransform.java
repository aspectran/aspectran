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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.variable.token.Token;
import com.aspectran.core.activity.variable.token.TokenExpression;
import com.aspectran.core.activity.variable.token.TokenExpressor;
import com.aspectran.core.activity.variable.token.Tokenizer;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.type.TokenType;

/**
 * <p>Created: 2008. 03. 22 오후 5:51:58</p>
 */
public class TextTransform extends TransformResponse implements Response {

	private final Logger logger = LoggerFactory.getLogger(TextTransform.class);

	private final boolean debugEnabled = logger.isDebugEnabled();

	private final TemplateRule templateRule;

	private final String contentType;
	
	private final String outputEncoding;

	private final String templateFile;
	
	private final String templateResource;
	
	private final String templateUrl;

	private final String templateEncoding;

	private boolean noCache;

	private Token[] contentTokens;

	private long templateLastModifiedTime;
	
	private boolean templateLoaded;
	
	private final boolean innerContent;
	
	/**
	 * Instantiates a new text transformer.
	 * 
	 * @param transformRule the transform rule
	 */
	public TextTransform(TransformRule transformRule) {
		super(transformRule);
		this.templateRule = transformRule.getTemplateRule();
		this.contentType = transformRule.getContentType();
		this.outputEncoding = transformRule.getCharacterEncoding();
		this.templateFile = templateRule.getFile();
		this.templateResource = templateRule.getResource();
		this.templateUrl = templateRule.getUrl();
		this.templateEncoding = templateRule.getEncoding();
		this.contentTokens = templateRule.getContentTokens();
		this.noCache = templateRule.isNoCache();
		
		if(contentTokens != null && templateFile == null && templateResource == null && templateUrl == null)
			innerContent = true;
		else
			innerContent = false;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Responsible#response(com.aspectran.core.activity.CoreActivity)
	 */
	public void response(Activity activity) throws TransformResponseException {
		ResponseAdapter responseAdapter = activity.getResponseAdapter();
		
		if(responseAdapter == null)
			return;

		if(debugEnabled) {
			logger.debug("response " + transformRule);
		}

		try {
			if(!innerContent)
				loadTemplate(activity.getApplicationAdapter());

			if(contentType != null)
				responseAdapter.setContentType(contentType);

			if(outputEncoding != null)
				responseAdapter.setCharacterEncoding(outputEncoding);
			
			TokenExpressor expressor = new TokenExpression(activity);
			String content = expressor.expressAsString(contentTokens);

			if(content != null) {
				Writer output = responseAdapter.getWriter();
				output.write(content);
				output.flush();
				output.close();
			}
		} catch(Exception e) {
			throw new TransformResponseException("Text Tranformation error: " + transformRule, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Responsible#getActionList()
	 */
	public ActionList getActionList() {
		return transformRule.getActionList();
	}

	private void loadTemplate(ApplicationAdapter applicationAdapter) throws IOException {
		if(templateFile != null) {
			if(noCache) {
				File file = applicationAdapter.toRealPathAsFile(templateFile);
				Reader reader = getTemplateAsReader(file, templateEncoding);
				setContent(reader);
				reader.close();
			} else {
				File file = applicationAdapter.toRealPathAsFile(templateFile);
				long lastModifiedTime = file.lastModified();
				
				if(lastModifiedTime > templateLastModifiedTime) {
					synchronized(this) {
						lastModifiedTime = file.lastModified();
	
						if(lastModifiedTime > templateLastModifiedTime) {
							Reader reader = getTemplateAsReader(file, templateEncoding);
							setContent(reader);
							reader.close();
							templateLastModifiedTime = lastModifiedTime;
						}
					}
				}
			}
		} else if(templateResource != null) {
			if(noCache) {
				ClassLoader classLoader = applicationAdapter.getClassLoader();
				File file = new File(classLoader.getResource(templateResource).getFile());
				Reader reader = getTemplateAsReader(file, templateEncoding);
				setContent(reader);
				reader.close();
			} else {
				if(!templateLoaded) {
					synchronized(this) {
						if(!templateLoaded) {
							ClassLoader classLoader = applicationAdapter.getClassLoader();
							File file = new File(classLoader.getResource(templateResource).getFile());
							Reader reader = getTemplateAsReader(file, templateEncoding);
							setContent(reader);
							reader.close();
							templateLoaded = true;
						}
					}
				}
			}
		} else if(templateUrl != null) {
			if(noCache) {
				Reader reader = getTemplateAsReader(new URL(templateUrl), templateEncoding);
				setContent(reader);
				reader.close();
			} else {
				if(!templateLoaded) {
					synchronized(this) {
						if(!templateLoaded) {
							Reader reader = getTemplateAsReader(new URL(templateUrl), templateEncoding);
							setContent(reader);
							reader.close();
							templateLoaded = true;
						}
					}
				}
			}
		}
	}
/*	
	private String getContent() {
		if(contentTokens == null || contentTokens.length == 0)
			return null;
		
		StringBuilder sb = new StringBuilder();
		
		for(Token t : contentTokens) {
			sb.append(t.toString());
		}
		
		return sb.toString();
	}
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

	private void setContent(String content) {
		if(content == null || content.length() == 0) {
			contentTokens = null;
			return;
		}

		List<Token> tokenList = Tokenizer.tokenize(content, false);

		if(tokenList.size() > 0) {
			contentTokens = tokenList.toArray(new Token[tokenList.size()]);
			contentTokens = Tokenizer.optimize(contentTokens);
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
				
				logger.debug("text-transform template tokens [" + sb.toString() + "]");
			}
		}
//		
//		if(traceEnabled) {
//			logger.trace("Sets the content of the text-transform..." + AspectranContextConstant.LINE_SEPARATOR + getContent());
//		}
	}
}
