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
package com.aspectran.core.activity.response.transform;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.List;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.context.expr.TokenExpression;
import com.aspectran.core.context.expr.TokenExpressor;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.Tokenizer;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class TextTransform.
 * 
 * <p>Created: 2008. 03. 22 오후 5:51:58</p>
 */
public class TextTransform extends TransformResponse implements Response {

	private final Log log = LogFactory.getLog(TextTransform.class);

	private final boolean debugEnabled = log.isDebugEnabled();

	private final TemplateRule templateRule;

	private final String contentType;
	
	private final String outputEncoding;

	private Token[] contentTokens;

	private long templateLastModifiedTime;
	
	private boolean templateLoaded;
	
	/**
	 * Instantiates a new TextTransform.
	 * 
	 * @param transformRule the transform rule
	 */
	public TextTransform(TransformRule transformRule) {
		super(transformRule);
		this.templateRule = transformRule.getTemplateRule();
		this.contentType = transformRule.getContentType();
		this.outputEncoding = transformRule.getCharacterEncoding();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Responsible#response(com.aspectran.core.activity.CoreActivity)
	 */
	public void response(Activity activity) throws TransformResponseException {
		if(debugEnabled) {
			log.debug("response " + transformRule);
		}
		
		ResponseAdapter responseAdapter = activity.getResponseAdapter();
		
		if(responseAdapter == null)
			return;

		try {
			Token[] contentTokens = loadTemplate(activity.getApplicationAdapter());
			
			if(contentType != null)
				responseAdapter.setContentType(contentType);

			if(outputEncoding != null)
				responseAdapter.setCharacterEncoding(outputEncoding);
			
			if(contentTokens != null) {
				TokenExpressor expressor = new TokenExpression(activity);
				String content = expressor.expressAsString(contentTokens);
				
				if(content != null) {
					Writer output = responseAdapter.getWriter();
					output.write(content);
					output.flush();
				}
			} else {
				ProcessResult processResult = activity.getProcessResult();
				
				if(processResult != null) {
					Writer output = responseAdapter.getWriter();
					int chunks = 0;
					
					for(ContentResult contentResult : processResult) {
						for(ActionResult actionResult : contentResult) {
							Object resultValue = actionResult.getResultValue();
							if(resultValue != null) {
								if(chunks++ > 0)
									output.write(AspectranConstant.LINE_SEPARATOR);
								
								output.write(resultValue.toString());
							}
						}
					}

					output.flush();
				}
			}

		} catch(Exception e) {
			throw new TransformResponseException(transformRule, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Responsible#getActionList()
	 */
	public ActionList getActionList() {
		return transformRule.getActionList();
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Response#getTemplateRule()
	 */
	public TemplateRule getTemplateRule() {
		return templateRule;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Response#newDerivedResponse()
	 */
	public Response newDerivedResponse() {
		TransformRule transformRule = getTransformRule();
		
		if(transformRule != null) {
			TransformRule newTransformRule = TransformRule.newDerivedTransformRule(transformRule);
			Response response = new TextTransform(newTransformRule);
			return response;
		}
		
		return this;
	}

	private Token[] loadTemplate(ApplicationAdapter applicationAdapter) throws IOException {
		if(templateRule == null)
			return null;
		
		String templateFile = templateRule.getFile();
		String templateResource = templateRule.getResource();
		String templateUrl = templateRule.getUrl();
		String templateEncoding = templateRule.getEncoding();
		boolean noCache = templateRule.isNoCache();
		Token[] contentTokens = templateRule.getContentTokens();

		if(templateFile == null && templateResource == null && templateUrl == null) {
			return contentTokens;
		}
		
		contentTokens = null;
		
		if(templateFile != null) {
			if(noCache) {
				File file = applicationAdapter.toRealPathAsFile(templateFile);
				Reader reader = getTemplateAsReader(file, templateEncoding);
				contentTokens = getContentTokens(reader);
				reader.close();
			} else {
				File file = applicationAdapter.toRealPathAsFile(templateFile);
				long lastModifiedTime = file.lastModified();
				
				if(lastModifiedTime > templateLastModifiedTime) {
					synchronized(this) {
						lastModifiedTime = file.lastModified();
	
						if(lastModifiedTime > templateLastModifiedTime) {
							Reader reader = getTemplateAsReader(file, templateEncoding);
							contentTokens = getContentTokens(reader);
							reader.close();
							this.templateLastModifiedTime = lastModifiedTime;
							this.contentTokens = contentTokens;
						}
					}
				}
			}
		} else if(templateResource != null) {
			if(noCache) {
				ClassLoader classLoader = applicationAdapter.getClassLoader();
				File file = new File(classLoader.getResource(templateResource).getFile());
				Reader reader = getTemplateAsReader(file, templateEncoding);
				contentTokens = getContentTokens(reader);
				reader.close();
			} else {
				if(!templateLoaded) {
					synchronized(this) {
						if(!templateLoaded) {
							ClassLoader classLoader = applicationAdapter.getClassLoader();
							File file = new File(classLoader.getResource(templateResource).getFile());
							Reader reader = getTemplateAsReader(file, templateEncoding);
							contentTokens = getContentTokens(reader);
							reader.close();
							this.contentTokens = contentTokens;
							this.templateLoaded = true;
						}
					}
				}
			}
		} else if(templateUrl != null) {
			if(noCache) {
				Reader reader = getTemplateAsReader(new URL(templateUrl), templateEncoding);
				contentTokens = getContentTokens(reader);
				reader.close();
			} else {
				if(!templateLoaded) {
					synchronized(this) {
						if(!templateLoaded) {
							Reader reader = getTemplateAsReader(new URL(templateUrl), templateEncoding);
							contentTokens = getContentTokens(reader);
							reader.close();
							this.contentTokens = contentTokens;
							this.templateLoaded = true;
						}
					}
				}
			}
		}
		
		if(contentTokens == null)
			return this.contentTokens;
		
		return contentTokens;
	}

	private Token[] getContentTokens(Reader reader) throws IOException {
		final char[] buffer = new char[1024];
		StringBuilder sb = new StringBuilder();
		int len;

		while((len = reader.read(buffer)) != -1) {
			sb.append(buffer, 0, len);
		}

		return getContentTokens(sb.toString());
	}

	private Token[] getContentTokens(String content) {
		List<Token> tokenList = Tokenizer.tokenize(content, false);
		Token[] contentTokens;

		if(tokenList.size() > 0) {
			contentTokens = tokenList.toArray(new Token[tokenList.size()]);
			//contentTokens = Tokenizer.optimize(contentTokens);
		} else
			contentTokens = new Token[0];
		
		if(debugEnabled) {
			StringBuilder sb = new StringBuilder();

			for(Token t : contentTokens) {
				if(t.getType() != TokenType.TEXT) {
					if(sb.length() > 0)
						sb.append(", ");
					sb.append(t.toString());
				}
			}
			
			log.debug("text-transform template tokens [" + sb.toString() + "]");
		}
		
		return contentTokens;
	}

}
