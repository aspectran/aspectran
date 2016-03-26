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
package com.aspectran.core.context.builder.importer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.aspectran.core.context.builder.AssistantLocal;
import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.context.builder.ShallowContextBuilderAssistant;
import com.aspectran.core.context.builder.apon.RootAponAssembler;
import com.aspectran.core.context.builder.apon.RootAponDisassembler;
import com.aspectran.core.context.builder.apon.params.RootParameters;
import com.aspectran.core.context.builder.xml.AspectranNodeParser;
import com.aspectran.core.context.rule.type.ImportFileType;
import com.aspectran.core.context.rule.type.ImporterType;
import com.aspectran.core.util.apon.AponDeserializer;
import com.aspectran.core.util.apon.AponSerializer;
import com.aspectran.core.util.apon.Parameters;

/**
 * The Class HybridImportHandler.
 */
public class HybridImportHandler extends AbstractImportHandler {
	
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

	@Override
	public void handle(Importer importer) throws Exception {
		AssistantLocal assistantLocal = assistant.backupAssistantLocal();
		
		boolean hybridon = false;
		
		if(importer.getImportFileType() == ImportFileType.APON) {
			Parameters rootParameters = AponDeserializer.deserialize(importer.getReader(encoding), new RootParameters());
			
			if(rootAponDisassembler == null)
				rootAponDisassembler = new RootAponDisassembler(assistant);
			
			rootAponDisassembler.disassembleAspectran(rootParameters);
		} else {
			if(hybridLoading && importer.getImporterType() == ImporterType.FILE) {
				File aponFile = makeAponFile((FileImporter)importer);

				if(importer.getLastModified() == aponFile.lastModified()) {
					log.info("Rapid loading for Aspectran Context Configuration: " + aponFile);

					hybridon = true;

					Parameters rootParameters = AponDeserializer.deserialize(aponFile, encoding, new RootParameters());
					
					if(rootAponDisassembler == null) {
						rootAponDisassembler = new RootAponDisassembler(assistant);
					}
					rootAponDisassembler.disassembleRoot(rootParameters);
				}
			}
			
			if(!hybridon) {
				if(aspectranNodeParser == null) {
					aspectranNodeParser = new AspectranNodeParser(assistant);
				}
				aspectranNodeParser.parse(importer.getInputStream());
			}
		}
		
		handle();

		// First default setting is held after configuration loading is completed.
		if(assistantLocal.getReplicatedCount() > 0) {
			assistant.restoreAssistantLocal(assistantLocal);
		}
		
		if(!hybridon && hybridLoading) {
			if(importer.getImporterType() == ImporterType.FILE && importer.getImportFileType() == ImportFileType.XML) {
				saveAsAponFormat((FileImporter)importer);
			}
		}
	}
	
	private void saveAsAponFormat(FileImporter fileImporter) throws Exception {
		log.info("Save as Apon Format: " + fileImporter);
		
		File aponFile = null;
		
		try {
			aponFile = makeAponFile(fileImporter);
			
			AponSerializer writer;
			
			if(encoding != null) {
				OutputStream outputStream = new FileOutputStream(aponFile);
				writer = new AponSerializer(new OutputStreamWriter(outputStream, encoding));
			} else {
				writer = new AponSerializer(new FileWriter(aponFile));
			}
			
			try {
				ContextBuilderAssistant assistant = new ShallowContextBuilderAssistant();
				AspectranNodeParser parser = new AspectranNodeParser(assistant, false);
				parser.parse(fileImporter.getInputStream());
				
				RootAponAssembler assembler = new RootAponAssembler(assistant);
				Parameters rootParameters = assembler.assembleRoot();
				
				writer.comment(aponFile.getAbsolutePath());
				writer.write(rootParameters);
			} finally {
				writer.close();
			}
			
			aponFile.setLastModified(fileImporter.getLastModified());
		} catch(Exception e) {
			log.error("Cannot save file " +  aponFile + " as APON Format.", e);
		}
	}
	
	private File makeAponFile(FileImporter fileImporter) {
		String basePath = fileImporter.getBasePath();
		String filePath = fileImporter.getFilePath() + "." + ImportFileType.APON.toString();

		return new File(basePath, filePath);
	}

}
