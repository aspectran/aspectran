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
import java.util.Objects;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.Tokenizer;
import com.aspectran.core.context.rule.ability.BeanReferenceInspectable;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.type.BeanReferrerType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class TemplateRule.
 * 
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class TemplateRule implements Replicable<TemplateRule>, BeanReferenceInspectable {

	private static final String DEFAULT_TEMPLATE_ENGINE_NAME = "builtin";

	private static final BeanReferrerType BEAN_REFERRER_TYPE = BeanReferrerType.TEMPLATE_RULE;

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

	private volatile long lastModifiedTime;

	private volatile boolean loaded;

	public TemplateRule() {
	}

	public TemplateRule(String engine) {
		if (engine != null && !engine.isEmpty() && !engine.equals(DEFAULT_TEMPLATE_ENGINE_NAME)) {
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
    	if (engine == null || engine.isEmpty() || engine.equals(DEFAULT_TEMPLATE_ENGINE_NAME)) {
    		this.engine = null;
    		this.contentTokens = null;
    	} else {
			if (this.content != null && !Objects.equals(this.engine, engine)) {
				this.contentTokens = parseContentTokens(this.content);
			}
			this.engine = engine;
		}
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
		return (name != null && file == null && resource == null && url == null);
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
		if (engine != null) {
			throw new UnsupportedOperationException();
		}

		if (this.file != null || this.resource != null || this.url != null) {
			if (this.noCache) {
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
		if (this.engine != null || content == null || content.isEmpty()) {
			return null;
		}

		List<Token> tokenList = Tokenizer.tokenize(content, false);
		if (!tokenList.isEmpty()) {
			return tokenList.toArray(new Token[tokenList.size()]);
		} else {
			return new Token[0];
		}
	}

	public String getTemplateSource(ApplicationAdapter applicationAdapter) throws IOException {
		if (engine == null) {
			throw new UnsupportedOperationException();
		}

		if (this.file != null || this.resource != null || this.url != null) {
			if (this.noCache) {
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
		if (this.file != null) {
			File file = applicationAdapter.toRealPathAsFile(this.file);
			long lastModifiedTime = file.lastModified();
			if (lastModifiedTime > this.lastModifiedTime) {
				synchronized(this) {
					lastModifiedTime = file.lastModified();
					if (lastModifiedTime > this.lastModifiedTime) {
						String template = ResourceUtils.read(file, this.encoding);
						setTemplateSource(template);
						this.lastModifiedTime = lastModifiedTime;
					}
				}
			}
		} else if (this.resource != null) {
			if (!this.loaded) {
				synchronized(this) {
					if (!this.loaded) {
						ClassLoader classLoader = applicationAdapter.getClassLoader();
						URL url = classLoader.getResource(this.resource);
						String template = ResourceUtils.read(url, this.encoding);
						setTemplateSource(template);
						this.loaded = true;
					}
				}
			}
		} else if (this.url != null) {
			if (!this.loaded) {
				synchronized(this) {
					if (!this.loaded) {
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
		if (this.file != null) {
			File file = applicationAdapter.toRealPathAsFile(this.file);
			templateSource = ResourceUtils.read(file, this.encoding);
		} else if (this.resource != null) {
			ClassLoader classLoader = applicationAdapter.getClassLoader();
			URL url = classLoader.getResource(this.resource);
			templateSource = ResourceUtils.read(url, this.encoding);
		} else if (this.url != null) {
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
	public BeanReferrerType getBeanReferrerType() {
		return BEAN_REFERRER_TYPE;
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		if (!builtin) {
			tsb.append("id", id);
		}
		tsb.append("engine", engine);
		if (file != null) {
			tsb.append("file", file);
		} else if (resource != null) {
			tsb.append("resource", resource);
		} else if (url != null) {
			tsb.append("url", url);
		} else if (name != null) {
			tsb.append("name", name);
		} else {
			tsb.appendSize("contentLength", content);
		}
		tsb.append("encoding", encoding);
		tsb.append("noCache", noCache);
		return tsb.toString();
	}

	public static TemplateRule newInstance(String id, String engine, String name, String file, 
			String resource, String url, String content, String encoding, Boolean noCache) {
		if (id == null) {
			throw new IllegalArgumentException("The 'template' element requires an 'id' attribute.");
		}
		
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

	public static TemplateRule newInstanceForBuiltin(String engine, String name, String file, 
			String resource, String url, String content, String encoding, Boolean noCache) {
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
