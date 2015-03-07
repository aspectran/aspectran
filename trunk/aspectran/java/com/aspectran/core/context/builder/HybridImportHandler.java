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
package com.aspectran.core.context.builder;

import java.io.Reader;

import com.aspectran.core.context.builder.apon.AspectranAponDisassembler;
import com.aspectran.core.context.builder.apon.params.AspectranParameters;
import com.aspectran.core.context.builder.xml.AspectranNodeParser;
import com.aspectran.core.context.rule.type.ImportFileType;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.apon.Parameters;

/**
 * Translet Map Parser.
 * 
 * <p>Created: 2008. 06. 14 오전 4:39:24</p>
 */
public class HybridImportHandler implements ImportHandler {
	
	private final ContextBuilderAssistant assistant;
	
	private final String encoding;
	
	public HybridImportHandler(ContextBuilderAssistant assistant, String encoding) {
		this.assistant = assistant;
		this.encoding = encoding;
	}
	
	public void handle(Importable importable) throws Exception {
		assistant.backupDefaultSettings();
		
		if(importable.getImportFileType() == ImportFileType.APON) {
			Reader reader = importable.getReader(encoding);
			AponReader aponReader = new AponReader();
			Parameters aspectranParameters = aponReader.read(reader, new AspectranParameters());
			reader.close();
			
			AspectranAponDisassembler aponDisassembler = new AspectranAponDisassembler(assistant);
			aponDisassembler.disassembleAspectran(aspectranParameters);
		} else {
			AspectranNodeParser aspectranNodeParser = new AspectranNodeParser(assistant);
			aspectranNodeParser.parse(importable.getInputStream());
		}

		assistant.restoreDefaultSettings();
	}

}
