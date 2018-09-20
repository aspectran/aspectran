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
package com.aspectran.core.support.pebble;

import com.aspectran.core.component.bean.aware.EnvironmentAware;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import com.mitchellbosecke.pebble.loader.DelegatingLoader;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mitchellbosecke.pebble.loader.Loader;
import com.mitchellbosecke.pebble.loader.StringLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Factory that configures a Pebble Engine Configuration.
 *
 * <p>Created: 2016. 1. 9.</p>
 */
public class PebbleEngineFactory implements EnvironmentAware {

    private final Log log = LogFactory.getLog(PebbleEngineFactory.class);

    private Environment environment;

    private Locale defaultLocale;

    private boolean strictVariables;

    private String[] templateLoaderPaths;

    private Loader<?>[] templateLoaders;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = StringUtils.parseLocaleString(defaultLocale);
    }

    public void setStrictVariables(boolean strictVariables) {
        this.strictVariables = strictVariables;
    }

    public void setTemplateLoaderPath(String templateLoaderPath) {
        this.templateLoaderPaths = new String[] { templateLoaderPath };
    }

    public void setTemplateLoaderPath(String[] templateLoaderPaths) {
        this.templateLoaderPaths = templateLoaderPaths;
    }

    public void setTemplateLoaderPath(List<String> templateLoaderPathList) {
        this.templateLoaderPaths = templateLoaderPathList.toArray(new String[0]);
    }

    public void setTemplateLoader(Loader<?> templateLoaders) {
        this.templateLoaders = new Loader<?>[] {templateLoaders};
    }

    public void setTemplateLoader(Loader<?>[] templateLoaders) {
        this.templateLoaders = templateLoaders;
    }

    public void setTemplateLoader(List<Loader<?>> templateLoaderList) {
        this.templateLoaders = templateLoaderList.toArray(new Loader<?>[0]);
    }

    /**
     * Creates a PebbleEngine instance.
     *
     * @return a PebbleEngine object that can be used to create PebbleTemplate objects
     */
    public PebbleEngine createPebbleEngine() {
        PebbleEngine.Builder builder = new PebbleEngine.Builder();
        builder.strictVariables(strictVariables);
        if (defaultLocale != null) {
            builder.defaultLocale(defaultLocale);
        }
        if (templateLoaders == null) {
            if (templateLoaderPaths != null && templateLoaderPaths.length > 0) {
                List<Loader<?>> templateLoaderList = new ArrayList<>();
                for (String path : templateLoaderPaths) {
                    templateLoaderList.add(getTemplateLoaderForPath(path));
                }
                setTemplateLoader(templateLoaderList);
            }
        }
        Loader<?> templateLoader = getAggregateTemplateLoader(templateLoaders);
        builder.loader(templateLoader);
        return builder.build();
    }

    /**
     * Return a Template Loader based on the given Template Loader list.
     * If more than one Template Loader has been registered, a DelegatingLoader needs to be created.
     *
     * @param templateLoaders the final List of TemplateLoader instances
     * @return the aggregate TemplateLoader
     */
    protected Loader<?> getAggregateTemplateLoader(Loader<?>[] templateLoaders) {
        int loaderCount = (templateLoaders == null) ? 0 : templateLoaders.length;
        switch (loaderCount) {
            case 0:
                // Register default template loaders.
                Loader<?> stringLoader = new StringLoader();
                if (log.isDebugEnabled()) {
                    log.debug("Pebble Engine Template Loader not specified. Default Template Loader registered: " + stringLoader);
                }
                return stringLoader;
            case 1:
                if (log.isDebugEnabled()) {
                    log.debug("One Pebble Engine Template Loader registered: " + templateLoaders[0]);
                }
                return templateLoaders[0];
            default:
                List<Loader<?>> defaultLoadingStrategies = new ArrayList<>();
                Collections.addAll(defaultLoadingStrategies, templateLoaders);
                Loader<?> delegatingLoader = new DelegatingLoader(defaultLoadingStrategies);
                if (log.isDebugEnabled()) {
                    log.debug("Multiple Pebble Engine Template Loader registered: " + delegatingLoader);
                }
                return delegatingLoader;
        }
    }

    /**
     * Determine a Pebble Engine Template Loader for the given path.
     *
     * @param templateLoaderPath the path to load templates from
     * @return an appropriate Template Loader
     */
    protected Loader<?> getTemplateLoaderForPath(String templateLoaderPath) {
        if (templateLoaderPath.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            String basePackagePath = templateLoaderPath.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
            if (log.isDebugEnabled()) {
                log.debug("Template loader path [" + templateLoaderPath + "] resolved to class path [" + basePackagePath + "]");
            }
            ClasspathLoader loader = new ClasspathLoader(environment.getClassLoader());
            loader.setPrefix(basePackagePath);
            return loader;
        } else if (templateLoaderPath.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
            File file = new File(templateLoaderPath.substring(ResourceUtils.FILE_URL_PREFIX.length()));
            String prefix = file.getAbsolutePath();
            if (log.isDebugEnabled()) {
                log.debug("Template loader path [" + templateLoaderPath + "] resolved to file path [" + prefix + "]");
            }
            FileLoader loader = new FileLoader();
            loader.setPrefix(prefix);
            return loader;
        } else {
            File file = new File(environment.getBasePath(), templateLoaderPath);
            String prefix = file.getAbsolutePath();
            if (log.isDebugEnabled()) {
                log.debug("Template loader path [" + templateLoaderPath + "] resolved to file path [" + prefix + "]");
            }
            FileLoader loader = new FileLoader();
            loader.setPrefix(prefix);
            return loader;
        }
    }

}
