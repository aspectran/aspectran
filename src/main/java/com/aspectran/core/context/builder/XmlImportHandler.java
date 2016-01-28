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
package com.aspectran.core.context.builder;

import com.aspectran.core.context.builder.xml.AspectranNodeParser;

/**
 * The Class XmlImportHandler.
 */
public class XmlImportHandler extends AbstractImportHandler implements ImportHandler {
	
	private final ContextBuilderAssistant assistant;
	
	private AspectranNodeParser aspectranNodeParser;
	
	public XmlImportHandler(ContextBuilderAssistant assistant) {
		this.assistant = assistant;
		aspectranNodeParser = new AspectranNodeParser(assistant);
	}
	
	public void handle(Importable importable) throws Exception {
		AssistantLocal assistantLocal = assistant.backupAssistantLocal();
		
		aspectranNodeParser.parse(importable.getInputStream());
		
		handle();
		
		// First default setting is held after configuration loading is completed.
		if(assistantLocal.getCloneCount() > 0) {
			assistant.restoreAssistantLocal(assistantLocal);
		}
	}

}
