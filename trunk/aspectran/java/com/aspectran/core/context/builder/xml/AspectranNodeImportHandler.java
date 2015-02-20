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
package com.aspectran.core.context.builder.xml;

import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.context.builder.DefaultSettings;
import com.aspectran.core.context.builder.ImportHandler;
import com.aspectran.core.context.builder.Importable;

/**
 * Translet Map Parser.
 * 
 * <p>Created: 2008. 06. 14 오전 4:39:24</p>
 */
public class AspectranNodeImportHandler implements ImportHandler {
	
	private final ContextBuilderAssistant assistant;
	
	public AspectranNodeImportHandler(ContextBuilderAssistant assistant) {
		this.assistant = assistant;
	}
	
	public void handle(Importable importable) throws Exception {
		DefaultSettings defaultSettings = assistant.getDefaultSettings().clone();
		
		AspectranNodeParser aspectranNodeParser = new AspectranNodeParser(assistant);
		aspectranNodeParser.parse(importable);
		
		assistant.setDefaultSettings(defaultSettings);
	}

}
