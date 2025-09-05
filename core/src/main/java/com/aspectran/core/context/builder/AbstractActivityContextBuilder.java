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
package com.aspectran.core.context.builder;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.DefaultApplicationAdapter;
import com.aspectran.core.component.aspect.AspectRuleRegistry;
import com.aspectran.core.component.aspect.pointcut.Pointcut;
import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.core.component.bean.BeanRuleAnalyzer;
import com.aspectran.core.component.bean.BeanRuleRegistry;
import com.aspectran.core.component.bean.DefaultBeanRegistry;
import com.aspectran.core.component.bean.async.AsyncTaskExecutor;
import com.aspectran.core.component.bean.async.SimpleAsyncTaskExecutor;
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
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.EnvironmentRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.context.rule.type.AutoReloadType;
import com.aspectran.core.context.rule.validation.AspectRuleValidator;
import com.aspectran.core.context.rule.validation.BeanReferenceException;
import com.aspectran.core.context.rule.validation.BeanReferenceInspector;
import com.aspectran.core.service.CoreService;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.SystemUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static com.aspectran.core.context.config.AspectranConfig.BASE_PATH_PROPERTY_NAME;
import static com.aspectran.core.context.config.AspectranConfig.TEMP_PATH_PROPERTY_NAME;
import static com.aspectran.core.context.config.AspectranConfig.WORK_PATH_PROPERTY_NAME;

/**
 * Abstract base class for {@link ActivityContextBuilder} implementations.
 *
 * <p>This class provides the common functionality for building an {@link com.aspectran.core.context.ActivityContext},
 * including:
 * <ul>
 *   <li>Managing configuration properties (e.g., base path, context rules, profiles).</li>
 *   <li>Handling the creation of a {@link com.aspectran.core.context.resource.SiblingClassLoader}.</li>
 *   <li>Setting up the application's directory structure (base, work, temp).</li>
 *   <li>Orchestrating the creation of the context and its internal components.</li>
 *   <li>Managing the context reloading timer.</li>
 * </ul>
 */
public abstract class AbstractActivityContextBuilder implements ActivityContextBuilder {

    private static final Logger logger = LoggerFactory.getLogger(AbstractActivityContextBuilder.class);

    private static final String TMP_BASE_DIRNAME_PREFIX = "com.aspectran-";

    private final CoreService masterService;

    private String basePath;

    private boolean ownBasePath;

    private ContextConfig contextConfig;

    private AspectranParameters aspectranParameters;

    private String[] contextRules;

    private String encoding;

    private String[] resourceLocations;

    private String[] basePackages;

    private String[] baseProfiles;

    private String[] defaultProfiles;

    private String[] activeProfiles;

    private ItemRuleMap propertyItemRuleMap;

    private ContextReloadingTimer contextReloadingTimer;

    private boolean hardReload;

    private boolean autoReloadEnabled;

    private int scanIntervalSeconds;

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

    public String[] getBaseProfiles() {
        return baseProfiles;
    }

    public void setBaseProfiles(String... baseProfiles) {
        this.baseProfiles = baseProfiles;
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
    public String[] getActiveProfiles() {
        return activeProfiles;
    }

    @Override
    public void setActiveProfiles(String... activeProfiles) {
        this.activeProfiles = activeProfiles;
    }

    @Override
    public void putPropertyItemRule(ItemRule propertyItemRule) {
        if (this.propertyItemRuleMap == null) {
            this.propertyItemRuleMap = new ItemRuleMap();
        }
        this.propertyItemRuleMap.putItemRule(propertyItemRule);
    }

    @Override
    public void configure(@Nullable ContextConfig contextConfig) throws IOException, InvalidResourceException {
        if (this.basePath == null) {
            if (masterService != null) {
                this.basePath = masterService.getBasePath();
            } else if (contextConfig != null) {
                this.basePath = contextConfig.getBasePath();
            }
        }

        if (masterService == null || masterService.isRootService()) {
            checkDirectoryStructure();
        }

        this.contextConfig = contextConfig;
        if (contextConfig != null) {
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
                configure(profilesConfig);
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

    private void configure(@NonNull ContextProfilesConfig profilesConfig) {
        setBaseProfiles(profilesConfig.getBaseProfiles());
        setDefaultProfiles(profilesConfig.getDefaultProfiles());
        setActiveProfiles(profilesConfig.getActiveProfiles());
    }

    @Override
    public SiblingClassLoader getSiblingClassLoader() {
        return siblingClassLoader;
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

    protected EnvironmentProfiles createEnvironmentProfiles(String contextName) {
        EnvironmentProfiles environmentProfiles = new EnvironmentProfiles(contextName);
        if (getBaseProfiles() != null) {
            if (environmentProfiles.hasBaseProfiles()) {
                logger.info("Ignored base profiles [{}]", StringUtils.joinWithCommas(getBaseProfiles()));
            } else {
                environmentProfiles.setBaseProfiles(getBaseProfiles());
            }
        }
        if (getDefaultProfiles() != null) {
            environmentProfiles.setDefaultProfiles(getDefaultProfiles());
        }
        if (getActiveProfiles() != null) {
            environmentProfiles.setActiveProfiles(getActiveProfiles());
        }
        // just for logging
        String[] activeProfiles = environmentProfiles.getActiveProfiles();
        if (activeProfiles.length == 0) {
            String[] defaultProfiles = environmentProfiles.getDefaultProfiles();
            if (defaultProfiles.length > 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("No active profile set, falling back to default profiles: {}",
                            StringUtils.joinWithCommas(defaultProfiles));
                }
            }
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
                assistant.getClassLoader(), assistant.getApplicationAdapter(), masterService);
        if (masterService != null) {
            context.setName(masterService.getContextName());
        } else if (contextConfig != null) {
            context.setName(contextConfig.getName());
        }
        context.setDescriptionRule(assistant.getAssistantLocal().getDescriptionRule());

        ActivityEnvironment activityEnvironment = createActivityEnvironment(context, assistant);
        context.setEnvironment(activityEnvironment);

        AspectRuleRegistry aspectRuleRegistry = assistant.getAspectRuleRegistry();

        if (contextConfig != null && contextConfig.getAsyncConfig() != null && contextConfig.getAsyncConfig().isEnabled()) {
            context.setAsyncTaskExecutor(createDefaultAsyncTaskExecutor(assistant.getClassLoader()));
        }

        BeanRuleRegistry beanRuleRegistry = initBeanRuleRegistry(assistant);
        DefaultBeanRegistry defaultBeanRegistry = new DefaultBeanRegistry(context, beanRuleRegistry);

        ScheduleRuleRegistry scheduleRuleRegistry = assistant.getScheduleRuleRegistry();

        TemplateRuleRegistry templateRuleRegistry = assistant.getTemplateRuleRegistry();
        DefaultTemplateRenderer defaultTemplateRenderer = new DefaultTemplateRenderer(context, templateRuleRegistry);

        TransletRuleRegistry transletRuleRegistry = initTransletRuleRegistry(assistant);

        AspectRuleValidator aspectRuleValidator = new AspectRuleValidator();
        aspectRuleValidator.validate(assistant);

        BeanRuleAnalyzer.clearAdvisableMethodsCache();

        context.setAspectRuleRegistry(aspectRuleRegistry);
        context.setBeanRegistry(defaultBeanRegistry);
        context.setScheduleRuleRegistry(scheduleRuleRegistry);
        context.setTemplateRenderer(defaultTemplateRenderer);
        context.setTransletRuleRegistry(transletRuleRegistry);
        return context;
    }

    @NonNull
    private AsyncTaskExecutor createDefaultAsyncTaskExecutor(ClassLoader classLoader) {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setContextClassLoader(classLoader);
        return asyncTaskExecutor;
    }

    @NonNull
    private ActivityEnvironment createActivityEnvironment(
            ActivityContext context, @NonNull ActivityRuleAssistant assistant) {
        EnvironmentProfiles environmentProfiles = assistant.getEnvironmentProfiles();
        ActivityEnvironmentBuilder builder = new ActivityEnvironmentBuilder()
                .environmentProfiles(environmentProfiles)
                .propertyItemRules(propertyItemRuleMap);
        for (EnvironmentRule environmentRule : assistant.getEnvironmentRules()) {
            if (environmentProfiles.acceptsProfiles(environmentRule.getProfiles())) {
                builder.propertyItemRules(environmentRule.getPropertyItemRuleMap());
            }
        }
        return builder.build(context);
    }

    @NonNull
    private BeanRuleRegistry initBeanRuleRegistry(@NonNull ActivityRuleAssistant assistant)
            throws IllegalRuleException, BeanReferenceException {
        BeanRuleRegistry beanRuleRegistry = assistant.getBeanRuleRegistry();
        beanRuleRegistry.postProcess(assistant);

        BeanRegistry parentBeanRegistry = null;
        if (masterService != null) {
            if (masterService.getParentService() != null) {
                parentBeanRegistry = masterService.getParentService().getActivityContext().getBeanRegistry();
            }
        }

        BeanReferenceInspector beanReferenceInspector = assistant.getBeanReferenceInspector();
        beanReferenceInspector.inspect(beanRuleRegistry, parentBeanRegistry);

        for (BeanRule beanRule : beanRuleRegistry.getConfigurableBeanRules()) {
            BeanRuleAnalyzer.determineProxyBean(beanRule);
        }
        for (BeanRule beanRule : beanRuleRegistry.getIdBasedBeanRules()) {
            BeanRuleAnalyzer.determineProxyBean(beanRule);
        }
        for (Set<BeanRule> beanRules : beanRuleRegistry.getTypeBasedBeanRules()) {
            for (BeanRule beanRule : beanRules) {
                BeanRuleAnalyzer.determineProxyBean(beanRule);
            }
        }

        return beanRuleRegistry;
    }

    private TransletRuleRegistry initTransletRuleRegistry(@NonNull ActivityRuleAssistant assistant) {
        TransletRuleRegistry transletRuleRegistry = assistant.getTransletRuleRegistry();
        AspectRuleRegistry aspectRuleRegistry = assistant.getAspectRuleRegistry();
        for (TransletRule transletRule : transletRuleRegistry.getTransletRules()) {
            if (!transletRule.hasPathVariables()) {
                for (AspectRule aspectRule : aspectRuleRegistry.getAspectRules()) {
                    Pointcut pointcut = aspectRule.getPointcut();
                    if (!aspectRule.isBeanRelevant()) {
                        if (pointcut == null || pointcut.matches(transletRule.getName())) {
                            // register to the translet scope
                            transletRule.touchAdviceRuleRegistry().register(aspectRule);
                        }
                    }
                }
            }
        }
        return transletRuleRegistry;
    }

    protected void startContextReloadingTimer() {
        if (autoReloadEnabled && masterService != null && siblingClassLoader != null) {
            contextReloadingTimer = new ContextReloadingTimer(siblingClassLoader, masterService.getServiceLifeCycle());
            contextReloadingTimer.start(scanIntervalSeconds);
        }
    }

    protected void stopContextReloadingTimer() {
        if (contextReloadingTimer != null) {
            contextReloadingTimer.stop();
            contextReloadingTimer = null;
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

    @Override
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
                    throw new IOException("Could not verify base directory", e);
                }
            }
            try {
                setBasePath(baseDir.getCanonicalPath());
                System.setProperty(BASE_PATH_PROPERTY_NAME, getBasePath());
            } catch (IOException e) {
                throw new IOException("Could not verify base directory", e);
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
            logger.warn("Could not verify working directory: {}", workDir);
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
            logger.warn("Could not verify temporary directory: {}", tempDir);
        }
    }

}
