/*
 * Copyright (c) 2008-2020 The Aspectran Project
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
import com.aspectran.core.component.bean.BeanRuleRegistry;
import com.aspectran.core.component.bean.ContextualBeanRegistry;
import com.aspectran.core.component.schedule.ScheduleRuleRegistry;
import com.aspectran.core.component.template.ContextualTemplateRenderer;
import com.aspectran.core.component.template.TemplateRuleRegistry;
import com.aspectran.core.component.translet.TransletRuleRegistry;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.AspectranActivityContext;
import com.aspectran.core.context.builder.reload.ActivityContextReloader;
import com.aspectran.core.context.config.ContextAutoReloadConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.context.config.ContextProfilesConfig;
import com.aspectran.core.context.env.ContextEnvironment;
import com.aspectran.core.context.resource.AspectranClassLoader;
import com.aspectran.core.context.resource.InvalidResourceException;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.EnvironmentRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.PointcutRule;
import com.aspectran.core.context.rule.assistant.BeanReferenceException;
import com.aspectran.core.context.rule.assistant.BeanReferenceInspector;
import com.aspectran.core.context.rule.assistant.ContextRuleAssistant;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.context.rule.type.AutoReloadType;
import com.aspectran.core.context.rule.type.BeanProxifierType;
import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.service.ServiceController;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.SystemUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.util.List;

public abstract class AbstractActivityContextBuilder implements ActivityContextBuilder {

    private static final Log log = LogFactory.getLog(AbstractActivityContextBuilder.class);

    private ContextConfig contextConfig;

    private AspectranParameters aspectranParameters;

    private String basePath;

    private String rootFile;

    private String encoding;

    private String[] resourceLocations;

    private String[] basePackages;

    private String[] activeProfiles;

    private String[] defaultProfiles;

    private ItemRuleMap propertyItemRuleMap;

    private boolean hardReload;

    private boolean autoReloadStartup;

    private int scanIntervalSeconds;

    private ActivityContextReloader contextReloader;

    private ServiceController serviceController;

    private AspectranClassLoader aspectranClassLoader;

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
        this.rootFile = null;
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
    public String getRootFile() {
        return rootFile;
    }

    @Override
    public void setRootFile(String rootFile) {
        this.rootFile = rootFile;
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
    public AspectranClassLoader getAspectranClassLoader() {
        return aspectranClassLoader;
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

        this.rootFile = contextConfig.getRootFile();

        AspectranParameters aspectranParameters = contextConfig.getAspectranParameters();
        if (aspectranParameters != null) {
            this.aspectranParameters = aspectranParameters;
        }

        this.encoding = contextConfig.getEncoding();

        String[] resourceLocations = contextConfig.getResourceLocations();
        this.resourceLocations = AspectranClassLoader.checkResourceLocations(resourceLocations, getBasePath());

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
            boolean autoReloadStartup = autoReloadConfig.isStartup();
            this.hardReload = AutoReloadType.HARD.toString().equals(reloadMode);
            this.autoReloadStartup = autoReloadStartup;
            this.scanIntervalSeconds = scanIntervalSeconds;
        }
        if (this.autoReloadStartup && (this.resourceLocations == null || this.resourceLocations.length == 0)) {
            this.autoReloadStartup = false;
        }
        if (this.autoReloadStartup) {
            if (this.scanIntervalSeconds == -1) {
                this.scanIntervalSeconds = 10;
                if (log.isDebugEnabled()) {
                    log.debug("Context option 'autoReload' not specified, defaulting to 10 seconds");
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
        AspectranClassLoader acl = newAspectranClassLoader();
        return new DefaultApplicationAdapter(basePath, acl);
    }

    protected ContextEnvironment createContextEnvironment() {
        ContextEnvironment environment = new ContextEnvironment();
        if (activeProfiles != null) {
            environment.setActiveProfiles(activeProfiles);
        }
        if (defaultProfiles != null) {
            environment.setDefaultProfiles(defaultProfiles);
        }
        if (propertyItemRuleMap != null && !propertyItemRuleMap.isEmpty()) {
            environment.setPropertyItemRuleMap(propertyItemRuleMap);
        }
        return environment;
    }

    /**
     * Returns a new instance of ActivityContext.
     *
     * @param assistant the context rule assistant
     * @return the activity context
     * @throws BeanReferenceException will be thrown when cannot resolve reference to bean
     * @throws IllegalRuleException if an illegal rule is found
     */
    protected ActivityContext createActivityContext(ContextRuleAssistant assistant)
            throws BeanReferenceException, IllegalRuleException {
        initContextEnvironment(assistant);

        AspectranActivityContext activityContext = new AspectranActivityContext(
                assistant.getApplicationAdapter(), assistant.getContextEnvironment());
        activityContext.setDescriptionRule(assistant.getAssistantLocal().getDescriptionRule());

        AspectRuleRegistry aspectRuleRegistry = assistant.getAspectRuleRegistry();

        BeanRuleRegistry beanRuleRegistry = assistant.getBeanRuleRegistry();
        beanRuleRegistry.postProcess(assistant);

        BeanReferenceInspector beanReferenceInspector = assistant.getBeanReferenceInspector();
        beanReferenceInspector.inspect(beanRuleRegistry);

        initAspectRuleRegistry(assistant);

        BeanProxifierType beanProxifierType = BeanProxifierType.resolve(
                (String)assistant.getSetting(DefaultSettingType.BEAN_PROXIFIER));
        ContextualBeanRegistry contextualBeanRegistry = new ContextualBeanRegistry(
                activityContext, beanRuleRegistry, beanProxifierType);

        TemplateRuleRegistry templateRuleRegistry = assistant.getTemplateRuleRegistry();
        ContextualTemplateRenderer contextualTemplateRenderer = new ContextualTemplateRenderer(
                activityContext, templateRuleRegistry);

        ScheduleRuleRegistry scheduleRuleRegistry = assistant.getScheduleRuleRegistry();
        TransletRuleRegistry transletRuleRegistry = assistant.getTransletRuleRegistry();

        activityContext.setAspectRuleRegistry(aspectRuleRegistry);
        activityContext.setContextualBeanRegistry(contextualBeanRegistry);
        activityContext.setScheduleRuleRegistry(scheduleRuleRegistry);
        activityContext.setContextualTemplateRenderer(contextualTemplateRenderer);
        activityContext.setTransletRuleRegistry(transletRuleRegistry);
        return activityContext;
    }

    protected void startContextReloader() {
        if (autoReloadStartup && aspectranClassLoader != null) {
            contextReloader = new ActivityContextReloader(serviceController);
            contextReloader.setResources(aspectranClassLoader.getAllResources());
            contextReloader.start(scanIntervalSeconds);
        }
    }

    protected void stopContextReloader() {
        if (contextReloader != null) {
            contextReloader.stop();
            contextReloader = null;
        }
    }

    private AspectranClassLoader newAspectranClassLoader() throws InvalidResourceException {
        if (aspectranClassLoader == null || hardReload) {
            AspectranClassLoader acl = new AspectranClassLoader();
            if (resourceLocations != null && resourceLocations.length > 0) {
                acl.setResourceLocations(resourceLocations);
            }
            aspectranClassLoader = acl;
        }
        return aspectranClassLoader;
    }

    private void initContextEnvironment(ContextRuleAssistant assistant) {
        ContextEnvironment environment = assistant.getContextEnvironment();
        for (EnvironmentRule environmentRule : assistant.getEnvironmentRules()) {
            String[] profiles = StringUtils.splitCommaDelimitedString(environmentRule.getProfile());
            if (environment.acceptsProfiles(profiles)) {
                if (environmentRule.getPropertyItemRuleMapList() != null) {
                    for (ItemRuleMap propertyItemRuleMap : environmentRule.getPropertyItemRuleMapList()) {
                        String[] profiles2 = StringUtils.splitCommaDelimitedString(propertyItemRuleMap.getProfile());
                        if (environment.acceptsProfiles(profiles2)) {
                            environment.addPropertyItemRule(propertyItemRuleMap);
                        }
                    }
                }
            }
        }
    }

    /**
     * Initialize the aspect rule registry.
     *
     * @param assistant the context rule assistant
     */
    private void initAspectRuleRegistry(ContextRuleAssistant assistant) {
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

        AspectAdviceRulePreRegister preRegister = new AspectAdviceRulePreRegister(aspectRuleRegistry);
        preRegister.register(beanRuleRegistry);
        preRegister.register(transletRuleRegistry);

        // check invalid pointcut pattern
        boolean pointcutPatternVerifiable = assistant.isPointcutPatternVerifiable();
        if (pointcutPatternVerifiable || log.isDebugEnabled()) {
            int invalidPointcutPatterns = 0;
            for (AspectRule aspectRule : aspectRuleRegistry.getAspectRules()) {
                Pointcut pointcut = aspectRule.getPointcut();
                if (pointcut != null) {
                    List<PointcutPatternRule> pointcutPatternRuleList = pointcut.getPointcutPatternRuleList();
                    if (pointcutPatternRuleList != null) {
                        for (PointcutPatternRule ppr : pointcutPatternRuleList) {
                            if (ppr.getBeanIdPattern() != null && ppr.getMatchedBeanCount() == 0) {
                                invalidPointcutPatterns++;
                                String msg = "No beans matching to '" + ppr.getBeanIdPattern() +
                                        "'; aspectRule " + aspectRule;
                                if (pointcutPatternVerifiable) {
                                    log.error(msg);
                                } else {
                                    log.debug(msg);
                                }
                            }
                            if (ppr.getClassNamePattern() != null && ppr.getMatchedClassCount() == 0) {
                                invalidPointcutPatterns++;
                                String msg = "No beans matching to '@class:" + ppr.getClassNamePattern() +
                                        "'; aspectRule " + aspectRule;
                                if (pointcutPatternVerifiable) {
                                    log.error(msg);
                                } else {
                                    log.debug(msg);
                                }
                            }
                            if (ppr.getMethodNamePattern() != null && ppr.getMatchedMethodCount() == 0) {
                                invalidPointcutPatterns++;
                                String msg = "No beans have methods matching to '^" + ppr.getMethodNamePattern() +
                                        "'; aspectRule " + aspectRule;
                                if (pointcutPatternVerifiable) {
                                    log.error(msg);
                                } else {
                                    log.debug(msg);
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
                    log.error(msg);
                    throw new InvalidPointcutPatternException(msg);
                } else {
                    log.debug(msg);
                }
            }
        }
    }

}
