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
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.apon.AponWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * The Class HybridRuleAppendHandler.
 */
public class HybridRuleAppendHandler extends AbstractAppendHandler {

    private final String encoding;

    private AspectranNodeParser aspectranNodeParser;

    private ParametersToRules ruleConverter;

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
                getRuleConverter().asRules(rootParameters);
            } else if (appender.getAppendedFileFormatType() == AppendedFileFormatType.APON) {
                RootParameters rootParameters = AponReader.parse(appender.getReader(encoding), new RootParameters());
                getRuleConverter().asRules(rootParameters);
            } else {
                /* TODO Using APON to load XML configuration
                RootParameters rootParameters;
                if (appender.getAppenderType() == AppenderType.FILE) {
                    FileRuleAppender fileRuleAppender = (FileRuleAppender)appender;
                    rootParameters = XmlToApon.from(fileRuleAppender.getFile(), RootParameters.class);
                } else {
                    rootParameters = XmlToApon.from(appender.getReader(encoding), RootParameters.class);
                }
                getRuleConverter().asRules(rootParameters);
                */

                if (aspectranNodeParser == null) {
                    aspectranNodeParser = new AspectranNodeParser(getContextRuleAssistant());
                }
                aspectranNodeParser.parse(appender);
            }
        }

        super.handle();

        // The first default settings will remain after all configuration settings have been completed.
        if (assistantLocal.getReplicatedCount() > 0) {
            getContextRuleAssistant().restoreAssistantLocal(assistantLocal);
        }

        if (appender != null && isDebugMode()) {
            if (appender.getAppenderType() == AppenderType.FILE
                    && appender.getAppendedFileFormatType() == AppendedFileFormatType.XML) {
                saveAsAponFile((FileRuleAppender)appender);
            }
        }
    }

    private ParametersToRules getRuleConverter() {
        if (ruleConverter == null) {
            ruleConverter = new ParametersToRules(getContextRuleAssistant());
        }
        return ruleConverter;
    }

    private void saveAsAponFile(FileRuleAppender fileRuleAppender) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Save as APON file: " + fileRuleAppender);
        }

        File aponFile = null;
        try {
            aponFile = makeAponFile(fileRuleAppender);

            AponWriter aponWriter;
            if (encoding != null) {
                OutputStream outputStream = new FileOutputStream(aponFile);
                aponWriter = new AponWriter(new OutputStreamWriter(outputStream, encoding)).nullWritable(false);
            } else {
                aponWriter = new AponWriter(new FileWriter(aponFile)).nullWritable(false);
            }

            try {
                ContextRuleAssistant assistant = new ShallowContextRuleAssistant();
                assistant.ready();

                AspectranNodeParser parser = new AspectranNodeParser(assistant, false, false);
                parser.parse(fileRuleAppender);

                RulesToParameters paramsConverter = new RulesToParameters(assistant);
                RootParameters rootParameters = paramsConverter.toRootParameters();

                aponWriter.comment(aponFile.getAbsolutePath());
                aponWriter.write(rootParameters);

                assistant.release();
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
