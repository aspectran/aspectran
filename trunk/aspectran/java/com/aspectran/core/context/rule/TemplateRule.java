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

import java.util.List;

import com.aspectran.core.activity.variable.token.Token;
import com.aspectran.core.activity.variable.token.Tokenizer;
import com.aspectran.core.context.rule.ability.ActionPossessable;
import com.aspectran.core.util.BooleanUtils;

/**
 * <p>
 * Created: 2008. 03. 22 오후 5:51:58
 * </p>
 */
public class TemplateRule extends ActionPossessSupport implements ActionPossessable {
	
	private String file;
	
	private String resource;
	
	private String url;

	private String encoding;

	private String content;
	
	private Token[] contentTokens;
	
	private Boolean noCache;
	
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

	public void setContentTokens(Token[] contentTokens) {
		this.contentTokens = contentTokens;
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
			List<Token> tokenList = Tokenizer.tokenize(content, false);
			if(tokenList.size() > 0) {
				contentTokens = tokenList.toArray(new Token[tokenList.size()]);
				contentTokens = Tokenizer.optimize(contentTokens);
			} else {
				contentTokens = null;
			}
		}
	}

	public Token[] getContentTokens() {
		return contentTokens;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{file=").append(file);
		sb.append(", resource=").append(resource);
		sb.append(", url=").append(url);
		sb.append(", encoding=").append(encoding);
		sb.append(", noCache=").append(noCache);
		sb.append("}");
		
		return sb.toString();
	}
	
	public static TemplateRule newInstance(String file, String resource, String url, String content, String encoding, Boolean noCache) {
		if(file == null && resource == null && url == null && content == null)
			throw new IllegalArgumentException("The <template> element requires either a file or a resource or a url attribute.");
		
		TemplateRule tr = new TemplateRule();
		tr.setFile(file);
		tr.setResource(resource);
		tr.setUrl(url);
		tr.setContent(content);
		tr.setEncoding(encoding);
		tr.setNoCache(noCache);
		
		return tr;
	}
	
}
