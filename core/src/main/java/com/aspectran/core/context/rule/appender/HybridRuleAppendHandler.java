/*
 * Copyright (c) 2008-present The Aspectran Project
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

import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.converter.ParametersToRules;
import com.aspectran.core.context.rule.converter.RulesToParameters;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.context.rule.params.RootParameters;
import com.aspectran.core.context.rule.parser.ActivityContextRuleParser;
import com.aspectran.core.context.rule.parser.FileAppendedListener;
import com.aspectran.core.context.rule.parser.xml.AspectranDtdResolver;
import com.aspectran.core.context.rule.parser.xml.AspectranNodeParser;
import com.aspectran.core.context.rule.parser.xml.AspectranNodeParsingContext;
import com.aspectran.core.context.rule.parsing.RuleParsingContext;
import com.aspectran.core.context.rule.parsing.RuleParsingScope;
import com.aspectran.core.context.rule.parsing.ShallowRuleParsingContext;
import com.aspectran.core.context.rule.type.AppendableFileFormatType;
import com.aspectran.core.context.rule.type.AppenderType;
import com.aspectran.utils.apon.AponWriter;
import com.aspectran.utils.apon.XmlToParameters;
import org.jspecify.annotations.NonNull;
import org.xml.sax.EntityResolver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;

/**
 * A {@link RuleAppendHandler} for hybrid parsing of XML and APON formats.
 */
public class HybridRuleAppendHandler extends AbstractAppendHandler {

    private final ActivityContextRuleParser activityContextParser;

    private final String encoding;

    private EntityResolver entityResolver;

    private FileAppendedListener fileAppendedListener;

    /**
     * Instantiates a new HybridRuleAppendHandler.
     * @param activityContextRuleParser the activity context rule parser
     * @param encoding the encoding
     */
    public HybridRuleAppendHandler(@NonNull ActivityContextRuleParser activityContextRuleParser, String encoding) {
        super(activityContextRuleParser.getRuleParsingContext());
        this.activityContextParser = activityContextRuleParser;
        this.encoding = encoding;
    }

    /**
     * Sets the listener for receiving file appended events.
     * @param listener the listener to be notified when a file is appended
     */
    public void setFileAppendedListener(FileAppendedListener listener) {
        this.fileAppendedListener = listener;
    }

    @Override
    public void handle(RuleAppender appender) throws Exception {
        setCurrentRuleAppender(appender);
        RuleParsingScope ruleParsingScope = getRuleParsingContext().backupRuleParsingScope();

        if (appender != null) {
            if (fileAppendedListener != null && appender.getAppenderType() == AppenderType.FILE) {
                fileAppendedListener.onFileAppended(makeFile((FileRuleAppender)appender));
            }

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
                activityContextParser.getAspectranNodeParser().parse(appender);
                if (isDebugMode() && appender.getAppenderType() == AppenderType.FILE) {
                    FileRuleAppender fileRuleAppender = (FileRuleAppender)appender;
                    saveAsAponFile(fileRuleAppender);
                }
            }

            AppendRule appendRule = appender.getAppendRule();
            if (appendRule != null && appendRule.hasChildRules()) {
                applyScopedOverrides(appender, appendRule, getRuleParsingContext().getRuleParsingScope());
            }
        }

        super.handle();

        // The first default settings will remain after all configuration settings have been completed.
        if (ruleParsingScope.getNestingLevel() > 0) {
            getRuleParsingContext().restoreRuleParsingScope(ruleParsingScope);
        } else {
            getRuleParsingContext().setFirstFileParsed(true);
        }
    }

    private void applyScopedOverrides(RuleAppender appender, @NonNull AppendRule appendRule, @NonNull RuleParsingScope scope)
            throws IllegalRuleException {
        RuleParsingContext context = getRuleParsingContext();
        for (Object childRule : appendRule.getChildRules()) {
            if (childRule instanceof com.aspectran.core.context.rule.BeanRule beanRule) {
                String beanId = beanRule.getId();
                if (beanId != null && !scope.hasScopedBeanId(beanId)) {
                    throw new IllegalRuleException("Target bean '" + beanId +
                            "' to override was not found in the appended scope [" + appender + "]");
                }
                context.resolveBeanClass(beanRule);
                context.resolveFactoryBeanClass(beanRule);
                context.getBeanRuleRegistry().addBeanRule(beanRule);
            } else if (childRule instanceof com.aspectran.core.context.rule.EnvironmentRule environmentRule) {
                if (environmentRule.getPropertyItemRuleMap() != null) {
                    for (com.aspectran.core.context.rule.ItemRule itemRule : environmentRule.getPropertyItemRuleMap().values()) {
                        String key = itemRule.getName();
                        if (key != null && !scope.hasScopedPropertyKey(key)) {
                            throw new IllegalRuleException("Target environment property '" + key +
                                    "' to override was not found in the appended scope [" + appender + "]");
                        }
                    }
                }
                context.getEnvironmentRules().add(environmentRule);
            } else if (childRule instanceof com.aspectran.core.context.rule.TypeAliasRule typeAliasRule) {
                String alias = typeAliasRule.getAlias();
                if (alias != null && !scope.hasScopedTypeAlias(alias)) {
                    throw new IllegalRuleException("Target typeAlias '" + alias +
                            "' to override was not found in the appended scope [" + appender + "]");
                }
                context.getTypeAliases().put(alias, typeAliasRule.getType());
            } else if (childRule instanceof com.aspectran.core.context.rule.TransletRule transletRule) {
                String name = transletRule.getName();
                if (name != null && !scope.hasScopedTransletName(name)) {
                    throw new IllegalRuleException("Target translet '" + name +
                            "' to override was not found in the appended scope [" + appender + "]");
                }
                context.getTransletRuleRegistry().addTransletRule(transletRule);
            } else if (childRule instanceof com.aspectran.core.context.rule.AspectRule aspectRule) {
                String id = aspectRule.getId();
                if (id != null && !scope.hasScopedAspectId(id)) {
                    throw new IllegalRuleException("Target aspect '" + id +
                            "' to override was not found in the appended scope [" + appender + "]");
                }
                context.getAspectRuleRegistry().addAspectRule(aspectRule);
            } else if (childRule instanceof com.aspectran.core.context.rule.ScheduleRule scheduleRule) {
                String id = scheduleRule.getId();
                if (id != null && !scope.hasScopedScheduleId(id)) {
                    throw new IllegalRuleException("Target schedule '" + id +
                            "' to override was not found in the appended scope [" + appender + "]");
                }
                context.getScheduleRuleRegistry().addScheduleRule(scheduleRule);
            } else if (childRule instanceof com.aspectran.core.context.rule.TemplateRule templateRule) {
                String id = templateRule.getId();
                if (id != null && !scope.hasScopedTemplateId(id)) {
                    throw new IllegalRuleException("Target template '" + id +
                            "' to override was not found in the appended scope [" + appender + "]");
                }
                context.getTemplateRuleRegistry().addTemplateRule(templateRule);
            }
        }
    }

    private EntityResolver getEntityResolver() {
        if (entityResolver == null) {
            entityResolver = new AspectranDtdResolver(false);
        }
        return entityResolver;
    }

    private void convertToRules(RootParameters rootParameters) throws IllegalRuleException {
        new ParametersToRules(getRuleParsingContext()).toRules(rootParameters);
    }

    private void saveAsAponFile(FileRuleAppender fileRuleAppender) throws IOException {
        RuleParsingContext ruleParsingContext = null;
        RootParameters rootParameters;
        try {
            ruleParsingContext = new ShallowRuleParsingContext(getRuleParsingContext().getClassLoader());
            ruleParsingContext.prepare();

            AspectranNodeParser parser = new AspectranNodeParser(ruleParsingContext, false, false);
            try {
                AspectranNodeParsingContext.set(parser);
                parser.parse(fileRuleAppender);
            } finally {
                AspectranNodeParsingContext.clear();
            }

            rootParameters = RulesToParameters.toRootParameters(ruleParsingContext);
        } catch (Exception e) {
            throw new IOException("Failed to convert as Root Parameters: " + fileRuleAppender, e);
        } finally {
            if (ruleParsingContext != null) {
                ruleParsingContext.release();
            }
        }

        saveAsAponFile(fileRuleAppender, rootParameters);
    }

    private void saveAsAponFile(FileRuleAppender fileRuleAppender, RootParameters rootParameters) throws IOException {
        File xmlFile = makeFile(fileRuleAppender);
        File aponFile = makeAponFile(fileRuleAppender);

        if (logger.isDebugEnabled()) {
            logger.debug("Save as APON file: {}", aponFile);
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
                    logger.error("Exception during closing file {}", aponFile, e);
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
