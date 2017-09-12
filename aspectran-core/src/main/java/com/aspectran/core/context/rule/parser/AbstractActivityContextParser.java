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
package com.aspectran.core.context.rule.parser;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RegulatedApplicationAdapter;
import com.aspectran.core.component.aspect.AspectAdviceRulePostRegister;
import com.aspectran.core.component.aspect.AspectAdviceRulePreRegister;
import com.aspectran.core.component.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.component.aspect.AspectRuleRegistry;
import com.aspectran.core.component.aspect.InvalidPointcutPatternException;
import com.aspectran.core.component.aspect.pointcut.Pointcut;
import com.aspectran.core.component.aspect.pointcut.PointcutFactory;
import com.aspectran.core.component.bean.BeanRuleRegistry;
import com.aspectran.core.component.bean.ContextBeanRegistry;
import com.aspectran.core.component.schedule.ScheduleRuleRegistry;
import com.aspectran.core.component.template.ContextTemplateProcessor;
import com.aspectran.core.component.template.TemplateRuleRegistry;
import com.aspectran.core.component.translet.TransletRuleRegistry;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.AspectranActivityContext;
import com.aspectran.core.context.env.ContextEnvironment;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.EnvironmentRule;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.PointcutRule;
import com.aspectran.core.context.rule.appender.FileRuleAppender;
import com.aspectran.core.context.rule.appender.ResourceRuleAppender;
import com.aspectran.core.context.rule.appender.RuleAppender;
import com.aspectran.core.context.rule.assistant.BeanReferenceException;
import com.aspectran.core.context.rule.assistant.BeanReferenceInspector;
import com.aspectran.core.context.rule.assistant.ContextRuleAssistant;
import com.aspectran.core.context.rule.type.AppenderFileFormatType;
import com.aspectran.core.context.rule.type.BeanProxifierType;
import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.context.rule.type.JoinpointType;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.util.List;

/**
 * The Class AbstractActivityContextParser.
 * 
 * <p>Created: 2008. 06. 14 PM 8:53:29</p>
 */
abstract class AbstractActivityContextParser implements ActivityContextParser {

    protected final Log log = LogFactory.getLog(getClass());

    private final AspectranActivityContext activityContext;

    private final ContextEnvironment environment;

    private final ContextRuleAssistant assistant;

    private String encoding;

    private boolean hybridLoad;

    protected AbstractActivityContextParser(ApplicationAdapter applicationAdapter) {
        activityContext = new AspectranActivityContext(new RegulatedApplicationAdapter(applicationAdapter));
        environment = activityContext.getContextEnvironment();

        assistant = new ContextRuleAssistant(environment);
        assistant.ready();
    }

    @Override
    public ContextRuleAssistant getContextRuleAssistant() {
        return assistant;
    }

    @Override
    public void setActiveProfiles(String... activeProfiles) {
        if (activeProfiles != null) {
            log.info("Activating profiles [" + StringUtils.joinCommaDelimitedList(activeProfiles) + "]");
        }
        environment.setActiveProfiles(activeProfiles);
    }

    @Override
    public void setDefaultProfiles(String... defaultProfiles) {
        if (defaultProfiles != null) {
            log.info("Default profiles [" + StringUtils.joinCommaDelimitedList(defaultProfiles) + "]");
        }
        environment.setDefaultProfiles(defaultProfiles);
    }

    public String getEncoding() {
        return encoding;
    }

    @Override
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public boolean isHybridLoad() {
        return hybridLoad;
    }

    @Override
    public void setHybridLoad(boolean hybridLoad) {
        this.hybridLoad = hybridLoad;
    }

    /**
     * Returns a new instance of ActivityContext.
     *
     * @return the activity context
     * @throws BeanReferenceException will be thrown when cannot resolve reference to bean
     */
    protected ActivityContext createActivityContext() throws Exception {
        activityContext.setDescription(assistant.getAssistantLocal().getDescription());

        initContextEnvironment();

        AspectRuleRegistry aspectRuleRegistry = assistant.getAspectRuleRegistry();

        BeanRuleRegistry beanRuleRegistry = assistant.getBeanRuleRegistry();
        beanRuleRegistry.postProcess(assistant);

        ScheduleRuleRegistry scheduleRuleRegistry = assistant.getScheduleRuleRegistry();
        TemplateRuleRegistry templateRuleRegistry = assistant.getTemplateRuleRegistry();
        TransletRuleRegistry transletRuleRegistry = assistant.getTransletRuleRegistry();

        BeanReferenceInspector beanReferenceInspector = assistant.getBeanReferenceInspector();
        beanReferenceInspector.inspect(beanRuleRegistry);

        initAspectRuleRegistry(aspectRuleRegistry, beanRuleRegistry, transletRuleRegistry);

        BeanProxifierType beanProxifierType = BeanProxifierType.resolve((String)assistant.getSetting(DefaultSettingType.BEAN_PROXIFIER));
        ContextBeanRegistry contextBeanRegistry = new ContextBeanRegistry(activityContext, beanRuleRegistry, beanProxifierType);

        ContextTemplateProcessor contextTemplateProcessor = new ContextTemplateProcessor(activityContext, templateRuleRegistry);

        assistant.release();

        activityContext.setAspectRuleRegistry(aspectRuleRegistry);
        activityContext.setContextBeanRegistry(contextBeanRegistry);
        activityContext.setScheduleRuleRegistry(scheduleRuleRegistry);
        activityContext.setContextTemplateProcessor(contextTemplateProcessor);
        activityContext.setTransletRuleRegistry(transletRuleRegistry);

        return activityContext;
    }

    private void initContextEnvironment() {
        for (EnvironmentRule environmentRule : assistant.getEnvironmentRules()) {
            if (environmentRule.getPropertyItemRuleMap() != null) {
                String[] profiles = StringUtils.splitCommaDelimitedString(environmentRule.getProfile());
                if (environment.acceptsProfiles(profiles)) {
                    environment.addPropertyItemRuleMap(environmentRule.getPropertyItemRuleMap());
                }
            }
        }
    }

    /**
     * Initialize the aspect rule registry.
     *
     * @param aspectRuleRegistry the aspect rule registry
     * @param beanRuleRegistry the bean rule registry
     * @param transletRuleRegistry the translet rule registry
     */
    private void initAspectRuleRegistry(AspectRuleRegistry aspectRuleRegistry, BeanRuleRegistry beanRuleRegistry,
                                        TransletRuleRegistry transletRuleRegistry) {
        AspectAdviceRulePostRegister sessionScopeAspectAdviceRulePostRegister = new AspectAdviceRulePostRegister();

        for (AspectRule aspectRule : aspectRuleRegistry.getAspectRules()) {
            PointcutRule pointcutRule = aspectRule.getPointcutRule();
            if (pointcutRule != null) {
                Pointcut pointcut = PointcutFactory.createPointcut(pointcutRule);
                aspectRule.setPointcut(pointcut);
            }
            if (aspectRule.getJoinpointType() == JoinpointType.SESSION) {
                sessionScopeAspectAdviceRulePostRegister.register(aspectRule);
            }
        }

        AspectAdviceRulePreRegister preRegister = new AspectAdviceRulePreRegister(aspectRuleRegistry);
        preRegister.register(beanRuleRegistry);
        preRegister.register(transletRuleRegistry);

        // check offending pointcut pattern
        boolean pointcutPatternVerifiable = assistant.isPointcutPatternVerifiable();
        int offendingPointcutPatterns = 0;

        for (AspectRule aspectRule : aspectRuleRegistry.getAspectRules()) {
            Pointcut pointcut = aspectRule.getPointcut();

            if (pointcut != null) {
                List<PointcutPatternRule> pointcutPatternRuleList = pointcut.getPointcutPatternRuleList();

                if (pointcutPatternRuleList != null) {
                    for (PointcutPatternRule ppr : pointcutPatternRuleList) {
                        /*
                        if (ppr.getTransletNamePattern() != null && ppr.getMatchedTransletCount() == 0) {
                            offendingPointcutPatterns++;
                            String msg = "Incorrect pointcut pattern for translet name '" + ppr.getTransletNamePattern() + "' : aspectRule " + aspectRule;
                            if (pointcutPatternVerifiable)
                                log.error(msg);
                            else
                                log.warn(msg);
                        }
                        */
                        if (ppr.getBeanIdPattern() != null && ppr.getMatchedBeanCount() == 0) {
                            offendingPointcutPatterns++;
                            String msg = "Incorrect pointcut pattern for bean id '" + ppr.getBeanIdPattern() + "' : aspectRule " + aspectRule;
                            if (pointcutPatternVerifiable) {
                                log.error(msg);
                            } else {
                                log.warn(msg);
                            }
                        }
                        if (ppr.getClassNamePattern() != null && ppr.getMatchedClassCount() == 0) {
                            offendingPointcutPatterns++;
                            String msg = "Incorrect pointcut pattern for class name '" + ppr.getClassNamePattern() + "' : aspectRule " + aspectRule;
                            if (pointcutPatternVerifiable) {
                                log.error(msg);
                            } else {
                                log.warn(msg);
                            }
                        }
                        if (ppr.getMethodNamePattern() != null && ppr.getMatchedMethodCount() == 0) {
                            offendingPointcutPatterns++;
                            String msg = "Incorrect pointcut pattern for bean's method name '" + ppr.getMethodNamePattern() + "' : aspectRule " + aspectRule;
                            if (pointcutPatternVerifiable) {
                                log.error(msg);
                            } else {
                                log.warn(msg);
                            }
                        }
                    }
                }
            }
        }

        if (offendingPointcutPatterns > 0) {
            String msg = offendingPointcutPatterns + " Offending pointcut patterns; Please check the logs for more information";
            if (pointcutPatternVerifiable) {
                log.error(msg);
                throw new InvalidPointcutPatternException(msg);
            } else {
                log.warn(msg);
            }
        }

        AspectAdviceRuleRegistry sessionScopeAspectAdviceRuleRegistry = sessionScopeAspectAdviceRulePostRegister.getAspectAdviceRuleRegistry();
        if (sessionScopeAspectAdviceRuleRegistry != null) {
            aspectRuleRegistry.setSessionAspectAdviceRuleRegistry(sessionScopeAspectAdviceRuleRegistry);
        }
    }

    protected RuleAppender resolveAppender(String rootContext) {
        RuleAppender appender;
        if (rootContext.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            String resource = rootContext.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
            appender = new ResourceRuleAppender(assistant.getClassLoader(), resource);
        } else if (rootContext.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
            String filePath = rootContext.substring(ResourceUtils.FILE_URL_PREFIX.length());
            appender = new FileRuleAppender(filePath);
        } else {
            appender = new FileRuleAppender(assistant.getBasePath(), rootContext);
        }
        if (rootContext.toLowerCase().endsWith(".apon")) {
            appender.setAppenderFileFormatType(AppenderFileFormatType.APON);
        } else {
            appender.setAppenderFileFormatType(AppenderFileFormatType.XML);
        }
        return appender;
    }

}
