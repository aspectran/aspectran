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
package com.aspectran.core.context.builder;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.DefaultApplicationAdapter;
import com.aspectran.core.component.aspect.AspectAdviceRulePreRegister;
import com.aspectran.core.component.aspect.AspectRuleRegistry;
import com.aspectran.core.component.aspect.InvalidPointcutPatternException;
import com.aspectran.core.component.aspect.pointcut.Pointcut;
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
import com.aspectran.core.context.env.ActivityEnvironmentBuilder;
import com.aspectran.core.context.env.EnvironmentProfiles;
import com.aspectran.core.context.resource.InvalidResourceException;
import com.aspectran.core.context.resource.ResourceManager;
import com.aspectran.core.context.resource.SiblingClassLoader;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.EnvironmentRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.core.context.rule.assistant.BeanReferenceException;
import com.aspectran.core.context.rule.assistant.BeanReferenceInspector;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.context.rule.type.AutoReloadType;
import com.aspectran.core.service.CoreService;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.SystemUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.aspectran.core.context.config.AspectranConfig.BASE_PATH_PROPERTY_NAME;
import static com.aspectran.core.context.config.AspectranConfig.TEMP_PATH_PROPERTY_NAME;
import static com.aspectran.core.context.config.AspectranConfig.WORK_PATH_PROPERTY_NAME;

public abstract class AbstractActivityContextBuilder implements ActivityContextBuilder {

    private static final Logger logger = LoggerFactory.getLogger(AbstractActivityContextBuilder.class);

    private static final String TMP_BASE_DIRNAME_PREFIX = "com.aspectran-";

    private String basePath;

    private boolean ownBasePath;

    private final CoreService masterService;

    private ContextConfig contextConfig;

    private AspectranParameters aspectranParameters;

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

    private SiblingClassLoader siblingClassLoader;

    private boolean useAponToLoadXml;

    private boolean debugMode;

    public AbstractActivityContextBuilder(CoreService masterService) {
        this.masterService = masterService;
        this.useAponToLoadXml = Boolean.parseBoolean(SystemUtils.getProperty(USE_APON_TO_LOAD_XML_PROPERTY_NAME));
        this.debugMode = Boolean.parseBoolean(SystemUtils.getProperty(DEBUG_MODE_PROPERTY_NAME));
    }

    @Override
    public String getBasePath() {
        return basePath;
    }

    @Override
    public void setBasePath(String basePath) {
        if (StringUtils.hasText(basePath)) {
            this.basePath = basePath;
        } else {
            this.basePath = null;
        }
    }

    @Override
    public boolean hasOwnBasePath() {
        return ownBasePath;
    }

    protected void setOwnBasePath(boolean ownBasePath) {
        this.ownBasePath = ownBasePath;
    }

    @Override
    public CoreService getMasterService() {
        return masterService;
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
    public void putPropertyItemRule(ItemRule propertyItemRule) {
        if (this.propertyItemRuleMap == null) {
            this.propertyItemRuleMap = new ItemRuleMap();
        }
        this.propertyItemRuleMap.putItemRule(propertyItemRule);
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
    public ClassLoader getClassLoader() {
        return siblingClassLoader;
    }

    @Override
    public void configure(ContextConfig contextConfig) throws IOException, InvalidResourceException {
        if (this.basePath == null) {
            if (getMasterService() != null) {
                this.basePath = getMasterService().getBasePath();
            } else {
                this.basePath = contextConfig.getBasePath();
            }
        }

        if (getMasterService() == null || getMasterService().isRootService()) {
            checkDirectoryStructure();
        }

        if (contextConfig != null) {
            this.contextConfig = contextConfig;
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
            if (this.autoReloadEnabled && this.resourceLocations == null) {
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

    protected SiblingClassLoader createSiblingClassLoader(String contextName, ClassLoader parentClassLoader)
        throws InvalidResourceException {
        if (siblingClassLoader == null || hardReload) {
            siblingClassLoader = new SiblingClassLoader(contextName, parentClassLoader, resourceLocations);
        } else {
            siblingClassLoader.reload();
        }
        return siblingClassLoader;
    }

    protected ApplicationAdapter createApplicationAdapter() {
        return new DefaultApplicationAdapter(basePath);
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
        DefaultActivityContext context = new DefaultActivityContext(
                assistant.getClassLoader(), assistant.getApplicationAdapter());
        if (getContextConfig() != null) {
            context.setName(getContextConfig().getName());
        }
        context.setDescriptionRule(assistant.getAssistantLocal().getDescriptionRule());

        ActivityEnvironment activityEnvironment = createActivityEnvironment(context, assistant);
        context.setActivityEnvironment(activityEnvironment);

        AspectRuleRegistry aspectRuleRegistry = assistant.getAspectRuleRegistry();

        BeanRuleRegistry beanRuleRegistry = assistant.getBeanRuleRegistry();
        beanRuleRegistry.postProcess(assistant);

        BeanReferenceInspector beanReferenceInspector = assistant.getBeanReferenceInspector();
        beanReferenceInspector.inspect(beanRuleRegistry);

        initAspectRuleRegistry(assistant);

        DefaultBeanRegistry defaultBeanRegistry = new DefaultBeanRegistry(context, beanRuleRegistry);

        TemplateRuleRegistry templateRuleRegistry = assistant.getTemplateRuleRegistry();
        DefaultTemplateRenderer defaultTemplateRenderer = new DefaultTemplateRenderer(
                context, templateRuleRegistry);

        ScheduleRuleRegistry scheduleRuleRegistry = assistant.getScheduleRuleRegistry();
        TransletRuleRegistry transletRuleRegistry = assistant.getTransletRuleRegistry();

        context.setAspectRuleRegistry(aspectRuleRegistry);
        context.setDefaultBeanRegistry(defaultBeanRegistry);
        context.setScheduleRuleRegistry(scheduleRuleRegistry);
        context.setDefaultTemplateRenderer(defaultTemplateRenderer);
        context.setTransletRuleRegistry(transletRuleRegistry);
        return context;
    }

    protected void startContextReloadingTimer() {
        if (autoReloadEnabled && masterService != null && siblingClassLoader != null) {
            contextReloadingTimer = new ContextReloadingTimer(masterService.getServiceLifeCycle());
            contextReloadingTimer.setResources(siblingClassLoader.getAllResources());
            contextReloadingTimer.start(scanIntervalSeconds);
        }
    }

    protected void stopContextReloadingTimer() {
        if (contextReloadingTimer != null) {
            contextReloadingTimer.stop();
            contextReloadingTimer = null;
        }
    }

    @NonNull
    private ActivityEnvironment createActivityEnvironment(
            ActivityContext context, @NonNull ActivityRuleAssistant assistant) {
        EnvironmentProfiles environmentProfiles = assistant.getEnvironmentProfiles();
        ActivityEnvironmentBuilder builder = new ActivityEnvironmentBuilder(context, environmentProfiles)
            .putPropertyItemRules(propertyItemRuleMap);
        for (EnvironmentRule environmentRule : assistant.getEnvironmentRules()) {
            if (environmentProfiles.acceptsProfiles(environmentRule.getProfiles())) {
                if (environmentRule.getPropertyItemRuleMapList() != null) {
                    for (ItemRuleMap propertyIrm : environmentRule.getPropertyItemRuleMapList()) {
                        if (environmentProfiles.acceptsProfiles(propertyIrm.getProfiles())) {
                            builder.putPropertyItemRules(propertyIrm);
                        }
                    }
                }
            }
        }
        return builder.build();
    }

    /**
     * Initialize the aspect rule registry.
     * @param assistant the activity rule assistant
     */
    private void initAspectRuleRegistry(@NonNull ActivityRuleAssistant assistant) {
        AspectRuleRegistry aspectRuleRegistry = assistant.getAspectRuleRegistry();
        BeanRuleRegistry beanRuleRegistry = assistant.getBeanRuleRegistry();
        TransletRuleRegistry transletRuleRegistry = assistant.getTransletRuleRegistry();

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

    public void clear() {
        SystemUtils.clearProperty(BASE_PATH_PROPERTY_NAME);
        SystemUtils.clearProperty(WORK_PATH_PROPERTY_NAME);
        SystemUtils.clearProperty(TEMP_PATH_PROPERTY_NAME);
    }

    private void checkDirectoryStructure() throws IOException {
        setOwnBasePath(true);

        // Determines the path of the base directory
        if (getBasePath() == null) {
            String basePath = SystemUtils.getProperty(BASE_PATH_PROPERTY_NAME);
            File baseDir;
            if (StringUtils.hasText(basePath)) {
                baseDir = new File(basePath);
                if (!baseDir.isDirectory()) {
                    throw new IOException("Make sure it is a valid base directory; " +
                        BASE_PATH_PROPERTY_NAME + "=" + basePath);
                }
            } else {
                setOwnBasePath(false);
                try {
                    String tmpDir = SystemUtils.getJavaIoTmpDir();
                    baseDir = Files.createTempDirectory(Path.of(tmpDir), TMP_BASE_DIRNAME_PREFIX).toFile();
                    baseDir.deleteOnExit();
                } catch (IOException e) {
                    throw new IOException("Could not verify the base directory", e);
                }
            }
            try {
                setBasePath(baseDir.getCanonicalPath());
                System.setProperty(BASE_PATH_PROPERTY_NAME, getBasePath());
            } catch (IOException e) {
                throw new IOException("Could not verify the base directory", e);
            }
        } else {
            System.setProperty(BASE_PATH_PROPERTY_NAME, getBasePath());
        }

        // Determines the path of the working directory.
        // If a 'work' directory exists under the base directory,
        // set it as the system property 'aspectran.workPath'.
        File workDir = null;
        String workPath = SystemUtils.getProperty(WORK_PATH_PROPERTY_NAME);
        if (StringUtils.hasText(workPath)) {
            workDir = new File(workPath);
        }
        if (workDir == null || !workDir.isDirectory()) {
            workDir = new File(getBasePath(), "work");
        }
        try {
            System.setProperty(WORK_PATH_PROPERTY_NAME, workDir.getCanonicalPath());
        } catch (Exception e) {
            logger.warn("Could not verify the working directory: " + workDir);
        }

        // Determines the path of the temporary directory.
        // If a 'temp' directory exists under the base directory,
        // set it as the system property 'aspectran.tempPath'.
        File tempDir = null;
        String tempPath = SystemUtils.getProperty(TEMP_PATH_PROPERTY_NAME);
        if (StringUtils.hasText(tempPath)) {
            tempDir = new File(tempPath);
        }
        if (tempDir == null || !tempDir.isDirectory()) {
            tempDir = new File(getBasePath(), "temp");
        }
        try {
            System.setProperty(TEMP_PATH_PROPERTY_NAME, tempDir.getCanonicalPath());
        } catch (Exception e) {
            logger.warn("Could not verify the temporary directory: " + tempDir);
        }
    }

}
