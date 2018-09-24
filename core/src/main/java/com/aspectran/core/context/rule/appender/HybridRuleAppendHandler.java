/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
import com.aspectran.core.context.rule.converter.ParamsToRuleConverter;
import com.aspectran.core.context.rule.converter.RuleToParamsConverter;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.context.rule.params.RootParameters;
import com.aspectran.core.context.rule.parser.xml.AspectranNodeParser;
import com.aspectran.core.context.rule.type.AppenderFileFormatType;
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

    private final boolean hybridLoad;

    private AspectranNodeParser aspectranNodeParser;

    private ParamsToRuleConverter ruleConverter;

    public HybridRuleAppendHandler(ContextRuleAssistant assistant, String encoding, boolean hybridLoad) {
        super(assistant);

        this.encoding = encoding;
        this.hybridLoad = hybridLoad;
    }

    @Override
    public void handle(RuleAppender appender) throws Exception {
        setCurrentRuleAppender(appender);

        AssistantLocal assistantLocal = getContextRuleAssistant().backupAssistantLocal();
        boolean hybridon = false;

        if (appender != null) {
            if (appender.getAppenderType() == AppenderType.PARAMETERS) {
                AspectranParameters aspectranParameters = appender.getAppendRule().getAspectranParameters();
                RootParameters rootParameters = new RootParameters(aspectranParameters);
                getRuleConverter().convertAsRule(rootParameters);
            } else if (appender.getAppenderFileFormatType() == AppenderFileFormatType.APON) {
                RootParameters rootParameters = AponReader.parse(appender.getReader(encoding), new RootParameters());
                getRuleConverter().convertAsRule(rootParameters);
            } else {
                if (hybridLoad && appender.getAppenderType() == AppenderType.FILE) {
                    File aponFile = makeAponFile((FileRuleAppender)appender);

                    if (appender.getLastModified() == aponFile.lastModified()) {
                        log.info("Rapid configuration loading with an APON file: " + aponFile);

                        hybridon = true;

                        RootParameters rootParameters = AponReader.parse(aponFile, encoding, new RootParameters());
                        getRuleConverter().convertAsRule(rootParameters);
                    }
                }

                if (!hybridon) {
                    if (aspectranNodeParser == null) {
                        aspectranNodeParser = new AspectranNodeParser(getContextRuleAssistant());
                    }
                    aspectranNodeParser.parse(appender);
                }
            }
        }

        super.handle();

        // The first default settings will remain after all configuration settings have been completed.
        if (assistantLocal.getReplicatedCount() > 0) {
            getContextRuleAssistant().restoreAssistantLocal(assistantLocal);
        }

        if (appender != null) {
            if (!hybridon && hybridLoad) {
                if (appender.getAppenderType() == AppenderType.FILE
                        && appender.getAppenderFileFormatType() == AppenderFileFormatType.XML) {
                    //appender.setProfiles(null);
                    saveAsAponFormatted((FileRuleAppender)appender);
                }
            }
        }
    }

    private ParamsToRuleConverter getRuleConverter() {
        if (ruleConverter == null) {
            ruleConverter = new ParamsToRuleConverter(getContextRuleAssistant());
        }
        return ruleConverter;
    }

    private void saveAsAponFormatted(FileRuleAppender fileRuleAppender) throws Exception {
        log.info("Save as APON formatted " + fileRuleAppender);

        File aponFile = null;
        try {
            aponFile = makeAponFile(fileRuleAppender);

            AponWriter aponWriter;
            if (encoding != null) {
                OutputStream outputStream = new FileOutputStream(aponFile);
                aponWriter = new AponWriter(new OutputStreamWriter(outputStream, encoding));
            } else {
                aponWriter = new AponWriter(new FileWriter(aponFile));
            }

            try {
                ContextRuleAssistant assistant = new ShallowContextRuleAssistant();
                assistant.ready();

                AspectranNodeParser parser = new AspectranNodeParser(assistant, false, false);
                parser.parse(fileRuleAppender);

                RuleToParamsConverter paramsConverter = new RuleToParamsConverter(assistant);
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

            aponFile.setLastModified(fileRuleAppender.getLastModified());
        } catch (Exception e) {
            log.error("Failed to save the converted APON format to file: " + aponFile, e);
        }
    }

    private File makeAponFile(FileRuleAppender fileRuleAppender) {
        String basePath = fileRuleAppender.getBasePath();
        String filePath = fileRuleAppender.getFilePath() + "." + AppenderFileFormatType.APON.toString();
        return new File(basePath, filePath);
    }

}
