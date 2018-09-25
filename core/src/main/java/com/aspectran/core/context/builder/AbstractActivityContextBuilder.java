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
package com.aspectran.core.context.builder;

import com.aspectran.core.adapter.ApplicationAdapter;
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
import com.aspectran.core.context.builder.reload.ActivityContextReloadingTimer;
import com.aspectran.core.context.config.AspectranConfig;
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
import com.aspectran.core.context.rule.type.BeanProxifierType;
import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.context.rule.type.JoinpointTargetType;
import com.aspectran.core.service.ServiceController;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.util.List;

public abstract class AbstractActivityContextBuilder implements ActivityContextBuilder {

    protected final Log log = LogFactory.getLog(getClass());

    private final ApplicationAdapter applicationAdapter;

    private ContextConfig contextConfig;

    private AspectranParameters aspectranParameters;

    private String basePath;

    private String rootConfigLocation;

    private String encoding;

    private String[] resourceLocations;

    private String[] activeProfiles;

    private String[] defaultProfiles;

    private ItemRuleMap propertyItemRuleMap;

    private boolean hybridLoad;

    private boolean hardReload;

    private boolean autoReloadStartup;

    private int scanIntervalSeconds;

    private ActivityContextReloadingTimer reloadingTimer;

    private ServiceController serviceController;

    private AspectranClassLoader aspectranClassLoader;

    public AbstractActivityContextBuilder(ApplicationAdapter applicationAdapter) {
        if (applicationAdapter == null) {
            throw new IllegalArgumentException("Argument 'applicationAdapter' must not be null");
        }
        this.applicationAdapter = applicationAdapter;
    }

    @Override
    public ApplicationAdapter getApplicationAdapter() {
        return applicationAdapter;
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
        this.rootConfigLocation = null;
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
    public String getRootConfigLocation() {
        return rootConfigLocation;
    }

    @Override
    public void setRootConfigLocation(String rootConfigLocation) {
        this.rootConfigLocation = rootConfigLocation;
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
    public void addPropertyItemRuleMap(ItemRuleMap propertyItemRuleMap) {
        if (this.propertyItemRuleMap == null) {
            this.propertyItemRuleMap = new ItemRuleMap(propertyItemRuleMap);
        } else {
            this.propertyItemRuleMap.putAll(propertyItemRuleMap);
        }
    }

    @Override
    public boolean isHybridLoad() {
        return hybridLoad;
    }

    @Override
    public void setHybridLoad(boolean hybridLoad) {
        this.hybridLoad = hybridLoad;
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
            throw new IllegalArgumentException("Argument 'contextConfig' must not be null");
        }

        this.contextConfig = contextConfig;

        String basePath = contextConfig.getString(ContextConfig.base);
        if (basePath != null) {
            setBasePath(basePath);
        }

        this.rootConfigLocation = contextConfig.getString(ContextConfig.root);

        AspectranParameters aspectranParameters = contextConfig.getParameters(ContextConfig.parameters);
        if (aspectranParameters != null) {
            this.aspectranParameters = aspectranParameters;
        }

        this.encoding = contextConfig.getString(ContextConfig.encoding);

        String[] resourceLocations = contextConfig.getStringArray(ContextConfig.resources);
        this.resourceLocations = AspectranClassLoader.checkResourceLocations(resourceLocations, getBasePath());

        ContextProfilesConfig contextProfilesConfig = contextConfig.getParameters(ContextConfig.profiles);
        if (contextProfilesConfig != null) {
            setActiveProfiles(contextProfilesConfig.getStringArray(ContextProfilesConfig.activeProfiles));
            setDefaultProfiles(contextProfilesConfig.getStringArray(ContextProfilesConfig.defaultProfiles));
        }

        this.hybridLoad = contextConfig.getBoolean(ContextConfig.hybridLoad, false);

        ContextAutoReloadConfig contextAutoReloadConfig = contextConfig.getParameters(ContextConfig.autoReload);
        if (contextAutoReloadConfig != null) {
            String reloadMode = contextAutoReloadConfig.getString(ContextAutoReloadConfig.reloadMode);
            int scanIntervalSeconds = contextAutoReloadConfig.getInt(ContextAutoReloadConfig.scanIntervalSeconds, -1);
            boolean autoReloadStartup = contextAutoReloadConfig.getBoolean(ContextAutoReloadConfig.startup, false);
            this.hardReload = "hard".equals(reloadMode);
            this.autoReloadStartup = autoReloadStartup;
            this.scanIntervalSeconds = scanIntervalSeconds;
        }
        if (this.autoReloadStartup && (this.resourceLocations == null || this.resourceLocations.length == 0)) {
            this.autoReloadStartup = false;
        }
        if (this.autoReloadStartup) {
            if (this.scanIntervalSeconds == -1) {
                this.scanIntervalSeconds = 10;
                String contextAutoReloadingParamName = AspectranConfig.context.getName() + "." + ContextConfig.autoReload.getName();
                log.info("'" + contextAutoReloadingParamName + "' is not specified, defaulting to 10 seconds");
            }
        }
    }

    protected ContextEnvironment createContextEnvironment() throws InvalidResourceException {
        AspectranClassLoader acl = newAspectranClassLoader();
        ContextEnvironment environment = new ContextEnvironment(getApplicationAdapter());
        environment.setClassLoader(acl);
        if (basePath != null) {
            environment.setBasePath(basePath);
        }
        if (activeProfiles != null) {
            environment.setActiveProfiles(activeProfiles);
        }
        if (defaultProfiles != null) {
            environment.setDefaultProfiles(defaultProfiles);
        }
        if (propertyItemRuleMap != null) {
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

        AspectranActivityContext activityContext = new AspectranActivityContext(assistant.getContextEnvironment());

        AspectRuleRegistry aspectRuleRegistry = assistant.getAspectRuleRegistry();

        BeanRuleRegistry beanRuleRegistry = assistant.getBeanRuleRegistry();
        beanRuleRegistry.postProcess(assistant);

        BeanReferenceInspector beanReferenceInspector = assistant.getBeanReferenceInspector();
        beanReferenceInspector.inspect(beanRuleRegistry);

        initAspectRuleRegistry(assistant);

        BeanProxifierType beanProxifierType = BeanProxifierType.resolve((String)assistant.getSetting(DefaultSettingType.BEAN_PROXIFIER));
        ContextBeanRegistry contextBeanRegistry = new ContextBeanRegistry(activityContext, beanRuleRegistry, beanProxifierType);

        TemplateRuleRegistry templateRuleRegistry = assistant.getTemplateRuleRegistry();
        ContextTemplateProcessor contextTemplateProcessor = new ContextTemplateProcessor(activityContext, templateRuleRegistry);

        ScheduleRuleRegistry scheduleRuleRegistry = assistant.getScheduleRuleRegistry();
        TransletRuleRegistry transletRuleRegistry = assistant.getTransletRuleRegistry();

        activityContext.setAspectRuleRegistry(aspectRuleRegistry);
        activityContext.setContextBeanRegistry(contextBeanRegistry);
        activityContext.setScheduleRuleRegistry(scheduleRuleRegistry);
        activityContext.setContextTemplateProcessor(contextTemplateProcessor);
        activityContext.setTransletRuleRegistry(transletRuleRegistry);
        activityContext.setDescription(assistant.getAssistantLocal().getDescription());

        return activityContext;
    }

    protected void startReloadingTimer() {
        if (autoReloadStartup && aspectranClassLoader != null) {
            reloadingTimer = new ActivityContextReloadingTimer(serviceController);
            reloadingTimer.setResources(aspectranClassLoader.extractResources());
            reloadingTimer.start(scanIntervalSeconds);
        }
    }

    protected void stopReloadingTimer() {
        if (reloadingTimer != null) {
            reloadingTimer.cancel();
            reloadingTimer = null;
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
     * @param assistant the context rule assistant
     */
    private void initAspectRuleRegistry(ContextRuleAssistant assistant) {
        AspectRuleRegistry aspectRuleRegistry = assistant.getAspectRuleRegistry();
        BeanRuleRegistry beanRuleRegistry = assistant.getBeanRuleRegistry();
        TransletRuleRegistry transletRuleRegistry = assistant.getTransletRuleRegistry();

        AspectAdviceRulePostRegister sessionScopeAspectAdviceRulePostRegister = new AspectAdviceRulePostRegister();
        for (AspectRule aspectRule : aspectRuleRegistry.getAspectRules()) {
            PointcutRule pointcutRule = aspectRule.getPointcutRule();
            if (pointcutRule != null) {
                Pointcut pointcut = PointcutFactory.createPointcut(pointcutRule);
                aspectRule.setPointcut(pointcut);
            }
            if (aspectRule.getJoinpointTargetType() == JoinpointTargetType.SESSION) {
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
                        if (ppr.getBeanIdPattern() != null && ppr.getMatchedBeanCount() == 0) {
                            offendingPointcutPatterns++;
                            String msg = "Incorrect pointcut pattern for bean id '" + ppr.getBeanIdPattern() +
                                    "' : aspectRule " + aspectRule;
                            if (pointcutPatternVerifiable) {
                                log.error(msg);
                            } else {
                                log.warn(msg);
                            }
                        }
                        if (ppr.getClassNamePattern() != null && ppr.getMatchedClassCount() == 0) {
                            offendingPointcutPatterns++;
                            String msg = "Incorrect pointcut pattern for class name '" + ppr.getClassNamePattern() +
                                    "' : aspectRule " + aspectRule;
                            if (pointcutPatternVerifiable) {
                                log.error(msg);
                            } else {
                                log.warn(msg);
                            }
                        }
                        if (ppr.getMethodNamePattern() != null && ppr.getMatchedMethodCount() == 0) {
                            offendingPointcutPatterns++;
                            String msg = "Incorrect pointcut pattern for bean's method name '" + ppr.getMethodNamePattern() +
                                    "' : aspectRule " + aspectRule;
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

        AspectAdviceRuleRegistry sessionScopeAarr = sessionScopeAspectAdviceRulePostRegister.getAspectAdviceRuleRegistry();
        if (sessionScopeAarr != null) {
            aspectRuleRegistry.setSessionAspectAdviceRuleRegistry(sessionScopeAarr);
        }
    }

}
