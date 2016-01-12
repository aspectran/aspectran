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
package com.aspectran.core.context.builder.xml;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Node;

import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.context.builder.ImportHandler;
import com.aspectran.core.context.builder.Importable;
import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.GenericParameters;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * Aspectran Node Parser.
 * 
 * @since 2008. 06. 14 오전 4:39:24
 */
public class AspectranNodeParser {
	
	private final NodeletParser parser = new NodeletParser();

	private final ContextBuilderAssistant assistant;
	
	/**
	 * Instantiates a new AspectranNodeParser.
	 * 
	 * @param assistant the assistant for Context Builder
	 */
	public AspectranNodeParser(ContextBuilderAssistant assistant) {
		this(assistant, true);
	}

	/**
	 * Instantiates a new AspectranNodeParser.
	 *
	 * @param assistant the assistant
	 * @param validating the validating
	 */
	public AspectranNodeParser(ContextBuilderAssistant assistant, boolean validating) {
		this.assistant = assistant;
		assistant.clearObjectStack();

		parser.setValidating(validating);
		parser.setEntityResolver(new AspectranDtdResolver(validating));

		addSettingsNodelets();
		addTypeAliasNodelets();
		addAspectRuleNodelets();
		addBeanNodelets();
		addTransletNodelets();
		addTemplateNodelets();
		addImportNodelets();
	}
	
	/**
	 * Parses the aspectran configuration.
	 *
	 * @param inputStream the input stream
	 * @throws Exception the exception
	 */
	public void parse(InputStream inputStream) throws Exception {
		try {
			parser.parse(inputStream);
		} catch(Exception e) {
			throw new Exception("Error parsing aspectran configuration.", e);
		} finally {
			if(inputStream != null) {
				inputStream.close();
				inputStream = null;
			}
		}
	}

	/**
	 * Adds the settings nodelets.
	 */
	private void addSettingsNodelets() {
		parser.addNodelet("/aspectran/description", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(text != null) {
					assistant.getAssistantLocal().setDescription(text);
				}
			}
		});
		parser.addNodelet("/aspectran/settings", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(StringUtils.hasText(text)) {
					Parameters parameters = new GenericParameters(text);
					Iterator<String> iter = parameters.getParameterNameSet().iterator();
					
					while(iter.hasNext()) {
						String name = iter.next();
						
						DefaultSettingType settingType = null;
						
						if(name != null) {
							settingType = DefaultSettingType.valueOf(name);
							
							if(settingType == null)
								throw new IllegalArgumentException("Unknown setting name '" + name + "'");
						}
						
						assistant.putSetting(settingType, parameters.getString(name));
					}
				}
			}
		});
		parser.addNodelet("/aspectran/settings/setting", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String name = attributes.get("name");
				String value = attributes.get("value");

				DefaultSettingType settingType = null;
				
				if(name != null) {
					settingType = DefaultSettingType.valueOf(name);
					
					if(settingType == null)
						throw new IllegalArgumentException("Unknown setting name '" + name + "'");
				}

				assistant.putSetting(settingType, (text == null) ? value : text);
			}
		});
		parser.addNodelet("/aspectran/settings/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				assistant.applySettings();
			}
		});
	}

	/**
	 * Adds the type alias nodelets.
	 */
	private void addTypeAliasNodelets() {
		parser.addNodelet("/aspectran/typeAliases", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(StringUtils.hasLength(text)) {
					Parameters parameters = new GenericParameters(text);
					Iterator<String> iter = parameters.getParameterNameSet().iterator();
					
					while(iter.hasNext()) {
						String alias = iter.next();
						assistant.addTypeAlias(alias, parameters.getString(alias));
					}
				}
			}
		});
		parser.addNodelet("/aspectran/typeAliases/typeAlias", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String alias = attributes.get("alias");
				String type = attributes.get("type");
				
				assistant.addTypeAlias(alias, type);
			}
		});
	}
	
	private void addAspectRuleNodelets() {
		parser.addNodelet("/aspectran", new AspectNodeletAdder(assistant));
		
	}

	/**
	 * Adds the bean nodelets.
	 */
	private void addBeanNodelets() {
		parser.addNodelet("/aspectran", new BeanNodeletAdder(assistant));
	}

	/**
	 * Adds the translet nodelets.
	 */
	private void addTransletNodelets() {
		parser.addNodelet("/aspectran", new TransletNodeletAdder(assistant));
	}

	/**
	 * Adds the template nodelets.
	 */
	private void addTemplateNodelets() {
		parser.addNodelet("/template", new TemplateNodeletAdder(assistant));
	}

	/**
	 * Adds the import nodelets.
	 */
	private void addImportNodelets() {
		parser.addNodelet("/aspectran/import", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String resource = attributes.get("resource");
				String file = attributes.get("file");
				String url = attributes.get("url");
				String fileType = attributes.get("fileType");

				Importable importable = Importable.newInstance(assistant, resource, file, url, fileType);
				
				ImportHandler importHandler = assistant.getImportHandler();
				
				if(importHandler != null)
					importHandler.pending(importable);
			}
		});
	}

}
