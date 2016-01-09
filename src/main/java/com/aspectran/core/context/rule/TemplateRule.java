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

import java.util.List;

import com.aspectran.core.activity.variable.token.Token;
import com.aspectran.core.activity.variable.token.Tokenizer;
import com.aspectran.core.util.BooleanUtils;

/**
 * The Class TemplateRule.
 * 
 * <p>Created: 2008. 03. 22 오후 5:51:58</p>
 */
public class TemplateRule {

	private String id;

	private String engine;

	private String file;

	private String resource;
	
	private String url;

	private String encoding;

	private String content;
	
	private Token[] contentTokens;
	
	private Boolean noCache;

	private boolean builtin;

	public TemplateRule() {
	}

	public TemplateRule(String engine) {
		this.engine = engine;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEngine() {
		return engine;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public Boolean getNoCache() {
		return noCache;
	}

	public boolean isNoCache() {
		return BooleanUtils.toBoolean(noCache);
	}

	public void setNoCache(Boolean noCache) {
		this.noCache = noCache;
	}

	public boolean isBuiltin() {
		return builtin;
	}

	public void setBuiltin(boolean builtin) {
		this.builtin = builtin;
	}

	public String getContent() {
		return content;
	}

	/**
	 * Sets the template content.
	 * 
	 * @param content the templateContent to set
	 */
	public void setContent(String content) {
		this.content = content;
		
		if(content == null || content.length() == 0) {
			contentTokens = null;
		} else {
			if(engine == null) {
				List<Token> tokenList = Tokenizer.tokenize(content, false);
				if (tokenList.size() > 0) {
					contentTokens = tokenList.toArray(new Token[tokenList.size()]);
					contentTokens = Tokenizer.optimize(contentTokens);
				} else {
					contentTokens = null;
				}
			}
		}
	}
	
	private void setContent(String content, Token[] contentTokens) {
		this.content = content;
		this.contentTokens = contentTokens;
	}
	
	public Token[] getContentTokens() {
		return contentTokens;
	}

	public void setContentTokens(Token[] contentTokens) {
		this.contentTokens = contentTokens;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		if(!builtin)
			sb.append("id=").append(id).append(", ");
		sb.append("engine=").append(engine);
		sb.append(", file=").append(file);
		sb.append(", resource=").append(resource);
		sb.append(", url=").append(url);
		sb.append(", encoding=").append(encoding);
		sb.append(", noCache=").append(noCache);
		sb.append("}");
		
		return sb.toString();
	}
	
	public static TemplateRule newInstance(String id, String engine, String file, String resource, String url, String content, String encoding, Boolean noCache) {
		TemplateRule tr = new TemplateRule(engine);
		tr.setId(id);
		tr.setFile(file);
		tr.setResource(resource);
		tr.setUrl(url);
		tr.setContent(content);
		tr.setEncoding(encoding);
		tr.setNoCache(noCache);
		
		return tr;
	}
	
	public static TemplateRule newInstanceForBuiltin(String engine, String file, String resource, String url, String content, String encoding, Boolean noCache) {
		TemplateRule tr = new TemplateRule(engine);
		tr.setFile(file);
		tr.setResource(resource);
		tr.setUrl(url);
		tr.setContent(content);
		tr.setEncoding(encoding);
		tr.setNoCache(noCache);
		tr.setBuiltin(true);

		return tr;
	}

	public static TemplateRule newDerivedTemplateRule(TemplateRule templateRule) {
		TemplateRule newTemplateRule = new TemplateRule();
		newTemplateRule.setFile(templateRule.getFile());
		newTemplateRule.setResource(templateRule.getResource());
		newTemplateRule.setUrl(templateRule.getUrl());
		newTemplateRule.setEncoding(templateRule.getEncoding());
		newTemplateRule.setContent(templateRule.getContent(), templateRule.getContentTokens());
		newTemplateRule.setNoCache(templateRule.getNoCache());
		newTemplateRule.setBuiltin(true);

		return newTemplateRule;
	}
	
}
