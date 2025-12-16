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
package com.aspectran.freemarker;

import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.freemarker.directive.CustomTrimDirective;
import com.aspectran.freemarker.directive.TrimDirective;
import com.aspectran.freemarker.directive.TrimDirectiveGroup;
import com.aspectran.freemarker.directive.Trimmer;
import com.aspectran.utils.PropertiesLoaderUtils;
import com.aspectran.utils.ResourceUtils;
import com.aspectran.utils.apon.Parameters;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateException;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * A factory that creates and configures a FreeMarker {@link Configuration} object.
 * <p>This class encapsulates the logic for setting FreeMarker properties, template loaders,
 * shared variables, and custom directives. It is typically used by {@link ConfigurationFactoryBean}
 * to produce a fully configured {@code Configuration} instance for the application.</p>
 *
 * <p>Created: 2016. 1. 9.</p>
 */
public class ConfigurationFactory implements ActivityContextAware {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationFactory.class);

    private static final String DIRECTIVE_NAME_PARAM_NAME = "name";

    private ActivityContext context;

    private String configLocation;

    private Properties freemarkerSettings;

    private Map<String, Object> freemarkerVariables;

    private String defaultEncoding;

    private String[] templateLoaderPaths;

    private TemplateLoader[] templateLoaders;

    private TrimDirective[] trimDirectives;

    @Override
    public void setActivityContext(@NonNull ActivityContext context) {
        this.context = context;
    }

    /**
     * Sets the location of the FreeMarker settings file (e.g., "classpath:freemarker.properties").
     * @param configLocation the location of the configuration file
     */
    public void setConfigLocation(String configLocation) {
        this.configLocation = configLocation;
    }

    /**
     * Sets FreeMarker properties directly.
     * These properties will be passed to FreeMarker's {@code Configuration.setSettings} method.
     * @param settings the FreeMarker settings properties
     */
    public void setFreemarkerSettings(Properties settings) {
        this.freemarkerSettings = settings;
    }

    /**
     * Sets shared variables that will be available to all templates.
     * @param variables a map of shared variables
     */
    public void setFreemarkerVariables(Map<String, Object> variables) {
        this.freemarkerVariables = variables;
    }

    /**
     * Sets the default encoding for templates.
     * @param defaultEncoding the default encoding
     */
    public void setDefaultEncoding(String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    /**
     * Sets a single path for the FreeMarker template loader.
     * @param templateLoaderPath the template loader path (e.g., "/WEB-INF/templates/")
     */
    public void setTemplateLoaderPath(String templateLoaderPath) {
        this.templateLoaderPaths = new String[] { templateLoaderPath };
    }

    /**
     * Sets multiple paths for the FreeMarker template loader.
     * @param templateLoaderPaths an array of template loader paths
     */
    public void setTemplateLoaderPath(String... templateLoaderPaths) {
        this.templateLoaderPaths = templateLoaderPaths;
    }

    /**
     * Sets a list of paths for the FreeMarker template loader.
     * @param templateLoaderPathList a list of template loader paths
     */
    public void setTemplateLoaderPath(@NonNull List<String> templateLoaderPathList) {
        this.templateLoaderPaths = templateLoaderPathList.toArray(new String[0]);
    }

    /**
     * Sets a single, pre-configured {@link TemplateLoader}.
     * @param templateLoader the template loader
     */
    public void setTemplateLoader(TemplateLoader templateLoader) {
        this.templateLoaders = new TemplateLoader[] { templateLoader };
    }

    /**
     * Sets multiple, pre-configured {@link TemplateLoader}s.
     * @param templateLoaders an array of template loaders
     */
    public void setTemplateLoader(TemplateLoader... templateLoaders) {
        this.templateLoaders = templateLoaders;
    }

    /**
     * Sets a list of pre-configured {@link TemplateLoader}s.
     * @param templateLoaderList a list of template loaders
     */
    public void setTemplateLoader(@NonNull List<TemplateLoader> templateLoaderList) {
        this.templateLoaders = templateLoaderList.toArray(new TemplateLoader[0]);
    }

    /**
     * Sets the custom trim directives.
     * @param trimDirectives an array of trim directives
     */
    public void setTrimDirectives(TrimDirective... trimDirectives) {
        this.trimDirectives = trimDirectives;
    }

    /**
     * Sets the custom trim directives from a parameter map.
     * @param parameters the parameters containing trim directive configurations
     */
    public void setTrimDirectives(@NonNull Parameters parameters) {
        String[] directiveGroupNames = parameters.getParameterNames();
        List<TrimDirective> list = new ArrayList<>();

        for (String groupName : directiveGroupNames) {
            List<Parameters> paramsList = parameters.getParametersList(groupName);
            for (Parameters p : paramsList) {
                if (p != null) {
                    String directiveName = p.getString(DIRECTIVE_NAME_PARAM_NAME);
                    String prefix = p.getString(TrimDirective.PREFIX_PARAM_NAME);
                    String suffix = p.getString(TrimDirective.SUFFIX_PARAM_NAME);
                    String[] deprefixes = p.getStringArray(TrimDirective.DEPREFIXES_PARAM_NAME);
                    String[] desuffixes = p.getStringArray(TrimDirective.DESUFFIXES_PARAM_NAME);
                    boolean caseSensitive = Boolean.parseBoolean(p.getString(TrimDirective.CASE_SENSITIVE_PARAM_NAME));

                    if (directiveName != null) {
                        Trimmer trimmer;
                        if (prefix != null || suffix != null || deprefixes != null || desuffixes != null) {
                            trimmer = new Trimmer();
                            trimmer.setPrefix(prefix);
                            trimmer.setSuffix(suffix);
                            trimmer.setDeprefixes(deprefixes);
                            trimmer.setDesuffixes(desuffixes);
                            trimmer.setCaseSensitive(caseSensitive);
                        } else {
                            trimmer = null;
                        }

                        TrimDirective ctd = new CustomTrimDirective(groupName, directiveName, trimmer);
                        list.add(ctd);

                        if (logger.isDebugEnabled()) {
                            logger.debug("CustomTrimDirective {}", ctd);
                        }
                    }
                }
            }
        }

        if (!list.isEmpty()) {
            trimDirectives = list.toArray(new TrimDirective[0]);
        } else {
            trimDirectives = null;
        }
    }

    /**
     * Creates and returns a fully configured FreeMarker {@link Configuration} object.
     * @return the configured FreeMarker Configuration
     * @throws IOException if a configuration file cannot be loaded
     * @throws TemplateException if FreeMarker initialization fails
     */
    public Configuration createConfiguration() throws IOException, TemplateException {
        Configuration config = newConfiguration();
        Properties props = new Properties();

        // Load config file if set.
        if (this.configLocation != null) {
            logger.info("Loading Freemarker settings from [{}]", this.configLocation);
            props.putAll(PropertiesLoaderUtils.loadProperties(this.configLocation));
        }

        // Merge local properties if specified.
        if (this.freemarkerSettings != null) {
            props.putAll(this.freemarkerSettings);
        }

        // FreeMarker will only accept known keys in its setSettings and
        // setAllSharedVariables methods.
        if (!props.isEmpty()) {
            config.setSettings(props);
        }

        if (this.freemarkerVariables != null && !freemarkerVariables.isEmpty()) {
            config.setAllSharedVariables(new SimpleHash(this.freemarkerVariables, config.getObjectWrapper()));
        }

        if (this.defaultEncoding != null) {
            config.setDefaultEncoding(this.defaultEncoding);
        }

        // determine FreeMarker TemplateLoader
        if (templateLoaders == null) {
            if (templateLoaderPaths != null && templateLoaderPaths.length > 0) {
                List<TemplateLoader> templateLoaderList = new ArrayList<>();
                for (String path : templateLoaderPaths) {
                    templateLoaderList.add(getTemplateLoaderForPath(path));
                }
                setTemplateLoader(templateLoaderList);
            }
        }

        TemplateLoader templateLoader = getAggregateTemplateLoader(templateLoaders);
        if (templateLoader != null) {
            config.setTemplateLoader(templateLoader);
        }

        // determine CustomTrimDirectives
        if (trimDirectives != null && trimDirectives.length > 0) {
            TrimDirectiveGroup group = new TrimDirectiveGroup(trimDirectives);
            for (Map.Entry<String, Map<String, TrimDirective>> directives : group.entrySet()) {
                config.setSharedVariable(directives.getKey(), directives.getValue());
            }
        }

        return config;
    }

    /**
     * Returns a new {@link Configuration} object. Subclasses can override this for custom
     * initialization, such as setting a specific FreeMarker compatibility level.
     * @return the new Configuration object
     */
    protected Configuration newConfiguration() {
        return new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
    }

    /**
     * Aggregates multiple {@link TemplateLoader} instances into a single loader.
     * If more than one loader is provided, a {@link MultiTemplateLoader} is used.
     * @param templateLoaders the array of TemplateLoader instances
     * @return the aggregate TemplateLoader, or {@code null} if none are provided
     */
    protected TemplateLoader getAggregateTemplateLoader(TemplateLoader[] templateLoaders) {
        int loaderCount = (templateLoaders != null ? templateLoaders.length : 0);
        switch (loaderCount) {
            case 0:
                if (logger.isDebugEnabled()) {
                    logger.debug("No FreeMarker TemplateLoaders specified; Can be used only inner template source");
                }
                return null;
            case 1:
                if (logger.isDebugEnabled()) {
                    logger.debug("One FreeMarker TemplateLoader registered: {}", templateLoaders[0]);
                }
                return templateLoaders[0];
            default:
                TemplateLoader loader = new MultiTemplateLoader(templateLoaders);
                if (logger.isDebugEnabled()) {
                    logger.debug("Multiple FreeMarker TemplateLoader registered: {}", loader);
                }
                return loader;
        }
    }

    /**
     * Creates a {@link TemplateLoader} for a given path, which can be a classpath or file system path.
     * @param templateLoaderPath the path to load templates from (e.g., "classpath:/templates", "/WEB-INF/freemarker")
     * @return an appropriate {@code TemplateLoader}
     * @throws IOException if an I/O error occurs
     */
    protected TemplateLoader getTemplateLoaderForPath(@NonNull String templateLoaderPath) throws IOException {
        if (templateLoaderPath.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            String basePackagePath = templateLoaderPath.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
            if (logger.isDebugEnabled()) {
                logger.debug("Template loader path [{}] resolved to class path [{}]",
                        templateLoaderPath, basePackagePath);
            }
            return new ClassTemplateLoader(context.getAvailableActivity().getClassLoader(), basePackagePath);
        } else if (templateLoaderPath.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
            File file = new File(templateLoaderPath.substring(ResourceUtils.FILE_URL_PREFIX.length()));
            if (logger.isDebugEnabled()) {
                logger.debug("Template loader path [{}] resolved to file path [{}]",
                        templateLoaderPath, file.getAbsolutePath());
            }
            return new FileTemplateLoader(file);
        } else {
            File file = new File(context.getApplicationAdapter().getBasePathString(), templateLoaderPath);
            if (logger.isDebugEnabled()) {
                logger.debug("Template loader path [{}] resolved to file path [{}]",
                        templateLoaderPath, file.getAbsolutePath());
            }
            return new FileTemplateLoader(file);
        }
    }

}
