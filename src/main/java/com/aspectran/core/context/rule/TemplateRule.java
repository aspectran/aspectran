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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.Tokenizer;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.ResourceUtils;

/**
 * The Class TemplateRule.
 * 
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class TemplateRule implements Replicable<TemplateRule> {

	public static final String DEFAULT_TEMPLATE_ENGINE_NAME = "token";
	
	private String id;

	private String engine;

	private String name;

	private String file;

	private String resource;
	
	private String url;

	private String encoding;

	private String content;
	
	private Token[] contentTokens;
	
	private Boolean noCache;

	private boolean builtin;

	private String templateSource;

	private long lastModifiedTime;

	private boolean loaded;

	public TemplateRule() {
	}

	public TemplateRule(String engine) {
		if(engine != null && engine.length() > 0 && !engine.equals(TemplateRule.DEFAULT_TEMPLATE_ENGINE_NAME)) {
			this.engine = engine;
		}
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

	public void setEngine(String engine) {
    	if(engine != null && (engine.length() == 0 || engine.equals(TemplateRule.DEFAULT_TEMPLATE_ENGINE_NAME))) {
    		this.engine = null;
    		this.contentTokens = null;
    		return;
    	}
    	
		if(this.engine != engine) {
			if(this.content != null) {
				this.contentTokens = parseContentTokens(this.content);
			}
		}
		
		this.engine = engine;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public boolean isUseExternalSource() {
		if(name != null && file == null && resource == null && url == null)
			return true;

		return false;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
		this.contentTokens = parseContentTokens(content);
	}

	private void setContent(String content, Token[] contentTokens) {
		this.content = content;
		this.contentTokens = contentTokens;
	}

	public Token[] getContentTokens() {
		return this.contentTokens;
	}

	protected void setContentTokens(Token[] contentTokens) {
		this.contentTokens = contentTokens;
	}

	public Token[] getContentTokens(ApplicationAdapter applicationAdapter) throws IOException {
		if(engine != null) {
			throw new UnsupportedOperationException();
		}

		if(this.file != null || this.resource != null || this.url != null) {
			if(this.noCache) {
				String source = loadTemplateSource(applicationAdapter);
				return parseContentTokens(source);
			} else {
				loadCachedTemplateSource(applicationAdapter);
				return this.contentTokens;
			}
		} else {
			return this.contentTokens;
		}
	}

	private Token[] parseContentTokens(String content) {
		if(this.engine != null || content == null || content.length() == 0)
			return null;

		List<Token> tokenList = Tokenizer.tokenize(content, false);
		if(tokenList.size() > 0) {
			return tokenList.toArray(new Token[tokenList.size()]);
		} else {
			return new Token[0];
		}
	}

	public String getTemplateSource(ApplicationAdapter applicationAdapter) throws IOException {
		if(engine == null) {
			throw new UnsupportedOperationException();
		}

		if(this.file != null || this.resource != null || this.url != null) {
			if(this.noCache) {
				return loadTemplateSource(applicationAdapter);
			} else {
				loadCachedTemplateSource(applicationAdapter);
				return this.templateSource;
			}
		} else {
			return this.content;
		}
	}

	private void setTemplateSource(String templateSource) {
		this.templateSource = templateSource;
		this.contentTokens = parseContentTokens(templateSource);
	}

	private void loadCachedTemplateSource(ApplicationAdapter applicationAdapter) throws IOException {
		if(this.file != null) {
			File file = applicationAdapter.toRealPathAsFile(this.file);
			long lastModifiedTime = file.lastModified();
			if(lastModifiedTime > this.lastModifiedTime) {
				synchronized(this) {
					lastModifiedTime = file.lastModified();
					if(lastModifiedTime > this.lastModifiedTime) {
						String template = ResourceUtils.read(file, this.encoding);
						setTemplateSource(template);
						this.lastModifiedTime = lastModifiedTime;
					}
				}
			}
		} else if(this.resource != null) {
			if(!this.loaded) {
				synchronized(this) {
					if(!this.loaded) {
						ClassLoader classLoader = applicationAdapter.getClassLoader();
						URL url = classLoader.getResource(this.resource);
						String template = ResourceUtils.read(url, this.encoding);
						setTemplateSource(template);
						this.loaded = true;
					}
				}
			}
		} else if(this.url != null) {
			if(!this.loaded) {
				synchronized(this) {
					if(!this.loaded) {
						URL url = new URL(this.url);
						String template = ResourceUtils.read(url, this.encoding);
						setTemplateSource(template);
						this.loaded = true;
					}
				}
			}
		}
	}

	private String loadTemplateSource(ApplicationAdapter applicationAdapter) throws IOException {
		String templateSource = null;
		if(this.file != null) {
			File file = applicationAdapter.toRealPathAsFile(this.file);
			templateSource = ResourceUtils.read(file, this.encoding);
		} else if(this.resource != null) {
			ClassLoader classLoader = applicationAdapter.getClassLoader();
			URL url = classLoader.getResource(this.resource);
			templateSource = ResourceUtils.read(url, this.encoding);
		} else if(this.url != null) {
			URL url = new URL(this.url);
			templateSource = ResourceUtils.read(url, this.encoding);
		}
		return templateSource;
	}

	@Override
	public TemplateRule replicate() {
		return replicate(this);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		if(!builtin)
			sb.append(", id=").append(id);
		if(engine != null)
			sb.append(", engine=").append(engine);
		if(file != null) {
			sb.append(", file=").append(file);
		} else if(resource != null) {
			sb.append(", resource=").append(resource);
		} else if(url != null) {
			sb.append(", url=").append(url);
		} else if(name != null) {
			sb.append(", name=").append(name);
		} else {
			sb.append(", contentLength=").append(content == null ? 0 : content.length());
		}
		if(contentTokens != null) {
			sb.append(", contentTokenNames=[");
			int i = 0;
			for(Token t : contentTokens) {
				if(t.getType() != TokenType.TEXT) {
					if(i++ > 0)
						sb.append(", ");
					sb.append(t.getName());
				}
			}
			sb.append("]");
		}
		if(encoding != null)
			sb.append(", encoding=").append(encoding);
		if(noCache != null)
			sb.append(", noCache=").append(noCache);
		sb.append("}");

		if(sb.charAt(1) == ',')
			sb.delete(1, 3);

		return sb.toString();
	}

	public static TemplateRule newInstance(String id, String engine, String name, String file, String resource, String url, String content, String encoding, Boolean noCache) {
		TemplateRule tr = new TemplateRule(engine);
		tr.setId(id);
		tr.setName(name);
		tr.setFile(file);
		tr.setResource(resource);
		tr.setUrl(url);
		tr.setContent(content);
		tr.setEncoding(encoding);
		tr.setNoCache(noCache);

		return tr;
	}

	public static TemplateRule newInstanceForBuiltin(String engine, String name, String file, String resource, String url, String content, String encoding, Boolean noCache) {
		TemplateRule tr = new TemplateRule(engine);
		tr.setName(name);
		tr.setFile(file);
		tr.setResource(resource);
		tr.setUrl(url);
		tr.setContent(content);
		tr.setEncoding(encoding);
		tr.setNoCache(noCache);
		tr.setBuiltin(true);

		return tr;
	}

	public static TemplateRule replicate(TemplateRule templateRule) {
		TemplateRule tr = new TemplateRule(templateRule.getEngine());
		tr.setId(templateRule.getId());
		tr.setName(templateRule.getName());
		tr.setFile(templateRule.getFile());
		tr.setResource(templateRule.getResource());
		tr.setUrl(templateRule.getUrl());
		tr.setEncoding(templateRule.getEncoding());
		tr.setContent(templateRule.getContent(), templateRule.getContentTokens());
		tr.setNoCache(templateRule.getNoCache());
		tr.setBuiltin(templateRule.isBuiltin());

		return tr;
	}
	
}
