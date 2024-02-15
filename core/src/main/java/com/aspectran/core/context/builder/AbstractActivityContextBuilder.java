/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.core.context.builder;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.DefaultApplicationAdapter;
import com.aspectran.core.component.aspect.AspectAdviceRulePreRegister;
import com.aspectran.core.component.aspect.AspectRuleRegistry;
import com.aspectran.core.component.aspect.InvalidPointcutPatternException;
import com.aspectran.core.component.aspect.pointcut.Pointcut;
import com.aspectran.core.component.aspect.pointcut.PointcutFactory;
import com.aspectran.core.component.aspect.pointcut.PointcutPattern;
import com.aspectran.core.component.bean.BeanRuleRegistry;
import com.aspectran.core.component.bean.DefaultBeanRegistry;
import com.aspectran.core.component.schedule.ScheduleRuleRegistry;
import com.aspectran.core.component.template.DefaultTemplateRenderer;
import com.aspectran.core.component.template.TemplateRuleRegistry;
import com.aspectran.core.component.translet.TransletRuleRegistry;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.DefaultActivityContext;
import com.aspectran.core.context.builder.reload.ContextReloadingTimer;
import com.aspectran.core.context.config.ContextAutoReloadConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.context.config.ContextProfilesConfig;
import com.aspectran.core.context.env.ActivityEnvironment;
import com.aspectran.core.context.env.EnvironmentProfiles;
import com.aspectran.core.context.resource.InvalidResourceException;
import com.aspectran.core.context.resource.ResourceManager;
import com.aspectran.core.context.resource.SiblingsClassLoader;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.EnvironmentRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.PointcutRule;
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.core.context.rule.assistant.BeanReferenceException;
import com.aspectran.core.context.rule.assistant.BeanReferenceInspector;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.context.rule.type.AutoReloadType;
import com.aspectran.core.service.ServiceController;
import com.aspectran.utils.SystemUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.util.List;

public abstract class AbstractActivityContextBuilder implements ActivityContextBuilder {

    private static final Logger logger = LoggerFactory.getLogger(AbstractActivityContextBuilder.class);

    private ContextConfig contextConfig;

    private AspectranParameters aspectranParameters;

    private String basePath;

    private String[] contextRules;

    private String encoding;

    private String[] resourceLocations;

    private String[] basePackages;

    private String[] activeProfiles;

    private String[] defaultProfiles;

    private ItemRuleMap propertyItemRuleMap;

    private boolean hardReload;

    private boolean autoReloadEnabled;

    private int scanIntervalSeconds;

    private ContextReloadingTimer contextReloadingTimer;

    private ServiceController serviceController;

    private SiblingsClassLoader siblingsClassLoader;

    private boolean useAponToLoadXml;

    private boolean debugMode;

    public AbstractActivityContextBuilder() {
        this.useAponToLoadXml = Boolean.parseBoolean(SystemUtils.getProperty(USE_APON_TO_LOAD_XML_PROPERTY_NAME));
        this.debugMode = Boolean.parseBoolean(SystemUtils.getProperty(DEBUG_MODE_PROPERTY_NAME));
    }

    @Override
    public ContextConfig getContextConfig() {
        return contextConfig;
    }

    @Override
    public AspectranParameters getAspectranParameters() {
        return aspectranParameters;
    }

    @Override
    public void setAspectranParameters(AspectranParameters aspectranParameters) {
        this.aspectranParameters = aspectranParameters;
        this.contextRules = null;
    }

    @Override
    public String getBasePath() {
        return basePath;
    }

    @Override
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public String[] getContextRules() {
        return contextRules;
    }

    @Override
    public void setContextRules(String[] contextRules) {
        this.contextRules = contextRules;
        this.aspectranParameters = null;
    }

    @Override
    public String getEncoding() {
        return (encoding == null ? ActivityContext.DEFAULT_ENCODING : encoding);
    }

    @Override
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Override
    public String[] getResourceLocations() {
        return resourceLocations;
    }

    @Override
    public void setResourceLocations(String... resourceLocations) {
        this.resourceLocations = resourceLocations;
    }

    @Override
    public String[] getBasePackages() {
        return basePackages;
    }

    @Override
    public void setBasePackages(String... basePackages) {
        if (basePackages != null && basePackages.length > 0) {
            this.basePackages = basePackages;
        }
    }

    @Override
    public String[] getActiveProfiles() {
        return activeProfiles;
    }

    @Override
    public void setActiveProfiles(String... activeProfiles) {
        this.activeProfiles = activeProfiles;
    }

    @Override
    public String[] getDefaultProfiles() {
        return defaultProfiles;
    }

    @Override
    public void setDefaultProfiles(String... defaultProfiles) {
        this.defaultProfiles = defaultProfiles;
    }

    @Override
    public ItemRuleMap getPropertyItemRuleMap() {
        return propertyItemRuleMap;
    }

    @Override
    public void setPropertyItemRuleMap(ItemRuleMap propertyItemRuleMap) {
        this.propertyItemRuleMap = propertyItemRuleMap;
    }

    @Override
    public void addPropertyItemRule(ItemRuleMap propertyItemRuleMap) {
        if (this.propertyItemRuleMap == null) {
            this.propertyItemRuleMap = new ItemRuleMap(propertyItemRuleMap);
        } else {
            this.propertyItemRuleMap.putAll(propertyItemRuleMap);
        }
    }

    @Override
    public boolean isHardReload() {
        return hardReload;
    }

    @Override
    public void setHardReload(boolean hardReload) {
        this.hardReload = hardReload;
    }

    @Override
    public ServiceController getServiceController() {
        return serviceController;
    }

    @Override
    public void setServiceController(ServiceController serviceController) {
        this.serviceController = serviceController;
    }

    @Override
    public ClassLoader getClassLoader() {
        return siblingsClassLoader;
    }

    @Override
    public void setContextConfig(ContextConfig contextConfig) throws InvalidResourceException {
        if (contextConfig == null) {
            throw new IllegalArgumentException("contextConfig must not be null");
        }

        this.contextConfig = contextConfig;

        if (getBasePath() == null) {
            setBasePath(contextConfig.getBasePath());
        }

        this.contextRules = contextConfig.getContextRules();

        AspectranParameters aspectranParameters = contextConfig.getAspectranParameters();
        if (aspectranParameters != null) {
            this.aspectranParameters = aspectranParameters;
        }

        this.encoding = contextConfig.getEncoding();

        String[] resourceLocations = contextConfig.getResourceLocations();
        this.resourceLocations = ResourceManager.checkResourceLocations(resourceLocations, getBasePath());

        this.basePackages = contextConfig.getBasePackages();

        ContextProfilesConfig profilesConfig = contextConfig.getProfilesConfig();
        if (profilesConfig != null) {
            setActiveProfiles(profilesConfig.getActiveProfiles());
            setDefaultProfiles(profilesConfig.getDefaultProfiles());
        }

        ContextAutoReloadConfig autoReloadConfig = contextConfig.getAutoReloadConfig();
        if (autoReloadConfig != null) {
            String reloadMode = autoReloadConfig.getReloadMode();
            int scanIntervalSeconds = autoReloadConfig.getScanIntervalSeconds();
            boolean autoReloadEnabled = autoReloadConfig.isEnabled();
            this.hardReload = AutoReloadType.HARD.toString().equals(reloadMode);
            this.autoReloadEnabled = autoReloadEnabled;
            this.scanIntervalSeconds = scanIntervalSeconds;
        }
        if (this.autoReloadEnabled && (this.resourceLocations == null || this.resourceLocations.length == 0)) {
            this.autoReloadEnabled = false;
        }
        if (this.autoReloadEnabled) {
            if (this.scanIntervalSeconds == -1) {
                this.scanIntervalSeconds = 10;
                if (logger.isDebugEnabled()) {
                    logger.debug("Context option 'autoReload' not specified, defaulting to 10 seconds");
                }
            }
        }
    }

    protected boolean isUseAponToLoadXml() {
        return useAponToLoadXml;
    }

    @Override
    public void setUseAponToLoadXml(boolean useAponToLoadXml) {
        this.useAponToLoadXml = useAponToLoadXml;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    @Override
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    protected ApplicationAdapter createApplicationAdapter() throws InvalidResourceException {
        SiblingsClassLoader classLoader = newSiblingsClassLoader();
        return new DefaultApplicationAdapter(basePath, classLoader);
    }

    protected EnvironmentProfiles createEnvironmentProfiles() {
        EnvironmentProfiles environmentProfiles = new EnvironmentProfiles();
        if (getDefaultProfiles() != null) {
            environmentProfiles.setDefaultProfiles(getDefaultProfiles());
        }
        if (getActiveProfiles() != null) {
            environmentProfiles.setActiveProfiles(getActiveProfiles());
        } else {
            environmentProfiles.getActiveProfiles(); // just for logging
        }
        return environmentProfiles;
    }

    /**
     * Returns a new instance of ActivityContext.
     * @param assistant the activity rule assistant
     * @return the activity context
     * @throws BeanReferenceException will be thrown when cannot resolve reference to bean
     * @throws IllegalRuleException if an illegal rule is found
     */
    protected ActivityContext createActivityContext(@NonNull ActivityRuleAssistant assistant)
            throws BeanReferenceException, IllegalRuleException {
        DefaultActivityContext activityContext = new DefaultActivityContext(assistant.getApplicationAdapter());
        activityContext.setDescriptionRule(assistant.getAssistantLocal().getDescriptionRule());

        ActivityEnvironment activityEnvironment = createActivityEnvironment(assistant, activityContext);
        activityContext.setActivityEnvironment(activityEnvironment);

        AspectRuleRegistry aspectRuleRegistry = assistant.getAspectRuleRegistry();

        BeanRuleRegistry beanRuleRegistry = assistant.getBeanRuleRegistry();
        beanRuleRegistry.postProcess(assistant);

        BeanReferenceInspector beanReferenceInspector = assistant.getBeanReferenceInspector();
        beanReferenceInspector.inspect(beanRuleRegistry);

        initAspectRuleRegistry(assistant);

        DefaultBeanRegistry defaultBeanRegistry = new DefaultBeanRegistry(activityContext, beanRuleRegistry);

        TemplateRuleRegistry templateRuleRegistry = assistant.getTemplateRuleRegistry();
        DefaultTemplateRenderer defaultTemplateRenderer = new DefaultTemplateRenderer(
                activityContext, templateRuleRegistry);

        ScheduleRuleRegistry scheduleRuleRegistry = assistant.getScheduleRuleRegistry();
        TransletRuleRegistry transletRuleRegistry = assistant.getTransletRuleRegistry();

        activityContext.setAspectRuleRegistry(aspectRuleRegistry);
        activityContext.setDefaultBeanRegistry(defaultBeanRegistry);
        activityContext.setScheduleRuleRegistry(scheduleRuleRegistry);
        activityContext.setDefaultTemplateRenderer(defaultTemplateRenderer);
        activityContext.setTransletRuleRegistry(transletRuleRegistry);
        return activityContext;
    }

    protected void startContextReloadingTimer() {
        if (autoReloadEnabled && siblingsClassLoader != null) {
            contextReloadingTimer = new ContextReloadingTimer(serviceController);
            contextReloadingTimer.setResources(siblingsClassLoader.getAllResources());
            contextReloadingTimer.start(scanIntervalSeconds);
        }
    }

    protected void stopContextReloadingTimer() {
        if (contextReloadingTimer != null) {
            contextReloadingTimer.stop();
            contextReloadingTimer = null;
        }
    }

    private SiblingsClassLoader newSiblingsClassLoader() throws InvalidResourceException {
        if (siblingsClassLoader == null || hardReload) {
            siblingsClassLoader = new SiblingsClassLoader(resourceLocations);
        } else {
            siblingsClassLoader.reload();
        }
        return siblingsClassLoader;
    }

    @NonNull
    private ActivityEnvironment createActivityEnvironment(@NonNull ActivityRuleAssistant assistant,
                                                          ActivityContext activityContext) {
        EnvironmentProfiles environmentProfiles = assistant.getEnvironmentProfiles();
        ActivityEnvironment environment = new ActivityEnvironment(environmentProfiles, activityContext);
        if (propertyItemRuleMap != null && !propertyItemRuleMap.isEmpty()) {
            environment.setPropertyItemRuleMap(propertyItemRuleMap);
        }
        for (EnvironmentRule environmentRule : assistant.getEnvironmentRules()) {
            if (environmentProfiles.acceptsProfiles(environmentRule.getProfiles())) {
                if (environmentRule.getPropertyItemRuleMapList() != null) {
                    for (ItemRuleMap propertyIrm : environmentRule.getPropertyItemRuleMapList()) {
                        if (environmentProfiles.acceptsProfiles(propertyIrm.getProfiles())) {
                            environment.addPropertyItemRule(propertyIrm);
                        }
                    }
                }
            }
        }
        return environment;
    }

    /**
     * Initialize the aspect rule registry.
     * @param assistant the activity rule assistant
     */
    private void initAspectRuleRegistry(@NonNull ActivityRuleAssistant assistant) {
        AspectRuleRegistry aspectRuleRegistry = assistant.getAspectRuleRegistry();
        BeanRuleRegistry beanRuleRegistry = assistant.getBeanRuleRegistry();
        TransletRuleRegistry transletRuleRegistry = assistant.getTransletRuleRegistry();

        for (AspectRule aspectRule : aspectRuleRegistry.getAspectRules()) {
            PointcutRule pointcutRule = aspectRule.getPointcutRule();
            if (pointcutRule != null) {
                Pointcut pointcut = PointcutFactory.createPointcut(pointcutRule);
                aspectRule.setPointcut(pointcut);
            }
        }

        boolean pointcutPatternVerifiable = assistant.isPointcutPatternVerifiable();

        AspectAdviceRulePreRegister preRegister = new AspectAdviceRulePreRegister(aspectRuleRegistry);
        preRegister.setPointcutPatternVerifiable(pointcutPatternVerifiable || logger.isDebugEnabled());
        preRegister.register(beanRuleRegistry);
        preRegister.register(transletRuleRegistry);

        // check invalid pointcut pattern
        if (pointcutPatternVerifiable || logger.isDebugEnabled()) {
            int invalidPointcutPatterns = 0;
            for (AspectRule aspectRule : aspectRuleRegistry.getAspectRules()) {
                Pointcut pointcut = aspectRule.getPointcut();
                if (pointcut != null) {
                    List<PointcutPatternRule> pointcutPatternRuleList = pointcut.getPointcutPatternRuleList();
                    if (pointcutPatternRuleList != null) {
                        for (PointcutPatternRule ppr : pointcutPatternRuleList) {
                            PointcutPattern pp = ppr.getPointcutPattern();
                            if (pp != null) {
                                if (pp.getBeanIdPattern() != null && ppr.getMatchedBeanIdCount() == 0) {
                                    invalidPointcutPatterns++;
                                    String msg = "No beans matching to '" + pp.getBeanIdPattern() +
                                            "'; aspectRule " + aspectRule;
                                    if (pointcutPatternVerifiable) {
                                        logger.error(msg);
                                    } else if (logger.isDebugEnabled()) {
                                        logger.debug(msg);
                                    }
                                }
                                if (pp.getClassNamePattern() != null && ppr.getMatchedClassNameCount() == 0) {
                                    invalidPointcutPatterns++;
                                    String msg = "No beans matching to '@class:" + pp.getClassNamePattern() +
                                            "'; aspectRule " + aspectRule;
                                    if (pointcutPatternVerifiable) {
                                        logger.error(msg);
                                    } else if (logger.isDebugEnabled()) {
                                        logger.debug(msg);
                                    }
                                }
                                if (pp.getMethodNamePattern() != null && ppr.getMatchedMethodNameCount() == 0) {
                                    invalidPointcutPatterns++;
                                    String msg = "No beans have methods matching to '^" + pp.getMethodNamePattern() +
                                            "'; aspectRule " + aspectRule;
                                    if (pointcutPatternVerifiable) {
                                        logger.error(msg);
                                    } else if (logger.isDebugEnabled()) {
                                        logger.debug(msg);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (invalidPointcutPatterns > 0) {
                String msg = "Invalid pointcut detected: " + invalidPointcutPatterns +
                        "; Please check the logs for more information";
                if (pointcutPatternVerifiable) {
                    logger.error(msg);
                    throw new InvalidPointcutPatternException(msg);
                } else {
                    logger.debug(msg);
                }
            }
        }
    }

}
