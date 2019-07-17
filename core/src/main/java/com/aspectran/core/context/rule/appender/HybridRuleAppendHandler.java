/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.context.rule.assistant.AssistantLocal;
import com.aspectran.core.context.rule.assistant.ContextRuleAssistant;
import com.aspectran.core.context.rule.assistant.ShallowContextRuleAssistant;
import com.aspectran.core.context.rule.converter.ParametersToRules;
import com.aspectran.core.context.rule.converter.RulesToParameters;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.context.rule.params.RootParameters;
import com.aspectran.core.context.rule.parser.xml.AspectranNodeParser;
import com.aspectran.core.context.rule.type.AppendedFileFormatType;
import com.aspectran.core.context.rule.type.AppenderType;
import com.aspectran.core.util.apon.AponWriter;
import com.aspectran.core.util.apon.XmlToApon;

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

    public HybridRuleAppendHandler(ContextRuleAssistant assistant, String encoding) {
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
                convertAsRules(rootParameters);
            } else if (appender.getAppendedFileFormatType() == AppendedFileFormatType.APON) {
                try (Reader reader = appender.getReader(encoding)) {
                    RootParameters rootParameters = new RootParameters(reader);
                    convertAsRules(rootParameters);
                }
            } else if (isUseAponToLoadXml()) {
                // Using APON to load XML configuration
                RootParameters rootParameters;
                if (appender.getAppenderType() == AppenderType.FILE) {
                    FileRuleAppender fileRuleAppender = (FileRuleAppender)appender;
                    rootParameters = XmlToApon.from(fileRuleAppender.getFile(), RootParameters.class);
                    if (isDebugMode()) {
                        saveAsAponFile(fileRuleAppender, rootParameters);
                    }
                } else {
                    try (Reader reader = appender.getReader(encoding)) {
                        rootParameters = XmlToApon.from(reader, RootParameters.class);
                    }
                }
                convertAsRules(rootParameters);
            } else {
                // Using Nodelet to load XML configuration: It is much faster than APON
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

    private void convertAsRules(RootParameters rootParameters) throws IllegalRuleException {
        new ParametersToRules(getContextRuleAssistant()).asRules(rootParameters);
    }

    private void saveAsAponFile(FileRuleAppender fileRuleAppender) throws IOException {
        ContextRuleAssistant assistant = null;
        RootParameters rootParameters;
        try {
            assistant = new ShallowContextRuleAssistant();
            assistant.ready();

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
        File aponFile = makeAponFile(fileRuleAppender);

        if (log.isDebugEnabled()) {
            log.debug("Save as APON file: " + aponFile);
        }

        try {
            AponWriter aponWriter;
            if (encoding != null) {
                OutputStream outputStream = new FileOutputStream(aponFile);
                aponWriter = new AponWriter(new OutputStreamWriter(outputStream, encoding)).nullWritable(false);
            } else {
                aponWriter = new AponWriter(new FileWriter(aponFile)).nullWritable(false);
            }

            try {
                aponWriter.comment(aponFile.getAbsolutePath());
                aponWriter.write(rootParameters);
            } finally {
                try {
                    aponWriter.close();
                } catch (IOException e) {
                    log.error("Exception during closing file " + aponFile, e);
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to save as APON file: " + aponFile, e);
        }
    }

    private File makeAponFile(FileRuleAppender fileRuleAppender) {
        String basePath = fileRuleAppender.getBasePath();
        String filePath = fileRuleAppender.getFilePath() + "." + AppendedFileFormatType.APON.toString();
        return new File(basePath, filePath);
    }

}
