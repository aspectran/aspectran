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
package com.aspectran.core.support.freemarker;

import com.aspectran.core.component.bean.aware.EnvironmentAware;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.support.freemarker.directive.CustomTrimDirective;
import com.aspectran.core.support.freemarker.directive.TrimDirective;
import com.aspectran.core.support.freemarker.directive.TrimDirectiveGroup;
import com.aspectran.core.support.freemarker.directive.Trimmer;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Factory that configures a FreeMarker Configuration.
 *
 * <p>Created: 2016. 1. 9.</p>
 */
public class FreeMarkerConfigurationFactory implements EnvironmentAware {

    private final Log log = LogFactory.getLog(FreeMarkerConfigurationFactory.class);

    private static final String DIRECTIVE_NAME_PARAM_NAME = "name";

    private Environment environment;

    private Properties freemarkerSettings;

    private Map<String, Object> freemarkerVariables;

    private String defaultEncoding;

    private String[] templateLoaderPaths;

    private TemplateLoader[] templateLoaders;

    private TrimDirective[] trimDirectives;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * Set properties that contain well-known FreeMarker keys which will be
     * passed to FreeMarker's {@code Configuration.setSettings} method.
     *
     * @param settings the settings
     * @see freemarker.template.Configuration#setSettings freemarker.template.Configuration#setSettings
     */
    public void setFreemarkerSettings(Properties settings) {
        this.freemarkerSettings = settings;
    }

    /**
     * Set a Map that contains well-known FreeMarker objects which will be passed
     * to FreeMarker's {@code Configuration.setAllSharedVariables()} method.
     *
     * @param variables the variables
     * @see freemarker.template.Configuration#setAllSharedVariables freemarker.template.Configuration#setAllSharedVariables
     */
    public void setFreemarkerVariables(Map<String, Object> variables) {
        this.freemarkerVariables = variables;
    }

    /**
     * Set the default encoding for the FreeMarker configuration.
     * If not specified, FreeMarker will use the platform file encoding.
     * <p>Used for template rendering unless there is an explicit encoding specified
     * for the rendering process (for example, on Spring's FreeMarkerView).
     *
     * @param defaultEncoding the default encoding
     * @see freemarker.template.Configuration#setDefaultEncoding freemarker.template.Configuration#setDefaultEncoding
     */
    public void setDefaultEncoding(String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    /**
     * Set the Freemarker template loader path.
     *
     * @param templateLoaderPath the Freemarker template loader path
     */
    public void setTemplateLoaderPath(String templateLoaderPath) {
        this.templateLoaderPaths = new String[] { templateLoaderPath };
    }

    /**
     * Set multiple Freemarker template loader paths.
     *
     * @param templateLoaderPaths the multiple Freemarker template loader paths
     */
    public void setTemplateLoaderPath(String... templateLoaderPaths) {
        this.templateLoaderPaths = templateLoaderPaths;
    }

    /**
     * Set a List of Freemarker template loader paths.
     *
     * @param templateLoaderPathList a List of Freemarker template loader paths
     */
    public void setTemplateLoaderPath(List<String> templateLoaderPathList) {
        this.templateLoaderPaths = templateLoaderPathList.toArray(new String[0]);
    }

    /**
     * Set a TemplateLoader that will be used to search for templates.
     *
     * @param templateLoader the template loader
     */
    public void setTemplateLoader(TemplateLoader templateLoader) {
        this.templateLoaders = new TemplateLoader[] { templateLoader };
    }

    /**
     * Set multiple TemplateLoaders that will be used to search for templates.
     *
     * @param templateLoaders the multiple TemplateLoaders
     */
    public void setTemplateLoader(TemplateLoader... templateLoaders) {
        this.templateLoaders = templateLoaders;
    }

    /**
     * Set a List of TemplateLoaders that will be used to search for templates.
     *
     * @param templateLoaderList a List of TemplateLoaders
     */
    public void setTemplateLoader(List<TemplateLoader> templateLoaderList) {
        this.templateLoaders = templateLoaderList.toArray(new TemplateLoader[0]);
    }

    public void setTrimDirectives(TrimDirective... trimDirectives) {
        this.trimDirectives = trimDirectives;
    }

    public void setTrimDirectives(Parameters parameters) {
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
                    Boolean caseSensitive = Boolean.valueOf(p.getString(TrimDirective.CASE_SENSITIVE_PARAM_NAME));

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

                        if (log.isDebugEnabled()) {
                            log.debug("CustomTrimDirective " + ctd);
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
     * Prepare the FreeMarker Configuration and return it.
     *
     * @return the FreeMarker Configuration object
     * @throws IOException if the config file wasn't found
     * @throws TemplateException on FreeMarker initialization failure
     */
    public Configuration createConfiguration() throws IOException, TemplateException {
        Configuration config = newConfiguration();
        Properties props = new Properties();

        // Merge local properties if specified.
        if (this.freemarkerSettings != null) {
            props.putAll(this.freemarkerSettings);
        }

        // FreeMarker will only accept known keys in its setSettings and
        // setAllSharedVariables methods.
        if (!props.isEmpty()) {
            config.setSettings(props);
        }

        if (this.freemarkerVariables != null && freemarkerVariables.size() > 0) {
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
     * Return a new Configuration object. Subclasses can override this for custom
     * initialization (e.g. specifying a FreeMarker compatibility level which is a
     * new feature in FreeMarker 2.3.21), or for using a mock object for testing.
     * <p>Called by {@code createConfiguration()}.</p>
     *
     * @return the Configuration object
     */
    protected Configuration newConfiguration() {
        return new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
    }

    /**
     * Return a TemplateLoader based on the given TemplateLoader list.
     * If more than one TemplateLoader has been registered, a FreeMarker
     * MultiTemplateLoader needs to be created.
     *
     * @param templateLoaders the final List of TemplateLoader instances
     * @return the aggregate TemplateLoader
     */
    protected TemplateLoader getAggregateTemplateLoader(TemplateLoader[] templateLoaders) {
        int loaderCount = (templateLoaders != null ? templateLoaders.length : 0);
        switch (loaderCount) {
            case 0:
                if (log.isDebugEnabled()) {
                    log.debug("No FreeMarker TemplateLoaders specified. Can be used only inner template source");
                }
                return null;
            case 1:
                if (log.isDebugEnabled()) {
                    log.debug("One FreeMarker TemplateLoader registered: " + templateLoaders[0]);
                }
                return templateLoaders[0];
            default:
                TemplateLoader loader = new MultiTemplateLoader(templateLoaders);
                if (log.isDebugEnabled()) {
                    log.debug("Multiple FreeMarker TemplateLoader registered: " + loader);
                }
                return loader;
        }
    }

    /**
     * Determine a FreeMarker TemplateLoader for the given path.
     *
     * @param templateLoaderPath the path to load templates from
     * @return an appropriate TemplateLoader
     * @throws IOException if an I/O error has occurred
     * @see freemarker.cache.FileTemplateLoader
     */
    protected TemplateLoader getTemplateLoaderForPath(String templateLoaderPath) throws IOException {
        if (templateLoaderPath.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            String basePackagePath = templateLoaderPath.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
            if (log.isDebugEnabled()) {
                log.debug("Template loader path [" + templateLoaderPath + "] resolved to class path [" + basePackagePath + "]");
            }
            return new ClassTemplateLoader(environment.getClassLoader(), basePackagePath);
        } else if (templateLoaderPath.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
            File file = new File(templateLoaderPath.substring(ResourceUtils.FILE_URL_PREFIX.length()));
            if (log.isDebugEnabled()) {
                log.debug("Template loader path [" + templateLoaderPath + "] resolved to file path [" + file.getAbsolutePath() + "]");
            }
            return new FileTemplateLoader(file);
        } else {
            File file = new File(environment.getBasePath(), templateLoaderPath);
            if (log.isDebugEnabled()) {
                log.debug("Template loader path [" + templateLoaderPath + "] resolved to file path [" + file.getAbsolutePath() + "]");
            }
            return new FileTemplateLoader(file);
        }
    }

}
