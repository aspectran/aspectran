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

import com.aspectran.core.context.builder.apon.RootAponDisassembler;
import com.aspectran.core.context.builder.apon.params.RootParameters;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.apon.Parameters;

/**
 * Translet Map Parser.
 * 
 * <p>Created: 2008. 06. 14 오전 4:39:24</p>
 */
public class AponImportHandler extends AbstractImportHandler implements ImportHandler {
	
	private final ContextBuilderAssistant assistant;
	
	private final String encoding;
	
	private RootAponDisassembler rootAponDisassembler;
	
	public AponImportHandler(ContextBuilderAssistant assistant, String encoding) {
		this.assistant = assistant;
		this.encoding = encoding;
		
		rootAponDisassembler = new RootAponDisassembler(assistant);
	}
	
	public void handle(Importable importable) throws Exception {
		DefaultSettings defaultSettings = assistant.backupDefaultSettings();
		
		Reader reader = importable.getReader(encoding);
		AponReader aponReader = new AponReader();
		Parameters rootParameters = aponReader.read(reader, new RootParameters());
		reader.close();
		
		rootAponDisassembler.disassembleRoot(rootParameters);

		handle();

		assistant.restoreDefaultSettings(defaultSettings);
	}

}
