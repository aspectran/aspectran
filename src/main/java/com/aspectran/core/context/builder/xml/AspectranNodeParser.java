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
package com.aspectran.core.context.builder.xml;

import java.io.IOException;
import java.io.InputStream;

import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.context.builder.importer.ImportHandler;
import com.aspectran.core.context.builder.importer.Importer;
import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.GenericParameters;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * The Class AspectranNodeParser.
 * 
 * <p>Created: 2008. 06. 14 AM 4:39:24</p>
 */
public class AspectranNodeParser {

	private final ContextBuilderAssistant assistant;

	private final NodeletParser parser;

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
	 * @param assistant the context builder assistant
	 * @param validating true if the parser produced will validate documents
	 *                   as they are parsed; false otherwise.
	 */
	public AspectranNodeParser(ContextBuilderAssistant assistant, boolean validating) {
		this.assistant = assistant;
		assistant.clearObjectStack();

		this.parser = new NodeletParser();
		this.parser.setValidating(validating);
		this.parser.setEntityResolver(new AspectranDtdResolver(validating));

		addSettingsNodelets();
		addTypeAliasNodelets();
		addAspectNodelets();
		addBeanNodelets();
		addTemplateNodelets();
		addTransletNodelets();
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
				try {
					inputStream.close();
				} catch(IOException e) {
					// ignore
				}
			}
		}
	}

	/**
	 * Adds the settings nodelets.
	 */
	private void addSettingsNodelets() {
		parser.addNodelet("/aspectran/description", (node, attributes, text) -> {
            if(text != null) {
                assistant.getAssistantLocal().setDescription(text);
            }
        });
		parser.addNodelet("/aspectran/settings", (node, attributes, text) -> {
            if(StringUtils.hasText(text)) {
                Parameters parameters = new GenericParameters(text);
                for(String name : parameters.getParameterNameSet()) {
                    DefaultSettingType settingType = null;
                    if(name != null) {
                        settingType = DefaultSettingType.lookup(name);
                        if(settingType == null)
                            throw new IllegalArgumentException("Unknown setting name '" + name + "'.");
                    }

                    assistant.putSetting(settingType, parameters.getString(name));
                }
            }
        });
		parser.addNodelet("/aspectran/settings/setting", (node, attributes, text) -> {
            String name = attributes.get("name");
            String value = attributes.get("value");

            DefaultSettingType settingType = null;

            if(name != null) {
                settingType = DefaultSettingType.lookup(name);

                if(settingType == null)
                    throw new IllegalArgumentException("Unknown setting name '" + name + "'");
            }

            assistant.putSetting(settingType, (text == null) ? value : text);
        });
		parser.addNodelet("/aspectran/settings/end()", (node, attributes, text) -> {
            assistant.applySettings();
        });
	}

	/**
	 * Adds the type alias nodelets.
	 */
	private void addTypeAliasNodelets() {
		parser.addNodelet("/aspectran/typeAliases", (node, attributes, text) -> {
            if(StringUtils.hasLength(text)) {
                Parameters parameters = new GenericParameters(text);
                for(String alias : parameters.getParameterNameSet()) {
                    assistant.addTypeAlias(alias, parameters.getString(alias));
                }
            }
        });
		parser.addNodelet("/aspectran/typeAliases/typeAlias", (node, attributes, text) -> {
            String alias = attributes.get("alias");
            String type = attributes.get("type");

            assistant.addTypeAlias(alias, type);
        });
	}

	/**
	 * Adds the aspect rule nodelets.
	 */
	private void addAspectNodelets() {
		parser.addNodelet("/aspectran", new AspectNodeletAdder(assistant));
		
	}

	/**
	 * Adds the bean nodelets.
	 */
	private void addBeanNodelets() {
		parser.addNodelet("/aspectran", new BeanNodeletAdder(assistant));
	}

	/**
	 * Adds the template nodelets.
	 */
	private void addTemplateNodelets() {
		parser.addNodelet("/aspectran", new TemplateNodeletAdder(assistant));
	}

	/**
	 * Adds the translet nodelets.
	 */
	private void addTransletNodelets() {
		parser.addNodelet("/aspectran", new TransletNodeletAdder(assistant));
	}

	/**
	 * Adds the import nodelets.
	 */
	private void addImportNodelets() {
		parser.addNodelet("/aspectran/import", (node, attributes, text) -> {
			String file = attributes.get("file");
			String resource = attributes.get("resource");
			String url = attributes.get("url");
			String fileType = attributes.get("fileType");

			ImportHandler importHandler = assistant.getImportHandler();
			if(importHandler != null) {
				Importer importer = assistant.newImporter(file, resource, url, fileType);
				if(importer == null) {
					throw new IllegalArgumentException("The <import> element requires either a 'file' or a 'resource' or a 'url' attribute.");
				}
				importHandler.pending(importer);
			}
		});
	}

}
