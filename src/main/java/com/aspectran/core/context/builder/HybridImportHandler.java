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
package com.aspectran.core.context.builder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.aspectran.core.context.builder.apon.RootAponAssembler;
import com.aspectran.core.context.builder.apon.RootAponDisassembler;
import com.aspectran.core.context.builder.apon.params.RootParameters;
import com.aspectran.core.context.builder.xml.AspectranNodeParser;
import com.aspectran.core.context.rule.type.ImportFileType;
import com.aspectran.core.context.rule.type.ImportType;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.apon.AponWriter;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class HybridImportHandler.
 */
public class HybridImportHandler extends AbstractImportHandler implements ImportHandler {
	
	private static final Log log = LogFactory.getLog(HybridImportHandler.class);
	
	private final ContextBuilderAssistant assistant;
	
	private final String encoding;
	
	private final boolean hybridLoading;
	
	private AspectranNodeParser aspectranNodeParser;
	
	private RootAponDisassembler rootAponDisassembler;
	
	public HybridImportHandler(ContextBuilderAssistant assistant, String encoding, boolean hybridLoading) {
		this.assistant = assistant;
		this.encoding = encoding;
		this.hybridLoading = hybridLoading;
	}
	
	public void handle(Importable importable) throws Exception {
		AssistantLocal assistantLocal = assistant.backupAssistantLocal();
		
		boolean hybridon = false;
		
		if(importable.getImportFileType() == ImportFileType.APON) {
			Parameters rootParameters = AponReader.read(importable.getReader(encoding), new RootParameters());
			
			if(rootAponDisassembler == null)
				rootAponDisassembler = new RootAponDisassembler(assistant);
			
			rootAponDisassembler.disassembleAspectran(rootParameters);
		} else {
			if(hybridLoading && importable.getImportType() == ImportType.FILE) {
				File aponFile = makeAponFile((ImportableFile)importable);

				if(importable.getLastModified() == aponFile.lastModified()) {
					log.info("Rapid Aspectran Context Configuration Loading: " + aponFile);
					hybridon = true;

					Parameters rootParameters = AponReader.read(aponFile, encoding, new RootParameters());
					
					if(rootAponDisassembler == null)
						rootAponDisassembler = new RootAponDisassembler(assistant);
					
					rootAponDisassembler.disassembleRoot(rootParameters);
				}
			}
			
			if(!hybridon) {
				if(aspectranNodeParser == null)
					aspectranNodeParser = new AspectranNodeParser(assistant);
				
				aspectranNodeParser.parse(importable.getInputStream());
			}
		}
		
		handle();

		// First default setting is held after configuration loading is completed.
		if(assistantLocal.getCloneCount() > 0) {
			assistant.restoreAssistantLocal(assistantLocal);
		}
		
		if(!hybridon && hybridLoading) {
			if(importable.getImportType() == ImportType.FILE && importable.getImportFileType() == ImportFileType.XML) {
				saveAsAponFormat((ImportableFile)importable);
			}
		}
	}
	
	private void saveAsAponFormat(ImportableFile importableFile) throws Exception {
		log.info("Save as Apon Format: " + importableFile);
		
		File aponFile = null;
		
		try {
			aponFile = makeAponFile(importableFile);
			
			AponWriter writer;
			
			if(encoding != null) {
				OutputStream outputStream = new FileOutputStream(aponFile);
				writer = new AponWriter(new OutputStreamWriter(outputStream, encoding));
			} else {
				writer = new AponWriter(new FileWriter(aponFile));
			}
			
			try {
				ContextBuilderAssistant assistant = new ShallowContextBuilderAssistant();
				AspectranNodeParser parser = new AspectranNodeParser(assistant, false);
				parser.parse(importableFile.getInputStream());
				
				RootAponAssembler assembler = new RootAponAssembler(assistant);
				Parameters rootParameters = assembler.assembleRoot();
				
				writer.comment(aponFile.getAbsolutePath());
				writer.write(rootParameters);
			} finally {
				writer.close();
			}
			
			aponFile.setLastModified(importableFile.getLastModified());
		} catch(Exception e) {
			log.error("Cannot save file " +  aponFile + " to APON Format.", e);
		}
	}
	
	private File makeAponFile(ImportableFile importableFile) {
		String basePath = importableFile.getBasePath();
		String filePath = importableFile.getFilePath() + "." + ImportFileType.APON.toString();
		File file = new File(basePath, filePath);
		
		return file;
	}

}
