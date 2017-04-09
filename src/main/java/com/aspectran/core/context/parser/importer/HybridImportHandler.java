/*
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.core.context.parser.importer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.aspectran.core.context.parser.ActivityContextParser;
import com.aspectran.core.context.parser.apon.RootAponAssembler;
import com.aspectran.core.context.parser.apon.RootAponDisassembler;
import com.aspectran.core.context.parser.apon.params.RootParameters;
import com.aspectran.core.context.parser.assistant.AssistantLocal;
import com.aspectran.core.context.parser.assistant.ContextParserAssistant;
import com.aspectran.core.context.parser.assistant.ShallowContextParserAssistant;
import com.aspectran.core.context.parser.xml.AspectranNodeParser;
import com.aspectran.core.context.rule.type.ImporterFileFormatType;
import com.aspectran.core.context.rule.type.ImporterType;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.apon.AponWriter;
import com.aspectran.core.util.apon.Parameters;

/**
 * The Class HybridImportHandler.
 */
public class HybridImportHandler extends AbstractImportHandler {

    private final ContextParserAssistant assistant;

    private final String encoding;

    private final boolean hybridLoad;

    private AspectranNodeParser aspectranNodeParser;

    private RootAponDisassembler rootAponDisassembler;

    public HybridImportHandler(ActivityContextParser builder, String encoding, boolean hybridLoad) {
        super(builder.getContextEnvironment());

        this.assistant = builder.getContextBuilderAssistant();
        this.encoding = encoding;
        this.hybridLoad = hybridLoad;
    }

    @Override
    public void handle(Importer importer) throws Exception {
        AssistantLocal assistantLocal = assistant.backupAssistantLocal();

        boolean hybridon = false;

        if (importer != null) {
            if (importer.getImporterFileFormatType() == ImporterFileFormatType.APON) {
                Parameters rootParameters = AponReader.parse(importer.getReader(encoding), new RootParameters());

                if (rootAponDisassembler == null) {
                    rootAponDisassembler = new RootAponDisassembler(assistant);
                }
                rootAponDisassembler.disassembleAspectran(rootParameters);
            } else {
                if (hybridLoad && importer.getImporterType() == ImporterType.FILE) {
                    File aponFile = makeAponFile((FileImporter)importer);

                    if (importer.getLastModified() == aponFile.lastModified()) {
                        log.info("Rapid configuration loading with an APON file: " + aponFile);

                        hybridon = true;

                        Parameters rootParameters = AponReader.parse(aponFile, encoding, new RootParameters());

                        if (rootAponDisassembler == null) {
                            rootAponDisassembler = new RootAponDisassembler(assistant);
                        }
                        rootAponDisassembler.disassembleRoot(rootParameters);
                    }
                }

                if (!hybridon) {
                    if (aspectranNodeParser == null) {
                        aspectranNodeParser = new AspectranNodeParser(assistant);
                    }
                    aspectranNodeParser.parse(importer.getInputStream());
                }
            }
        }

        super.handle();

        // The first default settings will remain after all configuration settings have been completed.
        if (assistantLocal.getReplicatedCount() > 0) {
            assistant.restoreAssistantLocal(assistantLocal);
        }

        if (importer != null) {
            if (!hybridon && hybridLoad) {
                if (importer.getImporterType() == ImporterType.FILE && importer.getImporterFileFormatType() == ImporterFileFormatType.XML) {
                    importer.setProfiles(null);
                    saveAsAponFormat((FileImporter)importer);
                }
            }
        }
    }

    private void saveAsAponFormat(FileImporter fileImporter) throws Exception {
        log.info("Save as APON format " + fileImporter);

        File aponFile = null;

        try {
            aponFile = makeAponFile(fileImporter);

            AponWriter aponWriter;
            if (encoding != null) {
                OutputStream outputStream = new FileOutputStream(aponFile);
                aponWriter = new AponWriter(new OutputStreamWriter(outputStream, encoding));
            } else {
                aponWriter = new AponWriter(new FileWriter(aponFile));
            }

            try {
                ContextParserAssistant assistant = new ShallowContextParserAssistant();
                assistant.ready();

                AspectranNodeParser parser = new AspectranNodeParser(assistant, false);
                parser.parse(fileImporter.getInputStream());

                RootAponAssembler assembler = new RootAponAssembler(assistant);
                Parameters rootParameters = assembler.assembleRootParameters();

                aponWriter.comment(aponFile.getAbsolutePath());
                aponWriter.write(rootParameters);

                assistant.release();
            } finally {
                try {
                    aponWriter.close();
                } catch (IOException e) {
                    // ignore
                }
            }

            aponFile.setLastModified(fileImporter.getLastModified());
        } catch (Exception e) {
            log.error("Failed to save the converted APON format to file: " + aponFile, e);
        }
    }

    private File makeAponFile(FileImporter fileImporter) {
        String basePath = fileImporter.getBasePath();
        String filePath = fileImporter.getFilePath() + "." + ImporterFileFormatType.APON.toString();

        return new File(basePath, filePath);
    }

}
