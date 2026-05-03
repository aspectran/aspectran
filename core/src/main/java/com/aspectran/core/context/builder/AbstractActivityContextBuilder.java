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
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.context.rule.parsing.RuleParsingContext;
import com.aspectran.core.context.rule.type.AutoReloadType;
import com.aspectran.core.context.rule.validation.AspectRuleValidator;
import com.aspectran.core.context.rule.validation.BeanReferenceException;
import com.aspectran.core.context.rule.validation.BeanReferenceInspector;
import com.aspectran.core.service.CoreService;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.SystemUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;

import static com.aspectran.core.context.config.AspectranConfig.ARCHIVED_LOGS_DIR_PROPERTY;
import static com.aspectran.core.context.config.AspectranConfig.BASE_PATH_PROPERTY;
import static com.aspectran.core.context.config.AspectranConfig.COMMANDS_PATH_PROPERTY;
import static com.aspectran.core.context.config.AspectranConfig.ENCRYPTION_ALGORITHM_PROPERTY;
import static com.aspectran.core.context.config.AspectranConfig.ENCRYPTION_PASSWORD_PROPERTY;
import static com.aspectran.core.context.config.AspectranConfig.ENCRYPTION_SALT_PROPERTY;
import static com.aspectran.core.context.config.AspectranConfig.LOGS_DIR_PROPERTY;
import static com.aspectran.core.context.config.AspectranConfig.LOG_CHARSET_PROPERTY;
import static com.aspectran.core.context.config.AspectranConfig.TEMP_PATH_PROPERTY;
import static com.aspectran.core.context.config.AspectranConfig.WORK_PATH_PROPERTY;
import static com.aspectran.core.context.env.EnvironmentProfiles.ACTIVE_PROFILES_PROPERTY;
import static com.aspectran.core.context.env.EnvironmentProfiles.BASE_PROFILES_PROPERTY;
import static com.aspectran.core.context.env.EnvironmentProfiles.DEFAULT_PROFILES_PROPERTY;
import static com.aspectran.utils.ResourceUtils.URL_PROTOCOL_JAR;

/**
 * Abstract base class for {@link ActivityContextBuilder} implementations.
 *
 * <p>This class provides the common functionality for building an {@link ActivityContext},
 * including:
 * <ul>
 *   <li>Managing configuration properties (e.g., base path, context rules, profiles).</li>
 *   <li>Handling the creation of a {@link SiblingClassLoader}.</li>
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

    private SiblingClassLoader siblingClassLoader;

    private ContextReloadingTimer contextReloadingTimer;

    private boolean hardReload;

    private int scanIntervalSeconds;

    private boolean autoReloadEnabled;

    private boolean useAponToLoadXml;

    private boolean debugMode;

    /**
     * Constructs a new instance of {@code AbstractActivityContextBuilder}.
     * Initializes the builder with the provided {@code CoreService}, setting the base path if available,
     * and configuring the behavior of the builder based on system properties.
     * @param masterService the core service to associate with this builder; provides base configurations
     */
    public AbstractActivityContextBuilder(CoreService masterService) {
        this.masterService = masterService;
        if (masterService != null) {
            this.basePath = masterService.getBasePath();
        }
        this.useAponToLoadXml = Boolean.parseBoolean(SystemUtils.getProperty(USE_APON_TO_LOAD_XML_PROPERTY));
        this.debugMode = Boolean.parseBoolean(SystemUtils.getProperty(DEBUG_MODE_PROPERTY));
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

    /**
     * Sets whether this builder has its own base path.
     * This flag is used to determine if the base path is independently configured
     * for the current context.
     * @param ownBasePath a boolean indicating if this builder has its own base path
     */
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

    /**
     * Retrieves the base profiles associated with the current context.
     * @return an array of strings representing the base profile names
     */
    public String[] getBaseProfiles() {
        return baseProfiles;
    }

    /**
     * Sets the base profiles to be associated with the context.
     * Base profiles are used to define the foundation or default configurations
     * that influence the initialization and behavior of the context.
     * @param baseProfiles the array of base profile names to set; each profile represents
     *                     a specific configuration or behavior customization for the context
     */
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

    /**
     * Configures the builder with settings from the provided {@link ContextConfig}.
     * This method extracts properties such as context rules, resource locations, profiles,
     * and auto-reloading settings.
     * @param contextConfig the context configuration object
     * @throws IOException if an I/O error occurs while checking directories
     * @throws InvalidResourceException if any specified resource locations are invalid
     */
    @Override
    public void configure(@Nullable ContextConfig contextConfig) throws IOException, InvalidResourceException {
        if (this.basePath == null && contextConfig != null) {
            this.basePath = contextConfig.getBasePath();
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
                this.hardReload = AutoReloadType.HARD.toString().equals(autoReloadConfig.getReloadMode());
                this.scanIntervalSeconds = autoReloadConfig.getScanIntervalSeconds();
                this.autoReloadEnabled = autoReloadConfig.isEnabled();
            }
            if (this.autoReloadEnabled) {
                if (this.scanIntervalSeconds == -1) {
                    this.scanIntervalSeconds = 10;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Context option 'autoReload' not specified, defaulting to 10 seconds.");
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

    /**
     * Creates a new {@link SiblingClassLoader} or reloads an existing one.
     * If {@code hardReload} is enabled or if the classloader has not been created yet,
     * a new instance is created. Otherwise, the existing classloader is reloaded.
     * In an IDE environment, resource locations are ignored to prevent class loading conflicts.
     * @param contextName the name of the context, used for naming the classloader
     * @param parentClassLoader the parent classloader
     * @return the created or reloaded {@link SiblingClassLoader}
     * @throws InvalidResourceException if any specified resource locations are invalid
     */
    protected SiblingClassLoader createSiblingClassLoader(String contextName, ClassLoader parentClassLoader)
            throws InvalidResourceException {
        if (siblingClassLoader == null || hardReload) {
            String[] resourcesToLoad = resourceLocations;
            if (resourceLocations != null) {
                URL url = ActivityContext.class.getResource("ActivityContext.class");
                if (url != null && !URL_PROTOCOL_JAR.equals(url.getProtocol())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Running in an IDE environment, so the resource locations {} " +
                                "will be ignored to avoid class loading conflicts", Arrays.toString(resourceLocations));
                    }
                    resourcesToLoad = null;
                }
            }
            siblingClassLoader = new SiblingClassLoader(contextName, parentClassLoader, resourcesToLoad);
        } else {
            siblingClassLoader.reload();
        }
        return siblingClassLoader;
    }

    /**
     * Creates and returns an instance of {@link ApplicationAdapter}. This method is protected
     * and is intended to provide a default implementation of {@link ApplicationAdapter} using
     * the current base path. The returned adapter is typically used to facilitate the interaction
     * with the application context, providing necessary configurations and utilities.
     * @return an instance of {@link ApplicationAdapter}, specifically {@link DefaultApplicationAdapter},
     * initialized with the base path.
     */
    protected ApplicationAdapter createApplicationAdapter() {
        return new DefaultApplicationAdapter(basePath);
    }

    /**
     * Creates and initializes an {@code EnvironmentProfiles} instance for the given context name.
     * The method allows setting base, default, and active profiles if they are provided. Logs profile
     * information based on their state.
     * @param contextName the name of the context for which the environment profiles are created
     * @return an initialized {@code EnvironmentProfiles} instance containing the specified
     *         base, default, and active profiles
     */
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
     * Creates and initializes a new {@link ActivityContext} instance based on the parsed rules.
     * @param ruleParsingContext the context containing all parsed configuration rules
     * @return a fully configured {@link ActivityContext}
     * @throws BeanReferenceException if a bean reference cannot be resolved
     * @throws IllegalRuleException if an illegal rule is found during validation
     */
    protected ActivityContext createActivityContext(@NonNull RuleParsingContext ruleParsingContext)
            throws BeanReferenceException, IllegalRuleException {
        DefaultActivityContext context = new DefaultActivityContext(
                ruleParsingContext.getClassLoader(), ruleParsingContext.getApplicationAdapter(), masterService);
        if (masterService != null) {
            context.setName(masterService.getContextName());
        } else if (contextConfig != null) {
            context.setName(contextConfig.getName());
        }
        context.setDescriptionRule(ruleParsingContext.getDescriptionRule());

        ActivityEnvironment activityEnvironment = createActivityEnvironment(context, ruleParsingContext);
        context.setEnvironment(activityEnvironment);

        AspectRuleRegistry aspectRuleRegistry = ruleParsingContext.getAspectRuleRegistry();

        if (contextConfig != null && contextConfig.getAsyncConfig() != null && contextConfig.getAsyncConfig().isEnabled()) {
            context.setAsyncTaskExecutor(createDefaultAsyncTaskExecutor(ruleParsingContext.getClassLoader()));
        }

        BeanRuleRegistry beanRuleRegistry = initBeanRuleRegistry(ruleParsingContext);
        DefaultBeanRegistry defaultBeanRegistry = new DefaultBeanRegistry(context, beanRuleRegistry);

        ScheduleRuleRegistry scheduleRuleRegistry = ruleParsingContext.getScheduleRuleRegistry();

        TemplateRuleRegistry templateRuleRegistry = ruleParsingContext.getTemplateRuleRegistry();
        DefaultTemplateRenderer defaultTemplateRenderer = new DefaultTemplateRenderer(context, templateRuleRegistry);

        TransletRuleRegistry transletRuleRegistry = initTransletRuleRegistry(ruleParsingContext);

        AspectRuleValidator aspectRuleValidator = new AspectRuleValidator();
        aspectRuleValidator.validate(ruleParsingContext);

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
            ActivityContext context, @NonNull RuleParsingContext ruleParsingContext) {
        EnvironmentProfiles environmentProfiles = ruleParsingContext.getEnvironmentProfiles();
        ActivityEnvironmentBuilder builder = new ActivityEnvironmentBuilder()
                .environmentProfiles(environmentProfiles)
                .propertyItemRules(propertyItemRuleMap);
        for (EnvironmentRule environmentRule : ruleParsingContext.getEnvironmentRules()) {
            if (environmentProfiles.acceptsProfiles(environmentRule.getProfiles())) {
                builder.propertyItemRules(environmentRule.getPropertyItemRuleMap());
            }
        }
        return builder.build(context);
    }

    @NonNull
    private BeanRuleRegistry initBeanRuleRegistry(@NonNull RuleParsingContext ruleParsingContext)
            throws IllegalRuleException, BeanReferenceException {
        BeanRuleRegistry beanRuleRegistry = ruleParsingContext.getBeanRuleRegistry();
        beanRuleRegistry.postProcess(ruleParsingContext);

        BeanRegistry parentBeanRegistry = null;
        if (masterService != null) {
            if (masterService.getParentService() != null) {
                parentBeanRegistry = masterService.getParentService().getActivityContext().getBeanRegistry();
            }
        }

        BeanReferenceInspector beanReferenceInspector = ruleParsingContext.getBeanReferenceInspector();
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

    private TransletRuleRegistry initTransletRuleRegistry(@NonNull RuleParsingContext ruleParsingContext) {
        TransletRuleRegistry transletRuleRegistry = ruleParsingContext.getTransletRuleRegistry();
        AspectRuleRegistry aspectRuleRegistry = ruleParsingContext.getAspectRuleRegistry();
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

    /**
     * Starts the context reloading timer if auto-reloading is enabled and a master service is present.
     * The timer monitors all classpath resources and specified rule files for modifications.
     * @param ruleFiles the list of rule files to monitor, including those appended during parsing
     */
    protected void startContextReloadingTimer(Iterable<File> ruleFiles) {
        if (autoReloadEnabled && masterService != null) {
            if (scanIntervalSeconds > 0) {
                // The restart command must be delivered to the root service to safely reload the entire application.
                contextReloadingTimer = new ContextReloadingTimer(
                        masterService.getRootService().getServiceLifeCycle(),
                        scanIntervalSeconds);
                if (siblingClassLoader != null) {
                    contextReloadingTimer.setResources(siblingClassLoader.getAllResources());
                }
                if (ruleFiles != null) {
                    for (File file : ruleFiles) {
                        contextReloadingTimer.addResource(file);
                    }
                }
                if (contextReloadingTimer.hasResources()) {
                    contextReloadingTimer.start();
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Context auto-reloading is enabled, but no resources to monitor for reloading. " +
                                "The reloading timer will not be started.");
                    }
                }
            } else {
                logger.warn("Context auto-reloading is enabled, but 'scanIntervalSeconds' is not a positive " +
                        "value; Was {}. The reloading timer will not be started.", scanIntervalSeconds);
            }
        }
    }

    /**
     * Stops and nullifies the context reloading timer if it is currently active.
     * <p>This method checks if the context reloading timer is not null, then
     * stops its operation and sets the timer reference to null to release
     * resources and prevent further use.</p>
     */
    protected void stopContextReloadingTimer() {
        if (contextReloadingTimer != null) {
            contextReloadingTimer.stop();
            contextReloadingTimer = null;
        }
    }

    /**
     * Returns whether the reloading timer is currently running.
     * @return true if the timer is running, otherwise false
     */
    public boolean isReloadingTimerRunning() {
        return (contextReloadingTimer != null);
    }

    public boolean isHardReload() {
        return hardReload;
    }

    public void setHardReload(boolean hardReload) {
        this.hardReload = hardReload;
    }

    public int getScanIntervalSeconds() {
        return scanIntervalSeconds;
    }

    public void setScanIntervalSeconds(int scanIntervalSeconds) {
        this.scanIntervalSeconds = scanIntervalSeconds;
    }

    public boolean isAutoReloadEnabled() {
        return autoReloadEnabled;
    }

    public void setAutoReloadEnabled(boolean autoReloadEnabled) {
        this.autoReloadEnabled = autoReloadEnabled;
    }

    protected boolean isUseAponToLoadXml() {
        return useAponToLoadXml;
    }

    public void setUseAponToLoadXml(boolean useAponToLoadXml) {
        this.useAponToLoadXml = useAponToLoadXml;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    @Override
    public void clear() {
        SystemUtils.clearProperty(BASE_PATH_PROPERTY);
        SystemUtils.clearProperty(WORK_PATH_PROPERTY);
        SystemUtils.clearProperty(TEMP_PATH_PROPERTY);
        SystemUtils.clearProperty(COMMANDS_PATH_PROPERTY);
        SystemUtils.clearProperty(LOGS_DIR_PROPERTY);
        SystemUtils.clearProperty(ARCHIVED_LOGS_DIR_PROPERTY);
        SystemUtils.clearProperty(LOG_CHARSET_PROPERTY);
        SystemUtils.clearProperty(ENCRYPTION_ALGORITHM_PROPERTY);
        SystemUtils.clearProperty(ENCRYPTION_PASSWORD_PROPERTY);
        SystemUtils.clearProperty(ENCRYPTION_SALT_PROPERTY);
        SystemUtils.clearProperty(BASE_PROFILES_PROPERTY);
        SystemUtils.clearProperty(DEFAULT_PROFILES_PROPERTY);
        SystemUtils.clearProperty(ACTIVE_PROFILES_PROPERTY);
        SystemUtils.clearProperty(DEBUG_MODE_PROPERTY);
        SystemUtils.clearProperty(USE_APON_TO_LOAD_XML_PROPERTY);
    }

    private void checkDirectoryStructure() throws IOException {
        setOwnBasePath(true);

        // Determines the path of the base directory
        if (getBasePath() == null) {
            String basePath = SystemUtils.getProperty(BASE_PATH_PROPERTY);
            File baseDir;
            if (StringUtils.hasText(basePath)) {
                baseDir = new File(basePath);
                if (!baseDir.isDirectory()) {
                    throw new IOException("Make sure it is a valid base directory; " +
                        BASE_PATH_PROPERTY + "=" + basePath);
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
                System.setProperty(BASE_PATH_PROPERTY, getBasePath());
            } catch (IOException e) {
                throw new IOException("Could not verify base directory", e);
            }
        } else {
            System.setProperty(BASE_PATH_PROPERTY, getBasePath());
        }

        // Determines the path of the working directory
        File workDir = null;
        String workPath = SystemUtils.getProperty(WORK_PATH_PROPERTY);
        if (StringUtils.hasText(workPath)) {
            workDir = new File(workPath);
        }
        if (workDir == null) {
            workDir = new File(getBasePath(), "work");
        }
        if (hasOwnBasePath() && !workDir.isDirectory()) {
            if (workDir.mkdirs()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Created working directory: {}", workDir);
                }
            } else if (!workDir.isDirectory()) {
                workDir = new File(getBasePath(), "work");
                if (!workDir.isDirectory()) {
                    workDir.mkdirs();
                }
            }
        }
        try {
            System.setProperty(WORK_PATH_PROPERTY, workDir.getCanonicalPath());
        } catch (Exception e) {
            logger.warn("Could not verify working directory: {}", workDir);
        }

        // Determines the path of the temporary directory
        File tempDir = null;
        String tempPath = SystemUtils.getProperty(TEMP_PATH_PROPERTY);
        if (StringUtils.hasText(tempPath)) {
            tempDir = new File(tempPath);
        }
        if (tempDir == null) {
            tempDir = new File(getBasePath(), "temp");
        }
        if (hasOwnBasePath() && !tempDir.isDirectory()) {
            if (tempDir.mkdirs()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Created temporary directory: {}", tempDir);
                }
            } else if (!tempDir.isDirectory()) {
                tempDir = new File(getBasePath(), "temp");
                if (!tempDir.isDirectory()) {
                    tempDir.mkdirs();
                }
            }
        }
        try {
            System.setProperty(TEMP_PATH_PROPERTY, tempDir.getCanonicalPath());
        } catch (Exception e) {
            logger.warn("Could not verify temporary directory: {}", tempDir);
        }

        // Determines the path of the commands directory
        File cmdDir = null;
        String cmdPath = SystemUtils.getProperty(COMMANDS_PATH_PROPERTY);
        if (StringUtils.hasText(cmdPath)) {
            cmdDir = new File(cmdPath);
        }
        if (cmdDir == null) {
            cmdDir = new File(getBasePath(), "cmd");
        }
        if (hasOwnBasePath() && !cmdDir.isDirectory()) {
            if (cmdDir.mkdirs()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Created commands directory: {}", cmdDir);
                }
            } else if (!cmdDir.isDirectory()) {
                cmdDir = new File(getBasePath(), "cmd");
                if (!cmdDir.isDirectory()) {
                    cmdDir.mkdirs();
                }
            }
        }
        try {
            System.setProperty(COMMANDS_PATH_PROPERTY, cmdDir.getCanonicalPath());
        } catch (Exception e) {
            logger.warn("Could not verify commands directory: {}", cmdDir);
        }

        // Determines the path of the logs directory
        File logsDir = null;
        String logsPath = SystemUtils.getProperty(LOGS_DIR_PROPERTY);
        if (StringUtils.hasText(logsPath)) {
            logsDir = new File(logsPath);
        }
        if (logsDir == null) {
            logsDir = new File(getBasePath(), "logs");
        }
        if (hasOwnBasePath() && !logsDir.isDirectory()) {
            if (logsDir.mkdirs()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Created logs directory: {}", logsDir);
                }
            } else if (!logsDir.isDirectory()) {
                logsDir = new File(getBasePath(), "logs");
                if (!logsDir.isDirectory()) {
                    logsDir.mkdirs();
                }
            }
        }
        try {
            System.setProperty(LOGS_DIR_PROPERTY, logsDir.getCanonicalPath());
        } catch (Exception e) {
            logger.warn("Could not verify logs directory: {}", logsDir);
        }

        // Determines the path of the archived logs directory
        File archivedLogsDir = null;
        String archivedLogsPath = SystemUtils.getProperty(ARCHIVED_LOGS_DIR_PROPERTY);
        if (StringUtils.hasText(archivedLogsPath)) {
            archivedLogsDir = new File(archivedLogsPath);
            if (hasOwnBasePath() && !archivedLogsDir.isDirectory()) {
                if (archivedLogsDir.mkdirs()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Created archived logs directory: {}", archivedLogsDir);
                    }
                } else if (!archivedLogsDir.isDirectory()) {
                    logger.warn("Could not create archived logs directory: {}", archivedLogsDir);
                }
            }
            try {
                System.setProperty(ARCHIVED_LOGS_DIR_PROPERTY, archivedLogsDir.getCanonicalPath());
            } catch (Exception e) {
                logger.warn("Could not verify archived logs directory: {}", archivedLogsDir);
            }
        }
    }

}
