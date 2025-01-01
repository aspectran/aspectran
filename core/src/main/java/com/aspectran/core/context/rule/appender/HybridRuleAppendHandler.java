/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.core.context.rule.appender;

import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.core.context.rule.assistant.AssistantLocal;
import com.aspectran.core.context.rule.assistant.ShallowContextRuleAssistant;
import com.aspectran.core.context.rule.converter.ParametersToRules;
import com.aspectran.core.context.rule.converter.RulesToParameters;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.context.rule.params.RootParameters;
import com.aspectran.core.context.rule.parser.xml.AspectranDtdResolver;
import com.aspectran.core.context.rule.parser.xml.AspectranNodeParser;
import com.aspectran.core.context.rule.type.AppendableFileFormatType;
import com.aspectran.core.context.rule.type.AppenderType;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.apon.AponWriter;
import com.aspectran.utils.apon.XmlToParameters;
import org.xml.sax.EntityResolver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;

/**
 * The Class HybridRuleAppendHandler.
 */
public class HybridRuleAppendHandler extends AbstractAppendHandler {

    private final String encoding;

    private AspectranNodeParser aspectranNodeParser;

    private EntityResolver entityResolver;

    public HybridRuleAppendHandler(ActivityRuleAssistant assistant, String encoding) {
        super(assistant);
        this.encoding = encoding;
    }

    @Override
    public void handle(RuleAppender appender) throws Exception {
        setCurrentRuleAppender(appender);
        AssistantLocal assistantLocal = getContextRuleAssistant().backupAssistantLocal();

        if (appender != null) {
            if (appender.getAppenderType() == AppenderType.PARAMETERS) {
                AspectranParameters aspectranParameters = appender.getAppendRule().getAspectranParameters();
                RootParameters rootParameters = new RootParameters(aspectranParameters);
                convertToRules(rootParameters);
            } else if (appender.getAppendableFileFormatType() == AppendableFileFormatType.APON) {
                try (Reader reader = appender.getReader(encoding)) {
                    RootParameters rootParameters = new RootParameters(reader);
                    convertToRules(rootParameters);
                }
            } else if (isUseAponToLoadXml()) {
                // Using APON to load XML based configuration
                RootParameters rootParameters;
                if (appender.getAppenderType() == AppenderType.FILE) {
                    FileRuleAppender fileRuleAppender = (FileRuleAppender)appender;
                    rootParameters = XmlToParameters.from(fileRuleAppender.getFile(), RootParameters.class, getEntityResolver());
                    if (isDebugMode()) {
                        saveAsAponFile(fileRuleAppender, rootParameters);
                    }
                } else {
                    try (Reader reader = appender.getReader(encoding)) {
                        rootParameters = XmlToParameters.from(reader, RootParameters.class, getEntityResolver());
                    }
                }
                convertToRules(rootParameters);
            } else {
                // Using Nodelet to load XML based configuration: It is much faster than APON
                getAspectranNodeParser().parse(appender);
                if (isDebugMode() && appender.getAppenderType() == AppenderType.FILE) {
                    FileRuleAppender fileRuleAppender = (FileRuleAppender)appender;
                    saveAsAponFile(fileRuleAppender);
                }
            }
        }

        super.handle();

        // The first default settings will remain after all configuration settings have been completed.
        if (assistantLocal.getReplicatedCount() > 0) {
            getContextRuleAssistant().restoreAssistantLocal(assistantLocal);
        }
    }

    private AspectranNodeParser getAspectranNodeParser() {
        if (aspectranNodeParser == null) {
            aspectranNodeParser = new AspectranNodeParser(getContextRuleAssistant());
        }
        return aspectranNodeParser;
    }

    private EntityResolver getEntityResolver() {
        if (entityResolver == null) {
            entityResolver = new AspectranDtdResolver(false);
        }
        return entityResolver;
    }

    private void convertToRules(RootParameters rootParameters) throws IllegalRuleException {
        new ParametersToRules(getContextRuleAssistant()).toRules(rootParameters);
    }

    private void saveAsAponFile(FileRuleAppender fileRuleAppender) throws IOException {
        ActivityRuleAssistant assistant = null;
        RootParameters rootParameters;
        try {
            assistant = new ShallowContextRuleAssistant();
            assistant.prepare();

            AspectranNodeParser parser = new AspectranNodeParser(assistant, false, false);
            parser.parse(fileRuleAppender);

            rootParameters = RulesToParameters.toRootParameters(assistant);
        } catch (Exception e) {
            throw new IOException("Failed to convert as Root Parameters: " + fileRuleAppender, e);
        } finally {
            if (assistant != null) {
                assistant.release();
            }
        }

        saveAsAponFile(fileRuleAppender, rootParameters);
    }

    private void saveAsAponFile(FileRuleAppender fileRuleAppender, RootParameters rootParameters) throws IOException {
        File xmlFile = makeFile(fileRuleAppender);
        File aponFile = makeAponFile(fileRuleAppender);

        if (logger.isDebugEnabled()) {
            logger.debug("Save as APON file: " + aponFile);
        }

        try {
            AponWriter aponWriter;
            if (encoding != null) {
                OutputStream outputStream = new FileOutputStream(aponFile);
                aponWriter = new AponWriter(new OutputStreamWriter(outputStream, encoding));
            } else {
                aponWriter = new AponWriter(new FileWriter(aponFile));
            }
            aponWriter.nullWritable(false);

            try {
                aponWriter.comment(xmlFile.getAbsolutePath());
                aponWriter.write(rootParameters);
            } finally {
                try {
                    aponWriter.close();
                } catch (IOException e) {
                    logger.error("Exception during closing file " + aponFile, e);
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to save as APON file: " + aponFile, e);
        }
    }

    @NonNull
    private File makeFile(@NonNull FileRuleAppender fileRuleAppender) {
        String basePath = fileRuleAppender.getBasePath();
        String filePath = fileRuleAppender.getFilePath();
        return new File(basePath, filePath);
    }

    @NonNull
    private File makeAponFile(@NonNull FileRuleAppender fileRuleAppender) {
        String basePath = fileRuleAppender.getBasePath();
        String filePath = fileRuleAppender.getFilePath() + "." + AppendableFileFormatType.APON;
        return new File(basePath, filePath);
    }

}
